package dev.slimevr.autobone.errors

import dev.slimevr.autobone.AutoBoneStep
import dev.slimevr.autobone.PoseFrameStep
import dev.slimevr.tracking.processor.skeleton.HumanSkeleton
import dev.slimevr.tracking.trackers.Tracker
import dev.slimevr.tracking.trackers.TrackerRole

// The change in position of the ankle over time
class SlideError : IAutoBoneError {
	@Throws(AutoBoneException::class)
	override fun getStepError(step: PoseFrameStep<AutoBoneStep>): Float = getSlideError(
		step.skeleton1.skeleton,
		step.skeleton2.skeleton,
	)

	companion object {
		fun getSlideError(skeleton1: HumanSkeleton, skeleton2: HumanSkeleton): Float {
			// Calculate and average between both feet
			return (
				getSlideError(skeleton1, skeleton2, TrackerRole.LEFT_FOOT) +
					getSlideError(skeleton1, skeleton2, TrackerRole.RIGHT_FOOT)
				) / 2f
		}

		fun getSlideError(
			skeleton1: HumanSkeleton,
			skeleton2: HumanSkeleton,
			trackerRole: TrackerRole,
		): Float {
			// Calculate and average between both feet
			return getSlideError(
				skeleton1.getComputedTracker(trackerRole),
				skeleton2.getComputedTracker(trackerRole),
			)
		}

		fun getSlideError(tracker1: Tracker, tracker2: Tracker): Float {
			// Return the midpoint distance
			return (tracker2.position - tracker1.position).len() / 2f
		}
	}
}
