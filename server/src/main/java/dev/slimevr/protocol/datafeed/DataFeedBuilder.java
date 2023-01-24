package dev.slimevr.protocol.datafeed;

import com.google.flatbuffers.FlatBufferBuilder;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.slimevr.vr.Device;
import dev.slimevr.vr.processor.skeleton.BoneInfo;
import dev.slimevr.vr.trackers.*;
import solarxr_protocol.data_feed.Bone;
import solarxr_protocol.data_feed.DataFeedUpdate;
import solarxr_protocol.data_feed.device_data.DeviceData;
import solarxr_protocol.data_feed.device_data.DeviceDataMask;
import solarxr_protocol.data_feed.tracker.TrackerData;
import solarxr_protocol.data_feed.tracker.TrackerDataMask;
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

		HardwareInfo.startHardwareInfo(fbb);
		HardwareInfo.addFirmwareVersion(fbb, nameOffset);
		HardwareInfo.addManufacturer(fbb, manufacturerOffset);
		var ipAddr = device.getIpAddress();
		if (ipAddr != null && ipAddr.getAddress() != null) {
			HardwareInfo
				.addIpAddress(
					fbb,
					Ipv4Address
						.createIpv4Address(
							fbb,
							ByteBuffer.wrap(ipAddr.getAddress()).getInt()
						)
				);
		}
		// BRUH MOMENT
		// TODO need support: HardwareInfo.addHardwareRevision(fbb,
		// hardwareRevisionOffset);
		// TODO need support: HardwareInfo.addDisplayName(fbb, de);

		// TODO need support: HardwareInfo.addMcuId(device);
		return HardwareInfo.endHardwareInfo(fbb);
	}

	public static int createTrackerId(FlatBufferBuilder fbb, Tracker tracker) {
		TrackerId.startTrackerId(fbb);

		TrackerId.addTrackerNum(fbb, (byte) tracker.getTrackerNum());
		if (tracker.getDevice() != null)
			TrackerId
				.addDeviceId(fbb, DeviceId.createDeviceId(fbb, (byte) tracker.getDevice().getId()));

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

	public static int createTrackerInfos(FlatBufferBuilder fbb, boolean infoMask, Tracker tracker) {

		if (!infoMask)
			return 0;

		int displayNameOffset = fbb.createString(tracker.getDisplayName());

		int customNameOffset = tracker.getCustomName() != null
			? fbb.createString(tracker.getCustomName())
			: 0;


		TrackerInfo.startTrackerInfo(fbb);
		if (tracker.getBodyPosition() != null)
			TrackerInfo.addBodyPart(fbb, tracker.getBodyPosition().bodyPart);
		TrackerInfo.addEditable(fbb, tracker.userEditable());
		TrackerInfo.addIsComputed(fbb, tracker.isComputed());
		TrackerInfo.addDisplayName(fbb, displayNameOffset);
		TrackerInfo.addCustomName(fbb, customNameOffset);

		// TODO need support: TrackerInfo.addImuType(fbb, tracker.im);
		// TODO need support: TrackerInfo.addPollRate(fbb, tracker.);

		if (tracker instanceof IMUTracker imuTracker) {
			TrackerInfo.addIsImu(fbb, true);

			if (imuTracker.getMountingOrientation() != null) {
				Quaternion quaternion = imuTracker.getMountingOrientation();
				TrackerInfo.addMountingOrientation(fbb, createQuat(fbb, quaternion));
			}

			TrackerInfo.addAllowDriftCompensation(fbb, imuTracker.getAllowDriftCompensation());
		} else {
			TrackerInfo.addIsImu(fbb, false);
		}

		return TrackerInfo.endTrackerInfo(fbb);
	}

	public static int createTrackerPosition(FlatBufferBuilder fbb, Tracker tracker) {
		Vector3f pos = new Vector3f();
		tracker.getPosition(pos);

		return Vec3f.createVec3f(fbb, pos.x, pos.y, pos.z);
	}

	public static int createTrackerRotation(FlatBufferBuilder fbb, Tracker tracker) {
		Quaternion quaternion = new Quaternion();
		if (tracker instanceof IMUTracker imuTracker) {
			imuTracker.getRawRotation(quaternion);
		} else {
			tracker.getRotation(quaternion);
		}

		return createQuat(fbb, quaternion);
	}

	public static int createTrackerAcceleration(FlatBufferBuilder fbb, Tracker tracker) {
		Vector3f accel = new Vector3f();
		tracker.getAcceleration(accel);

		return Vec3f.createVec3f(fbb, accel.x, accel.y, accel.z);
	}

	public static int createTrackerTemperature(FlatBufferBuilder fbb, Tracker tracker) {
		if (!(tracker instanceof IMUTracker imuTracker))
			return 0;
		return Temperature.createTemperature(fbb, imuTracker.temperature);
	}

	public static int createTrackerData(
		FlatBufferBuilder fbb,
		TrackerDataMask mask,
		Tracker tracker
	) {
		int trackerInfosOffset = DataFeedBuilder.createTrackerInfos(fbb, mask.getInfo(), tracker);
		int trackerIdOffset = DataFeedBuilder.createTrackerId(fbb, tracker);

		TrackerData.startTrackerData(fbb);

		TrackerData.addTrackerId(fbb, trackerIdOffset);

		if (trackerInfosOffset != 0)
			TrackerData.addInfo(fbb, trackerInfosOffset);
		if (mask.getStatus())
			TrackerData.addStatus(fbb, (byte) (tracker.getStatus().getId() + 1));
		if (mask.getPosition() && tracker.hasPosition())
			TrackerData.addPosition(fbb, DataFeedBuilder.createTrackerPosition(fbb, tracker));
		if (mask.getRotation() && tracker.hasRotation())
			TrackerData.addRotation(fbb, DataFeedBuilder.createTrackerRotation(fbb, tracker));
		if (mask.getLinearAcceleration() && tracker.hasAcceleration())
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
		if (tracker instanceof IMUTracker imuTracker) {
			Quaternion quaternion = new Quaternion();
			if (mask.getRotationReferenceAdjusted() && tracker.hasRotation()) {
				imuTracker.getRotation(quaternion);
				TrackerData.addRotationReferenceAdjusted(fbb, createQuat(fbb, quaternion));
			}
			if (mask.getRotationIdentityAdjusted() && tracker.hasRotation()) {
				imuTracker.getIdentityAdjustedRotation(quaternion);
				TrackerData.addRotationIdentityAdjusted(fbb, createQuat(fbb, quaternion));
			}
		}

		return TrackerData.endTrackerData(fbb);
	}

	public static int createTrackersData(
		FlatBufferBuilder fbb,
		DeviceDataMask mask,
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

	/**
	 * @param id Unsigned byte!
	 */
	public static int createDeviceData(
		FlatBufferBuilder fbb,
		byte id,
		DeviceDataMask mask,
		Device device
	) {
		if (!mask.getDeviceData())
			return 0;

		if (device.getTrackers().isEmpty())
			return 0;

		Tracker firstTracker = device.getTrackers().get(0);
		if (firstTracker == null) {
			// Not actually the "first" tracker, but do we care?
			firstTracker = device.getTrackers().entrySet().iterator().next().getValue();
		}

		Tracker tracker = firstTracker.get();
		if (tracker == null)
			return 0;

		HardwareStatus.startHardwareStatus(fbb);
		HardwareStatus.addErrorStatus(fbb, tracker.getStatus().getId());

		// FIXME: A lot of casting in here, smells suspicious ඞ
		if (tracker instanceof TrackerWithTPS)
			HardwareStatus.addTps(fbb, (byte) ((TrackerWithTPS) tracker).getTPS());

		if (tracker instanceof TrackerWithBattery twb) {
			HardwareStatus.addBatteryVoltage(fbb, twb.getBatteryVoltage());
			HardwareStatus.addBatteryPctEstimate(fbb, (byte) twb.getBatteryLevel());
		}

		if (tracker instanceof TrackerWithWireless tww) {
			HardwareStatus.addPing(fbb, (byte) tww.getPing());
			HardwareStatus.addRssi(fbb, (short) tww.getSignalStrength());
		}

		int hardwareDataOffset = HardwareStatus.endHardwareStatus(fbb);
		int hardwareInfoOffset = DataFeedBuilder.createHardwareInfo(fbb, device);
		int trackersOffset = DataFeedBuilder.createTrackersData(fbb, mask, device);

		int nameOffset = device.getCustomName() != null
			? fbb.createString(device.getCustomName())
			: 0;

		DeviceData.startDeviceData(fbb);
		DeviceData.addCustomName(fbb, nameOffset);
		DeviceData.addId(fbb, DeviceId.createDeviceId(fbb, id));
		DeviceData.addHardwareStatus(fbb, hardwareDataOffset);
		DeviceData.addHardwareInfo(fbb, hardwareInfoOffset);
		DeviceData.addTrackers(fbb, trackersOffset);

		return DeviceData.endDeviceData(fbb);
	}

	public static int createSyntheticTrackersData(
		FlatBufferBuilder fbb,
		TrackerDataMask trackerDataMask,
		List<Tracker> trackers
	) {
		if (trackerDataMask == null)
			return 0;

		List<Integer> trackerOffsets = new ArrayList<>();

		trackers
			.forEach(
				(tracker) -> trackerOffsets
					.add(DataFeedBuilder.createTrackerData(fbb, trackerDataMask, tracker))
			);

		DataFeedUpdate.startSyntheticTrackersVector(fbb, trackerOffsets.size());
		trackerOffsets.forEach((tracker -> DataFeedUpdate.addSyntheticTrackers(fbb, tracker)));
		return fbb.endVector();
	}

	public static int createDevicesData(
		FlatBufferBuilder fbb,
		DeviceDataMask deviceDataMask,
		List<Device> devices
	) {
		if (deviceDataMask == null)
			return 0;

		int[] devicesDataOffsets = new int[devices.size()];
		for (byte i = 0; i < devices.size(); i++) {
			Device device = devices.get(i);
			devicesDataOffsets[i] = DataFeedBuilder
				.createDeviceData(fbb, i, deviceDataMask, device);
		}

		return DataFeedUpdate.createDevicesVector(fbb, devicesDataOffsets);
	}

	public static int createBonesData(
		FlatBufferBuilder fbb,
		boolean shouldSend,
		List<BoneInfo> boneInfos
	) {
		if (!shouldSend) {
			return 0;
		}

		var boneOffsets = new int[boneInfos.size()];
		for (var i = 0; i < boneInfos.size(); ++i) {
			var bi = boneInfos.get(i);

			var headPosG = bi.tailNode.getParent().worldTransform.getTranslation();
			var rotG = bi.getGlobalRotation();

			// TODO: figure out why this value is stale, so that we don't need
			// to recalculate it all the time, since thats not performant.
			bi.updateLength();
			var length = bi.length;

			Bone.startBone(fbb);

			var rotGOffset = createQuat(fbb, rotG);
			Bone.addRotationG(fbb, rotGOffset);
			var headPosGOffset = Vec3f.createVec3f(fbb, headPosG.x, headPosG.y, headPosG.z);
			Bone.addHeadPositionG(fbb, headPosGOffset);
			Bone.addBodyPart(fbb, bi.boneType.getBodyPart());
			Bone.addBoneLength(fbb, length);

			boneOffsets[i] = Bone.endBone(fbb);
		}

		return DataFeedUpdate.createBonesVector(fbb, boneOffsets);
	}
}
