package dev.slimevr.tracking.processor.stayaligned.adjust

import dev.slimevr.config.StayAlignedConfig
import dev.slimevr.math.Angle
import dev.slimevr.math.Angle.Companion.abs
import dev.slimevr.tracking.processor.stayaligned.StayAlignedDefaults
import dev.slimevr.tracking.processor.stayaligned.skeleton.RelaxedBodyAngles
import dev.slimevr.tracking.processor.stayaligned.skeleton.TrackerSkeleton
import dev.slimevr.tracking.processor.stayaligned.skeleton.TrackerSkeletonPose
import dev.slimevr.tracking.processor.stayaligned.state.YawErrors
import dev.slimevr.tracking.trackers.Tracker
import dev.slimevr.tracking.trackers.udp.MagnetometerStatus

object AdjustTrackerYaw {

	/**
	 * Adjusts the yaw of a tracker.
	 *
	 * We apply 3 forces to the tracker which help keep it balanced with the
	 * rest of the trackers in the skeleton. Hopefully, this leads to a
	 * reasonable alignment which closely matches the player's real alignment.
	 *
	 * Locked Trackers
	 * ---------------
	 * When a tracker is at rest, we lock it and save its yaw. We assume that
	 * locked trackers really are at rest, and that any rotation is due to yaw
	 * drift. We add an error term that is the difference between the tracker's
	 * yaw and its locked yaw. This works very well when the player is leaning
	 * against something, e.g. a chair or couch. But when the player is
	 * standing still, they tend to sway back toward the neutral position, which
	 * causes us to adjust the tracker a bit too much outwards. Luckily, this is
	 * balanced by the centering force.
	 *
	 * Centering Force
	 * ---------------
	 * We have a centering force which nudges the trackers into a relaxed pose
	 * for the player. For example, we know that a standing player tends to be
	 * facing forward with their legs slightly spread apart. We calculate the
	 * average yaw of the skeleton and nudge the trackers into the expected
	 * position centered around this average yaw.
	 *
	 * Neighbor Trackers
	 * -----------------
	 * The player is often not in a relaxed pose. If we only had the above
	 * forces, a moving tracker next to a locked tracker can rotate into
	 * unnatural positions. For example, consider a lower leg tracker that is
	 * moving, next to a foot tracker that is locked, but stretched out
	 * in-front of the player. The centering force will try to adjust the
	 * lower leg tracker back to the relaxed position, which could be very far
	 * away from the foot tracker. To address this, we nudge the tracker into
	 * the midpoint between its neighbors, after accounting for the relaxed
	 * pose.
	 *
	 * After we calculate all these error terms, we weight them, and use
	 * gradient descent to find the direction to apply a yaw correction. By
	 * applying this 50 times a second, the whole body is nudged into a
	 * reasonable alignment.
	 */
	fun adjust(
		tracker: Tracker,
		trackers: TrackerSkeleton,
		yawCorrection: Angle,
		config: StayAlignedConfig,
	) {
		if (tracker.stayAligned.lockedRotation != null) {
			adjustLockedTracker(tracker, trackers, yawCorrection)
		} else {
			adjustMovingTracker(tracker, trackers, yawCorrection, config)
		}
	}

	/**
	 * Adjusts a locked tracker.
	 */
	private fun adjustLockedTracker(
		tracker: Tracker,
		trackers: TrackerSkeleton,
		yawCorrection: Angle,
	) {
		adjustByError(tracker, yawCorrection) {
			val yawErrors = YawErrors()

			yawErrors.lockedError =
				trackers.visit(tracker, LockedErrorVisitor())
					?: Angle.ZERO

			yawErrors
		}
	}

	/**
	 * Adjusts a tracker that is moving.
	 */
	private fun adjustMovingTracker(
		tracker: Tracker,
		trackers: TrackerSkeleton,
		yawCorrection: Angle,
		config: StayAlignedConfig,
	) {
		val pose = TrackerSkeletonPose.ofTrackers(trackers)
		val relaxedBodyAngles = RelaxedBodyAngles.forPose(pose, config) ?: return
		val centerYaw = CenterErrorVisitor.trackersCenterYaw(trackers) ?: return

		adjustByError(tracker, yawCorrection) {
			val yawErrors = YawErrors()

			yawErrors.centerError =
				trackers.visit(
					tracker,
					CenterErrorVisitor(centerYaw, relaxedBodyAngles),
				) ?: Angle.ZERO

			yawErrors.neighborError =
				trackers.visit(
					tracker,
					NeighborErrorVisitor(relaxedBodyAngles),
				) ?: Angle.ZERO

			yawErrors
		}
	}

	/**
	 * Adjusts the yaw by applying gradient descent.
	 */
	private fun adjustByError(
		tracker: Tracker,
		yawCorrection: Angle,
		errorFn: (tracker: Tracker) -> YawErrors,
	) {
		// Only IMUs have yaw drift
		if (!tracker.isImu()) {
			return
		}

		// Magnetometer directly determines the yaw
		if (tracker.magStatus == MagnetometerStatus.ENABLED) {
			return
		}

		val state = tracker.stayAligned

		val curYaw = state.yawCorrection.yaw
		val curError = errorFn(tracker)

		val posYaw = curYaw + yawCorrection
		state.yawCorrection.yaw = posYaw
		val posError = errorFn(tracker)

		val negYaw = curYaw - yawCorrection
		state.yawCorrection.yaw = negYaw
		val negError = errorFn(tracker)

		val posYawDelta = gradient(posError, curError)
		val negYawDelta = gradient(negError, curError)

		// Pick the yaw that minimizes the error
		if ((posYawDelta < Angle.ZERO) && (posYawDelta < negYawDelta)) {
			state.yawCorrection.yaw = posYaw
			state.yawErrors = posError
		} else if (negYawDelta < Angle.ZERO) {
			state.yawCorrection.yaw = negYaw
			state.yawErrors = negError
		} else {
			state.yawCorrection.yaw = curYaw
			state.yawErrors = curError
		}
	}

	/**
	 * Calculates the gradient between two errors. A negative gradient means that there
	 * is less error in that direction.
	 */
	private fun gradient(errors: YawErrors, base: YawErrors) =
		(abs(errors.lockedError) - abs(base.lockedError)) * StayAlignedDefaults.YAW_ERRORS_LOCKED_ERROR_WEIGHT +
			(abs(errors.centerError) - abs(base.centerError)) * StayAlignedDefaults.YAW_ERRORS_CENTER_ERROR_WEIGHT +
			(abs(errors.neighborError) - abs(base.neighborError)) * StayAlignedDefaults.YAW_ERRORS_NEIGHBOR_ERROR_WEIGHT
}
