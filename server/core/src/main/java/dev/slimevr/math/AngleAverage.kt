package dev.slimevr.math

import kotlin.math.*

/**
 * Averages angles by summing vectors.
 *
 * See https://www.themathdoctors.org/averaging-angles/
 */
class AngleAverage {

	private var sumX = 0.0f
	private var sumY = 0.0f

	fun add(angle: Angle, weight: Float = 1.0f) {
		sumX += cos(angle.toRad()) * weight
		sumY += sin(angle.toRad()) * weight
	}

	fun toAngle(): Angle =
		Angle.ofRad(atan2(sumY, sumX))
}
