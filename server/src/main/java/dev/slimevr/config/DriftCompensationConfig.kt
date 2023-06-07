package dev.slimevr.config

import dev.slimevr.vrServer
import kotlinx.serialization.Serializable

@Serializable
class DriftCompensationConfig {

	// Is drift compensation enabled
	var enabled = false

	// Amount of drift compensation applied
	var amount = 0.8f

	// Max resets for the calculated average drift
	var maxResets = 6

	fun updateTrackersDriftCompensation() {
		for (t in vrServer.allTrackers) {
			if (t.allowFiltering) {
				t.resetsHandler.readDriftCompensationConfig(this)
			}
		}
	}
}
