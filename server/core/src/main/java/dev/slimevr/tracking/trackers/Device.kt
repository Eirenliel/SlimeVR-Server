package dev.slimevr.tracking.trackers

import dev.slimevr.tracking.trackers.udp.BoardType
import dev.slimevr.tracking.trackers.udp.MCUType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

open class Device(val magSupport: Boolean = false) {
	open val id: Int = nextLocalDeviceId.incrementAndGet()
	open var name: String? = null
	open var firmwareVersion: String? = null
	open var manufacturer: String? = null
	open val trackers: MutableMap<Int, Tracker> = ConcurrentHashMap()

	/**
	 * Implement toString() to return a string that uniquely identifies the board type
	 * SHOULDN'T RETURN NULL WHEN toString() IS CALLED
	 */
	open val boardType: Any = BoardType.UNKNOWN
	open val mcuType: MCUType = MCUType.UNKNOWN

	open val hardwareIdentifier: String = "Unknown"

	open var logMessages: MutableList<String> = mutableListOf()

	val isOpenVrDevice: Boolean
		get() = manufacturer == "OpenVR"

	init {
		CoroutineScope(Job()).launch {
			// Wait a little for device to get configured
			delay(1000)
		}
	}

	/**
	 * Enables or disables magnetometers in all the trackers of the device
	 * if `sensorId` null or in the specified tracker
	 * @param sensorId If null, every sensor will be modified
	 */
	open suspend fun setMag(state: Boolean, sensorId: Int = 255) {
		TODO("Not implemented because no mag support: $magSupport")
	}

	companion object {
		@JvmStatic
		protected val nextLocalDeviceId = AtomicInteger()
	}
}
