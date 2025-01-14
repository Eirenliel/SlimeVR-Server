package dev.slimevr.protocol.rpc

import com.google.flatbuffers.FlatBufferBuilder
import dev.slimevr.config.config
import dev.slimevr.protocol.GenericConnection
import dev.slimevr.protocol.ProtocolAPI
import dev.slimevr.protocol.ProtocolHandler
import dev.slimevr.protocol.datafeed.DataFeedBuilder
import dev.slimevr.protocol.rpc.autobone.RPCAutoBoneHandler
import dev.slimevr.protocol.rpc.firmware.RPCFirmwareUpdateHandler
import dev.slimevr.protocol.rpc.reset.RPCResetHandler
import dev.slimevr.protocol.rpc.serial.RPCProvisioningHandler
import dev.slimevr.protocol.rpc.serial.RPCSerialHandler
import dev.slimevr.protocol.rpc.settings.RPCSettingsHandler
import dev.slimevr.protocol.rpc.setup.RPCHandshakeHandler
import dev.slimevr.protocol.rpc.setup.RPCTapSetupHandler
import dev.slimevr.protocol.rpc.setup.RPCUtil.getLocalIp
import dev.slimevr.protocol.rpc.status.RPCStatusHandler
import dev.slimevr.protocol.rpc.trackingpause.RPCTrackingPause
import dev.slimevr.tracking.processor.config.SkeletonConfigOffsets
import dev.slimevr.tracking.trackers.TrackerPosition.Companion.getByBodyPart
import dev.slimevr.tracking.trackers.TrackerStatus
import dev.slimevr.tracking.trackers.TrackerUtils.getTrackerForSkeleton
import io.eiren.util.logging.LogManager
import io.github.axisangles.ktmath.Quaternion
import kotlinx.coroutines.*
import solarxr_protocol.MessageBundle
import solarxr_protocol.datatypes.TransactionId
import solarxr_protocol.rpc.*

class RPCHandler(private val api: ProtocolAPI) : ProtocolHandler<RpcMessageHeader>() {
	private var currTransactionId: Long = 0
	private val mainScope = CoroutineScope(SupervisorJob())

	init {
		RPCResetHandler(this, api)
		RPCSerialHandler(this, api)
		RPCProvisioningHandler(this, api)
		RPCSettingsHandler(this, api)
		RPCTapSetupHandler(this, api)
		RPCStatusHandler(this, api)
		RPCAutoBoneHandler(this, api)
		RPCHandshakeHandler(this, api)
		RPCTrackingPause(this, api)
		RPCFirmwareUpdateHandler(this, api)

		registerPacketListener(
			RpcMessage.ResetRequest,
		) { conn: GenericConnection, messageHeader: RpcMessageHeader ->
			this.onResetRequest(
				conn,
				messageHeader,
			)
		}
		registerPacketListener(
			RpcMessage.ClearMountingResetRequest,
		) { conn: GenericConnection, messageHeader: RpcMessageHeader ->
			this.onClearMountingResetRequest(
				conn,
				messageHeader,
			)
		}
		registerPacketListener(
			RpcMessage.AssignTrackerRequest,
		) { conn: GenericConnection, messageHeader: RpcMessageHeader ->
			this.onAssignTrackerRequest(
				conn,
				messageHeader,
			)
		}

		registerPacketListener(
			RpcMessage.ClearDriftCompensationRequest,
		) { conn: GenericConnection, messageHeader: RpcMessageHeader ->
			this.onClearDriftCompensationRequest(
				conn,
				messageHeader,
			)
		}

		registerPacketListener(
			RpcMessage.RecordBVHRequest,
		) { conn: GenericConnection, messageHeader: RpcMessageHeader ->
			this.onRecordBVHRequest(
				conn,
				messageHeader,
			)
		}

		registerPacketListener(
			RpcMessage.SkeletonResetAllRequest,
		) { conn: GenericConnection, messageHeader: RpcMessageHeader ->
			this.onSkeletonResetAllRequest(
				conn,
				messageHeader,
			)
		}
		registerPacketListener(
			RpcMessage.SkeletonConfigRequest,
		) { conn: GenericConnection, messageHeader: RpcMessageHeader ->
			this.onSkeletonConfigRequest(
				conn,
				messageHeader,
			)
		}
		registerPacketListener(
			RpcMessage.ChangeSkeletonConfigRequest,
		) { conn: GenericConnection, messageHeader: RpcMessageHeader ->
			this.onChangeSkeletonConfigRequest(
				conn,
				messageHeader,
			)
		}

		registerPacketListener(
			RpcMessage.OverlayDisplayModeChangeRequest,
		) { conn: GenericConnection, messageHeader: RpcMessageHeader ->
			this.onOverlayDisplayModeChangeRequest(
				conn,
				messageHeader,
			)
		}
		registerPacketListener(
			RpcMessage.OverlayDisplayModeRequest,
		) { conn: GenericConnection, messageHeader: RpcMessageHeader ->
			this.onOverlayDisplayModeRequest(
				conn,
				messageHeader,
			)
		}

		registerPacketListener(
			RpcMessage.ServerInfosRequest,
		) { conn: GenericConnection, messageHeader: RpcMessageHeader ->
			this.onServerInfosRequest(
				conn,
				messageHeader,
			)
		}

		registerPacketListener(
			RpcMessage.LegTweaksTmpChange,
		) { conn: GenericConnection, messageHeader: RpcMessageHeader ->
			this.onLegTweaksTmpChange(
				conn,
				messageHeader,
			)
		}

		registerPacketListener(
			RpcMessage.LegTweaksTmpClear,
		) { conn: GenericConnection, messageHeader: RpcMessageHeader ->
			this.onLegTweaksTmpClear(
				conn,
				messageHeader,
			)
		}

		registerPacketListener(
			RpcMessage.StatusSystemRequest,
		) { conn: GenericConnection, messageHeader: RpcMessageHeader ->
			this.onStatusSystemRequest(
				conn,
				messageHeader,
			)
		}

		registerPacketListener(
			RpcMessage.SetPauseTrackingRequest,
		) { conn: GenericConnection, messageHeader: RpcMessageHeader ->
			this.onSetPauseTrackingRequest(
				conn,
				messageHeader,
			)
		}

		registerPacketListener(
			RpcMessage.HeightRequest,
		) { conn: GenericConnection, messageHeader: RpcMessageHeader ->
			this.onHeightRequest(
				conn,
				messageHeader,
			)
		}

		registerPacketListener(
			RpcMessage.MagToggleRequest,
		) { conn: GenericConnection, messageHeader: RpcMessageHeader ->
			this.onMagToggleRequest(conn, messageHeader)
		}

		registerPacketListener(
			RpcMessage.ChangeMagToggleRequest,
		) { conn: GenericConnection, messageHeader: RpcMessageHeader ->
			this.onChangeMagToggleRequest(conn, messageHeader)
		}
	}

	private fun onServerInfosRequest(
		conn: GenericConnection,
		messageHeader: RpcMessageHeader,
	) {
		val fbb = FlatBufferBuilder(32)

		val localIp = getLocalIp() ?: return
		val response = ServerInfosResponse
			.createServerInfosResponse(fbb, fbb.createString(localIp))
		val outbound = this.createRPCMessage(fbb, RpcMessage.ServerInfosResponse, response)
		fbb.finish(outbound)
		conn.send(fbb.dataBuffer())
	}

	private fun onOverlayDisplayModeRequest(
		conn: GenericConnection,
		messageHeader: RpcMessageHeader,
	) {
		val fbb = FlatBufferBuilder(32)
		val config = api.server.configManager.vrConfig.overlay
		val response = OverlayDisplayModeResponse
			.createOverlayDisplayModeResponse(fbb, config.isVisible, config.isMirrored)
		val outbound = this.createRPCMessage(fbb, RpcMessage.OverlayDisplayModeResponse, response)
		fbb.finish(outbound)
		conn.send(fbb.dataBuffer())
	}

	private fun onOverlayDisplayModeChangeRequest(
		conn: GenericConnection,
		messageHeader: RpcMessageHeader,
	) {
		val req = messageHeader
			.message(OverlayDisplayModeChangeRequest()) as? OverlayDisplayModeChangeRequest ?: return
		val config = api.server.configManager.vrConfig.overlay
		config.isMirrored = req.isMirrored
		config.isVisible = req.isVisible

		api.server.configManager.saveConfig()
	}

	fun onSkeletonResetAllRequest(conn: GenericConnection, messageHeader: RpcMessageHeader) {
		if (messageHeader
				.message(SkeletonResetAllRequest()) !is SkeletonResetAllRequest
		) {
			return
		}

		api.server.humanPoseManager.resetOffsets()
		api.server.humanPoseManager.saveConfig()
		api.server.configManager.saveConfig()

		// might not be a good idea maybe let the client ask again
		val fbb = FlatBufferBuilder(300)
		val config = RPCBuilder.createSkeletonConfig(fbb, api.server.humanPoseManager)
		val outbound = this.createRPCMessage(fbb, RpcMessage.SkeletonConfigResponse, config)
		fbb.finish(outbound)
		conn.send(fbb.dataBuffer())
	}

	fun onSkeletonConfigRequest(conn: GenericConnection, messageHeader: RpcMessageHeader) {
		if (messageHeader
				.message(SkeletonConfigRequest()) !is SkeletonConfigRequest
		) {
			return
		}

		val fbb = FlatBufferBuilder(300)
		val config = RPCBuilder.createSkeletonConfig(fbb, api.server.humanPoseManager)
		val outbound = this.createRPCMessage(fbb, RpcMessage.SkeletonConfigResponse, config)
		fbb.finish(outbound)
		conn.send(fbb.dataBuffer())
	}

	fun onChangeSkeletonConfigRequest(
		conn: GenericConnection,
		messageHeader: RpcMessageHeader,
	) {
		val req = messageHeader
			.message(ChangeSkeletonConfigRequest()) as? ChangeSkeletonConfigRequest ?: return

		val joint = SkeletonConfigOffsets.getById(req.bone())

		api.server.humanPoseManager.setOffset(joint, req.value())
		api.server.humanPoseManager.saveConfig()
		api.server.configManager.saveConfig()
	}

	fun onRecordBVHRequest(conn: GenericConnection, messageHeader: RpcMessageHeader) {
		val req = messageHeader.message(RecordBVHRequest()) as? RecordBVHRequest ?: return

		if (req.stop()) {
			if (api.server.bvhRecorder.isRecording) api.server.bvhRecorder.endRecording()
		} else {
			if (!api.server.bvhRecorder.isRecording) api.server.bvhRecorder.startRecording()
		}

		val fbb = FlatBufferBuilder(40)
		val status = RecordBVHStatus
			.createRecordBVHStatus(fbb, api.server.bvhRecorder.isRecording)
		val outbound = this.createRPCMessage(fbb, RpcMessage.RecordBVHStatus, status)
		fbb.finish(outbound)
		conn.send(fbb.dataBuffer())
	}

	fun onResetRequest(conn: GenericConnection, messageHeader: RpcMessageHeader) {
		val req = messageHeader.message(ResetRequest()) as? ResetRequest ?: return

		if (req.resetType() == ResetType.Yaw) api.server.resetTrackersYaw(RESET_SOURCE_NAME)
		if (req.resetType() == ResetType.Full) api.server.resetTrackersFull(RESET_SOURCE_NAME)
		if (req.resetType() == ResetType.Mounting) api.server.resetTrackersMounting(RESET_SOURCE_NAME)
	}

	fun onClearMountingResetRequest(
		conn: GenericConnection,
		messageHeader: RpcMessageHeader,
	) {
		if (messageHeader
				.message(ClearMountingResetRequest()) !is ClearMountingResetRequest
		) {
			return
		}

		api.server.clearTrackersMounting(RESET_SOURCE_NAME)
	}

	fun onAssignTrackerRequest(conn: GenericConnection, messageHeader: RpcMessageHeader) {
		val req = messageHeader
			.message(AssignTrackerRequest()) as? AssignTrackerRequest ?: return

		val tracker = api.server.getTrackerById(req.trackerId().unpack()) ?: return

		val pos = getByBodyPart(req.bodyPosition())
		val previousTracker = if (pos != null
		) {
			getTrackerForSkeleton(api.server.allTrackers, pos)
		} else {
			null
		}
		if (previousTracker != null) {
			previousTracker.trackerPosition = null
			api.server.trackerUpdated(previousTracker)
		}
		tracker.trackerPosition = pos

		if (req.mountingOrientation() != null) {
			if (tracker.needsMounting) {
				tracker
					.resetsHandler
					.mountingOrientation = Quaternion(
					req.mountingOrientation().w(),
					req.mountingOrientation().x(),
					req.mountingOrientation().y(),
					req.mountingOrientation().z(),
				)
			}
		}

		if (req.displayName() != null) {
			tracker.customName = req.displayName()
		}

		if (tracker.isImu()) {
			tracker.resetsHandler.allowDriftCompensation = req.allowDriftCompensation()
		}

		api.server.trackerUpdated(tracker)
	}

	fun onClearDriftCompensationRequest(
		conn: GenericConnection,
		messageHeader: RpcMessageHeader,
	) {
		if (messageHeader
				.message(ClearDriftCompensationRequest()) !is ClearDriftCompensationRequest
		) {
			return
		}

		api.server.clearTrackersDriftCompensation()
	}

	fun onLegTweaksTmpChange(
		conn: GenericConnection,
		messageHeader: RpcMessageHeader,
	) {
		val req = messageHeader
			.message(LegTweaksTmpChange()) as? LegTweaksTmpChange ?: return

		api.server.humanPoseManager
			.setLegTweaksStateTemp(
				req.skatingCorrection(),
				req.floorClip(),
				req.toeSnap(),
				req.footPlant(),
			)
	}

	fun onLegTweaksTmpClear(
		conn: GenericConnection,
		messageHeader: RpcMessageHeader,
	) {
		val req = messageHeader
			.message(LegTweaksTmpClear()) as? LegTweaksTmpClear ?: return

		api.server.humanPoseManager
			.clearLegTweaksStateTemp(
				req.skatingCorrection(),
				req.floorClip(),
				req.toeSnap(),
				req.footPlant(),
			)
	}

	override fun onMessage(conn: GenericConnection, message: RpcMessageHeader) {
		val consumer = handlers[message.messageType().toInt()]
		if (consumer != null) {
			consumer.accept(conn, message)
		} else {
			LogManager
				.info("[ProtocolAPI] Unhandled RPC packet received id: ${message.messageType()}")
		}
	}

	fun createRPCMessage(fbb: FlatBufferBuilder, messageType: Byte, messageOffset: Int): Int {
		val data = IntArray(1)

		RpcMessageHeader.startRpcMessageHeader(fbb)
		RpcMessageHeader.addMessage(fbb, messageOffset)
		RpcMessageHeader.addMessageType(fbb, messageType)
		RpcMessageHeader.addTxId(fbb, TransactionId.createTransactionId(fbb, currTransactionId++))
		data[0] = RpcMessageHeader.endRpcMessageHeader(fbb)

		val messages = MessageBundle.createRpcMsgsVector(fbb, data)

		MessageBundle.startMessageBundle(fbb)
		MessageBundle.addRpcMsgs(fbb, messages)
		return MessageBundle.endMessageBundle(fbb)
	}

	override fun messagesCount(): Int = RpcMessage.names.size

	fun onStatusSystemRequest(conn: GenericConnection, messageHeader: RpcMessageHeader) {
		val req = messageHeader
			.message(StatusSystemRequest()) as? StatusSystemRequest ?: return

		val statuses = api.server.statusSystem.getStatuses()

		val fbb = FlatBufferBuilder(
			statuses.size * RPCStatusHandler.STATUS_EXPECTED_SIZE,
		)
		val response = StatusSystemResponseT()
		response.currentStatuses = statuses
		val offset = StatusSystemResponse.pack(fbb, response)
		val outbound = this.createRPCMessage(fbb, RpcMessage.StatusSystemResponse, offset)
		fbb.finish(outbound)
		conn.send(fbb.dataBuffer())
	}

	fun onSetPauseTrackingRequest(conn: GenericConnection, messageHeader: RpcMessageHeader) {
		val req = messageHeader
			.message(SetPauseTrackingRequest()) as? SetPauseTrackingRequest ?: return

		api.server.setPauseTracking(req.pauseTracking(), RESET_SOURCE_NAME)
	}

	fun onHeightRequest(conn: GenericConnection, messageHeader: RpcMessageHeader?) {
		val fbb = FlatBufferBuilder(32)

		val posTrackers = api.server.allTrackers.filter { !it.isInternal && it.status == TrackerStatus.OK && it.hasPosition && it.trackerPosition != null }
		val response = if (posTrackers.isNotEmpty()) {
			HeightResponse
				.createHeightResponse(
					fbb,
					posTrackers.minOf { it.position.y },
					posTrackers.maxOf { it.position.y },
				)
		} else {
			HeightResponse
				.createHeightResponse(
					fbb,
					0f,
					0f,
				)
		}
		fbb.finish(createRPCMessage(fbb, RpcMessage.HeightResponse, response))
		conn.send(fbb.dataBuffer())
	}

	fun onMagToggleRequest(conn: GenericConnection, messageHeader: RpcMessageHeader) {
		val req = messageHeader
			.message(MagToggleRequest()) as? MagToggleRequest ?: return
		val fbb = FlatBufferBuilder(32)

		if (req.trackerId() == null) {
			val response = MagToggleResponse.createMagToggleResponse(
				fbb,
				0,
				api.server.configManager.vrConfig.server.useMagnetometerOnAllTrackers,
			)
			fbb.finish(createRPCMessage(fbb, RpcMessage.MagToggleResponse, response))
			conn.send(fbb.dataBuffer())
			return
		}

		val tracker = api.server.getTrackerById(req.trackerId().unpack()) ?: return
		val trackerId = DataFeedBuilder.createTrackerId(fbb, tracker)
		val response = MagToggleResponse.createMagToggleResponse(
			fbb,
			trackerId,
			tracker.config.shouldHaveMagEnabled == true,
		)
		fbb.finish(createRPCMessage(fbb, RpcMessage.MagToggleResponse, response))
		conn.send(fbb.dataBuffer())
	}

	fun onChangeMagToggleRequest(conn: GenericConnection, messageHeader: RpcMessageHeader) {
		val req = messageHeader
			.message(ChangeMagToggleRequest()) as? ChangeMagToggleRequest ?: return

		if (req.trackerId() == null) {
			mainScope.launch {
				withTimeoutOrNull(MAG_TIMEOUT) {
					api.server.configManager.vrConfig.server.defineMagOnAllTrackers(req.enable())
				}

				val fbb = FlatBufferBuilder(32)
				val response = MagToggleResponse.createMagToggleResponse(
					fbb,
					0,
					api.server.configManager.vrConfig.server.useMagnetometerOnAllTrackers,
				)
				fbb.finish(createRPCMessage(fbb, RpcMessage.MagToggleResponse, response))
				conn.send(fbb.dataBuffer())
			}
			return
		}

		val tracker = api.server.getTrackerById(req.trackerId().unpack()) ?: return
		if (tracker.device == null || tracker.config.shouldHaveMagEnabled == req.enable()) return
		val state = req.enable()
		tracker.config.shouldHaveMagEnabled = state
		// Don't apply magnetometer setting if use magnetometer global setting is not enabled
		if (!api.server.configManager.vrConfig.server.useMagnetometerOnAllTrackers) {
			val fbb = FlatBufferBuilder(32)
			val trackerId = DataFeedBuilder.createTrackerId(fbb, tracker)
			val response = MagToggleResponse.createMagToggleResponse(
				fbb,
				trackerId,
				state,
			)
			fbb.finish(createRPCMessage(fbb, RpcMessage.MagToggleResponse, response))
			conn.send(fbb.dataBuffer())
			return
		}

		mainScope.launch {
			withTimeoutOrNull(MAG_TIMEOUT) {
				tracker.device.setMag(state, tracker.trackerNum)
			}

			val fbb = FlatBufferBuilder(32)
			val trackerId = DataFeedBuilder.createTrackerId(fbb, tracker)
			val response = MagToggleResponse.createMagToggleResponse(
				fbb,
				trackerId,
				state,
			)
			fbb.finish(createRPCMessage(fbb, RpcMessage.MagToggleResponse, response))
			conn.send(fbb.dataBuffer())
		}
	}

	companion object {
		private const val RESET_SOURCE_NAME = "WebSocketAPI"
	}
}
const val MAG_TIMEOUT: Long = 10_000L
