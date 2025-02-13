package dev.slimevr.autobone.errors

import dev.slimevr.autobone.AutoBoneStep
import dev.slimevr.autobone.PoseFrameStep
import dev.slimevr.autobone.errors.proportions.ProportionLimiter
import dev.slimevr.autobone.errors.proportions.RangeProportionLimiter
import dev.slimevr.tracking.processor.HumanPoseManager
import dev.slimevr.tracking.processor.config.SkeletonConfigManager
import dev.slimevr.tracking.processor.config.SkeletonConfigOffsets
import kotlin.math.*

// The distance from average human proportions
class BodyProportionError : IAutoBoneError {
	@Throws(AutoBoneException::class)
	override fun getStepError(step: PoseFrameStep<AutoBoneStep>): Float = getBodyProportionError(
		step.skeleton1,
		// Skeletons are now normalized to reduce bias, so height is always 1
		1f,
	)

	fun getBodyProportionError(humanPoseManager: HumanPoseManager, fullHeight: Float): Float {
		var sum = 0f
		for (limiter in proportionLimits) {
			sum += abs(limiter.getProportionError(humanPoseManager, fullHeight))
		}
		return sum
	}

	companion object {
		// TODO hip tracker stuff... Hip tracker should be around 3 to 5
		// centimeters.
		// The headset height is not the full height! This value compensates for the
		// offset from the headset height to the user height
		@JvmField
		var eyeHeightToHeightRatio = 0.936f

		private val defaultHeight = SkeletonConfigManager.HEIGHT_OFFSETS.sumOf { it.defaultValue.toDouble() }.toFloat()
		private fun makeLimiter(offset: SkeletonConfigOffsets, range: Float): RangeProportionLimiter = RangeProportionLimiter(
			offset.defaultValue / defaultHeight,
			offset,
			range,
		)

		// "Expected" are values from Drillis and Contini (1966)
		// Default are values from experimentation by the SlimeVR community
		/**
		 * Proportions are based off the headset height (or eye height), not the total height of the user.
		 * To use the total height of the user, multiply it by [eyeHeightToHeightRatio] and use that in the limiters.
		 */
		val proportionLimits = arrayOf<ProportionLimiter>(
			makeLimiter(
				SkeletonConfigOffsets.HEAD,
				0.01f,
			),
			// Expected: 0.052
			makeLimiter(
				SkeletonConfigOffsets.NECK,
				0.002f,
			),
			makeLimiter(
				SkeletonConfigOffsets.UPPER_CHEST,
				0.01f,
			),
			makeLimiter(
				SkeletonConfigOffsets.CHEST,
				0.01f,
			),
			makeLimiter(
				SkeletonConfigOffsets.WAIST,
				0.05f,
			),
			makeLimiter(
				SkeletonConfigOffsets.HIP,
				0.01f,
			),
			// Expected: 0.191
			makeLimiter(
				SkeletonConfigOffsets.HIPS_WIDTH,
				0.04f,
			),
			// Expected: 0.245
			makeLimiter(
				SkeletonConfigOffsets.UPPER_LEG,
				0.02f,
			),
			// Expected: 0.246 (0.285 including below ankle, could use a separate
			// offset?)
			makeLimiter(
				SkeletonConfigOffsets.LOWER_LEG,
				0.02f,
			),
		)

		@JvmStatic
		val proportionLimitMap = proportionLimits.associateBy { it.skeletonConfigOffset }
	}
}
