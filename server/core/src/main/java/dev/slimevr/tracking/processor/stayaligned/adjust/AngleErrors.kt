package dev.slimevr.tracking.processor.stayaligned.adjust

import dev.slimevr.math.Angle
import kotlin.math.*

/**
 * Aggregator for angle errors.
 */
class AngleErrors {

	private var sumSqrError = 0.0f

	/**
	 * Adds an angle error.
	 */
	fun add(error: Angle, weight: Float = 1.0f) {
		val weightedError = error.normalizedAroundZero().toRad() * weight
		sumSqrError += weightedError * weightedError
	}

	/**
	 * Gets the L2-norm of the angle errors.
	 *
	 * We use the L2 norm of the errors so that big errors are much worse than
	 * small errors. For example, when balancing an upper body tracker in
	 * between two leg trackers, we want the error to be minimized when the
	 * upper body tracker is midway between the two leg trackers.
	 */
	fun toL2Norm() =
		Angle.ofRad(sqrt(sumSqrError))
}
