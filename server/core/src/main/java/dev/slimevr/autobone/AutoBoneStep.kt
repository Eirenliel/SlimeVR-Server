package dev.slimevr.autobone

class AutoBoneStep(
	var hmdHeight: Float = 1f,
	val targetHmdHeight: Float = 1f,
	var adjustRate: Float = 0f,
) {

	val errorStats = StatsCalculator()

	val heightOffset: Float
		get() = targetHmdHeight - hmdHeight
}
