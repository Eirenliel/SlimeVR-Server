package dev.slimevr.config

import dev.slimevr.vrServer
import kotlinx.serialization.Serializable

@Serializable
class FiltersConfig {

	// Type of filtering applied (none, smoothing or prediction)
	var type = "prediction"

	// Amount/Intensity of the specified filtering (0 to 1)
	var amount = 0.2f

	fun updateTrackersFilters() {
		for (tracker in vrServer.allTrackers) {
			if (tracker.allowFiltering) {
				tracker.filteringHandler.readFilteringConfig(this, tracker.getRawRotation())
			}
		}
	}
}
