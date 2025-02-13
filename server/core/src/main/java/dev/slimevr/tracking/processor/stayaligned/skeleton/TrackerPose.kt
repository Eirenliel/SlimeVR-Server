package dev.slimevr.tracking.processor.stayaligned.skeleton

import dev.slimevr.tracking.trackers.Tracker
import io.github.axisangles.ktmath.Vector3
import kotlin.math.*

enum class TrackerPose {
	NONE,
	TOP_FACING_UP,
	TOP_FACING_DOWN,
	FRONT_FACING_UP,
	FRONT_FACING_DOWN,
	ON_SIDE,
	;

	companion object {

		fun ofTracker(tracker: Tracker): TrackerPose {
			val rotation = tracker.getRotation()

			val x = rotation.sandwichUnitX()
			val y = rotation.sandwichUnitY()
			val z = rotation.sandwichUnitZ()

			val xDot = x.dot(Vector3.POS_Y)
			val yDot = y.dot(Vector3.POS_Y)
			val zDot = z.dot(Vector3.POS_Y)

			val xAbsDot = abs(xDot)
			val yAbsDot = abs(yDot)
			val zAbsDot = abs(zDot)

			val pose =
				if ((xAbsDot >= yAbsDot) && (xAbsDot >= zAbsDot)) {
					ON_SIDE
				} else if ((yAbsDot >= xDot) && (yAbsDot >= zAbsDot)) {
					if (yDot >= 0) {
						TOP_FACING_UP
					} else {
						TOP_FACING_DOWN
					}
				} else {
					// Tracker local POS_Z is behind
					if (zDot >= 0) {
						FRONT_FACING_DOWN
					} else {
						FRONT_FACING_UP
					}
				}

			return pose
		}
	}
}
