package dev.slimevr.tracking.trackers

import dev.slimevr.tracking.trackers.udp.BoardType
import dev.slimevr.tracking.trackers.udp.MCUType
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicInteger

open class Device {
	open val id: Int = nextLocalDeviceId.incrementAndGet()
	open var name: String? = null
	open var firmwareVersion: String? = null
	open var manufacturer: String? = null
	open val trackers = HashMap<Int, Tracker>()

	/**
	 * Implement toString() to return a string that uniquely identifies the board type
	 * SHOULDN'T RETURN NULL WHEN toString() IS CALLED
	 */
	open val boardType: Any = BoardType.UNKNOWN
	open val mcuType: MCUType = MCUType.UNKNOWN

	open val ipAddress: InetAddress?
		get() = null

	companion object {
		@JvmField
		val nextLocalDeviceId = AtomicInteger()
	}
}
