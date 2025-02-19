package dev.slimevr.autobone.errors

import dev.slimevr.autobone.AutoBoneStep
import dev.slimevr.tracking.processor.Bone
import dev.slimevr.tracking.processor.BoneType
import dev.slimevr.tracking.processor.skeleton.HumanSkeleton

// The change in position of the ankle over time
class SlideError : IAutoBoneError {
	@Throws(AutoBoneException::class)
	override fun getStepError(trainingStep: AutoBoneStep): Float = getSlideError(
		trainingStep.skeleton1.skeleton,
		trainingStep.skeleton2.skeleton,
	)

	companion object {
		fun getSlideError(skeleton1: HumanSkeleton, skeleton2: HumanSkeleton): Float {
			// Calculate and average between both feet
			return (
				getSlideError(skeleton1, skeleton2, BoneType.LEFT_LOWER_LEG) +
					getSlideError(skeleton1, skeleton2, BoneType.RIGHT_LOWER_LEG)
				) / 2f
		}

		fun getSlideError(
			skeleton1: HumanSkeleton,
			skeleton2: HumanSkeleton,
			bone: BoneType,
		): Float {
			// Calculate and average between both feet
			return getSlideError(
				skeleton1.getBone(bone),
				skeleton2.getBone(bone),
			)
		}

		fun getSlideError(bone1: Bone, bone2: Bone): Float {
			// Return the midpoint distance
			return (bone2.getTailPosition() - bone1.getTailPosition()).len() / 2f
		}
	}
}
