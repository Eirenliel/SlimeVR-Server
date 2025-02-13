package dev.slimevr.tracking.processor.stayaligned.skeleton

import dev.slimevr.config.StayAlignedConfig
import dev.slimevr.math.Angle
import dev.slimevr.tracking.processor.stayaligned.StayAlignedDefaults

class RelaxedBodyAngles(
	val upperLeg: Angle,
	val lowerLeg: Angle,
	val foot: Angle?,
) {
	companion object {

		/**
		 * Gets the relaxed angles for a particular pose. May provide defaults if the
		 * angles aren't configured for the pose.
		 */
		fun forPose(
			pose: TrackerSkeletonPose,
			config: StayAlignedConfig,
		) =
			when (pose) {
				TrackerSkeletonPose.STANDING ->
					fromConfig(
						config.standingUpperLegAngle,
						config.standingLowerLegAngle,
						config.standingFootAngle,
					) ?: StayAlignedDefaults.RELAXED_BODY_ANGLES_STANDING

				TrackerSkeletonPose.SITTING_IN_CHAIR ->
					fromConfig(
						config.sittingUpperLegAngle,
						config.sittingLowerLegAngle,
						config.sittingFootAngle,
					) ?: StayAlignedDefaults.RELAXED_BODY_ANGLES_SITTING_IN_CHAIR

				TrackerSkeletonPose.SITTING_ON_GROUND,
				TrackerSkeletonPose.LYING_ON_BACK,
				->
					fromConfig(
						config.lyingOnBackUpperLegAngle,
						config.lyingOnBackLowerLegAngle,
						null,
					) ?: StayAlignedDefaults.RELAXED_BODY_ANGLES_LYING_ON_BACK

				TrackerSkeletonPose.KNEELING ->
					StayAlignedDefaults.RELAXED_BODY_ANGLES_KNEELING

				else ->
					null
			}

		private fun fromConfig(
			upperLegAngle: Angle,
			lowerLegAngle: Angle,
			footAngle: Angle?,
		): RelaxedBodyAngles? {
			if (
				upperLegAngle.nearZero() &&
				lowerLegAngle.nearZero() &&
				(footAngle == null || footAngle.nearZero())
			) {
				return null
			}

			return RelaxedBodyAngles(upperLegAngle, lowerLegAngle, footAngle)
		}
	}
}
