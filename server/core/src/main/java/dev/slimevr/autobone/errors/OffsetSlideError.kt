package dev.slimevr.autobone.errors

import dev.slimevr.autobone.AutoBoneStep
import dev.slimevr.autobone.PoseFrameStep
import dev.slimevr.tracking.processor.skeleton.HumanSkeleton
import dev.slimevr.tracking.trackers.Tracker
import dev.slimevr.tracking.trackers.TrackerRole
import kotlin.math.*

// The change in distance between both of the ankles over time
class OffsetSlideError : IAutoBoneError {
	@Throws(AutoBoneException::class)
	override fun getStepError(step: PoseFrameStep<AutoBoneStep>): Float = getSlideError(
		step.skeleton1.skeleton,
		step.skeleton2.skeleton,
	)

	companion object {
		fun getSlideError(skeleton1: HumanSkeleton, skeleton2: HumanSkeleton): Float {
			val leftTracker1: Tracker = skeleton1.getComputedTracker(TrackerRole.LEFT_FOOT)
			val rightTracker1: Tracker = skeleton1.getComputedTracker(TrackerRole.RIGHT_FOOT)
			val leftTracker2: Tracker = skeleton2.getComputedTracker(TrackerRole.LEFT_FOOT)
			val rightTracker2: Tracker = skeleton2.getComputedTracker(TrackerRole.RIGHT_FOOT)
			return getSlideError(leftTracker1, rightTracker1, leftTracker2, rightTracker2)
		}

		fun getSlideError(
			leftTracker1: Tracker,
			rightTracker1: Tracker,
			leftTracker2: Tracker,
			rightTracker2: Tracker,
		): Float {
			val leftFoot1 = leftTracker1.position
			val rightFoot1 = rightTracker1.position
			val leftFoot2 = leftTracker2.position
			val rightFoot2 = rightTracker2.position
			val slideDist1 = (rightFoot1 - leftFoot1).len()
			val slideDist2 = (rightFoot2 - leftFoot2).len()
			val slideDist3 = (rightFoot2 - leftFoot1).len()
			val slideDist4 = (rightFoot1 - leftFoot2).len()

			// Compute all combinations of distances
			val dist1 = abs(slideDist1 - slideDist2)
			val dist2 = abs(slideDist1 - slideDist3)
			val dist3 = abs(slideDist1 - slideDist4)
			val dist4 = abs(slideDist2 - slideDist3)
			val dist5 = abs(slideDist2 - slideDist4)
			val dist6 = abs(slideDist3 - slideDist4)

			// Divide by 12 (6 values * 2 to halve) to halve and average, it's
			// halved because you want to approach a midpoint, not the other point
			return (dist1 + dist2 + dist3 + dist4 + dist5 + dist6) / 12f
		}
	}
}
