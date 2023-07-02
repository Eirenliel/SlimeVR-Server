package dev.slimevr.websocketapi;

import dev.slimevr.VRServer;
import dev.slimevr.protocol.GenericConnection;
import dev.slimevr.protocol.ProtocolAPI;
import dev.slimevr.protocol.ProtocolAPIServer;
import io.eiren.util.logging.LogManager;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;


public class WebsocketAPI extends WebSocketServer implements ProtocolAPIServer {

	public final VRServer server;
	public final ProtocolAPI protocolAPI;

	public WebsocketAPI(VRServer server, ProtocolAPI protocolAPI) {
		super(new InetSocketAddress(21110), Collections.singletonList(new Draft_6455()));
		this.server = server;
		this.protocolAPI = protocolAPI;

		this.protocolAPI.registerAPIServer(this);
		setReuseAddr(true);
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		LogManager
			.info(
				"[WebSocketAPI] New connection from: "
					+ conn.getRemoteSocketAddress().getAddress().getHostAddress()
			);
		conn.setAttachment(new WebsocketConnection(conn));
	}

	/**
	 * Helper function to get the string of the `conn` while handling `null`
	 */
	protected static String connAddr(WebSocket conn) {
		if (conn == null) {
			return "null";
		}
		var remote = conn.getRemoteSocketAddress();
		if (remote == null) {
			return conn.toString();
		}
		var addr = remote.getAddress();
		if (addr == null) {
			return remote.toString();
		}
		return addr.getHostAddress();
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		LogManager
			.info(
				"[WebSocketAPI] Disconnected: "
					+ connAddr(conn)
					+ ", ("
					+ code
					+ ") "
					+ reason
					+ ". Remote: "
					+ remote
			);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer message) {
		var connection = conn.<WebsocketConnection>getAttachment();
		if (connection != null)
			this.protocolAPI.onMessage(connection, message);
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		if (ex instanceof java.net.BindException) {
			LogManager.warning("[WebSocket] Port was already in use!");
			return;
		}

		LogManager
			.severe(
				"[WebSocket] Exception on connection " + connAddr(conn),
				ex
			);
	}

	@Override
	public void onStart() {
		LogManager.info("[WebSocketAPI] Web Socket API started on port " + getPort());
		setConnectionLostTimeout(0);
	}

	@Override
	public Stream<GenericConnection> getAPIConnections() {
		return this.getConnections().stream().map(conn -> {
			var c = conn.<WebsocketConnection>getAttachment();
			return (GenericConnection) c;
		}).filter(Objects::nonNull);
	}
}
