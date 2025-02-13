package dev.slimevr.autobone.errors

import dev.slimevr.autobone.AutoBoneStep
import dev.slimevr.autobone.PoseFrameStep
import dev.slimevr.poseframeformat.trackerdata.TrackerFrames
import dev.slimevr.tracking.processor.skeleton.HumanSkeleton

// The distance of any points to the corresponding absolute position
class PositionError : IAutoBoneError {
	@Throws(AutoBoneException::class)
	override fun getStepError(step: PoseFrameStep<AutoBoneStep>): Float {
		val trackers = step.frames.frameHolders
		return (
			(
				getPositionError(
					trackers,
					step.cursor1,
					step.skeleton1.skeleton,
				) +
					getPositionError(
						trackers,
						step.cursor2,
						step.skeleton2.skeleton,
					)
				) /
				2f
			)
	}

	companion object {
		fun getPositionError(
			trackers: List<TrackerFrames>,
			cursor: Int,
			skeleton: HumanSkeleton,
		): Float {
			var offset = 0f
			var offsetCount = 0
			for (tracker in trackers) {
				val trackerFrame = tracker.tryGetFrame(cursor) ?: continue
				val position = trackerFrame.tryGetPosition() ?: continue
				val trackerRole = trackerFrame.tryGetTrackerPosition()?.trackerRole ?: continue

				try {
					val computedTracker = skeleton.getComputedTracker(trackerRole)

					offset += (position - computedTracker.position).len()
					offsetCount++
				} catch (_: Exception) {
					// Ignore unsupported positions
				}
			}
			return if (offsetCount > 0) offset / offsetCount else 0f
		}
	}
}
