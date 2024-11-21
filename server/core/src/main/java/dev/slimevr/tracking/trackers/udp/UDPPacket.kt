package dev.slimevr.tracking.trackers.udp

import dev.slimevr.tracking.trackers.TrackerStatus
import io.github.axisangles.ktmath.Quaternion
import io.github.axisangles.ktmath.Vector3
import java.io.IOException
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer

sealed class UDPPacket(val packetId: Int) {
	@Throws(IOException::class, BufferUnderflowException::class)
	open fun readData(buf: ByteBuffer) {}

	@Throws(IOException::class)
	open fun writeData(buf: ByteBuffer) {}

	companion object {
		/**
		 * Naively read null-terminated ASCII string from the byte buffer
		 *
		 * @param buf
		 * @return
		 * @throws IOException
		 */
		@Throws(IOException::class)
		fun readASCIIString(buf: ByteBuffer): String {
			val sb = StringBuilder()
			while (true) {
				val c = (buf.get().toInt() and 0xFF).toChar()
				if (c.code == 0) break
				sb.append(c)
			}
			return sb.toString()
		}

		@JvmStatic
		@Throws(IOException::class)
		fun readASCIIString(buf: ByteBuffer, length: Int): String {
			var length = length
			val sb = StringBuilder()
			while (length-- > 0) {
				val c = (buf.get().toInt() and 0xFF).toChar()
				if (c.code == 0) break
				sb.append(c)
			}
			return sb.toString()
		}

		/**
		 * Naively write null-terminated ASCII string to byte buffer
		 *
		 * @param str
		 * @param buf
		 * @throws IOException
		 */
		@Throws(IOException::class)
		fun writeASCIIString(str: String, buf: ByteBuffer) {
			for (element in str) {
				buf.put((element.code and 0xFF).toByte())
			}
			buf.put(0.toByte())
		}
	}
}

sealed interface SensorSpecificPacket {
	val sensorId: Int
	companion object {
		/**
		 * Sensor with id 255 is "global" representing a whole device
		 *
		 * @param sensorId
		 * @return
		 */
		fun isGlobal(sensorId: Int): Boolean = sensorId == 255
	}
}

sealed interface RotationPacket : SensorSpecificPacket {
	var rotation: Quaternion
}

data object UDPPacket0Heartbeat : UDPPacket(0)
data object UDPPacket1Heartbeat : UDPPacket(1)
data class UDPPacket1Rotation(override var rotation: Quaternion = Quaternion.IDENTITY) :
	UDPPacket(1),
	RotationPacket {
	override val sensorId = 0
	override fun readData(buf: ByteBuffer) {
		rotation = UDPUtils.getSafeBufferQuaternion(buf)
	}
}

data class UDPPacket3Handshake(
	var boardType: BoardType = BoardType.UNKNOWN,
	var imuType: IMUType = IMUType.UNKNOWN,
	var mcuType: MCUType = MCUType.UNKNOWN,
	var firmwareBuild: Int = 0,
	var firmware: String? = null,
	var macString: String? = null,
) : UDPPacket(3) {
	override fun readData(buf: ByteBuffer) {
		if (buf.remaining() == 0) return
		if (buf.remaining() > 3) {
			boardType = BoardType.getById(buf.int.toUInt()) ?: BoardType.UNKNOWN
		}
		if (buf.remaining() > 3) {
			imuType = IMUType.getById(buf.int.toUInt()) ?: IMUType.UNKNOWN
		}
		if (buf.remaining() > 3) {
			mcuType = MCUType.getById(buf.int.toUInt()) ?: MCUType.UNKNOWN
		} // MCU TYPE
		if (buf.remaining() > 11) {
			buf.int
			buf.int
			buf.int // IMU info
		}
		if (buf.remaining() > 3) firmwareBuild = buf.int
		var length = 0
		if (buf.remaining() > 0) length = buf.get().toInt()
		// firmware version length is 1 longer than
		// that because it's nul-terminated
		firmware = readASCIIString(buf, length)
		val mac = ByteArray(6)
		if (buf.remaining() >= mac.size) {
			buf.get(mac)
			macString = String.format(
				"%02X:%02X:%02X:%02X:%02X:%02X",
				mac[0],
				mac[1],
				mac[2],
				mac[3],
				mac[4],
				mac[5],
			)
			if (macString == "00:00:00:00:00:00") macString = null
		}
	}

	override fun writeData(buf: ByteBuffer) {
		// Never sent back in current protocol
		// Handshake for RAW SlimeVR and legacy owoTrack has different packet id
		// byte
		// order from normal packets
		// So it's handled by raw protocol call
	}
}

data class UDPPacket4Acceleration(var acceleration: Vector3 = Vector3.NULL) :
	UDPPacket(4),
	SensorSpecificPacket {
	override var sensorId = 0
	override fun readData(buf: ByteBuffer) {
		acceleration = Vector3(UDPUtils.getSafeBufferFloat(buf), UDPUtils.getSafeBufferFloat(buf), UDPUtils.getSafeBufferFloat(buf))

		sensorId = try {
			buf.get().toInt() and 0xFF
		} catch (e: BufferUnderflowException) {
			// for owo track app
			0
		}
	}
}

data class UDPPacket10PingPong(var pingId: Int = 0) : UDPPacket(10) {
	override fun readData(buf: ByteBuffer) {
		pingId = buf.int
	}

	override fun writeData(buf: ByteBuffer) {
		buf.putInt(pingId)
	}
}

data class UDPPacket11Serial(var serial: String = "") : UDPPacket(11) {
	override fun readData(buf: ByteBuffer) {
		val length = buf.int
		val sb = StringBuilder(length)
		for (i in 0 until length) {
			val ch = Char(buf.get().toUShort())
			sb.append(ch)
		}
		serial = sb.toString()
	}
}

data class UDPPacket12BatteryLevel(
	var voltage: Float = 0.0f,
	var level: Float = 0.0f,
) : UDPPacket(12) {

	override fun readData(buf: ByteBuffer) {
		voltage = UDPUtils.getSafeBufferFloat(buf)
		if (buf.remaining() > 3) {
			level = UDPUtils.getSafeBufferFloat(buf)
		} else {
			level = voltage
			voltage = 0.0f
		}
	}
}

data class UDPPacket13Tap(var tap: SensorTap = SensorTap(0)) :
	UDPPacket(13),
	SensorSpecificPacket {
	override var sensorId = 0
	override fun readData(buf: ByteBuffer) {
		sensorId = buf.get().toInt() and 0xFF
		tap = SensorTap(buf.get().toInt() and 0xFF)
	}
}

data class UDPPacket14Error(var errorNumber: Int = 0) :
	UDPPacket(14),
	SensorSpecificPacket {
	override var sensorId = 0
	override fun readData(buf: ByteBuffer) {
		sensorId = buf.get().toInt() and 0xFF
		errorNumber = buf.get().toInt() and 0xFF
	}
}

data class UDPPacket15SensorInfo(
	var sensorStatus: Int = 0,
	var sensorType: IMUType = IMUType.UNKNOWN,
	var sensorConfig: SensorConfig? = null,
) : UDPPacket(15),
	SensorSpecificPacket {
	override var sensorId = 0
	override fun readData(buf: ByteBuffer) {
		sensorId = buf.get().toInt() and 0xFF
		sensorStatus = buf.get().toInt() and 0xFF
		if (buf.remaining() > 0) {
			sensorType =
				IMUType.getById(buf.get().toUInt() and 0xFFu) ?: IMUType.UNKNOWN
		}
		if (buf.remaining() > 1) {
			sensorConfig = SensorConfig(buf.getShort().toUShort())
		}
	}

	companion object {
		fun getStatus(sensorStatus: Int): TrackerStatus? = when (sensorStatus) {
			0 -> TrackerStatus.DISCONNECTED
			1 -> TrackerStatus.OK
			2 -> TrackerStatus.ERROR
			else -> null
		}
	}
}

data class UDPPacket16Rotation2(override var rotation: Quaternion = Quaternion.IDENTITY) :
	UDPPacket(16),
	RotationPacket {
	override val sensorId = 1
	override fun readData(buf: ByteBuffer) {
		rotation = UDPUtils.getSafeBufferQuaternion(buf)
	}
}

data class UDPPacket17RotationData(
	var rotation: Quaternion = Quaternion.IDENTITY,
	var dataType: Int = 0,
	var calibrationInfo: Int = 0,
) : UDPPacket(17),
	SensorSpecificPacket {
	override var sensorId: Int = 0
	override fun readData(buf: ByteBuffer) {
		sensorId = buf.get().toInt() and 0xFF
		dataType = buf.get().toInt() and 0xFF
		rotation = UDPUtils.getSafeBufferQuaternion(buf)
		calibrationInfo = buf.get().toInt() and 0xFF
	}

	companion object {
		const val DATA_TYPE_NORMAL = 1
		const val DATA_TYPE_CORRECTION = 2
	}
}

data class UDPPacket18MagnetometerAccuracy(var accuracyInfo: Float = 0.0f) :
	UDPPacket(18),
	SensorSpecificPacket {
	override var sensorId = 0
	override fun readData(buf: ByteBuffer) {
		sensorId = buf.get().toInt() and 0xFF
		accuracyInfo = UDPUtils.getSafeBufferFloat(buf)
	}
}

data class UDPPacket19SignalStrength(var signalStrength: Int = 0) :
	UDPPacket(19),
	SensorSpecificPacket {
	override var sensorId = 0
	override fun readData(buf: ByteBuffer) {
		sensorId = buf.get().toInt() and 0xFF
		signalStrength = buf.get().toInt()
	}
}

data class UDPPacket20Temperature(var temperature: Float = 0.0f) :
	UDPPacket(20),
	SensorSpecificPacket {
	override var sensorId = 0
	override fun readData(buf: ByteBuffer) {
		sensorId = buf.get().toInt() and 0xFF
		temperature = UDPUtils.getSafeBufferFloat(buf)
	}
}

data class UDPPacket21UserAction(var type: Int = 0) : UDPPacket(21) {
	override fun readData(buf: ByteBuffer) {
		type = buf.get().toInt() and 0xFF
	}

	companion object {
		const val RESET_FULL = 2
		const val RESET_YAW = 3
		const val RESET_MOUNTING = 4
		const val PAUSE_TRACKING = 5
	}
}

class UDPPacket22FeatureFlags(
	var firmwareFeatures: FirmwareFeatures = FirmwareFeatures(),
) : UDPPacket(22) {
	override fun readData(buf: ByteBuffer) {
		firmwareFeatures = FirmwareFeatures.from(buf, buf.remaining())
	}

	override fun writeData(buf: ByteBuffer) {
		buf.put(ServerFeatureFlags.packed)
	}
}

data class UDPPacket23RotationAndAcceleration(
	override var rotation: Quaternion = Quaternion.IDENTITY,
	var acceleration: Vector3 = Vector3.NULL,
) : UDPPacket(23),
	RotationPacket {
	override var sensorId: Int = 0
	override fun readData(buf: ByteBuffer) {
		// s16 s16 s16 s16 s16 s16 s16
		// qX  qY  qZ  qW  aX  aY  aZ
		sensorId = buf.get().toInt() and 0xFF
		val scaleR = 1 / (1 shl 15).toFloat() // Q15: 1 is represented as 0x7FFF and -1 as 0x8000
		val x = buf.short * scaleR
		val y = buf.short * scaleR
		val z = buf.short * scaleR
		val w = buf.short * scaleR
		rotation = Quaternion(w, x, y, z).unit()
		val scaleA = 1 / (1 shl 7).toFloat() // The same as the HID scale
		acceleration = Vector3(buf.short * scaleA, buf.short * scaleA, buf.short * scaleA)
	}
}

data class UDPPacket24AckConfigChange(
	override var sensorId: Int = 0,
	var configType: ConfigTypeId = ConfigTypeId(0u),
) : UDPPacket(24),
	SensorSpecificPacket {
	override fun readData(buf: ByteBuffer) {
		sensorId = buf.get().toInt() and 0xFF
		configType = ConfigTypeId(buf.getShort().toUShort())
	}
}

data class UDPPacket25SetConfigFlag(
	override var sensorId: Int = 255,
	var configType: ConfigTypeId,
	var state: Boolean,
) : UDPPacket(25),
	SensorSpecificPacket {
	override fun writeData(buf: ByteBuffer) {
		buf.put(sensorId.toByte())
		buf.putShort(configType.v.toShort())
		buf.put(if (state) 1 else 0)
	}
}

data class UDPPacket26Log(var message: String = "") : UDPPacket(26) {
	override fun readData(buf: ByteBuffer) {
		val length = buf.get().toUByte().toInt()
		message = readASCIIString(buf, length)
	}
}

data class UDPPacket200ProtocolChange(
	var targetProtocol: Int = 0,
	var targetProtocolVersion: Int = 0,
) : UDPPacket(200) {
	override fun readData(buf: ByteBuffer) {
		targetProtocol = buf.get().toInt() and 0xFF
		targetProtocolVersion = buf.get().toInt() and 0xFF
	}

	override fun writeData(buf: ByteBuffer) {
		buf.put(targetProtocol.toByte())
		buf.put(targetProtocolVersion.toByte())
	}
}

class UDPUtils {
	companion object {
		fun getSafeBufferQuaternion(byteBuffer: ByteBuffer): Quaternion {
			val x = byteBuffer.getFloat()
			val y = byteBuffer.getFloat()
			val z = byteBuffer.getFloat()
			val w = byteBuffer.getFloat()

			return if (
				(x.isNaN() || y.isNaN() || z.isNaN() || w.isNaN()) ||
				(x == 0f && y == 0f && z == 0f && w == 0f)
			) {
				Quaternion.IDENTITY
			} else {
				Quaternion(w, x, y, z)
			}
		}
		fun getSafeBufferFloat(byteBuffer: ByteBuffer): Float {
			val value = byteBuffer.getFloat()
			return if (value.isNaN()) {
				0f
			} else {
				value
			}
		}
	}
}
