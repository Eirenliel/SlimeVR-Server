package dev.slimevr.config

import kotlinx.serialization.Serializable

@Serializable
class ServerConfig {
	@JvmField
	val trackerPort = 6969
}
