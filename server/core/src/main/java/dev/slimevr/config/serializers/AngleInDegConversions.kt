package dev.slimevr.config.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dev.slimevr.math.Angle

object AngleInDegConversions {

	object Serializer : JsonSerializer<Angle>() {
		override fun serialize(
			value: Angle,
			generator: JsonGenerator,
			serializer: SerializerProvider,
		) {
			generator.writeNumber(value.toDeg())
		}
	}

	object Deserializer : JsonDeserializer<Angle>() {
		override fun deserialize(parser: JsonParser, context: DeserializationContext): Angle =
			Angle.ofDeg(parser.numberValue.toFloat())
	}
}
