package dev.slimevr.protocol.datafeed;

import com.google.flatbuffers.FlatBufferBuilder;
import dev.slimevr.tracking.trackers.Device;
import dev.slimevr.tracking.trackers.Tracker;
import dev.slimevr.tracking.trackers.udp.UDPDevice;
import io.github.axisangles.ktmath.Quaternion;
import io.github.axisangles.ktmath.Vector3;
import solarxr_protocol.data_feed.Bone;
import solarxr_protocol.data_feed.DataFeedUpdate;
import solarxr_protocol.data_feed.device_data.DeviceData;
import solarxr_protocol.data_feed.device_data.DeviceDataMaskT;
import solarxr_protocol.data_feed.tracker.TrackerData;
import solarxr_protocol.data_feed.tracker.TrackerDataMaskT;
import solarxr_protocol.data_feed.tracker.TrackerInfo;
import solarxr_protocol.datatypes.DeviceId;
import solarxr_protocol.datatypes.Ipv4Address;
import solarxr_protocol.datatypes.Temperature;
import solarxr_protocol.datatypes.TrackerId;
import solarxr_protocol.datatypes.hardware_info.HardwareInfo;
import solarxr_protocol.datatypes.hardware_info.HardwareStatus;
import solarxr_protocol.datatypes.math.Quat;
import solarxr_protocol.datatypes.math.Vec3f;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class DataFeedBuilder {

	public static int createHardwareInfo(FlatBufferBuilder fbb, Device device) {
		int nameOffset = device.getFirmwareVersion() != null
			? fbb.createString(device.getFirmwareVersion())
			: 0;

		int manufacturerOffset = device.getManufacturer() != null
			? fbb.createString(device.getManufacturer())
			: 0;

		int boardTypeOffset = fbb.createString(device.getBoardType().toString());

		int hardwareIdentifierOffset = fbb.createString(device.getHardwareIdentifier());

		HardwareInfo.startHardwareInfo(fbb);
		HardwareInfo.addFirmwareVersion(fbb, nameOffset);
		HardwareInfo.addManufacturer(fbb, manufacturerOffset);
		HardwareInfo.addHardwareIdentifier(fbb, hardwareIdentifierOffset);

		if (device instanceof UDPDevice udpDevice) {
			var address = udpDevice.getIpAddress().getAddress();
			HardwareInfo
				.addIpAddress(
					fbb,
					Ipv4Address
						.createIpv4Address(
							fbb,
							ByteBuffer.wrap(address).getInt()
						)
				);

			HardwareInfo.addNetworkProtocolVersion(fbb, udpDevice.firmwareBuild);
		}

		// BRUH MOMENT
		// TODO need support: HardwareInfo.addHardwareRevision(fbb,
		// hardwareRevisionOffset);
		// TODO need support: HardwareInfo.addDisplayName(fbb, de);

		HardwareInfo.addMcuId(fbb, device.getMcuType().getSolarType());
		HardwareInfo.addBoardType(fbb, boardTypeOffset);
		return HardwareInfo.endHardwareInfo(fbb);
	}

	public static int createTrackerId(FlatBufferBuilder fbb, Tracker tracker) {
		TrackerId.startTrackerId(fbb);

		TrackerId.addTrackerNum(fbb, tracker.getTrackerNum());
		if (tracker.getDevice() != null)
			TrackerId.addDeviceId(fbb, DeviceId.createDeviceId(fbb, tracker.getDevice().getId()));

		return TrackerId.endTrackerId(fbb);
	}

	public static int createQuat(FlatBufferBuilder fbb, Quaternion quaternion) {
		return Quat
			.createQuat(
				fbb,
				quaternion.getX(),
				quaternion.getY(),
				quaternion.getZ(),
				quaternion.getW()
			);
	}

	public static int createTrackerInfos(
		FlatBufferBuilder fbb,
		boolean infoMask,
		Tracker tracker
	) {

		if (!infoMask)
			return 0;

		int displayNameOffset = fbb.createString(tracker.getDisplayName());
		int customNameOffset = tracker.getCustomName() != null
			? fbb.createString(tracker.getCustomName())
			: 0;

		TrackerInfo.startTrackerInfo(fbb);
		if (tracker.getTrackerPosition() != null)
			TrackerInfo.addBodyPart(fbb, tracker.getTrackerPosition().getBodyPart());
		TrackerInfo.addEditable(fbb, tracker.getUserEditable());
		TrackerInfo.addIsComputed(fbb, tracker.isComputed());
		TrackerInfo.addDisplayName(fbb, displayNameOffset);
		TrackerInfo.addCustomName(fbb, customNameOffset);
		if (tracker.getImuType() != null) {
			TrackerInfo.addImuType(fbb, tracker.getImuType().getSolarType());
		}

		// TODO need support: TrackerInfo.addPollRate(fbb, tracker.);

		if (tracker.isImu()) {
			TrackerInfo.addIsImu(fbb, true);
			TrackerInfo
				.addAllowDriftCompensation(
					fbb,
					tracker.getResetsHandler().getAllowDriftCompensation()
				);
		} else {
			TrackerInfo.addIsImu(fbb, false);
			TrackerInfo.addAllowDriftCompensation(fbb, false);
		}

		if (tracker.getNeedsMounting()) {
			Quaternion quaternion = tracker.getResetsHandler().getMountingOrientation();
			Quaternion mountResetFix = tracker.getResetsHandler().getMountRotFix();
			TrackerInfo.addMountingOrientation(fbb, createQuat(fbb, quaternion));
			TrackerInfo.addMountingResetOrientation(fbb, createQuat(fbb, mountResetFix));
		}

		TrackerInfo.addMagnetometer(fbb, tracker.getMagStatus().getSolarType());
		TrackerInfo.addIsHmd(fbb, tracker.isHmd());

		return TrackerInfo.endTrackerInfo(fbb);
	}

	public static int createTrackerPosition(FlatBufferBuilder fbb, Tracker tracker) {
		Vector3 pos = tracker.getPosition();
		return Vec3f
			.createVec3f(
				fbb,
				pos.getX(),
				pos.getY(),
				pos.getZ()
			);
	}

	public static int createTrackerRotation(FlatBufferBuilder fbb, Tracker tracker) {
		return createQuat(fbb, tracker.getRawRotation());
	}

	public static int createTrackerAcceleration(FlatBufferBuilder fbb, Tracker tracker) {
		Vector3 accel = tracker.getAcceleration();
		return Vec3f
			.createVec3f(
				fbb,
				accel.getX(),
				accel.getY(),
				accel.getZ()
			);
	}

	public static int createTrackerTemperature(FlatBufferBuilder fbb, Tracker tracker) {
		if (tracker.getTemperature() == null)
			return 0;
		return Temperature.createTemperature(fbb, tracker.getTemperature());
	}

	public static int createTrackerData(
		FlatBufferBuilder fbb,
		TrackerDataMaskT mask,
		Tracker tracker
	) {
		int trackerInfosOffset = DataFeedBuilder.createTrackerInfos(fbb, mask.getInfo(), tracker);
		int trackerIdOffset = DataFeedBuilder.createTrackerId(fbb, tracker);

		TrackerData.startTrackerData(fbb);

		TrackerData.addTrackerId(fbb, trackerIdOffset);

		if (trackerInfosOffset != 0)
			TrackerData.addInfo(fbb, trackerInfosOffset);
		if (mask.getStatus())
			TrackerData.addStatus(fbb, tracker.getStatus().getId() + 1);
		if (mask.getPosition() && tracker.getHasPosition())
			TrackerData.addPosition(fbb, DataFeedBuilder.createTrackerPosition(fbb, tracker));
		if (mask.getRotation() && tracker.getHasRotation())
			TrackerData.addRotation(fbb, DataFeedBuilder.createTrackerRotation(fbb, tracker));
		if (mask.getLinearAcceleration() && tracker.getHasAcceleration())
			TrackerData
				.addLinearAcceleration(
					fbb,
					DataFeedBuilder.createTrackerAcceleration(fbb, tracker)
				);
		if (mask.getTemp()) {
			int trackerTemperatureOffset = DataFeedBuilder.createTrackerTemperature(fbb, tracker);
			if (trackerTemperatureOffset != 0)
				TrackerData.addTemp(fbb, trackerTemperatureOffset);
		}
		if (tracker.getNeedsMounting() && tracker.getHasRotation()) {
			if (mask.getRotationReferenceAdjusted()) {
				TrackerData
					.addRotationReferenceAdjusted(fbb, createQuat(fbb, tracker.getRotation()));
			}
			if (mask.getRotationIdentityAdjusted()) {
				TrackerData
					.addRotationIdentityAdjusted(
						fbb,
						createQuat(fbb, tracker.getIdentityAdjustedRotation())
					);
			}
		} else if (tracker.getNeedsReset() && tracker.getHasRotation()) {
			if (mask.getRotationReferenceAdjusted()) {
				TrackerData
					.addRotationReferenceAdjusted(fbb, createQuat(fbb, tracker.getRotation()));
			}
			if (mask.getRotationIdentityAdjusted()) {
				TrackerData
					.addRotationIdentityAdjusted(fbb, createQuat(fbb, tracker.getRawRotation()));
			}
		}
		if (mask.getTps()) {
			TrackerData.addTps(fbb, (int) tracker.getTps());
		}

		return TrackerData.endTrackerData(fbb);
	}

	public static int createTrackersData(
		FlatBufferBuilder fbb,
		DeviceDataMaskT mask,
		Device device
	) {
		if (mask.getTrackerData() == null)
			return 0;

		List<Integer> trackersOffsets = new ArrayList<>();

		device
			.getTrackers()
			.forEach(
				(key, value) -> trackersOffsets
					.add(DataFeedBuilder.createTrackerData(fbb, mask.getTrackerData(), value))
			);

		DeviceData.startTrackersVector(fbb, trackersOffsets.size());
		trackersOffsets.forEach(offset -> DeviceData.addTrackers(fbb, offset));
		return fbb.endVector();
	}

	public static int createLogMessagesData(
		FlatBufferBuilder fbb,
		Device device
	) {
		List<String> messages = device.getLogMessages();

		int numMessages = messages.size();
		int[] messageOffsets = new int[numMessages];
		for (int i = 0; i < numMessages; ++i) {
			messageOffsets[i] = fbb.createString(messages.get(i));
		}

		return DeviceData.createLogMessagesVector(fbb, messageOffsets);
	}

	public static int createDeviceData(
		FlatBufferBuilder fbb,
		int id,
		DeviceDataMaskT mask,
		Device device
	) {
		if (!mask.getDeviceData())
			return 0;

		if (device.getTrackers().size() <= 0)
			return 0;

		Tracker firstTracker = device.getTrackers().get(0);
		if (firstTracker == null) {
			// Not actually the "first" tracker, but do we care?
			firstTracker = device.getTrackers().entrySet().iterator().next().getValue();
		}

		Tracker tracker = firstTracker;
		if (tracker == null)
			return 0;

		HardwareStatus.startHardwareStatus(fbb);
		HardwareStatus.addErrorStatus(fbb, tracker.getStatus().getId());

		if (tracker.getBatteryVoltage() != null) {
			HardwareStatus.addBatteryVoltage(fbb, tracker.getBatteryVoltage());
		}
		if (tracker.getBatteryLevel() != null) {
			HardwareStatus.addBatteryPctEstimate(fbb, (int) tracker.getBatteryLevel().floatValue());
		}
		if (tracker.getPing() != null) {
			HardwareStatus.addPing(fbb, tracker.getPing());
		}
		if (tracker.getSignalStrength() != null) {
			HardwareStatus.addRssi(fbb, (short) tracker.getSignalStrength().floatValue());
		}


		int hardwareDataOffset = HardwareStatus.endHardwareStatus(fbb);
		int hardwareInfoOffset = DataFeedBuilder.createHardwareInfo(fbb, device);
		int trackersOffset = DataFeedBuilder.createTrackersData(fbb, mask, device);

		int nameOffset = device.getName() != null
			? fbb.createString(device.getName())
			: 0;

		int logMessagesOffset = DataFeedBuilder.createLogMessagesData(fbb, device);

		DeviceData.startDeviceData(fbb);
		DeviceData.addCustomName(fbb, nameOffset);
		DeviceData.addId(fbb, DeviceId.createDeviceId(fbb, id));
		DeviceData.addHardwareStatus(fbb, hardwareDataOffset);
		DeviceData.addHardwareInfo(fbb, hardwareInfoOffset);
		DeviceData.addTrackers(fbb, trackersOffset);
		DeviceData.addLogMessages(fbb, logMessagesOffset);

		return DeviceData.endDeviceData(fbb);
	}

	public static int createSyntheticTrackersData(
		FlatBufferBuilder fbb,
		TrackerDataMaskT trackerDataMaskT,
		List<Tracker> trackers
	) {
		if (trackerDataMaskT == null)
			return 0;

		List<Integer> trackerOffsets = new ArrayList<>();

		trackers
			.forEach(
				(tracker) -> trackerOffsets
					.add(DataFeedBuilder.createTrackerData(fbb, trackerDataMaskT, tracker))
			);

		DataFeedUpdate.startSyntheticTrackersVector(fbb, trackerOffsets.size());
		trackerOffsets.forEach((tracker -> DataFeedUpdate.addSyntheticTrackers(fbb, tracker)));
		return fbb.endVector();
	}

	public static int createDevicesData(
		FlatBufferBuilder fbb,
		DeviceDataMaskT deviceDataMaskT,
		List<Device> devices
	) {
		if (deviceDataMaskT == null)
			return 0;

		int[] devicesDataOffsets = new int[devices.size()];
		for (int i = 0; i < devices.size(); i++) {
			Device device = devices.get(i);
			devicesDataOffsets[i] = DataFeedBuilder
				.createDeviceData(fbb, i, deviceDataMaskT, device);
		}

		return DataFeedUpdate.createDevicesVector(fbb, devicesDataOffsets);
	}

	public static int createBonesData(
		FlatBufferBuilder fbb,
		boolean shouldSend,
		List<dev.slimevr.tracking.processor.Bone> bones
	) {
		if (!shouldSend) {
			return 0;
		}

		var boneOffsets = new int[bones.size()];
		for (var i = 0; i < bones.size(); ++i) {
			var bi = bones.get(i);

			var headPosG = bi.getPosition();
			var rotG = bi.getGlobalRotation();
			var length = bi.getLength();

			Bone.startBone(fbb);

			var rotGOffset = createQuat(fbb, rotG);
			Bone.addRotationG(fbb, rotGOffset);
			var headPosGOffset = Vec3f
				.createVec3f(fbb, headPosG.getX(), headPosG.getY(), headPosG.getZ());
			Bone.addHeadPositionG(fbb, headPosGOffset);
			Bone.addBodyPart(fbb, bi.getBoneType().bodyPart);
			Bone.addBoneLength(fbb, length);

			boneOffsets[i] = Bone.endBone(fbb);
		}

		return DataFeedUpdate.createBonesVector(fbb, boneOffsets);
	}
}
