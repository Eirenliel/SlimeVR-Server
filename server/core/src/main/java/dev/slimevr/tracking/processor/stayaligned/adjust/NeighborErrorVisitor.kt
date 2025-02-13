package dev.slimevr.tracking.processor.stayaligned.adjust

import dev.slimevr.math.Angle
import dev.slimevr.tracking.processor.stayaligned.StayAlignedDefaults.NEIGHBOR_ERROR_ABOVE_TRACKER_WEIGHT
import dev.slimevr.tracking.processor.stayaligned.StayAlignedDefaults.NEIGHBOR_ERROR_BELOW_TRACKER_WEIGHT
import dev.slimevr.tracking.processor.stayaligned.adjust.TrackerYaw.extraYaw
import dev.slimevr.tracking.processor.stayaligned.adjust.TrackerYaw.trackerYaw
import dev.slimevr.tracking.processor.stayaligned.skeleton.RelaxedBodyAngles
import dev.slimevr.tracking.processor.stayaligned.skeleton.Side
import dev.slimevr.tracking.processor.stayaligned.skeleton.TrackerSkeleton
import dev.slimevr.tracking.trackers.Tracker
import dev.slimevr.tracking.trackers.TrackerPosition

/**
 * Error between a tracker's yaw and its neighbors' yaws.
 */
class NeighborErrorVisitor(
	private val relaxedBodyAngles: RelaxedBodyAngles,
) : TrackerSkeleton.TrackerVisitor<Angle> {

	override fun visitHeadTracker(
		tracker: Tracker,
		belowUpperBody: Tracker?,
	): Angle =
		// The head tracker is often off to the side, and we should not force it to
		// align to the upper body
		Angle.ZERO

	override fun visitUpperBodyTracker(
		tracker: Tracker,
		aboveHeadOrUpperBody: Tracker?,
		belowUpperBody: Tracker?,
	): Angle {
		val errors = AngleErrors()

		if (aboveHeadOrUpperBody != null) {
			// Head often drags the upper body trackers off to the side, so we ignore it
			if (aboveHeadOrUpperBody.trackerPosition != TrackerPosition.HEAD) {
				errors.add(error(tracker, aboveHeadOrUpperBody), NEIGHBOR_ERROR_ABOVE_TRACKER_WEIGHT)
			}
		}

		if (belowUpperBody != null) {
			errors.add(error(tracker, belowUpperBody), NEIGHBOR_ERROR_BELOW_TRACKER_WEIGHT)
		}

		return errors.toL2Norm()
	}

	override fun visitUpperBodyTracker(
		tracker: Tracker,
		aboveHeadOrUpperBody: Tracker?,
		belowLeftUpperLeg: Tracker?,
		belowRightUpperLeg: Tracker?,
	): Angle {
		val errors = AngleErrors()

		if (aboveHeadOrUpperBody != null) {
			// Head often drags the upper body trackers off to the side, so we ignore it
			if (aboveHeadOrUpperBody.trackerPosition != TrackerPosition.HEAD) {
				errors.add(error(tracker, aboveHeadOrUpperBody), NEIGHBOR_ERROR_ABOVE_TRACKER_WEIGHT)
			}
		}

		// Only consider upper leg trackers if both are available, so that the upper
		// body tracker can be balanced between both
		if (belowLeftUpperLeg != null && belowRightUpperLeg != null) {
			errors.add(
				error(
					tracker,
					belowLeftUpperLeg,
					// Upper body tracker is on the RIGHT side of the left upper leg tracker
					extraYaw(Side.RIGHT, relaxedBodyAngles.upperLeg),
				),
				NEIGHBOR_ERROR_BELOW_TRACKER_WEIGHT * 0.5f,
			)

			errors.add(
				error(
					tracker,
					belowRightUpperLeg,
					// Upper body tracker is on the LEFT side of the right upper leg tracker
					extraYaw(Side.LEFT, relaxedBodyAngles.upperLeg),
				),
				NEIGHBOR_ERROR_BELOW_TRACKER_WEIGHT * 0.5f,
			)
		}

		return errors.toL2Norm()
	}

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
	): Angle {
		val errors = AngleErrors()

		if (aboveUpperBody != null) {
			errors.add(
				error(tracker, aboveUpperBody, extraYaw(side, relaxedBodyAngles.upperLeg)),
				NEIGHBOR_ERROR_ABOVE_TRACKER_WEIGHT,
			)
		}

		if (belowLowerLeg != null) {
			errors.add(
				// Negative extra yaw because we are on the opposite side of the lower leg
				error(tracker, belowLowerLeg, -extraYaw(side, relaxedBodyAngles.lowerLeg - relaxedBodyAngles.upperLeg)),
				NEIGHBOR_ERROR_BELOW_TRACKER_WEIGHT,
			)
		}

		return errors.toL2Norm()
	}

	override fun visitLowerLegTracker(
		side: Side,
		tracker: Tracker,
		aboveUpperLeg: Tracker?,
		belowFoot: Tracker?,
	): Angle {
		val errors = AngleErrors()

		if (aboveUpperLeg != null) {
			errors.add(
				error(tracker, aboveUpperLeg, extraYaw(side, relaxedBodyAngles.lowerLeg - relaxedBodyAngles.upperLeg)),
				NEIGHBOR_ERROR_ABOVE_TRACKER_WEIGHT,
			)
		}

		if (belowFoot != null && relaxedBodyAngles.foot != null) {
			errors.add(
				// Negative extra yaw because we are on the opposite side of the foot
				error(tracker, belowFoot, -extraYaw(side, relaxedBodyAngles.foot - relaxedBodyAngles.lowerLeg)),
			)
		}

		return errors.toL2Norm()
	}

	override fun visitFootTracker(
		side: Side,
		tracker: Tracker,
		aboveLowerLeg: Tracker?,
	): Angle {
		val errors = AngleErrors()

		if (aboveLowerLeg != null && relaxedBodyAngles.foot != null) {
			errors.add(
				error(
					tracker,
					aboveLowerLeg,
					extraYaw(side, relaxedBodyAngles.foot - relaxedBodyAngles.lowerLeg),
				),
				NEIGHBOR_ERROR_ABOVE_TRACKER_WEIGHT,
			)
		}

		return errors.toL2Norm()
	}

	private fun error(
		tracker: Tracker,
		neighborTracker: Tracker,
		extraYawRelativeToNeighbor: Angle = Angle.ZERO,
	) =
		Angle.signedBetween(
			trackerYaw(tracker),
			trackerYaw(neighborTracker) + extraYawRelativeToNeighbor,
		)
}
