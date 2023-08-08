package dev.slimevr.tracking.processor.skeleton

import com.jme3.math.FastMath
import dev.slimevr.config.LegTweaksConfig
import dev.slimevr.tracking.processor.TransformNode
import dev.slimevr.tracking.processor.config.SkeletonConfigToggles
import io.github.axisangles.ktmath.EulerAngles
import io.github.axisangles.ktmath.EulerOrder
import io.github.axisangles.ktmath.Quaternion
import io.github.axisangles.ktmath.Vector3
import kotlin.math.*

class LegTweaks(private val skeleton: HumanSkeleton) {
	/**
	 * here is an explanation of each parameter that may need explaining
	 * STANDING_CUTOFF_VERTICAL is the percentage the hip has to be below its
	 * position at calibration to register as the user not standing
	 * MAX_DISENGAGEMENT_OFFSET is how much the floor will be shifted to allow
	 * an offset to happen smoothly DYNAMIC_DISPLACEMENT_CUTOFF is the percent
	 * of downwards rotation that can contribute to dynamic displacement
	 * MAX_DYNAMIC_DISPLACEMENT is the max amount the floor will be moved up to
	 * account for the foot rotating downward and needing to be put higher to
	 * avoid clipping in the game world MIN_ACCEPTABLE_ERROR and
	 * MAX_ACCEPTABLE_ERROR Defines the distance where CORRECTION_WEIGHT_MIN and
	 * CORRECTION_WEIGHT_MAX are calculating a percent of velocity to correct
	 * rather than using the min or max FLOOR_CALIBRATION_OFFSET is the amount
	 * the floor plane is shifted up. This can help the feet from floating
	 * slightly above the ground
	 */

	companion object {
		// clip correction
		const val DYNAMIC_DISPLACEMENT_CUTOFF = 1.0f
		const val FLOOR_CALIBRATION_OFFSET = 0.015f

		// skating correction
		private const val MIN_ACCEPTABLE_ERROR = 0.01f
		private const val MAX_ACCEPTABLE_ERROR = 0.05f
		private const val CORRECTION_WEIGHT_MIN = 0.55f
		private const val CORRECTION_WEIGHT_MAX = 0.70f
		private const val CONTINUOUS_CORRECTION_DIST = 0.5f
		private const val CONTINUOUS_CORRECTION_WARMUP = 175

		// knee / hip correction
		private const val KNEE_CORRECTION_WEIGHT = 0.00f
		private const val KNEE_LATERAL_WEIGHT = 0.8f
		private const val WAIST_PUSH_WEIGHT = 0.2f

		// COM calculation
		private const val HEAD_MASS = 0.0827f
		private const val THORAX_MASS = 0.1870f
		private const val ABDOMEN_MASS = 0.1320f
		private const val PELVIS_MASS = 0.1530f
		private const val THIGH_MASS = 0.1122f
		private const val LEG_AND_FOOT_MASS = 0.0620f
		private const val UPPER_ARM_MASS = 0.0263f
		private const val FOREARM_AND_HAND_MASS = 0.0224f

		// rotation correction
		private const val ROTATION_CORRECTION_VERTICAL = 0.1f
		private const val MAXIMUM_CORRECTION_ANGLE = 0.4f
		private const val MAXIMUM_CORRECTION_ANGLE_DELTA = 0.7f
		private const val MAXIMUM_TOE_DOWN_ANGLE = 0.8f
		private const val TOE_SNAP_COOLDOWN = 3.0f
		private const val MIN_DISTANCE_FOR_PLANT = 0.025f
		private const val MAX_DISTANCE_FOR_PLANT = 0.075f

		// misc
		const val NEARLY_ZERO = 0.001f
		private const val STANDING_CUTOFF_VERTICAL = 0.65f
		private const val MAX_DISENGAGEMENT_OFFSET = 0.30f
		private const val DEFAULT_ARM_DISTANCE = 0.15f
		private const val MAX_CORRECTION_STRENGTH_DELTA = 1.0f
	}

	// state variables
	private var floorLevel = 0f
	private var hipToFloorDist = 0f
	private var currentDisengagementOffset = 0.0f
	private var footLength = 0.0f
	private var currentCorrectionStrength = 0.3f // default value

	private var initialized = true
	private var enabled = true // master switch

	private var floorClipEnabled = false
	private var alwaysUseFloorclip = false
	private var skatingCorrectionEnabled = false
	private var toeSnapEnabled = false
	private var footPlantEnabled = false
	private var active = false
	private var rightLegActive = false
	private var leftLegActive = false
	private var leftFramesLocked = 0
	private var rightFramesLocked = 0
	private var leftFramesUnlocked = 0
	private var rightFramesUnlocked = 0
	private var leftToeAngle = 0.0f
	private var leftToeTouched = false
	private var rightToeAngle = 0.0f
	private var rightToeTouched = false
	private var localizerMode = false

	// config
	private var config: LegTweaksConfig? = null

	// leg data
	private var leftFootPosition = Vector3.NULL
	private var rightFootPosition = Vector3.NULL
	private var leftKneePosition = Vector3.NULL
	private var rightKneePosition = Vector3.NULL
	private var hipPosition = Vector3.NULL
	private var leftFootRotation = Quaternion.IDENTITY
	private var rightFootRotation = Quaternion.IDENTITY

	private var leftFootAcceleration = Vector3.NULL
	private var rightFootAcceleration = Vector3.NULL
	private var leftLowerLegAcceleration = Vector3.NULL
	private var rightLowerLegAcceleration = Vector3.NULL

	// buffer for holding previous frames of data
	private var bufferHead = LegTweaksBuffer()
	private var bufferInvalid = true

	constructor(skeleton: HumanSkeleton, config: LegTweaksConfig) : this(skeleton) {
		this.config = config
		updateConfig()
	}

	fun getLeftFootPosition(): Vector3 {
		return leftFootPosition
	}

	fun setLeftFootPosition(leftFootPosition: Vector3) {
		this.leftFootPosition = leftFootPosition
	}

	fun setLeftFootRotation(leftFootRotation: Quaternion) {
		this.leftFootRotation = leftFootRotation
	}

	fun getRightFootPosition(): Vector3 {
		return rightFootPosition
	}

	fun setRightFootPosition(rightFootPosition: Vector3) {
		this.rightFootPosition = rightFootPosition
	}

	fun setRightFootRotation(rightFootRotation: Quaternion) {
		this.rightFootRotation = rightFootRotation
	}

	fun getLeftKneePosition(): Vector3 {
		return leftKneePosition
	}

	fun setLeftKneePosition(leftKneePosition: Vector3) {
		this.leftKneePosition = leftKneePosition
	}

	fun getRightKneePosition(): Vector3 {
		return rightKneePosition
	}

	fun setRightKneePosition(rightKneePosition: Vector3) {
		this.rightKneePosition = rightKneePosition
	}

	fun getHipPosition(): Vector3 {
		return hipPosition
	}

	fun setHipPosition(hipPosition: Vector3) {
		this.hipPosition = hipPosition
	}

	fun getFloorLevel(): Float {
		return floorLevel
	}

	fun resetFloorLevel() {
		initialized = false
	}

	fun getActive(): Boolean {
		return active
	}

	fun setEnabled(enabled: Boolean) {
		this.enabled = enabled
	}

	fun setFloorClipEnabled(floorClipEnabled: Boolean) {
		this.floorClipEnabled = floorClipEnabled

		// reset the buffer
		bufferHead = LegTweaksBuffer()
		bufferInvalid = true
	}

	fun setSkatingReductionEnabled(skatingCorrectionEnabled: Boolean) {
		this.skatingCorrectionEnabled = skatingCorrectionEnabled

		// reset the buffer
		bufferHead = LegTweaksBuffer()
		bufferInvalid = true
	}

	fun setToeSnapEnabled(value: Boolean) {
		toeSnapEnabled = value
	}

	fun setFootPlantEnabled(value: Boolean) {
		footPlantEnabled = value
	}

	fun getEnabled(): Boolean {
		return enabled
	}

	fun getFloorclipEnabled(): Boolean {
		return floorClipEnabled
	}

	fun getSkatingReductionEnabled(): Boolean {
		return skatingCorrectionEnabled
	}

	fun getToeSnapEnabled(): Boolean {
		return toeSnapEnabled
	}

	fun getFootPlantEnabled(): Boolean {
		return footPlantEnabled
	}

	fun setLocalizerMode(value: Boolean) {
		localizerMode = value
		if (value) setFloorLevel(0.0f)
	}

	fun resetBuffer() {
		bufferInvalid = true
	}

	fun getBuffer(): LegTweaksBuffer {
		return bufferHead
	}

	fun setConfig(config: LegTweaksConfig) {
		this.config = config
		updateConfig()
	}

	fun updateConfig() {
		if (config == null) return

		updateHyperParameters(config!!.correctionStrength)
		floorClipEnabled =
			skeleton.humanPoseManager.getToggle(SkeletonConfigToggles.FLOOR_CLIP)
		alwaysUseFloorclip = config!!.alwaysUseFloorclip
		skatingCorrectionEnabled = skeleton.humanPoseManager
			.getToggle(SkeletonConfigToggles.SKATING_CORRECTION)
		toeSnapEnabled =
			skeleton.humanPoseManager.getToggle(SkeletonConfigToggles.TOE_SNAP)
		footPlantEnabled =
			skeleton.humanPoseManager.getToggle(SkeletonConfigToggles.FOOT_PLANT)
	}

	// tweak the position of the legs based on data from the last frames
	fun tweakLegs() {
		// If user doesn't have knees or legtweaks is disabled,
		// don't spend time doing calculations!
		if (!skeleton.hasKneeTrackers && !alwaysUseFloorclip || !enabled) return

		// update the class with the latest data from the skeleton
		// if false is returned something indicated that the legs should not
		// be tweaked
		preUpdate()

		// correct foot rotation's (Foot plant & Toe snap)
		if (footPlantEnabled || toeSnapEnabled) correctFootRotations()

		// push the feet up if needed (Floor clip)
		if (floorClipEnabled && !localizerMode) correctClipping()

		// correct for skating if needed (Skating correction)
		if (skatingCorrectionEnabled) correctSkating()

		// determine if either leg is in a position to activate or
		// deactivate
		// (use the buffer to get the positions before corrections)
		val leftFootDif = FastMath
			.abs(
				bufferHead
					.getLeftFootPosition()
					.minus(leftFootPosition)
					.y
			)
		val rightFootDif = FastMath
			.abs(
				bufferHead
					.getRightFootPosition()
					.minus(rightFootPosition)
					.y
			)

		if (!active && leftFootDif < NEARLY_ZERO) {
			leftLegActive = false
		} else if (active && leftFootDif < NEARLY_ZERO) {
			leftLegActive = true
		}
		if (!active && rightFootDif < NEARLY_ZERO) {
			rightLegActive = false
		} else if (active && rightFootDif < NEARLY_ZERO) {
			rightLegActive = true
		}

		// restore the y positions of inactive legs
		if (!leftLegActive) {
			leftFootPosition = Vector3(
				leftFootPosition.x,
				bufferHead.getLeftFootPosition().y,
				leftFootPosition.z
			)
			leftKneePosition = Vector3(
				leftKneePosition.x,
				bufferHead.getLeftKneePosition().y,
				leftKneePosition.z
			)
		}
		if (!rightLegActive) {
			rightFootPosition = Vector3(
				rightFootPosition.x,
				bufferHead.getRightFootPosition().y,
				rightFootPosition.z
			)
			rightKneePosition = Vector3(
				rightKneePosition.x,
				bufferHead.getRightKneePosition().y,
				rightKneePosition.z
			)
		}

		// calculate the correction for the knees
		if (initialized) solveLowerBody()

		// populate the corrected data into the current frame
		bufferHead
			.setCorrectedPositions(
				leftFootPosition,
				rightFootPosition,
				leftKneePosition,
				rightKneePosition,
				hipPosition
			)

		// Set the corrected positions in the skeleton
		skeleton.computedHipTracker.position = hipPosition
		skeleton.computedLeftKneeTracker.position = leftKneePosition
		skeleton.computedRightKneeTracker.position = rightKneePosition
		skeleton.computedLeftFootTracker.position = leftFootPosition
		skeleton.computedRightFootTracker.position = rightFootPosition
	}

	// update the hyperparameters with the config
	private fun updateHyperParameters(newStrength: Float) {
		LegTweaksBuffer.setSkatingVelocityThreshold(
			getScaledHyperParameter(
				newStrength,
				LegTweaksBuffer.getSkatingVelocityThreshold()
			)
		)
		LegTweaksBuffer.setSkatingAccelerationThreshold(
			getScaledHyperParameter(
				newStrength,
				LegTweaksBuffer.getSkatingAccelerationThreshold()
			)
		)
		currentCorrectionStrength = newStrength
	}

	private fun getScaledHyperParameter(newStrength: Float, currentValue: Float): Float {
		return currentValue - currentCorrectionStrength * MAX_CORRECTION_STRENGTH_DELTA + newStrength * MAX_CORRECTION_STRENGTH_DELTA
	}

	private fun setFloorLevel(floorLevel: Float) {
		this.floorLevel = floorLevel
		hipToFloorDist = hipPosition.y - floorLevel
	}

	// set the vectors in this object to the vectors in the skeleton
	private fun setVectors() {
		// set the positions of the feet and knees to the skeleton's
		// current positions
		hipPosition = skeleton.computedHipTracker.position
		leftKneePosition = skeleton.computedLeftKneeTracker.position
		rightKneePosition = skeleton.computedRightKneeTracker.position
		leftFootPosition = skeleton.computedLeftFootTracker.position
		rightFootPosition = skeleton.computedRightFootTracker.position
		leftFootRotation = skeleton.computedLeftFootTracker.getRotation()
		rightFootRotation = skeleton.computedRightFootTracker.getRotation()

		// get the vector for acceleration of the feet and lower legs
		leftFootAcceleration =
			if (skeleton.leftFootTracker != null) skeleton.leftFootTracker.getAcceleration() else Vector3.NULL
		rightFootAcceleration =
			if (skeleton.rightFootTracker != null) skeleton.rightFootTracker.getAcceleration() else Vector3.NULL
		leftLowerLegAcceleration =
			if (skeleton.leftLowerLegTracker != null) skeleton.leftLowerLegTracker.getAcceleration() else Vector3.NULL
		rightLowerLegAcceleration =
			if (skeleton.rightLowerLegTracker != null) skeleton.rightLowerLegTracker.getAcceleration() else Vector3.NULL
	}

	// updates the object with the latest data from the skeleton
	private fun preUpdate() {
		// populate the vectors with the latest data
		setVectors()

		// if not initialized, we need to calculate some values from this frame
		// to be used later (must happen immediately after reset)
		if (!initialized) {
			setFloorLevel(((leftFootPosition.y + rightFootPosition.y) / 2f + FLOOR_CALIBRATION_OFFSET))

			// invalidate the buffer since the non-initialized output may be
			// very wrong
			bufferInvalid = true
			initialized = true
		}

		// update the foot length
		footLength = skeleton.leftFootNode.localTransform.translation.len()

		// if the user is standing start checking for a good time to enable leg
		// tweaks
		active = isStanding()

		// if the buffer is invalid add all the extra info
		if (bufferInvalid && !localizerMode) {
			bufferHead
				.setPositions(
					leftFootPosition,
					rightFootPosition,
					leftKneePosition,
					rightKneePosition,
					hipPosition
				)

			// if active correct clipping before populating corrected positions
			if (active) {
				correctClipping()
			}

			bufferHead
				.setCorrectedPositions(
					leftFootPosition,
					rightFootPosition,
					leftKneePosition,
					rightKneePosition,
					hipPosition
				)
			bufferHead.setLeftLegState(LegTweaksBuffer.UNLOCKED)
			bufferHead.setRightLegState(LegTweaksBuffer.UNLOCKED)
			bufferInvalid = false
		}

		// update the buffer
		val leftFloorLevel: Float = (
			floorLevel + footLength * getFootOffset(leftFootRotation) -
				currentDisengagementOffset
			)
		val rightFloorLevel: Float = (
			floorLevel + footLength * getFootOffset(rightFootRotation) -
				currentDisengagementOffset
			)
		val leftFootAccel =
			if (skeleton.leftFootTracker != null) leftFootAcceleration else leftLowerLegAcceleration
		val rightFootAccel =
			if (skeleton.rightFootTracker != null) rightFootAcceleration else rightLowerLegAcceleration
		val detectionMode =
			if (skeleton.leftFootTracker != null && skeleton.rightFootTracker != null) LegTweaksBuffer.FOOT_ACCEL else LegTweaksBuffer.ANKLE_ACCEL
		val centerOfMass: Vector3 = computeCenterOfMass()

		// update the buffer head
		bufferHead = LegTweaksBuffer(
			leftFootPosition,
			rightFootPosition,
			leftKneePosition,
			rightKneePosition,
			leftFootRotation,
			rightFootRotation,
			leftFloorLevel,
			rightFloorLevel,
			leftFootAccel,
			rightFootAccel,
			detectionMode,
			hipPosition,
			centerOfMass,
			bufferHead,
			active
		)

		// update the lock duration counters
		updateLockStateCounters()
	}

	// returns true if the foot is clipped and false if it is not
	private fun isClipped(leftOffset: Float, rightOffset: Float): Boolean {
		return (
			leftFootPosition.y < floorLevel + leftOffset * footLength ||
				rightFootPosition.y < floorLevel + rightOffset * footLength
			)
	}

	// corrects the foot position to be above the floor level that is calculated
	// on calibration
	private fun correctClipping() {
		// calculate how angled down the feet are as a scalar value between 0
		// and 1 (0 = flat, 1 = max angle)
		val leftOffset: Float = getFootOffset(leftFootRotation)
		val rightOffset: Float = getFootOffset(rightFootRotation)
		var avgOffset = 0f

		// if there is no clipping, return
		if (!isClipped(leftOffset, rightOffset)) return

		// move the feet to their new positions
		if (leftFootPosition.y
			< floorLevel + footLength * leftOffset -
			currentDisengagementOffset
		) {
			val displacement = abs(
				(
					floorLevel + footLength * leftOffset - leftFootPosition.y -
						currentDisengagementOffset
					)
			)
			leftFootPosition = Vector3(
				leftFootPosition.x,
				leftFootPosition.y + displacement,
				leftFootPosition.z
			)
			leftKneePosition = Vector3(
				leftKneePosition.x,
				leftKneePosition.y + displacement,
				leftKneePosition.z
			)
			avgOffset += displacement
		}

		if (rightFootPosition.y
			< floorLevel + footLength * rightOffset -
			currentDisengagementOffset
		) {
			val displacement = abs(
				(
					floorLevel + footLength * rightOffset - rightFootPosition.y -
						currentDisengagementOffset
					)
			)
			rightFootPosition = Vector3(
				rightFootPosition.x,
				rightFootPosition.y + displacement,
				rightFootPosition.z
			)
			rightKneePosition = Vector3(
				rightKneePosition.x,
				rightKneePosition.y + displacement,
				rightKneePosition.z
			)
			avgOffset += displacement
		}

		hipPosition = Vector3(
			hipPosition.x,
			hipPosition.y + avgOffset / 2 * WAIST_PUSH_WEIGHT,
			hipPosition.z
		)
	}

	// based on the data from the last frame compute a new position that reduces
	// ice skating
	private fun correctSkating() {
		// for either foot that is locked get its position (x and z only we let
		// y move freely) and set it to be there
		val bufPrev = bufferHead.getParent() ?: return

		if (bufferHead.getLeftLegState() == LegTweaksBuffer.LOCKED) {
			leftFootPosition = Vector3(
				bufPrev
					.getLeftFootPositionCorrected()
					.x,
				leftFootPosition.y,
				bufPrev
					.getLeftFootPositionCorrected()
					.z
			)
		}
		if (bufferHead.getRightLegState() == LegTweaksBuffer.LOCKED) {
			rightFootPosition = Vector3(
				bufPrev
					.getRightFootPositionCorrected()
					.x,
				rightFootPosition.y,
				bufPrev
					.getRightFootPositionCorrected()
					.z
			)
		}

		// for either foot that is unlocked get its last position and calculate
		// its position for this frame. the amount of displacement is based on
		// the distance between the last position, the current position, and
		// the hyperparameters
		if (bufferHead.getLeftLegState() == LegTweaksBuffer.UNLOCKED) {
			leftFootPosition = correctUnlockedFootTracker(leftFootPosition, bufPrev.getLeftFootPosition(), bufPrev.getLeftFootPositionCorrected(), bufferHead.getLeftFootVelocity(), leftFramesUnlocked)
		}
		if (bufferHead.getRightLegState() == LegTweaksBuffer.UNLOCKED) {
			rightFootPosition = correctUnlockedFootTracker(rightFootPosition, bufPrev.getRightFootPosition(), bufPrev.getRightFootPositionCorrected(), bufferHead.getRightFootVelocity(), rightFramesUnlocked)
		}
	}

	private fun correctUnlockedFootTracker(footPosition: Vector3, previousFootPosition: Vector3, previousFootPositionCorrected: Vector3, footVelocity: Vector3, framesUnlocked: Int): Vector3 {
		var newFootPosition = footPosition
		var footDif = footPosition
			.minus(previousFootPositionCorrected)
		footDif = Vector3(footDif.x, 0f, footDif.z)

		if (footDif.len() > NEARLY_ZERO) {
			val leftY = footPosition.y
			var temp = previousFootPositionCorrected
			val (x, _, z) = footVelocity

			// first add the difference from the last frame to this frame
			temp = temp
				.minus(
					previousFootPosition
						.minus(footPosition)
				)
			newFootPosition = Vector3(temp.x, leftY, temp.z)

			// if velocity and dif are pointing in the same direction,
			// add a small amount of velocity to the dif
			// else subtract a small amount of velocity from the dif
			// calculate the correction weight.
			// it is also right here where the constant correction is
			// applied
			val weight: Float = calculateCorrectionWeight(
				newFootPosition,
				previousFootPositionCorrected
			)
			val constantCorrection = getConstantCorrectionQuantity(framesUnlocked)
			var newX = newFootPosition.x
			var newZ = newFootPosition.z

			if (x * footDif.x > 0) {
				newX += (
					x * weight +
						(
							constantCorrection
								* (if (x > 0) 1 else -1) /
								bufferHead.getTimeDelta()
							)
					)
			} else if (x * footDif.x < 0) {
				newX -= (
					x * weight +
						(
							constantCorrection
								* (if (x > 0) 1 else -1) /
								bufferHead.getTimeDelta()
							)
					)
			}
			if (z * footDif.z > 0) {
				newZ += (
					z * weight +
						(
							constantCorrection
								* (if (z > 0) 1 else -1) /
								bufferHead.getTimeDelta()
							)
					)
			} else if (z * footDif.z < 0) {
				newZ -= (
					z * weight +
						(
							constantCorrection
								* (if (z > 0) 1 else -1) /
								bufferHead.getTimeDelta()
							)
					)
			}

			// if the foot overshot the target, move it back to the target
			if (checkOverShoot(
					footPosition.x,
					previousFootPositionCorrected.x,
					newX
				)
			) {
				newX = footPosition.x
			}
			if (checkOverShoot(
					footPosition.z,
					previousFootPositionCorrected.z,
					newZ
				)
			) {
				newZ = footPosition.z
			}
			newFootPosition = Vector3(newX, newFootPosition.y, newZ)
		}

		return newFootPosition
	}

	// get the amount of the constant correction to apply.
	private fun getConstantCorrectionQuantity(framesUnlocked: Int): Float {
		return if (framesUnlocked >= CONTINUOUS_CORRECTION_WARMUP) {
			CONTINUOUS_CORRECTION_DIST
		} else {
			(
				CONTINUOUS_CORRECTION_DIST
					* (leftFramesUnlocked.toFloat() / CONTINUOUS_CORRECTION_WARMUP)
				)
		}
	}

	// correct the rotations of the feet
	// this is done by planting the foot better and by snapping the toes to the
	// ground
	private fun correctFootRotations() {
		if (bufferHead.getParent() == null) return

		// boolean for if there is a foot tracker
		val leftFootTracker = skeleton.leftFootTracker != null
		val rightFootTracker = skeleton.rightFootTracker != null

		// get the foot positions
		var leftFootRotation = bufferHead.getLeftFootRotation()
		var rightFootRotation = bufferHead.getRightFootRotation()

		// between maximum correction angle and maximum correction angle delta
		// the values are interpolated
		val kneeAngleL = getXZAmount(leftFootPosition, leftKneePosition)
		val kneeAngleR = getXZAmount(rightFootPosition, rightKneePosition)
		val masterWeightL = getMasterWeight(kneeAngleL)
		val masterWeightR = getMasterWeight(kneeAngleR)

		// corrects rotations when planted firmly on the ground
		if (footPlantEnabled) {
			// the further from the ground the foot is, the less weight it
			// should have
			var weightL = getFootPlantWeight(leftFootPosition)
			var weightR = getFootPlantWeight(rightFootPosition)

			// if foot trackers exist add to the weights
			if (leftFootTracker) {
				weightL *= getRotationalDistanceToPlant(
					leftFootRotation
				)
			}
			if (rightFootTracker) {
				weightR *= getRotationalDistanceToPlant(
					rightFootRotation
				)
			}

			// perform the correction
			leftFootRotation = leftFootRotation
				.interpR(
					isolateYaw(leftFootRotation),
					weightL * masterWeightL
				)
			rightFootRotation = rightFootRotation
				.interpR(
					isolateYaw(rightFootRotation),
					weightR * masterWeightR
				)
		}

		// corrects rotations when the foot is in the air by rotating the foot
		// down so that the toes are touching
		if (toeSnapEnabled && !(leftFootTracker && rightFootTracker)) {
			// this correction step has its own weight vars
			var weightL: Float
			var weightR: Float

			// first compute the angle of the foot
			val angleL = getToeSnapAngle(leftFootPosition)
			val angleR = getToeSnapAngle(rightFootPosition)

			// then compute the weight of the correction
			weightL = getToeSnapWeight(leftFootPosition)
			weightR = getToeSnapWeight(rightFootPosition)

			// depending on the state variables, the correction weights should
			// be clamped
			if (!leftToeTouched) {
				weightL = min(weightL, leftToeAngle)
			}
			if (!rightToeTouched) {
				weightR = min(weightR, rightToeAngle)
			}

			// then slerp the rotation to the new rotation based on the weight
			if (!leftFootTracker) {
				leftFootRotation = leftFootRotation
					.interpR(
						replacePitch(leftFootRotation, -angleL),
						weightL * masterWeightL
					)
			}
			if (!rightFootTracker) {
				rightFootRotation = rightFootRotation
					.interpR(
						replacePitch(rightFootRotation, -angleR),
						weightR * masterWeightR
					)
			}

			// update state variables regarding toe snap
			if (leftFootPosition.y - floorLevel > footLength * MAXIMUM_TOE_DOWN_ANGLE) {
				leftToeTouched = false
				leftToeAngle = weightL
			} else if (leftFootPosition.y - floorLevel <= 0.0f) {
				leftToeTouched = true
				leftToeAngle = 1.0f
			}
			if (rightFootPosition.y - floorLevel > footLength * MAXIMUM_TOE_DOWN_ANGLE) {
				rightToeTouched = false
				rightToeAngle = weightR
			} else if (rightFootPosition.y - floorLevel <= 0.0f) {
				rightToeTouched = true
				rightToeAngle = 1.0f
			}
		}

		// update the foot rotations in the buffer
		bufferHead.setCorrectedRotations(leftFootRotation, rightFootRotation)

		// update the skeleton
		skeleton.computedLeftFootTracker.setRotation(leftFootRotation)
		skeleton.computedRightFootTracker.setRotation(rightFootRotation)
	}

	// returns the length of the xz components of the normalized difference
	// between two vectors
	private fun getXZAmount(vec1: Vector3, vec2: Vector3?): Float {
		val (x, _, z) = vec1.minus(vec2!!).unit()
		return Vector3(x, 0f, z).len()
	}

	// returns a float between 0 and 1 that represents the master weight for
	// foot rotation correciton
	private fun getMasterWeight(kneeAngle: Float): Float {
		val masterWeight = if (kneeAngle > MAXIMUM_CORRECTION_ANGLE &&
			kneeAngle < MAXIMUM_CORRECTION_ANGLE_DELTA
		) {
			(
				1.0f -
					(
						(kneeAngle - MAXIMUM_CORRECTION_ANGLE) /
							(MAXIMUM_CORRECTION_ANGLE_DELTA - MAXIMUM_CORRECTION_ANGLE)
						)
				)
		} else {
			0.0f
		}
		return if (kneeAngle < MAXIMUM_CORRECTION_ANGLE) 1.0f else masterWeight
	}

	// return the weight of the correction for toe snap
	private fun getToeSnapWeight(footPos: Vector3): Float {
		// then compute the weight of the correction
		val weight =
			if (footPos.y - floorLevel > footLength * TOE_SNAP_COOLDOWN) {
				0.0f
			} else {
				(
					1.0f -
						(
							(footPos.y - floorLevel - footLength) /
								(footLength * (TOE_SNAP_COOLDOWN - 1.0f))
							)
					)
			}
		return FastMath.clamp(weight, 0.0f, 1.0f)
	}

	// returns the angle of the foot for toe snap
	private fun getToeSnapAngle(footPos: Vector3): Float {
		val angle = FastMath.clamp(footPos.y - floorLevel, 0.0f, footLength)
		return if (angle > footLength * MAXIMUM_TOE_DOWN_ANGLE) {
			FastMath.asin(
				footLength * MAXIMUM_TOE_DOWN_ANGLE / footLength
			)
		} else {
			FastMath.asin(
				angle / footLength
			)
		}
	}

	// returns the weight for floor plant
	private fun getFootPlantWeight(footPos: Vector3): Float {
		val weight =
			if (footPos.y - floorLevel > ROTATION_CORRECTION_VERTICAL) 0.0f else 1.0f - (footPos.y - floorLevel) / ROTATION_CORRECTION_VERTICAL
		return FastMath.clamp(weight, 0.0f, 1.0f)
	}

	// returns the amount to slerp for foot plant when foot trackers are active
	private fun getRotationalDistanceToPlant(footRot: Quaternion): Float {
		val footRotYaw: Quaternion = isolateYaw(footRot)
		var angle = footRot.angleToR(footRotYaw)
		angle = (angle / (2 * Math.PI)).toFloat()
		angle = FastMath.clamp(
			angle,
			MIN_DISTANCE_FOR_PLANT,
			MAX_DISTANCE_FOR_PLANT
		)
		return (
			1 -
				(
					(angle - MIN_DISTANCE_FOR_PLANT) /
						(MAX_DISTANCE_FOR_PLANT - MIN_DISTANCE_FOR_PLANT)
					)
			)
	}

	// returns true if it is likely the user is standing
	private fun isStanding(): Boolean {
		// if the hip is below the vertical cutoff, user is not standing
		val cutoff = (
			floorLevel +
				hipToFloorDist -
				(hipToFloorDist * STANDING_CUTOFF_VERTICAL)
			)
		if (hipPosition.y < cutoff) {
			currentDisengagementOffset = (
				(
					1 -
						(
							(floorLevel - hipPosition.y) /
								(floorLevel - cutoff)
							)
					) *
					MAX_DISENGAGEMENT_OFFSET
				)
			return false
		}
		currentDisengagementOffset = 0f
		return true
	}

	// move the knees in to a position that is closer to the truth
	private fun solveLowerBody() {
		// calculate the left and right hip nodes in standing space
		val leftHip = hipPosition
		val rightHip = hipPosition

		// before moving the knees back closer to the hip nodes, offset them
		// the same amount the foot trackers where offset
		val leftXDif = leftFootPosition.x - bufferHead.getLeftFootPosition().x
		val rightXDif = rightFootPosition.x - bufferHead.getRightFootPosition().x
		val leftZDif = leftFootPosition.z - bufferHead.getLeftFootPosition().z
		val rightZDif = rightFootPosition.z - bufferHead.getRightFootPosition().z
		val leftX = leftKneePosition.x + leftXDif * KNEE_LATERAL_WEIGHT
		val leftZ = leftKneePosition.z + leftZDif * KNEE_LATERAL_WEIGHT
		val rightX = rightKneePosition.x + rightXDif * KNEE_LATERAL_WEIGHT
		val rightZ = rightKneePosition.z + rightZDif * KNEE_LATERAL_WEIGHT
		leftKneePosition = Vector3(leftX, leftKneePosition.y, leftZ)
		rightKneePosition = Vector3(rightX, rightKneePosition.y, rightZ)

		// calculate the bone distances
		val leftKneeHip = bufferHead.getLeftKneePosition().minus(leftHip).len()
		val rightKneeHip = bufferHead.getRightKneePosition().minus(rightHip).len()
		val leftKneeHipNew = leftKneePosition.minus(leftHip).len()
		val rightKneeHipNew = rightKneePosition.minus(rightHip).len()
		val leftKneeOffset = leftKneeHipNew - leftKneeHip
		val rightKneeOffset = rightKneeHipNew - rightKneeHip

		// get the vector from the hip to the knee
		val leftKneeVector = leftKneePosition
			.minus(leftHip)
			.unit()
			.times(leftKneeOffset * KNEE_CORRECTION_WEIGHT)
		val rightKneeVector = rightKneePosition
			.minus(rightHip)
			.unit()
			.times(rightKneeOffset * KNEE_CORRECTION_WEIGHT)

		// correct the knees
		leftKneePosition = leftKneePosition.minus(leftKneeVector)
		rightKneePosition = rightKneePosition.minus(rightKneeVector)
	}

	private fun getFootOffset(footRotation: Quaternion): Float {
		val offset: Float = computeUnitVector(footRotation).y
		return FastMath.clamp(offset, 0f, DYNAMIC_DISPLACEMENT_CUTOFF)
	}

	// calculate the weight of foot correction
	private fun calculateCorrectionWeight(
		foot: Vector3,
		footCorrected: Vector3,
	): Float {
		var footDif = foot.minus(footCorrected)
		footDif = Vector3(footDif.x, 0f, footDif.z)
		if (footDif.len() < MIN_ACCEPTABLE_ERROR) {
			return CORRECTION_WEIGHT_MIN
		} else if (footDif.len() > MAX_ACCEPTABLE_ERROR) {
			return CORRECTION_WEIGHT_MAX
		}
		return (
			CORRECTION_WEIGHT_MIN +
				(footDif.len() - MIN_ACCEPTABLE_ERROR) /
				(MAX_ACCEPTABLE_ERROR - MIN_ACCEPTABLE_ERROR) *
				(CORRECTION_WEIGHT_MAX - CORRECTION_WEIGHT_MIN)
			)
	}

	// calculate the center of mass of the user for the current frame
	// returns a vector representing the center of mass position
	private fun computeCenterOfMass(): Vector3 {
		// check if arm data is available
		val armsAvailable = (
			skeleton.hasLeftArmTracker &&
				skeleton.hasRightArmTracker
			)
		var centerOfMass = Vector3(0f, 0f, 0f)

		// compute the center of mass of smaller body parts and then sum them up
		// with their respective weights
		val head = skeleton.headNode.worldTransform.translation
		val thorax: Vector3 =
			getCenterOfJoint(skeleton.chestNode, skeleton.upperChestNode)
		val abdomen = skeleton.waistNode.worldTransform.translation
		val pelvis = skeleton.hipNode.worldTransform.translation
		val leftCalf: Vector3 =
			getCenterOfJoint(skeleton.leftAnkleNode, skeleton.leftKneeNode)
		val rightCalf: Vector3 =
			getCenterOfJoint(skeleton.rightAnkleNode, skeleton.rightKneeNode)
		val leftThigh: Vector3 =
			getCenterOfJoint(skeleton.leftKneeNode, skeleton.leftHipNode)
		val rightThigh: Vector3 =
			getCenterOfJoint(skeleton.rightKneeNode, skeleton.rightHipNode)
		centerOfMass = centerOfMass.plus(head.times(HEAD_MASS))
		centerOfMass = centerOfMass.plus(thorax.times(THORAX_MASS))
		centerOfMass = centerOfMass.plus(abdomen.times(ABDOMEN_MASS))
		centerOfMass = centerOfMass.plus(pelvis.times(PELVIS_MASS))
		centerOfMass = centerOfMass.plus(leftCalf.times(LEG_AND_FOOT_MASS))
		centerOfMass = centerOfMass.plus(rightCalf.times(LEG_AND_FOOT_MASS))
		centerOfMass = centerOfMass.plus(leftThigh.times(THIGH_MASS))
		centerOfMass = centerOfMass.plus(rightThigh.times(THIGH_MASS))

		if (armsAvailable) {
			val leftUpperArm: Vector3 = getCenterOfJoint(
				skeleton.leftElbowNode,
				skeleton.leftShoulderTailNode
			)
			val rightUpperArm: Vector3 = getCenterOfJoint(
				skeleton.rightElbowNode,
				skeleton.rightShoulderTailNode
			)
			val leftForearm: Vector3 =
				getCenterOfJoint(skeleton.leftElbowNode, skeleton.leftHandNode)
			val rightForearm: Vector3 = getCenterOfJoint(
				skeleton.rightElbowNode,
				skeleton.rightHandNode
			)
			centerOfMass =
				centerOfMass.plus(leftUpperArm.times(UPPER_ARM_MASS))
			centerOfMass =
				centerOfMass.plus(rightUpperArm.times(UPPER_ARM_MASS))
			centerOfMass =
				centerOfMass.plus(leftForearm.times(FOREARM_AND_HAND_MASS))
			centerOfMass =
				centerOfMass.plus(rightForearm.times(FOREARM_AND_HAND_MASS))
		} else {
			// if the arms are not available put them slightly in front
			// of the upper chest.
			val chestUnitVector: Vector3 = computeUnitVector(
				skeleton.upperChestNode.worldTransform.rotation
			)
			val armLocation =
				abdomen.plus(chestUnitVector.times(DEFAULT_ARM_DISTANCE))
			centerOfMass =
				centerOfMass.plus(armLocation.times(UPPER_ARM_MASS * 2.0f))
			centerOfMass =
				centerOfMass.plus(armLocation.times(FOREARM_AND_HAND_MASS * 2.0f))
		}

		// finally translate in to tracker space
		centerOfMass = hipPosition
			.plus(
				centerOfMass.minus(skeleton.trackerHipNode.worldTransform.translation)
			)
		return centerOfMass
	}

	// get the center of two joints
	private fun getCenterOfJoint(node1: TransformNode, node2: TransformNode): Vector3 {
		return node1
			.worldTransform
			.translation
			.plus(node2.worldTransform.translation)
			.times(0.5f)
	}

	// update counters for the lock state of the feet
	private fun updateLockStateCounters() {
		if (bufferHead.getLeftLegState() == LegTweaksBuffer.LOCKED) {
			leftFramesUnlocked = 0
			leftFramesLocked++
		} else {
			leftFramesLocked = 0
			leftFramesUnlocked++
		}
		if (bufferHead.getRightLegState() == LegTweaksBuffer.LOCKED) {
			rightFramesUnlocked = 0
			rightFramesLocked++
		} else {
			rightFramesLocked = 0
			rightFramesUnlocked++
		}
	}

	// remove the x and z components of the given quaternion
	private fun isolateYaw(quaternion: Quaternion): Quaternion {
		return Quaternion(
			quaternion.w,
			0f,
			quaternion.y,
			0f
		)
	}

	// return a quaternion that has been rotated by the new pitch amount
	private fun replacePitch(quaternion: Quaternion, newPitch: Float): Quaternion {
		val (_, _, y, z) = quaternion.toEulerAngles(EulerOrder.YZX)
		val newAngs = EulerAngles(
			EulerOrder.YZX,
			newPitch,
			y,
			z
		)
		return newAngs.toQuaternion()
	}

	// check if correction overshot the true value
	// returns true if overshot
	private fun checkOverShoot(
		trueVal: Float,
		valBefore: Float,
		valAfter: Float,
	): Boolean {
		return (trueVal - valBefore) * (trueVal - valAfter) < 0
	}

	// get the unit vector of the given rotation
	private fun computeUnitVector(quaternion: Quaternion): Vector3 {
		return quaternion.toMatrix().z.unit()
	}
}
