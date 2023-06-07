package dev.slimevr.config

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers
import dev.slimevr.config.serializers.BooleanMapDeserializer
import dev.slimevr.tracking.trackers.TrackerRole
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
class BridgeConfig {
	@JsonDeserialize(using = BooleanMapDeserializer::class)
	@JsonSerialize(keyUsing = StdKeySerializers.StringKeySerializer::class)
	var trackers: MutableMap<String, Boolean> = HashMap()
	fun getBridgeTrackerRole(role: TrackerRole, def: Boolean): Boolean {
		return trackers.getOrDefault(role.name.lowercase(Locale.getDefault()), def)
	}

	fun setBridgeTrackerRole(role: TrackerRole, `val`: Boolean) {
		trackers[role.name.lowercase(Locale.getDefault())] = `val`
	}
}
