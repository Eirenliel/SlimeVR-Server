package dev.slimevr.tracking.processor;

import com.jme3.math.FastMath;
import dev.slimevr.VRServer;
import dev.slimevr.config.ConfigManager;
import dev.slimevr.tracking.processor.config.SkeletonConfigManager;
import dev.slimevr.tracking.processor.config.SkeletonConfigOffsets;
import dev.slimevr.tracking.processor.config.SkeletonConfigToggles;
import dev.slimevr.tracking.processor.config.SkeletonConfigValues;
import dev.slimevr.tracking.processor.skeleton.HumanSkeleton;
import dev.slimevr.tracking.trackers.Tracker;
import dev.slimevr.tracking.trackers.TrackerPosition;
import dev.slimevr.tracking.trackers.TrackerRole;
import dev.slimevr.tracking.trackers.TrackerStatus;
import dev.slimevr.util.ann.VRServerThread;
import io.eiren.util.ann.ThreadSafe;
import io.eiren.util.collections.FastList;
import io.eiren.util.logging.LogManager;
import io.github.axisangles.ktmath.Quaternion;
import io.github.axisangles.ktmath.Vector3;
import org.apache.commons.math3.util.Precision;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


/**
 * Class to handle communicate between classes in "skeleton" package and outside
 */
public class HumanPoseManager {

	private VRServer server;
	private final List<Tracker> computedTrackers = new FastList<>();
	private final List<Consumer<HumanSkeleton>> onSkeletonUpdated = new FastList<>();
	private final SkeletonConfigManager skeletonConfigManager;
	private HumanSkeleton skeleton;
	private long timeAtLastReset;

	// #region Constructors
	private HumanPoseManager() {
		skeletonConfigManager = new SkeletonConfigManager(true, this);
		initializeComputedHumanPoseTracker();
	}

	/**
	 * Creates a new HumanPoseManager that uses the VRServer
	 *
	 * @param server the used VRServer
	 */
	public HumanPoseManager(VRServer server) {
		this();
		this.server = server;
	}

	/**
	 * Creates a new HumanPoseManager that uses the given trackers for the
	 * HumanSkeleton and the default config
	 *
	 * @param trackers a list of all trackers
	 */
	public HumanPoseManager(List<Tracker> trackers) {
		this();
		skeleton = new HumanSkeleton(this, trackers);
		// Set default node offsets on the new skeleton
		skeletonConfigManager.updateNodeOffsetsInSkeleton();
		skeletonConfigManager.updateSettingsInSkeleton();
	}

	/**
	 * Creates a new HumanPoseManager that uses the given trackers for the
	 * HumanSkeleton and the given offsets for config
	 *
	 * @param trackers a list of all trackers
	 * @param offsetConfigs a map of the SkeletonConfigOffsets and values for
	 * them
	 */
	public HumanPoseManager(
		List<Tracker> trackers,
		Map<SkeletonConfigOffsets, Float> offsetConfigs
	) {
		this();
		skeleton = new HumanSkeleton(this, trackers);
		// Set default node offsets on the new skeleton
		skeletonConfigManager.updateNodeOffsetsInSkeleton();
		// Set offsetConfigs from given offsetConfigs on creation
		skeletonConfigManager.setOffsets(offsetConfigs);
		skeletonConfigManager.updateSettingsInSkeleton();
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
	public HumanPoseManager(
		List<Tracker> trackers,
		Map<SkeletonConfigOffsets, Float> offsetConfigs,
		Map<SkeletonConfigOffsets, Float> altOffsetConfigs
	) {
		this();
		skeleton = new HumanSkeleton(this, trackers);
		// Set default node offsets on the new skeleton
		skeletonConfigManager.updateNodeOffsetsInSkeleton();
		// Set offsetConfigs from given offsetConfigs on creation
		if (altOffsetConfigs != null) {
			// Set alts first, so if there's any overlap it doesn't affect
			// the values
			skeletonConfigManager.setOffsets(altOffsetConfigs);
		}
		skeletonConfigManager.setOffsets(offsetConfigs);
		skeletonConfigManager.updateSettingsInSkeleton();
	}

	// #endregion
	// #region private methods
	private void initializeComputedHumanPoseTracker() {
		computedTrackers
			.add(
				new Tracker(
					null,
					VRServer.getNextLocalTrackerId(),
					"human://HEAD",
					"Computed head",
					TrackerPosition.HEAD,
					null,
					true,
					true,
					false,
					false,
					true,
					true
				)
			);
		computedTrackers
			.add(
				new Tracker(
					null,
					VRServer.getNextLocalTrackerId(),
					"human://CHEST",
					"Computed chest",
					TrackerPosition.CHEST,
					null,
					true,
					true,
					false,
					false,
					true,
					true
				)
			);
		computedTrackers
			.add(
				new Tracker(
					null,
					VRServer.getNextLocalTrackerId(),
					"human://WAIST",
					"Computed hip",
					TrackerPosition.HIP,
					null,
					true,
					true,
					false,
					false,
					true,
					true
				)
			);
		computedTrackers
			.add(
				new Tracker(
					null,
					VRServer.getNextLocalTrackerId(),
					"human://LEFT_FOOT",
					"Computed left foot",
					TrackerPosition.LEFT_FOOT,
					null,
					true,
					true,
					false,
					false,
					true,
					true
				)
			);
		computedTrackers
			.add(
				new Tracker(
					null,
					VRServer.getNextLocalTrackerId(),
					"human://RIGHT_FOOT",
					"Computed right foot",
					TrackerPosition.RIGHT_FOOT,
					null,
					true,
					true,
					false,
					false,
					true,
					true
				)
			);
		computedTrackers
			.add(
				new Tracker(
					null,
					VRServer.getNextLocalTrackerId(),
					"human://LEFT_KNEE",
					"Computed left knee",
					TrackerPosition.LEFT_UPPER_LEG,
					null,
					true,
					true,
					false,
					false,
					true,
					true
				)
			);
		computedTrackers
			.add(
				new Tracker(
					null,
					VRServer.getNextLocalTrackerId(),
					"human://RIGHT_KNEE",
					"Computed right knee",
					TrackerPosition.RIGHT_UPPER_LEG,
					null,
					true,
					true,
					false,
					false,
					true,
					true
				)
			);
		computedTrackers
			.add(
				new Tracker(
					null,
					VRServer.getNextLocalTrackerId(),
					"human://LEFT_ELBOW",
					"Computed left elbow",
					TrackerPosition.LEFT_UPPER_ARM,
					null,
					true,
					true,
					false,
					false,
					true,
					true
				)
			);
		computedTrackers
			.add(
				new Tracker(
					null,
					VRServer.getNextLocalTrackerId(),
					"human://RIGHT_ELBOW",
					"Computed right elbow",
					TrackerPosition.RIGHT_UPPER_ARM,
					null,
					true,
					true,
					false,
					false,
					true,
					true
				)
			);
		computedTrackers
			.add(
				new Tracker(
					null,
					VRServer.getNextLocalTrackerId(),
					"human://LEFT_HAND",
					"Computed left hand",
					TrackerPosition.LEFT_HAND,
					null,
					true,
					true,
					false,
					false,
					true,
					true

				)
			);
		computedTrackers
			.add(
				new Tracker(
					null,
					VRServer.getNextLocalTrackerId(),
					"human://RIGHT_HAND",
					"Computed right hand",
					TrackerPosition.RIGHT_HAND,
					null,
					true,
					true,
					false,
					false,
					true,
					true
				)
			);
	}

	public void loadFromConfig(ConfigManager configManager) {
		skeletonConfigManager.loadFromConfig(configManager);
	}

	@VRServerThread
	public void updateSkeletonModelFromServer() {
		disconnectComputedHumanPoseTrackers();
		skeleton = new HumanSkeleton(this, server);
		// This recomputes all node offsets, so the defaults don't need to be
		// explicitly loaded into the skeleton (no need for
		// `updateNodeOffsetsInSkeleton()`)
		loadFromConfig(server.getConfigManager());
		for (Consumer<HumanSkeleton> sc : onSkeletonUpdated)
			sc.accept(skeleton);
	}

	@VRServerThread
	private void disconnectComputedHumanPoseTrackers() {
		for (Tracker t : computedTrackers) {
			t.setStatus(TrackerStatus.DISCONNECTED);
		}
	}

	// #endregion
	// #region public methods
	// #region skeleton methods
	@VRServerThread
	public void trackerAdded(Tracker tracker) {
		updateSkeletonModelFromServer();
	}

	@VRServerThread
	public void trackerUpdated(Tracker tracker) {
		updateSkeletonModelFromServer();
	}

	@VRServerThread
	public void addSkeletonUpdatedCallback(Consumer<HumanSkeleton> consumer) {
		onSkeletonUpdated.add(consumer);
		if (isSkeletonPresent())
			consumer.accept(skeleton);
	}

	/**
	 * @return True if the skeleton isn't null, or false if it's null
	 */
	@ThreadSafe
	public boolean isSkeletonPresent() {
		return skeleton != null;
	}

	/**
	 * Updates the pose of the skeleton from trackers rotations
	 */
	@VRServerThread
	public void update() {
		if (isSkeletonPresent())
			skeleton.updatePose();
	}

	@ThreadSafe
	public HumanSkeleton getSkeleton() {
		return skeleton;
	}

	// #endregion
	// #region tracker/nodes/bones methods
	/**
	 * @return a list of the computed trackers
	 */
	@ThreadSafe
	public List<Tracker> getComputedTrackers() {
		return computedTrackers;
	}

	/**
	 * Get the corresponding computed tracker for a given TrackerRole
	 *
	 * @param trackerRole the role of the tracker which we want
	 * @return the corresponding computed tracker for the trackerRole
	 */
	@ThreadSafe
	public Tracker getComputedTracker(TrackerRole trackerRole) {
		if (isSkeletonPresent())
			return skeleton.getComputedTracker(trackerRole);
		return null;
	}

	/**
	 * Returns all trackers if VRServer is non-null. Else, returns the
	 * skeleton's assigned trackers.
	 *
	 * @return a list of trackers to use for reset.
	 */
	public List<Tracker> getTrackersToReset() {
		if (server != null)
			return server.getAllTrackers();
		else if (isSkeletonPresent()) {
			return skeleton.getLocalTrackers();
		}
		return null;
	}

	/**
	 * @return the root node of the skeleton, which is the HMD
	 */
	@ThreadSafe
	public TransformNode getRootNode() {
		if (isSkeletonPresent())
			return skeleton.getRootNode();
		return null;
	}

	/**
	 * Get the tail node (away from the tracking root) of the given bone
	 *
	 * @param bone the bone from which we want the tail node
	 * @return the tail node of the bone
	 */
	@ThreadSafe
	public TransformNode getTailNodeOfBone(BoneType bone) {
		if (isSkeletonPresent())
			return skeleton.getTailNodeOfBone(bone);
		return null;
	}

	/**
	 * @return the HMD's y position from the skeleton
	 */
	@ThreadSafe
	public float getHmdHeight() {
		if (isSkeletonPresent())
			return skeleton.getHmdHeight();
		return 0.0f;
	}

	/**
	 * Runs checks to know if we should (and are) performing the tracking of the
	 * left arm from the controller.
	 *
	 * @return a bool telling us if we are tracking the left arm from the
	 * controller or not.
	 */
	@ThreadSafe
	public boolean isTrackingLeftArmFromController() {
		if (isSkeletonPresent())
			return skeleton.isTrackingLeftArmFromController();
		return false;
	}

	/**
	 * Runs checks to know if we should (and are) performing the tracking of the
	 * right arm from the controller.
	 *
	 * @return a bool telling us if we are tracking the right arm from the
	 * controller or not.
	 */
	@ThreadSafe
	public boolean isTrackingRightArmFromController() {
		if (isSkeletonPresent())
			return skeleton.isTrackingRightArmFromController();
		return false;
	}

	/**
	 * @return All skeleton bones as BoneInfo
	 */
	@ThreadSafe
	public List<BoneInfo> getAllBoneInfo() {
		if (isSkeletonPresent())
			return skeleton.allBoneInfo;
		return null;
	}

	/**
	 * @return All shareable bones as BoneInfo
	 */
	@ThreadSafe
	public List<BoneInfo> getShareableBoneInfo() {
		if (isSkeletonPresent())
			return skeleton.shareableBoneInfo;
		return null;
	}

	/**
	 * @return The bone as BoneInfo for the given BoneType
	 */
	@ThreadSafe
	public BoneInfo getBoneInfoForBoneType(BoneType boneType) {
		if (isSkeletonPresent())
			return skeleton.getBoneInfoForBoneType(boneType);
		return null;
	}

	// #endregion
	// #region config methods
	/**
	 * @param key the offset from which to get the corresponding value
	 * @return the offset in config corresponding to the key
	 */
	@ThreadSafe
	public float getOffset(SkeletonConfigOffsets key) {
		return skeletonConfigManager.getOffset(key);
	}

	/**
	 * @param key the offset to set the length to
	 * @param newLength the new attributed length to the offset
	 */
	@ThreadSafe
	public void setOffset(SkeletonConfigOffsets key, Float newLength) {
		skeletonConfigManager.setOffset(key, newLength);
	}

	/**
	 * Resets all the offsets in the current SkeletonConfigManager
	 */
	@ThreadSafe
	public void resetOffsets() {
		skeletonConfigManager.resetOffsets();
	}

	/**
	 * @param key the toggle from which to get the corresponding value
	 * @return the toggle in config corresponding to the key
	 */
	@ThreadSafe
	public boolean getToggle(SkeletonConfigToggles key) {
		return skeletonConfigManager.getToggle(key);
	}

	/**
	 * @param key the toggle to set the value to
	 * @param newValue the new attributed value to the toggle
	 */
	@ThreadSafe
	public void setToggle(SkeletonConfigToggles key, Boolean newValue) {
		skeletonConfigManager.setToggle(key, newValue);
	}

	/**
	 * Resets all the toggles in the current SkeletonConfigManager
	 */
	@ThreadSafe
	public void resetToggles() {
		skeletonConfigManager.resetToggles();
	}

	/**
	 * @param key the value from which to get the corresponding value
	 * @return the value in config corresponding to the key
	 */
	@ThreadSafe
	public float getValue(SkeletonConfigValues key) {
		return skeletonConfigManager.getValue(key);
	}

	/**
	 * @param key the value to set the value to
	 * @param newValue the new attributed value to the value
	 */
	@ThreadSafe
	public void setValue(SkeletonConfigValues key, Float newValue) {
		skeletonConfigManager.setValue(key, newValue);
	}

	/**
	 * Resets all the values in the current SkeletonConfigManager
	 */
	@ThreadSafe
	public void resetValues() {
		skeletonConfigManager.resetValues();
	}

	/**
	 * Resets all the skeleton configs in the current SkeletonConfigManager
	 */
	@ThreadSafe
	public void resetAllConfigs() {
		skeletonConfigManager.resetAllConfigs();
	}

	/**
	 * Writes the skeleton configs
	 */
	@ThreadSafe
	public void saveConfig() {
		skeletonConfigManager.save();
	}

	/**
	 * Update the given node with the given offset
	 *
	 * @param node the node to update
	 * @param offset the new offset to apply to the node
	 */
	@ThreadSafe
	public void updateNodeOffset(BoneType node, Vector3 offset) {
		if (isSkeletonPresent())
			skeleton.updateNodeOffset(node, offset);
	}

	/**
	 * Update the given toggle to the new given value in the skeleton
	 *
	 * @param configToggle the toggle to update
	 * @param newValue the new value to apply to the toggle
	 */
	@ThreadSafe
	public void updateToggleState(SkeletonConfigToggles configToggle, boolean newValue) {
		if (isSkeletonPresent())
			skeleton.updateToggleState(configToggle, newValue);
	}

	/**
	 * Update the given value to the new given value in the skeleton
	 *
	 * @param configValue the value to update
	 * @param newValue the new value to apply to the value
	 */
	@ThreadSafe
	public void updateValueState(SkeletonConfigValues configValue, float newValue) {
		if (isSkeletonPresent())
			skeleton.updateValueState(configValue, newValue);
	}

	/**
	 * Compute the offset for the given node and apply the new offset
	 *
	 * @param node the node to update
	 */
	public void computeNodeOffset(BoneType node) {
		skeletonConfigManager.computeNodeOffset(node);
	}

	public void resetTrackersFull(String resetSourceName) {
		if (isSkeletonPresent()) {
			skeleton.resetTrackersFull(resetSourceName);
			if (server != null) {
				server.getVrcOSCHandler().yawAlign();
				server
					.getVMCHandler()
					.alignVMCTracking(getRootNode().getWorldTransform().getRotation());
				logTrackersDrift();
			}
		}
	}

	public void resetTrackersYaw(String resetSourceName) {
		if (isSkeletonPresent()) {
			skeleton.resetTrackersYaw(resetSourceName);
			if (server != null) {
				server.getVrcOSCHandler().yawAlign();
				server
					.getVMCHandler()
					.alignVMCTracking(getRootNode().getWorldTransform().getRotation());
				logTrackersDrift();
			}
		}
	}

	private void logTrackersDrift() {
		if (timeAtLastReset == 0L)
			timeAtLastReset = System.currentTimeMillis();

		// Get time since last reset in seconds
		long timeSinceLastReset = (System.currentTimeMillis() - timeAtLastReset) / 1000L;
		timeAtLastReset = System.currentTimeMillis();

		// Build String for trackers drifts
		StringBuilder trackersDriftText = new StringBuilder();
		for (Tracker tracker : server.getAllTrackers()) {
			if (
				tracker.getNeedsReset()
					&& tracker.getResetsHandler().getLastResetQuaternion() != null
			) {
				if (!trackersDriftText.isEmpty()) {
					trackersDriftText.append(" | ");
				}

				// Get the difference between last reset and now
				Quaternion difference = tracker
					.getRotation()
					.times(tracker.getResetsHandler().getLastResetQuaternion().inv());
				// Get the pure yaw
				float trackerDriftAngle = Math
					.abs(
						FastMath.atan2(difference.getY(), difference.getW())
							* 2
							* FastMath.RAD_TO_DEG
					);
				// Fix for polarity or something
				if (trackerDriftAngle > 180)
					trackerDriftAngle = Math.abs(trackerDriftAngle - 360);

				// Calculate drift per minute
				float driftPerMin = trackerDriftAngle / (timeSinceLastReset / 60f);

				trackersDriftText.append(tracker.getName());
				TrackerPosition trackerPosition = tracker.getTrackerPosition();
				if (trackerPosition != null)
					trackersDriftText.append(" (").append(trackerPosition.name()).append(")");

				trackersDriftText
					.append(", ")
					.append(Precision.round(trackerDriftAngle, 4))
					.append(" deg (")
					.append(Precision.round(driftPerMin, 4))
					.append(" deg/min)");
			}
		}

		if (trackersDriftText.length() > 0) {
			LogManager
				.info(
					"[HumanPoseManager] "
						+ timeSinceLastReset
						+ " seconds since last reset. Tracker yaw drifts: "
						+ trackersDriftText
				);
		}
	}

	public void resetTrackersMounting(String resetSourceName) {
		if (isSkeletonPresent())
			skeleton.resetTrackersMounting(resetSourceName);
	}

	@ThreadSafe
	public boolean[] getLegTweaksState() {
		return skeleton.getLegTweaksState();
	}

	@VRServerThread
	public void setLegTweaksEnabled(boolean value) {
		if (isSkeletonPresent())
			skeleton.setLegTweaksEnabled(value);
	}

	@VRServerThread
	public void setFloorClipEnabled(boolean value) {
		if (isSkeletonPresent()) {
			skeleton.setFloorclipEnabled(value);
			if (server != null) {
				server
					.getConfigManager()
					.getVrConfig().skeleton.toggles
						.put(SkeletonConfigToggles.FLOOR_CLIP.configKey, value);
				server.getConfigManager().saveConfig();
			}
		}
	}

	@VRServerThread
	public void setSkatingCorrectionEnabled(boolean value) {
		if (isSkeletonPresent()) {
			skeleton.setSkatingCorrectionEnabled(value);
			if (server != null) {
				server
					.getConfigManager()
					.getVrConfig().skeleton.toggles
						.put(SkeletonConfigToggles.SKATING_CORRECTION.configKey, value);
				server.getConfigManager().saveConfig();
			}
		}
	}

	public void setLegTweaksStateTemp(
		boolean skatingCorrection,
		boolean floorClip,
		boolean toeSnap,
		boolean footPlant
	) {
		if (isSkeletonPresent())
			skeleton.setLegTweaksStateTemp(skatingCorrection, floorClip, toeSnap, footPlant);
	}

	public void clearLegTweaksStateTemp(
		boolean skatingCorrection,
		boolean floorClip,
		boolean toeSnap,
		boolean footPlant
	) {
		if (isSkeletonPresent())
			skeleton.clearLegTweaksStateTemp(skatingCorrection, floorClip, toeSnap, footPlant);
	}

	public void updateTapDetectionConfig() {
		if (isSkeletonPresent())
			skeleton.updateTapDetectionConfig();
	}

	public void updateLegTweaksConfig() {
		if (isSkeletonPresent())
			skeleton.updateLegTweaksConfig();
	}

	@ThreadSafe
	public float getUserHeightFromConfig() {
		if (isSkeletonPresent()) {
			return skeletonConfigManager.getUserHeightFromOffsets();
		}
		return 0f;
	}
	// #endregion

	public void setPauseTracking(boolean pauseTracking) {
		if (isSkeletonPresent())
			skeleton.setPauseTracking(pauseTracking);
	}
	// #endregion
}
