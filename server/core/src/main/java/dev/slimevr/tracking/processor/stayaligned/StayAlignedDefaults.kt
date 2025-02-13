package dev.slimevr.tracking.processor.stayaligned

import dev.slimevr.math.Angle
import dev.slimevr.tracking.processor.stayaligned.skeleton.RelaxedBodyAngles
import dev.slimevr.tracking.processor.stayaligned.state.RestDetector
import kotlin.time.Duration.Companion.seconds

/**
 * All non-user-configurable defaults used by Stay Aligned, so that we can tune the
 * algorithm from a single place.
 */
object StayAlignedDefaults {

	fun restDetector() =
		RestDetector(
			maxRotation = Angle.ofDeg(3.0f),
			minDuration = 2.seconds,
		)

	val RELAXED_BODY_ANGLES_STANDING =
		RelaxedBodyAngles(
			upperLeg = Angle.ofDeg(6.0f),
			lowerLeg = Angle.ofDeg(9.0f),
			foot = Angle.ofDeg(10.0f),
		)

	val RELAXED_BODY_ANGLES_SITTING_IN_CHAIR =
		RelaxedBodyAngles(
			upperLeg = Angle.ofDeg(6.0f),
			lowerLeg = Angle.ofDeg(9.0f),
			foot = Angle.ofDeg(10.0f),
		)

	val RELAXED_BODY_ANGLES_LYING_ON_BACK =
		RelaxedBodyAngles(
			upperLeg = Angle.ofDeg(4.0f),
			lowerLeg = Angle.ofDeg(4.0f),
			foot = null,
		)

	val RELAXED_BODY_ANGLES_KNEELING =
		RelaxedBodyAngles(
			upperLeg = Angle.ofDeg(4.0f),
			lowerLeg = Angle.ofDeg(4.0f),
			foot = null,
		)

	const val NEIGHBOR_ERROR_ABOVE_TRACKER_WEIGHT = 0.7f
	const val NEIGHBOR_ERROR_BELOW_TRACKER_WEIGHT = 0.3f

	const val CENTER_ERROR_HEAD_WEIGHT = 1.0f
	const val CENTER_ERROR_UPPER_BODY_WEIGHT = 1.0f
	const val CENTER_ERROR_UPPER_LEG_WEIGHT = 0.4f
	const val CENTER_ERROR_LOWER_LEG_WEIGHT = 0.3f

	const val YAW_ERRORS_LOCKED_ERROR_WEIGHT = 10.0f
	const val YAW_ERRORS_CENTER_ERROR_WEIGHT = 2.0f
	const val YAW_ERRORS_NEIGHBOR_ERROR_WEIGHT = 1.0f
}
