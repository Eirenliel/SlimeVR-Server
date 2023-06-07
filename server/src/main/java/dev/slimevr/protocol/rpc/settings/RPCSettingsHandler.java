package dev.slimevr.protocol.rpc.settings;

import com.google.flatbuffers.FlatBufferBuilder;
import dev.slimevr.config.*;
import dev.slimevr.filtering.TrackerFilters;
import dev.slimevr.osc.OSCRouter;
import dev.slimevr.osc.VMCHandler;
import dev.slimevr.osc.VRCOSCHandler;
import dev.slimevr.platform.SteamVRBridge;
import dev.slimevr.protocol.GenericConnection;
import dev.slimevr.protocol.ProtocolAPI;
import dev.slimevr.protocol.rpc.RPCHandler;
import dev.slimevr.tracking.processor.config.SkeletonConfigToggles;
import dev.slimevr.tracking.processor.config.SkeletonConfigValues;
import dev.slimevr.tracking.trackers.TrackerRole;
import solarxr_protocol.rpc.ChangeSettingsRequest;
import solarxr_protocol.rpc.RpcMessage;
import solarxr_protocol.rpc.RpcMessageHeader;
import solarxr_protocol.rpc.SettingsResponse;


public record RPCSettingsHandler(RPCHandler rpcHandler, ProtocolAPI api) {


	public RPCSettingsHandler(RPCHandler rpcHandler, ProtocolAPI api) {
		this.rpcHandler = rpcHandler;
		this.api = api;

		rpcHandler.registerPacketListener(RpcMessage.SettingsRequest, this::onSettingsRequest);
		rpcHandler
			.registerPacketListener(
				RpcMessage.ChangeSettingsRequest,
				this::onChangeSettingsRequest
			);
	}

	public void onSettingsRequest(GenericConnection conn, RpcMessageHeader messageHeader) {
		FlatBufferBuilder fbb = new FlatBufferBuilder(32);

		SteamVRBridge bridge = this.api.server.getVRBridge(SteamVRBridge.class);

		int settings = SettingsResponse
			.createSettingsResponse(
				fbb,
				RPCSettingsBuilder.createSteamVRSettings(fbb, bridge),
				RPCSettingsBuilder
					.createFilterSettings(
						fbb,
						this.api.server.getConfigManager().getVrConfig().filters
					),
				RPCSettingsBuilder
					.createDriftCompensationSettings(
						fbb,
						this.api.server.getConfigManager().getVrConfig().driftCompensation
					),
				RPCSettingsBuilder
					.createOSCRouterSettings(
						fbb,
						this.api.server.getConfigManager().getVrConfig().oscRouter
					),
				RPCSettingsBuilder
					.createVRCOSCSettings(
						fbb,
						this.api.server.getConfigManager().getVrConfig().vrcOSC
					),
				RPCSettingsBuilder
					.createVMCOSCSettings(
						fbb,
						this.api.server.getConfigManager().getVrConfig().getVmc()
					),
				RPCSettingsBuilder
					.createModelSettings(
						fbb,
						this.api.server.humanPoseManager,
						this.api.server.getConfigManager().getVrConfig().legTweaks
					),
				RPCSettingsBuilder
					.createTapDetectionSettings(
						fbb,
						this.api.server.getConfigManager().getVrConfig().tapDetection
					)
			);
		int outbound = rpcHandler.createRPCMessage(fbb, RpcMessage.SettingsResponse, settings);
		fbb.finish(outbound);
		conn.send(fbb.dataBuffer());
	}

	public void onChangeSettingsRequest(GenericConnection conn, RpcMessageHeader messageHeader) {
		ChangeSettingsRequest req = (ChangeSettingsRequest) messageHeader
			.message(new ChangeSettingsRequest());
		if (req == null)
			return;

		if (req.steamVrTrackers() != null) {
			SteamVRBridge bridge = this.api.server
				.getVRBridge(SteamVRBridge.class);

			if (bridge != null) {
				bridge.changeShareSettings(TrackerRole.WAIST, req.steamVrTrackers().waist());
				bridge.changeShareSettings(TrackerRole.CHEST, req.steamVrTrackers().chest());
				bridge.changeShareSettings(TrackerRole.LEFT_FOOT, req.steamVrTrackers().feet());
				bridge.changeShareSettings(TrackerRole.RIGHT_FOOT, req.steamVrTrackers().feet());
				bridge.changeShareSettings(TrackerRole.LEFT_KNEE, req.steamVrTrackers().knees());
				bridge.changeShareSettings(TrackerRole.RIGHT_KNEE, req.steamVrTrackers().knees());
				bridge.changeShareSettings(TrackerRole.LEFT_ELBOW, req.steamVrTrackers().elbows());
				bridge.changeShareSettings(TrackerRole.RIGHT_ELBOW, req.steamVrTrackers().elbows());
				bridge.changeShareSettings(TrackerRole.LEFT_HAND, req.steamVrTrackers().hands());
				bridge.changeShareSettings(TrackerRole.RIGHT_HAND, req.steamVrTrackers().hands());
			}
		}

		if (req.filtering() != null) {
			TrackerFilters type = TrackerFilters.fromId(req.filtering().type());
			if (type != null) {
				FiltersConfig filtersConfig = this.api.server
					.getConfigManager()
					.getVrConfig().filters;
				filtersConfig.setType(type.getConfigKey());
				filtersConfig.setAmount(req.filtering().amount());
				filtersConfig.updateTrackersFilters();
			}
		}

		if (req.driftCompensation() != null) {
			DriftCompensationConfig driftCompensationConfig = this.api.server
				.getConfigManager()
				.getVrConfig().driftCompensation;
			driftCompensationConfig.setEnabled(req.driftCompensation().enabled());
			driftCompensationConfig.setAmount(req.driftCompensation().amount());
			driftCompensationConfig.setMaxResets(req.driftCompensation().maxResets());
			driftCompensationConfig.updateTrackersDriftCompensation();
		}

		if (req.oscRouter() != null) {
			OSCConfig oscRouterConfig = this.api.server
				.getConfigManager()
				.getVrConfig().oscRouter;
			if (oscRouterConfig != null) {
				OSCRouter oscRouter = this.api.server.getOSCRouter();
				var osc = req.oscRouter().oscSettings();
				if (osc != null) {
					oscRouterConfig.setEnabled(osc.enabled());
					oscRouterConfig.setPortIn(osc.portIn());
					oscRouterConfig.setPortOut(osc.portOut());
					oscRouterConfig.setAddress(osc.address());
				}

				oscRouter.refreshSettings(true);
			}
		}

		if (req.vrcOsc() != null) {
			VRCOSCConfig vrcOSCConfig = this.api.server
				.getConfigManager()
				.getVrConfig().vrcOSC;
			if (vrcOSCConfig != null) {
				VRCOSCHandler VRCOSCHandler = this.api.server.getVrcOSCHandler();
				var osc = req.vrcOsc().oscSettings();
				var trackers = req.vrcOsc().trackers();

				if (osc != null) {
					vrcOSCConfig.setEnabled(osc.enabled());
					vrcOSCConfig.setPortIn(osc.portIn());
					vrcOSCConfig.setPortOut(osc.portOut());
					vrcOSCConfig.setAddress(osc.address());
				}
				if (trackers != null) {
					vrcOSCConfig.setOSCTrackerRole(TrackerRole.HEAD, trackers.head());
					vrcOSCConfig.setOSCTrackerRole(TrackerRole.CHEST, trackers.chest());
					vrcOSCConfig.setOSCTrackerRole(TrackerRole.WAIST, trackers.waist());
					vrcOSCConfig.setOSCTrackerRole(TrackerRole.LEFT_KNEE, trackers.knees());
					vrcOSCConfig.setOSCTrackerRole(TrackerRole.RIGHT_KNEE, trackers.knees());
					vrcOSCConfig.setOSCTrackerRole(TrackerRole.LEFT_FOOT, trackers.feet());
					vrcOSCConfig.setOSCTrackerRole(TrackerRole.RIGHT_FOOT, trackers.feet());
					vrcOSCConfig.setOSCTrackerRole(TrackerRole.LEFT_ELBOW, trackers.elbows());
					vrcOSCConfig.setOSCTrackerRole(TrackerRole.RIGHT_ELBOW, trackers.elbows());
					vrcOSCConfig.setOSCTrackerRole(TrackerRole.LEFT_HAND, trackers.hands());
					vrcOSCConfig.setOSCTrackerRole(TrackerRole.RIGHT_HAND, trackers.hands());
				}

				VRCOSCHandler.refreshSettings(true);
			}
		}

		if (req.vmcOsc() != null) {
			VMCConfig vmcConfig = this.api.server
				.getConfigManager()
				.getVrConfig()
				.getVmc();
			if (vmcConfig != null) {
				VMCHandler VMCHandler = this.api.server.getVMCHandler();
				var osc = req.vmcOsc().oscSettings();

				if (osc != null) {
					vmcConfig.setEnabled(osc.enabled());
					vmcConfig.setPortIn(osc.portIn());
					vmcConfig.setPortOut(osc.portOut());
					vmcConfig.setAddress(osc.address());
				}
				if (req.vmcOsc().vrmJson() != null)
					vmcConfig.setVrmJson(req.vmcOsc().vrmJson());
				vmcConfig.setAnchorHip(req.vmcOsc().anchorHip());

				VMCHandler.refreshSettings(true);
			}
		}

		if (req.tapDetectionSettings() != null) {
			TapDetectionConfig tapDetectionConfig = this.api.server
				.getConfigManager()
				.getVrConfig().tapDetection;
			var tapDetectionSettings = req.tapDetectionSettings();

			if (tapDetectionSettings != null) {
				// enable/disable tap detection
				tapDetectionConfig.setYawResetEnabled(tapDetectionSettings.yawResetEnabled());
				tapDetectionConfig.setFullResetEnabled(tapDetectionSettings.fullResetEnabled());
				tapDetectionConfig
					.setMountingResetEnabled(tapDetectionSettings.mountingResetEnabled());
				tapDetectionConfig.setSetupMode(tapDetectionSettings.setupMode());

				// set tap detection delays
				if (tapDetectionSettings.hasYawResetDelay()) {
					tapDetectionConfig.setYawResetDelay(tapDetectionSettings.yawResetDelay());
				}
				if (tapDetectionSettings.hasFullResetDelay()) {
					tapDetectionConfig.setFullResetDelay(tapDetectionSettings.fullResetDelay());
				}
				if (tapDetectionSettings.hasMountingResetDelay()) {
					tapDetectionConfig
						.setMountingResetDelay(tapDetectionSettings.mountingResetDelay());
				}

				// set the number of taps required for each action
				if (tapDetectionSettings.hasYawResetTaps()) {
					tapDetectionConfig
						.setYawResetTaps(tapDetectionSettings.yawResetTaps());
				}
				if (tapDetectionSettings.hasFullResetTaps()) {
					tapDetectionConfig
						.setFullResetTaps(tapDetectionSettings.fullResetTaps());
				}
				if (tapDetectionSettings.hasMountingResetTaps()) {
					tapDetectionConfig
						.setMountingResetTaps(tapDetectionSettings.mountingResetTaps());
				}

				this.api.server.humanPoseManager.updateTapDetectionConfig();
			}
		}

		var modelSettings = req.modelSettings();
		if (modelSettings != null) {
			var hpm = this.api.server.humanPoseManager;
			var legTweaksConfig = this.api.server.getConfigManager().getVrConfig().legTweaks;
			var toggles = modelSettings.toggles();
			var ratios = modelSettings.ratios();
			var legTweaks = modelSettings.legTweaks();

			if (toggles != null) {
				// Note: toggles.has____ returns the same as toggles._____ this
				// seems like a bug
				hpm.setToggle(SkeletonConfigToggles.EXTENDED_SPINE_MODEL, toggles.extendedSpine());
				hpm
					.setToggle(
						SkeletonConfigToggles.EXTENDED_PELVIS_MODEL,
						toggles.extendedPelvis()
					);
				hpm.setToggle(SkeletonConfigToggles.EXTENDED_KNEE_MODEL, toggles.extendedKnee());
				hpm
					.setToggle(
						SkeletonConfigToggles.FORCE_ARMS_FROM_HMD,
						toggles.forceArmsFromHmd()
					);
				hpm.setToggle(SkeletonConfigToggles.EXTENDED_SPINE_MODEL, toggles.extendedSpine());
				hpm
					.setToggle(
						SkeletonConfigToggles.EXTENDED_PELVIS_MODEL,
						toggles.extendedPelvis()
					);
				hpm.setToggle(SkeletonConfigToggles.EXTENDED_KNEE_MODEL, toggles.extendedKnee());
				hpm
					.setToggle(
						SkeletonConfigToggles.FORCE_ARMS_FROM_HMD,
						toggles.forceArmsFromHmd()
					);
				hpm.setToggle(SkeletonConfigToggles.FLOOR_CLIP, toggles.floorClip());
				hpm
					.setToggle(
						SkeletonConfigToggles.SKATING_CORRECTION,
						toggles.skatingCorrection()
					);
				hpm.setToggle(SkeletonConfigToggles.VIVE_EMULATION, toggles.viveEmulation());
				hpm.setToggle(SkeletonConfigToggles.TOE_SNAP, toggles.toeSnap());
				hpm.setToggle(SkeletonConfigToggles.FOOT_PLANT, toggles.footPlant());
			}

			if (ratios != null) {
				if (ratios.hasImputeWaistFromChestHip()) {
					hpm
						.setValue(
							SkeletonConfigValues.WAIST_FROM_CHEST_HIP_AVERAGING,
							ratios.imputeWaistFromChestHip()
						);
				}
				if (ratios.hasImputeWaistFromChestLegs()) {
					hpm
						.setValue(
							SkeletonConfigValues.WAIST_FROM_CHEST_LEGS_AVERAGING,
							ratios.imputeWaistFromChestLegs()
						);
				}
				if (ratios.hasImputeHipFromChestLegs()) {
					hpm
						.setValue(
							SkeletonConfigValues.HIP_FROM_CHEST_LEGS_AVERAGING,
							ratios.imputeHipFromChestLegs()
						);
				}
				if (ratios.hasImputeHipFromWaistLegs()) {
					hpm
						.setValue(
							SkeletonConfigValues.HIP_FROM_WAIST_LEGS_AVERAGING,
							ratios.imputeHipFromWaistLegs()
						);
				}
				if (ratios.hasInterpHipLegs()) {
					hpm.setValue(SkeletonConfigValues.HIP_LEGS_AVERAGING, ratios.interpHipLegs());
				}
				if (ratios.hasInterpKneeTrackerAnkle()) {
					hpm
						.setValue(
							SkeletonConfigValues.KNEE_TRACKER_ANKLE_AVERAGING,
							ratios.interpKneeTrackerAnkle()
						);
				}
			}

			if (legTweaks != null) {
				if (legTweaks.hasCorrectionStrength()) {
					legTweaksConfig.correctionStrength = legTweaks.correctionStrength();
				}
				this.api.server.humanPoseManager.updateLegTweaksConfig();
			}

			hpm.saveConfig();

		}

		this.api.server.getConfigManager().saveConfig();
	}

}
