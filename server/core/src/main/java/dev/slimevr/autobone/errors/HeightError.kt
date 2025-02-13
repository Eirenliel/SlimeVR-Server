package dev.slimevr.autobone.errors

import dev.slimevr.autobone.AutoBoneStep
import dev.slimevr.autobone.PoseFrameStep
import kotlin.math.*

// The difference from the current height to the target height
class HeightError : IAutoBoneError {
	@Throws(AutoBoneException::class)
	override fun getStepError(step: PoseFrameStep<AutoBoneStep>): Float = getHeightError(
		step.data.hmdHeight,
		step.data.targetHmdHeight,
	)

	fun getHeightError(currentHeight: Float, targetHeight: Float): Float = abs(targetHeight - currentHeight)
}
