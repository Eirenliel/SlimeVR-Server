package dev.slimevr.protocol.rpc.settings;

import com.google.flatbuffers.FlatBufferBuilder;
import dev.slimevr.config.*;
import dev.slimevr.filtering.TrackerFilters;
import dev.slimevr.platform.SteamVRBridge;
import dev.slimevr.tracking.processor.HumanPoseManager;
import dev.slimevr.tracking.processor.config.SkeletonConfigToggles;
import dev.slimevr.tracking.processor.config.SkeletonConfigValues;
import dev.slimevr.tracking.trackers.TrackerRole;
import solarxr_protocol.rpc.*;
import solarxr_protocol.rpc.settings.LegTweaksSettings;
import solarxr_protocol.rpc.settings.ModelRatios;
import solarxr_protocol.rpc.settings.ModelSettings;
import solarxr_protocol.rpc.settings.ModelToggles;


public class RPCSettingsBuilder {

	public static int createOSCRouterSettings(
		FlatBufferBuilder fbb,
		OSCConfig config
	) {
		int addressStringOffset = fbb.createString(config.getAddress());

		int oscSettingOffset = OSCSettings
			.createOSCSettings(
				fbb,
				config.getEnabled(),
				config.getPortIn(),
				config.getPortOut(),
				addressStringOffset
			);

		OSCRouterSettings.startOSCRouterSettings(fbb);
		OSCRouterSettings.addOscSettings(fbb, oscSettingOffset);

		return OSCRouterSettings.endOSCRouterSettings(fbb);
	}

	public static int createVRCOSCSettings(
		FlatBufferBuilder fbb,
		VRCOSCConfig config
	) {
		int addressStringOffset = fbb.createString(config.getAddress());
		int generalSettingOffset = OSCSettings
			.createOSCSettings(
				fbb,
				config.getEnabled(),
				config.getPortIn(),
				config.getPortOut(),
				addressStringOffset
			);
		int oscSettingOffset = OSCTrackersSetting
			.createOSCTrackersSetting(
				fbb,
				config.getOSCTrackerRole(TrackerRole.HEAD, false),
				config.getOSCTrackerRole(TrackerRole.CHEST, false),
				config.getOSCTrackerRole(TrackerRole.WAIST, false),
				config.getOSCTrackerRole(TrackerRole.LEFT_KNEE, false)
					&& config.getOSCTrackerRole(TrackerRole.RIGHT_KNEE, false),
				config.getOSCTrackerRole(TrackerRole.LEFT_FOOT, false)
					&& config.getOSCTrackerRole(TrackerRole.RIGHT_FOOT, false),
				config.getOSCTrackerRole(TrackerRole.LEFT_ELBOW, false)
					&& config.getOSCTrackerRole(TrackerRole.RIGHT_ELBOW, false),
				config.getOSCTrackerRole(TrackerRole.LEFT_HAND, false)
					&& config.getOSCTrackerRole(TrackerRole.RIGHT_HAND, false)
			);
		VRCOSCSettings.startVRCOSCSettings(fbb);
		VRCOSCSettings.addOscSettings(fbb, generalSettingOffset);
		VRCOSCSettings.addTrackers(fbb, oscSettingOffset);

		return VRCOSCSettings.endVRCOSCSettings(fbb);
	}

	public static int createVMCOSCSettings(
		FlatBufferBuilder fbb,
		VMCConfig config
	) {
		int addressStringOffset = fbb.createString(config.getAddress());
		int generalSettingOffset = OSCSettings
			.createOSCSettings(
				fbb,
				config.getEnabled(),
				config.getPortIn(),
				config.getPortOut(),
				addressStringOffset
			);

		String vrmJson = config.getVrmJson();
		int vrmJsonOffset = 0;
		if (vrmJson != null)
			vrmJsonOffset = fbb.createString(vrmJson);

		VMCOSCSettings.startVMCOSCSettings(fbb);
		VMCOSCSettings.addOscSettings(fbb, generalSettingOffset);
		if (vrmJson != null)
			VMCOSCSettings.addVrmJson(fbb, vrmJsonOffset);
		VMCOSCSettings.addAnchorHip(fbb, config.getAnchorHip());

		return VMCOSCSettings.endVMCOSCSettings(fbb);
	}

	public static int createFilterSettings(
		FlatBufferBuilder fbb,
		FiltersConfig filtersConfig
	) {
		return FilteringSettings
			.createFilteringSettings(
				fbb,
				TrackerFilters.getByConfigkey(filtersConfig.getType()).getId(),
				filtersConfig.getAmount()
			);
	}

	public static int createDriftCompensationSettings(
		FlatBufferBuilder fbb,
		DriftCompensationConfig driftCompensationConfig
	) {
		return DriftCompensationSettings
			.createDriftCompensationSettings(
				fbb,
				driftCompensationConfig.getEnabled(),
				driftCompensationConfig.getAmount(),
				driftCompensationConfig.getMaxResets()
			);
	}

	public static int createTapDetectionSettings(
		FlatBufferBuilder fbb,
		TapDetectionConfig tapDetectionConfig
	) {
		return TapDetectionSettings
			.createTapDetectionSettings(
				fbb,
				tapDetectionConfig.getFullResetDelay(),
				tapDetectionConfig.getFullResetEnabled(),
				tapDetectionConfig.getFullResetTaps(),
				tapDetectionConfig.getYawResetDelay(),
				tapDetectionConfig.getYawResetEnabled(),
				tapDetectionConfig.getYawResetTaps(),
				tapDetectionConfig.getMountingResetDelay(),
				tapDetectionConfig.getMountingResetEnabled(),
				tapDetectionConfig.getMountingResetTaps(),
				tapDetectionConfig.getSetupMode()
			);
	}

	public static int createSteamVRSettings(FlatBufferBuilder fbb, SteamVRBridge bridge) {
		int steamvrTrackerSettings = 0;
		if (bridge != null) {
			steamvrTrackerSettings = SteamVRTrackersSetting
				.createSteamVRTrackersSetting(
					fbb,
					bridge.getShareSetting(TrackerRole.WAIST),
					bridge.getShareSetting(TrackerRole.CHEST),
					bridge.getShareSetting(TrackerRole.LEFT_FOOT)
						&& bridge.getShareSetting(TrackerRole.RIGHT_FOOT),
					bridge.getShareSetting(TrackerRole.LEFT_KNEE)
						&& bridge.getShareSetting(TrackerRole.RIGHT_KNEE),
					bridge.getShareSetting(TrackerRole.LEFT_ELBOW)
						&& bridge.getShareSetting(TrackerRole.RIGHT_ELBOW),
					bridge.getShareSetting(TrackerRole.LEFT_HAND)
						&& bridge.getShareSetting(TrackerRole.RIGHT_HAND)
				);
		}
		return steamvrTrackerSettings;
	}

	public static int createModelSettings(
		FlatBufferBuilder fbb,
		HumanPoseManager humanPoseManager,
		LegTweaksConfig legTweaksConfig
	) {
		int togglesOffset = ModelToggles
			.createModelToggles(
				fbb,
				humanPoseManager.getToggle(SkeletonConfigToggles.EXTENDED_SPINE_MODEL),
				humanPoseManager.getToggle(SkeletonConfigToggles.EXTENDED_PELVIS_MODEL),
				humanPoseManager.getToggle(SkeletonConfigToggles.EXTENDED_KNEE_MODEL),
				humanPoseManager.getToggle(SkeletonConfigToggles.FORCE_ARMS_FROM_HMD),
				humanPoseManager.getToggle(SkeletonConfigToggles.FLOOR_CLIP),
				humanPoseManager.getToggle(SkeletonConfigToggles.SKATING_CORRECTION),
				humanPoseManager.getToggle(SkeletonConfigToggles.VIVE_EMULATION),
				humanPoseManager.getToggle(SkeletonConfigToggles.TOE_SNAP),
				humanPoseManager.getToggle(SkeletonConfigToggles.FOOT_PLANT)
			);
		int ratiosOffset = ModelRatios
			.createModelRatios(
				fbb,
				humanPoseManager.getValue(SkeletonConfigValues.WAIST_FROM_CHEST_HIP_AVERAGING),
				humanPoseManager.getValue(SkeletonConfigValues.WAIST_FROM_CHEST_LEGS_AVERAGING),
				humanPoseManager.getValue(SkeletonConfigValues.HIP_FROM_CHEST_LEGS_AVERAGING),
				humanPoseManager.getValue(SkeletonConfigValues.HIP_FROM_WAIST_LEGS_AVERAGING),
				humanPoseManager.getValue(SkeletonConfigValues.HIP_LEGS_AVERAGING),
				humanPoseManager.getValue(SkeletonConfigValues.KNEE_TRACKER_ANKLE_AVERAGING)
			);
		int legTweaksOffset = LegTweaksSettings
			.createLegTweaksSettings(
				fbb,
				legTweaksConfig.correctionStrength
			);
		return ModelSettings.createModelSettings(fbb, togglesOffset, ratiosOffset, legTweaksOffset);
	}
}
