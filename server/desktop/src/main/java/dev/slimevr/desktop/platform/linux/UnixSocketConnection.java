package dev.slimevr.desktop.platform.linux;

import dev.slimevr.protocol.ConnectionContext;
import dev.slimevr.protocol.GenericConnection;
import io.eiren.util.logging.LogManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.UUID;


public class UnixSocketConnection implements GenericConnection {
	public final UUID id;
	public final ConnectionContext context;
	private final ByteBuffer dst = ByteBuffer.allocate(2048).order(ByteOrder.LITTLE_ENDIAN);
	private final SocketChannel channel;
	private int remainingBytes;

	public UnixSocketConnection(SocketChannel channel) {
		this.id = UUID.randomUUID();
		this.context = new ConnectionContext();
		this.channel = channel;
	}

	@Override
	public UUID getConnectionId() {
		return id;
	}

	@Override
	public ConnectionContext getContext() {
		return this.context;
	}

	public boolean isConnected() {
		return this.channel.isConnected();
	}

	private void resetChannel() {
		try {
			this.channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void send(ByteBuffer bytes) {
		if (!this.channel.isConnected())
			return;
		try {
			ByteBuffer[] src = new ByteBuffer[] {
				ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN),
				bytes.slice(),
			};
			src[0].putInt(src[1].remaining() + 4);
			src[0].flip();
			synchronized (this) {
				while (src[1].hasRemaining()) {
					this.channel.write(src);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ByteBuffer read() {
		if (dst.position() < 4 || dst.position() < dst.getInt(0)) {
			if (!this.channel.isConnected())
				return null;
			try {
				int result = this.channel.read(dst);
				if (result == -1) {
					LogManager.info("[SolarXR Bridge] Reached end-of-stream on connection");
					this.resetChannel();
					return null;
				}
				if (dst.position() < 4) {
					return null;
				}
			} catch (IOException e) {
				e.printStackTrace();
				this.resetChannel();
				return null;
			}
		}
		int messageLength = dst.getInt(0);
		if (messageLength > 1024) {
			LogManager
				.severe(
					"[SolarXR Bridge] Buffer overflow on socket. Message length: " + messageLength
				);
			this.resetChannel();
			return null;
		}
		if (dst.position() < messageLength) {
			return null;
		}
		remainingBytes = dst.position() - messageLength;
		dst.position(4);
		dst.limit(messageLength);
		return dst;
	}

	public void next() {
		dst.position(dst.limit());
		dst.limit(dst.limit() + remainingBytes);
		dst.compact();
		dst.limit(dst.capacity());
	}
}
