package dev.slimevr

import com.jme3.system.NanoTimer
import dev.slimevr.autobone.AutoBoneHandler
import dev.slimevr.bridge.Bridge
import dev.slimevr.bridge.ISteamVRBridge
import dev.slimevr.config.ConfigManager
import dev.slimevr.osc.OSCHandler
import dev.slimevr.osc.OSCRouter
import dev.slimevr.osc.VMCHandler
import dev.slimevr.osc.VRCOSCHandler
import dev.slimevr.posestreamer.BVHRecorder
import dev.slimevr.protocol.ProtocolAPI
import dev.slimevr.reset.ResetHandler
import dev.slimevr.serial.ProvisioningHandler
import dev.slimevr.serial.SerialHandler
import dev.slimevr.serial.SerialHandlerStub
import dev.slimevr.setup.TapSetupHandler
import dev.slimevr.status.StatusSystem
import dev.slimevr.tracking.processor.HumanPoseManager
import dev.slimevr.tracking.processor.skeleton.HumanSkeleton
import dev.slimevr.tracking.trackers.DeviceManager
import dev.slimevr.tracking.trackers.Tracker
import dev.slimevr.tracking.trackers.TrackerPosition
import dev.slimevr.tracking.trackers.TrackerUtils
import dev.slimevr.tracking.trackers.udp.TrackersUDPServer
import dev.slimevr.util.ann.VRServerThread
import dev.slimevr.websocketapi.WebSocketVRBridge
import io.eiren.util.ann.ThreadSafe
import io.eiren.util.ann.ThreadSecure
import io.eiren.util.collections.FastList
import io.eiren.util.logging.LogManager
import solarxr_protocol.datatypes.TrackerIdT
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

typealias SteamBridgeProvider = (
	server: VRServer,
	computedTrackers: List<Tracker>,
) -> ISteamVRBridge?

const val SLIMEVR_IDENTIFIER = "dev.slimevr.SlimeVR"

class VRServer @JvmOverloads constructor(
	driverBridgeProvider: SteamBridgeProvider = { _, _ -> null },
	feederBridgeProvider: (VRServer) -> ISteamVRBridge? = { _ -> null },
	serialHandlerProvider: (VRServer) -> SerialHandler = { _ -> SerialHandlerStub() },
	// configPath is used by VRWorkout, do not remove!
	configPath: String,
) : Thread("VRServer") {
	@JvmField
	val configManager: ConfigManager

	@JvmField
	val humanPoseManager: HumanPoseManager
	private val trackers: MutableList<Tracker> = FastList()
	val trackersServer: TrackersUDPServer
	private val bridges: MutableList<Bridge> = FastList()
	private val tasks: Queue<Runnable> = LinkedBlockingQueue()
	private val onTick: MutableList<Runnable> = FastList()
	val oSCRouter: OSCRouter

	@JvmField
	val vrcOSCHandler: VRCOSCHandler
	val vMCHandler: VMCHandler

	@JvmField
	val deviceManager: DeviceManager

	@JvmField
	val bvhRecorder: BVHRecorder

	@JvmField
	val serialHandler: SerialHandler

	@JvmField
	val autoBoneHandler: AutoBoneHandler

	@JvmField
	val tapSetupHandler: TapSetupHandler

	@JvmField
	val protocolAPI: ProtocolAPI
	private val timer = Timer()
	val fpsTimer = NanoTimer()

	@JvmField
	val provisioningHandler: ProvisioningHandler

	@JvmField
	val resetHandler: ResetHandler

	@JvmField
	val statusSystem = StatusSystem()

	init {
		// UwU
		configManager = ConfigManager(configPath)
		configManager.loadConfig()
		deviceManager = DeviceManager(this)
		serialHandler = serialHandlerProvider(this)
		provisioningHandler = ProvisioningHandler(this)
		resetHandler = ResetHandler()
		tapSetupHandler = TapSetupHandler()
		autoBoneHandler = AutoBoneHandler(this)
		protocolAPI = ProtocolAPI(this)
		humanPoseManager = HumanPoseManager(this)
		val computedTrackers = humanPoseManager.computedTrackers

		// Start server for SlimeVR trackers
		val trackerPort = configManager.vrConfig.server.trackerPort
		LogManager.info("Starting the tracker server on port $trackerPort...")
		trackersServer = TrackersUDPServer(
			trackerPort,
			"Sensors UDP server"
		) { tracker: Tracker -> registerTracker(tracker) }

		// Start bridges for SteamVR and Feeder
		val driverBridge = driverBridgeProvider(this, computedTrackers)
		if (driverBridge != null) {
			tasks.add(Runnable { driverBridge.startBridge() })
			bridges.add(driverBridge)
		}
		val feederBridge = feederBridgeProvider(this)
		if (feederBridge != null) {
			tasks.add(Runnable { feederBridge.startBridge() })
			bridges.add(feederBridge)
		}

		// Create WebSocket server
		val wsBridge = WebSocketVRBridge(computedTrackers, this)
		tasks.add(Runnable { wsBridge.startBridge() })
		bridges.add(wsBridge)

		// Initialize OSC handlers
		vrcOSCHandler = VRCOSCHandler(
			this,
			humanPoseManager,
			driverBridge,
			configManager.vrConfig.vrcOSC,
			computedTrackers
		)
		vMCHandler = VMCHandler(
			this,
			humanPoseManager,
			configManager.vrConfig.vmc,
			computedTrackers
		)

		// Initialize OSC router
		val oscHandlers = FastList<OSCHandler>()
		oscHandlers.add(vrcOSCHandler)
		oscHandlers.add(vMCHandler)
		oSCRouter = OSCRouter(configManager.vrConfig.oscRouter, oscHandlers)
		bvhRecorder = BVHRecorder(this)
		for (tracker in computedTrackers) {
			registerTracker(tracker)
		}
		instance = this
	}

	fun hasBridge(bridgeClass: Class<out Bridge?>): Boolean {
		for (bridge in bridges) {
			if (bridgeClass.isAssignableFrom(bridge.javaClass)) {
				return true
			}
		}
		return false
	}

	@ThreadSafe
	fun <E : Bridge?> getVRBridge(bridgeClass: Class<E>): E? {
		for (bridge in bridges) {
			if (bridgeClass.isAssignableFrom(bridge.javaClass)) {
				return bridgeClass.cast(bridge)
			}
		}
		return null
	}

	fun addOnTick(runnable: Runnable) {
		onTick.add(runnable)
	}

	@ThreadSafe
	fun trackerUpdated(tracker: Tracker?) {
		queueTask {
			humanPoseManager.trackerUpdated(tracker)
			updateSkeletonModel()
			refreshTrackersDriftCompensationEnabled()
			configManager.vrConfig.writeTrackerConfig(tracker)
			configManager.saveConfig()
			if (tracker != null) {
				if (tracker.trackerPosition == TrackerPosition.ACCESSORY) {
					vrcOSCHandler.addAccessoryTracker(tracker)
				}
			}
		}
	}

	@ThreadSafe
	fun addSkeletonUpdatedCallback(consumer: Consumer<HumanSkeleton?>?) {
		queueTask { humanPoseManager.addSkeletonUpdatedCallback(consumer) }
	}

	@VRServerThread
	override fun run() {
		trackersServer.start()
		while (true) {
			// final long start = System.currentTimeMillis();
			fpsTimer.update()
			do {
				val task = tasks.poll() ?: break
				task.run()
			} while (true)
			for (task in onTick) {
				task.run()
			}
			for (bridge in bridges) {
				bridge.dataRead()
			}
			for (tracker in trackers) {
				tracker.tick()
			}
			humanPoseManager.update()
			for (bridge in bridges) {
				bridge.dataWrite()
			}
			vrcOSCHandler.update()
			vMCHandler.update()
			// final long time = System.currentTimeMillis() - start;
			try {
				sleep(1) // 1000Hz
			} catch (error: InterruptedException) {
				LogManager.info("VRServer thread interrupted")
				break
			}
		}
	}

	@ThreadSafe
	fun queueTask(r: Runnable) {
		tasks.add(r)
	}

	@VRServerThread
	private fun trackerAdded(tracker: Tracker) {
		humanPoseManager.trackerAdded(tracker)
		updateSkeletonModel()
		if (tracker.isComputed) {
			vMCHandler.addComputedTracker(tracker)
		}
		if (tracker.trackerPosition == TrackerPosition.ACCESSORY) {
			vrcOSCHandler.addAccessoryTracker(tracker)
		}
		refreshTrackersDriftCompensationEnabled()
	}

	@ThreadSecure
	fun registerTracker(tracker: Tracker) {
		configManager.vrConfig.readTrackerConfig(tracker)
		queueTask {
			trackers.add(tracker)
			trackerAdded(tracker)
		}
	}

	@ThreadSafe
	fun updateSkeletonModel() {
		queueTask { humanPoseManager.updateSkeletonModelFromServer() }
		vrcOSCHandler.setHeadTracker(
			TrackerUtils.getTrackerForSkeleton(trackers, TrackerPosition.HEAD)
		)
	}

	fun resetTrackersFull(resetSourceName: String?) {
		queueTask { humanPoseManager.resetTrackersFull(resetSourceName) }
	}

	fun resetTrackersYaw(resetSourceName: String?) {
		queueTask { humanPoseManager.resetTrackersYaw(resetSourceName) }
	}

	fun resetTrackersMounting(resetSourceName: String?) {
		queueTask { humanPoseManager.resetTrackersMounting(resetSourceName) }
	}

	fun clearTrackersMounting(resetSourceName: String?) {
		queueTask { humanPoseManager.clearTrackersMounting(resetSourceName) }
	}

	fun scheduleResetTrackersFull(resetSourceName: String?, delay: Long) {
		val resetTask: TimerTask = object : TimerTask() {
			override fun run() {
				queueTask { humanPoseManager.resetTrackersFull(resetSourceName) }
			}
		}
		timer.schedule(resetTask, delay)
	}

	fun scheduleResetTrackersYaw(resetSourceName: String?, delay: Long) {
		val yawResetTask: TimerTask = object : TimerTask() {
			override fun run() {
				queueTask { humanPoseManager.resetTrackersYaw(resetSourceName) }
			}
		}
		timer.schedule(yawResetTask, delay)
	}

	fun scheduleResetTrackersMounting(resetSourceName: String?, delay: Long) {
		val resetMountingTask: TimerTask = object : TimerTask() {
			override fun run() {
				queueTask { humanPoseManager.resetTrackersMounting(resetSourceName) }
			}
		}
		timer.schedule(resetMountingTask, delay)
	}

	fun setLegTweaksEnabled(value: Boolean) {
		queueTask { humanPoseManager.setLegTweaksEnabled(value) }
	}

	fun setSkatingReductionEnabled(value: Boolean) {
		queueTask { humanPoseManager.setSkatingCorrectionEnabled(value) }
	}

	fun setFloorClipEnabled(value: Boolean) {
		queueTask { humanPoseManager.setFloorClipEnabled(value) }
	}

	val trackersCount: Int
		get() = trackers.size
	val allTrackers: List<Tracker>
		get() = FastList(trackers)

	fun getTrackerById(id: TrackerIdT): Tracker? {
		for (tracker in trackers) {
			if (tracker.trackerNum != id.trackerNum) {
				continue
			}

			// Handle synthetic devices
			if (id.deviceId == null && tracker.device == null) {
				return tracker
			}
			if (tracker.device != null && id.deviceId != null && id.deviceId.id == tracker.device.id) {
				// This is a physical tracker, and both device id and the
				// tracker num match
				return tracker
			}
		}
		return null
	}

	fun clearTrackersDriftCompensation() {
		for (t in allTrackers) {
			if (t.isImu()) {
				t.resetsHandler.clearDriftCompensation()
			}
		}
	}

	fun refreshTrackersDriftCompensationEnabled() {
		for (t in allTrackers) {
			if (t.isImu()) {
				t.resetsHandler.refreshDriftCompensationEnabled()
			}
		}
	}

	companion object {
		private val nextLocalTrackerId = AtomicInteger()
		lateinit var instance: VRServer
			private set

		val instanceInitialized: Boolean
			get() = ::instance.isInitialized

		@JvmStatic
		fun getNextLocalTrackerId(): Int {
			return nextLocalTrackerId.incrementAndGet()
		}

		@JvmStatic
		val currentLocalTrackerId: Int
			get() = nextLocalTrackerId.get()
	}
}
