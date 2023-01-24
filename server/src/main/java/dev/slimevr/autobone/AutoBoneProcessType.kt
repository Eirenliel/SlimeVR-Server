package dev.slimevr.autobone

enum class AutoBoneProcessType(val id: UByte) {
	NONE(0u),
	RECORD(1u),
	SAVE(2u),
	PROCESS(3u),
	APPLY(3u),
	;
	companion object {
		@JvmStatic
		fun getById(id: UByte): AutoBoneProcessType? = byId[id]
	}
}

private val byId = AutoBoneProcessType.values().associateBy { it.id }
