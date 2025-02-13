package dev.slimevr.tracking.processor.stayaligned.skeleton

import kotlin.math.*

/**
 * Detects the pose of trackers in a skeleton.
 */
enum class TrackerSkeletonPose {
	UNKNOWN,
	STANDING,
	SITTING_IN_CHAIR,
	SITTING_ON_GROUND,
	LYING_ON_BACK,
	KNEELING,
	;

	companion object {

		fun ofTrackers(trackers: TrackerSkeleton): TrackerSkeletonPose {
			val poses = TrackerPoses(
				trackers.upperBody.map(TrackerPose::ofTracker),
				trackers.leftUpperLeg?.let(TrackerPose::ofTracker) ?: TrackerPose.NONE,
				trackers.rightUpperLeg?.let(TrackerPose::ofTracker) ?: TrackerPose.NONE,
				trackers.leftLowerLeg?.let(TrackerPose::ofTracker) ?: TrackerPose.NONE,
				trackers.rightLowerLeg?.let(TrackerPose::ofTracker) ?: TrackerPose.NONE,
			)

			return if (isStanding(poses)) {
				STANDING
			} else if (isSittingInChair(poses)) {
				SITTING_IN_CHAIR
			} else if (isSittingOnGround(poses)) {
				SITTING_ON_GROUND
			} else if (isLyingOnBack(poses)) {
				LYING_ON_BACK
			} else if (isKneeling(poses)) {
				KNEELING
			} else {
				UNKNOWN
			}
		}

		private class TrackerPoses(
			val upperBody: List<TrackerPose>,
			val leftUpperLeg: TrackerPose,
			val rightUpperLeg: TrackerPose,
			val leftLowerLeg: TrackerPose,
			val rightLowerLeg: TrackerPose,
		)

		private fun isStanding(pose: TrackerPoses) =
			pose.upperBody.all(::topFacingUp) &&
				topFacingUp(pose.leftUpperLeg) &&
				topFacingUp(pose.rightUpperLeg) &&
				topFacingUp(pose.leftLowerLeg) &&
				topFacingUp(pose.rightLowerLeg)

		private fun isSittingInChair(pose: TrackerPoses) =
			pose.upperBody.isNotEmpty() && topFacingUp(pose.upperBody[0]) &&
				pose.upperBody.all { topFacingUp(it) || frontFacingUp(it) } &&
				frontFacingUp(pose.leftUpperLeg) &&
				frontFacingUp(pose.rightUpperLeg) &&
				topFacingUp(pose.leftLowerLeg) &&
				topFacingUp(pose.rightLowerLeg)

		private fun isSittingOnGround(pose: TrackerPoses) =
			pose.upperBody.isNotEmpty() && topFacingUp(pose.upperBody[0]) &&
				pose.upperBody.all { topFacingUp(it) || frontFacingUp(it) } &&
				pose.leftUpperLeg.let { frontFacingUp(it) || topFacingDown(it) } &&
				pose.rightUpperLeg.let { frontFacingUp(it) || topFacingDown(it) } &&
				pose.leftLowerLeg.let { frontFacingUp(it) || topFacingUp(it) } &&
				pose.rightLowerLeg.let { frontFacingUp(it) || topFacingUp(it) }

		private fun isLyingOnBack(pose: TrackerPoses) =
			pose.upperBody.all(::frontFacingUp) &&
				pose.leftUpperLeg.let { frontFacingUp(it) || topFacingDown(it) } &&
				pose.rightUpperLeg.let { frontFacingUp(it) || topFacingDown(it) } &&
				pose.leftLowerLeg.let { frontFacingUp(it) || topFacingUp(it) } &&
				pose.rightLowerLeg.let { frontFacingUp(it) || topFacingUp(it) }

		private fun isKneeling(pose: TrackerPoses) =
			pose.leftUpperLeg.let { topFacingUp(it) || frontFacingUp(it) } &&
				pose.rightUpperLeg.let { topFacingUp(it) || frontFacingUp(it) } &&
				frontFacingDown(pose.leftLowerLeg) &&
				frontFacingDown(pose.rightLowerLeg)

		// Helper functions to make checks more readable
		private fun topFacingUp(pose: TrackerPose) = pose == TrackerPose.TOP_FACING_UP
		private fun topFacingDown(pose: TrackerPose) = pose == TrackerPose.TOP_FACING_DOWN
		private fun frontFacingUp(pose: TrackerPose) = pose == TrackerPose.FRONT_FACING_UP
		private fun frontFacingDown(pose: TrackerPose) = pose == TrackerPose.FRONT_FACING_DOWN
	}
}
