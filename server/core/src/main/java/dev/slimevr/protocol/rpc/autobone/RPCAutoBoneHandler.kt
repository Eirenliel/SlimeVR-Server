package dev.slimevr.protocol.rpc.autobone

import com.google.flatbuffers.FlatBufferBuilder
import dev.slimevr.autobone.AutoBone.Epoch
import dev.slimevr.autobone.AutoBoneListener
import dev.slimevr.autobone.AutoBoneProcessType
import dev.slimevr.autobone.AutoBoneProcessType.Companion.getById
import dev.slimevr.poseframeformat.PoseFrames
import dev.slimevr.protocol.GenericConnection
import dev.slimevr.protocol.ProtocolAPI
import dev.slimevr.protocol.rpc.RPCBuilder
import dev.slimevr.protocol.rpc.RPCHandler
import dev.slimevr.tracking.processor.config.SkeletonConfigOffsets
import solarxr_protocol.rpc.AutoBoneEpochResponse
import solarxr_protocol.rpc.AutoBoneProcessRequest
import solarxr_protocol.rpc.AutoBoneProcessStatusResponse
import solarxr_protocol.rpc.RpcMessage
import solarxr_protocol.rpc.RpcMessageHeader
import solarxr_protocol.rpc.SkeletonPart
import java.util.*

class RPCAutoBoneHandler(
	private val rpcHandler: RPCHandler,
	val api: ProtocolAPI,
) : AutoBoneListener {
	init {
		rpcHandler.registerPacketListener(
			RpcMessage.AutoBoneProcessRequest,
			::onAutoBoneProcessRequest,
		)
		rpcHandler.registerPacketListener(
			RpcMessage.AutoBoneApplyRequest,
			::onAutoBoneApplyRequest,
		)
		rpcHandler.registerPacketListener(
			RpcMessage.AutoBoneStopRecordingRequest,
			::onAutoBoneStopRecordingRequest,
		)
		rpcHandler.registerPacketListener(
			RpcMessage.AutoBoneCancelRecordingRequest,
			::onAutoBoneCancelRecordingRequest,
		)

		this.api.server.autoBoneHandler.addListener(this)
	}

	fun onAutoBoneProcessRequest(
		conn: GenericConnection,
		messageHeader: RpcMessageHeader,
	) {
		val req = messageHeader
			.message(AutoBoneProcessRequest()) as AutoBoneProcessRequest
		if (conn.context.useAutoBone()) return
		conn.context.setUseAutoBone(true)
		api.server
			.autoBoneHandler
			.startProcessByType(getById(req.processType()))
	}

	override fun onAutoBoneProcessStatus(
		processType: AutoBoneProcessType,
		message: String?,
		current: Long,
		total: Long,
		eta: Float,
		completed: Boolean,
		success: Boolean,
	) {
		forAllListeners { conn ->
			if (!conn.context.useAutoBone()) {
				return@forAllListeners
			}

			val fbb = FlatBufferBuilder(32)

			AutoBoneProcessStatusResponse.startAutoBoneProcessStatusResponse(fbb)
			AutoBoneProcessStatusResponse.addProcessType(
				fbb,
				processType.id,
			)

			AutoBoneProcessStatusResponse.addCurrent(fbb, current)
			AutoBoneProcessStatusResponse.addTotal(fbb, total)
			AutoBoneProcessStatusResponse.addEta(fbb, eta)
			AutoBoneProcessStatusResponse.addCompleted(fbb, completed)
			AutoBoneProcessStatusResponse.addSuccess(fbb, success)

			val update = AutoBoneProcessStatusResponse
				.endAutoBoneProcessStatusResponse(fbb)
			val outbound: Int = rpcHandler.createRPCMessage(
				fbb,
				RpcMessage.AutoBoneProcessStatusResponse,
				update,
			)
			fbb.finish(outbound)
			conn.send(fbb.dataBuffer())
			if (completed) {
				conn.context.setUseAutoBone(false)
			}
		}
	}

	override fun onAutoBoneRecordingEnd(recording: PoseFrames) {
		// Do nothing, this is broadcasted by "onAutoBoneProcessStatus" uwu
	}

	override fun onAutoBoneEpoch(epoch: Epoch) {
		forAllListeners { conn ->
			if (!conn.context.useAutoBone()) {
				return@forAllListeners
			}

			val fbb = FlatBufferBuilder(32)

			val skeletonPartsOffset = AutoBoneEpochResponse
				.createAdjustedSkeletonPartsVector(
					fbb,
					epoch.configValues.map { (key, value) ->
						SkeletonPart.createSkeletonPart(fbb, key.id, value)
					}.toIntArray(),
				)
			val update = AutoBoneEpochResponse
				.createAutoBoneEpochResponse(
					fbb,
					epoch.epoch.toLong(),
					epoch.totalEpochs.toLong(),
					epoch.epochError.mean,
					skeletonPartsOffset,
				)
			val outbound: Int = rpcHandler.createRPCMessage(
				fbb,
				RpcMessage.AutoBoneEpochResponse,
				update,
			)
			fbb.finish(outbound)
			conn.send(fbb.dataBuffer())
		}
	}

	override fun onAutoBoneEnd(configValues: EnumMap<SkeletonConfigOffsets, Float>) {
		// Do nothing, the last epoch from "onAutoBoneEpoch" should be all
		// that's needed
	}

	private fun onAutoBoneApplyRequest(
		conn: GenericConnection,
		@Suppress("UNUSED_PARAMETER")messageHeader: RpcMessageHeader,
	) {
		api.server.autoBoneHandler.applyValues()

		// Send the new body proportions, this is to update the listener's state
		val fbb = FlatBufferBuilder(300)
		val outbound = rpcHandler.createRPCMessage(
			fbb,
			RpcMessage.SkeletonConfigResponse,
			RPCBuilder.createSkeletonConfig(fbb, api.server.humanPoseManager),
		)
		fbb.finish(outbound)
		conn.send(fbb.dataBuffer())
	}

	private fun onAutoBoneStopRecordingRequest(
		@Suppress("UNUSED_PARAMETER")conn: GenericConnection,
		@Suppress("UNUSED_PARAMETER")messageHeader: RpcMessageHeader,
	) {
		api.server.autoBoneHandler.stopRecording()
	}

	private fun onAutoBoneCancelRecordingRequest(
		@Suppress("UNUSED_PARAMETER") conn: GenericConnection,
		@Suppress("UNUSED_PARAMETER") messageHeader: RpcMessageHeader,
	) {
		api.server.autoBoneHandler.cancelRecording()
	}

	private fun forAllListeners(action: (GenericConnection) -> Unit) {
		api.apiServers.forEach {
			it.apiConnections.forEach(action)
		}
	}
}
