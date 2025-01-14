package dev.slimevr.tracking.processor.skeleton

import com.jme3.math.FastMath
import dev.slimevr.VRServer
import dev.slimevr.config.YawCorrectionConfig
import dev.slimevr.tracking.trackers.Tracker
import io.github.axisangles.ktmath.EulerOrder
import io.github.axisangles.ktmath.Quaternion
import io.github.axisangles.ktmath.Vector3.Companion.POS_Y
import kotlin.math.*

class SpineYawCorrection {

	private var config = YawCorrectionConfig()
	private var upperBodyTrackers: List<Tracker> = listOf()

	fun setTrackers(skeleton: HumanSkeleton) {
		upperBodyTrackers = filterImuTrackers(
			skeleton.headTracker,
			skeleton.neckTracker,
			skeleton.upperChestTracker,
			skeleton.chestTracker,
			skeleton.waistTracker,
			skeleton.hipTracker,
		)
	}

	fun setConfig(config: YawCorrectionConfig) {
		this.config = config
	}

	fun updateTrackers() {
		if (!config.enabled) {
			return
		}

		updateTrackers(upperBodyTrackers)
	}

	private fun updateTrackers(trackers: List<Tracker>) {
		for ((parentTracker, tracker) in trackers.zipWithNext()) {
			updateTracker(tracker, parentTracker)
		}
	}

	private fun updateTracker(tracker: Tracker, parentTracker: Tracker) {
		val trackerRot = tracker.getRotation()
		val parentTrackerRot = parentTracker.getRotation()

		// For now, we only handle trackers which are relatively "upright", i.e. in the
		// standing position, or sitting position for trackers that are high up on the
		// spine. When someone is lying down, they could be curled up such that the yaws
		// don't necessarily align.
		val maxDeviationInRad = 30.0f * FastMath.DEG_TO_RAD
		if (!isTrackerPointingUp(trackerRot, maxDeviationInRad) ||
			!isTrackerPointingUp(parentTrackerRot, maxDeviationInRad)
		) {
			return
		}

		val deltaRot = trackerRot * parentTrackerRot.inv()
		val deltaYawInRad = deltaRot.toEulerAngles(EulerOrder.YZX).y

		// Amount of yaw should be roughly the maximum yaw bias of the gyroscope. If it
		// is too small, the gyroscope will overpower the correction and the skeleton
		// will drift. If it is too big, the player will notice that the skeleton is
		// rotating when the player doesn't face forward for a long time.
		val adjustYawInRad = -sign(deltaYawInRad) * (config.amountInDegPerSec * FastMath.DEG_TO_RAD) * VRServer.instance.fpsTimer.timePerFrame

		// Adjust the tracker's yaw towards the parent tracker's yaw
		tracker.resetsHandler.spineYawCorrectionInRad += adjustYawInRad
	}

	private fun isTrackerPointingUp(trackerRot: Quaternion, maxDeviationInRad: Float): Boolean {
		val trackerUp = trackerRot.sandwich(POS_Y)
		return trackerUp.angleTo(POS_Y) < maxDeviationInRad
	}

	companion object {
		private fun filterImuTrackers(vararg trackers: Tracker?): List<Tracker> = trackers.filterNotNull().filter { it.isImu() }.toList()
	}
}
