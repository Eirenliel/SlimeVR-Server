package dev.slimevr.desktop.platform.linux;

import dev.slimevr.bridge.BridgeThread;
import dev.slimevr.protocol.GenericConnection;
import dev.slimevr.protocol.ProtocolAPI;
import dev.slimevr.tracking.trackers.Tracker;
import dev.slimevr.util.ann.VRServerThread;
import dev.slimevr.VRServer;
import io.eiren.util.logging.LogManager;

import java.io.File;
import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;


public class UnixSocketRpcBridge implements dev.slimevr.bridge.Bridge,
	dev.slimevr.protocol.ProtocolAPIServer, Runnable, AutoCloseable {
	private final Thread runnerThread = new Thread(this, "Named socket thread");
	private final String socketPath;
	private final ProtocolAPI protocolAPI;
	private final ServerSocketChannel socket;
	private final Selector selector;

	public UnixSocketRpcBridge(
		VRServer server,
		String socketPath,
		List<Tracker> shareableTrackers
	) {
		this.socketPath = socketPath;
		this.protocolAPI = server.protocolAPI;
		File socketFile = new File(socketPath);
		if (socketFile.exists())
			throw new RuntimeException(socketPath + " socket already exists.");
		socketFile.deleteOnExit();
		try {
			socket = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Socket open failed.");
		}

		server.protocolAPI.registerAPIServer(this);
	}

	@VRServerThread
	private void disconnected() {
	}

	@Override
	@VRServerThread
	public void dataRead() {
	}

	@Override
	@VRServerThread
	public void dataWrite() {
	}

	@Override
	@VRServerThread
	public void addSharedTracker(Tracker tracker) {
	}

	@Override
	@VRServerThread
	public void removeSharedTracker(Tracker tracker) {
	}

	@Override
	@VRServerThread
	public void startBridge() {
		this.runnerThread.start();
	}

	@Override
	@BridgeThread
	public void run() {
		try {
			this.socket.bind(UnixDomainSocketAddress.of(this.socketPath));
			this.socket.configureBlocking(false);
			this.socket.register(this.selector, SelectionKey.OP_ACCEPT);
			LogManager.info("[SolarXR Bridge] Socket " + this.socketPath + " created");
			while (this.socket.isOpen()) {
				this.selector.select(0);
				for (SelectionKey key : this.selector.selectedKeys()) {
					UnixSocketConnection conn = (UnixSocketConnection) key.attachment();
					if (conn != null) {
						for (ByteBuffer message; (message = conn.read()) != null; conn.next())
							this.protocolAPI.onMessage(conn, message);
					} else
						for (SocketChannel channel; (channel = socket.accept()) != null;) {
							channel.configureBlocking(false);
							channel
								.register(
									this.selector,
									SelectionKey.OP_READ,
									new UnixSocketConnection(channel)
								);
							LogManager
								.info(
									"[SolarXR Bridge] Connected to "
										+ channel.getRemoteAddress().toString()
								);
						}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws Exception {
		this.socket.close();
		this.selector.close();
	}

	@Override
	public boolean isConnected() {
		return this.selector.keys().stream().anyMatch(key -> key.attachment() != null);
	}

	@Override
	public java.util.stream.Stream<GenericConnection> getAPIConnections() {
		return this.selector
			.keys()
			.stream()
			.map(key -> (GenericConnection) key.attachment())
			.filter(conn -> conn != null);
	}
}
