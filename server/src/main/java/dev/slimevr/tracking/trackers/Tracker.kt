package dev.slimevr.tracking.trackers

import dev.slimevr.config.TrackerConfig
import dev.slimevr.tracking.trackers.TrackerPosition.Companion.getByDesignation
import dev.slimevr.vrServer
import io.eiren.util.BufferedTimer
import io.github.axisangles.ktmath.Quaternion
import io.github.axisangles.ktmath.Vector3

const val TIMEOUT_MS = 2000L

/**
 * Generic tracker class for input and output tracker,
 * with flags on instantiation.
 */
class Tracker @JvmOverloads constructor(
	val device: Device?,
	val id: Int, // VRServer.nextLocalTrackerId
	val name: String, // unique, for config
	val displayName: String = "Tracker #$id", // default display GUI name
	var trackerPosition: TrackerPosition?,
	/**
	 * It's like the ID, but it should be local to the device if it has one
	 */
	trackerNum: Int? = null,
	val hasPosition: Boolean = false,
	val hasRotation: Boolean = false,
	val hasAcceleration: Boolean = false,
	val userEditable: Boolean = false, // User can change TrackerPosition, mounting...
	val isInternal: Boolean = false, // Is used within SlimeVR (shareable trackers)
	val isComputed: Boolean = false, // Has solved position + rotation (Vive trackers)
	val isImu: Boolean = false,
	val usesTimeout: Boolean = false, // Automatically set the status to DISCONNECTED
	val allowFiltering: Boolean = false,
	val needsReset: Boolean = false,
	val needsMounting: Boolean = false,
) {
	private val timer = BufferedTimer(1f)
	private var timeAtLastUpdate: Long = 0
	private var rotation = Quaternion.IDENTITY
	var position = Vector3.NULL
	var acceleration = Vector3.NULL
	val resetsHandler: TrackerResetsHandler = TrackerResetsHandler(this)
	val filteringHandler: TrackerFilteringHandler = TrackerFilteringHandler()
	var batteryVoltage: Float? = null
	var batteryLevel: Float? = null
	var ping: Int? = null
	var signalStrength: Int? = null
	var temperature: Float? = null
	var customName: String? = null

	var status = TrackerStatus.DISCONNECTED
		set(value) {
			if (field != value) {
				field = value
				if (!isInternal) {
					// If the status of a non-internal tracker has changed, inform
					// the VRServer to recreate the skeleton, as it may need to
					// assign or un-assign the tracker to a body part
					vrServer.updateSkeletonModel()
				}
			}
		}

	// Computed value to simplify availability checks
	val hasAdjustedRotation = hasRotation && (allowFiltering || needsReset)

	/**
	 * It's like the ID, but it should be local to the device if it has one
	 */
	val trackerNum: Int = trackerNum ?: id

	init {
		// IMPORTANT: Look here for the required states of inputs
		require(!needsReset || (hasRotation && needsReset)) {
			"If ${::needsReset.name} is true, then ${::hasRotation.name} must also be true"
		}
		require(!needsMounting || (needsReset && needsMounting)) {
			"If ${::needsMounting.name} is true, then ${::needsReset.name} must also be true"
		}
// 		require(device != null && _trackerNum == null) {
// 			"If ${::device.name} exists, then ${::trackerNum.name} must not be null"
// 		}
	}

	/**
	 * Reads/loads from the given config
	 */
	fun readConfig(config: TrackerConfig) {
		config.customName?.let {
			customName = it
		}
		config.designation?.let { designation ->
			getByDesignation(designation)?.let { trackerPosition = it }
		} ?: run { trackerPosition = null }
		if (needsMounting) {
			config.mountingOrientation?.let { resetsHandler.mountingOrientation = it }
		}
		if (isImu && config.allowDriftCompensation == null) {
			// If value didn't exist, default to true and save
			resetsHandler.allowDriftCompensation = true
			vrServer.configManager.vrConfig.getTracker(this).allowDriftCompensation = true
			vrServer.configManager.saveConfig()
		} else {
			config.allowDriftCompensation?.let {
				resetsHandler.allowDriftCompensation = it
			}
		}
	}

	/**
	 * Writes/saves to the given config
	 */
	fun writeConfig(config: TrackerConfig) {
		trackerPosition?.let { config.designation = it.designation } ?: run { config.designation = null }
		customName?.let { config.customName = it }
		if (needsMounting) {
			config.mountingOrientation = resetsHandler.mountingOrientation
		}
		if (isImu) {
			config.allowDriftCompensation = resetsHandler.allowDriftCompensation
		}
	}

	/**
	 * Synchronized with the VRServer's 1000hz while loop
	 */
	fun tick() {
		if (usesTimeout) {
			if (System.currentTimeMillis() - timeAtLastUpdate > TIMEOUT_MS) {
				status = TrackerStatus.DISCONNECTED
			}
		}
		filteringHandler.tick()
	}

	/**
	 * Tells the tracker that it received new data
	 */
	fun dataTick() {
		timer.update()
		timeAtLastUpdate = System.currentTimeMillis()
		filteringHandler.dataTick(rotation)
	}

	/**
	 * Gets the adjusted tracker rotation after all corrections
	 * (filtering, reset, mounting and drift compensation).
	 * This is the rotation that is applied on the SlimeVR skeleton bones.
	 * Warning: This may perform several Quaternion multiplications, so calling
	 * it too much should be avoided for performance reasons.
	 */
	fun getRotation(): Quaternion {
		var rot = if (allowFiltering && filteringHandler.enabled) {
			// Get filtered rotation
			filteringHandler.getFilteredRotation()
		} else {
			// Get unfiltered rotation
			rotation
		}

		if (needsReset && !(isComputed && trackerPosition == TrackerPosition.HEAD)) {
			// Adjust to reset, mounting and drift compensation
			rot = resetsHandler.getReferenceAdjustedDriftRotationFrom(rot)
		}

		return rot
	}

	/**
	 * Gets the identity-adjusted tracker rotation after some corrections
	 * (filtering, identity reset and identity mounting).
	 * This is used for debugging/visualizing tracker data
	 */
	fun getIdentityAdjustedRotation(): Quaternion {
		var rot = if (filteringHandler.enabled) {
			// Get filtered rotation
			filteringHandler.getFilteredRotation()
		} else {
			// Get unfiltered rotation
			rotation
		}

		if (needsReset && trackerPosition != TrackerPosition.HEAD) {
			// Adjust to reset and mounting
			rot = resetsHandler.getIdentityAdjustedRotationFrom(rot)
		}

		return rot
	}

	/**
	 * Gets the raw (unadjusted) rotation of the tracker.
	 * If this is an IMU, this will be the raw sensor rotation.
	 */
	fun getRawRotation(): Quaternion {
		return rotation
	}

	/**
	 * Sets the raw (unadjusted) rotation of the tracker.
	 */
	fun setRotation(rotation: Quaternion) {
		this.rotation = rotation
	}

	/**
	 * Gets the current TPS of the tracker
	 */
	val tps: Float
		get() = timer.averageFPS
}
