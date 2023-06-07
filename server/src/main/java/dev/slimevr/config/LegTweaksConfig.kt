package dev.slimevr.config

import kotlinx.serialization.Serializable

@Serializable
class LegTweaksConfig {
	@JvmField
	var correctionStrength = 0.3f
}
