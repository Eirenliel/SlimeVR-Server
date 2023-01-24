package dev.slimevr.vr.trackers

enum class TrackerStatus(
	val id: UByte,
	@JvmField val sendData: Boolean,
) {
	DISCONNECTED(0u, false),
	OK(1u, true),
	BUSY(2u, true),
	ERROR(3u, false),
	OCCLUDED(4u, false),
	;

	companion object {
		@JvmStatic
		fun getById(id: UByte): TrackerStatus? = byId[id]
	}
}

private val byId = TrackerStatus.values().associateBy { it.id }
