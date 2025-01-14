package dev.slimevr.websocketapi;

import dev.slimevr.protocol.ConnectionContext;
import dev.slimevr.protocol.GenericConnection;
import org.java_websocket.WebSocket;

import java.nio.ByteBuffer;
import java.util.UUID;


public class WebsocketConnection implements GenericConnection {

	public final ConnectionContext context;
	public final WebSocket conn;
	public UUID id;

	public WebsocketConnection(WebSocket conn) {
		this.context = new ConnectionContext();
		this.conn = conn;
		this.id = UUID.randomUUID();
	}

	@Override
	public ConnectionContext getContext() {
		return this.context;
	}

	@Override
	public void send(ByteBuffer bytes) {
		if (this.conn.isOpen())
			this.conn.send(bytes.slice());
	}

	@Override
	public UUID getConnectionId() {
		return id;
	}
}
