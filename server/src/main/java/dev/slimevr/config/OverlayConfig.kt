package dev.slimevr.config

import kotlinx.serialization.Serializable

@Serializable
class OverlayConfig {
	@JvmField
	var mirrored = false

	@JvmField
	var visible = false
}
