package dev.slimevr.tracking.trackers

import dev.slimevr.VRServer
import dev.slimevr.config.TrackerConfig
import dev.slimevr.tracking.trackers.TrackerPosition.Companion.getByDesignation
import dev.slimevr.tracking.trackers.udp.IMUType
import io.eiren.math.FloatMath.INV_SQRT_TWO
import io.eiren.util.BufferedTimer
import io.github.axisangles.ktmath.Quaternion
import io.github.axisangles.ktmath.Vector3
import solarxr_protocol.datatypes.DeviceIdT
import solarxr_protocol.datatypes.TrackerIdT
import solarxr_protocol.rpc.StatusData
import solarxr_protocol.rpc.StatusDataUnion
import solarxr_protocol.rpc.StatusTrackerErrorT
import solarxr_protocol.rpc.StatusTrackerResetT
import kotlin.properties.Delegates

const val TIMEOUT_MS = 2_000L
const val DISCONNECT_MS = 3_000L + TIMEOUT_MS

/**
 * Generic tracker class for input and output tracker,
 * with flags on instantiation.
 */
class Tracker @JvmOverloads constructor(
	val device: Device?,
	/**
	 * VRServer.nextLocalTrackerId
	 */
	val id: Int,
	/**
	 * unique, for config
	 */
	val name: String,
	/**
	 * default display GUI name
	 */
	val displayName: String = "Tracker #$id",
	trackerPosition: TrackerPosition?,
	/**
	 * It's like the ID, but it should be local to the device if it has one
	 */
	trackerNum: Int? = null,
	val hasPosition: Boolean = false,
	val hasRotation: Boolean = false,
	val hasAcceleration: Boolean = false,
	/**
	 * User can change TrackerPosition, mounting...
	 */
	val userEditable: Boolean = false,
	/**
	 * Is used within SlimeVR (shareable trackers)
	 */
	val isInternal: Boolean = false,
	/**
	 * Has solved position + rotation (Vive trackers)
	 */
	val isComputed: Boolean = false,
	val imuType: IMUType? = null,
	/**
	 * Automatically set the status to DISCONNECTED
	 */
	val usesTimeout: Boolean = false,
	val allowFiltering: Boolean = false,
	val needsReset: Boolean = false,
	val needsMounting: Boolean = false,
) {
	private val timer = BufferedTimer(1f)
	private var timeAtLastUpdate: Long = System.currentTimeMillis()
	private var _rotation = Quaternion.IDENTITY
	private var _acceleration = Vector3.NULL
	var position = Vector3.NULL
	val resetsHandler: TrackerResetsHandler = TrackerResetsHandler(this)
	val filteringHandler: TrackerFilteringHandler = TrackerFilteringHandler()
	var batteryVoltage: Float? = null
	var batteryLevel: Float? = null
	var ping: Int? = null
	var signalStrength: Int? = null
	var temperature: Float? = null
	var customName: String? = null

	/**
	 * If the tracker has gotten disconnected after it was initialized first time
	 */
	var statusResetRecently = false
	private var alreadyInitialized = false
	var status: TrackerStatus by Delegates.observable(TrackerStatus.DISCONNECTED) { _, old, new ->
		if (old == new) return@observable

		if (!new.reset) {
			if (alreadyInitialized) {
				statusResetRecently = true
			}
			alreadyInitialized = true
		}
		if (!isInternal) {
			// If the status of a non-internal tracker has changed, inform
			// the VRServer to recreate the skeleton, as it may need to
			// assign or un-assign the tracker to a body part
			VRServer.instance.updateSkeletonModel()
			VRServer.instance.refreshTrackersDriftCompensationEnabled()

			checkReportErrorStatus()
			checkReportRequireReset()
		}
	}

	var trackerPosition: TrackerPosition? by Delegates.observable(trackerPosition) { _, old, new ->
		if (old == new) return@observable

		if (!isInternal) {
			// Set default mounting orientation for that body part
			new?.let { resetsHandler.mountingOrientation = it.defaultMounting() }

			checkReportRequireReset()
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

	private fun checkReportRequireReset() {
		if (needsReset && trackerPosition != null && lastResetStatus == 0u &&
			!status.reset && (isImu() || !statusResetRecently)
		) {
			reportRequireReset()
		} else if (lastResetStatus != 0u && (trackerPosition == null || status.reset)) {
			VRServer.instance.statusSystem.removeStatus(lastResetStatus)
			lastResetStatus = 0u
		}
	}

	/**
	 * If 0 then it's null
	 */
	var lastResetStatus = 0u
	private fun reportRequireReset() {
		require(lastResetStatus == 0u) {
			"lastResetStatus must be 0u, but was $lastResetStatus"
		}

		val tempTrackerNum = this.trackerNum
		val statusMsg = StatusTrackerResetT().apply {
			trackerId = TrackerIdT().apply {
				if (device != null) {
					deviceId = DeviceIdT().apply { id = device.id }
				}
				trackerNum = tempTrackerNum
			}
		}
		val status = StatusDataUnion().apply {
			type = StatusData.StatusTrackerReset
			value = statusMsg
		}
		lastResetStatus = VRServer.instance.statusSystem.addStatus(status, true)
	}

	private fun checkReportErrorStatus() {
		if (status == TrackerStatus.ERROR && lastErrorStatus == 0u) {
			reportErrorStatus()
		} else if (lastErrorStatus != 0u && status != TrackerStatus.ERROR) {
			VRServer.instance.statusSystem.removeStatus(lastErrorStatus)
			lastErrorStatus = 0u
		}
	}

	var lastErrorStatus = 0u
	private fun reportErrorStatus() {
		require(lastErrorStatus == 0u) {
			"lastResetStatus must be 0u, but was $lastErrorStatus"
		}

		val tempTrackerNum = this.trackerNum
		val statusMsg = StatusTrackerErrorT().apply {
			trackerId = TrackerIdT().apply {
				if (device != null) {
					deviceId = DeviceIdT().apply { id = device.id }
				}
				trackerNum = tempTrackerNum
			}
		}
		val status = StatusDataUnion().apply {
			type = StatusData.StatusTrackerError
			value = statusMsg
		}
		lastErrorStatus = VRServer.instance.statusSystem.addStatus(status, true)
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
			// Load manual mounting
			config.mountingOrientation?.let { resetsHandler.mountingOrientation = it.toValue() }
		}
		if (this.isImu() && config.allowDriftCompensation == null) {
			// If value didn't exist, default to true and save
			resetsHandler.allowDriftCompensation = true
			VRServer.instance.configManager.vrConfig.getTracker(this).allowDriftCompensation = true
			VRServer.instance.configManager.saveConfig()
		} else {
			config.allowDriftCompensation?.let {
				resetsHandler.allowDriftCompensation = it
			}
		}
		if (!isInternal &&
			!(!isImu() && (trackerPosition == TrackerPosition.LEFT_HAND || trackerPosition == TrackerPosition.RIGHT_HAND))
		) {
			checkReportRequireReset()
		}
	}

	/**
	 * Writes/saves to the given config
	 */
	fun writeConfig(config: TrackerConfig) {
		trackerPosition?.let { config.designation = it.designation } ?: run { config.designation = null }
		customName?.let { config.customName = it }
		if (needsMounting) {
			// Save manual mounting
			config.mountingOrientation = resetsHandler.mountingOrientation.toObject()
		}
		if (this.isImu()) {
			config.allowDriftCompensation = resetsHandler.allowDriftCompensation
		}
	}

	/**
	 * Loads the mounting reset quaternion from disk
	 */
	fun saveMountingResetOrientation(config: TrackerConfig) {
		// Load automatic mounting
		config.mountingResetOrientation?.let {
			resetsHandler.trySetMountingReset(it.toValue())
		}
	}

	/**
	 * Saves the mounting reset quaternion to disk
	 */
	fun saveMountingResetOrientation(quat: Quaternion?) {
		val configManager = VRServer.instance.configManager
		configManager.vrConfig.getTracker(this).mountingResetOrientation = quat?.toObject()
		configManager.saveConfig()
	}

	/**
	 * Synchronized with the VRServer's 1000hz while loop
	 */
	fun tick() {
		if (usesTimeout) {
			if (System.currentTimeMillis() - timeAtLastUpdate > DISCONNECT_MS) {
				status = TrackerStatus.DISCONNECTED
			} else if (System.currentTimeMillis() - timeAtLastUpdate > TIMEOUT_MS) {
				status = TrackerStatus.TIMED_OUT
			}
		}
		filteringHandler.update()
		resetsHandler.update()
	}

	/**
	 * Tells the tracker that it received new data
	 */
	fun dataTick() {
		timer.update()
		timeAtLastUpdate = System.currentTimeMillis()
		filteringHandler.dataTick(_rotation)
	}

	/**
	 * A way to delay the timeout of the tracker
	 */
	fun heartbeat() {
		timeAtLastUpdate = System.currentTimeMillis()
	}

	/**
	 * Gets the adjusted tracker rotation after all corrections
	 * (filtering, reset, mounting and drift compensation).
	 * This is the rotation that is applied on the SlimeVR skeleton bones.
	 * Warning: This performs several Quaternion multiplications, so calling
	 * it too much should be avoided for performance reasons.
	 */
	fun getRotation(): Quaternion {
		var rot = if (allowFiltering && filteringHandler.enabled) {
			// Get filtered rotation
			filteringHandler.getFilteredRotation()
		} else {
			// Get unfiltered rotation
			_rotation
		}

		if (needsReset && !(isComputed && trackerPosition == TrackerPosition.HEAD)) {
			// Adjust to reset, mounting and drift compensation
			rot = resetsHandler.getReferenceAdjustedDriftRotationFrom(rot)
		}

		return rot
	}

	/**
	 * Gets the world-adjusted acceleration
	 */
	fun getAcceleration(): Vector3 = if (needsReset) {
		resetsHandler.getReferenceAdjustedAccel(_rotation, _acceleration)
	} else {
		_acceleration
	}

	/**
	 * Gets the identity-adjusted tracker rotation after corrections
	 * (filtering, identity reset, drift and identity mounting).
	 * This is used for debugging/visualizing tracker data
	 */
	fun getIdentityAdjustedRotation(): Quaternion {
		var rot = if (filteringHandler.enabled) {
			// Get filtered rotation
			filteringHandler.getFilteredRotation()
		} else {
			// Get unfiltered rotation
			_rotation
		}

		if (needsReset && trackerPosition != TrackerPosition.HEAD) {
			// Adjust to reset and mounting
			rot = resetsHandler.getIdentityAdjustedDriftRotationFrom(rot)
		}

		return rot
	}

	/**
	 * Gets the raw (unadjusted) rotation of the tracker.
	 * If this is an IMU, this will be the raw sensor rotation.
	 */
	fun getRawRotation(): Quaternion = _rotation

	/**
	 * Sets the raw (unadjusted) rotation of the tracker.
	 */
	fun setRotation(rotation: Quaternion) {
		this._rotation = rotation
	}

	/**
	 * Sets the raw (unadjusted) acceleration of the tracker.
	 */
	fun setAcceleration(vec: Vector3) {
		this._acceleration = vec
	}

	fun isImu(): Boolean = imuType != null

	/**
	 * Gets the current TPS of the tracker
	 */
	val tps: Float
		get() = timer.averageFPS

	companion object {
		/**
		 * Changes from IMU axis to OpenGL/SteamVR axis
		 */
		fun axisOffset(v: Vector3): Vector3 = Vector3(v.x, v.z, -v.y)
		fun axisOffset(q: Quaternion): Quaternion = Quaternion(INV_SQRT_TWO, -INV_SQRT_TWO, 0f, 0f) * q
	}
}
