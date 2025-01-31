package dev.slimevr.tracking.trackers

import dev.slimevr.config.FiltersConfig
import dev.slimevr.filtering.QuaternionMovingAverage
import dev.slimevr.filtering.TrackerFilters
import io.github.axisangles.ktmath.Quaternion

/**
 * Class taking care of filtering logic
 * (smoothing and prediction)
 * See QuaternionMovingAverage.kt for the quaternion math.
 */
class TrackerFilteringHandler {
	// Instantiated by default in case config doesn't get read (if tracker doesn't support filtering)
	private var movingAverage: QuaternionMovingAverage? = null
	var filteringEnabled = false

	/**
	 * Reads/loads filtering settings from given config
	 */
	fun readFilteringConfig(config: FiltersConfig, currentRawRotation: Quaternion) {
		val type = TrackerFilters.getByConfigkey(config.type)
		if (type == TrackerFilters.SMOOTHING || type == TrackerFilters.PREDICTION) {
			movingAverage = QuaternionMovingAverage(
				type,
				config.amount,
				currentRawRotation,
			)
			filteringEnabled = true
		}
	}

	/**
	 * Update the moving average to make it smooth
	 */
	fun update() {
		movingAverage?.update()
	}

	/**
	 * Updates the latest rotation
	 */
	fun dataTick(currentRawRotation: Quaternion) {
		movingAverage?.addQuaternion(currentRawRotation)
	}

	/**
	 * Call when doing a full reset to reset the tracking of rotations >180 degrees
	 */
	fun resetMovingAverage(currentRawRotation: Quaternion) {
		movingAverage?.resetQuats(currentRawRotation)
	}

	/**
	 * Get the filtered rotation from the moving average
	 */
	fun getFilteredRotation() = movingAverage?.filteredQuaternion ?: Quaternion.IDENTITY

	/**
	 * Get the impact filtering has on the rotation
	 */
	fun getFilteringImpact(): Float = movingAverage?.filteringImpact ?: 0f
}
