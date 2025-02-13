package dev.slimevr.protocol.rpc.settings

import com.google.flatbuffers.FlatBufferBuilder
import dev.slimevr.bridge.ISteamVRBridge
import dev.slimevr.config.ArmsResetModes
import dev.slimevr.filtering.TrackerFilters
import dev.slimevr.math.Angle
import dev.slimevr.protocol.GenericConnection
import dev.slimevr.protocol.ProtocolAPI
import dev.slimevr.protocol.rpc.RPCHandler
import dev.slimevr.tracking.processor.config.SkeletonConfigToggles
import dev.slimevr.tracking.processor.config.SkeletonConfigValues
import dev.slimevr.tracking.trackers.TrackerRole
import solarxr_protocol.rpc.ChangeSettingsRequest
import solarxr_protocol.rpc.RpcMessage
import solarxr_protocol.rpc.RpcMessageHeader
import solarxr_protocol.rpc.SettingsResponse
import kotlin.math.*

class RPCSettingsHandler(var rpcHandler: RPCHandler, var api: ProtocolAPI) {
	init {
		rpcHandler.registerPacketListener(RpcMessage.SettingsRequest) { conn: GenericConnection, messageHeader: RpcMessageHeader? ->
			this.onSettingsRequest(
				conn,
				messageHeader,
			)
		}
		rpcHandler
			.registerPacketListener(
				RpcMessage.ChangeSettingsRequest,
			) { conn: GenericConnection?, messageHeader: RpcMessageHeader ->
				this.onChangeSettingsRequest(
					conn,
					messageHeader,
				)
			}
		rpcHandler.registerPacketListener(RpcMessage.SettingsResetRequest) { conn: GenericConnection, messageHeader: RpcMessageHeader? ->
			this.onSettingsResetRequest(conn, messageHeader)
		}
	}

	fun onSettingsRequest(conn: GenericConnection, messageHeader: RpcMessageHeader?) {
		val fbb = FlatBufferBuilder(32)

		val settings = RPCSettingsBuilder.createSettingsResponse(fbb, api.server)
		val outbound = rpcHandler.createRPCMessage(fbb, RpcMessage.SettingsResponse, settings)
		fbb.finish(outbound)
		conn.send(fbb.dataBuffer())
	}

	fun onChangeSettingsRequest(conn: GenericConnection?, messageHeader: RpcMessageHeader) {
		val req = messageHeader
			.message(ChangeSettingsRequest()) as? ChangeSettingsRequest ?: return

		if (req.steamVrTrackers() != null) {
			val bridge = api.server
				.getVRBridge(ISteamVRBridge::class.java)

			if (bridge != null) {
				bridge.changeShareSettings(TrackerRole.WAIST, req.steamVrTrackers().waist())
				bridge.changeShareSettings(TrackerRole.CHEST, req.steamVrTrackers().chest())
				bridge.changeShareSettings(TrackerRole.LEFT_FOOT, req.steamVrTrackers().leftFoot())
				bridge.changeShareSettings(TrackerRole.RIGHT_FOOT, req.steamVrTrackers().rightFoot())
				bridge.changeShareSettings(TrackerRole.LEFT_KNEE, req.steamVrTrackers().leftKnee())
				bridge.changeShareSettings(TrackerRole.RIGHT_KNEE, req.steamVrTrackers().rightKnee())
				bridge.changeShareSettings(TrackerRole.LEFT_ELBOW, req.steamVrTrackers().leftElbow())
				bridge.changeShareSettings(TrackerRole.RIGHT_ELBOW, req.steamVrTrackers().rightElbow())
				bridge.changeShareSettings(TrackerRole.LEFT_HAND, req.steamVrTrackers().leftHand())
				bridge.changeShareSettings(TrackerRole.RIGHT_HAND, req.steamVrTrackers().rightHand())
				bridge.setAutomaticSharedTrackers(req.steamVrTrackers().automaticTrackerToggle())
			}
		}

		if (req.filtering() != null) {
			val type = TrackerFilters.fromId(req.filtering().type())
			if (type != null) {
				val filtersConfig = api.server.configManager
					.vrConfig
					.filters
				filtersConfig.type = type.configKey
				filtersConfig.amount = req.filtering().amount()
				filtersConfig.updateTrackersFilters()
			}
		}

		if (req.driftCompensation() != null) {
			val driftCompensationConfig = api.server.configManager
				.vrConfig
				.driftCompensation
			driftCompensationConfig.enabled = req.driftCompensation().enabled()
			driftCompensationConfig.prediction = req.driftCompensation().prediction()
			driftCompensationConfig.amount = req.driftCompensation().amount()
			driftCompensationConfig.maxResets = req.driftCompensation().maxResets()
			driftCompensationConfig.updateTrackersDriftCompensation()
		}

		if (req.oscRouter() != null) {
			val oscRouterConfig = api.server.configManager
				.vrConfig
				.oscRouter
			val oscRouter = api.server.oSCRouter
			val osc = req.oscRouter().oscSettings()
			if (osc != null) {
				oscRouterConfig.enabled = osc.enabled()
				oscRouterConfig.portIn = osc.portIn()
				oscRouterConfig.portOut = osc.portOut()
				oscRouterConfig.address = osc.address()
			}

			oscRouter.refreshSettings(true)
		}

		if (req.vrcOsc() != null) {
			val vrcOSCConfig = api.server.configManager
				.vrConfig
				.vrcOSC
			val vrcOscHandler = api.server.vrcOSCHandler
			val osc = req.vrcOsc().oscSettings()
			val trackers = req.vrcOsc().trackers()

			if (osc != null) {
				vrcOSCConfig.enabled = osc.enabled()
				vrcOSCConfig.portIn = osc.portIn()
				vrcOSCConfig.portOut = osc.portOut()
				vrcOSCConfig.address = osc.address()
			}
			if (trackers != null) {
				vrcOSCConfig.setOSCTrackerRole(TrackerRole.HEAD, trackers.head())
				vrcOSCConfig.setOSCTrackerRole(TrackerRole.CHEST, trackers.chest())
				vrcOSCConfig.setOSCTrackerRole(TrackerRole.WAIST, trackers.waist())
				vrcOSCConfig.setOSCTrackerRole(TrackerRole.LEFT_KNEE, trackers.knees())
				vrcOSCConfig.setOSCTrackerRole(TrackerRole.RIGHT_KNEE, trackers.knees())
				vrcOSCConfig.setOSCTrackerRole(TrackerRole.LEFT_FOOT, trackers.feet())
				vrcOSCConfig.setOSCTrackerRole(TrackerRole.RIGHT_FOOT, trackers.feet())
				vrcOSCConfig.setOSCTrackerRole(TrackerRole.LEFT_ELBOW, trackers.elbows())
				vrcOSCConfig.setOSCTrackerRole(TrackerRole.RIGHT_ELBOW, trackers.elbows())
				vrcOSCConfig.setOSCTrackerRole(TrackerRole.LEFT_HAND, trackers.hands())
				vrcOSCConfig.setOSCTrackerRole(TrackerRole.RIGHT_HAND, trackers.hands())
			}

			vrcOscHandler.refreshSettings(true)
		}

		if (req.vmcOsc() != null) {
			val vmcConfig = api.server.configManager
				.vrConfig
				.vmc
			val vmcHandler = api.server.vMCHandler
			val osc = req.vmcOsc().oscSettings()

			if (osc != null) {
				vmcConfig.enabled = osc.enabled()
				vmcConfig.portIn = osc.portIn()
				vmcConfig.portOut = osc.portOut()
				vmcConfig.address = osc.address()
			}
			if (req.vmcOsc().vrmJson() != null) {
				vmcConfig.vrmJson = req.vmcOsc().vrmJson().ifEmpty { null }
			}
			vmcConfig.anchorHip = req.vmcOsc().anchorHip()
			vmcConfig.mirrorTracking = req.vmcOsc().mirrorTracking()

			vmcHandler.refreshSettings(true)
		}

		if (req.tapDetectionSettings() != null) {
			val tapDetectionConfig = api.server.configManager
				.vrConfig
				.tapDetection
			val tapDetectionSettings = req.tapDetectionSettings()

			if (tapDetectionSettings != null) {
				// enable/disable tap detection
				tapDetectionConfig.yawResetEnabled = tapDetectionSettings.yawResetEnabled()
				tapDetectionConfig.fullResetEnabled = tapDetectionSettings.fullResetEnabled()
				tapDetectionConfig
					.mountingResetEnabled = tapDetectionSettings.mountingResetEnabled()
				tapDetectionConfig.setupMode = tapDetectionSettings.setupMode()

				// set number of trackers that can have high accel before taps
				// are rejected
				if (tapDetectionSettings.hasNumberTrackersOverThreshold()) {
					tapDetectionConfig
						.numberTrackersOverThreshold = tapDetectionSettings.numberTrackersOverThreshold()
				}

				// set tap detection delays
				if (tapDetectionSettings.hasYawResetDelay()) {
					tapDetectionConfig.yawResetDelay = tapDetectionSettings.yawResetDelay()
				}
				if (tapDetectionSettings.hasFullResetDelay()) {
					tapDetectionConfig.fullResetDelay = tapDetectionSettings.fullResetDelay()
				}
				if (tapDetectionSettings.hasMountingResetDelay()) {
					tapDetectionConfig
						.mountingResetDelay = tapDetectionSettings.mountingResetDelay()
				}

				// set the number of taps required for each action
				if (tapDetectionSettings.hasYawResetTaps()) {
					tapDetectionConfig
						.yawResetTaps = tapDetectionSettings.yawResetTaps()
				}
				if (tapDetectionSettings.hasFullResetTaps()) {
					tapDetectionConfig
						.fullResetTaps = tapDetectionSettings.fullResetTaps()
				}
				if (tapDetectionSettings.hasMountingResetTaps()) {
					tapDetectionConfig
						.mountingResetTaps = tapDetectionSettings.mountingResetTaps()
				}

				api.server.humanPoseManager.updateTapDetectionConfig()
			}
		}

		val modelSettings = req.modelSettings()
		if (modelSettings != null) {
			val hpm = api.server.humanPoseManager
			val legTweaksConfig = api.server.configManager.vrConfig.legTweaks
			val toggles = modelSettings.toggles()
			val ratios = modelSettings.ratios()
			val legTweaks = modelSettings.legTweaks()

			if (toggles != null) {
				// Note: toggles.has____ returns the same as toggles._____ this
				// seems like a bug
				hpm.setToggle(SkeletonConfigToggles.EXTENDED_SPINE_MODEL, toggles.extendedSpine())
				hpm
					.setToggle(
						SkeletonConfigToggles.EXTENDED_PELVIS_MODEL,
						toggles.extendedPelvis(),
					)
				hpm.setToggle(SkeletonConfigToggles.EXTENDED_KNEE_MODEL, toggles.extendedKnee())
				hpm
					.setToggle(
						SkeletonConfigToggles.FORCE_ARMS_FROM_HMD,
						toggles.forceArmsFromHmd(),
					)
				hpm.setToggle(SkeletonConfigToggles.EXTENDED_SPINE_MODEL, toggles.extendedSpine())
				hpm
					.setToggle(
						SkeletonConfigToggles.EXTENDED_PELVIS_MODEL,
						toggles.extendedPelvis(),
					)
				hpm.setToggle(SkeletonConfigToggles.EXTENDED_KNEE_MODEL, toggles.extendedKnee())
				hpm
					.setToggle(
						SkeletonConfigToggles.FORCE_ARMS_FROM_HMD,
						toggles.forceArmsFromHmd(),
					)
				hpm.setToggle(SkeletonConfigToggles.FLOOR_CLIP, toggles.floorClip())
				hpm
					.setToggle(
						SkeletonConfigToggles.SKATING_CORRECTION,
						toggles.skatingCorrection(),
					)
				hpm.setToggle(SkeletonConfigToggles.VIVE_EMULATION, toggles.viveEmulation())
				hpm.setToggle(SkeletonConfigToggles.TOE_SNAP, toggles.toeSnap())
				hpm.setToggle(SkeletonConfigToggles.FOOT_PLANT, toggles.footPlant())
				hpm.setToggle(SkeletonConfigToggles.SELF_LOCALIZATION, toggles.selfLocalization())
				hpm.setToggle(SkeletonConfigToggles.ENFORCE_CONSTRAINTS, toggles.enforceConstraints())
				hpm.setToggle(SkeletonConfigToggles.CORRECT_CONSTRAINTS, toggles.correctConstraints())
			}

			if (ratios != null) {
				if (ratios.hasImputeWaistFromChestHip()) {
					hpm
						.setValue(
							SkeletonConfigValues.WAIST_FROM_CHEST_HIP_AVERAGING,
							max(0f, ratios.imputeWaistFromChestHip()),
						)
				}
				if (ratios.hasImputeWaistFromChestLegs()) {
					hpm
						.setValue(
							SkeletonConfigValues.WAIST_FROM_CHEST_LEGS_AVERAGING,
							max(0f, ratios.imputeWaistFromChestLegs()),
						)
				}
				if (ratios.hasImputeHipFromChestLegs()) {
					hpm
						.setValue(
							SkeletonConfigValues.HIP_FROM_CHEST_LEGS_AVERAGING,
							max(0f, ratios.imputeHipFromChestLegs()),
						)
				}
				if (ratios.hasImputeHipFromWaistLegs()) {
					hpm
						.setValue(
							SkeletonConfigValues.HIP_FROM_WAIST_LEGS_AVERAGING,
							max(0f, ratios.imputeHipFromWaistLegs()),
						)
				}
				if (ratios.hasInterpHipLegs()) {
					hpm
						.setValue(
							SkeletonConfigValues.HIP_LEGS_AVERAGING,
							max(0f, ratios.interpHipLegs()),
						)
				}
				if (ratios.hasInterpKneeTrackerAnkle()) {
					hpm
						.setValue(
							SkeletonConfigValues.KNEE_TRACKER_ANKLE_AVERAGING,
							max(0f, ratios.interpKneeTrackerAnkle()),
						)
				}
				if (ratios.hasInterpKneeAnkle()) {
					hpm
						.setValue(
							SkeletonConfigValues.KNEE_ANKLE_AVERAGING,
							max(0f, ratios.interpKneeAnkle()),
						)
				}
			}

			if (legTweaks != null) {
				if (legTweaks.hasCorrectionStrength()) {
					legTweaksConfig.correctionStrength = legTweaks.correctionStrength()
				}
				api.server.humanPoseManager.updateLegTweaksConfig()
			}

			modelSettings.skeletonHeight()?.let {
				api.server.configManager.vrConfig.skeleton.hmdHeight = it.hmdHeight()
				api.server.configManager.vrConfig.skeleton.floorHeight = it.floorHeight()
			}

			hpm.saveConfig()
		}

		val autoBoneSettings = req.autoBoneSettings()
		if (autoBoneSettings != null) {
			val autoBoneConfig = api.server.configManager
				.vrConfig
				.autoBone

			RPCSettingsBuilder.readAutoBoneSettings(autoBoneSettings, autoBoneConfig)
		}

		if (req.resetsSettings() != null) {
			val resetsConfig = api.server.configManager
				.vrConfig
				.resetsConfig
			val mode = ArmsResetModes
				.fromId(max(req.resetsSettings().armsMountingResetMode(), 0))
			if (mode != null) {
				resetsConfig.mode = mode
			}
			resetsConfig.resetMountingFeet = req.resetsSettings().resetMountingFeet()
			resetsConfig.saveMountingReset = req.resetsSettings().saveMountingReset()
			resetsConfig.yawResetSmoothTime = req.resetsSettings().yawResetSmoothTime()
			resetsConfig.resetHmdPitch = req.resetsSettings().resetHmdPitch()
			resetsConfig.updateTrackersResetsSettings()
		}

		if (req.yawCorrectionSettings() != null) {
			val config = api.server.configManager.vrConfig.stayAlignedConfig
			config.enabled = req.yawCorrectionSettings().enabled()
			config.yawCorrectionPerSec = Angle.ofDeg(req.yawCorrectionSettings().amountInDegPerSec())
			config.standingUpperLegAngle = Angle.ofDeg(req.yawCorrectionSettings().standingUpperLegAngle())
			config.standingLowerLegAngle = Angle.ofDeg(req.yawCorrectionSettings().standingLowerLegAngle())
			config.standingFootAngle = Angle.ofDeg(req.yawCorrectionSettings().standingFootAngle())
			config.sittingUpperLegAngle = Angle.ofDeg(req.yawCorrectionSettings().sittingUpperLegAngle())
			config.sittingLowerLegAngle = Angle.ofDeg(req.yawCorrectionSettings().sittingLowerLegAngle())
			config.sittingFootAngle = Angle.ofDeg(req.yawCorrectionSettings().sittingFootAngle())
			config.lyingOnBackUpperLegAngle = Angle.ofDeg(req.yawCorrectionSettings().lyingOnBackUpperLegAngle())
			config.lyingOnBackLowerLegAngle = Angle.ofDeg(req.yawCorrectionSettings().lyingOnBackLowerLegAngle())
			api.server.humanPoseManager.setStayAlignedConfig(config)
		}

		api.server.configManager.saveConfig()
	}

	fun onSettingsResetRequest(conn: GenericConnection, messageHeader: RpcMessageHeader?) {
		api.server.configManager.resetConfig()
	}

	companion object {
		fun sendSteamVRUpdatedSettings(api: ProtocolAPI, rpcHandler: RPCHandler) {
			val fbb = FlatBufferBuilder(32)
			val bridge: ISteamVRBridge =
				api.server.getVRBridge(ISteamVRBridge::class.java) ?: return

			val settings = SettingsResponse
				.createSettingsResponse(
					fbb,
					RPCSettingsBuilder.createSteamVRSettings(fbb, bridge), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				)
			val outbound =
				rpcHandler.createRPCMessage(fbb, RpcMessage.SettingsResponse, settings)
			fbb.finish(outbound)
			api.apiServers.forEach { apiServer ->
				apiServer.apiConnections.forEach { it.send(fbb.dataBuffer()) }
			}
		}
	}
}
