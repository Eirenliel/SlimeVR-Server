package dev.slimevr.tracking.processor.stayaligned.adjust

import dev.slimevr.math.Angle
import dev.slimevr.math.AngleAverage
import dev.slimevr.tracking.processor.stayaligned.StayAlignedDefaults.CENTER_ERROR_HEAD_WEIGHT
import dev.slimevr.tracking.processor.stayaligned.StayAlignedDefaults.CENTER_ERROR_LOWER_LEG_WEIGHT
import dev.slimevr.tracking.processor.stayaligned.StayAlignedDefaults.CENTER_ERROR_UPPER_BODY_WEIGHT
import dev.slimevr.tracking.processor.stayaligned.StayAlignedDefaults.CENTER_ERROR_UPPER_LEG_WEIGHT
import dev.slimevr.tracking.processor.stayaligned.adjust.TrackerYaw.extraYaw
import dev.slimevr.tracking.processor.stayaligned.adjust.TrackerYaw.hasTrackerYaw
import dev.slimevr.tracking.processor.stayaligned.adjust.TrackerYaw.trackerYaw
import dev.slimevr.tracking.processor.stayaligned.skeleton.RelaxedBodyAngles
import dev.slimevr.tracking.processor.stayaligned.skeleton.Side
import dev.slimevr.tracking.processor.stayaligned.skeleton.TrackerSkeleton
import dev.slimevr.tracking.trackers.Tracker

/**
 * Error between a tracker's yaw, and its expected yaw. Assumes that the body is in a
 * relaxed position centered around an average yaw.
 */
class CenterErrorVisitor(
	private val averageYaw: Angle,
	private val relaxedBodyAngles: RelaxedBodyAngles,
) : TrackerSkeleton.TrackerVisitor<Angle> {

	override fun visitHeadTracker(
		tracker: Tracker,
		belowUpperBody: Tracker?,
	): Angle =
		error(tracker, averageYaw)

	override fun visitUpperBodyTracker(
		tracker: Tracker,
		aboveHeadOrUpperBody: Tracker?,
		belowUpperBody: Tracker?,
	): Angle =
		error(tracker, averageYaw)

	override fun visitUpperBodyTracker(
		tracker: Tracker,
		aboveHeadOrUpperBody: Tracker?,
		belowLeftUpperLeg: Tracker?,
		belowRightUpperLeg: Tracker?,
	): Angle =
		error(tracker, averageYaw)

	override fun visitArmTracker(
		side: Side,
		tracker: Tracker,
		aboveUpperBodyOrArm: Tracker?,
		belowHandOrArm: Tracker?,
	): Angle =
		// Arms can go anywhere
		Angle.ZERO

	override fun visitHandTracker(
		side: Side,
		tracker: Tracker,
		aboveArm: Tracker?,
	): Angle =
		// Hands can go anywhere
		Angle.ZERO

	override fun visitUpperLegTracker(
		side: Side,
		tracker: Tracker,
		aboveUpperBody: Tracker?,
		belowLowerLeg: Tracker?,
	): Angle =
		error(tracker, averageYaw + extraYaw(side, relaxedBodyAngles.upperLeg))

	override fun visitLowerLegTracker(
		side: Side,
		tracker: Tracker,
		aboveUpperLeg: Tracker?,
		belowFoot: Tracker?,
	): Angle =
		error(tracker, averageYaw + extraYaw(side, relaxedBodyAngles.lowerLeg))

	override fun visitFootTracker(
		side: Side,
		tracker: Tracker,
		aboveLowerLeg: Tracker?,
	): Angle =
		if (relaxedBodyAngles.foot != null) {
			error(tracker, averageYaw + extraYaw(side, relaxedBodyAngles.foot))
		} else {
			Angle.ZERO
		}

	private fun error(tracker: Tracker, expectedYaw: Angle) =
		Angle.signedBetween(trackerYaw(tracker), expectedYaw)

	companion object {

		/**
		 * Finds the center yaw of the skeleton.
		 */
		fun trackersCenterYaw(
			trackers: TrackerSkeleton,
		): Angle? {
			val head = trackers.head
			val upperBody = trackers.upperBody
			val leftUpperLeg = trackers.leftUpperLeg
			val rightUpperLeg = trackers.rightUpperLeg
			val leftLowerLeg = trackers.leftLowerLeg
			val rightLowerLeg = trackers.rightLowerLeg

			if (
				// Head is optional
				upperBody.isEmpty() ||
				leftUpperLeg == null ||
				rightUpperLeg == null ||
				leftLowerLeg == null ||
				rightLowerLeg == null
			) {
				return null
			}

			// Check whether we can calculate a center yaw
			val hasCenterYaw =
				upperBody.all(::hasTrackerYaw) &&
					hasTrackerYaw(leftUpperLeg) &&
					hasTrackerYaw(rightUpperLeg) &&
					hasTrackerYaw(leftLowerLeg) &&
					hasTrackerYaw(rightLowerLeg)
			if (!hasCenterYaw) {
				return null
			}

			// Calculate average yaw of the body
			val averageYaw = AngleAverage()

			if (head != null && hasTrackerYaw(head)) {
				averageYaw.add(trackerYaw(head), CENTER_ERROR_HEAD_WEIGHT)
			}

			upperBody.forEach {
				averageYaw.add(trackerYaw(it), CENTER_ERROR_UPPER_BODY_WEIGHT)
			}

			averageYaw.add(trackerYaw(leftUpperLeg), CENTER_ERROR_UPPER_LEG_WEIGHT)
			averageYaw.add(trackerYaw(rightUpperLeg), CENTER_ERROR_UPPER_LEG_WEIGHT)

			averageYaw.add(trackerYaw(leftLowerLeg), CENTER_ERROR_LOWER_LEG_WEIGHT)
			averageYaw.add(trackerYaw(rightLowerLeg), CENTER_ERROR_LOWER_LEG_WEIGHT)

			return averageYaw.toAngle()
		}
	}
}
