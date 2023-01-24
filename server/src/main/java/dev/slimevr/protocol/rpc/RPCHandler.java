package dev.slimevr.protocol.rpc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.jme3.math.Quaternion;
import dev.slimevr.autobone.AutoBone.Epoch;
import dev.slimevr.autobone.AutoBoneListener;
import dev.slimevr.autobone.AutoBoneProcessType;
import dev.slimevr.config.OverlayConfig;
import dev.slimevr.poserecorder.PoseFrames;
import dev.slimevr.protocol.GenericConnection;
import dev.slimevr.protocol.ProtocolAPI;
import dev.slimevr.protocol.ProtocolHandler;
import dev.slimevr.protocol.rpc.serial.RPCSerialHandler;
import dev.slimevr.protocol.rpc.settings.RPCSettingsHandler;
import dev.slimevr.vr.processor.skeleton.SkeletonConfigOffsets;
import dev.slimevr.vr.trackers.IMUTracker;
import dev.slimevr.vr.trackers.Tracker;
import dev.slimevr.vr.trackers.TrackerPosition;
import io.eiren.util.logging.LogManager;
import solarxr_protocol.MessageBundle;
import solarxr_protocol.datatypes.TransactionId;
import solarxr_protocol.rpc.*;

import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;


public class RPCHandler extends ProtocolHandler<RpcMessageHeader>
	implements AutoBoneListener {

	private final ProtocolAPI api;

	/**
	 * Unsigned int!
	 */
	private long currTransactionId = 0;

	public RPCHandler(ProtocolAPI api) {
		super();
		this.api = api;

		new RPCSerialHandler(this, api);
		new RPCSettingsHandler(this, api);

		registerPacketListener(RpcMessage.ResetRequest, this::onResetRequest);
		registerPacketListener(RpcMessage.AssignTrackerRequest, this::onAssignTrackerRequest);


		registerPacketListener(RpcMessage.RecordBVHRequest, this::onRecordBVHRequest);

		registerPacketListener(RpcMessage.SkeletonResetAllRequest, this::onSkeletonResetAllRequest);
		registerPacketListener(RpcMessage.SkeletonConfigRequest, this::onSkeletonConfigRequest);
		registerPacketListener(
			RpcMessage.ChangeSkeletonConfigRequest,
			this::onChangeSkeletonConfigRequest
		);

		registerPacketListener(RpcMessage.AutoBoneProcessRequest, this::onAutoBoneProcessRequest);

		registerPacketListener(
			RpcMessage.OverlayDisplayModeChangeRequest,
			this::onOverlayDisplayModeChangeRequest
		);
		registerPacketListener(
			RpcMessage.OverlayDisplayModeRequest,
			this::onOverlayDisplayModeRequest
		);

		this.api.server.getAutoBoneHandler().addListener(this);
	}

	private void onOverlayDisplayModeRequest(
		GenericConnection conn,
		RpcMessageHeader messageHeader
	) {
		FlatBufferBuilder fbb = new FlatBufferBuilder(32);
		OverlayConfig config = this.api.server.getConfigManager().getVrConfig().getOverlay();
		int response = OverlayDisplayModeResponse
			.createOverlayDisplayModeResponse(fbb, config.isVisible(), config.isMirrored());
		int outbound = this.createRPCMessage(fbb, RpcMessage.OverlayDisplayModeResponse, response);
		fbb.finish(outbound);
		conn.send(fbb.dataBuffer());
	}

	private void onOverlayDisplayModeChangeRequest(
		GenericConnection conn,
		RpcMessageHeader messageHeader
	) {
		OverlayDisplayModeChangeRequest req = (OverlayDisplayModeChangeRequest) messageHeader
			.message(new OverlayDisplayModeChangeRequest());
		if (req == null)
			return;
		OverlayConfig config = this.api.server.getConfigManager().getVrConfig().getOverlay();
		config.setMirrored(req.isMirrored());
		config.setVisible(req.isVisible());

		this.api.server.getConfigManager().saveConfig();
	}

	public void onSkeletonResetAllRequest(GenericConnection conn, RpcMessageHeader messageHeader) {
		SkeletonResetAllRequest req = (SkeletonResetAllRequest) messageHeader
			.message(new SkeletonResetAllRequest());
		if (req == null)
			return;

		this.api.server.humanPoseProcessor.getSkeletonConfig().resetOffsets();
		this.api.server.humanPoseProcessor.getSkeletonConfig().save();
		this.api.server.getConfigManager().saveConfig();

		// might not be a good idea maybe let the client ask again
		FlatBufferBuilder fbb = new FlatBufferBuilder(300);
		int config = RPCBuilder.createSkeletonConfig(fbb, this.api.server.humanPoseProcessor);
		int outbound = this.createRPCMessage(fbb, RpcMessage.SkeletonConfigResponse, config);
		fbb.finish(outbound);
		conn.send(fbb.dataBuffer());
	}

	public void onSkeletonConfigRequest(GenericConnection conn, RpcMessageHeader messageHeader) {
		SkeletonConfigRequest req = (SkeletonConfigRequest) messageHeader
			.message(new SkeletonConfigRequest());
		if (req == null)
			return;

		FlatBufferBuilder fbb = new FlatBufferBuilder(300);
		int config = RPCBuilder.createSkeletonConfig(fbb, this.api.server.humanPoseProcessor);
		int outbound = this.createRPCMessage(fbb, RpcMessage.SkeletonConfigResponse, config);
		fbb.finish(outbound);
		conn.send(fbb.dataBuffer());
	}

	public void onChangeSkeletonConfigRequest(
		GenericConnection conn,
		RpcMessageHeader messageHeader
	) {
		ChangeSkeletonConfigRequest req = (ChangeSkeletonConfigRequest) messageHeader
			.message(new ChangeSkeletonConfigRequest());
		if (req == null)
			return;

		SkeletonConfigOffsets joint = SkeletonConfigOffsets.getById(req.getBone());

		this.api.server.humanPoseProcessor.setSkeletonConfig(joint, req.getValue());
		this.api.server.humanPoseProcessor.getSkeletonConfig().save();
		this.api.server.getConfigManager().saveConfig();
	}

	public void onRecordBVHRequest(GenericConnection conn, RpcMessageHeader messageHeader) {
		RecordBVHRequest req = (RecordBVHRequest) messageHeader.message(new RecordBVHRequest());
		if (req == null)
			return;

		if (req.getStop()) {
			if (this.api.server.getBvhRecorder().isRecording())
				this.api.server.getBvhRecorder().endRecording();
		} else {
			if (!this.api.server.getBvhRecorder().isRecording())
				this.api.server.getBvhRecorder().startRecording();
		}

		FlatBufferBuilder fbb = new FlatBufferBuilder(40);
		int status = RecordBVHStatus
			.createRecordBVHStatus(fbb, this.api.server.getBvhRecorder().isRecording());
		int outbound = this.createRPCMessage(fbb, RpcMessage.RecordBVHStatus, status);
		fbb.finish(outbound);
		conn.send(fbb.dataBuffer());
	}

	public void onResetRequest(GenericConnection conn, RpcMessageHeader messageHeader) {
		ResetRequest req = (ResetRequest) messageHeader.message(new ResetRequest());
		if (req == null)
			return;

		if (req.getResetType() == ResetType.Quick)
			this.api.server.resetTrackersYaw();
		if (req.getResetType() == ResetType.Full)
			this.api.server.resetTrackers();
		if (req.getResetType() == ResetType.Mounting)
			this.api.server.resetTrackersMounting();
		LogManager.info("[WebSocketAPI] Reset performed");
	}

	public void onAssignTrackerRequest(GenericConnection conn, RpcMessageHeader messageHeader) {
		AssignTrackerRequest req = (AssignTrackerRequest) messageHeader
			.message(new AssignTrackerRequest());
		if (req == null)
			return;

		Tracker tracker = this.api.server.getTrackerById(req.getTrackerId());
		if (tracker == null)
			return;
		tracker = tracker.get();

		TrackerPosition pos = TrackerPosition.getByBodyPart(req.getBodyPosition()).orElse(null);
		tracker.setBodyPosition(pos);

		if (req.getMountingOrientation() != null) {
			if (tracker instanceof IMUTracker imu) {
				imu
					.setMountingOrientation(
						new Quaternion(
							req.getMountingOrientation().getX(),
							req.getMountingOrientation().getY(),
							req.getMountingOrientation().getZ(),
							req.getMountingOrientation().getW()
						)
					);
			}
		}

		if (req.getDisplayName() != null) {
			if (tracker instanceof IMUTracker imu) {
				imu.setCustomName(req.getDisplayName());
			}
		}

		if (tracker instanceof IMUTracker imu) {
			imu.setAllowDriftCompensation(req.getAllowDriftCompensation());
		}

		this.api.server.trackerUpdated(tracker);
	}

	@Override
	public void onMessage(GenericConnection conn, RpcMessageHeader message) {
		BiConsumer<GenericConnection, RpcMessageHeader> consumer = this.handlers[message
			.getMessageType()];
		if (consumer != null)
			consumer.accept(conn, message);
		else
			LogManager
				.info(
					"[ProtocolAPI] Unhandled RPC packet received id: " + message.getMessageType()
				);
	}

	public int createRPCMessage(FlatBufferBuilder fbb, byte messageType, int messageOffset) {
		int[] data = new int[1];

		RpcMessageHeader.startRpcMessageHeader(fbb);
		RpcMessageHeader.addMessage(fbb, messageOffset);
		RpcMessageHeader.addMessageType(fbb, messageType);
		RpcMessageHeader
			.addTxId(
				fbb,
				TransactionId.createTransactionId(fbb, (int) currTransactionId++)
			);
		data[0] = RpcMessageHeader.endRpcMessageHeader(fbb);

		int messages = MessageBundle.createRpcMsgsVector(fbb, data);

		MessageBundle.startMessageBundle(fbb);
		MessageBundle.addRpcMsgs(fbb, messages);
		return MessageBundle.endMessageBundle(fbb);
	}

	@Override
	public int messagesCount() {
		return RpcMessage.Companion.getNames().length;
	}

	public void onAutoBoneProcessRequest(GenericConnection conn, RpcMessageHeader messageHeader) {
		AutoBoneProcessRequest req = (AutoBoneProcessRequest) messageHeader
			.message(new AutoBoneProcessRequest());
		if (req == null || conn.getContext().useAutoBone())
			return;

		conn.getContext().setUseAutoBone(true);
		this.api.server
			.getAutoBoneHandler()
			.startProcessByType(
				Objects.requireNonNull(AutoBoneProcessType.getById(req.getProcessType()))
			);
	}

	/**
	 *
	 * @param processType
	 * @param message
	 * @param current Unsigned int!
	 * @param total Unsigned int!
	 * @param completed
	 * @param success
	 */
	@Override
	public void onAutoBoneProcessStatus(
		AutoBoneProcessType processType,
		String message,
		long current,
		long total,
		boolean completed,
		boolean success
	) {
		this.api
			.getAPIServers()
			.forEach(
				(server) -> server
					.getAPIConnections()
					.filter(conn -> conn.getContext().useAutoBone())
					.forEach((conn) -> {
						FlatBufferBuilder fbb = new FlatBufferBuilder(32);

						Integer messageOffset = message != null ? fbb.createString(message) : null;

						AutoBoneProcessStatusResponse
							.startAutoBoneProcessStatusResponse(fbb);
						AutoBoneProcessStatusResponse
							.addProcessType(fbb, processType.getId());
						if (messageOffset != null)
							AutoBoneProcessStatusResponse.addMessage(fbb, messageOffset);
						if (total > 0 && current >= 0) {
							AutoBoneProcessStatusResponse.addCurrent(fbb, (int) current);
							AutoBoneProcessStatusResponse.addTotal(fbb, (int) total);
						}
						AutoBoneProcessStatusResponse.addCompleted(fbb, completed);
						AutoBoneProcessStatusResponse.addSuccess(fbb, success);
						int update = AutoBoneProcessStatusResponse
							.endAutoBoneProcessStatusResponse(fbb);
						int outbound = this
							.createRPCMessage(
								fbb,
								RpcMessage.AutoBoneProcessStatusResponse,
								update
							);
						fbb.finish(outbound);

						conn.send(fbb.dataBuffer());
						if (completed) {
							conn.getContext().setUseAutoBone(false);
						}
					})
			);
	}

	@Override
	public void onAutoBoneRecordingEnd(PoseFrames recording) {
		// Do nothing, this is broadcasted by "onAutoBoneProcessStatus" uwu
	}

	@Override
	public void onAutoBoneEpoch(Epoch epoch) {
		this.api
			.getAPIServers()
			.forEach(
				(server) -> server
					.getAPIConnections()
					.filter(conn -> conn.getContext().useAutoBone())
					.forEach((conn) -> {
						FlatBufferBuilder fbb = new FlatBufferBuilder(32);

						int[] skeletonPartOffsets = new int[epoch.configValues.size()];
						int i = 0;
						for (
							Entry<SkeletonConfigOffsets, Float> skeletonConfig : epoch.configValues
								.entrySet()
						) {
							skeletonPartOffsets[i++] = SkeletonPart
								.createSkeletonPart(
									fbb,
									skeletonConfig.getKey().getId(),
									skeletonConfig.getValue()
								);
						}

						int skeletonPartsOffset = AutoBoneEpochResponse
							.createAdjustedSkeletonPartsVector(fbb, skeletonPartOffsets);

						int update = AutoBoneEpochResponse
							.createAutoBoneEpochResponse(
								fbb,
								epoch.epoch,
								epoch.totalEpochs,
								epoch.epochError.getMean(),
								skeletonPartsOffset
							);
						int outbound = this
							.createRPCMessage(fbb, RpcMessage.AutoBoneEpochResponse, update);
						fbb.finish(outbound);

						conn.send(fbb.dataBuffer());
					})
			);
	}


	@Override
	public void onAutoBoneEnd(EnumMap<SkeletonConfigOffsets, Float> configValues) {
		// Do nothing, the last epoch from "onAutoBoneEpoch" should be all
		// that's needed
	}
}
