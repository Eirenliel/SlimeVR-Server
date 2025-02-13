package dev.slimevr.config

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import dev.slimevr.config.serializers.AngleInDegConversions
import dev.slimevr.math.Angle

class StayAlignedConfig {

	// Apply yaw correction
	var enabled = false

	// Amount of yaw correction
	@JsonSerialize(using = AngleInDegConversions.Serializer::class)
	@JsonDeserialize(using = AngleInDegConversions.Deserializer::class)
	var yawCorrectionPerSec = Angle.ofDeg(0.2f)

	@JsonSerialize(using = AngleInDegConversions.Serializer::class)
	@JsonDeserialize(using = AngleInDegConversions.Deserializer::class)
	var standingUpperLegAngle = Angle.ofDeg(0.0f)

	@JsonSerialize(using = AngleInDegConversions.Serializer::class)
	@JsonDeserialize(using = AngleInDegConversions.Deserializer::class)
	var standingLowerLegAngle = Angle.ofDeg(0.0f)

	@JsonSerialize(using = AngleInDegConversions.Serializer::class)
	@JsonDeserialize(using = AngleInDegConversions.Deserializer::class)
	var standingFootAngle = Angle.ofDeg(0.0f)

	@JsonSerialize(using = AngleInDegConversions.Serializer::class)
	@JsonDeserialize(using = AngleInDegConversions.Deserializer::class)
	var sittingUpperLegAngle = Angle.ofDeg(0.0f)

	@JsonSerialize(using = AngleInDegConversions.Serializer::class)
	@JsonDeserialize(using = AngleInDegConversions.Deserializer::class)
	var sittingLowerLegAngle = Angle.ofDeg(0.0f)

	@JsonSerialize(using = AngleInDegConversions.Serializer::class)
	@JsonDeserialize(using = AngleInDegConversions.Deserializer::class)
	var sittingFootAngle = Angle.ofDeg(0.0f)

	@JsonSerialize(using = AngleInDegConversions.Serializer::class)
	@JsonDeserialize(using = AngleInDegConversions.Deserializer::class)
	var lyingOnBackUpperLegAngle = Angle.ofDeg(0.0f)

	@JsonSerialize(using = AngleInDegConversions.Serializer::class)
	@JsonDeserialize(using = AngleInDegConversions.Deserializer::class)
	var lyingOnBackLowerLegAngle = Angle.ofDeg(0.0f)
}
