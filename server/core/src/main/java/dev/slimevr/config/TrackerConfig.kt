package dev.slimevr.config

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import dev.slimevr.tracking.trackers.Tracker
import io.github.axisangles.ktmath.Quaternion

class TrackerConfig {
	var customName: String? = null
	var designation: String? = null
	var isHide = false
	var adjustment: Quaternion? = null
	var mountingOrientation: Quaternion? = null
	var allowDriftCompensation: Boolean? = null
	var accessoryId: Int? = null

	constructor()
	constructor(tracker: Tracker) {
		designation = if (tracker.trackerPosition
			!= null
		) {
			tracker.trackerPosition!!.designation
		} else {
			null
		}
		customName = tracker.customName
		allowDriftCompensation = tracker.isImu()
	}

	companion object {
		@JvmStatic
		fun toV2(v1: JsonNode, factory: JsonNodeFactory): JsonNode {
			val node = factory.objectNode()
			if (v1.has("customName")) node.set<JsonNode>("customName", v1["customName"])
			if (v1.has("designation")) node.set<JsonNode>("designation", v1["designation"])
			if (v1.has("hide")) node.set<JsonNode>("hide", v1["hide"])
			if (v1.has("mountingRotation")) node.set<JsonNode>("mountingRotation", v1["mountingRotation"])
			if (v1.has("adjustment")) node.set<JsonNode>("adjustment", v1["adjustment"])
			return node
		}
	}
}
