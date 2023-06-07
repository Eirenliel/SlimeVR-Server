package dev.slimevr.config

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers
import dev.slimevr.config.serializers.BooleanMapDeserializer
import dev.slimevr.config.serializers.FloatMapDeserializer
import kotlinx.serialization.Serializable

@Serializable
class SkeletonConfig {
	@JvmField
	@JsonDeserialize(using = BooleanMapDeserializer::class)
	@JsonSerialize(keyUsing = StdKeySerializers.StringKeySerializer::class)
	val toggles: MutableMap<String, Boolean> = HashMap()

	@JvmField
	@JsonDeserialize(using = FloatMapDeserializer::class)
	@JsonSerialize(keyUsing = StdKeySerializers.StringKeySerializer::class)
	val values: MutableMap<String, Float> = HashMap()

	@JvmField
	@JsonDeserialize(using = FloatMapDeserializer::class)
	@JsonSerialize(keyUsing = StdKeySerializers.StringKeySerializer::class)
	val offsets: MutableMap<String, Float> = HashMap()
}
