package dev.slimevr.tracking.processor.stayaligned.state

import dev.slimevr.math.Angle
import io.github.axisangles.ktmath.Quaternion
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * Detects whether a tracker is at rest.
 *
 * A tracker is at rest when it stays within a certain rotational range for a given
 * amount of time. If it rotates past that range, it is no longer at rest.
 *
 * TODO: In practice this is good enough for Stay Aligned, but we could also consider
 * 		acceleration if we want to make this a general purpose rest detector.
 */
class RestDetector(
	private val maxRotation: Angle,
	private val minDuration: Duration,
) {
	private var startAt = TimeSource.Monotonic.markNow()
	private var startRotation = Quaternion.IDENTITY
	private var atRest = false

	/**
	 * Resets the detector so that the tracker is no longer at rest
	 */
	fun reset(rotation: Quaternion = Quaternion.IDENTITY) {
		startAt = TimeSource.Monotonic.markNow()
		startRotation = rotation
		atRest = false
	}

	/**
	 * Provides a new rotation sample to the detector.
	 *
	 * @return whether the tracker is at rest
	 */
	fun update(rotation: Quaternion): Boolean {
		if (Angle.absBetween(startRotation, rotation) < maxRotation) {
			// When we detect the tracker is at rest, use the current rotation as the
			// new start rotation for continuing to detect the tracker is at rest
			if (!atRest &&
				TimeSource.Monotonic.markNow() > startAt.plus(minDuration)
			) {
				startRotation = rotation
				atRest = true
			}
		} else {
			reset(rotation)
		}

		return atRest
	}
}
