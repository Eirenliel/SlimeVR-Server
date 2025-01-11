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

	private var filteringMovingAverage: QuaternionMovingAverage? = null
	private var trackingMovingAverage = QuaternionMovingAverage(TrackerFilters.NONE)
	var filteringEnabled = false

	/**
	 * Reads/loads filtering settings from given config
	 */
	fun readFilteringConfig(config: FiltersConfig, currentRawRotation: Quaternion) {
		val type = TrackerFilters.getByConfigkey(config.type)
		if (type == TrackerFilters.SMOOTHING || type == TrackerFilters.PREDICTION) {
			filteringMovingAverage = QuaternionMovingAverage(
				type,
				config.amount,
				currentRawRotation,
			)
			filteringEnabled = true
		} else {
			filteringMovingAverage = null
			filteringEnabled = false
		}
	}

	/**
	 * Update the moving average to make it smooth
	 */
	fun update() {
		trackingMovingAverage.update()
		filteringMovingAverage?.update()
	}

	/**
	 * Updates the latest rotation
	 */
	fun dataTick(currentRawRotation: Quaternion) {
		trackingMovingAverage.addQuaternion(currentRawRotation)
		filteringMovingAverage?.addQuaternion(currentRawRotation)
	}

	/**
	 * Call when doing a full reset to reset the tracking of rotations >180 degrees
	 */
	fun resetQuats(currentRawRotation: Quaternion) {
		trackingMovingAverage.resetQuats(currentRawRotation)
		filteringMovingAverage?.resetQuats(currentRawRotation)
	}

	/**
	 * Gets the tracked rotation from the moving average (allows >180 degrees)
	 */
	fun getTrackedRotation() = trackingMovingAverage.filteredQuaternion

	/**
	 * Get the filtered rotation from the moving average
	 */
	fun getFilteredRotation() = filteringMovingAverage?.filteredQuaternion ?: Quaternion.IDENTITY

	/**
	 * Get the impact filtering has on the rotation
	 */
	fun getFilteringImpact(): Float = filteringMovingAverage?.filteringImpact ?: 0f
}
