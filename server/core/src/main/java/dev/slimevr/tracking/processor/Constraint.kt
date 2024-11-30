package dev.slimevr.tracking.processor

import io.github.axisangles.ktmath.Quaternion
import io.github.axisangles.ktmath.Vector3
import kotlin.math.*

/**
 * Represents a function that applies a rotational constraint.
 */
typealias ConstraintFunction = (inputRotation: Quaternion, thisBone: Bone, limit1: Float, limit2: Float) -> Quaternion

/**
 * Represents the rotational limits of a Bone relative to its parent
 */
class Constraint(
	val constraintType: ConstraintType,
	twist: Float = 0.0f,
	swing: Float = 0.0f,
	allowedDeviation: Float = 0f
) {
	private val constraintFunction = constraintTypeToFunc(constraintType)
	private val twistRad = Math.toRadians(twist.toDouble()).toFloat()
	private val swingRad = Math.toRadians(swing.toDouble()).toFloat()
	private val allowedDeviationRad = Math.toRadians(allowedDeviation.toDouble()).toFloat()
	var hasTrackerRotation = false

	/**
	 * If false solve with minimal movement applied to this link
	 */
	var allowModifications = true

	var initialRotation = Quaternion.IDENTITY

	/**
	 * Apply rotational constraints and if applicable force the rotation
	 * to be unchanged unless it violates the constraints
	 */
	fun applyConstraint(rotation: Quaternion, thisBone: Bone): Quaternion = constraintFunction(rotation, thisBone, swingRad, twistRad).unit()

	/**
	 * Force the given rotation to be within allowedDeviation degrees away from
	 * initialRotation on both the twist and swing axis
	 */
	fun constrainToInitialRotation(rotation: Quaternion): Quaternion {
		val rotationLocal = rotation * initialRotation.inv()
		var (swingQ, twistQ) = decompose(rotationLocal, Vector3.NEG_Y)
		swingQ = constrain(swingQ, allowedDeviationRad)
		twistQ = constrain(twistQ, allowedDeviationRad)
		return initialRotation * (swingQ * twistQ)
	}

	companion object {
		enum class ConstraintType {
			TWIST_SWING,
			HINGE,
			COMPLETE,
		}

		private fun constraintTypeToFunc(type: ConstraintType) =
			when (type) {
				ConstraintType.COMPLETE -> completeConstraint
				ConstraintType.TWIST_SWING -> twistSwingConstraint
				ConstraintType.HINGE -> hingeConstraint
			}

		private fun decompose(
			rotation: Quaternion,
			twistAxis: Vector3,
		): Pair<Quaternion, Quaternion> {
			val projection = rotation.project(twistAxis).unit()

			val twist = Quaternion(rotation.w, projection.xyz).unit()
			val swing = (rotation * twist.inv()).unit()

			return Pair(swing, twist)
		}

		private fun constrain(rotation: Quaternion, angle: Float): Quaternion {
			val magnitude = sin(angle * 0.5f)
			val magnitudeSqr = magnitude * magnitude
			val sign = if (rotation.w != 0f) sign(rotation.w) else 1f
			var vector = rotation.xyz
			var rot = rotation

			if (vector.lenSq() > magnitudeSqr) {
				vector = vector.unit() * magnitude
				rot = Quaternion(
					sqrt(1.0f - magnitudeSqr) * sign,
					vector.x,
					vector.y,
					vector.z,
				)
			}

			return rot.unit()
		}

		private fun constrain(rotation: Quaternion, minAngle: Float, maxAngle: Float, axis: Vector3): Quaternion {
			val magnitudeMin = sin(minAngle * 0.5f)
			val magnitudeMax = sin(maxAngle * 0.5f)
			val magnitudeSqrMin = magnitudeMin * magnitudeMin * if (minAngle != 0f) sign(minAngle) else 1f
			val magnitudeSqrMax = magnitudeMax * magnitudeMax * if (maxAngle != 0f) sign(maxAngle) else 1f
			var vector = rotation.xyz
			var rot = rotation

			val rotMagnitude = vector.lenSq() * if (vector.dot(axis) < 0) -1f else 1f
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
			{ rotation: Quaternion, thisBone: Bone, swingRad: Float, twistRad: Float ->
				if (thisBone.parent == null) {
					rotation
				} else {
					val parent = thisBone.parent!!
					val localRotationOffset = parent.rotationOffset.inv() * thisBone.rotationOffset
					val rotationLocal =
						(parent.getGlobalRotation() * localRotationOffset).inv() * rotation
					var (swingQ, twistQ) = decompose(rotationLocal, Vector3.NEG_Y)

					swingQ = constrain(swingQ, swingRad)
					twistQ = constrain(twistQ, twistRad)

					(parent.getGlobalRotation() * localRotationOffset * (swingQ * twistQ)).unit()
				}
			}

		// Constraint function for a hinge constraint with min and max angles
		private val hingeConstraint: ConstraintFunction =
			{ rotation: Quaternion, thisBone: Bone, min: Float, max: Float ->
				if (thisBone.parent == null) {
					rotation
				} else {
					val parent = thisBone.parent!!
					val localRotationOffset = parent.rotationOffset.inv() * thisBone.rotationOffset
					val rotationLocal =
						(parent.getGlobalRotation() * localRotationOffset).inv() * rotation

					var (_, hingeAxisRot) = decompose(rotationLocal, Vector3.NEG_X)

					hingeAxisRot = constrain(hingeAxisRot, min, max, Vector3.NEG_X)

					(parent.getGlobalRotation() * localRotationOffset * hingeAxisRot).unit()
				}
			}

		// Constraint function for CompleteConstraint
		private val completeConstraint: ConstraintFunction = { _: Quaternion, thisBone: Bone, _: Float, _: Float ->
			thisBone.getGlobalRotation()
		}
	}
}
