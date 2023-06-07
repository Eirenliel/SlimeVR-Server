package dev.slimevr.config

import kotlinx.serialization.Serializable

@Serializable
class KeybindingsConfig {
	@JvmField
	val fullResetBinding = "CTRL+ALT+SHIFT+Y"

	@JvmField
	val yawResetBinding = "CTRL+ALT+SHIFT+U"

	@JvmField
	val mountingResetBinding = "CTRL+ALT+SHIFT+I"

	@JvmField
	var fullResetDelay = 0L

	@JvmField
	var yawResetDelay = 0L

	@JvmField
	var mountingResetDelay = 0L
}
