package dev.slimevr.tracking.processor.stayaligned.adjust

import dev.slimevr.math.Angle
import dev.slimevr.tracking.processor.stayaligned.skeleton.Side
import dev.slimevr.tracking.processor.stayaligned.skeleton.TrackerSkeleton
import dev.slimevr.tracking.trackers.Tracker
import io.github.axisangles.ktmath.Quaternion
import io.github.axisangles.ktmath.Vector3
import kotlin.math.*

/**
 * Error between a locked tracker's yaw and its yaw when it was initially locked.
 */
class LockedErrorVisitor : TrackerSkeleton.TrackerVisitor<Angle> {

	override fun visitHeadTracker(
		tracker: Tracker,
		belowUpperBody: Tracker?,
	): Angle =
		// The head tracker is usually a headset and does not need to be aligned
		Angle.ZERO

	override fun visitUpperBodyTracker(
		tracker: Tracker,
		aboveHeadOrUpperBody: Tracker?,
		belowUpperBody: Tracker?,
	): Angle =
		error(tracker)

	override fun visitUpperBodyTracker(
		tracker: Tracker,
		aboveHeadOrUpperBody: Tracker?,
		belowLeftUpperLeg: Tracker?,
		belowRightUpperLeg: Tracker?,
	): Angle =
		error(tracker)

	override fun visitArmTracker(
		side: Side,
		tracker: Tracker,
		aboveUpperBodyOrArm: Tracker?,
		belowHandOrArm: Tracker?,
	): Angle =
		error(tracker)

	override fun visitHandTracker(
		side: Side,
		tracker: Tracker,
		aboveArm: Tracker?,
	): Angle =
		error(tracker)

	override fun visitUpperLegTracker(
		side: Side,
		tracker: Tracker,
		aboveUpperBody: Tracker?,
		belowLowerLeg: Tracker?,
	): Angle =
		error(tracker)

	override fun visitLowerLegTracker(
		side: Side,
		tracker: Tracker,
		aboveUpperLeg: Tracker?,
		belowFoot: Tracker?,
	): Angle =
		error(tracker)

	override fun visitFootTracker(
		side: Side,
		tracker: Tracker,
		aboveLowerLeg: Tracker?,
	): Angle =
		error(tracker)

	private fun error(tracker: Tracker): Angle {
		val lockedRotation = tracker.stayAligned.lockedRotation
			?: return Angle.ZERO

		return yawDifference(tracker.getRotation(), lockedRotation)
	}

	/**
	 * Gets the yaw between two rotations, for small rotations.
	 *
	 * A locked tracker can be in any rotation, so we cannot use TrackerYaw::trackerYaw,
	 * which doesn't work for a tracker that is on its side.
	 *
	 * WARNING: DO NOT USE for large rotations because the chosen axis might have a very
	 * small projection on the yaw plane, which yields a low confidence yaw.
	 *
	 * TODO: It might be possible to pick a different EulerOrder when we encounter
	 * 		singularities, but I wasn't able to get this working correctly.
	 */
	private fun yawDifference(rotation: Quaternion, targetRotation: Quaternion): Angle {
		val targetX = targetRotation.sandwichUnitX()
		val targetY = targetRotation.sandwichUnitY()
		val targetZ = targetRotation.sandwichUnitZ()

		// Find the axis that is closest to the yaw plane
		val axis: Vector3
		val targetAxis: Vector3

		val targetXScore = abs(targetX.dot(Vector3.POS_Y))
		val targetYScore = abs(targetY.dot(Vector3.POS_Y))
		val targetZScore = abs(targetZ.dot(Vector3.POS_Y))

		// The axis that is closest to the yaw plane has the smallest absolute dot
		// product with the Y axis
		if ((targetXScore <= targetYScore) && (targetXScore <= targetZScore)) {
			axis = rotation.sandwichUnitX()
			targetAxis = targetX
		} else if ((targetYScore <= targetXScore) && (targetYScore <= targetZScore)) {
			axis = rotation.sandwichUnitY()
			targetAxis = targetY
		} else {
			axis = rotation.sandwichUnitZ()
			targetAxis = targetZ
		}

		val yaw = atan2(axis.z, axis.x)
		val targetYaw = atan2(targetAxis.z, targetAxis.x)

		return Angle.ofRad(yaw - targetYaw)
	}
}
