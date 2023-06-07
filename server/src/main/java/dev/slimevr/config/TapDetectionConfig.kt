package dev.slimevr.config

import com.jme3.math.FastMath
import kotlinx.serialization.Serializable

// handles the tap detection config
// this involves the number of taps, the delay, and whether or not the feature is enabled
// for each reset type
@Serializable
class TapDetectionConfig {
	var yawResetDelay = 0.2f
	var fullResetDelay = 1.0f
	var mountingResetDelay = 1.0f
	var yawResetEnabled = true
	var fullResetEnabled = true
	var mountingResetEnabled = true
	var setupMode = false
	var yawResetTaps = 2
		// clamp to 2-3 to prevent errors
		set(yawResetTaps) {
			field = FastMath.clamp(yawResetTaps.toFloat(), 2f, 10f).toInt()
			field = yawResetTaps
		}
	var fullResetTaps = 3
		set(fullResetTaps) {
			field = FastMath.clamp(fullResetTaps.toFloat(), 2f, 10f).toInt()
			field = fullResetTaps
		}
	var mountingResetTaps = 3
		set(mountingResetTaps) {
			field = FastMath.clamp(mountingResetTaps.toFloat(), 2f, 10f).toInt()
			field = mountingResetTaps
		}
	var numberTrackersOverThreshold = 1
}
