package dev.slimevr.config

import kotlinx.serialization.Serializable

@Serializable
open class OSCConfig {

	// Are the OSC receiver and sender enabled?
	var enabled = false

	// Port to receive OSC messages from
	var portIn = 0

	// Port to send out OSC messages at
	var portOut = 0

	// Address to send out OSC messages at
	var address = "127.0.0.1"
}
