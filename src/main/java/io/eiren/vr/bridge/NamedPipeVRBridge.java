package io.eiren.vr.bridge;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

import io.eiren.util.collections.FastList;
import io.eiren.util.logging.LogManager;
import io.eiren.vr.VRServer;
import io.eiren.vr.trackers.ComputedTracker;
import io.eiren.vr.trackers.HMDTracker;
import io.eiren.vr.trackers.Tracker;
import io.eiren.vr.trackers.TrackerStatus;

public class NamedPipeVRBridge extends Thread implements VRBridge {

	public static final String HMDPipeName = "\\\\.\\pipe\\HMDPipe";
	public static final String TrackersPipeName = "\\\\.\\pipe\\TrackPipe";
	public static final Charset ASCII = Charset.forName("ASCII");

	private final byte[] buffer = new byte[1024];
	private final StringBuilder sbBuffer = new StringBuilder(1024);
	private final Vector3f vBuffer = new Vector3f();
	private final Vector3f vBuffer2 = new Vector3f();
	private final Quaternion qBuffer = new Quaternion();
	private final Quaternion qBuffer2 = new Quaternion();

	private final VRServer server;
	private Pipe hmdPipe;
	private final HMDTracker hmd;
	private final List<Pipe> trackerPipes;
	private final List<? extends Tracker> shareTrackers;
	private final List<ComputedTracker> internalTrackers;

	private final HMDTracker internalHMDTracker = new HMDTracker("itnernal://HMD");
	private final AtomicBoolean newHMDData = new AtomicBoolean(false);

	public NamedPipeVRBridge(HMDTracker hmd, List<? extends Tracker> shareTrackers, VRServer server) {
		super("Named Pipe VR Bridge");
		this.server = server;
		this.hmd = hmd;
		this.shareTrackers = new FastList<>(shareTrackers);
		this.trackerPipes = new FastList<>(shareTrackers.size());
		this.internalTrackers = new FastList<>(shareTrackers.size());
		for(int i = 0; i < shareTrackers.size(); ++i) {
			Tracker t = shareTrackers.get(i);
			ComputedTracker ct = new ComputedTracker("internal://" + t.getName());
			ct.setStatus(TrackerStatus.OK);
			this.internalTrackers.add(ct);
		}
	}

	@Override
	public void run() {
		try {
			createPipes();
			while(true) {
				waitForPipesToOpen();
				if(areAllPipesOpen()) {
					boolean hmdUpdated = updateHMD(); // Update at HMDs frequency
					for(int i = 0; i < trackerPipes.size(); ++i) {
						updateTracker(i, hmdUpdated);
					}
					if(!hmdUpdated) {
						Thread.sleep(5); // Up to 200Hz
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dataRead() {
		if(newHMDData.compareAndSet(true, false)) {
			hmd.position.set(internalHMDTracker.position);
			hmd.rotation.set(internalHMDTracker.rotation);
			hmd.dataTick();
		}
	}

	@Override
	public void dataWrite() {
		for(int i = 0; i < shareTrackers.size(); ++i) {
			Tracker t = shareTrackers.get(i);
			ComputedTracker it = this.internalTrackers.get(i);
			if(t.getPosition(vBuffer2))
				it.position.set(vBuffer2);
			if(t.getRotation(qBuffer2))
				it.rotation.set(qBuffer2);
		}
	}

	private void waitForPipesToOpen() {
		if(hmdPipe.state == PipeState.CREATED) {
			if(tryOpeningPipe(hmdPipe))
				initHMDPipe(hmdPipe);
		}
		for(int i = 0; i < trackerPipes.size(); ++i) {
			Pipe trackerPipe = trackerPipes.get(i);
			if(trackerPipe.state == PipeState.CREATED) {
				if(tryOpeningPipe(trackerPipe))
					initTrackerPipe(trackerPipe, i);
			}
		}
	}

	public boolean updateHMD() {
		// pipe isn't open, don't even bother.
		if(hmdPipe.state != PipeState.OPEN) {
			return false;
		}

		// take a peek at what's there
		IntByReference bytesRead = new IntByReference(0);
		if(!Kernel32.INSTANCE.PeekNamedPipe(hmdPipe.pipeHandle, buffer, buffer.length, bytesRead, null, null)) {
			return false;
		}

		// nothing there
		if (bytesRead.getValue() <= 0) {
			return false;
		}

		// determine how much is a single entry
		int newline_index = -1;
		for (int iii = 0; iii < bytesRead.getValue(); ++iii) {
			if (buffer[iii] == '\n') {
				newline_index = iii;
				break;
			}
		}

		// don't consume partial data.
		if (newline_index == -1) {
			return false;
		}

		// consume a single entry, including the newline
		if(!Kernel32.INSTANCE.ReadFile(hmdPipe.pipeHandle, buffer, newline_index + 1, bytesRead, null)) {
			// something went wrong while consuming the data!
			// TODO: throw exception?
			return false;
		}

		// TODO: should we check that we got the number of bytes we asked for?

		// discard the newline
		String str = new String(buffer, 0, bytesRead.getValue() - 1, ASCII);
		String[] split = str.split(" ");
		try {
			double x = Double.parseDouble(split[0]);
			double y = Double.parseDouble(split[1]);
			double z = Double.parseDouble(split[2]);
			double qw = Double.parseDouble(split[3]);
			double qx = Double.parseDouble(split[4]);
			double qy = Double.parseDouble(split[5]);
			double qz = Double.parseDouble(split[6]);

			internalHMDTracker.position.set((float) x, (float) y, (float) z);
			internalHMDTracker.rotation.set((float) qx, (float) qy, (float) qz, (float) qw);
			internalHMDTracker.dataTick();
			newHMDData.set(true);
		} catch(NumberFormatException e) {
			e.printStackTrace();
		}

		return true;
	}

	public void updateTracker(int trackerId, boolean hmdUpdated) {
		Tracker sensor = internalTrackers.get(trackerId);
		if(sensor.getStatus().sendData) {
			Pipe trackerPipe = trackerPipes.get(trackerId);
			if(hmdUpdated && trackerPipe.state == PipeState.OPEN) {
				sbBuffer.setLength(0);
				sensor.getPosition(vBuffer);
				sensor.getRotation(qBuffer);
				sbBuffer.append(vBuffer.x).append(' ').append(vBuffer.y).append(' ').append(vBuffer.z).append(' ');
				sbBuffer.append(qBuffer.getW()).append(' ').append(qBuffer.getX()).append(' ').append(qBuffer.getY()).append(' ').append(qBuffer.getZ()).append('\n');
				String str = sbBuffer.toString();
				byte[] str_bytes = Native.toByteArray(str, ASCII);
				IntByReference lpNumberOfBytesWritten = new IntByReference(0);
				Kernel32.INSTANCE.WriteFile(trackerPipe.pipeHandle, str_bytes, str_bytes.length, lpNumberOfBytesWritten, null);
			}
		}
	}

	private void initHMDPipe(Pipe pipe) {
		hmd.setStatus(TrackerStatus.OK);
	}

	private void initTrackerPipe(Pipe pipe, int trackerId) {
		String trackerHello = this.shareTrackers.size() + " 0";
		byte[] trackerHelloBytes = Native.toByteArray(trackerHello);
		IntByReference lpNumberOfBytesWritten = new IntByReference(0);
		Kernel32.INSTANCE.WriteFile(pipe.pipeHandle,
			trackerHelloBytes,
			trackerHelloBytes.length,
			lpNumberOfBytesWritten,
			null);
	}

	private boolean tryOpeningPipe(Pipe pipe) {
		if(Kernel32.INSTANCE.ConnectNamedPipe(pipe.pipeHandle, null)) {
			pipe.state = PipeState.OPEN;
			LogManager.log.info("[VRBridge] Pipe " + pipe.name + " is open");
			return true;
		}

		LogManager.log.info("[VRBridge] Error connecting to pipe " + pipe.name + ": " + Kernel32.INSTANCE.GetLastError());
		return false;
	}

	private boolean areAllPipesOpen() {
		if(hmdPipe == null || hmdPipe.state == PipeState.CREATED) {
			return false;
		}
		for(int i = 0; i < trackerPipes.size(); ++i) {
			if(trackerPipes.get(i).state == PipeState.CREATED)
				return false;
		}
		return true;
	}

	private void createPipes() throws IOException {
		try {
			hmdPipe = new Pipe(Kernel32.INSTANCE.CreateNamedPipe(HMDPipeName, WinBase.PIPE_ACCESS_DUPLEX, // dwOpenMode
					WinBase.PIPE_TYPE_BYTE | WinBase.PIPE_READMODE_BYTE | WinBase.PIPE_WAIT, // dwPipeMode
					1, // nMaxInstances,
					1024 * 16, // nOutBufferSize,
					1024 * 16, // nInBufferSize,
					0, // nDefaultTimeOut,
					null), HMDPipeName); // lpSecurityAttributes
			LogManager.log.info("[VRBridge] Pipe " + hmdPipe.name + " created");
			if(WinBase.INVALID_HANDLE_VALUE.equals(hmdPipe.pipeHandle))
				throw new IOException("Can't open " + HMDPipeName + " pipe: " + Kernel32.INSTANCE.GetLastError());
			for(int i = 0; i < this.shareTrackers.size(); ++i) {
				String pipeName = TrackersPipeName + i;
				HANDLE pipeHandle = Kernel32.INSTANCE.CreateNamedPipe(pipeName, WinBase.PIPE_ACCESS_DUPLEX, // dwOpenMode
					WinBase.PIPE_TYPE_BYTE | WinBase.PIPE_READMODE_BYTE | WinBase.PIPE_WAIT, // dwPipeMode
					1, // nMaxInstances,
					1024 * 16, // nOutBufferSize,
					1024 * 16, // nInBufferSize,
					0, // nDefaultTimeOut,
					null); // lpSecurityAttributes
				if(WinBase.INVALID_HANDLE_VALUE.equals(pipeHandle))
					throw new IOException("Can't open " + pipeName + " pipe: " + Kernel32.INSTANCE.GetLastError());
				LogManager.log.info("[VRBridge] Pipe " + pipeName + " created");
				trackerPipes.add(new Pipe(pipeHandle, pipeName));
			}
			LogManager.log.info("[VRBridge] Pipes are open");
		} catch(IOException e) {
			safeDisconnect(hmdPipe);
			for(int i = 0; i < trackerPipes.size(); ++i)
				safeDisconnect(trackerPipes.get(i));
			trackerPipes.clear();
			throw e;
		}
	}

	public static void safeDisconnect(Pipe pipe) {
		try {
			if(pipe != null && pipe.pipeHandle != null)
				Kernel32.INSTANCE.DisconnectNamedPipe(pipe.pipeHandle);
		} catch(Exception e) {
		}
	}
}
