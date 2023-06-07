package dev.slimevr.tracking.processor.skeleton;

import com.jme3.math.FastMath;
import dev.slimevr.VRServer;
import dev.slimevr.tracking.processor.BoneInfo;
import dev.slimevr.tracking.processor.BoneType;
import dev.slimevr.tracking.processor.HumanPoseManager;
import dev.slimevr.tracking.processor.TransformNode;
import dev.slimevr.tracking.processor.config.SkeletonConfigToggles;
import dev.slimevr.tracking.processor.config.SkeletonConfigValues;
import dev.slimevr.tracking.trackers.Tracker;
import dev.slimevr.tracking.trackers.TrackerPosition;
import dev.slimevr.tracking.trackers.TrackerRole;
import dev.slimevr.tracking.trackers.TrackerUtils;
import dev.slimevr.util.ann.VRServerThread;
import io.eiren.util.ann.ThreadSafe;
import io.eiren.util.collections.FastList;
import io.eiren.util.logging.LogManager;
import io.github.axisangles.ktmath.EulerAngles;
import io.github.axisangles.ktmath.EulerOrder;
import io.github.axisangles.ktmath.Quaternion;
import io.github.axisangles.ktmath.Vector3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class HumanSkeleton {
	protected final HumanPoseManager humanPoseManager;
	// #region Upper body nodes (torso)
	// @formatter:off
	protected final TransformNode hmdNode = new TransformNode(BoneType.HMD, false);
	protected final TransformNode headNode = new TransformNode(BoneType.HEAD, false);
	protected final TransformNode trackerHeadNode = new TransformNode(BoneType.HEAD_TRACKER, false);
	protected final TransformNode neckNode = new TransformNode(BoneType.NECK, false);
	protected final TransformNode chestNode = new TransformNode(BoneType.CHEST, false);
	protected final TransformNode trackerChestNode = new TransformNode(BoneType.CHEST_TRACKER, false);
	protected final TransformNode waistNode = new TransformNode(BoneType.WAIST, false);
	protected final TransformNode hipNode = new TransformNode(BoneType.HIP, false);
	protected final TransformNode trackerHipNode = new TransformNode(BoneType.HIP_TRACKER, false);
	// #endregion
	// #region Lower body nodes (legs)
	protected final TransformNode leftHipNode = new TransformNode(BoneType.LEFT_HIP, false);
	protected final TransformNode leftKneeNode = new TransformNode(BoneType.LEFT_UPPER_LEG, false);
	protected final TransformNode trackerLeftKneeNode = new TransformNode(BoneType.LEFT_KNEE_TRACKER, false);
	protected final TransformNode leftAnkleNode = new TransformNode(BoneType.LEFT_LOWER_LEG, false);
	protected final TransformNode leftFootNode = new TransformNode(BoneType.LEFT_FOOT, false);
	protected final TransformNode trackerLeftFootNode = new TransformNode(BoneType.LEFT_FOOT_TRACKER, false);
	protected final TransformNode rightHipNode = new TransformNode(BoneType.RIGHT_HIP, false);
	protected final TransformNode rightKneeNode = new TransformNode(BoneType.RIGHT_UPPER_LEG, false);
	protected final TransformNode trackerRightKneeNode = new TransformNode(BoneType.RIGHT_KNEE_TRACKER, false);
	protected final TransformNode rightAnkleNode = new TransformNode(BoneType.RIGHT_LOWER_LEG, false);
	protected final TransformNode rightFootNode = new TransformNode(BoneType.RIGHT_FOOT, false);
	protected final TransformNode trackerRightFootNode = new TransformNode(BoneType.RIGHT_FOOT_TRACKER, false);
	// #endregion
	// #region Arms
	protected final TransformNode leftShoulderHeadNode = new TransformNode(BoneType.LEFT_SHOULDER, false);
	protected final TransformNode rightShoulderHeadNode = new TransformNode(BoneType.RIGHT_SHOULDER, false);
	protected final TransformNode leftShoulderTailNode = new TransformNode(BoneType.LEFT_UPPER_ARM, false);
	protected final TransformNode rightShoulderTailNode = new TransformNode(BoneType.RIGHT_UPPER_ARM, false);
	protected final TransformNode leftElbowNode = new TransformNode(BoneType.LEFT_LOWER_ARM, false);
	protected final TransformNode rightElbowNode = new TransformNode(BoneType.RIGHT_LOWER_ARM, false);
	protected final TransformNode trackerLeftElbowNode = new TransformNode(BoneType.LEFT_ELBOW_TRACKER, false);
	protected final TransformNode trackerRightElbowNode = new TransformNode(BoneType.RIGHT_ELBOW_TRACKER, false);
	protected final TransformNode leftWristNode = new TransformNode(BoneType.LEFT_HAND, false);
	protected final TransformNode rightWristNode = new TransformNode(BoneType.RIGHT_HAND, false);
	protected final TransformNode leftHandNode = new TransformNode(BoneType.LEFT_HAND, false);
	protected final TransformNode rightHandNode = new TransformNode(BoneType.RIGHT_HAND, false);
	protected final TransformNode trackerLeftHandNode = new TransformNode(BoneType.LEFT_HAND_TRACKER, false);
	protected final TransformNode trackerRightHandNode = new TransformNode(BoneType.RIGHT_HAND_TRACKER, false);
	// @formatter:on
	public final List<BoneInfo> allBoneInfo = new ArrayList<>();
	public final List<BoneInfo> shareableBoneInfo = new ArrayList<>();
	// #endregion
	// #region Buffers
	protected boolean hasSpineTracker;
	protected boolean hasKneeTrackers;
	protected boolean hasLeftLegTracker;
	protected boolean hasRightLegTracker;
	protected boolean hasLeftArmTracker;
	protected boolean hasRightArmTracker;
	static final Quaternion FORWARD_QUATERNION = new EulerAngles(
		EulerOrder.YZX,
		FastMath.HALF_PI,
		0,
		0
	).toQuaternion();
	// #region Tracker Input
	protected Tracker headTracker;
	protected Tracker neckTracker;
	protected Tracker chestTracker;
	protected Tracker waistTracker;
	protected Tracker hipTracker;
	protected Tracker leftUpperLegTracker;
	protected Tracker leftLowerLegTracker;
	protected Tracker leftFootTracker;
	protected Tracker rightUpperLegTracker;
	protected Tracker rightLowerLegTracker;
	protected Tracker rightFootTracker;
	protected Tracker leftLowerArmTracker;
	protected Tracker rightLowerArmTracker;
	protected Tracker leftUpperArmTracker;
	protected Tracker rightUpperArmTracker;
	protected Tracker leftHandTracker;
	protected Tracker rightHandTracker;
	protected Tracker leftShoulderTracker;
	protected Tracker rightShoulderTracker;
	// #endregion
	// #region Tracker Output
	protected Tracker computedHeadTracker;
	protected Tracker computedChestTracker;
	protected Tracker computedHipTracker;
	protected Tracker computedLeftKneeTracker;
	protected Tracker computedLeftFootTracker;
	protected Tracker computedRightKneeTracker;
	protected Tracker computedRightFootTracker;
	protected Tracker computedLeftElbowTracker;
	protected Tracker computedRightElbowTracker;
	protected Tracker computedLeftHandTracker;
	protected Tracker computedRightHandTracker;
	// #endregion

	// #region Settings
	// Toggles
	protected boolean extendedSpineModel;
	protected boolean extendedPelvisModel;
	protected boolean extendedKneeModel;
	protected boolean forceArmsFromHMD = true;
	// Values
	protected float waistFromChestHipAveraging;
	protected float waistFromChestLegsAveraging;
	protected float hipFromChestLegsAveraging;
	protected float hipFromWaistLegsAveraging;
	protected float hipLegsAveraging;
	protected float kneeTrackerAnkleAveraging;
	// Others
	protected boolean sendAllBones = false;
	// Pauses skeleton tracking if true, resumes skeleton tracking if false
	protected boolean pauseTracking = false;
	// #endregion

	// #region Clip Correction
	protected LegTweaks legTweaks = new LegTweaks(this);
	// #endregion

	// #region tap detection
	protected TapDetectionManager tapDetectionManager = new TapDetectionManager(this);
	// #endregion

	// #region Vive emulation
	protected ViveEmulation viveEmulation = new ViveEmulation(this);
	// #endregion

	// #region Constructors
	protected HumanSkeleton(
		HumanPoseManager humanPoseManager
	) {
		this.humanPoseManager = humanPoseManager;

		assembleSkeleton();

		if (humanPoseManager.getComputedTrackers() != null) {
			setComputedTrackers(humanPoseManager.getComputedTrackers());
		}

		resetBones();
	}

	public HumanSkeleton(
		HumanPoseManager humanPoseManager,
		VRServer server
	) {
		this(humanPoseManager);

		setTrackersFromList(server.getAllTrackers());

		tapDetectionManager = new TapDetectionManager(
			this,
			humanPoseManager,
			server.getConfigManager().getVrConfig().tapDetection,
			server.getResetHandler(),
			server.getTapSetupHandler(),
			server.getAllTrackers()
		);
		legTweaks.setConfig(server.getConfigManager().getVrConfig().legTweaks);
	}

	public HumanSkeleton(
		HumanPoseManager humanPoseManager,
		List<Tracker> trackers
	) {
		this(humanPoseManager);

		setTrackersFromList(Objects.requireNonNullElseGet(trackers, () -> new FastList<>(0)));
	}
	// #endregion

	@ThreadSafe
	protected void assembleSkeleton() {
		// #region Assemble skeleton from head to hip
		hmdNode.attachChild(headNode);
		headNode.attachChild(neckNode);
		neckNode.attachChild(chestNode);
		chestNode.attachChild(waistNode);
		waistNode.attachChild(hipNode);
		// #endregion

		// #region Assemble skeleton from hips to feet
		hipNode.attachChild(leftHipNode);
		hipNode.attachChild(rightHipNode);

		leftHipNode.attachChild(leftKneeNode);
		rightHipNode.attachChild(rightKneeNode);

		leftKneeNode.attachChild(leftAnkleNode);
		rightKneeNode.attachChild(rightAnkleNode);

		leftAnkleNode.attachChild(leftFootNode);
		rightAnkleNode.attachChild(rightFootNode);
		// #endregion

		// #region Attach tracker nodes for tracker offsets
		neckNode.attachChild(trackerHeadNode);
		neckNode.attachChild(trackerChestNode);
		hipNode.attachChild(trackerHipNode);

		leftKneeNode.attachChild(trackerLeftKneeNode);
		rightKneeNode.attachChild(trackerRightKneeNode);

		leftFootNode.attachChild(trackerLeftFootNode);
		rightFootNode.attachChild(trackerRightFootNode);
		// #endregion

		// Attach arms
		assembleSkeletonArms(false);
	}

	@ThreadSafe
	protected void assembleSkeletonArms(boolean reset) {
		if (reset) {
			for (TransformNode node : getArmNodes()) {
				node.detachWithChildren();
			}
		}

		// #region Assemble skeleton arms
		neckNode.attachChild(leftShoulderHeadNode);
		neckNode.attachChild(rightShoulderHeadNode);

		leftShoulderHeadNode.attachChild(leftShoulderTailNode);
		rightShoulderHeadNode.attachChild(rightShoulderTailNode);

		if (isTrackingLeftArmFromController()) {
			leftWristNode.attachChild(leftElbowNode);
			leftHandNode.attachChild(leftWristNode);

		} else {
			leftShoulderTailNode.attachChild(leftElbowNode);
			leftElbowNode.attachChild(leftWristNode);
			leftWristNode.attachChild(leftHandNode);

		}
		if (isTrackingRightArmFromController()) {
			rightWristNode.attachChild(rightElbowNode);
			rightHandNode.attachChild(rightWristNode);
		} else {
			rightShoulderTailNode.attachChild(rightElbowNode);
			rightElbowNode.attachChild(rightWristNode);
			rightWristNode.attachChild(rightHandNode);

		}
		// #endregion

		// #region Attach tracker nodes for tracker offsets
		leftElbowNode.attachChild(trackerLeftElbowNode);
		rightElbowNode.attachChild(trackerRightElbowNode);

		leftHandNode.attachChild(trackerLeftHandNode);
		rightHandNode.attachChild(trackerRightHandNode);
		// #endregion
	}

	protected void resetBones() {
		allBoneInfo.clear();
		shareableBoneInfo.clear();

		// Create all bones and add to allBoneInfo
		for (BoneType boneType : BoneType.values)
			allBoneInfo.add(new BoneInfo(boneType, getTailNodeOfBone(boneType)));

		// Add shareable bones to shareableBoneInfo
		// Head
		shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.HEAD));
		shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.NECK));

		// Spine and legs
		if (hasSpineTracker || hasLeftLegTracker || hasRightLegTracker || sendAllBones) {
			// Spine
			shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.CHEST));
			shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.WAIST));
			shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.HIP));

			// Left leg
			if (hasLeftLegTracker || sendAllBones) {
				if (sendAllBones) {
					// don't send currently
					shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.LEFT_HIP));
				}

				shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.LEFT_UPPER_LEG));
				shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.LEFT_LOWER_LEG));

				if (leftFootTracker != null || sendAllBones) {
					shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.LEFT_FOOT));
				}
			}

			// Right leg
			if (hasRightLegTracker || sendAllBones) {
				if (sendAllBones) {
					// don't send currently
					shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.RIGHT_HIP));
				}

				shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.RIGHT_UPPER_LEG));
				shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.RIGHT_LOWER_LEG));

				if (rightFootTracker != null || sendAllBones) {
					shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.RIGHT_FOOT));
				}
			}
		}

		// TODO: Expose the hand/controller bones while accounting for the z and
		// y offsets.
		// TODO: Handle going from HMD and handle shoulder/upper arm. RN we only
		// support controller based bones in the overlay.

		// Left arm
		if ((hasLeftArmTracker || leftShoulderTracker != null) && sendAllBones) {
			// don't send currently
			shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.LEFT_SHOULDER));
		}
		if (hasLeftArmTracker || sendAllBones) {
			// don't send currently
			shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.LEFT_UPPER_ARM));
		}
		if (hasLeftArmTracker && sendAllBones) {
			shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.LEFT_LOWER_ARM));
		}
		if ((hasLeftArmTracker || leftHandTracker != null) && sendAllBones) {
			// don't send currently
			shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.LEFT_HAND));
		}

		// Right arm
		if ((hasRightArmTracker || rightShoulderTracker != null) && sendAllBones) {
			// don't send currently
			shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.RIGHT_SHOULDER));
		}
		if (hasRightArmTracker || sendAllBones) {
			// don't send currently
			shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.RIGHT_UPPER_ARM));
		}
		if (hasRightArmTracker && sendAllBones) {
			shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.RIGHT_LOWER_ARM));
		}
		if ((hasRightArmTracker && rightHandTracker != null) && sendAllBones) {
			// don't send currently
			shareableBoneInfo.add(getBoneInfoForBoneType(BoneType.RIGHT_HAND));
		}
	}


	// #region Set trackers inputs
	protected void setTrackersFromList(List<Tracker> trackers) {
		// TODO prioritize IMU over Computed for head
		headTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.HEAD
			);
		neckTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.NECK
			);
		chestTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.CHEST
			);
		waistTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.WAIST
			);
		hipTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.HIP
			);
		leftUpperLegTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.LEFT_UPPER_LEG
			);
		leftLowerLegTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.LEFT_LOWER_LEG
			);
		leftFootTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.LEFT_FOOT
			);
		rightUpperLegTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.RIGHT_UPPER_LEG
			);
		rightLowerLegTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.RIGHT_LOWER_LEG
			);
		rightFootTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.RIGHT_FOOT
			);
		leftLowerArmTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.LEFT_LOWER_ARM
			);
		rightLowerArmTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.RIGHT_LOWER_ARM
			);
		leftUpperArmTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.LEFT_UPPER_ARM
			);
		rightUpperArmTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.RIGHT_UPPER_ARM
			);
		leftHandTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.LEFT_HAND
			);
		rightHandTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.RIGHT_HAND
			);
		leftShoulderTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.LEFT_SHOULDER
			);
		rightShoulderTracker = TrackerUtils
			.getNonInternalTrackerForBodyPosition(
				trackers,
				TrackerPosition.RIGHT_SHOULDER
			);

		// Check for specific conditions and store them in booleans.
		hasSpineTracker = chestTracker != null || waistTracker != null || hipTracker != null;
		hasKneeTrackers = leftUpperLegTracker != null && rightUpperLegTracker != null;
		hasLeftLegTracker = leftUpperLegTracker != null
			|| leftLowerLegTracker != null
			|| leftFootTracker != null;
		hasRightLegTracker = rightUpperLegTracker != null
			|| rightLowerLegTracker != null
			|| rightFootTracker != null;
		hasLeftArmTracker = leftLowerArmTracker != null || leftUpperArmTracker != null;
		hasRightArmTracker = rightLowerArmTracker != null || rightUpperArmTracker != null;

		// Rebuilds the arm skeleton nodes attachments
		assembleSkeletonArms(true);

		// Refresh node offsets for arms
		computeDependentArmOffsets();

		// Rebuild the bone list
		resetBones();
	}

	protected void setComputedTracker(Tracker tracker) {
		switch (tracker.getTrackerPosition()) {
			case HEAD -> computedHeadTracker = tracker;
			case CHEST -> computedChestTracker = tracker;
			case HIP -> computedHipTracker = tracker;
			case LEFT_UPPER_LEG -> computedLeftKneeTracker = tracker;
			case LEFT_FOOT -> computedLeftFootTracker = tracker;
			case RIGHT_UPPER_LEG -> computedRightKneeTracker = tracker;
			case RIGHT_FOOT -> computedRightFootTracker = tracker;
			case LEFT_UPPER_ARM -> computedLeftElbowTracker = tracker;
			case RIGHT_UPPER_ARM -> computedRightElbowTracker = tracker;
			case LEFT_HAND -> computedLeftHandTracker = tracker;
			case RIGHT_HAND -> computedRightHandTracker = tracker;
			default -> {}
		}
	}

	protected void setComputedTrackers(List<Tracker> trackers) {
		for (Tracker t : trackers) {
			setComputedTracker(t);
		}
	}
	// #endregion
	// #endregion

	// #region Get trackers
	public Tracker getComputedTracker(TrackerRole trackerRole) {
		return switch (trackerRole) {
			case HEAD -> computedHeadTracker;
			case CHEST -> computedChestTracker;
			case WAIST -> computedHipTracker;
			case LEFT_KNEE -> computedLeftKneeTracker;
			case LEFT_FOOT -> computedLeftFootTracker;
			case RIGHT_KNEE -> computedRightKneeTracker;
			case RIGHT_FOOT -> computedRightFootTracker;
			case LEFT_ELBOW -> computedLeftElbowTracker;
			case RIGHT_ELBOW -> computedRightElbowTracker;
			case LEFT_HAND -> computedLeftHandTracker;
			case RIGHT_HAND -> computedRightHandTracker;
			default -> null;
		};
	}

	// #region Processing
	// Updates the pose from tracker positions
	@VRServerThread
	public void updatePose() {
		tapDetectionManager.update();
		updateLocalTransforms();
		updateRootTrackers();
		updateComputedTrackers();
		// Don't run leg tweaks if the tracking is paused
		if (!pauseTracking)
			tweakLegPos();
		viveEmulation.update();
	}
	// #endregion

	@ThreadSafe
	protected void updateRootTrackers() {
		hmdNode.update();
		if (isTrackingLeftArmFromController()) {
			leftHandNode.update();
		}
		if (isTrackingRightArmFromController()) {
			rightHandNode.update();
		}
	}

	// correct any clipping that is happening to the feet trackers
	private void tweakLegPos() {
		// correct the foot positions
		legTweaks.tweakLegs();
	}

	// #region Update the node transforms from the trackers
	protected void updateLocalTransforms() {
		// HMD, head and neck
		Quaternion headRot = Quaternion.Companion.getIDENTITY();
		if (headTracker != null) {
			if (headTracker.getHasPosition())
				hmdNode.getLocalTransform().setTranslation(headTracker.getPosition());

			headRot = headTracker.getRotation();
			hmdNode.getLocalTransform().setRotation(headRot);
			trackerHeadNode.getLocalTransform().setRotation(headRot);

			if (neckTracker != null)
				headRot = neckTracker.getRotation();
			headNode.getLocalTransform().setRotation(headRot);
		} else {
			hmdNode.getLocalTransform().setTranslation(Vector3.Companion.getNULL());

			if (neckTracker != null) {
				headRot = neckTracker.getRotation();
			} else if (hasSpineTracker) {
				headRot = TrackerUtils
					.getFirstAvailableTracker(chestTracker, waistTracker, hipTracker)
					.getRotation();
			}

			hmdNode.getLocalTransform().setRotation(headRot);
			trackerHeadNode.getLocalTransform().setRotation(headRot);
			headNode.getLocalTransform().setRotation(headRot);
		}

		// Only update the head and neck as they are relevant to the position
		// of the computed trackers for VR, the rest should be frozen
		if (pauseTracking)
			return;

		// Spine
		if (hasSpineTracker) {
			Quaternion torsoRot = TrackerUtils
				.getFirstAvailableTracker(chestTracker, waistTracker, hipTracker)
				.getRotation();
			neckNode.getLocalTransform().setRotation(torsoRot);
			trackerChestNode.getLocalTransform().setRotation(torsoRot);

			torsoRot = TrackerUtils
				.getFirstAvailableTracker(waistTracker, chestTracker, hipTracker)
				.getRotation();
			chestNode.getLocalTransform().setRotation(torsoRot);

			torsoRot = TrackerUtils
				.getFirstAvailableTracker(hipTracker, waistTracker, chestTracker)
				.getRotation();
			waistNode.getLocalTransform().setRotation(torsoRot);
			hipNode.getLocalTransform().setRotation(torsoRot);
			trackerHipNode.getLocalTransform().setRotation(torsoRot);
		} else if (headTracker != null) {
			// Align with last tracker's yaw (HMD or neck)
			Quaternion yawQuat = headRot.project(Vector3.Companion.getPOS_Y()).unit();

			neckNode.getLocalTransform().setRotation(yawQuat);
			trackerChestNode.getLocalTransform().setRotation(yawQuat);
			chestNode.getLocalTransform().setRotation(yawQuat);
			waistNode.getLocalTransform().setRotation(yawQuat);
			hipNode.getLocalTransform().setRotation(yawQuat);
			trackerHipNode.getLocalTransform().setRotation(yawQuat);
		}

		// Left Leg
		// Get rotation
		Quaternion leftUpperLeg;
		if (leftUpperLegTracker != null) {
			leftUpperLeg = leftUpperLegTracker.getRotation();
		} else {
			// Align with the hip's yaw
			Quaternion hip = hipNode.getLocalTransform().getRotation();
			leftUpperLeg = hip.project(Vector3.Companion.getPOS_Y()).unit();
		}
		Quaternion leftLowerLeg;
		if (leftLowerLegTracker != null) {
			leftLowerLeg = leftLowerLegTracker.getRotation();
		} else {
			// Align with the upper leg's yaw
			leftLowerLeg = leftUpperLeg.project(Vector3.Companion.getPOS_Y()).unit();
		}

		leftHipNode.getLocalTransform().setRotation(leftUpperLeg);
		trackerLeftKneeNode.getLocalTransform().setRotation(leftUpperLeg);
		leftKneeNode.getLocalTransform().setRotation(leftLowerLeg);


		if (leftFootTracker != null) {
			leftLowerLeg = leftFootTracker.getRotation();
		}

		leftAnkleNode.getLocalTransform().setRotation(leftLowerLeg);
		leftFootNode.getLocalTransform().setRotation(leftLowerLeg);
		trackerLeftFootNode.getLocalTransform().setRotation(leftLowerLeg);

		// Extended left knee
		if (leftUpperLegTracker != null && leftLowerLegTracker != null && extendedKneeModel) {
			// Averages the knee's rotation with the local ankle's
			// pitch and roll and apply to the tracker node.
			Quaternion leftHipRot = leftHipNode.getLocalTransform().getRotation();
			Quaternion leftKneeRot = leftKneeNode.getLocalTransform().getRotation();

			Quaternion extendedRot = extendedKneeYawRoll(leftHipRot, leftKneeRot);

			trackerLeftKneeNode
				.getLocalTransform()
				.setRotation(leftHipRot.interpR(extendedRot, kneeTrackerAnkleAveraging));
		}

		// Right Leg
		// Get rotations
		Quaternion rightUpperLeg;
		if (rightUpperLegTracker != null) {
			rightUpperLeg = rightUpperLegTracker.getRotation();
		} else {
			// Align with the hip's yaw
			Quaternion hip = hipNode.getLocalTransform().getRotation();
			rightUpperLeg = hip.project(Vector3.Companion.getPOS_Y()).unit();
		}

		Quaternion rightLowerLeg;
		if (rightLowerLegTracker != null) {
			rightLowerLeg = rightLowerLegTracker.getRotation();
		} else {
			// Align with the upper leg's yaw
			rightLowerLeg = rightUpperLeg.project(Vector3.Companion.getPOS_Y()).unit();
		}

		rightHipNode.getLocalTransform().setRotation(rightUpperLeg);
		trackerRightKneeNode.getLocalTransform().setRotation(rightUpperLeg);
		rightKneeNode.getLocalTransform().setRotation(rightLowerLeg);

		if (rightFootTracker != null)
			rightLowerLeg = rightFootTracker.getRotation();

		rightAnkleNode.getLocalTransform().setRotation(rightLowerLeg);
		rightFootNode.getLocalTransform().setRotation(rightLowerLeg);
		trackerRightFootNode.getLocalTransform().setRotation(rightLowerLeg);

		// Extended right knee
		if (rightUpperLegTracker != null && rightLowerLegTracker != null && extendedKneeModel) {
			// Averages the knee's rotation with the local ankle's
			// pitch and roll and apply to the tracker node.
			Quaternion rightHipRot = rightHipNode.getLocalTransform().getRotation();
			Quaternion rightKneeRot = rightKneeNode.getLocalTransform().getRotation();

			Quaternion extendedRot = extendedKneeYawRoll(rightHipRot, rightKneeRot);

			trackerRightKneeNode
				.getLocalTransform()
				.setRotation(rightHipRot.interpR(extendedRot, kneeTrackerAnkleAveraging));
		}

		// Extended spine
		if (extendedSpineModel && hasSpineTracker) {
			// Tries to guess missing lower spine trackers by interpolating
			// rotations
			if (waistTracker == null) {
				if (chestTracker != null && hipTracker != null) {
					// Calculates waist from chest + hip
					var hipRot = hipTracker.getRotation();
					var chestRot = chestTracker.getRotation();

					// Get the rotation relative to where we expect the hip to
					// be
					if (chestRot.times(FORWARD_QUATERNION).dot(hipRot) < 0.0f) {
						hipRot = hipRot.unaryMinus();
					}

					// Interpolate between the chest and the hip
					chestRot = chestRot.interpQ(hipRot, waistFromChestHipAveraging);

					chestNode.getLocalTransform().setRotation(chestRot);
				} else if (chestTracker != null && hasKneeTrackers) {
					// Calculates waist from chest + legs
					var leftHipRot = leftHipNode.getLocalTransform().getRotation();
					var rightHipRot = rightHipNode.getLocalTransform().getRotation();
					var chestRot = chestTracker.getRotation();

					// Get the rotation relative to where we expect the
					// upper legs to be
					var expectedUpperLegsRot = chestRot.times(FORWARD_QUATERNION);
					if (expectedUpperLegsRot.dot(leftHipRot) < 0.0f) {
						leftHipRot = leftHipRot.unaryMinus();
					}
					if (expectedUpperLegsRot.dot(rightHipRot) < 0.0f) {
						rightHipRot = rightHipRot.unaryMinus();
					}

					// Interpolate between the pelvis, averaged from the legs,
					// and the chest
					chestRot = chestRot
						.interpQ(leftHipRot.lerpQ(rightHipRot, 0.5f), waistFromChestLegsAveraging)
						.unit();

					chestNode.getLocalTransform().setRotation(chestRot);
				}
			}
			if (hipTracker == null && hasKneeTrackers) {
				if (waistTracker != null) {
					// Calculates hip from waist + legs
					var leftHipRot = leftHipNode.getLocalTransform().getRotation();
					var rightHipRot = rightHipNode.getLocalTransform().getRotation();
					var waistRot = waistTracker.getRotation();

					// Get the rotation relative to where we expect the
					// upper legs to be
					var expectedUpperLegsRot = waistRot.times(FORWARD_QUATERNION);
					if (expectedUpperLegsRot.dot(leftHipRot) < 0.0f) {
						leftHipRot = leftHipRot.unaryMinus();
					}
					if (expectedUpperLegsRot.dot(rightHipRot) < 0.0f) {
						rightHipRot = rightHipRot.unaryMinus();
					}

					// Interpolate between the pelvis, averaged from the legs,
					// and the chest
					waistRot = waistRot
						.interpQ(leftHipRot.lerpQ(rightHipRot, 0.5f), hipFromWaistLegsAveraging)
						.unit();

					waistNode.getLocalTransform().setRotation(waistRot);
					hipNode.getLocalTransform().setRotation(waistRot);
					trackerHipNode.getLocalTransform().setRotation(waistRot);
				} else if (chestTracker != null) {
					// Calculates hip from chest + legs
					var leftHipRot = leftHipNode.getLocalTransform().getRotation();
					var rightHipRot = rightHipNode.getLocalTransform().getRotation();
					var chestRot = chestTracker.getRotation();

					// Get the rotation relative to where we expect the
					// upper legs to be
					var expectedUpperLegsRot = chestRot.times(FORWARD_QUATERNION);
					if (expectedUpperLegsRot.dot(leftHipRot) < 0.0f) {
						leftHipRot = leftHipRot.unaryMinus();
					}
					if (expectedUpperLegsRot.dot(rightHipRot) < 0.0f) {
						rightHipRot = rightHipRot.unaryMinus();
					}

					// Interpolate between the pelvis, averaged from the legs,
					// and the chest
					chestRot = chestRot
						.interpQ(leftHipRot.lerpQ(rightHipRot, 0.5f), hipFromChestLegsAveraging)
						.unit();

					waistNode.getLocalTransform().setRotation(chestRot);
					hipNode.getLocalTransform().setRotation(chestRot);
					trackerHipNode.getLocalTransform().setRotation(chestRot);
				}
			}
		}

		// Extended pelvis
		if (extendedPelvisModel && hasKneeTrackers && hipTracker == null) {
			var leftHipRot = leftHipNode.getLocalTransform().getRotation();
			var rightHipRot = rightHipNode.getLocalTransform().getRotation();
			var hipRot = hipNode.getLocalTransform().getRotation();

			var extendedPelvisRot = extendedPelvisYawRoll(leftHipRot, rightHipRot, hipRot);

			var slerp = hipRot.interpR(extendedPelvisRot, hipLegsAveraging);
			hipNode.getLocalTransform().setRotation(slerp);
			trackerHipNode.getLocalTransform().setRotation(slerp);
		}

		// Left arm
		if (isTrackingLeftArmFromController()) { // From controller
			leftHandNode.getLocalTransform().setTranslation(leftHandTracker.getPosition());
			leftHandNode.getLocalTransform().setRotation(leftHandTracker.getRotation());

			Tracker lowerArm = TrackerUtils
				.getFirstAvailableTracker(leftLowerArmTracker, leftUpperArmTracker);
			if (lowerArm != null) {
				leftWristNode.getLocalTransform().setRotation(lowerArm.getRotation());

				var leftArmRot = TrackerUtils
					.getFirstAvailableTracker(leftUpperArmTracker, leftLowerArmTracker)
					.getRotation();
				leftElbowNode.getLocalTransform().setRotation(leftArmRot);
				trackerLeftElbowNode.getLocalTransform().setRotation(leftArmRot);
			}
		} else { // From HMD
			Quaternion leftShoulderRot;
			if (leftShoulderTracker != null)
				leftShoulderRot = leftShoulderTracker.getRotation();
			else
				leftShoulderRot = neckNode.getLocalTransform().getRotation();
			leftShoulderHeadNode.getLocalTransform().setRotation(leftShoulderRot);

			Quaternion leftArmRot;
			if (leftUpperArmTracker != null || leftLowerArmTracker != null) {
				leftArmRot = TrackerUtils
					.getFirstAvailableTracker(leftUpperArmTracker, leftLowerArmTracker)
					.getRotation();
				leftShoulderTailNode.getLocalTransform().setRotation(leftArmRot);
				trackerLeftElbowNode.getLocalTransform().setRotation(leftArmRot);

				leftArmRot = TrackerUtils
					.getFirstAvailableTracker(leftLowerArmTracker, leftUpperArmTracker)
					.getRotation();
				leftElbowNode.getLocalTransform().setRotation(leftArmRot);
			} else {
				leftArmRot = neckNode.getLocalTransform().getRotation();
				leftShoulderTailNode.getLocalTransform().setRotation(leftArmRot);
				trackerLeftElbowNode.getLocalTransform().setRotation(leftArmRot);
				leftElbowNode.getLocalTransform().setRotation(leftArmRot);
			}

			if (leftHandTracker != null)
				leftArmRot = leftHandTracker.getRotation();

			leftWristNode.getLocalTransform().setRotation(leftArmRot);
			leftHandNode.getLocalTransform().setRotation(leftArmRot);
			trackerLeftHandNode.getLocalTransform().setRotation(leftArmRot);
		}

		// Right arm
		if (isTrackingRightArmFromController()) { // From controller
			rightHandNode.getLocalTransform().setTranslation(rightHandTracker.getPosition());
			rightHandNode.getLocalTransform().setRotation(rightHandTracker.getRotation());

			Tracker lowerArm = TrackerUtils
				.getFirstAvailableTracker(rightLowerArmTracker, rightUpperArmTracker);
			if (lowerArm != null) {
				rightWristNode.getLocalTransform().setRotation(lowerArm.getRotation());

				var rightArmRot = TrackerUtils
					.getFirstAvailableTracker(rightUpperArmTracker, rightLowerArmTracker)
					.getRotation();
				rightElbowNode.getLocalTransform().setRotation(rightArmRot);
				trackerRightElbowNode.getLocalTransform().setRotation(rightArmRot);
			}
		} else { // From HMD
			Quaternion rightShoulderRot;
			if (rightShoulderTracker != null)
				rightShoulderRot = rightShoulderTracker.getRotation();
			else
				rightShoulderRot = neckNode.getLocalTransform().getRotation();
			rightShoulderHeadNode.getLocalTransform().setRotation(rightShoulderRot);

			Quaternion rightArmRot;
			if (rightUpperArmTracker != null || rightLowerArmTracker != null) {
				rightArmRot = TrackerUtils
					.getFirstAvailableTracker(rightUpperArmTracker, rightLowerArmTracker)
					.getRotation();
				rightShoulderTailNode.getLocalTransform().setRotation(rightArmRot);
				trackerRightElbowNode.getLocalTransform().setRotation(rightArmRot);

				rightArmRot = TrackerUtils
					.getFirstAvailableTracker(rightLowerArmTracker, rightUpperArmTracker)
					.getRotation();
				rightElbowNode.getLocalTransform().setRotation(rightArmRot);
			} else {
				rightArmRot = neckNode.getLocalTransform().getRotation();
				rightShoulderTailNode.getLocalTransform().setRotation(rightArmRot);
				trackerRightElbowNode.getLocalTransform().setRotation(rightArmRot);
				rightElbowNode.getLocalTransform().setRotation(rightArmRot);
			}

			if (rightHandTracker != null)
				rightArmRot = rightHandTracker.getRotation();

			rightWristNode.getLocalTransform().setRotation(rightArmRot);
			rightHandNode.getLocalTransform().setRotation(rightArmRot);
			trackerRightHandNode.getLocalTransform().setRotation(rightArmRot);
		}
	}

	/**
	 * Rotates the first Quaternion to match its yaw and roll to the rotation of
	 * the second Quaternion
	 *
	 * @param knee the first Quaternion
	 * @param ankle the second Quaternion
	 * @return the rotated Quaternion
	 */
	private Quaternion extendedKneeYawRoll(Quaternion knee, Quaternion ankle) {
		// R = InverseKnee * Ankle
		// C = Quaternion(R.w, -R.x, 0, 0)
		// Knee = Knee * R * C
		// normalize(Knee)
		Quaternion r = knee.inv().times(ankle);
		Quaternion c = new Quaternion(r.getW(), -r.getX(), 0, 0);
		return knee.times(r).times(c).unit();
	}

	/**
	 * Rotates the first Quaternion to match its yaw and roll to the rotation of
	 * the average of the second and third quaternions.
	 *
	 * @param leftKnee the first Quaternion
	 * @param rightKnee the second Quaternion
	 * @param hip the third Quaternion
	 * @return the rotated Quaternion
	 */
	private Quaternion extendedPelvisYawRoll(
		Quaternion leftKnee,
		Quaternion rightKnee,
		Quaternion hip
	) {
		// Get the knees' rotation relative to where we expect them to be.
		// The angle between your knees and hip can be over 180 degrees...
		var kneeRot = hip.times(FORWARD_QUATERNION);
		if (kneeRot.dot(leftKnee) < 0.0f) {
			leftKnee = leftKnee.unaryMinus();
		}
		if (kneeRot.dot(rightKnee) < 0.0f) {
			rightKnee = rightKnee.unaryMinus();
		}

		// R = InverseHip * (LeftLeft + RightLeg)
		// C = Quaternion(R.w, -R.x, 0, 0)
		// Pelvis = Hip * R * C
		// normalize(Pelvis)
		var r = hip.inv().times(leftKnee.plus(rightKnee));
		var c = new Quaternion(r.getW(), -r.getX(), 0, 0);
		return hip.times(r).times(c).unit();
	}

	// #region Update the output trackers
	protected void updateComputedTrackers() {
		computedHeadTracker
			.setPosition(trackerHeadNode.getWorldTransform().getTranslation());
		computedHeadTracker
			.setRotation(trackerHeadNode.getWorldTransform().getRotation());
		computedHeadTracker.dataTick();

		computedChestTracker
			.setPosition(trackerChestNode.getWorldTransform().getTranslation());
		computedChestTracker
			.setRotation(trackerChestNode.getWorldTransform().getRotation());
		computedChestTracker.dataTick();

		computedHipTracker
			.setPosition(trackerHipNode.getWorldTransform().getTranslation());
		computedHipTracker
			.setRotation(trackerHipNode.getWorldTransform().getRotation());
		computedHipTracker.dataTick();

		computedLeftKneeTracker
			.setPosition(trackerLeftKneeNode.getWorldTransform().getTranslation());
		computedLeftKneeTracker.setRotation(trackerLeftKneeNode.getWorldTransform().getRotation());
		computedLeftKneeTracker.dataTick();

		computedLeftFootTracker
			.setPosition(trackerLeftFootNode.getWorldTransform().getTranslation());
		computedLeftFootTracker
			.setRotation(trackerLeftFootNode.getWorldTransform().getRotation());
		computedLeftFootTracker.dataTick();

		computedRightKneeTracker
			.setPosition(trackerRightKneeNode.getWorldTransform().getTranslation());
		computedRightKneeTracker
			.setRotation(trackerRightKneeNode.getWorldTransform().getRotation());
		computedRightKneeTracker.dataTick();

		computedRightFootTracker
			.setPosition(trackerRightFootNode.getWorldTransform().getTranslation());
		computedRightFootTracker
			.setRotation(trackerRightFootNode.getWorldTransform().getRotation());
		computedRightFootTracker.dataTick();

		computedLeftElbowTracker
			.setPosition(trackerLeftElbowNode.getWorldTransform().getTranslation());
		computedLeftElbowTracker
			.setRotation(trackerLeftElbowNode.getWorldTransform().getRotation());
		computedLeftElbowTracker.dataTick();

		computedRightElbowTracker
			.setPosition(trackerRightElbowNode.getWorldTransform().getTranslation());
		computedRightElbowTracker
			.setRotation(trackerRightElbowNode.getWorldTransform().getRotation());
		computedRightElbowTracker.dataTick();

		computedLeftHandTracker
			.setPosition(trackerLeftHandNode.getWorldTransform().getTranslation());
		computedLeftHandTracker
			.setRotation(trackerLeftHandNode.getWorldTransform().getRotation());
		computedLeftHandTracker.dataTick();

		computedRightHandTracker
			.setPosition(trackerRightHandNode.getWorldTransform().getTranslation());
		computedRightHandTracker
			.setRotation(trackerRightHandNode.getWorldTransform().getRotation());
		computedRightHandTracker.dataTick();
	}
	// #endregion
	// #endregion

	// #region Skeleton Config
	public void updateToggleState(SkeletonConfigToggles configToggle, boolean newValue) {
		if (configToggle == null) {
			return;
		}

		// Cache the values of these configs
		switch (configToggle) {
			case EXTENDED_SPINE_MODEL -> extendedSpineModel = newValue;
			case EXTENDED_PELVIS_MODEL -> extendedPelvisModel = newValue;
			case EXTENDED_KNEE_MODEL -> extendedKneeModel = newValue;
			case FORCE_ARMS_FROM_HMD -> {
				forceArmsFromHMD = newValue;
				// Rebuilds the arm skeleton nodes attachments
				assembleSkeletonArms(true);
				// Refresh node offsets for arms
				computeDependentArmOffsets();
			}
			case SKATING_CORRECTION -> legTweaks.setSkatingReductionEnabled(newValue);
			case FLOOR_CLIP -> legTweaks.setFloorclipEnabled(newValue);
			case VIVE_EMULATION -> viveEmulation.setEnabled(newValue);
			case TOE_SNAP -> legTweaks.setToeSnapEnabled(newValue);
			case FOOT_PLANT -> legTweaks.setFootPlantEnabled(newValue);
		}
	}

	public void updateValueState(SkeletonConfigValues configValue, float newValue) {
		if (configValue == null) {
			return;
		}

		// Cache the values of these configs
		switch (configValue) {
			case WAIST_FROM_CHEST_HIP_AVERAGING -> waistFromChestHipAveraging = newValue;
			case WAIST_FROM_CHEST_LEGS_AVERAGING -> waistFromChestLegsAveraging = newValue;
			case HIP_FROM_CHEST_LEGS_AVERAGING -> hipFromChestLegsAveraging = newValue;
			case HIP_FROM_WAIST_LEGS_AVERAGING -> hipFromWaistLegsAveraging = newValue;
			case HIP_LEGS_AVERAGING -> hipLegsAveraging = newValue;
			case KNEE_TRACKER_ANKLE_AVERAGING -> kneeTrackerAnkleAveraging = newValue;
		}
	}

	public void updateNodeOffset(BoneType nodeOffset, Vector3 offset) {
		if (nodeOffset == null) {
			return;
		}

		switch (nodeOffset) {
			case HEAD -> {
				if (headTracker != null && headTracker.getHasPosition()) {
					headNode.getLocalTransform().setTranslation(offset);
				} else {
					headNode.getLocalTransform().setTranslation(Vector3.Companion.getNULL());
				}
			}
			case NECK -> neckNode.getLocalTransform().setTranslation(offset);
			case CHEST -> chestNode.getLocalTransform().setTranslation(offset);
			case CHEST_TRACKER -> trackerChestNode.getLocalTransform().setTranslation(offset);
			case WAIST -> waistNode.getLocalTransform().setTranslation(offset);
			case HIP -> hipNode.getLocalTransform().setTranslation(offset);
			case HIP_TRACKER -> trackerHipNode.getLocalTransform().setTranslation(offset);
			case LEFT_HIP -> leftHipNode.getLocalTransform().setTranslation(offset);
			case RIGHT_HIP -> rightHipNode.getLocalTransform().setTranslation(offset);
			case LEFT_UPPER_LEG -> leftKneeNode.getLocalTransform().setTranslation(offset);
			case RIGHT_UPPER_LEG -> rightKneeNode.getLocalTransform().setTranslation(offset);
			case LEFT_KNEE_TRACKER -> trackerLeftKneeNode
				.getLocalTransform()
				.setTranslation(offset);
			case RIGHT_KNEE_TRACKER -> trackerRightKneeNode
				.getLocalTransform()
				.setTranslation(offset);
			case LEFT_LOWER_LEG -> leftAnkleNode.getLocalTransform().setTranslation(offset);
			case RIGHT_LOWER_LEG -> rightAnkleNode.getLocalTransform().setTranslation(offset);
			case LEFT_FOOT -> leftFootNode.getLocalTransform().setTranslation(offset);
			case RIGHT_FOOT -> rightFootNode.getLocalTransform().setTranslation(offset);
			case LEFT_FOOT_TRACKER -> trackerLeftFootNode
				.getLocalTransform()
				.setTranslation(offset);
			case RIGHT_FOOT_TRACKER -> trackerRightFootNode
				.getLocalTransform()
				.setTranslation(offset);
			case LEFT_SHOULDER -> leftShoulderTailNode
				.getLocalTransform()
				.setTranslation(offset);
			case RIGHT_SHOULDER -> rightShoulderTailNode
				.getLocalTransform()
				.setTranslation(offset);
			case LEFT_UPPER_ARM -> {
				if (!isTrackingLeftArmFromController()) {
					leftElbowNode.getLocalTransform().setTranslation(offset);
				}
			}
			case RIGHT_UPPER_ARM -> {
				if (!isTrackingRightArmFromController()) {
					rightElbowNode.getLocalTransform().setTranslation(offset);
				}
			}
			case LEFT_LOWER_ARM -> {
				if (isTrackingLeftArmFromController()) {
					leftElbowNode.getLocalTransform().setTranslation(offset);
				} else {
					leftWristNode.getLocalTransform().setTranslation(offset.unaryMinus());
				}
			}
			case RIGHT_LOWER_ARM -> {
				if (isTrackingRightArmFromController()) {
					rightElbowNode.getLocalTransform().setTranslation(offset);
				} else {
					rightWristNode.getLocalTransform().setTranslation(offset.unaryMinus());
				}
			}
			case LEFT_ELBOW_TRACKER -> trackerLeftElbowNode
				.getLocalTransform()
				.setTranslation(offset);
			case RIGHT_ELBOW_TRACKER -> trackerRightElbowNode
				.getLocalTransform()
				.setTranslation(offset);
			case LEFT_HAND -> {
				if (isTrackingLeftArmFromController()) {
					leftWristNode.getLocalTransform().setTranslation(offset.unaryMinus());
				} else {
					leftHandNode.getLocalTransform().setTranslation(offset);
				}
			}
			case RIGHT_HAND -> {
				if (isTrackingRightArmFromController()) {
					rightWristNode.getLocalTransform().setTranslation(offset.unaryMinus());
				} else {
					rightHandNode.getLocalTransform().setTranslation(offset);
				}
			}
		}

		for (BoneInfo bone : allBoneInfo) {
			bone.updateLength();
		}
	}

	private void computeDependentArmOffsets() {
		humanPoseManager.computeNodeOffset(BoneType.LEFT_UPPER_ARM);
		humanPoseManager.computeNodeOffset(BoneType.RIGHT_UPPER_ARM);
		humanPoseManager.computeNodeOffset(BoneType.LEFT_LOWER_ARM);
		humanPoseManager.computeNodeOffset(BoneType.RIGHT_LOWER_ARM);
	}

	public TransformNode getTailNodeOfBone(BoneType bone) {
		if (bone == null) {
			return null;
		}

		return switch (bone) {
			case HMD, HEAD -> headNode;
			case HEAD_TRACKER -> trackerHeadNode;
			case NECK -> neckNode;
			case CHEST -> chestNode;
			case CHEST_TRACKER -> trackerChestNode;
			case WAIST -> waistNode;
			case HIP -> hipNode;
			case HIP_TRACKER -> trackerHipNode;
			case LEFT_HIP -> leftHipNode;
			case RIGHT_HIP -> rightHipNode;
			case LEFT_UPPER_LEG -> leftKneeNode;
			case RIGHT_UPPER_LEG -> rightKneeNode;
			case RIGHT_KNEE_TRACKER -> trackerRightKneeNode;
			case LEFT_KNEE_TRACKER -> trackerLeftKneeNode;
			case LEFT_LOWER_LEG -> leftAnkleNode;
			case RIGHT_LOWER_LEG -> rightAnkleNode;
			case LEFT_FOOT -> leftFootNode;
			case RIGHT_FOOT -> rightFootNode;
			case LEFT_FOOT_TRACKER -> trackerLeftFootNode;
			case RIGHT_FOOT_TRACKER -> trackerRightFootNode;
			case LEFT_SHOULDER -> leftShoulderTailNode;
			case RIGHT_SHOULDER -> rightShoulderTailNode;
			case LEFT_UPPER_ARM -> leftElbowNode;
			case RIGHT_UPPER_ARM -> rightElbowNode;
			case LEFT_ELBOW_TRACKER -> trackerLeftElbowNode;
			case RIGHT_ELBOW_TRACKER -> trackerRightElbowNode;
			case LEFT_LOWER_ARM -> isTrackingLeftArmFromController()
				? leftElbowNode
				: leftWristNode;
			case RIGHT_LOWER_ARM -> isTrackingRightArmFromController()
				? rightElbowNode
				: rightWristNode;
			case LEFT_HAND -> isTrackingLeftArmFromController()
				? leftWristNode
				: leftHandNode;
			case RIGHT_HAND -> isTrackingRightArmFromController()
				? rightWristNode
				: rightHandNode;
			case LEFT_HAND_TRACKER -> trackerLeftHandNode;
			case RIGHT_HAND_TRACKER -> trackerRightHandNode;
		};
	}

	public BoneInfo getBoneInfoForBoneType(BoneType boneType) {
		for (BoneInfo bone : allBoneInfo) {
			if (bone.boneType == boneType)
				return bone;
		}

		return null;
	}
	// #endregion

	public TransformNode getRootNode() {
		return hmdNode;
	}

	protected TransformNode[] getAllNodes() {
		return new TransformNode[] {
			hmdNode,
			headNode,
			trackerHeadNode,
			neckNode,
			chestNode,
			trackerChestNode,
			waistNode,
			hipNode,
			trackerHipNode,
			leftHipNode,
			leftKneeNode,
			trackerLeftKneeNode,
			leftAnkleNode,
			leftFootNode,
			trackerLeftFootNode,
			rightHipNode,
			rightKneeNode,
			trackerRightKneeNode,
			rightAnkleNode,
			rightFootNode,
			trackerRightFootNode,
			leftShoulderHeadNode,
			rightShoulderHeadNode,
			leftShoulderTailNode,
			rightShoulderTailNode,
			leftElbowNode,
			rightElbowNode,
			trackerLeftElbowNode,
			trackerRightElbowNode,
			leftWristNode,
			rightWristNode,
			leftHandNode,
			rightHandNode,
			trackerLeftHandNode,
			trackerRightHandNode,
		};
	}

	protected TransformNode[] getArmNodes() {
		return new TransformNode[] {
			leftShoulderHeadNode,
			rightShoulderHeadNode,
			leftShoulderTailNode,
			rightShoulderTailNode,
			leftElbowNode,
			rightElbowNode,
			trackerLeftElbowNode,
			trackerRightElbowNode,
			leftWristNode,
			rightWristNode,
			leftHandNode,
			rightHandNode,
			trackerLeftHandNode,
			trackerRightHandNode,
		};
	}

	public float getHmdHeight() {
		if (headTracker != null && headTracker.getHasPosition())
			return headTracker.getPosition().getY();
		return 0f;
	}

	/**
	 * Runs checks to know if we should (and are) performing the tracking of the
	 * left arm from the controller.
	 *
	 * @return a bool telling us if we are tracking the left arm from the
	 * controller or not.
	 */
	public boolean isTrackingLeftArmFromController() {
		return leftHandTracker != null && leftHandTracker.getHasPosition() && !forceArmsFromHMD;
	}

	/**
	 * Runs checks to know if we should (and are) performing the tracking of the
	 * right arm from the controller.
	 *
	 * @return a bool telling us if we are tracking the right arm from the
	 * controller or not.
	 */
	public boolean isTrackingRightArmFromController() {
		return rightHandTracker != null && rightHandTracker.getHasPosition() && !forceArmsFromHMD;
	}

	public List<Tracker> getLocalTrackers() {
		return List
			.of(
				this.neckTracker,
				this.chestTracker,
				this.waistTracker,
				this.hipTracker,
				this.leftUpperLegTracker,
				this.leftLowerLegTracker,
				this.leftFootTracker,
				this.rightUpperLegTracker,
				this.rightLowerLegTracker,
				this.rightFootTracker,
				this.leftLowerArmTracker,
				this.rightLowerArmTracker,
				this.leftUpperArmTracker,
				this.rightUpperArmTracker,
				this.leftHandTracker,
				this.rightHandTracker,
				this.leftShoulderTracker,
				this.rightShoulderTracker
			);
	}

	public void resetTrackersFull(String resetSourceName) {
		List<Tracker> trackersToReset = humanPoseManager.getTrackersToReset();

		// Resets all axis of the trackers with the HMD as reference.
		Quaternion referenceRotation = Quaternion.Companion.getIDENTITY();
		if (headTracker != null) {
			if (headTracker.getNeedsReset())
				headTracker.getResetsHandler().resetFull(referenceRotation);
			else
				referenceRotation = headTracker.getRotation();
		}

		for (Tracker tracker : trackersToReset) {
			if (tracker != null && tracker.getNeedsReset()) {
				tracker.getResetsHandler().resetFull(referenceRotation);
			}
		}

		// tell the clip corrector to reset its floor level on the next update
		// of the computed trackers
		this.legTweaks.resetFloorLevel();
		this.legTweaks.resetBuffer();

		LogManager.info("[HumanSkeleton] Reset: full (%s)".formatted(resetSourceName));
	}

	@VRServerThread
	public void resetTrackersYaw(String resetSourceName) {
		List<Tracker> trackersToReset = humanPoseManager.getTrackersToReset();

		// Resets the yaw of the trackers with the head as reference.
		Quaternion referenceRotation = Quaternion.Companion.getIDENTITY();
		if (headTracker != null) {
			if (headTracker.getNeedsReset())
				headTracker.getResetsHandler().resetYaw(referenceRotation);
			else
				referenceRotation = headTracker.getRotation();
		}

		for (Tracker tracker : trackersToReset) {
			if (tracker != null && tracker.getNeedsReset()) {
				tracker.getResetsHandler().resetYaw(referenceRotation);
			}
		}
		this.legTweaks.resetBuffer();

		LogManager.info("[HumanSkeleton] Reset: yaw (%s)".formatted(resetSourceName));
	}

	private boolean shouldResetMounting(TrackerPosition position) {
		return position != null
			// TODO: Feet can't currently be reset using this method, maybe
			// they'll need a separate step just for them?
			&& position != TrackerPosition.LEFT_FOOT
			&& position != TrackerPosition.RIGHT_FOOT;
	}

	private boolean shouldResetMounting(Tracker tracker) {
		return shouldResetMounting(tracker.getTrackerPosition());
	}

	private boolean shouldReverseYaw(TrackerPosition position) {
		switch (position) {
			case LEFT_UPPER_LEG:
			case RIGHT_UPPER_LEG:
			case LEFT_LOWER_ARM:
			case LEFT_HAND:
			case RIGHT_LOWER_ARM:
			case RIGHT_HAND:
				return true;
			default:
				return false;
		}
	}

	private boolean shouldReverseYaw(Tracker tracker) {
		return shouldReverseYaw(tracker.getTrackerPosition());
	}

	@VRServerThread
	public void resetTrackersMounting(String resetSourceName) {
		List<Tracker> trackersToReset = humanPoseManager.getTrackersToReset();

		// Resets the mounting rotation of the trackers with the HMD as
		// reference.
		Quaternion referenceRotation = Quaternion.Companion.getIDENTITY();
		if (headTracker != null) {
			if (headTracker.getNeedsMounting())
				headTracker
					.getResetsHandler()
					.resetMounting(shouldReverseYaw(headTracker), referenceRotation);
			else
				referenceRotation = headTracker.getRotation();
		}

		for (Tracker tracker : trackersToReset) {
			if (
				tracker != null
					&& tracker.getNeedsMounting()
					&& shouldResetMounting(tracker)
			) {
				tracker
					.getResetsHandler()
					.resetMounting(shouldReverseYaw(tracker), referenceRotation);
			}
		}
		this.legTweaks.resetBuffer();

		LogManager.info("[HumanSkeleton] Reset: mounting (%s)".formatted(resetSourceName));
	}

	public void updateTapDetectionConfig() {
		tapDetectionManager.updateConfig();
	}

	public void updateLegTweaksConfig() {
		legTweaks.updateConfig();
	}

	// does not save to config
	public void setLegTweaksStateTemp(
		boolean skatingCorrection,
		boolean floorClip,
		boolean toeSnap,
		boolean footPlant
	) {
		this.legTweaks.setSkatingReductionEnabled(skatingCorrection);
		this.legTweaks.setFloorclipEnabled(floorClip);
		this.legTweaks.setToeSnapEnabled(toeSnap);
		this.legTweaks.setFootPlantEnabled(footPlant);
	}

	// resets to config values
	public void clearLegTweaksStateTemp(
		boolean skatingCorrection,
		boolean floorClip,
		boolean toeSnap,
		boolean footPlant
	) {
		// only reset the true values as they are a mask for what to reset
		if (skatingCorrection)
			this.legTweaks
				.setSkatingReductionEnabled(
					humanPoseManager.getToggle(SkeletonConfigToggles.SKATING_CORRECTION)
				);
		if (floorClip)
			this.legTweaks
				.setFloorclipEnabled(humanPoseManager.getToggle(SkeletonConfigToggles.FLOOR_CLIP));
		if (toeSnap)
			this.legTweaks
				.setToeSnapEnabled(humanPoseManager.getToggle(SkeletonConfigToggles.TOE_SNAP));
		if (footPlant)
			this.legTweaks
				.setFootPlantEnabled(humanPoseManager.getToggle(SkeletonConfigToggles.FOOT_PLANT));
	}

	public boolean[] getLegTweaksState() {
		boolean[] state = new boolean[4];
		state[0] = this.legTweaks.getFloorclipEnabled();
		state[1] = this.legTweaks.getSkatingReductionEnabled();
		state[2] = this.legTweaks.getToeSnapEnabled();
		state[3] = this.legTweaks.getFootPlantEnabled();

		return state;
	}

	// master enable/disable of all leg tweaks (for autobone)
	@VRServerThread
	public void setLegTweaksEnabled(boolean value) {
		this.legTweaks.setEnabled(value);
	}

	@VRServerThread
	public void setFloorclipEnabled(boolean value) {
		humanPoseManager.setToggle(SkeletonConfigToggles.FLOOR_CLIP, value);
	}

	@VRServerThread
	public void setSkatingCorrectionEnabled(boolean value) {
		humanPoseManager.setToggle(SkeletonConfigToggles.SKATING_CORRECTION, value);
	}

	public boolean getPauseTracking() {
		return pauseTracking;
	}

	public void setPauseTracking(boolean pauseTracking) {
		this.pauseTracking = pauseTracking;
	}
}
