package dev.slimevr.tracking.processor.stayaligned.state

import dev.slimevr.math.Angle

class YawErrors {
	var lockedError = Angle.ZERO
	var centerError = Angle.ZERO
	var neighborError = Angle.ZERO
}
