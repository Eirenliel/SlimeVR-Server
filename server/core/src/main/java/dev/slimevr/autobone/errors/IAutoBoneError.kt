package dev.slimevr.autobone.errors

import dev.slimevr.autobone.AutoBoneStep
import dev.slimevr.autobone.PoseFrameStep

interface IAutoBoneError {
	@Throws(AutoBoneException::class)
	fun getStepError(step: PoseFrameStep<AutoBoneStep>): Float
}
