package dev.slimevr;

import com.jme3.system.NanoTimer;
import dev.slimevr.autobone.AutoBoneHandler;
import dev.slimevr.bridge.Bridge;
import dev.slimevr.bridge.VMCBridge;
import dev.slimevr.config.ConfigManager;
import dev.slimevr.osc.OSCHandler;
import dev.slimevr.osc.OSCRouter;
import dev.slimevr.osc.VRCOSCHandler;
import dev.slimevr.platform.SteamVRBridge;
import dev.slimevr.platform.linux.UnixSocketBridge;
import dev.slimevr.platform.windows.WindowsNamedPipeBridge;
import dev.slimevr.poserecorder.BVHRecorder;
import dev.slimevr.protocol.ProtocolAPI;
import dev.slimevr.serial.SerialHandler;
import dev.slimevr.util.ann.VRServerThread;
import dev.slimevr.vr.DeviceManager;
import dev.slimevr.vr.processor.HumanPoseProcessor;
import dev.slimevr.vr.processor.skeleton.Skeleton;
import dev.slimevr.vr.trackers.HMDTracker;
import dev.slimevr.vr.trackers.ShareableTracker;
import dev.slimevr.vr.trackers.Tracker;
import dev.slimevr.vr.trackers.udp.TrackersUDPServer;
import dev.slimevr.websocketapi.WebSocketVRBridge;
import io.eiren.util.OperatingSystem;
import io.eiren.util.ann.ThreadSafe;
import io.eiren.util.ann.ThreadSecure;
import io.eiren.util.collections.FastList;
import io.eiren.util.logging.LogManager;
import solarxr_protocol.datatypes.TrackerId;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;


public class VRServer extends Thread {

	public final HumanPoseProcessor humanPoseProcessor;
	public final HMDTracker hmdTracker;
	private final List<Tracker> trackers = new FastList<>();
	private final TrackersUDPServer trackersServer;
	private final List<Bridge> bridges = new FastList<>();
	private final Queue<Runnable> tasks = new LinkedBlockingQueue<>();
	private final List<Consumer<Tracker>> newTrackersConsumers = new FastList<>();
	private final List<Runnable> onTick = new FastList<>();
	private final List<? extends ShareableTracker> shareTrackers;
	private final OSCRouter oscRouter;
	private final VRCOSCHandler vrcOSCHandler;
	private final DeviceManager deviceManager;
	private final BVHRecorder bvhRecorder;
	private final SerialHandler serialHandler;
	private final AutoBoneHandler autoBoneHandler;
	private final ProtocolAPI protocolAPI;
	private final ConfigManager configManager;
	private final Timer timer = new Timer();
	private final NanoTimer fpsTimer = new NanoTimer();

	/**
	 * This function is used by VRWorkout, do not remove!
	 */
	public VRServer() {
		this("vrconfig.yml");
	}

	public VRServer(String configPath) {
		super("VRServer");

		this.configManager = new ConfigManager(configPath);
		this.configManager.loadConfig();

		deviceManager = new DeviceManager(this);

		serialHandler = new SerialHandler();
		autoBoneHandler = new AutoBoneHandler(this);
		protocolAPI = new ProtocolAPI(this);

		hmdTracker = new HMDTracker("HMD");
		hmdTracker.position.set(0, 1.8f, 0); // Set starting position for easier
												// debugging
		// TODO Multiple processors
		humanPoseProcessor = new HumanPoseProcessor(this);
		shareTrackers = humanPoseProcessor.getComputedTrackers();

		// Start server for SlimeVR trackers
		int trackerPort = configManager.getVrConfig().getServer().getTrackerPort();
		LogManager.info("Starting the tracker server on port " + trackerPort + "...");
		trackersServer = new TrackersUDPServer(
			trackerPort,
			"Sensors UDP server",
			this::registerTracker
		);

		// OpenVR bridge currently only supports Windows
		final SteamVRBridge driverBridge;
		if (OperatingSystem.getCurrentPlatform() == OperatingSystem.WINDOWS) {

			// Create named pipe bridge for SteamVR driver
			driverBridge = new WindowsNamedPipeBridge(
				this,
				hmdTracker,
				"steamvr",
				"SteamVR Driver Bridge",
				"\\\\.\\pipe\\SlimeVRDriver",
				shareTrackers
			);
			tasks.add(driverBridge::startBridge);
			bridges.add(driverBridge);

			// Create named pipe bridge for SteamVR input
			// TODO: how do we want to handle HMD input from the feeder app?
			WindowsNamedPipeBridge feederBridge = new WindowsNamedPipeBridge(
				this,
				null,
				"steamvr_feeder",
				"SteamVR Feeder Bridge",
				"\\\\.\\pipe\\SlimeVRInput",
				new FastList<>()
			);
			tasks.add(feederBridge::startBridge);
			bridges.add(feederBridge);
		} else if (OperatingSystem.getCurrentPlatform() == OperatingSystem.LINUX) {
			SteamVRBridge linuxBridge = null;
			try {
				linuxBridge = new UnixSocketBridge(
					this,
					hmdTracker,
					"steamvr",
					"SteamVR Driver Bridge",
					"/tmp/SlimeVRDriver",
					shareTrackers
				);
			} catch (Exception ex) {
				LogManager.severe("Failed to initiate Unix socket, disabling driver bridge...", ex);
			}
			driverBridge = linuxBridge;
			if (driverBridge != null) {
				tasks.add(driverBridge::startBridge);
				bridges.add(driverBridge);
			}
		} else {
			driverBridge = null;
		}

		// Add shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				if (driverBridge instanceof UnixSocketBridge linuxBridge) {
					// Auto-close Linux SteamVR bridge on JVM shutdown
					linuxBridge.close();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}));

		// Create WebSocket server
		WebSocketVRBridge wsBridge = new WebSocketVRBridge(hmdTracker, shareTrackers, this);
		tasks.add(wsBridge::startBridge);
		bridges.add(wsBridge);

		// Create VMCBridge
		try {
			VMCBridge vmcBridge = new VMCBridge(39539, 39540, InetAddress.getLocalHost());
			tasks.add(vmcBridge::startBridge);
			bridges.add(vmcBridge);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		// Initialize OSC handlers
		vrcOSCHandler = new VRCOSCHandler(
			this,
			hmdTracker,
			humanPoseProcessor,
			driverBridge,
			getConfigManager().getVrConfig().getVrcOSC(),
			shareTrackers
		);

		// Initialize OSC router
		FastList<OSCHandler> oscHandlers = new FastList<>();
		oscHandlers.add(vrcOSCHandler);
		oscRouter = new OSCRouter(getConfigManager().getVrConfig().getOscRouter(), oscHandlers);

		bvhRecorder = new BVHRecorder(this);

		registerTracker(hmdTracker);
		for (Tracker tracker : shareTrackers) {
			registerTracker(tracker);
		}
	}

	public boolean hasBridge(Class<? extends Bridge> bridgeClass) {
		for (Bridge bridge : bridges) {
			if (bridgeClass.isAssignableFrom(bridge.getClass())) {
				return true;
			}
		}
		return false;
	}

	@ThreadSafe
	public <E extends Bridge> E getVRBridge(Class<E> bridgeClass) {
		for (Bridge bridge : bridges) {
			if (bridgeClass.isAssignableFrom(bridge.getClass())) {
				return bridgeClass.cast(bridge);
			}
		}
		return null;
	}


	public void addOnTick(Runnable runnable) {
		this.onTick.add(runnable);
	}

	@ThreadSafe
	public void addNewTrackerConsumer(Consumer<Tracker> consumer) {
		queueTask(() -> {
			newTrackersConsumers.add(consumer);
			for (Tracker tracker : trackers) {
				consumer.accept(tracker);
			}
		});
	}

	@ThreadSafe
	public void trackerUpdated(Tracker tracker) {
		queueTask(() -> {
			humanPoseProcessor.trackerUpdated(tracker);
			this.getConfigManager().getVrConfig().writeTrackerConfig(tracker);
			this.getConfigManager().saveConfig();
		});
	}

	@ThreadSafe
	public void addSkeletonUpdatedCallback(Consumer<Skeleton> consumer) {
		queueTask(() -> humanPoseProcessor.addSkeletonUpdatedCallback(consumer));
	}

	@Override
	@VRServerThread
	public void run() {
		trackersServer.start();
		while (true) {
			// final long start = System.currentTimeMillis();
			fpsTimer.update();
			do {
				Runnable task = tasks.poll();
				if (task == null)
					break;
				task.run();
			} while (true);
			for (Runnable task : onTick) {
				task.run();
			}
			for (Bridge bridge : bridges) {
				bridge.dataRead();
			}
			for (Tracker tracker : trackers) {
				tracker.tick();
			}
			humanPoseProcessor.update();
			for (Bridge bridge : bridges) {
				bridge.dataWrite();
			}
			vrcOSCHandler.update();
			// final long time = System.currentTimeMillis() - start;
			try {
				Thread.sleep(1); // 1000Hz
			} catch (InterruptedException ignored) {}
		}
	}

	@ThreadSafe
	public void queueTask(Runnable r) {
		tasks.add(r);
	}

	@VRServerThread
	private void trackerAdded(Tracker tracker) {
		humanPoseProcessor.trackerAdded(tracker);
	}

	@ThreadSecure
	public void registerTracker(Tracker tracker) {
		this.getConfigManager().getVrConfig().readTrackerConfig(tracker);
		queueTask(() -> {
			trackers.add(tracker);
			trackerAdded(tracker);
			for (Consumer<Tracker> tc : newTrackersConsumers) {
				tc.accept(tracker);
			}
		});
	}

	public void resetTrackers() {
		queueTask(humanPoseProcessor::resetTrackers);
	}

	public void resetTrackersYaw() {
		queueTask(humanPoseProcessor::resetTrackersYaw);
	}

	public void resetTrackersMounting() {
		queueTask(humanPoseProcessor::resetTrackersMounting);
	}

	public void scheduleResetTrackers(long delay) {
		TimerTask resetTask = new resetTask();
		timer.schedule(resetTask, delay);
	}

	public void scheduleResetTrackersYaw(long delay) {
		TimerTask yawResetTask = new yawResetTask();
		timer.schedule(yawResetTask, delay);
	}

	public void scheduleResetTrackersMounting(long delay) {
		TimerTask resetMountingTask = new resetMountingTask();
		timer.schedule(resetMountingTask, delay);
	}

	class resetTask extends TimerTask {
		public void run() {
			queueTask(humanPoseProcessor::resetTrackers);
		}
	}

	class yawResetTask extends TimerTask {
		public void run() {
			queueTask(humanPoseProcessor::resetTrackersYaw);
		}
	}

	class resetMountingTask extends TimerTask {
		public void run() {
			queueTask(humanPoseProcessor::resetTrackersMounting);
		}
	}

	public void setLegTweaksEnabled(boolean value) {
		queueTask(() -> humanPoseProcessor.setLegTweaksEnabled(value));
	}

	public void setSkatingReductionEnabled(boolean value) {
		queueTask(() -> humanPoseProcessor.setSkatingCorrectionEnabled(value));
	}

	public void setFloorClipEnabled(boolean value) {
		queueTask(() -> humanPoseProcessor.setFloorClipEnabled(value));
	}

	public int getTrackersCount() {
		return trackers.size();
	}

	public List<Tracker> getAllTrackers() {
		return new FastList<>(trackers);
	}

	public Tracker getTrackerById(TrackerId id) {
		for (Tracker tracker : trackers) {
			if (tracker.getTrackerNum() != id.getTrackerNum()) {
				continue;
			}

			// Handle synthetic devices
			if (id.getDeviceId() == null && tracker.getDevice() == null) {
				return tracker;
			}

			if (
				tracker.getDevice() != null
					&& id.getDeviceId() != null
					&& id.getDeviceId().getId() == tracker.getDevice().getId()
			) {
				// This is a physical tracker, and both device id and the
				// tracker num match
				return tracker;
			}
		}
		return null;
	}

	public BVHRecorder getBvhRecorder() {
		return this.bvhRecorder;
	}

	public SerialHandler getSerialHandler() {
		return this.serialHandler;
	}

	public AutoBoneHandler getAutoBoneHandler() {
		return this.autoBoneHandler;
	}

	public ProtocolAPI getProtocolAPI() {
		return protocolAPI;
	}

	public TrackersUDPServer getTrackersServer() {
		return trackersServer;
	}

	public OSCRouter getOSCRouter() {
		return oscRouter;
	}

	public VRCOSCHandler getVrcOSCHandler() {
		return vrcOSCHandler;
	}

	public DeviceManager getDeviceManager() {
		return deviceManager;
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public NanoTimer getFpsTimer() {
		return fpsTimer;
	}

}
