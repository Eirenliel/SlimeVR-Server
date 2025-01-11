package dev.slimevr.tracking.processor

import com.jme3.math.FastMath
import io.github.axisangles.ktmath.Quaternion
import io.github.axisangles.ktmath.Vector3
import kotlin.math.*

/**
 * Represents a function that applies a rotational constraint.
 */
typealias ConstraintFunction = (localRotation: Quaternion, limit1: Float, limit2: Float, limit3: Float) -> Quaternion

/**
 * Represents the rotational limits of a Bone relative to its parent,
 * twist and swing are the max and min when constraintType is a hinge.
 * Twist, swing, allowedDeviation, and maxDeviationFromTracker represent
 * an angle in degrees.
 */
class Constraint(
	val constraintType: ConstraintType,
	twist: Float = 0.0f,
	swing: Float = 0.0f,
	allowedDeviation: Float = 0f,
	maxDeviationFromTracker: Float = 15f,
) {
	private val constraintFunction = constraintTypeToFunc(constraintType)
	private val twistRad = twist * FastMath.DEG_TO_RAD
	private val swingRad = swing * FastMath.DEG_TO_RAD
	private val allowedDeviationRad = allowedDeviation * FastMath.DEG_TO_RAD
	private val maxDeviationFromTrackerRad = maxDeviationFromTracker * FastMath.DEG_TO_RAD

	/**
	 * allowModification may be false for reasons other than a tracker being on this bone
	 * while hasTrackerRotation is only true if this bone has a tracker. These values are
	 * to be used with an IK solver and are not currently set accurately
	 */
	var allowModifications = true
	var hasTrackerRotation = false

	/**
	 * The rotation before any IK solve takes place. Again this value is not currently set accurately
	 */
	var initialRotation = Quaternion.IDENTITY

	/**
	 * Apply rotational constraints and if applicable force the rotation
	 * to be unchanged unless it violates the constraints
	 */
	fun applyConstraint(rotation: Quaternion, thisBone: Bone): Quaternion {
		// When constraints are being used during a IK solve the input rotation is not necessarily
		// the bones global rotation, thus complete constraints must be specifically handled.
		if (constraintType == ConstraintType.COMPLETE) return thisBone.getGlobalRotation()

		// If there is no parent and this is not a complete constraint accept the rotation as is.
		if (thisBone.parent == null) return rotation

		val localRotation = getLocalRotation(rotation, thisBone)
		val constrainedRotation = constraintFunction(localRotation, swingRad, twistRad, allowedDeviationRad)
		return getWorldRotationFromLocal(constrainedRotation, thisBone)
	}

	/**
	 * Force the given rotation to be within allowedDeviation degrees away from
	 * initialRotation on both the twist and swing axis
	 */
	fun constrainToInitialRotation(rotation: Quaternion): Quaternion {
		val rotationLocal = rotation * initialRotation.inv()
		var (swingQ, twistQ) = decompose(rotationLocal, Vector3.NEG_Y)
		swingQ = constrain(swingQ, maxDeviationFromTrackerRad)
		twistQ = constrain(twistQ, maxDeviationFromTrackerRad)
		return initialRotation * (swingQ * twistQ)
	}

	companion object {
		const val ANGLE_THRESHOLD = 0.004f // == 0.25 degrees
		const val FILTER_IMPACT_THRESHOLD = 0.0349f // == 2 degrees

		enum class ConstraintType {
			TWIST_SWING,
			HINGE,
			LOOSE_HINGE,
			COMPLETE,
		}

		private fun constraintTypeToFunc(type: ConstraintType) =
			when (type) {
				ConstraintType.COMPLETE -> completeConstraint
				ConstraintType.TWIST_SWING -> twistSwingConstraint
				ConstraintType.HINGE -> hingeConstraint
				ConstraintType.LOOSE_HINGE -> looseHingeConstraint
			}

		private fun getLocalRotation(rotation: Quaternion, thisBone: Bone): Quaternion {
			val parent = thisBone.parent!!
			val localRotationOffset = parent.rotationOffset.inv() * thisBone.rotationOffset
			return (parent.getGlobalRotation() * localRotationOffset).inv() * rotation
		}

		private fun getWorldRotationFromLocal(rotation: Quaternion, thisBone: Bone): Quaternion {
			val parent = thisBone.parent!!
			val localRotationOffset = parent.rotationOffset.inv() * thisBone.rotationOffset
			return (parent.getGlobalRotation() * localRotationOffset * rotation).unit()
		}

		private fun decompose(
			rotation: Quaternion,
			twistAxis: Vector3,
		): Pair<Quaternion, Quaternion> {
			val projection = rotation.project(twistAxis).unit()
			val twist = Quaternion(sqrt(1.0f - projection.xyz.lenSq()) * if (rotation.w >= 0f) 1f else -1f, projection.xyz).unit()
			val swing = (rotation * twist.inv()).unit()
			return Pair(swing, twist)
		}

		private fun constrain(rotation: Quaternion, angle: Float): Quaternion {
			// Use angle to get the maximum magnitude the vector part of rotation can be
			// before it has violated a constraint.
			// Multiplying by 0.5 uniquely maps angles 0-180 degrees to 0-1 which works
			// nicely with unit quaternions.
			val magnitude = sin(angle * 0.5f)
			val magnitudeSqr = magnitude * magnitude
			val sign = if (rotation.w >= 0f) 1f else -1f
			var vector = rotation.xyz
			var rot = rotation

			if (vector.lenSq() > magnitudeSqr) {
				vector = vector.unit() * magnitude
				rot = Quaternion(sqrt(1.0f - magnitudeSqr) * sign, vector)
			}

			return rot.unit()
		}

		private fun constrain(rotation: Quaternion, minAngle: Float, maxAngle: Float, axis: Vector3): Quaternion {
			val magnitudeMin = sin(minAngle * 0.5f)
			val magnitudeMax = sin(maxAngle * 0.5f)
			val magnitudeSqrMin = magnitudeMin * magnitudeMin * if (minAngle >= 0f) 1f else -1f
			val magnitudeSqrMax = magnitudeMax * magnitudeMax * if (maxAngle >= 0f) 1f else -1f
			var vector = rotation.xyz
			var rot = rotation

			val rotMagnitude = vector.lenSq() * if (vector.dot(axis) * sign(rot.w) < 0) -1f else 1f
			if (rotMagnitude < magnitudeSqrMin || rotMagnitude > magnitudeSqrMax) {
				val distToMin = min(abs(rotMagnitude - magnitudeSqrMin), abs(rotMagnitude + magnitudeSqrMin))
				val distToMax = min(abs(rotMagnitude - magnitudeSqrMax), abs(rotMagnitude + magnitudeSqrMax))

				val magnitude = if (distToMin < distToMax) magnitudeMin else magnitudeMax
				val magnitudeSqr = abs(if (distToMin < distToMax) magnitudeSqrMin else magnitudeSqrMax)
				vector = vector.unit() * -magnitude

				rot = Quaternion(sqrt(1.0f - magnitudeSqr), vector)
			}

			return rot.unit()
		}

		// Constraint function for TwistSwingConstraint
		private val twistSwingConstraint: ConstraintFunction =
			{ rotation: Quaternion, swingRad: Float, twistRad: Float, _: Float ->
				var (swingQ, twistQ) = decompose(rotation, Vector3.NEG_Y)
				swingQ = constrain(swingQ, swingRad)
				twistQ = constrain(twistQ, twistRad)

				swingQ * twistQ
			}

		// Constraint function for a hinge constraint with min and max angles
		private val hingeConstraint: ConstraintFunction =
			{ rotation: Quaternion, min: Float, max: Float, _: Float ->
				val (_, hingeAxisRot) = decompose(rotation, Vector3.NEG_X)

				constrain(hingeAxisRot, min, max, Vector3.NEG_X)
			}

		// Constraint function for a hinge constraint with min and max angles that allows nonHingeDeviation
		// rotation on all axis but the hinge
		private val looseHingeConstraint: ConstraintFunction =
			{ rotation: Quaternion, min: Float, max: Float, nonHingeDeviation: Float ->
				var (nonHingeRot, hingeAxisRot) = decompose(rotation, Vector3.NEG_X)
				hingeAxisRot = constrain(hingeAxisRot, min, max, Vector3.NEG_X)
				nonHingeRot = constrain(nonHingeRot, nonHingeDeviation)

				nonHingeRot * hingeAxisRot
			}

		// Constraint function for CompleteConstraint
		private val completeConstraint: ConstraintFunction = { rotation: Quaternion, _: Float, _: Float, _: Float ->
			rotation
		}
	}
}
