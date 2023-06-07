package dev.slimevr.tracking.processor.skeleton;

import com.jme3.math.FastMath;
import dev.slimevr.config.LegTweaksConfig;
import dev.slimevr.tracking.processor.TransformNode;
import dev.slimevr.tracking.processor.config.SkeletonConfigToggles;
import io.github.axisangles.ktmath.EulerAngles;
import io.github.axisangles.ktmath.EulerOrder;
import io.github.axisangles.ktmath.Quaternion;
import io.github.axisangles.ktmath.Vector3;


public class LegTweaks {
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

	// hyperparameters (clip correction)
	static float DYNAMIC_DISPLACEMENT_CUTOFF = 1.0f;
	private static final float FLOOR_CALIBRATION_OFFSET = 0.015f;

	// hyperparameters (skating correction)
	private static final float MIN_ACCEPTABLE_ERROR = 0.01f;
	private static final float MAX_ACCEPTABLE_ERROR = 0.05f;
	private static final float CORRECTION_WEIGHT_MIN = 0.55f;
	private static final float CORRECTION_WEIGHT_MAX = 0.70f;
	private static final float CONTINUOUS_CORRECTION_DIST = 0.5f;
	private static final int CONTINUOUS_CORRECTION_WARMUP = 175;

	// hyperparameters (knee / hip correction)
	private static final float KNEE_CORRECTION_WEIGHT = 0.00f;
	private static final float KNEE_LATERAL_WEIGHT = 0.8f;
	private static final float WAIST_PUSH_WEIGHT = 0.2f;

	// hyperparameters (COM calculation)
	// mass percentages of the body
	private static final float HEAD_MASS = 0.082f;
	private static final float CHEST_MASS = 0.25f;
	private static final float WAIST_MASS = 0.209f;
	private static final float THIGH_MASS = 0.128f;
	private static final float CALF_MASS = 0.0535f;
	private static final float UPPER_ARM_MASS = 0.031f;
	private static final float FOREARM_MASS = 0.017f;

	// hyperparameters (rotation correction)
	private static final float ROTATION_CORRECTION_VERTICAL = 0.1f;
	private static final float MAXIMUM_CORRECTION_ANGLE = 0.4f;
	private static final float MAXIMUM_CORRECTION_ANGLE_DELTA = 0.7f;
	private static final float MAXIMUM_TOE_DOWN_ANGLE = 0.8f;
	private static final float TOE_SNAP_COOLDOWN = 3.0f;
	private static final float MIN_DISTANCE_FOR_PLANT = 0.025f;
	private static final float MAX_DISTANCE_FOR_PLANT = 0.075f;

	// hyperparameters (misc)
	static final float NEARLY_ZERO = 0.001f;
	private static final float STANDING_CUTOFF_VERTICAL = 0.65f;
	private static final float MAX_DISENGAGEMENT_OFFSET = 0.30f;
	private static final float DEFAULT_ARM_DISTANCE = 0.15f;
	private static final float MAX_CORRECTION_STRENGTH_DELTA = 1.0f;

	// state variables
	private float floorLevel;
	private float hipToFloorDist;
	private float currentDisengagementOffset = 0.0f;
	private float footLength = 0.0f;
	private static float currentCorrectionStrength = 0.3f; // default value
	private boolean initialized = true;
	private boolean enabled = true; // master switch
	private boolean floorclipEnabled = false;
	private boolean skatingCorrectionEnabled = false;
	private boolean toeSnapEnabled = false;
	private boolean footPlantEnabled = false;
	private boolean active = false;
	private boolean rightLegActive = false;
	private boolean leftLegActive = false;
	private int leftFramesLocked = 0;
	private int rightFramesLocked = 0;
	private int leftFramesUnlocked = 0;
	private int rightFramesUnlocked = 0;
	private float leftToeAngle = 0.0f;
	private boolean leftToeTouched = false;
	private float rightToeAngle = 0.0f;
	private boolean rightToeTouched = false;

	// skeleton and config
	private final HumanSkeleton skeleton;
	private LegTweaksConfig config;

	// leg data
	private Vector3 leftFootPosition = Vector3.Companion.getNULL();
	private Vector3 rightFootPosition = Vector3.Companion.getNULL();
	private Vector3 leftKneePosition = Vector3.Companion.getNULL();
	private Vector3 rightKneePosition = Vector3.Companion.getNULL();
	private Vector3 hipPosition = Vector3.Companion.getNULL();
	private Quaternion leftFootRotation = Quaternion.Companion.getIDENTITY();
	private Quaternion rightFootRotation = Quaternion.Companion.getIDENTITY();

	private Vector3 leftFootAcceleration = Vector3.Companion.getNULL();
	private Vector3 rightFootAcceleration = Vector3.Companion.getNULL();
	private Vector3 leftLowerLegAcceleration = Vector3.Companion.getNULL();
	private Vector3 rightLowerLegAcceleration = Vector3.Companion.getNULL();

	// buffer for holding previous frames of data
	private LegTweakBuffer bufferHead = new LegTweakBuffer();
	private boolean bufferInvalid = true;

	public LegTweaks(HumanSkeleton skeleton) {
		this.skeleton = skeleton;
	}

	public LegTweaks(HumanSkeleton skeleton, LegTweaksConfig config) {
		this.skeleton = skeleton;
		// set all the hyperparameters from the config
		this.config = config;
		updateConfig();
	}

	public Vector3 getLeftFootPosition() {
		return leftFootPosition;
	}

	public void setLeftFootPosition(Vector3 leftFootPosition) {
		this.leftFootPosition = leftFootPosition;
	}

	public void setLeftFootRotation(Quaternion leftFootRotation) {
		this.leftFootRotation = leftFootRotation;
	}

	public Vector3 getRightFootPosition() {
		return rightFootPosition;
	}

	public void setRightFootPosition(Vector3 rightFootPosition) {
		this.rightFootPosition = rightFootPosition;
	}

	public void setRightFootRotation(Quaternion rightFootRotation) {
		this.rightFootRotation = rightFootRotation;
	}

	public Vector3 getLeftKneePosition() {
		return leftKneePosition;
	}

	public void setLeftKneePosition(Vector3 leftKneePosition) {
		this.leftKneePosition = leftKneePosition;
	}

	public Vector3 getRightKneePosition() {
		return rightKneePosition;
	}

	public void setRightKneePosition(Vector3 rightKneePosition) {
		this.rightKneePosition = rightKneePosition;
	}

	public Vector3 getHipPosition() {
		return hipPosition;
	}

	public void setHipPosition(Vector3 hipPosition) {
		this.hipPosition = hipPosition;
	}

	public double getFloorLevel() {
		return floorLevel;
	}

	public void setFloorLevel(float floorLevel) {
		this.floorLevel = floorLevel;
	}

	public void resetFloorLevel() {
		this.initialized = false;
	}

	public boolean getActive() {
		return this.active;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setFloorclipEnabled(boolean floorclipEnabled) {
		this.floorclipEnabled = floorclipEnabled;

		// reset the buffer
		this.bufferHead = new LegTweakBuffer();
		this.bufferInvalid = true;
	}

	public void setSkatingReductionEnabled(boolean skatingCorrectionEnabled) {
		this.skatingCorrectionEnabled = skatingCorrectionEnabled;

		// reset the buffer
		this.bufferHead = new LegTweakBuffer();
		this.bufferInvalid = true;
	}

	public void setToeSnapEnabled(boolean val) {
		this.toeSnapEnabled = val;
	}

	public void setFootPlantEnabled(boolean val) {
		this.footPlantEnabled = val;
	}

	public boolean getEnabled() {
		return this.enabled;
	}

	public boolean getFloorclipEnabled() {
		return this.floorclipEnabled;
	}

	public boolean getSkatingReductionEnabled() {
		return this.skatingCorrectionEnabled;
	}

	public boolean getToeSnapEnabled() {
		return this.toeSnapEnabled;
	}

	public boolean getFootPlantEnabled() {
		return this.footPlantEnabled;
	}

	public void resetBuffer() {
		bufferInvalid = true;
	}

	public void setConfig(LegTweaksConfig config) {
		this.config = config;
		updateConfig();
	}

	public void updateConfig() {
		LegTweaks.updateHyperParameters(config.correctionStrength);

		floorclipEnabled = skeleton.humanPoseManager.getToggle(SkeletonConfigToggles.FLOOR_CLIP);
		skatingCorrectionEnabled = skeleton.humanPoseManager
			.getToggle(SkeletonConfigToggles.SKATING_CORRECTION);
		toeSnapEnabled = skeleton.humanPoseManager.getToggle(SkeletonConfigToggles.TOE_SNAP);
		footPlantEnabled = skeleton.humanPoseManager.getToggle(SkeletonConfigToggles.FOOT_PLANT);
	}

	// update the hyperparameters with the config
	public static void updateHyperParameters(float newStrength) {
		LegTweakBuffer.SKATING_VELOCITY_THRESHOLD = getScaledHyperParameter(
			newStrength,
			LegTweakBuffer.SKATING_VELOCITY_THRESHOLD
		);
		LegTweakBuffer.SKATING_ACCELERATION_THRESHOLD = getScaledHyperParameter(
			newStrength,
			LegTweakBuffer.SKATING_ACCELERATION_THRESHOLD
		);
		currentCorrectionStrength = newStrength;
	}

	public static float getScaledHyperParameter(float newStrength, float currentValue) {
		return (currentValue - (currentCorrectionStrength * MAX_CORRECTION_STRENGTH_DELTA))
			+ (newStrength * MAX_CORRECTION_STRENGTH_DELTA);
	}

	// set the vectors in this object to the vectors in the skeleton
	private void setVectors() {
		// set the positions of the feet and knees to the skeleton's
		// current positions
		hipPosition = skeleton.computedHipTracker.getPosition();

		leftKneePosition = skeleton.computedLeftKneeTracker.getPosition();
		rightKneePosition = skeleton.computedRightKneeTracker.getPosition();

		leftFootPosition = skeleton.computedLeftFootTracker.getPosition();
		rightFootPosition = skeleton.computedRightFootTracker.getPosition();
		leftFootRotation = skeleton.computedLeftFootTracker.getRotation();
		rightFootRotation = skeleton.computedRightFootTracker.getRotation();

		// get the vector for acceleration of the feet and knees
		// (if feet are not available, fallback to 6 tracker mode)
		if (skeleton.leftFootTracker != null && skeleton.rightFootTracker != null) {
			leftFootAcceleration = skeleton.leftFootTracker.getAcceleration();
			rightFootAcceleration = skeleton.rightFootTracker.getAcceleration();
		} else {
			leftFootAcceleration = Vector3.Companion.getNULL();
			rightFootAcceleration = Vector3.Companion.getNULL();
		}

		if (skeleton.leftLowerLegTracker != null && skeleton.rightLowerLegTracker != null) {
			leftLowerLegAcceleration = skeleton.leftLowerLegTracker.getAcceleration();
			rightLowerLegAcceleration = skeleton.rightLowerLegTracker.getAcceleration();
		} else {
			leftLowerLegAcceleration = Vector3.Companion.getNULL();
			rightLowerLegAcceleration = Vector3.Companion.getNULL();
		}
	}

	// updates the object with the latest data from the skeleton
	private boolean preUpdate() {
		// populate the vectors with the latest data
		setVectors();

		// if not initialized, we need to calculate some values from this frame
		// to be used later (must happen immediately after reset)
		if (!initialized) {
			floorLevel = (leftFootPosition.getY() + rightFootPosition.getY()) / 2f
				+ FLOOR_CALIBRATION_OFFSET;
			hipToFloorDist = hipPosition.getY() - floorLevel;

			// invalidate the buffer since the non-initialized output may be
			// very wrong
			bufferInvalid = true;
			initialized = true;
		}

		// update the foot length
		footLength = skeleton.leftFootNode.getLocalTransform().getTranslation().len();

		// if the user is standing start checking for a good time to enable leg
		// tweaks
		active = isStanding();

		// if the buffer is invalid add all the extra info
		if (bufferInvalid) {
			bufferHead.setLeftFootPositionCorrected(leftFootPosition);
			bufferHead.setRightFootPositionCorrected(rightFootPosition);
			bufferHead.setLeftKneePositionCorrected(leftKneePosition);
			bufferHead.setRightKneePositionCorrected(rightKneePosition);
			bufferHead.setHipPositionCorrected(hipPosition);
			bufferHead.setLeftFootPosition(leftFootPosition);
			bufferHead.setRightFootPosition(rightFootPosition);
			bufferHead.setLeftKneePosition(leftKneePosition);
			bufferHead.setRightKneePosition(rightKneePosition);
			bufferHead.setHipPosition(hipPosition);
			bufferHead.setLeftLegState(LegTweakBuffer.UNLOCKED);
			bufferHead.setRightLegState(LegTweakBuffer.UNLOCKED);

			// if the system is active, populate the buffer with corrected floor
			// clip feet positions
			if (active && isStanding()) {
				correctClipping();
				bufferHead.setLeftFootPositionCorrected(leftFootPosition);
				bufferHead.setRightFootPositionCorrected(rightFootPosition);
			}

			bufferInvalid = false;
		}

		// update the buffer
		LegTweakBuffer currentFrame = new LegTweakBuffer();
		currentFrame.setLeftFootPosition(leftFootPosition);
		currentFrame.setLeftFootRotation(leftFootRotation);
		currentFrame.setLeftKneePosition(leftKneePosition);
		currentFrame.setRightFootPosition(rightFootPosition);
		currentFrame.setRightFootRotation(rightFootRotation);
		currentFrame.setRightKneePosition(rightKneePosition);
		currentFrame.setHipPosition(hipPosition);
		currentFrame.setCenterOfMass(computeCenterOfMass());

		currentFrame
			.setLeftFloorLevel(
				(floorLevel + (footLength * getLeftFootOffset()))
					- currentDisengagementOffset
			);

		currentFrame
			.setRightFloorLevel(
				(floorLevel + (footLength * getRightFootOffset()))
					- currentDisengagementOffset
			);

		// put the acceleration vector that is applicable to the tracker
		// quantity in the buffer
		// (if feet are not available, fall back to 6 tracker mode)
		if (skeleton.leftFootTracker != null && skeleton.rightFootTracker != null) {
			currentFrame.setLeftFootAcceleration(leftFootAcceleration);
			currentFrame.setRightFootAcceleration(rightFootAcceleration);
			currentFrame.setDetectionMode(LegTweakBuffer.FOOT_ACCEL);
		} else if (skeleton.leftLowerLegTracker != null && skeleton.rightLowerLegTracker != null) {
			currentFrame.setLeftFootAcceleration(leftLowerLegAcceleration);
			currentFrame.setRightFootAcceleration(rightLowerLegAcceleration);
			currentFrame.setDetectionMode(LegTweakBuffer.ANKLE_ACCEL);
		}

		// update the buffer head and compute the current state of the legs
		currentFrame.setParent(bufferHead);
		this.bufferHead = currentFrame;
		this.bufferHead.calculateFootAttributes(active);

		// update the lock duration counters
		updateLockStateCounters();

		return true;
	}

	// tweak the position of the legs based on data from the last frames
	public void tweakLegs() {
		// If user doesn't have knees or legtweaks is disabled,
		// don't spend time doing calculations!
		if (!skeleton.hasKneeTrackers || !enabled)
			return;

		// update the class with the latest data from the skeleton
		// if false is returned something indicated that the legs should not
		// be tweaked
		if (!preUpdate())
			return;

		// correct foot rotation's (Foot plant & Toe snap)
		if (footPlantEnabled || toeSnapEnabled)
			correctFootRotations();

		// push the feet up if needed (Floor clip)
		if (floorclipEnabled)
			correctClipping();

		// correct for skating if needed (Skating correction)
		if (skatingCorrectionEnabled)
			correctSkating();

		// determine if either leg is in a position to activate or
		// deactivate
		// (use the buffer to get the positions before corrections)
		float leftFootDif = FastMath
			.abs(
				bufferHead
					.getLeftFootPosition()
					.minus(leftFootPosition)
					.getY()
			);

		float rightFootDif = FastMath
			.abs(
				bufferHead
					.getRightFootPosition()
					.minus(rightFootPosition)
					.getY()
			);

		if (!active && leftFootDif < NEARLY_ZERO) {
			leftLegActive = false;
		} else if (active && leftFootDif < NEARLY_ZERO) {
			leftLegActive = true;
		}

		if (!active && rightFootDif < NEARLY_ZERO) {
			rightLegActive = false;
		} else if (active && rightFootDif < NEARLY_ZERO) {
			rightLegActive = true;
		}

		// restore the y positions of inactive legs
		if (!leftLegActive) {
			leftFootPosition = new Vector3(
				leftFootPosition.getX(),
				bufferHead.getLeftFootPosition().getY(),
				leftFootPosition.getZ()
			);
			leftKneePosition = new Vector3(
				leftKneePosition.getX(),
				bufferHead.getLeftKneePosition().getY(),
				leftKneePosition.getZ()
			);
		}

		if (!rightLegActive) {
			rightFootPosition = new Vector3(
				rightFootPosition.getX(),
				bufferHead.getRightFootPosition().getY(),
				rightFootPosition.getZ()
			);
			rightKneePosition = new Vector3(
				rightKneePosition.getX(),
				bufferHead.getRightKneePosition().getY(),
				rightKneePosition.getZ()
			);
		}

		// calculate the correction for the knees
		if (initialized)
			solveLowerBody();

		// populate the corrected data into the current frame
		this.bufferHead.setLeftFootPositionCorrected(leftFootPosition);
		this.bufferHead.setRightFootPositionCorrected(rightFootPosition);
		this.bufferHead.setLeftKneePositionCorrected(leftKneePosition);
		this.bufferHead.setRightKneePositionCorrected(rightKneePosition);
		this.bufferHead.setHipPositionCorrected(hipPosition);

		// Set the corrected positions in the skeleton
		skeleton.computedHipTracker.setPosition(hipPosition);
		skeleton.computedLeftKneeTracker.setPosition(leftKneePosition);
		skeleton.computedRightKneeTracker.setPosition(rightKneePosition);
		skeleton.computedLeftFootTracker.setPosition(leftFootPosition);
		skeleton.computedRightFootTracker.setPosition(rightFootPosition);
	}

	// returns true if the foot is clipped and false if it is not
	public boolean isClipped(float leftOffset, float rightOffset) {
		return (leftFootPosition.getY() < floorLevel + leftOffset
			|| rightFootPosition.getY() < floorLevel + rightOffset);
	}

	// corrects the foot position to be above the floor level that is calculated
	// on calibration
	private void correctClipping() {
		// calculate how angled down the feet are as a scalar value between 0
		// and 1 (0 = flat, 1 = max angle)
		float leftOffset = getLeftFootOffset();
		float rightOffset = getRightFootOffset();
		float avgOffset = 0;

		// if there is no clipping, return
		if (!isClipped(leftOffset, rightOffset))
			return;

		// move the feet to their new positions
		if (
			leftFootPosition.getY()
				< (floorLevel + (footLength * leftOffset))
					- currentDisengagementOffset
		) {
			float displacement = Math
				.abs(
					floorLevel
						+ (footLength * leftOffset)
						- leftFootPosition.getY()
						- currentDisengagementOffset
				);

			leftFootPosition = new Vector3(
				leftFootPosition.getX(),
				leftFootPosition.getY() + displacement,
				leftFootPosition.getZ()
			);
			leftKneePosition = new Vector3(
				leftKneePosition.getX(),
				leftKneePosition.getY() + displacement,
				leftKneePosition.getZ()
			);
			avgOffset += displacement;
		}

		if (
			rightFootPosition.getY()
				< (floorLevel + (footLength * rightOffset))
					- currentDisengagementOffset
		) {
			float displacement = Math
				.abs(
					floorLevel
						+ (footLength * rightOffset)
						- rightFootPosition.getY()
						- currentDisengagementOffset
				);

			rightFootPosition = new Vector3(
				rightFootPosition.getX(),
				rightFootPosition.getY() + displacement,
				rightFootPosition.getZ()
			);
			rightKneePosition = new Vector3(
				rightKneePosition.getX(),
				rightKneePosition.getY() + displacement,
				rightKneePosition.getZ()
			);
			avgOffset += displacement;
		}

		hipPosition = new Vector3(
			hipPosition.getX(),
			hipPosition.getY() + ((avgOffset / 2) * WAIST_PUSH_WEIGHT),
			hipPosition.getZ()
		);
	}

	// based on the data from the last frame compute a new position that reduces
	// ice skating
	private void correctSkating() {
		// for either foot that is locked get its position (x and z only we let
		// y move freely) and set it to be there
		if (bufferHead.getLeftLegState() == LegTweakBuffer.LOCKED) {
			leftFootPosition = new Vector3(
				bufferHead
					.getParent()
					.getLeftFootPositionCorrected()
					.getX(),
				leftFootPosition.getY(),
				bufferHead
					.getParent()
					.getLeftFootPositionCorrected()
					.getZ()
			);
		}

		if (bufferHead.getRightLegState() == LegTweakBuffer.LOCKED) {
			rightFootPosition = new Vector3(
				bufferHead
					.getParent()
					.getRightFootPositionCorrected()
					.getX(),
				rightFootPosition.getY(),
				bufferHead
					.getParent()
					.getRightFootPositionCorrected()
					.getZ()
			);
		}

		// for either foot that is unlocked get its last position and calculate
		// its position for this frame. the amount of displacement is based on
		// the distance between the last position, the current position, and
		// the hyperparameters
		if (bufferHead.getLeftLegState() == LegTweakBuffer.UNLOCKED)
			correctUnlockedLeftFootTracker();
		if (bufferHead.getRightLegState() == LegTweakBuffer.UNLOCKED)
			correctUnlockedRightFootTracker();
	}

	private void correctUnlockedLeftFootTracker() {
		Vector3 leftFootDif = leftFootPosition
			.minus(bufferHead.getParent().getLeftFootPositionCorrected());
		leftFootDif = new Vector3(leftFootDif.getX(), 0f, leftFootDif.getZ());

		if (leftFootDif.len() > NEARLY_ZERO) {
			float leftY = leftFootPosition.getY();

			Vector3 temp = bufferHead.getParent().getLeftFootPositionCorrected();
			Vector3 velocity = bufferHead.getLeftFootVelocity();

			// first add the difference from the last frame to this frame
			temp = temp
				.minus(
					bufferHead
						.getParent()
						.getLeftFootPosition()
						.minus(bufferHead.getLeftFootPosition())
				);

			leftFootPosition = new Vector3(temp.getX(), leftY, temp.getZ());

			// if velocity and dif are pointing in the same direction,
			// add a small amount of velocity to the dif
			// else subtract a small amount of velocity from the dif
			// calculate the correction weight.
			// it is also right here where the constant correction is
			// applied
			float weight = calculateCorrectionWeight(
				leftFootPosition,
				bufferHead.getParent().getLeftFootPositionCorrected()
			);

			float leftX = leftFootPosition.getX();
			float leftZ = leftFootPosition.getZ();
			if (velocity.getX() * leftFootDif.getX() > 0) {
				leftX += (velocity.getX() * weight)
					+ (getConstantCorrectionQuantityLeft()
						* (velocity.getX() > 0 ? 1 : -1)
						/ bufferHead.getTimeDelta());
			} else if (velocity.getX() * leftFootDif.getX() < 0) {
				leftX -= (velocity.getX() * weight)
					+ (getConstantCorrectionQuantityLeft()
						* (velocity.getX() > 0 ? 1 : -1)
						/ bufferHead.getTimeDelta());
			}

			if (velocity.getZ() * leftFootDif.getZ() > 0) {
				leftZ += (velocity.getZ() * weight)
					+ (getConstantCorrectionQuantityLeft()
						* (velocity.getZ() > 0 ? 1 : -1)
						/ bufferHead.getTimeDelta());
			} else if (velocity.getZ() * leftFootDif.getZ() < 0) {
				leftZ -= (velocity.getZ() * weight)
					+ (getConstantCorrectionQuantityLeft()
						* (velocity.getZ() > 0 ? 1 : -1)
						/ bufferHead.getTimeDelta());
			}

			// if the foot overshot the target, move it back to the target
			if (
				checkOverShoot(
					this.bufferHead.getLeftFootPosition().getX(),
					this.bufferHead.getParent().getLeftFootPositionCorrected().getX(),
					leftX
				)
			) {
				leftX = bufferHead.getLeftFootPosition().getX();
			}

			if (
				checkOverShoot(
					this.bufferHead.getLeftFootPosition().getZ(),
					this.bufferHead.getParent().getLeftFootPositionCorrected().getZ(),
					leftZ
				)
			) {
				leftZ = bufferHead.getLeftFootPosition().getZ();
			}

			leftFootPosition = new Vector3(leftX, leftFootPosition.getY(), leftZ);
		}
	}

	private void correctUnlockedRightFootTracker() {
		Vector3 rightFootDif = rightFootPosition
			.minus(bufferHead.getParent().getRightFootPositionCorrected());
		rightFootDif = new Vector3(rightFootDif.getX(), 0f, rightFootDif.getZ());

		if (rightFootDif.len() > NEARLY_ZERO) {
			float rightY = rightFootPosition.getY();

			Vector3 temp = bufferHead
				.getParent()
				.getRightFootPositionCorrected();
			Vector3 velocity = bufferHead.getRightFootVelocity();

			// first add the difference from the last frame to this frame
			temp = temp
				.minus(
					bufferHead
						.getParent()
						.getRightFootPosition()
						.minus(bufferHead.getRightFootPosition())
				);

			rightFootPosition = new Vector3(temp.getX(), rightY, temp.getZ());

			// if velocity and dif are pointing in the same direction,
			// add a small amount of velocity to the dif
			// else subtract a small amount of velocity from the dif
			// calculate the correction weight
			float weight = calculateCorrectionWeight(
				rightFootPosition,
				bufferHead.getParent().getRightFootPositionCorrected()
			);

			float rightX = rightFootPosition.getX();
			float rightZ = rightFootPosition.getZ();
			if (velocity.getX() * rightFootDif.getX() > 0) {
				rightX += (velocity.getX() * weight)
					+ (getConstantCorrectionQuantityRight()
						* (velocity.getX() > 0 ? 1 : -1)
						/ bufferHead.getTimeDelta());
			} else if (velocity.getX() * rightFootDif.getX() < 0) {
				rightX -= (velocity.getX() * weight)
					+ (getConstantCorrectionQuantityRight()
						* (velocity.getX() > 0 ? 1 : -1)
						/ bufferHead.getTimeDelta());
			}

			if (velocity.getZ() * rightFootDif.getZ() > 0) {
				rightZ += (velocity.getZ() * weight)
					+ (getConstantCorrectionQuantityRight()
						* (velocity.getZ() > 0 ? 1 : -1)
						/ bufferHead.getTimeDelta());
			} else if (velocity.getZ() * rightFootDif.getZ() < 0) {
				rightZ -= (velocity.getZ() * weight)
					+ (getConstantCorrectionQuantityRight()
						* (velocity.getZ() > 0 ? 1 : -1)
						/ bufferHead.getTimeDelta());
			}

			// if the foot overshot the target, move it back to the target
			if (
				checkOverShoot(
					this.bufferHead.getRightFootPosition().getX(),
					this.bufferHead.getParent().getRightFootPositionCorrected().getX(),
					rightX
				)
			) {
				rightX = bufferHead.getRightFootPosition().getX();
			}

			if (
				checkOverShoot(
					this.bufferHead.getRightFootPosition().getZ(),
					this.bufferHead.getParent().getRightFootPositionCorrected().getZ(),
					rightZ
				)
			) {
				rightZ = bufferHead.getRightFootPosition().getZ();
			}

			rightFootPosition = new Vector3(
				rightX,
				rightFootPosition.getY(),
				rightZ
			);
		}
	}

	// correct the rotations of the feet
	// this is done by planting the foot better and by snapping the toes to the
	// ground
	private void correctFootRotations() {
		// null check's
		if (bufferHead == null || bufferHead.getParent() == null)
			return;

		// boolean for if there is a foot tracker
		boolean leftFootTracker = skeleton.leftFootTracker != null;
		boolean rightFootTracker = skeleton.rightFootTracker != null;

		// get the foot positions
		Quaternion leftFootRotation = bufferHead.getLeftFootRotation();
		Quaternion rightFootRotation = bufferHead.getRightFootRotation();

		// between maximum correction angle and maximum correction angle delta
		// the values are interpolated
		float kneeAngleL = getXZAmount(leftFootPosition, leftKneePosition);
		float kneeAngleR = getXZAmount(rightFootPosition, rightKneePosition);

		float masterWeightL = getMasterWeight(kneeAngleL);
		float masterWeightR = getMasterWeight(kneeAngleR);

		// corrects rotations when planted firmly on the ground
		if (footPlantEnabled) {
			// prepare the weight vars for this correction step
			float weightL;
			float weightR;

			// the further from the ground the foot is, the less weight it
			// should have
			weightL = getFootPlantWeight(leftFootPosition);
			weightR = getFootPlantWeight(rightFootPosition);

			// if foot trackers exist add to the weights
			if (leftFootTracker)
				weightL *= getRotationalDistanceToPlant(leftFootRotation);
			if (rightFootTracker)
				weightR *= getRotationalDistanceToPlant(rightFootRotation);

			// perform the correction
			leftFootRotation = (leftFootRotation
				.interpR(
					isolateYaw(leftFootRotation),
					weightL * masterWeightL
				));

			rightFootRotation = (rightFootRotation
				.interpR(
					isolateYaw(rightFootRotation),
					weightR * masterWeightR
				));
		}

		// corrects rotations when the foot is in the air by rotating the foot
		// down so that the toes are touching
		if (toeSnapEnabled && !(leftFootTracker && rightFootTracker)) {
			// this correction step has its own weight vars
			float weightL;
			float weightR;

			// first compute the angle of the foot
			float angleL = getToeSnapAngle(leftFootPosition);
			float angleR = getToeSnapAngle(rightFootPosition);

			// then compute the weight of the correction
			weightL = getToeSnapWeight(leftFootPosition, angleL);
			weightR = getToeSnapWeight(rightFootPosition, angleR);

			// depending on the state variables, the correction weights should
			// be clamped
			if (!leftToeTouched) {
				weightL = Math.min(weightL, leftToeAngle);
			}
			if (!rightToeTouched) {
				weightR = Math.min(weightR, rightToeAngle);
			}

			// then slerp the rotation to the new rotation based on the weight
			if (leftFootTracker) {
				leftFootRotation = leftFootRotation
					.interpR(
						replacePitch(leftFootRotation, -angleL),
						weightL * masterWeightL
					);
			}
			if (rightFootTracker) {
				rightFootRotation = rightFootRotation
					.interpR(
						replacePitch(rightFootRotation, -angleR),
						weightR * masterWeightR
					);
			}

			// update state variables regarding toe snap
			if (leftFootPosition.getY() - floorLevel > footLength * MAXIMUM_TOE_DOWN_ANGLE) {
				leftToeTouched = false;
				leftToeAngle = weightL;
			} else if (leftFootPosition.getY() - floorLevel < 0.0f) {
				leftToeTouched = true;
				leftToeAngle = 1.0f;
			}
			if (rightFootPosition.getY() - floorLevel > footLength * MAXIMUM_TOE_DOWN_ANGLE) {
				rightToeTouched = false;
				rightToeAngle = weightR;
			} else if (rightFootPosition.getY() - floorLevel < 0.0f) {
				rightToeTouched = true;
				rightToeAngle = 1.0f;
			}
		}

		// update the foot rotations in the buffer
		bufferHead.setLeftFootRotationCorrected(leftFootRotation);
		bufferHead.setRightFootRotationCorrected(rightFootRotation);

		// update the skeleton
		skeleton.computedLeftFootTracker.setRotation(leftFootRotation);
		skeleton.computedRightFootTracker.setRotation(rightFootRotation);
	}

	// returns the length of the xz components of the normalized difference
	// between two vectors
	public float getXZAmount(Vector3 vec1, Vector3 vec2) {
		Vector3 vec = vec1.minus(vec2).unit();
		return new Vector3(vec.getX(), 0f, vec.getZ()).len();
	}

	// returns a float between 0 and 1 that represents the master weight for
	// foot rotation correciton
	private float getMasterWeight(float kneeAngle) {
		float masterWeight = (kneeAngle > MAXIMUM_CORRECTION_ANGLE
			&& kneeAngle < MAXIMUM_CORRECTION_ANGLE_DELTA)
				? 1.0f
					- ((kneeAngle - MAXIMUM_CORRECTION_ANGLE)
						/ (MAXIMUM_CORRECTION_ANGLE_DELTA - MAXIMUM_CORRECTION_ANGLE))
				: 0.0f;
		return (kneeAngle < MAXIMUM_CORRECTION_ANGLE) ? 1.0f : masterWeight;
	}

	// return the weight of the correction for toe snap
	private float getToeSnapWeight(Vector3 footPos, float angle) {
		// then compute the weight of the correction
		float weight = ((footPos.getY() - floorLevel) > footLength * TOE_SNAP_COOLDOWN)
			? 0.0f
			: 1.0f
				- ((footPos.getY() - floorLevel - footLength)
					/ (footLength * (TOE_SNAP_COOLDOWN - 1.0f)));
		return FastMath.clamp(weight, 0.0f, 1.0f);
	}

	// returns the angle of the foot for toe snap
	private float getToeSnapAngle(Vector3 footPos) {
		float angle = FastMath.clamp(footPos.getY() - floorLevel, 0.0f, footLength);
		return (angle > footLength * MAXIMUM_TOE_DOWN_ANGLE)
			? FastMath.asin((footLength * MAXIMUM_TOE_DOWN_ANGLE) / footLength)
			: FastMath.asin((angle / footLength));
	}

	// returns the weight for floor plant
	private float getFootPlantWeight(Vector3 footPos) {
		float weight = (footPos.getY() - floorLevel > ROTATION_CORRECTION_VERTICAL)
			? 0.0f
			: 1.0f - ((footPos.getY() - floorLevel) / ROTATION_CORRECTION_VERTICAL);
		return FastMath.clamp(weight, 0.0f, 1.0f);
	}

	// returns the amount to slerp for foot plant when foot trackers are active
	private float getRotationalDistanceToPlant(Quaternion footRot) {
		Quaternion footRotYaw = isolateYaw(footRot);
		float angle = footRot.angleToR(footRotYaw);
		angle = (float) (angle / (2 * Math.PI));

		angle = FastMath.clamp(angle, MIN_DISTANCE_FOR_PLANT, MAX_DISTANCE_FOR_PLANT);

		return 1
			- ((angle - MIN_DISTANCE_FOR_PLANT)
				/ (MAX_DISTANCE_FOR_PLANT - MIN_DISTANCE_FOR_PLANT));
	}

	// returns true if it is likely the user is standing
	public boolean isStanding() {
		// if the hip is below the vertical cutoff, user is not standing
		float cutoff = floorLevel
			+ hipToFloorDist
			- (hipToFloorDist * STANDING_CUTOFF_VERTICAL);

		if (hipPosition.getY() < cutoff) {
			currentDisengagementOffset = (1 - hipPosition.getY() / cutoff)
				* MAX_DISENGAGEMENT_OFFSET;

			return false;
		}

		currentDisengagementOffset = 0f;

		return true;
	}

	// move the knees in to a position that is closer to the truth
	private void solveLowerBody() {
		// calculate the left and right hip nodes in standing space
		Vector3 leftHip = hipPosition;
		Vector3 rightHip = hipPosition;

		// before moving the knees back closer to the hip nodes, offset them
		// the same amount the foot trackers where offset
		float leftXDif = leftFootPosition.getX() - bufferHead.getLeftFootPosition().getX();
		float rightXDif = rightFootPosition.getX() - bufferHead.getRightFootPosition().getX();
		float leftZDif = leftFootPosition.getZ() - bufferHead.getLeftFootPosition().getZ();
		float rightZDif = rightFootPosition.getZ() - bufferHead.getRightFootPosition().getZ();

		float leftX = leftKneePosition.getX() + (leftXDif * KNEE_LATERAL_WEIGHT);
		float leftZ = leftKneePosition.getZ() + (leftZDif * KNEE_LATERAL_WEIGHT);
		float rightX = rightKneePosition.getX() + (rightXDif * KNEE_LATERAL_WEIGHT);
		float rightZ = rightKneePosition.getZ() + (rightZDif * KNEE_LATERAL_WEIGHT);

		leftKneePosition = new Vector3(leftX, leftKneePosition.getY(), leftZ);
		rightKneePosition = new Vector3(rightX, rightKneePosition.getY(), rightZ);

		// calculate the bone distances
		float leftKneeHip = bufferHead.getLeftKneePosition().minus(leftHip).len();
		float rightKneeHip = bufferHead.getRightKneePosition().minus(rightHip).len();

		float leftKneeHipNew = leftKneePosition.minus(leftHip).len();
		float rightKneeHipNew = rightKneePosition.minus(rightHip).len();
		float leftKneeOffset = leftKneeHipNew - leftKneeHip;
		float rightKneeOffset = rightKneeHipNew - rightKneeHip;

		// get the vector from the hip to the knee
		Vector3 leftKneeVector = leftKneePosition
			.minus(leftHip)
			.unit()
			.times(leftKneeOffset * KNEE_CORRECTION_WEIGHT);

		Vector3 rightKneeVector = rightKneePosition
			.minus(rightHip)
			.unit()
			.times(rightKneeOffset * KNEE_CORRECTION_WEIGHT);

		// correct the knees
		leftKneePosition = leftKneePosition.minus(leftKneeVector);
		rightKneePosition = rightKneePosition.minus(rightKneeVector);
	}

	private float getLeftFootOffset() {
		float offset = computeUnitVector(this.leftFootRotation).getY();
		return FastMath.clamp(offset, 0, DYNAMIC_DISPLACEMENT_CUTOFF);
	}

	private float getRightFootOffset() {
		float offset = computeUnitVector(this.rightFootRotation).getY();
		return FastMath.clamp(offset, 0, DYNAMIC_DISPLACEMENT_CUTOFF);
	}

	// calculate the weight of foot correction
	private float calculateCorrectionWeight(
		Vector3 foot,
		Vector3 footCorrected
	) {
		Vector3 footDif = foot.minus(footCorrected);
		footDif = new Vector3(footDif.getX(), 0f, footDif.getZ());

		if (footDif.len() < MIN_ACCEPTABLE_ERROR) {
			return CORRECTION_WEIGHT_MIN;
		} else if (footDif.len() > MAX_ACCEPTABLE_ERROR) {
			return CORRECTION_WEIGHT_MAX;
		}

		return CORRECTION_WEIGHT_MIN
			+ (footDif.len() - MIN_ACCEPTABLE_ERROR)
				/ (MAX_ACCEPTABLE_ERROR - MIN_ACCEPTABLE_ERROR)
				* (CORRECTION_WEIGHT_MAX - CORRECTION_WEIGHT_MIN);
	}

	// calculate the center of mass of the user for the current frame
	// returns a vector representing the center of mass position
	private Vector3 computeCenterOfMass() {
		// check if arm data is available
		boolean armsAvailable = skeleton.hasLeftArmTracker
			&& skeleton.hasRightArmTracker;

		Vector3 centerOfMass = new Vector3(0f, 0f, 0f);

		// compute the center of mass of smaller body parts and then sum them up
		// with their respective weights
		Vector3 head = skeleton.headNode.getWorldTransform().getTranslation();
		Vector3 chest = skeleton.chestNode.getWorldTransform().getTranslation();
		Vector3 hip = skeleton.hipNode.getWorldTransform().getTranslation();
		Vector3 leftCalf = getCenterOfJoint(skeleton.leftAnkleNode, skeleton.leftKneeNode);
		Vector3 rightCalf = getCenterOfJoint(skeleton.rightAnkleNode, skeleton.rightKneeNode);
		Vector3 leftThigh = getCenterOfJoint(skeleton.leftKneeNode, skeleton.leftHipNode);
		Vector3 rightThigh = getCenterOfJoint(skeleton.rightKneeNode, skeleton.rightHipNode);
		centerOfMass = centerOfMass.plus(head.times(HEAD_MASS));
		centerOfMass = centerOfMass.plus(chest.times(CHEST_MASS));
		centerOfMass = centerOfMass.plus(hip.times(WAIST_MASS));
		centerOfMass = centerOfMass.plus(leftCalf.times(CALF_MASS));
		centerOfMass = centerOfMass.plus(rightCalf.times(CALF_MASS));
		centerOfMass = centerOfMass.plus(leftThigh.times(THIGH_MASS));
		centerOfMass = centerOfMass.plus(rightThigh.times(THIGH_MASS));

		if (armsAvailable) {
			Vector3 leftUpperArm = getCenterOfJoint(
				skeleton.leftElbowNode,
				skeleton.leftShoulderTailNode
			);
			Vector3 rightUpperArm = getCenterOfJoint(
				skeleton.rightElbowNode,
				skeleton.rightShoulderTailNode
			);
			Vector3 leftForearm = getCenterOfJoint(skeleton.leftElbowNode, skeleton.leftHandNode);
			Vector3 rightForearm = getCenterOfJoint(
				skeleton.rightElbowNode,
				skeleton.rightHandNode
			);
			centerOfMass = centerOfMass.plus(leftUpperArm.times(UPPER_ARM_MASS));
			centerOfMass = centerOfMass.plus(rightUpperArm.times(UPPER_ARM_MASS));
			centerOfMass = centerOfMass.plus(leftForearm.times(FOREARM_MASS));
			centerOfMass = centerOfMass.plus(rightForearm.times(FOREARM_MASS));
		} else {
			// if the arms are not available put them slightly in front
			// of the chest.
			Vector3 chestUnitVector = computeUnitVector(
				skeleton.chestNode.getWorldTransform().getRotation()
			);
			Vector3 armLocation = chest.plus(chestUnitVector.times(DEFAULT_ARM_DISTANCE));
			centerOfMass = centerOfMass.plus(armLocation.times(UPPER_ARM_MASS * 2.0f));
			centerOfMass = centerOfMass.plus(armLocation.times(FOREARM_MASS * 2.0f));
		}

		// finally translate in to tracker space
		centerOfMass = hipPosition
			.plus(
				centerOfMass.minus(skeleton.trackerHipNode.getWorldTransform().getTranslation())
			);

		return centerOfMass;
	}

	// get the center of two joints
	private Vector3 getCenterOfJoint(TransformNode node1, TransformNode node2) {
		return node1
			.getWorldTransform()
			.getTranslation()
			.plus(node2.getWorldTransform().getTranslation())
			.times(0.5f);
	}

	// get the amount of the constant correction to apply.
	private float getConstantCorrectionQuantityLeft() {
		if (leftFramesUnlocked >= CONTINUOUS_CORRECTION_WARMUP)
			return CONTINUOUS_CORRECTION_DIST;

		return CONTINUOUS_CORRECTION_DIST
			* ((float) leftFramesUnlocked / CONTINUOUS_CORRECTION_WARMUP);
	}

	private float getConstantCorrectionQuantityRight() {
		if (rightFramesUnlocked >= CONTINUOUS_CORRECTION_WARMUP)
			return CONTINUOUS_CORRECTION_DIST;

		return CONTINUOUS_CORRECTION_DIST
			* ((float) rightFramesUnlocked / CONTINUOUS_CORRECTION_WARMUP);
	}

	// update counters for the lock state of the feet
	private void updateLockStateCounters() {
		if (bufferHead.getLeftLegState() == LegTweakBuffer.LOCKED) {
			leftFramesUnlocked = 0;
			leftFramesLocked++;
		} else {
			leftFramesLocked = 0;
			leftFramesUnlocked++;
		}

		if (bufferHead.getRightLegState() == LegTweakBuffer.LOCKED) {
			rightFramesUnlocked = 0;
			rightFramesLocked++;
		} else {
			rightFramesLocked = 0;
			rightFramesUnlocked++;
		}
	}

	// remove the x and z components of the given quaternion
	private Quaternion isolateYaw(Quaternion quaternion) {
		return new Quaternion(
			quaternion.getW(),
			0,
			quaternion.getY(),
			0
		);
	}

	// return a quaternion that has been rotated by the new pitch amount
	private Quaternion replacePitch(Quaternion quaternion, float newPitch) {
		EulerAngles curAngs = quaternion.toEulerAngles(EulerOrder.YZX);
		EulerAngles newAngs = new EulerAngles(
			EulerOrder.YZX,
			newPitch,
			curAngs.getY(),
			curAngs.getZ()
		);
		return newAngs.toQuaternion();
	}

	// check if the difference between two floats flipped after correction
	private boolean checkOverShoot(float trueVal, float valBefore, float valAfter) {
		return (trueVal - valBefore) * (trueVal - valAfter) < 0;
	}

	// get the unit vector of the given rotation
	private Vector3 computeUnitVector(Quaternion quaternion) {
		return quaternion.toMatrix().getZ().unit();
	}
}
