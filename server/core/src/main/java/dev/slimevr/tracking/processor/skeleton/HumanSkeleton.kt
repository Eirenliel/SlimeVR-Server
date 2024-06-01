package dev.slimevr.tracking.processor.skeleton

import com.jme3.math.FastMath
import dev.slimevr.VRServer
import dev.slimevr.tracking.processor.Bone
import dev.slimevr.tracking.processor.BoneType
import dev.slimevr.tracking.processor.HumanPoseManager
import dev.slimevr.tracking.processor.config.SkeletonConfigToggles
import dev.slimevr.tracking.processor.config.SkeletonConfigValues
import dev.slimevr.tracking.trackers.Tracker
import dev.slimevr.tracking.trackers.TrackerPosition
import dev.slimevr.tracking.trackers.TrackerRole
import dev.slimevr.tracking.trackers.TrackerUtils.getFirstAvailableTracker
import dev.slimevr.tracking.trackers.TrackerUtils.getTrackerForSkeleton
import dev.slimevr.util.ann.VRServerThread
import io.eiren.util.ann.ThreadSafe
import io.eiren.util.collections.FastList
import io.eiren.util.logging.LogManager
import io.github.axisangles.ktmath.EulerAngles
import io.github.axisangles.ktmath.EulerOrder
import io.github.axisangles.ktmath.Quaternion
import io.github.axisangles.ktmath.Quaternion.Companion.I
import io.github.axisangles.ktmath.Quaternion.Companion.IDENTITY
import io.github.axisangles.ktmath.Quaternion.Companion.fromTo
import io.github.axisangles.ktmath.Vector3
import io.github.axisangles.ktmath.Vector3.Companion.NEG_Y
import io.github.axisangles.ktmath.Vector3.Companion.NULL
import io.github.axisangles.ktmath.Vector3.Companion.POS_Y
import java.lang.IllegalArgumentException

class HumanSkeleton(
	val humanPoseManager: HumanPoseManager,
) {
	// Upper body bones
	val headBone = Bone(BoneType.HEAD)
	val neckBone = Bone(BoneType.NECK)
	val upperChestBone = Bone(BoneType.UPPER_CHEST)
	val chestBone = Bone(BoneType.CHEST)
	val waistBone = Bone(BoneType.WAIST)
	val hipBone = Bone(BoneType.HIP)

	// Lower body bones
	val leftHipBone = Bone(BoneType.LEFT_HIP)
	val rightHipBone = Bone(BoneType.RIGHT_HIP)
	val leftUpperLegBone = Bone(BoneType.LEFT_UPPER_LEG)
	val rightUpperLegBone = Bone(BoneType.RIGHT_UPPER_LEG)
	val leftLowerLegBone = Bone(BoneType.LEFT_LOWER_LEG)
	val rightLowerLegBone = Bone(BoneType.RIGHT_LOWER_LEG)
	val leftFootBone = Bone(BoneType.LEFT_FOOT)
	val rightFootBone = Bone(BoneType.RIGHT_FOOT)

	// Arm bones
	val leftShoulderBone = Bone(BoneType.LEFT_SHOULDER)
	val rightShoulderBone = Bone(BoneType.RIGHT_SHOULDER)
	val leftUpperArmBone = Bone(BoneType.LEFT_UPPER_ARM)
	val rightUpperArmBone = Bone(BoneType.RIGHT_UPPER_ARM)
	val leftLowerArmBone = Bone(BoneType.LEFT_LOWER_ARM)
	val rightLowerArmBone = Bone(BoneType.RIGHT_LOWER_ARM)
	val leftHandBone = Bone(BoneType.LEFT_HAND)
	val rightHandBone = Bone(BoneType.RIGHT_HAND)

	// Tracker bones
	val headTrackerBone = Bone(BoneType.HEAD_TRACKER)
	val chestTrackerBone = Bone(BoneType.CHEST_TRACKER)
	val hipTrackerBone = Bone(BoneType.HIP_TRACKER)
	val leftKneeTrackerBone = Bone(BoneType.LEFT_KNEE_TRACKER)
	val rightKneeTrackerBone = Bone(BoneType.RIGHT_KNEE_TRACKER)
	val leftFootTrackerBone = Bone(BoneType.LEFT_FOOT_TRACKER)
	val rightFootTrackerBone = Bone(BoneType.RIGHT_FOOT_TRACKER)
	val leftElbowTrackerBone = Bone(BoneType.LEFT_ELBOW_TRACKER)
	val rightElbowTrackerBone = Bone(BoneType.RIGHT_ELBOW_TRACKER)
	val leftHandTrackerBone = Bone(BoneType.LEFT_HAND_TRACKER)
	val rightHandTrackerBone = Bone(BoneType.RIGHT_HAND_TRACKER)

	// Buffers
	var hasSpineTracker = false
	var hasKneeTrackers = false
	var hasLeftLegTracker = false
	var hasRightLegTracker = false
	var hasLeftFootTracker = false
	var hasRightFootTracker = false
	var hasLeftArmTracker = false
	var hasRightArmTracker = false

	// Input trackers
	var headTracker: Tracker? = null
	var neckTracker: Tracker? = null
	var upperChestTracker: Tracker? = null
	var chestTracker: Tracker? = null
	var waistTracker: Tracker? = null
	var hipTracker: Tracker? = null
	var leftUpperLegTracker: Tracker? = null
	var leftLowerLegTracker: Tracker? = null
	var leftFootTracker: Tracker? = null
	var rightUpperLegTracker: Tracker? = null
	var rightLowerLegTracker: Tracker? = null
	var rightFootTracker: Tracker? = null
	var leftLowerArmTracker: Tracker? = null
	var rightLowerArmTracker: Tracker? = null
	var leftUpperArmTracker: Tracker? = null
	var rightUpperArmTracker: Tracker? = null
	var leftHandTracker: Tracker? = null
	var rightHandTracker: Tracker? = null
	var leftShoulderTracker: Tracker? = null
	var rightShoulderTracker: Tracker? = null
	var playspaceTracker: Tracker? = null

	// Output trackers
	var computedHeadTracker: Tracker? = null
	var computedChestTracker: Tracker? = null
	var computedHipTracker: Tracker? = null
	var computedLeftKneeTracker: Tracker? = null
	var computedLeftFootTracker: Tracker? = null
	var computedRightKneeTracker: Tracker? = null
	var computedRightFootTracker: Tracker? = null
	var computedLeftElbowTracker: Tracker? = null
	var computedRightElbowTracker: Tracker? = null
	var computedLeftHandTracker: Tracker? = null
	var computedRightHandTracker: Tracker? = null

	// Toggles
	private var extendedSpineModel = false
	private var extendedPelvisModel = false
	private var extendedKneeModel = false
	private var forceArmsFromHMD = true

	// Ratios
	private var waistFromChestHipAveraging = 0f
	private var waistFromChestLegsAveraging = 0f
	private var hipFromChestLegsAveraging = 0f
	private var hipFromWaistLegsAveraging = 0f
	private var hipLegsAveraging = 0f
	private var kneeTrackerAnkleAveraging = 0f
	private var kneeAnkleAveraging = 0f

	// Others
	private var pauseTracking = false // Pauses skeleton tracking if true, resumes skeleton tracking if false

	// Modules
	var legTweaks = LegTweaks(this)
	var tapDetectionManager = TapDetectionManager(this)
	var viveEmulation = ViveEmulation(this)
	var localizer = Localizer(this)

	// Constructors
	init {
		assembleSkeleton()
		setComputedTrackers(humanPoseManager.computedTrackers)
	}

	constructor(
		humanPoseManager: HumanPoseManager,
		server: VRServer,
	) : this(humanPoseManager) {
		setTrackersFromList(server.allTrackers)
		tapDetectionManager = TapDetectionManager(
			this,
			humanPoseManager,
			server.configManager.vrConfig.tapDetection,
			server.resetHandler,
			server.tapSetupHandler,
			server.allTrackers,
		)
		legTweaks.setConfig(server.configManager.vrConfig.legTweaks)
		localizer.setEnabled(humanPoseManager.getToggle(SkeletonConfigToggles.SELF_LOCALIZATION))
	}

	constructor(
		humanPoseManager: HumanPoseManager,
		trackers: List<Tracker>?,
	) : this(humanPoseManager) {
		var trackersList = trackers
		if (trackersList == null) {
			trackersList = FastList(0)
		}
		setTrackersFromList(trackersList)
	}

	/**
	 * Assembles the whole skeleton
	 */
	@ThreadSafe
	fun assembleSkeleton() {
		// Assemble upper skeleton (head to hip)
		headBone.attachChild(neckBone)
		neckBone.attachChild(upperChestBone)
		upperChestBone.attachChild(chestBone)
		chestBone.attachChild(waistBone)
		waistBone.attachChild(hipBone)

		// Assemble lower skeleton (hip to feet)
		hipBone.attachChild(leftHipBone)
		hipBone.attachChild(rightHipBone)
		leftHipBone.attachChild(leftUpperLegBone)
		rightHipBone.attachChild(rightUpperLegBone)
		leftUpperLegBone.attachChild(leftLowerLegBone)
		rightUpperLegBone.attachChild(rightLowerLegBone)
		leftLowerLegBone.attachChild(leftFootBone)
		rightLowerLegBone.attachChild(rightFootBone)

		// Attach tracker bones for tracker offsets
		neckBone.attachChild(headTrackerBone)
		upperChestBone.attachChild(chestTrackerBone)
		hipBone.attachChild(hipTrackerBone)
		leftUpperLegBone.attachChild(leftKneeTrackerBone)
		rightUpperLegBone.attachChild(rightKneeTrackerBone)
		leftFootBone.attachChild(leftFootTrackerBone)
		rightFootBone.attachChild(rightFootTrackerBone)

		// Attach arms
		assembleSkeletonArms(false)
	}

	/**
	 * Dynamically assembles the arms of the skeleton
	 *
	 * @param reset disassemble before reassembling
	 */
	@ThreadSafe
	fun assembleSkeletonArms(reset: Boolean) {
		if (reset) {
			for (bone in allArmBones) {
				bone.detachWithChildren()
			}
		}

		// Shoulders
		neckBone.attachChild(leftShoulderBone)
		neckBone.attachChild(rightShoulderBone)

		// Upper arm
		leftShoulderBone.attachChild(leftUpperArmBone)
		rightShoulderBone.attachChild(rightUpperArmBone)

		// Lower arm and hand
		if (isTrackingLeftArmFromController) {
			leftHandTrackerBone.attachChild(leftHandBone)
			leftHandBone.attachChild(leftLowerArmBone)
			leftLowerArmBone.attachChild(leftElbowTrackerBone)
		} else {
			leftUpperArmBone.attachChild(leftLowerArmBone)
			leftUpperArmBone.attachChild(leftElbowTrackerBone)
			leftLowerArmBone.attachChild(leftHandBone)
			leftHandBone.attachChild(leftHandTrackerBone)
		}
		if (isTrackingRightArmFromController) {
			rightHandTrackerBone.attachChild(rightHandBone)
			rightHandBone.attachChild(rightLowerArmBone)
			rightLowerArmBone.attachChild(rightElbowTrackerBone)
		} else {
			rightUpperArmBone.attachChild(rightLowerArmBone)
			rightUpperArmBone.attachChild(rightElbowTrackerBone)
			rightLowerArmBone.attachChild(rightHandBone)
			rightHandBone.attachChild(rightHandTrackerBone)
		}
	}

	/**
	 * Set input trackers from a list
	 */
	fun setTrackersFromList(trackers: List<Tracker>) {
		headTracker = getTrackerForSkeleton(trackers, TrackerPosition.HEAD)
		neckTracker = getTrackerForSkeleton(trackers, TrackerPosition.NECK)
		upperChestTracker = getTrackerForSkeleton(trackers, TrackerPosition.UPPER_CHEST)
		chestTracker = getTrackerForSkeleton(trackers, TrackerPosition.CHEST)
		waistTracker = getTrackerForSkeleton(trackers, TrackerPosition.WAIST)
		hipTracker = getTrackerForSkeleton(trackers, TrackerPosition.HIP)
		leftUpperLegTracker = getTrackerForSkeleton(trackers, TrackerPosition.LEFT_UPPER_LEG)
		leftLowerLegTracker = getTrackerForSkeleton(trackers, TrackerPosition.LEFT_LOWER_LEG)
		leftFootTracker = getTrackerForSkeleton(trackers, TrackerPosition.LEFT_FOOT)
		rightUpperLegTracker = getTrackerForSkeleton(trackers, TrackerPosition.RIGHT_UPPER_LEG)
		rightLowerLegTracker = getTrackerForSkeleton(trackers, TrackerPosition.RIGHT_LOWER_LEG)
		rightFootTracker = getTrackerForSkeleton(trackers, TrackerPosition.RIGHT_FOOT)
		leftLowerArmTracker = getTrackerForSkeleton(trackers, TrackerPosition.LEFT_LOWER_ARM)
		rightLowerArmTracker = getTrackerForSkeleton(trackers, TrackerPosition.RIGHT_LOWER_ARM)
		leftUpperArmTracker = getTrackerForSkeleton(trackers, TrackerPosition.LEFT_UPPER_ARM)
		rightUpperArmTracker = getTrackerForSkeleton(trackers, TrackerPosition.RIGHT_UPPER_ARM)
		leftHandTracker = getTrackerForSkeleton(trackers, TrackerPosition.LEFT_HAND)
		rightHandTracker = getTrackerForSkeleton(trackers, TrackerPosition.RIGHT_HAND)
		leftShoulderTracker = getTrackerForSkeleton(trackers, TrackerPosition.LEFT_SHOULDER)
		rightShoulderTracker = getTrackerForSkeleton(trackers, TrackerPosition.RIGHT_SHOULDER)
		playspaceTracker = getTrackerForSkeleton(trackers, TrackerPosition.PLAYSPACE)

		// Check for specific conditions and store them in booleans.
		hasSpineTracker = upperChestTracker != null || chestTracker != null || waistTracker != null || hipTracker != null
		hasKneeTrackers = leftUpperLegTracker != null && rightUpperLegTracker != null
		hasLeftLegTracker = leftUpperLegTracker != null || leftLowerLegTracker != null || leftFootTracker != null
		hasRightLegTracker = rightUpperLegTracker != null || rightLowerLegTracker != null || rightFootTracker != null
		hasLeftFootTracker = leftFootTracker != null
		hasRightFootTracker = rightFootTracker != null
		hasLeftArmTracker = leftLowerArmTracker != null || leftUpperArmTracker != null
		hasRightArmTracker = rightLowerArmTracker != null || rightUpperArmTracker != null

		// Rebuilds the arm skeleton nodes attachments
		assembleSkeletonArms(true)

		// Refresh headShift
		humanPoseManager.computeNodeOffset(BoneType.HEAD)

		// Refresh node offsets for arms
		computeDependentArmOffsets()

		// Update tap detection's trackers
		tapDetectionManager.updateConfig(trackers)
	}

	/**
	 * Set computed trackers from list
	 */
	private fun setComputedTrackers(trackers: List<Tracker>) {
		for (t in trackers) {
			setComputedTracker(t)
		}
	}

	/**
	 * Set computed tracker
	 */
	private fun setComputedTracker(tracker: Tracker) {
		when (tracker.trackerPosition) {
			TrackerPosition.HEAD -> computedHeadTracker = tracker
			TrackerPosition.UPPER_CHEST -> computedChestTracker = tracker
			TrackerPosition.HIP -> computedHipTracker = tracker
			TrackerPosition.LEFT_UPPER_LEG -> computedLeftKneeTracker = tracker
			TrackerPosition.LEFT_FOOT -> computedLeftFootTracker = tracker
			TrackerPosition.RIGHT_UPPER_LEG -> computedRightKneeTracker = tracker
			TrackerPosition.RIGHT_FOOT -> computedRightFootTracker = tracker
			TrackerPosition.LEFT_UPPER_ARM -> computedLeftElbowTracker = tracker
			TrackerPosition.RIGHT_UPPER_ARM -> computedRightElbowTracker = tracker
			TrackerPosition.LEFT_HAND -> computedLeftHandTracker = tracker
			TrackerPosition.RIGHT_HAND -> computedRightHandTracker = tracker
			else -> {}
		}
	}

	/**
	 * Get output tracker from TrackerRole
	 */
	fun getComputedTracker(trackerRole: TrackerRole): Tracker = when (trackerRole) {
		TrackerRole.HEAD -> computedHeadTracker!!
		TrackerRole.CHEST -> computedChestTracker!!
		TrackerRole.WAIST -> computedHipTracker!!
		TrackerRole.LEFT_KNEE -> computedLeftKneeTracker!!
		TrackerRole.LEFT_FOOT -> computedLeftFootTracker!!
		TrackerRole.RIGHT_KNEE -> computedRightKneeTracker!!
		TrackerRole.RIGHT_FOOT -> computedRightFootTracker!!
		TrackerRole.LEFT_ELBOW -> computedLeftElbowTracker!!
		TrackerRole.RIGHT_ELBOW -> computedRightElbowTracker!!
		TrackerRole.LEFT_HAND -> computedLeftHandTracker!!
		TrackerRole.RIGHT_HAND -> computedRightHandTracker!!
		else -> throw IllegalArgumentException("Unsupported computed tracker's TrackerRole in HumanSkeleton")
	}

	/**
	 * Updates the pose from tracker positions
	 */
	@VRServerThread
	fun updatePose() {
		// Check taps for resets
		tapDetectionManager.update()

		// Do FK
		updateTransforms()

		// Update bones and output trackers
		updateBones()
		updateComputedTrackers()

		// Don't run post-processing if the tracking is paused
		if (pauseTracking) return

		// Run Legtweaks
		legTweaks.tweakLegs()

		// Run Mocap mode
		localizer.update()

		// Run Vive hip emulation
		viveEmulation.update()

		// Playspace motion compensation
		playspaceTracker?.let {
			if (it.hasRotation) {
				val motionCompensationRotOffset = it.getRotation().inv()
				for (bone in allBones) {
					bone.setRawRotation(motionCompensationRotOffset * bone.getLocalRotation())
				}
			}
			if (it.hasPosition) {
				// TODO motion compensate for position as well?
				// headBone.setPosition(headBone.getPosition() - it.position)
			}

			// Update bones and output trackers
			updateBones()
			updateComputedTrackers()
		}
	}

	/**
	 * Update all the bones by updating the roots
	 */
	@ThreadSafe
	fun updateBones() {
		headBone.update()
		if (isTrackingLeftArmFromController) leftHandTrackerBone.update()
		if (isTrackingRightArmFromController) rightHandTrackerBone.update()
	}

	/**
	 * Update all the bones' transforms from trackers
	 */
	private fun updateTransforms() {
		// Head
		updateHeadTransforms()

		// Stop at head (for the body to follow) if tracking is paused
		if (pauseTracking) return

		// Spine
		updateSpineTransforms()

		// Left leg
		updateLegTransforms(
			leftUpperLegBone,
			leftKneeTrackerBone,
			leftLowerLegBone,
			leftFootBone,
			leftFootTrackerBone,
			leftUpperLegTracker,
			leftLowerLegTracker,
			leftFootTracker,
		)

		// Right leg
		updateLegTransforms(
			rightUpperLegBone,
			rightKneeTrackerBone,
			rightLowerLegBone,
			rightFootBone,
			rightFootTrackerBone,
			rightUpperLegTracker,
			rightLowerLegTracker,
			rightFootTracker,
		)

		// Left arm
		updateArmTransforms(
			isTrackingLeftArmFromController,
			leftShoulderBone,
			leftUpperArmBone,
			leftElbowTrackerBone,
			leftLowerArmBone,
			leftHandBone,
			leftHandTrackerBone,
			leftShoulderTracker,
			leftUpperArmTracker,
			leftLowerArmTracker,
			leftHandTracker,
		)

		// Right arm
		updateArmTransforms(
			isTrackingRightArmFromController,
			rightShoulderBone,
			rightUpperArmBone,
			rightElbowTrackerBone,
			rightLowerArmBone,
			rightHandBone,
			rightHandTrackerBone,
			rightShoulderTracker,
			rightUpperArmTracker,
			rightLowerArmTracker,
			rightHandTracker,
		)
	}

	/**
	 * Update the head and neck bone transforms
	 */
	private fun updateHeadTransforms() {
		var headRot = IDENTITY
		headTracker?.let { head ->
			// Set head position
			if (head.hasPosition) headBone.setPosition(head.position)

			// Get head rotation
			headRot = head.getRotation()

			// Set head rotation
			headBone.setRotation(headRot)
			headTrackerBone.setRotation(headRot)

			// Get neck rotation
			neckTracker?.let { headRot = it.getRotation() }

			// Set neck rotation
			neckBone.setRotation(headRot)
		} ?: run {
			// Set head position
			if (!localizer.getEnabled()) headBone.setPosition(NULL)

			// Get neck or spine rotation (else is identity)
			getFirstAvailableTracker(
				neckTracker,
				upperChestTracker,
				chestTracker,
				waistTracker,
				hipTracker,
			)?.let { headRot = it.getRotation() }

			headBone.setRotation(headRot)
			headTrackerBone.setRotation(headRot)
			neckBone.setRotation(headRot)
		}
	}

	/**
	 * Update the spine transforms, from the upper chest to the hip
	 */
	private fun updateSpineTransforms() {
		if (hasSpineTracker) {
			// Upper chest and chest tracker
			getFirstAvailableTracker(upperChestTracker, chestTracker, waistTracker, hipTracker)?.let {
				upperChestBone.setRotation(it.getRotation())
				chestTrackerBone.setRotation(it.getRotation())
			}

			// Chest
			getFirstAvailableTracker(chestTracker, upperChestTracker, waistTracker, hipTracker)?.let {
				chestBone.setRotation(it.getRotation())
			}

			// Waist
			getFirstAvailableTracker(waistTracker, chestTracker, hipTracker, upperChestTracker)?.let {
				waistBone.setRotation(it.getRotation())
			}

			// Hip and hip tracker
			getFirstAvailableTracker(hipTracker, waistTracker, chestTracker, upperChestTracker)?.let {
				hipBone.setRotation(it.getRotation())
				hipTrackerBone.setRotation(it.getRotation())
			}
		} else if (headTracker != null) {
			// Align with neck's yaw
			val yawRot = neckBone.getGlobalRotation().project(POS_Y).unit()
			upperChestBone.setRotation(yawRot)
			chestTrackerBone.setRotation(yawRot)
			chestBone.setRotation(yawRot)
			waistBone.setRotation(yawRot)
			hipBone.setRotation(yawRot)
			hipTrackerBone.setRotation(yawRot)
		}

		// Extended spine model
		if (extendedSpineModel && hasSpineTracker) {
			// Tries to guess missing lower spine trackers by interpolating rotations
			if (waistTracker == null) {
				getFirstAvailableTracker(chestTracker, upperChestTracker)?.let { chest ->
					hipTracker?.let {
						// Calculates waist from chest + hip
						var hipRot = it.getRotation()
						var chestRot = chest.getRotation()

						// Get the rotation relative to where we expect the hip to be
						if (chestRot.times(FORWARD_QUATERNION).dot(hipRot) < 0.0f) {
							hipRot = hipRot.unaryMinus()
						}

						// Interpolate between the chest and the hip
						chestRot = chestRot.interpQ(hipRot, waistFromChestHipAveraging)

						// Set waist's rotation
						waistBone.setRotation(chestRot)
					} ?: run {
						if (hasKneeTrackers) {
							// Calculates waist from chest + legs
							var leftLegRot = leftUpperLegTracker?.getRotation() ?: IDENTITY
							var rightLegRot = rightUpperLegTracker?.getRotation() ?: IDENTITY
							var chestRot = chest.getRotation()

							// Get the rotation relative to where we expect the upper legs to be
							val expectedUpperLegsRot = chestRot.times(FORWARD_QUATERNION)
							if (expectedUpperLegsRot.dot(leftLegRot) < 0.0f) {
								leftLegRot = leftLegRot.unaryMinus()
							}
							if (expectedUpperLegsRot.dot(rightLegRot) < 0.0f) {
								rightLegRot = rightLegRot.unaryMinus()
							}

							// Interpolate between the pelvis, averaged from the legs, and the chest
							chestRot = chestRot.interpQ(leftLegRot.lerpQ(rightLegRot, 0.5f), waistFromChestLegsAveraging).unit()

							// Set waist's rotation
							waistBone.setRotation(chestRot)
						}
					}
				}
			}
			if (hipTracker == null && hasKneeTrackers) {
				waistTracker?.let {
					// Calculates hip from waist + legs
					var leftLegRot = leftUpperLegTracker?.getRotation() ?: IDENTITY
					var rightLegRot = rightUpperLegTracker?.getRotation() ?: IDENTITY
					var waistRot = it.getRotation()

					// Get the rotation relative to where we expect the upper legs to be
					val expectedUpperLegsRot = waistRot.times(FORWARD_QUATERNION)
					if (expectedUpperLegsRot.dot(leftLegRot) < 0.0f) {
						leftLegRot = leftLegRot.unaryMinus()
					}
					if (expectedUpperLegsRot.dot(rightLegRot) < 0.0f) {
						rightLegRot = rightLegRot.unaryMinus()
					}

					// Interpolate between the pelvis, averaged from the legs, and the chest
					waistRot = waistRot.interpQ(leftLegRot.lerpQ(rightLegRot, 0.5f), hipFromWaistLegsAveraging).unit()

					// Set hip rotation
					hipBone.setRotation(waistRot)
					hipTrackerBone.setRotation(waistRot)
				} ?: run {
					getFirstAvailableTracker(chestTracker, upperChestTracker)?.let {
						// Calculates hip from chest + legs
						var leftLegRot = leftUpperLegTracker?.getRotation() ?: IDENTITY
						var rightLegRot = rightUpperLegTracker?.getRotation() ?: IDENTITY
						var chestRot = it.getRotation()

						// Get the rotation relative to where we expect the upper legs to be
						val expectedUpperLegsRot = chestRot.times(FORWARD_QUATERNION)
						if (expectedUpperLegsRot.dot(leftLegRot) < 0.0f) {
							leftLegRot = leftLegRot.unaryMinus()
						}
						if (expectedUpperLegsRot.dot(rightLegRot) < 0.0f) {
							rightLegRot = rightLegRot.unaryMinus()
						}

						// Interpolate between the pelvis, averaged from the legs, and the chest
						chestRot = chestRot.interpQ(leftLegRot.lerpQ(rightLegRot, 0.5f), hipFromChestLegsAveraging).unit()

						// Set hip rotation
						hipBone.setRotation(chestRot)
						hipTrackerBone.setRotation(chestRot)
					}
				}
			}
		}

		// Extended pelvis model
		if (extendedPelvisModel && hasKneeTrackers && hipTracker == null) {
			val leftLegRot = leftUpperLegTracker?.getRotation() ?: IDENTITY
			val rightLegRot = rightUpperLegTracker?.getRotation() ?: IDENTITY
			val hipRot = hipBone.getLocalRotation()

			val extendedPelvisRot = extendedPelvisYawRoll(leftLegRot, rightLegRot, hipRot)

			// Interpolate between the hipRot and extendedPelvisRot
			val newHipRot = hipRot.interpR(extendedPelvisRot, hipLegsAveraging)

			// Set new hip rotation
			hipBone.setRotation(newHipRot)
			hipTrackerBone.setRotation(newHipRot)
		}

		// Set left and right hip rotations to the hip's
		leftHipBone.setRotation(hipBone.getLocalRotation())
		rightHipBone.setRotation(hipBone.getLocalRotation())
	}

	/**
	 * Update a leg's transforms, from its hip to its foot
	 */
	private fun updateLegTransforms(
		upperLegBone: Bone,
		kneeTrackerBone: Bone,
		lowerLegBone: Bone,
		footBone: Bone,
		footTrackerBone: Bone,
		upperLegTracker: Tracker?,
		lowerLegTracker: Tracker?,
		footTracker: Tracker?,
	) {
		var legRot = IDENTITY

		upperLegTracker?.let {
			// Get upper leg rotation
			legRot = it.getRotation()
		} ?: run {
			// Use hip's yaw
			legRot = hipBone.getLocalRotation().project(POS_Y).unit()
		}
		// Set upper leg rotation
		upperLegBone.setRotation(legRot)
		kneeTrackerBone.setRotation(legRot)

		lowerLegTracker?.let {
			// Get lower leg rotation
			legRot = it.getRotation()
		} ?: run {
			// Use lower leg or hip's yaw
			legRot = legRot.project(POS_Y).unit()
		}
		// Set lower leg rotation
		lowerLegBone.setRotation(legRot)

		// Get foot rotation
		footTracker?.let { legRot = it.getRotation() }
		// Set foot rotation
		footBone.setRotation(legRot)
		footTrackerBone.setRotation(legRot)

		// Extended knee model
		if (extendedKneeModel) {
			upperLegTracker?.let { upper ->
				lowerLegTracker?.let { lower ->
					// Averages the upper leg's rotation with the local lower leg's
					// pitch and roll and apply to the tracker node.
					val upperRot = upper.getRotation()
					val lowerRot = lower.getRotation()
					val extendedRot = extendedKneeYawRoll(upperRot, lowerRot)

					upperLegBone.setRotation(upperRot.interpR(extendedRot, kneeAnkleAveraging))
					kneeTrackerBone.setRotation(upperRot.interpR(extendedRot, kneeTrackerAnkleAveraging))
				}
			}
		}
	}

	/**
	 * Update an arm's transforms, from its shoulder to its hand
	 */
	private fun updateArmTransforms(
		isTrackingFromController: Boolean,
		shoulderBone: Bone,
		upperArmBone: Bone,
		elbowTrackerBone: Bone,
		lowerArmBone: Bone,
		handBone: Bone,
		handTrackerBone: Bone,
		shoulderTracker: Tracker?,
		upperArmTracker: Tracker?,
		lowerArmTracker: Tracker?,
		handTracker: Tracker?,
	) {
		if (isTrackingFromController) { // From controller
			// Set hand rotation and position from tracker
			handTracker?.let {
				handTrackerBone.setPosition(it.position)
				handTrackerBone.setRotation(it.getRotation())
				handBone.setRotation(it.getRotation())
			}

			// Get lower arm rotation
			var armRot = getFirstAvailableTracker(lowerArmTracker, upperArmTracker)?.getRotation() ?: IDENTITY
			// Set lower arm rotation
			lowerArmBone.setRotation(armRot)

			// Get upper arm rotation
			armRot = getFirstAvailableTracker(upperArmTracker, lowerArmTracker)?.getRotation() ?: IDENTITY
			// Set elbow tracker rotation
			elbowTrackerBone.setRotation(armRot)
		} else { // From HMD
			// Get shoulder rotation
			var armRot = shoulderTracker?.getRotation() ?: upperChestBone.getLocalRotation()
			// Set shoulder rotation
			shoulderBone.setRotation(armRot)

			if (upperArmTracker != null || lowerArmTracker != null) {
				// Get upper arm rotation
				getFirstAvailableTracker(upperArmTracker, lowerArmTracker)?.let { armRot = it.getRotation() }
				// Set upper arm and elbow tracker rotation
				upperArmBone.setRotation(armRot)
				elbowTrackerBone.setRotation(armRot)

				// Get lower arm rotation
				getFirstAvailableTracker(lowerArmTracker, upperArmTracker)?.let { armRot = it.getRotation() }
				// Set lower arm rotation
				lowerArmBone.setRotation(armRot)
			} else {
				// Fallback arm rotation as upper chest
				armRot = upperChestBone.getLocalRotation()
				upperArmBone.setRotation(armRot)
				elbowTrackerBone.setRotation(armRot)
				lowerArmBone.setRotation(armRot)
			}

			// Get hand rotation
			handTracker?.let { armRot = it.getRotation() }
			// Set hand, and hand tracker rotation
			handBone.setRotation(armRot)
			handTrackerBone.setRotation(armRot)
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
	private fun extendedKneeYawRoll(knee: Quaternion, ankle: Quaternion): Quaternion {
		val r = knee.inv() * ankle
		val c = Quaternion(r.w, -r.x, 0f, 0f)
		return (knee * r * c).unit()
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
	private fun extendedPelvisYawRoll(
		leftKnee: Quaternion,
		rightKnee: Quaternion,
		hip: Quaternion,
	): Quaternion {
		// Get the knees' rotation relative to where we expect them to be.
		// The angle between your knees and hip can be over 180 degrees...
		var leftKneeRot = leftKnee
		var rightKneeRot = rightKnee

		val kneeRot = hip.times(FORWARD_QUATERNION)
		if (kneeRot.dot(leftKneeRot) < 0.0f) {
			leftKneeRot = leftKneeRot.unaryMinus()
		}
		if (kneeRot.dot(rightKneeRot) < 0.0f) {
			rightKneeRot = rightKneeRot.unaryMinus()
		}

		// R = InverseHip * (LeftLeft + RightLeg)
		// C = Quaternion(R.w, -R.x, 0, 0)
		// Pelvis = Hip * R * C
		// normalize(Pelvis)
		val r = hip.inv() * (leftKneeRot + rightKneeRot)
		val c = Quaternion(r.w, -r.x, 0f, 0f)
		return (hip * r * c).unit()
	}

	// Update the output trackers
	private fun updateComputedTrackers() {
		updateComputedTracker(computedHeadTracker, headTrackerBone)
		updateComputedTracker(computedChestTracker, chestTrackerBone)
		updateComputedTracker(computedHipTracker, hipTrackerBone)
		updateComputedTracker(computedLeftKneeTracker, leftKneeTrackerBone)
		updateComputedTracker(computedRightKneeTracker, rightKneeTrackerBone)
		updateComputedTracker(computedLeftFootTracker, leftFootTrackerBone)
		updateComputedTracker(computedRightFootTracker, rightFootTrackerBone)
		updateComputedTracker(computedLeftElbowTracker, leftElbowTrackerBone)
		updateComputedTracker(computedRightElbowTracker, rightElbowTrackerBone)
		updateComputedTracker(computedLeftHandTracker, leftHandTrackerBone)
		updateComputedTracker(computedRightHandTracker, rightHandTrackerBone)
	}

	private fun updateComputedTracker(computedTracker: Tracker?, trackerBone: Bone) {
		computedTracker?.let {
			it.position = trackerBone.getTailPosition()
			it.setRotation(trackerBone.getGlobalRotation() * trackerBone.rotationOffset.inv())
			it.dataTick()
		}
	}

	// Skeleton Config toggles
	fun updateToggleState(configToggle: SkeletonConfigToggles, newValue: Boolean) {
		when (configToggle) {
			SkeletonConfigToggles.EXTENDED_SPINE_MODEL -> extendedSpineModel = newValue

			SkeletonConfigToggles.EXTENDED_PELVIS_MODEL -> extendedPelvisModel = newValue

			SkeletonConfigToggles.EXTENDED_KNEE_MODEL -> extendedKneeModel = newValue

			SkeletonConfigToggles.FORCE_ARMS_FROM_HMD -> {
				forceArmsFromHMD = newValue
				assembleSkeletonArms(true) // Rebuilds the arm skeleton nodes attachments
				computeDependentArmOffsets() // Refresh node offsets for arms
			}

			SkeletonConfigToggles.SKATING_CORRECTION -> legTweaks.setSkatingCorrectionEnabled(newValue)

			SkeletonConfigToggles.FLOOR_CLIP -> legTweaks.setFloorClipEnabled(newValue)

			SkeletonConfigToggles.VIVE_EMULATION -> viveEmulation.enabled = newValue

			SkeletonConfigToggles.TOE_SNAP -> legTweaks.toeSnapEnabled = newValue

			SkeletonConfigToggles.FOOT_PLANT -> legTweaks.footPlantEnabled = newValue

			SkeletonConfigToggles.SELF_LOCALIZATION -> localizer.setEnabled(newValue)
		}
	}

	// Skeleton Config ratios
	fun updateValueState(configValue: SkeletonConfigValues, newValue: Float) {
		when (configValue) {
			SkeletonConfigValues.WAIST_FROM_CHEST_HIP_AVERAGING -> waistFromChestHipAveraging = newValue
			SkeletonConfigValues.WAIST_FROM_CHEST_LEGS_AVERAGING -> waistFromChestLegsAveraging = newValue
			SkeletonConfigValues.HIP_FROM_CHEST_LEGS_AVERAGING -> hipFromChestLegsAveraging = newValue
			SkeletonConfigValues.HIP_FROM_WAIST_LEGS_AVERAGING -> hipFromWaistLegsAveraging = newValue
			SkeletonConfigValues.HIP_LEGS_AVERAGING -> hipLegsAveraging = newValue
			SkeletonConfigValues.KNEE_TRACKER_ANKLE_AVERAGING -> kneeTrackerAnkleAveraging = newValue
			SkeletonConfigValues.KNEE_ANKLE_AVERAGING -> kneeAnkleAveraging = newValue
		}
	}

	// Skeleton Config bone lengths
	fun updateNodeOffset(boneType: BoneType, offset: Vector3) {
		var transOffset = offset

		// If no head position + rotation, headShift == 0
		if (boneType == BoneType.HEAD && (headTracker == null || !(headTracker!!.hasPosition && headTracker!!.hasRotation))) {
			transOffset = NULL
		}
		// If trackingArmFromController, reverse
		if (((boneType == BoneType.LEFT_LOWER_ARM || boneType == BoneType.LEFT_HAND) && isTrackingLeftArmFromController) ||
			(boneType == BoneType.RIGHT_LOWER_ARM || boneType == BoneType.RIGHT_HAND) && isTrackingRightArmFromController
		) {
			transOffset = -transOffset
		}

		// Compute bone rotation
		val rotOffset = if (transOffset.len() > 0f) {
			if (transOffset.unit().y == 1f) {
				I
			} else {
				fromTo(NEG_Y, transOffset)
			}
		} else {
			IDENTITY
		}

		// Get the bone
		val bone = getBone(boneType)

		// Update bone length
		bone.length = transOffset.len()

		// Set bone rotation offset
		bone.rotationOffset = rotOffset
	}

	private fun computeDependentArmOffsets() {
		humanPoseManager.computeNodeOffset(BoneType.LEFT_LOWER_ARM)
		humanPoseManager.computeNodeOffset(BoneType.RIGHT_LOWER_ARM)
		humanPoseManager.computeNodeOffset(BoneType.LEFT_HAND)
		humanPoseManager.computeNodeOffset(BoneType.RIGHT_HAND)
	}

	fun getBone(bone: BoneType): Bone = when (bone) {
		BoneType.HEAD -> headBone
		BoneType.HEAD_TRACKER -> headTrackerBone
		BoneType.NECK -> neckBone
		BoneType.UPPER_CHEST -> upperChestBone
		BoneType.CHEST_TRACKER -> chestTrackerBone
		BoneType.CHEST -> chestBone
		BoneType.WAIST -> waistBone
		BoneType.HIP -> hipBone
		BoneType.HIP_TRACKER -> hipTrackerBone
		BoneType.LEFT_HIP -> leftHipBone
		BoneType.RIGHT_HIP -> rightHipBone
		BoneType.LEFT_UPPER_LEG -> leftUpperLegBone
		BoneType.RIGHT_UPPER_LEG -> rightUpperLegBone
		BoneType.LEFT_KNEE_TRACKER -> leftKneeTrackerBone
		BoneType.RIGHT_KNEE_TRACKER -> rightKneeTrackerBone
		BoneType.LEFT_LOWER_LEG -> leftLowerLegBone
		BoneType.RIGHT_LOWER_LEG -> rightLowerLegBone
		BoneType.LEFT_FOOT -> leftFootBone
		BoneType.RIGHT_FOOT -> rightFootBone
		BoneType.LEFT_FOOT_TRACKER -> leftFootTrackerBone
		BoneType.RIGHT_FOOT_TRACKER -> rightFootTrackerBone
		BoneType.LEFT_SHOULDER -> leftShoulderBone
		BoneType.RIGHT_SHOULDER -> rightShoulderBone
		BoneType.LEFT_UPPER_ARM -> leftUpperArmBone
		BoneType.RIGHT_UPPER_ARM -> rightUpperArmBone
		BoneType.LEFT_ELBOW_TRACKER -> leftElbowTrackerBone
		BoneType.RIGHT_ELBOW_TRACKER -> rightElbowTrackerBone
		BoneType.LEFT_LOWER_ARM -> leftLowerArmBone
		BoneType.RIGHT_LOWER_ARM -> rightLowerArmBone
		BoneType.LEFT_HAND -> leftHandBone
		BoneType.RIGHT_HAND -> rightHandBone
		BoneType.LEFT_HAND_TRACKER -> leftHandTrackerBone
		BoneType.RIGHT_HAND_TRACKER -> rightHandTrackerBone
	}

	/**
	 * Returns an array of all the non-tracker bones.
	 */
	val allHumanBones: Array<Bone>
		get() = arrayOf(
			headBone,
			neckBone,
			upperChestBone,
			chestBone,
			waistBone,
			hipBone,
			leftHipBone,
			rightHipBone,
			leftUpperLegBone,
			rightUpperLegBone,
			leftLowerLegBone,
			rightLowerLegBone,
			leftFootBone,
			rightFootBone,
			leftShoulderBone,
			rightShoulderBone,
			leftUpperArmBone,
			rightUpperArmBone,
			leftLowerArmBone,
			rightLowerArmBone,
			leftHandBone,
			rightHandBone,
		)

	/**
	 * Returns an array of all the bones, trackers or not
	 */
	private val allBones: Array<Bone>
		get() = arrayOf(
			headBone,
			neckBone,
			headTrackerBone,
			upperChestBone,
			chestBone,
			chestTrackerBone,
			waistBone,
			hipBone,
			hipTrackerBone,
			leftHipBone,
			rightHipBone,
			leftUpperLegBone,
			leftKneeTrackerBone,
			rightUpperLegBone,
			rightKneeTrackerBone,
			leftLowerLegBone,
			rightLowerLegBone,
			leftFootBone,
			leftFootTrackerBone,
			rightFootBone,
			rightFootTrackerBone,
		) + allArmBones

	/**
	 * Returns all the arm bones, tracker or not.
	 */
	private val allArmBones: Array<Bone>
		get() = arrayOf(
			leftShoulderBone,
			rightShoulderBone,
			leftUpperArmBone,
			rightUpperArmBone,
			leftElbowTrackerBone,
			rightElbowTrackerBone,
			leftLowerArmBone,
			rightLowerArmBone,
			leftHandBone,
			rightHandBone,
			leftHandTrackerBone,
			rightHandTrackerBone,
		)

	val hmdHeight: Float
		get() = headTracker?.position?.y ?: 0f

	/**
	 * Runs checks to know if we should (and are) performing the tracking of the
	 * left arm from the controller.
	 *
	 * @return a bool telling us if we are tracking the left arm from the
	 * controller or not.
	 */
	val isTrackingLeftArmFromController: Boolean
		get() = leftHandTracker != null && leftHandTracker!!.hasPosition && !forceArmsFromHMD

	/**
	 * Runs checks to know if we should (and are) performing the tracking of the
	 * right arm from the controller.
	 *
	 * @return a bool telling us if we are tracking the right arm from the
	 * controller or not.
	 */
	val isTrackingRightArmFromController: Boolean
		get() = rightHandTracker != null && rightHandTracker!!.hasPosition && !forceArmsFromHMD
	val trackersToReset: List<Tracker?>
		get() = listOf(
			neckTracker,
			chestTracker,
			waistTracker,
			hipTracker,
			leftUpperLegTracker,
			leftLowerLegTracker,
			leftFootTracker,
			rightUpperLegTracker,
			rightLowerLegTracker,
			rightFootTracker,
			leftLowerArmTracker,
			rightLowerArmTracker,
			leftUpperArmTracker,
			rightUpperArmTracker,
			leftHandTracker,
			rightHandTracker,
			leftShoulderTracker,
			rightShoulderTracker,
		)

	fun resetTrackersFull(resetSourceName: String?) {
		var referenceRotation = IDENTITY
		// Reset the head tracker from identity if needed, else store its rotation
		headTracker?.let {
			if (it.needsReset) {
				it.resetsHandler.resetFull(referenceRotation)
			} else {
				referenceRotation = it.getRotation()
			}
		}
		// Resets the trackers with the HMD as reference.
		for (tracker in trackersToReset) {
			if (tracker != null && tracker.needsReset) {
				tracker.resetsHandler.resetFull(referenceRotation)
			}
		}
		// Reset the playspace tracker from identity rotation
		playspaceTracker?.resetsHandler?.resetFull(IDENTITY)

		// Tell floorclip to reset its floor level on the next update
		// of the computed trackers
		if (!localizer.getEnabled()) {
			legTweaks.resetFloorLevel()
		}
		legTweaks.resetBuffer()
		localizer.reset()
		LogManager.info(String.format("[HumanSkeleton] Reset: full (%s)", resetSourceName))
	}

	@VRServerThread
	fun resetTrackersYaw(resetSourceName: String?) {
		var referenceRotation = IDENTITY
		// Reset the head tracker from identity if needed, else store its rotation
		headTracker?.let {
			if (it.needsReset) {
				it.resetsHandler.resetYaw(referenceRotation)
			} else {
				referenceRotation = it.getRotation()
			}
		}
		// Resets the trackers with the HMD as reference.
		for (tracker in trackersToReset) {
			if (tracker != null && tracker.needsReset) {
				tracker.resetsHandler.resetYaw(referenceRotation)
			}
		}
		// Reset the playspace tracker from identity rotation
		playspaceTracker?.resetsHandler?.resetYaw(IDENTITY)

		legTweaks.resetBuffer()
		LogManager.info(String.format("[HumanSkeleton] Reset: yaw (%s)", resetSourceName))
	}

	@VRServerThread
	fun resetTrackersMounting(resetSourceName: String?) {
		var referenceRotation = IDENTITY
		// Reset the head tracker from identity if needed, else store its rotation
		headTracker?.let {
			if (it.needsMounting) {
				it.resetsHandler.resetMounting(referenceRotation)
			} else {
				referenceRotation = it.getRotation()
			}
		}
		// Resets the trackers with the HMD as reference.
		for (tracker in trackersToReset) {
			if (tracker != null && tracker.needsMounting) {
				tracker.resetsHandler.resetMounting(referenceRotation)
			}
		}

		legTweaks.resetBuffer()
		localizer.reset()
		LogManager.info(String.format("[HumanSkeleton] Reset: mounting (%s)", resetSourceName))
	}

	@VRServerThread
	fun clearTrackersMounting(resetSourceName: String?) {
		headTracker?.let {
			if (it.needsMounting) it.resetsHandler.clearMounting()
		}
		for (tracker in trackersToReset) {
			if (tracker != null &&
				tracker.needsMounting
			) {
				tracker.resetsHandler.clearMounting()
			}
		}
		legTweaks.resetBuffer()
		LogManager.info(String.format("[HumanSkeleton] Clear: mounting (%s)", resetSourceName))
	}

	fun updateTapDetectionConfig() {
		tapDetectionManager.updateConfig(null)
	}

	fun updateLegTweaksConfig() {
		legTweaks.updateConfig()
	}

	/**
	 * Does not save to config
	 */
	fun setLegTweaksStateTemp(
		skatingCorrection: Boolean,
		floorClip: Boolean,
		toeSnap: Boolean,
		footPlant: Boolean,
	) {
		legTweaks.setSkatingCorrectionEnabled(skatingCorrection)
		legTweaks.setFloorClipEnabled(floorClip)
		legTweaks.toeSnapEnabled = toeSnap
		legTweaks.footPlantEnabled = footPlant
	}

	/**
	 * Resets to config values
	 */
	fun clearLegTweaksStateTemp(
		skatingCorrection: Boolean,
		floorClip: Boolean,
		toeSnap: Boolean,
		footPlant: Boolean,
	) {
		// only reset the true values as they are a mask for what to reset
		if (skatingCorrection) {
			legTweaks
				.setSkatingCorrectionEnabled(
					humanPoseManager.getToggle(SkeletonConfigToggles.SKATING_CORRECTION),
				)
		}
		if (floorClip) {
			legTweaks
				.setFloorClipEnabled(humanPoseManager.getToggle(SkeletonConfigToggles.FLOOR_CLIP))
		}
		if (toeSnap) legTweaks.toeSnapEnabled = humanPoseManager.getToggle(SkeletonConfigToggles.TOE_SNAP)
		if (footPlant) legTweaks.footPlantEnabled = humanPoseManager.getToggle(SkeletonConfigToggles.FOOT_PLANT)
	}

	val legTweaksState: BooleanArray
		get() {
			val state = BooleanArray(4)
			state[0] = legTweaks.floorClipEnabled
			state[1] = legTweaks.skatingCorrectionEnabled
			state[2] = legTweaks.toeSnapEnabled
			state[3] = legTweaks.footPlantEnabled
			return state
		}

	/**
	 * Master enable/disable of all leg tweaks (for Autobone)
	 */
	@VRServerThread
	fun setLegTweaksEnabled(value: Boolean) {
		legTweaks.enabled = value
	}

	@VRServerThread
	fun setFloorclipEnabled(value: Boolean) {
		humanPoseManager.setToggle(SkeletonConfigToggles.FLOOR_CLIP, value)
	}

	@VRServerThread
	fun setSkatingCorrectionEnabled(value: Boolean) {
		humanPoseManager.setToggle(SkeletonConfigToggles.SKATING_CORRECTION, value)
	}

	fun getPauseTracking(): Boolean = pauseTracking

	fun setPauseTracking(pauseTracking: Boolean, sourceName: String?) {
		if (!pauseTracking && this.pauseTracking) {
			// If unpausing tracking, clear the legtweaks buffer
			legTweaks.resetBuffer()
		}
		this.pauseTracking = pauseTracking
		LogManager.info(String.format("[HumanSkeleton] ${if (pauseTracking) "Pause" else "Unpause"} tracking (%s)", sourceName))
		// Report the new state of tracking pause
		humanPoseManager.trackingPauseHandler.sendTrackingPauseState(pauseTracking)
	}

	fun togglePauseTracking(sourceName: String?): Boolean {
		val newState = !pauseTracking
		setPauseTracking(newState, sourceName)
		return newState
	}

	companion object {
		val FORWARD_QUATERNION = EulerAngles(
			EulerOrder.YZX,
			FastMath.HALF_PI,
			0f,
			0f,
		).toQuaternion()
	}
}
