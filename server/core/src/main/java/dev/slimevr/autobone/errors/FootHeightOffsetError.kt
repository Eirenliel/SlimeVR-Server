package dev.slimevr.autobone.errors

import dev.slimevr.autobone.AutoBoneStep
import dev.slimevr.tracking.processor.BoneType
import dev.slimevr.tracking.processor.skeleton.HumanSkeleton
import io.github.axisangles.ktmath.Vector3
import kotlin.math.*

// The offset between the height both feet at one instant and over time
class FootHeightOffsetError : IAutoBoneError {
	@Throws(AutoBoneException::class)
	override fun getStepError(trainingStep: AutoBoneStep): Float = getSlideError(
		trainingStep.skeleton1.skeleton,
		trainingStep.skeleton2.skeleton,
	)

	companion object {
		fun getSlideError(skeleton1: HumanSkeleton, skeleton2: HumanSkeleton): Float = getFootHeightError(
			skeleton1.getBone(BoneType.LEFT_LOWER_LEG).getTailPosition(),
			skeleton1.getBone(BoneType.RIGHT_LOWER_LEG).getTailPosition(),
			skeleton2.getBone(BoneType.LEFT_LOWER_LEG).getTailPosition(),
			skeleton2.getBone(BoneType.RIGHT_LOWER_LEG).getTailPosition(),
		)

		fun getFootHeightError(
			leftFoot1: Vector3,
			rightFoot1: Vector3,
			leftFoot2: Vector3,
			rightFoot2: Vector3,
		): Float {
			val lFoot1Y = leftFoot1.y
			val rFoot1Y = rightFoot1.y
			val lFoot2Y = leftFoot2.y
			val rFoot2Y = rightFoot2.y

			// Compute all combinations of heights
			val dist1 = abs(lFoot1Y - rFoot1Y)
			val dist2 = abs(lFoot1Y - lFoot2Y)
			val dist3 = abs(lFoot1Y - rFoot2Y)
			val dist4 = abs(rFoot1Y - lFoot2Y)
			val dist5 = abs(rFoot1Y - rFoot2Y)
			val dist6 = abs(lFoot2Y - rFoot2Y)

			// Divide by 12 (6 values * 2 to halve) to halve and average, it's
			// halved because you want to approach a midpoint, not the other point
			return (dist1 + dist2 + dist3 + dist4 + dist5 + dist6) / 12f
		}
	}
}
