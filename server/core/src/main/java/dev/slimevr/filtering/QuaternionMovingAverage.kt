package dev.slimevr.filtering

import dev.slimevr.VRServer
import io.github.axisangles.ktmath.Quaternion
import io.github.axisangles.ktmath.Quaternion.Companion.IDENTITY

// influences the range of smoothFactor.
private const val SMOOTH_MULTIPLIER = 42f
private const val SMOOTH_MIN = 11f

// influences the range of predictFactor
private const val PREDICT_MULTIPLIER = 15f
private const val PREDICT_MIN = 10f

// how many past rotations are used for prediction.
private const val PREDICT_BUFFER = 6

class QuaternionMovingAverage(
	val type: TrackerFilters,
	var amount: Float,
	initialRotation: Quaternion,
) {
	var filteredQuaternion = IDENTITY
	var filteringImpact = 0f
	private var smoothFactor = 0f
	private var predictFactor = 0f
	private lateinit var rotBuffer: CircularArrayList<Quaternion>
	private var latestQuaternion = IDENTITY
	private var smoothingQuaternion = IDENTITY
	private val fpsTimer = VRServer.instance.fpsTimer
	private var frameCounter = 0
	private var lastAmt = 0f

	init {
		// amount should range from 0 to 1.
		// GUI should clamp it from 0.01 (1%) or 0.1 (10%)
		// to 1 (100%).
		amount = amount.coerceAtLeast(0f)
		if (type == TrackerFilters.SMOOTHING) {
			// lower smoothFactor = more smoothing
			smoothFactor = SMOOTH_MULTIPLIER * (1 - amount.coerceAtMost(1f)) + SMOOTH_MIN
			// Totally a hack
			if (amount > 1) {
				smoothFactor /= amount
			}
		}
		if (type == TrackerFilters.PREDICTION) {
			// higher predictFactor = more prediction
			predictFactor = PREDICT_MULTIPLIER * amount + PREDICT_MIN
			rotBuffer = CircularArrayList(PREDICT_BUFFER)
		}
		filteredQuaternion = initialRotation
		latestQuaternion = initialRotation
		smoothingQuaternion = initialRotation
	}

	// Runs at up to 1000hz. We use a timer to make it framerate-independent
	// since it runs a bit below 1000hz in practice.
	@Synchronized
	fun update() {
		if (type == TrackerFilters.PREDICTION) {
			if (rotBuffer.size > 0) {
				var quatBuf = latestQuaternion

				// Applies the past rotations to the current rotation
				rotBuffer.forEach { quatBuf *= it }

				// Calculate how much to slerp
				val amt = predictFactor * fpsTimer.timePerFrame

				// Slerps the target rotation to that predicted rotation by amt
				filteredQuaternion = filteredQuaternion.interpR(quatBuf, amt)
			}
		} else { // Smoothing
			// Increase every update for linear interpolation
			frameCounter++

			// Calculate the slerp factor based off the smoothFactor and smoothingCounter
			var amt = smoothFactor * frameCounter

			// Make it framerate-independent
			amt *= fpsTimer.timePerFrame

			// Be at least last amount to not rollback
			amt = amt.coerceAtLeast(lastAmt)

			// limit to 1 to not overshoot
			amt = amt.coerceAtMost(1f)

			lastAmt = amt

			// Smooth towards the target rotation by the slerp factor
			filteredQuaternion = smoothingQuaternion.interpR(latestQuaternion, amt)
		}

		filteringImpact = latestQuaternion.angleToR(filteredQuaternion)
	}

	@Synchronized
	fun addQuaternion(q: Quaternion) {
		if (type == TrackerFilters.PREDICTION) {
			if (rotBuffer.size == rotBuffer.capacity()) {
				rotBuffer.removeLast()
			}

			// Gets and stores the rotation between the last 2 quaternions
			rotBuffer.add(latestQuaternion.inv().times(q))
		} else { // Smoothing
			frameCounter = 0
			lastAmt = 0f
			smoothingQuaternion = filteredQuaternion
		}

		latestQuaternion = q
	}
}
