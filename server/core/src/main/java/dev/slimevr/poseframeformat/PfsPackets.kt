package dev.slimevr.poseframeformat

/**
 * Packet ID ([UByte]),
 * Packet data (see [PfsPackets], implemented in [PfsIO])
 */
enum class PfsPackets(val id: Int) {
	/**
	 * Frame interval ([Float] seconds)
	 */
	RECORDING_DEFINITION(0),

	/**
	 * Tracker ID ([UByte]),
	 * Tracker name (UTF-8 [String])
	 */
	TRACKER_DEFINITION(1),

	/**
	 * Tracker ID ([UByte]),
	 * Frame number ([UInt]),
	 * PFR frame data (see [PfrIO.writeFrame] & [PfrIO.readFrame])
	 */
	TRACKER_FRAME(2),

	/**
	 * Hmd height ([Float]),
	 * Floor height ([Float]),
	 * Body proportion configs (Count ([UShort]) x (Key (UTF-8 [String]), Value ([Float]))
	 */
	PROPORTIONS_CONFIG(3),
	;

	val byteId = id.toUByte()

	companion object {
		val byId = entries.associateBy { it.id }
		val byByteId = entries.associateBy { it.byteId }
	}
}
