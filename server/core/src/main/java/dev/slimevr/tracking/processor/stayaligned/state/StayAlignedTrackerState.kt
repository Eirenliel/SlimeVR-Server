package dev.slimevr.tracking.processor.stayaligned.state

import dev.slimevr.tracking.processor.stayaligned.StayAlignedDefaults
import dev.slimevr.tracking.trackers.Tracker
import io.github.axisangles.ktmath.Quaternion

class StayAlignedTrackerState(
	val tracker: Tracker,
) {
	// Detects whether the tracker is at rest
	val restDetector = StayAlignedDefaults.restDetector()

	// Yaw of the tracker when it was locked
	var lockedRotation: Quaternion? = null

	// Yaw correction to apply to tracker rotation
	val yawCorrection = YawCorrection()

	// Alignment error that yaw correction attempts to minimize
	var yawErrors = YawErrors()

	fun update() {
		val atRest = restDetector.update(tracker.getRawRotation())
		if (atRest) {
			if (lockedRotation == null) {
				lockedRotation = tracker.getRotation()
			}
		} else {
			lockedRotation = null
		}
	}

	fun prepareForAdjustment() {
		yawErrors = YawErrors()
	}

	fun reset() {
		restDetector.reset()
		lockedRotation = null
		yawCorrection.reset()
		yawErrors = YawErrors()
	}
}
