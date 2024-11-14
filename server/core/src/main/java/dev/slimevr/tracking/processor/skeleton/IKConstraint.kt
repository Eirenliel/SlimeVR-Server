package dev.slimevr.tracking.processor.skeleton

import dev.slimevr.tracking.trackers.Tracker
import io.github.axisangles.ktmath.Quaternion
import io.github.axisangles.ktmath.Vector3
import solarxr_protocol.datatypes.BodyPart

class IKConstraint(val tracker: Tracker) {
	private var offset = Vector3.NULL
	private var rotationOffset = Quaternion.IDENTITY

	fun getPosition(): Vector3 =
		tracker.position + (tracker.getRotation() * rotationOffset).sandwich(offset)

	fun reset(nodePosition: Vector3) {
		val bodyPartsToSkip = setOf(BodyPart.LEFT_HAND, BodyPart.RIGHT_HAND)

		rotationOffset = tracker.getRotation().inv()
		if (tracker.trackerPosition?.bodyPart in bodyPartsToSkip) return
		offset = nodePosition - tracker.position
	}
}
