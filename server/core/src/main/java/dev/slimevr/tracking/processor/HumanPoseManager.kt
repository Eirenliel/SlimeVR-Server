package dev.slimevr.tracking.processor

import com.jme3.math.FastMath
import dev.slimevr.VRServer
import dev.slimevr.VRServer.Companion.getNextLocalTrackerId
import dev.slimevr.config.ConfigManager
import dev.slimevr.tracking.processor.config.SkeletonConfigManager
import dev.slimevr.tracking.processor.config.SkeletonConfigOffsets
import dev.slimevr.tracking.processor.config.SkeletonConfigToggles
import dev.slimevr.tracking.processor.config.SkeletonConfigValues
import dev.slimevr.tracking.processor.skeleton.HumanSkeleton
import dev.slimevr.tracking.trackers.Tracker
import dev.slimevr.tracking.trackers.TrackerPosition
import dev.slimevr.tracking.trackers.TrackerRole
import dev.slimevr.tracking.trackers.TrackerStatus
import dev.slimevr.trackingpause.TrackingPauseHandler
import dev.slimevr.util.ann.VRServerThread
import io.eiren.util.ann.ThreadSafe
import io.eiren.util.collections.FastList
import io.eiren.util.logging.LogManager
import io.github.axisangles.ktmath.Quaternion.Companion.IDENTITY
import io.github.axisangles.ktmath.Vector3
import io.github.axisangles.ktmath.Vector3.Companion.POS_Y
import org.apache.commons.math3.util.Precision
import solarxr_protocol.datatypes.DeviceIdT
import solarxr_protocol.datatypes.TrackerIdT
import solarxr_protocol.rpc.StatusData
import solarxr_protocol.rpc.StatusDataUnion
import solarxr_protocol.rpc.StatusUnassignedHMD
import solarxr_protocol.rpc.StatusUnassignedHMDT
import java.util.function.Consumer
import kotlin.math.*

/**
 * Class to handle communicate between classes in "skeleton" package and outside
 * @param server the used VRServer
 */
class HumanPoseManager(val server: VRServer?) {
	val computedTrackers: MutableList<Tracker> = FastList()
	private val onSkeletonUpdated: MutableList<Consumer<HumanSkeleton>> = FastList()
	private val skeletonConfigManager = SkeletonConfigManager(true, this)

	@get:ThreadSafe
	lateinit var skeleton: HumanSkeleton
	private var timeAtLastReset: Long = 0
	val trackingPauseHandler: TrackingPauseHandler = TrackingPauseHandler()

	// #region Constructors
	init {
		initializeComputedHumanPoseTracker()
	}

	init {
		if (server != null) {
			skeleton = HumanSkeleton(this, server)
			// This computes all node offsets, so the defaults don't need to be
			// explicitly loaded into the skeleton (no need for
			// `updateNodeOffsetsInSkeleton()`)
			loadFromConfig(server.configManager)
			for (sc in onSkeletonUpdated) sc.accept(skeleton)
		}
	}

	/**
	 * Creates a new HumanPoseManager that uses the given trackers for the
	 * HumanSkeleton and the default config
	 *
	 * @param trackers a list of all trackers
	 */
	constructor(trackers: List<Tracker>?) : this(server = null) {
		skeleton = HumanSkeleton(this, trackers)
		// Set default node offsets on the new skeleton
		skeletonConfigManager.updateNodeOffsetsInSkeleton()
		skeletonConfigManager.updateSettingsInSkeleton()
	}

	/**
	 * Creates a new HumanPoseManager that uses the given trackers for the
	 * HumanSkeleton and the given offsets for config
	 *
	 * @param trackers a list of all trackers
	 * @param offsetConfigs a map of the SkeletonConfigOffsets and values for
	 * them
	 */
	constructor(
		trackers: List<Tracker>?,
		offsetConfigs: Map<SkeletonConfigOffsets, Float>?,
	) : this(server = null) {
		skeleton = HumanSkeleton(this, trackers)
		// Set default node offsets on the new skeleton
		skeletonConfigManager.updateNodeOffsetsInSkeleton()
		// Set offsetConfigs from given offsetConfigs on creation
		skeletonConfigManager.setOffsets(offsetConfigs)
		skeletonConfigManager.updateSettingsInSkeleton()
	}

	/**
	 * Creates a new HumanPoseManager that uses the given trackers for the
	 * HumanSkeleton and the given offsets for config
	 *
	 * @param trackers a list of all trackers
	 * @param offsetConfigs a map of the SkeletonConfigOffsets and values for
	 * them
	 * @param altOffsetConfigs an alternative map of the SkeletonConfigOffsets
	 * and values for them
	 */
	constructor(
		trackers: List<Tracker>?,
		offsetConfigs: Map<SkeletonConfigOffsets, Float>?,
		altOffsetConfigs: Map<SkeletonConfigOffsets, Float>?,
	) : this(server = null) {
		skeleton = HumanSkeleton(this, trackers)
		// Set default node offsets on the new skeleton
		skeletonConfigManager.updateNodeOffsetsInSkeleton()
		// Set offsetConfigs from given offsetConfigs on creation
		if (altOffsetConfigs != null) {
			// Set alts first, so if there's any overlap it doesn't affect
			// the values
			skeletonConfigManager.setOffsets(altOffsetConfigs)
		}
		skeletonConfigManager.setOffsets(offsetConfigs)
		skeletonConfigManager.updateSettingsInSkeleton()
	}

	// #endregion
	// #region private methods
	private fun initializeComputedHumanPoseTracker() {
		computedTrackers
			.add(
				Tracker(
					null,
					getNextLocalTrackerId(),
					"human://HEAD",
					"Computed head",
					TrackerPosition.HEAD,
					hasPosition = true,
					hasRotation = true,
					isInternal = true,
					isComputed = true,
				),
			)
		computedTrackers
			.add(
				Tracker(
					null,
					getNextLocalTrackerId(),
					"human://CHEST",
					"Computed chest",
					TrackerPosition.UPPER_CHEST,
					hasPosition = true,
					hasRotation = true,
					isInternal = true,
					isComputed = true,
				),
			)
		computedTrackers
			.add(
				Tracker(
					null,
					getNextLocalTrackerId(),
					"human://WAIST",
					"Computed hip",
					TrackerPosition.HIP,
					hasPosition = true,
					hasRotation = true,
					isInternal = true,
					isComputed = true,
				),
			)
		computedTrackers
			.add(
				Tracker(
					null,
					getNextLocalTrackerId(),
					"human://LEFT_KNEE",
					"Computed left knee",
					TrackerPosition.LEFT_UPPER_LEG,
					hasPosition = true,
					hasRotation = true,
					isInternal = true,
					isComputed = true,
				),
			)
		computedTrackers
			.add(
				Tracker(
					null,
					getNextLocalTrackerId(),
					"human://RIGHT_KNEE",
					"Computed right knee",
					TrackerPosition.RIGHT_UPPER_LEG,
					hasPosition = true,
					hasRotation = true,
					isInternal = true,
					isComputed = true,
				),
			)
		computedTrackers
			.add(
				Tracker(
					null,
					getNextLocalTrackerId(),
					"human://LEFT_FOOT",
					"Computed left foot",
					TrackerPosition.LEFT_FOOT,
					hasPosition = true,
					hasRotation = true,
					isInternal = true,
					isComputed = true,
				),
			)
		computedTrackers
			.add(
				Tracker(
					null,
					getNextLocalTrackerId(),
					"human://RIGHT_FOOT",
					"Computed right foot",
					TrackerPosition.RIGHT_FOOT,
					hasPosition = true,
					hasRotation = true,
					isInternal = true,
					isComputed = true,
				),
			)
		computedTrackers
			.add(
				Tracker(
					null,
					getNextLocalTrackerId(),
					"human://LEFT_ELBOW",
					"Computed left elbow",
					TrackerPosition.LEFT_UPPER_ARM,
					hasPosition = true,
					hasRotation = true,
					isInternal = true,
					isComputed = true,
				),
			)
		computedTrackers
			.add(
				Tracker(
					null,
					getNextLocalTrackerId(),
					"human://RIGHT_ELBOW",
					"Computed right elbow",
					TrackerPosition.RIGHT_UPPER_ARM,
					hasPosition = true,
					hasRotation = true,
					isInternal = true,
					isComputed = true,
				),
			)
		computedTrackers
			.add(
				Tracker(
					null,
					getNextLocalTrackerId(),
					"human://LEFT_HAND",
					"Computed left hand",
					TrackerPosition.LEFT_HAND,
					hasPosition = true,
					hasRotation = true,
					isInternal = true,
					isComputed = true,

				),
			)
		computedTrackers
			.add(
				Tracker(
					null,
					getNextLocalTrackerId(),
					"human://RIGHT_HAND",
					"Computed right hand",
					TrackerPosition.RIGHT_HAND,
					hasPosition = true,
					hasRotation = true,
					isInternal = true,
					isComputed = true,
				),
			)

		connectComputedHumanPoseTrackers()
	}

	fun loadFromConfig(configManager: ConfigManager) {
		skeletonConfigManager.loadFromConfig(configManager)
	}

	@VRServerThread
	fun updateSkeletonModelFromServer() {
		skeleton.setTrackersFromList(server!!.allTrackers)
	}

	private fun connectComputedHumanPoseTrackers() {
		for (t in computedTrackers) {
			t.status = TrackerStatus.OK
		}
	}

	// #endregion
	// #region public methods
	// #region skeleton methods
	@VRServerThread
	fun trackerAdded(tracker: Tracker?) {
	}

	@VRServerThread
	fun trackerUpdated(tracker: Tracker?) {
	}

	@VRServerThread
	fun addSkeletonUpdatedCallback(consumer: Consumer<HumanSkeleton>) {
		onSkeletonUpdated.add(consumer)
		consumer.accept(skeleton)
	}

	/**
	 * @return False if the skeleton isn't yet initialized
	 */
	@get:ThreadSafe
	val isSkeletonPresent: Boolean
		get() = this::skeleton.isInitialized

	/**
	 * Updates the pose of the skeleton from trackers rotations
	 */
	@VRServerThread
	fun update() {
		skeleton.updatePose()
	}

	/**
	 * Get the corresponding computed tracker for a given TrackerRole
	 *
	 * @param trackerRole the role of the tracker which we want
	 * @return the corresponding computed tracker for the trackerRole
	 */
	@ThreadSafe
	fun getComputedTracker(trackerRole: TrackerRole): Tracker = skeleton.getComputedTracker(trackerRole)

	/**
	 * @return the head bone, which is the root of the skeleton
	 */
	@get:ThreadSafe
	val headBone: Bone
		get() = skeleton.headBone

	/**
	 * Get a specified bone from the passed BoneType
	 *
	 * @param boneType the type of the bone we want
	 * @return the specified bone
	 */
	@ThreadSafe
	fun getBone(boneType: BoneType): Bone = skeleton.getBone(boneType)

	/**
	 * @return the HMD's y position from the skeleton
	 */
	@get:ThreadSafe
	val hmdHeight: Float
		get() = skeleton.hmdHeight

	/**
	 * Runs checks to know if we should (and are) performing the tracking of the
	 * left arm from the controller.
	 *
	 * @return a bool telling us if we are tracking the left arm from the
	 * controller or not.
	 */
	@get:ThreadSafe
	val isTrackingLeftArmFromController: Boolean
		get() = skeleton.isTrackingLeftArmFromController

	/**
	 * Runs checks to know if we should (and are) performing the tracking of the
	 * right arm from the controller.
	 *
	 * @return a bool telling us if we are tracking the right arm from the
	 * controller or not.
	 */
	@get:ThreadSafe
	val isTrackingRightArmFromController: Boolean
		get() = skeleton.isTrackingRightArmFromController

	/**
	 * @return All non-tracker bones
	 */
	@get:ThreadSafe
	val allBones: List<Bone>
		get() = listOf(*skeleton.allHumanBones)

	// #endregion
	// #region config methods
	/**
	 * @param key the offset from which to get the corresponding value
	 * @return the offset in config corresponding to the key
	 */
	@ThreadSafe
	fun getOffset(key: SkeletonConfigOffsets?): Float = skeletonConfigManager.getOffset(key)

	/**
	 * @param key the offset to set the length to
	 * @param newLength the new attributed length to the offset
	 */
	@ThreadSafe
	fun setOffset(key: SkeletonConfigOffsets, newLength: Float?) {
		skeletonConfigManager.setOffset(key, newLength)
	}

	/**
	 * Resets all the offsets in the current SkeletonConfigManager
	 */
	@ThreadSafe
	fun resetOffsets() {
		skeletonConfigManager.resetOffsets()
	}

	/**
	 * @param key the toggle from which to get the corresponding value
	 * @return the toggle in config corresponding to the key
	 */
	@ThreadSafe
	fun getToggle(key: SkeletonConfigToggles?): Boolean = skeletonConfigManager.getToggle(key)

	/**
	 * @param key the toggle to set the value to
	 * @param newValue the new attributed value to the toggle
	 */
	@ThreadSafe
	fun setToggle(key: SkeletonConfigToggles, newValue: Boolean?) {
		skeletonConfigManager.setToggle(key, newValue)
	}

	/**
	 * Resets all the toggles in the current SkeletonConfigManager
	 */
	@ThreadSafe
	fun resetToggles() {
		skeletonConfigManager.resetToggles()
	}

	/**
	 * @param key the value from which to get the corresponding value
	 * @return the value in config corresponding to the key
	 */
	@ThreadSafe
	fun getValue(key: SkeletonConfigValues): Float = skeletonConfigManager.getValue(key)

	/**
	 * @param key the value to set the value to
	 * @param newValue the new attributed value to the value
	 */
	@ThreadSafe
	fun setValue(key: SkeletonConfigValues, newValue: Float?) {
		skeletonConfigManager.setValue(key, newValue)
	}

	/**
	 * Resets all the values in the current SkeletonConfigManager
	 */
	@ThreadSafe
	fun resetValues() {
		skeletonConfigManager.resetValues()
	}

	/**
	 * Resets all the skeleton configs in the current SkeletonConfigManager
	 */
	@ThreadSafe
	fun resetAllConfigs() {
		skeletonConfigManager.resetAllConfigs()
	}

	/**
	 * Writes the skeleton configs
	 */
	@ThreadSafe
	fun saveConfig() {
		skeletonConfigManager.save()
	}

	/**
	 * Update the given bone with the given offset
	 *
	 * @param boneType the type of the bone to update
	 * @param offset the new offset to apply to the boneType
	 */
	@ThreadSafe
	fun updateNodeOffset(boneType: BoneType, offset: Vector3) {
		if (!isSkeletonPresent) return
		skeleton.updateNodeOffset(boneType, offset)
	}

	/**
	 * Updates all the node offsets in the skeleton
	 */
	fun updateNodeOffsetsInSkeleton() {
		if (!isSkeletonPresent) return
		skeletonConfigManager.updateNodeOffsetsInSkeleton()
	}

	/**
	 * Update the given toggle to the new given value in the skeleton
	 *
	 * @param configToggle the toggle to update
	 * @param newValue the new value to apply to the toggle
	 */
	@ThreadSafe
	fun updateToggleState(configToggle: SkeletonConfigToggles, newValue: Boolean) {
		skeleton.updateToggleState(configToggle, newValue)
	}

	/**
	 * Update the given value to the new given value in the skeleton
	 *
	 * @param configValue the value to update
	 * @param newValue the new value to apply to the value
	 */
	@ThreadSafe
	fun updateValueState(configValue: SkeletonConfigValues, newValue: Float) {
		skeleton.updateValueState(configValue, newValue)
	}

	/**
	 * Compute the offset for the given node and apply the new offset
	 *
	 * @param node the node to update
	 */
	fun computeNodeOffset(node: BoneType) {
		skeletonConfigManager.computeNodeOffset(node)
	}

	fun resetTrackersFull(resetSourceName: String?) {
		skeleton.resetTrackersFull(resetSourceName)
		if (server != null) {
			if (skeleton.headTracker == null && skeleton.neckTracker == null) {
				server.vrcOSCHandler.yawAlign(IDENTITY)
			} else {
				server.vrcOSCHandler
					.yawAlign(
						headBone.getGlobalRotation().project(POS_Y),
					)
			}
			server.vMCHandler.alignVMCTracking(headBone.getGlobalRotation())
			logTrackersDrift()
		}
	}

	fun resetTrackersYaw(resetSourceName: String?) {
		skeleton.resetTrackersYaw(resetSourceName)
		if (server != null) {
			if (skeleton.headTracker == null && skeleton.neckTracker == null) {
				server.vrcOSCHandler.yawAlign(IDENTITY)
			} else {
				server.vrcOSCHandler
					.yawAlign(
						headBone.getGlobalRotation().project(POS_Y),
					)
			}
			server.vMCHandler.alignVMCTracking(headBone.getGlobalRotation())
			logTrackersDrift()
		}
	}

	private fun logTrackersDrift() {
		if (timeAtLastReset == 0L) timeAtLastReset = System.currentTimeMillis()

		// Get time since last reset in seconds
		val timeSinceLastReset = (System.currentTimeMillis() - timeAtLastReset) / 1000L
		timeAtLastReset = System.currentTimeMillis()

		// Build String for trackers drifts
		val trackersDriftText = StringBuilder()
		for (tracker in server!!.allTrackers) {
			if ((
					tracker.isImu() &&
						tracker.needsReset
					) && tracker.resetsHandler.lastResetQuaternion != null
			) {
				if (trackersDriftText.isNotEmpty()) {
					trackersDriftText.append(" | ")
				}

				// Get the difference between last reset and now
				val difference = tracker
					.getRotation() * tracker.resetsHandler.lastResetQuaternion!!.inv()
				// Get the pure yaw
				var trackerDriftAngle = abs(
					(
						atan2(difference.y, difference.w) *
							2 *
							FastMath.RAD_TO_DEG
						),
				)
				// Fix for polarity or something
				if (trackerDriftAngle > 180) trackerDriftAngle = abs((trackerDriftAngle - 360))

				// Calculate drift per minute
				val driftPerMin = trackerDriftAngle / (timeSinceLastReset / 60f)

				trackersDriftText.append(tracker.name)
				val trackerPosition = tracker.trackerPosition
				if (trackerPosition != null) trackersDriftText.append(" (").append(trackerPosition.name).append(")")

				trackersDriftText
					.append(", ")
					.append(Precision.round(trackerDriftAngle, 4))
					.append(" deg (")
					.append(Precision.round(driftPerMin, 4))
					.append(" deg/min)")
			}
		}

		if (trackersDriftText.isNotEmpty()) {
			LogManager
				.info(
					"[HumanPoseManager] $timeSinceLastReset seconds since last reset. Tracker yaw drifts: $trackersDriftText",
				)
		}
	}

	fun resetTrackersMounting(resetSourceName: String?) {
		skeleton.resetTrackersMounting(resetSourceName)
	}

	fun clearTrackersMounting(resetSourceName: String?) {
		skeleton.clearTrackersMounting(resetSourceName)
	}

	@get:ThreadSafe
	val legTweaksState: BooleanArray
		get() = skeleton.legTweaksState

	@VRServerThread
	fun setLegTweaksEnabled(value: Boolean) {
		skeleton.setLegTweaksEnabled(value)
	}

	@VRServerThread
	fun setIKSolverEnabled(value: Boolean) {
		skeleton.setIKSolverEnabled(value)
	}

	@VRServerThread
	fun setFloorClipEnabled(value: Boolean) {
		skeleton.setFloorclipEnabled(value)
		if (server != null) {
			server.configManager
				.vrConfig
				.skeleton
				.getToggles()[SkeletonConfigToggles.FLOOR_CLIP.configKey] = value
			server.configManager.saveConfig()
		}
	}

	@VRServerThread
	fun setSkatingCorrectionEnabled(value: Boolean) {
		skeleton.setSkatingCorrectionEnabled(value)
		if (server != null) {
			server.configManager
				.vrConfig
				.skeleton
				.getToggles()[SkeletonConfigToggles.SKATING_CORRECTION.configKey] = value
			server.configManager.saveConfig()
		}
	}

	fun setLegTweaksStateTemp(
		skatingCorrection: Boolean,
		floorClip: Boolean,
		toeSnap: Boolean,
		footPlant: Boolean,
	) {
		skeleton.setLegTweaksStateTemp(skatingCorrection, floorClip, toeSnap, footPlant)
	}

	fun clearLegTweaksStateTemp(
		skatingCorrection: Boolean,
		floorClip: Boolean,
		toeSnap: Boolean,
		footPlant: Boolean,
	) {
		skeleton.clearLegTweaksStateTemp(skatingCorrection, floorClip, toeSnap, footPlant)
	}

	fun updateTapDetectionConfig() {
		skeleton.updateTapDetectionConfig()
	}

	fun updateLegTweaksConfig() {
		skeleton.updateLegTweaksConfig()
	}

	@get:ThreadSafe
	val userHeightFromConfig: Float
		get() = skeletonConfigManager.userHeightFromOffsets

	// #endregion
	fun getPauseTracking(): Boolean = skeleton.getPauseTracking()

	fun setPauseTracking(pauseTracking: Boolean, sourceName: String?) {
		skeleton.setPauseTracking(pauseTracking, sourceName)
	}

	fun togglePauseTracking(sourceName: String?): Boolean = skeleton.togglePauseTracking(sourceName)

	// This should be executed when the head tracker is changed
	fun checkTrackersRequiringReset() {
		// Checks if this is main human pose manager (having server) or
		// skeleton doesn't have a head tracker or not an HMD one
		if (server == null ||
			skeleton.headTracker?.isComputed != true
		) {
			return
		}
		server.allTrackers
			.filter { !it.isInternal && it.trackerPosition != null }
			.forEach {
				it.checkReportRequireReset()
			}
	}

	private var lastMissingHmdStatus = 0u
	fun checkReportMissingHmd() {
		// Check if this is main skeleton, there is no head tracker currently,
		// and there is an available HMD one
		if (server == null) return
		val tracker = VRServer.instance.allTrackers.firstOrNull { it.isHmd && !it.isInternal && it.status.sendData }
		if (skeleton.headTracker == null &&
			lastMissingHmdStatus == 0u &&
			tracker != null
		) {
			reportMissingHmd(tracker)
		} else if (lastMissingHmdStatus != 0u &&
			(skeleton.headTracker != null || tracker == null)
		) {
			server.statusSystem.removeStatus(lastMissingHmdStatus)
			lastMissingHmdStatus = 0u
		}
	}

	private fun reportMissingHmd(tracker: Tracker) {
		require(lastMissingHmdStatus == 0u) {
			"${::lastMissingHmdStatus.name} must be 0u, but was $lastMissingHmdStatus"
		}
		require(server != null) {
			"${::server.name} must not be null"
		}

		val status = StatusDataUnion().apply {
			type = StatusData.StatusUnassignedHMD
			value = StatusUnassignedHMDT().apply {
				trackerId = TrackerIdT().apply {
					if (tracker.device != null) {
						deviceId = DeviceIdT().apply { id = tracker.device.id }
					}
					trackerNum = tracker.trackerNum
				}
			}
		}
		lastMissingHmdStatus = server.statusSystem.addStatus(status, true)
	}

	// #endregion
}
