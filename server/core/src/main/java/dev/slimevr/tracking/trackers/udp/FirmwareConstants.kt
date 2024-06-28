package dev.slimevr.tracking.trackers.udp

enum class IMUType(val id: UInt) {
	UNKNOWN(0u),
	MPU9250(1u),
	MPU6500(2u),
	BNO080(3u),
	BNO085(4u),
	BNO055(5u),
	MPU6050(6u),
	BNO086(7u),
	BMI160(8u),
	ICM20948(9u),
	ICM42688(10u),
	;

	fun getSolarType(): Int = this.id.toInt()

	companion object {
		private val byId = IMUType.values().associateBy { it.id }

		@JvmStatic
		fun getById(id: UInt): IMUType? = byId[id]
	}
}

enum class BoardType(val id: UInt) {
	UNKNOWN(0u),
	SLIMEVR_LEGACY(1u),
	SLIMEVR_DEV(2u),
	NODEMCU(3u),
	CUSTOM(4u),
	WROOM32(5u),
	WEMOSD1MINI(6u),
	TTGO_TBASE(7u),
	ESP01(8u),
	SLIMEVR(9u),
	LOLIN_C3_MINI(10u),
	BEETLE32C32(11u),
	ES32C3DEVKITM1(12u),
	OWOTRACK(13u),
	WRANGLER(14u),
	MOCOPI(15u),
	WEMOSWROOM02(16u),
	GESTURES1(17u),
	DEV_RESERVED(250u),
	;

	override fun toString(): String = when (this) {
		UNKNOWN -> "Unknown"
		SLIMEVR_LEGACY -> "SlimeVR Legacy"
		SLIMEVR_DEV -> "SlimeVR Dev"
		NODEMCU -> "NodeMCU"
		CUSTOM -> "Custom Board"
		WROOM32 -> "WROOM32"
		WEMOSD1MINI -> "Wemos D1 Mini"
		TTGO_TBASE -> "TTGO T-Base"
		ESP01 -> "ESP-01"
		SLIMEVR -> "SlimeVR"
		LOLIN_C3_MINI -> "Lolin C3 Mini"
		BEETLE32C32 -> "Beetle ESP32-C3"
		ES32C3DEVKITM1 -> "Espressif ESP32-C3 DevKitM-1"
		OWOTRACK -> "owoTrack"
		WRANGLER -> "Wrangler Joycons"
		MOCOPI -> "Sony Mocopi"
		WEMOSWROOM02 -> "Wemos Wroom-02 D1 Mini"
		GESTURES1 -> "Gestures Mark 1"
		DEV_RESERVED -> "Prototype"
	}

	companion object {
		private val byId = BoardType.values().associateBy { it.id }

		@JvmStatic
		fun getById(id: UInt): BoardType? = byId[id]
	}
}

enum class MCUType(val id: UInt) {
	UNKNOWN(0u),
	ESP8266(1u),
	ESP32(2u),
	OWOTRACK_ANDROID(3u),
	WRANGLER(4u),
	OWOTRACK_IOS(5u),
	ESP32_C3(6u),
	MOCOPI(7u),
	DEV_RESERVED(250u),
	;

	fun getSolarType(): Int = this.id.toInt()

	companion object {
		private val byId = MCUType.values().associateBy { it.id }

		@JvmStatic
		fun getById(id: UInt): MCUType? = byId[id]
	}
}
