package dev.slimevr.tracking.processor.stayaligned.state

import dev.slimevr.math.Angle
import io.github.axisangles.ktmath.EulerAngles
import io.github.axisangles.ktmath.EulerOrder
import io.github.axisangles.ktmath.Quaternion

/**
 * Tracks the yaw correction that should be applied to a tracker.
 */
class YawCorrection {

	var yaw = Angle.ZERO
		set(value) {
			field = value
			yawRotation = EulerAngles(EulerOrder.YZX, 0.0f, value.toRad(), 0.0f).toQuaternion()
		}

	var yawRotation = Quaternion.IDENTITY
		private set

	var yawAtLastReset = Angle.ZERO
		private set

	fun reset() {
		yawAtLastReset = yaw

		yaw = Angle.ZERO
		yawRotation = Quaternion.IDENTITY
	}
}
