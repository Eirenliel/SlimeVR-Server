package dev.slimevr.tracking.trackers.udp

import java.nio.ByteBuffer

/**
 * Bit packed flags, enum values start with 0 and indicate which bit it is.
 *
 * Change the enums and `flagsEnabled` inside to extend.
 */
class FirmwareFeatures {
	enum class FirmwareFeatureFlags {
		// EXAMPLE_FEATURE,

		// Add new flags here
		REMOTE_COMMAND,
		B64_WIFI_SCANNING,
		SENSOR_CONFIG,

		BITS_TOTAL,
	}

	fun has(flag: FirmwareFeatureFlags): Boolean {
		val bit = flag.ordinal
		return (flags[bit / 8].toInt() and (1 shl (bit % 8))) != 0
	}

	/**
	 * Whether the firmware supports the "feature flags" feature,
	 * set to true when we've received flags packet from the firmware.
	 */
	var available = false
		private set

	companion object {
		fun from(received: ByteBuffer, length: Int): FirmwareFeatures {
			val res = FirmwareFeatures()
			res.available = true
			received.get(res.flags, 0, res.flags.size.coerceAtMost(length))
			return res
		}
	}

	private val flags = ByteArray(FirmwareFeatureFlags.BITS_TOTAL.ordinal / 8 + 1)
}

enum class ServerFeatureFlags {
	/** Server can parse bundle packets: `PACKET_BUNDLE` = 100 (0x64). */
	PROTOCOL_BUNDLE_SUPPORT,

	/** Server can parse bundle packets with compact headers and packed IMU rotation/acceleration frames:
	 - `PACKET_BUNDLE_COMPACT` = 101 (0x65),
	 - `PACKET_ROTATION_AND_ACCELERATION` = 23 (0x17). */
	PROTOCOL_BUNDLE_COMPACT_SUPPORT,

	/** Server can receive log messages: `PACKET_LOG` = 102 (0x66). */
	PROTOCOL_LOG_SUPPORT,

	// Add new flags here

	BITS_TOTAL, ;

	companion object {
		val flagsEnabled: Set<ServerFeatureFlags> = setOf(
			PROTOCOL_BUNDLE_SUPPORT,
			PROTOCOL_BUNDLE_COMPACT_SUPPORT,
			PROTOCOL_LOG_SUPPORT,

			// Add enabled flags here
		)

		val packed = run {
			val byteLength = BITS_TOTAL.ordinal / 8 + 1
			val tempPacked = ByteArray(byteLength)

			for (flag in flagsEnabled) {
				val bit = flag.ordinal
				tempPacked[bit / 8] =
					(tempPacked[bit / 8].toInt() or (1 shl (bit % 8))).toByte()
			}

			tempPacked
		}
	}
}
