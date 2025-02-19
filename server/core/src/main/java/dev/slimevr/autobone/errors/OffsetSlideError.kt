package dev.slimevr.autobone.errors

import dev.slimevr.autobone.AutoBoneStep
import dev.slimevr.tracking.processor.BoneType
import dev.slimevr.tracking.processor.skeleton.HumanSkeleton
import io.github.axisangles.ktmath.Vector3
import kotlin.math.*

// The change in distance between both of the ankles over time
class OffsetSlideError : IAutoBoneError {
	@Throws(AutoBoneException::class)
	override fun getStepError(trainingStep: AutoBoneStep): Float = getSlideError(
		trainingStep.skeleton1.skeleton,
		trainingStep.skeleton2.skeleton,
	)

	companion object {
		fun getSlideError(skeleton1: HumanSkeleton, skeleton2: HumanSkeleton): Float = getSlideError(
			skeleton1.getBone(BoneType.LEFT_LOWER_LEG).getTailPosition(),
			skeleton1.getBone(BoneType.RIGHT_LOWER_LEG).getTailPosition(),
			skeleton2.getBone(BoneType.LEFT_LOWER_LEG).getTailPosition(),
			skeleton2.getBone(BoneType.RIGHT_LOWER_LEG).getTailPosition(),
		)

		fun getSlideError(
			leftFoot1: Vector3,
			rightFoot1: Vector3,
			leftFoot2: Vector3,
			rightFoot2: Vector3,
		): Float {
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
