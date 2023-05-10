package dev.slimevr.config

class VMCConfig : // Address of the VRM to be used
	OSCConfig() {
	// Anchor the tracking at the hip?

	private var portIn: Int = 39540
	private var portOut: Int = 39539

	var anchorHip = true

	var vrmJson: String? = null

	override fun getPortIn(): Int {
		return portIn
	}

	override fun setPortIn(portIn: Int) {
		this.portIn = portIn
	}

	override fun getPortOut(): Int {
		return portOut
	}

	override fun setPortOut(portOut: Int) {
		this.portOut = portOut
	}
}
