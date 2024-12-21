package dev.slimevr.protocol.rpc.trackingpause

import com.google.flatbuffers.FlatBufferBuilder
import dev.slimevr.protocol.GenericConnection
import dev.slimevr.protocol.ProtocolAPI
import dev.slimevr.protocol.rpc.RPCHandler
import dev.slimevr.trackingpause.TrackingPauseListener
import solarxr_protocol.rpc.RpcMessage
import solarxr_protocol.rpc.RpcMessageHeader
import solarxr_protocol.rpc.TrackingPauseStateResponse
import java.nio.ByteBuffer

class RPCTrackingPause(private val rpcHandler: RPCHandler, private val api: ProtocolAPI) : TrackingPauseListener {

	private val currentPauseState
		get() = api.server.humanPoseManager.skeleton.getPauseTracking()

	init {
		rpcHandler.registerPacketListener(
			RpcMessage.TrackingPauseStateRequest,
			::onTrackingPauseStateRequest,
		)

		// HumanPoseManager might not be immediately available, so queue the server
		// to register the listener once it's fully initialized
		api.server.queueTask {
			api.server.humanPoseManager.trackingPauseHandler.addListener(this)
		}
	}

	private fun getPauseStateResponse(trackingPaused: Boolean): ByteBuffer {
		val fbb = FlatBufferBuilder(32)
		val state = TrackingPauseStateResponse.createTrackingPauseStateResponse(fbb, trackingPaused)
		val outbound = rpcHandler.createRPCMessage(fbb, RpcMessage.TrackingPauseStateResponse, state)
		fbb.finish(outbound)
		return fbb.dataBuffer()
	}

	private fun onTrackingPauseStateRequest(conn: GenericConnection, @Suppress("UNUSED_PARAMETER") messageHeader: RpcMessageHeader) {
		conn.send(getPauseStateResponse(currentPauseState))
	}

	override fun onTrackingPause(trackingPaused: Boolean) {
		val pauseState = getPauseStateResponse(trackingPaused)
		forAllListeners { it.send(pauseState) }
	}

	private fun forAllListeners(action: (GenericConnection) -> Unit) {
		api.apiServers.forEach {
			it.apiConnections.forEach(action)
		}
	}
}
