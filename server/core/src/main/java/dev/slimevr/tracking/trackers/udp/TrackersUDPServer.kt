package dev.slimevr.tracking.trackers.udp

import com.jme3.math.FastMath
import dev.slimevr.NetworkProtocol
import dev.slimevr.VRServer
import dev.slimevr.config.config
import dev.slimevr.protocol.rpc.MAG_TIMEOUT
import dev.slimevr.tracking.trackers.Tracker
import dev.slimevr.tracking.trackers.TrackerStatus
import io.eiren.util.Util
import io.eiren.util.collections.FastList
import io.eiren.util.logging.LogManager
import io.github.axisangles.ktmath.Quaternion.Companion.fromRotationVector
import io.github.axisangles.ktmath.Vector3
import kotlinx.coroutines.*
import org.apache.commons.lang3.ArrayUtils
import solarxr_protocol.rpc.ResetType
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.SocketAddress
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.function.Consumer
import kotlin.collections.HashMap
import kotlin.coroutines.resume

/**
 * Receives trackers data by UDP using extended owoTrack protocol.
 */
class TrackersUDPServer(private val port: Int, name: String, private val trackersConsumer: Consumer<Tracker>) : Thread(name) {
	private val random = Random()
	private val connections: MutableList<UDPDevice> = FastList()
	private val connectionsByAddress: MutableMap<SocketAddress, UDPDevice> = HashMap()
	private val connectionsByMAC: MutableMap<String, UDPDevice> = HashMap()
	private val broadcastAddresses: List<InetSocketAddress> = try {
		NetworkInterface.getNetworkInterfaces().asSequence().filter {
			// Ignore loopback, PPP, virtual and disabled interfaces
			!it.isLoopback && it.isUp && !it.isPointToPoint && !it.isVirtual
		}.flatMap {
			it.interfaceAddresses.asSequence()
		}.map {
			// This ignores IPv6 addresses
			it.broadcast
		}.filter { it != null && it.isSiteLocalAddress }.map { InetSocketAddress(it, this.port) }.toList()
	} catch (e: Exception) {
		LogManager.severe("[TrackerServer] Can't enumerate network interfaces", e)
		emptyList()
	}
	private val parser = UDPProtocolParser()

	// 1500 is a common network MTU. 1472 is the maximum size of a UDP packet (1500 - 20 for IPv4 header - 8 for UDP header)
	private val rcvBuffer = ByteArray(1500 - 20 - 8)
	private val bb = ByteBuffer.wrap(rcvBuffer).order(ByteOrder.BIG_ENDIAN)

	// Gets initialized in this.run()
	private lateinit var socket: DatagramSocket
	private var lastKeepup = System.currentTimeMillis()

	private fun setUpNewConnection(handshakePacket: DatagramPacket, handshake: UDPPacket3Handshake) {
		LogManager.info("[TrackerServer] Handshake received from ${handshakePacket.address}:${handshakePacket.port}")
		val addr = handshakePacket.address
		val socketAddr = handshakePacket.socketAddress

		// Check if it's a known device
		VRServer.instance.configManager.vrConfig.let { vrConfig ->
			if (vrConfig.isKnownDevice(handshake.macString)) return@let
			val mac = handshake.macString ?: return@let

			VRServer.instance.handshakeHandler.sendUnknownHandshake(mac)
			return
		}

		// Get a connection either by an existing one, or by creating a new one
		val connection: UDPDevice = synchronized(connections) {
			connectionsByMAC[handshake.macString]?.apply {
				// Look for an existing connection by the MAC address and update the
				// connection information
				connectionsByAddress.remove(address)
				address = socketAddr
				lastPacketNumber = 0
				ipAddress = addr
				name = handshake.macString?.let { "udp://$it" }
				descriptiveName = "udp:/$addr"
				firmwareBuild = handshake.firmwareBuild
				firmwareVersion = handshake.firmware
				connectionsByAddress[address] = this

				val i = connections.indexOf(this)
				LogManager
					.info(
						"""
						[TrackerServer] Tracker $i handed over to address $socketAddr.
						Board type: ${handshake.boardType},
						imu type: ${handshake.imuType},
						firmware: ${handshake.firmware} ($firmwareBuild),
						mac: ${handshake.macString},
						name: $name
						""".trimIndent(),
					)
			} ?: connectionsByAddress[socketAddr]?.apply {
				// Look for an existing connection by the socket address (IP and port)
				// and update the connection information
				lastPacketNumber = 0
				ipAddress = addr
				name = handshake.macString?.let { "udp://$it" }
					?: "udp:/$addr"
				descriptiveName = "udp:/$addr"
				firmwareBuild = handshake.firmwareBuild
				firmwareVersion = handshake.firmware
				val i = connections.indexOf(this)
				LogManager
					.info(
						"""
						[TrackerServer] Tracker $i reconnected from address $socketAddr.
						Board type: ${handshake.boardType},
						imu type: ${handshake.imuType},
						firmware: ${handshake.firmware} ($firmwareBuild),
						mac: ${handshake.macString},
						name: $name
						""".trimIndent(),
					)
			}
		} ?: run {
			// No existing connection could be found, create a new one

			val connection = UDPDevice(
				socketAddr,
				addr,
				handshake.macString ?: addr.hostAddress,
				handshake.boardType,
				handshake.mcuType,
			)
			VRServer.instance.deviceManager.addDevice(connection)
			connection.firmwareBuild = handshake.firmwareBuild
			connection.protocol = if (handshake.firmware?.isEmpty() == true) {
				// Only old owoTrack doesn't report firmware and have different packet IDs with SlimeVR
				NetworkProtocol.OWO_LEGACY
			} else {
				NetworkProtocol.SLIMEVR_RAW
			}
			connection.name = handshake.macString?.let { "udp://$it" }
				?: "udp:/$addr"
			// TODO: The missing slash in udp:// was intended because InetAddress.toString()
			// 		returns "hostname/address" but it wasn't known that if hostname is empty
			// 		string it just looks like "/address" lol.
			// 		Fixing this would break config!
			connection.descriptiveName = "udp:/$addr"
			connection.firmwareVersion = handshake.firmware
			synchronized(connections) {
				// Register the new connection
				val i = connections.size
				connections.add(connection)
				connectionsByAddress[socketAddr] = connection
				if (handshake.macString != null) {
					connectionsByMAC[handshake.macString!!] = connection
				}
				LogManager
					.info(
						"""
						[TrackerServer] Tracker $i connected from address $socketAddr.
						Board type: ${handshake.boardType},
						imu type: ${handshake.imuType},
						firmware: ${handshake.firmware} (${connection.firmwareBuild}),
						mac: ${handshake.macString},
						name: ${connection.name}
						""".trimIndent(),
					)
			}
			if (connection.protocol == NetworkProtocol.OWO_LEGACY || connection.firmwareBuild < 9) {
				// Set up new sensor for older firmware.
				// Firmware after 7 should send sensor status packet and sensor
				// will be created when it's received
				setUpSensor(connection, 0, handshake.imuType, 1, MagnetometerStatus.NOT_SUPPORTED)
			}
			connection
		}
		connection.firmwareFeatures = FirmwareFeatures()
		connection.logMessages.clear()
		bb.limit(bb.capacity())
		bb.rewind()
		parser.writeHandshakeResponse(bb, connection)
		socket.send(DatagramPacket(rcvBuffer, bb.position(), connection.address))
	}

	private val mainScope = CoroutineScope(SupervisorJob())
	private fun setUpSensor(connection: UDPDevice, trackerId: Int, sensorType: IMUType, sensorStatus: Int, magStatus: MagnetometerStatus) {
		LogManager.info("[TrackerServer] Sensor $trackerId for ${connection.name} status: $sensorStatus")
		var imuTracker = connection.getTracker(trackerId)
		if (imuTracker == null) {
			var formattedHWID = connection.hardwareIdentifier.replace(":", "").takeLast(5)
			if (trackerId != 0) {
				formattedHWID += "_$trackerId"
			}

			imuTracker = Tracker(
				connection,
				VRServer.getNextLocalTrackerId(),
				connection.name + "/" + trackerId,
				"IMU Tracker $formattedHWID",
				null,
				trackerNum = trackerId,
				hasRotation = true,
				hasAcceleration = true,
				userEditable = true,
				imuType = sensorType,
				allowFiltering = true,
				needsReset = true,
				needsMounting = true,
				usesTimeout = true,
				magStatus = magStatus,
			)
			connection.trackers[trackerId] = imuTracker
			trackersConsumer.accept(imuTracker)
			LogManager.info("[TrackerServer] Added sensor $trackerId for ${connection.name}, type $sensorType")
		}
		val status = UDPPacket15SensorInfo.getStatus(sensorStatus)
		if (status != null) imuTracker.status = status

		if (magStatus == MagnetometerStatus.NOT_SUPPORTED) return
		if (magStatus == MagnetometerStatus.ENABLED &&
			(!VRServer.instance.configManager.vrConfig.server.useMagnetometerOnAllTrackers || imuTracker.config.shouldHaveMagEnabled == false)
		) {
			mainScope.launch {
				withTimeoutOrNull(MAG_TIMEOUT) {
					connection.setMag(false, trackerId)
				}
			}
		} else if (magStatus == MagnetometerStatus.DISABLED &&
			VRServer.instance.configManager.vrConfig.server.useMagnetometerOnAllTrackers && imuTracker.config.shouldHaveMagEnabled == true
		) {
			mainScope.launch {
				withTimeoutOrNull(MAG_TIMEOUT) {
					connection.setMag(true, trackerId)
				}
			}
		}
	}

	private data class ConfigStateWaiter(
		val expectedState: Boolean,
		val channel: CancellableContinuation<Boolean>,
		var ran: Boolean = false,
	)

	private val queues: MutableMap<Triple<SocketAddress, ConfigTypeId, Int>, Deque<ConfigStateWaiter>> = ConcurrentHashMap()
	suspend fun setConfigFlag(device: UDPDevice, configTypeId: ConfigTypeId, state: Boolean, sensorId: Int = 255) {
		if (device.timedOut) return
		val triple = Triple(device.address, configTypeId, sensorId)
		val queue = queues.computeIfAbsent(triple) { _ -> ConcurrentLinkedDeque() }

		suspendCancellableCoroutine {
			val waiter = ConfigStateWaiter(state, it)
			queue.add(waiter)
			it.invokeOnCancellation {
				queue.remove(waiter)
			}
		}
	}

	private fun actualSetConfigFlag(device: UDPDevice, configTypeId: ConfigTypeId, state: Boolean, sensorId: Int) {
		val packet = UDPPacket25SetConfigFlag(sensorId, configTypeId, state)
		bb.limit(bb.capacity())
		bb.rewind()
		parser.write(bb, null, packet)
		socket.send(DatagramPacket(rcvBuffer, bb.position(), device.address))
	}

	override fun run() {
		val serialBuffer2 = StringBuilder()
		try {
			socket = DatagramSocket(port)
			var prevPacketTime = System.currentTimeMillis()
			socket.soTimeout = 250
			while (true) {
				var received: DatagramPacket? = null
				try {
					val hasActiveTrackers = connections.any { it.trackers.size > 0 }
					if (!hasActiveTrackers) {
						val discoveryPacketTime = System.currentTimeMillis()
						if (discoveryPacketTime - prevPacketTime >= 2000) {
							for (addr in broadcastAddresses) {
								bb.limit(bb.capacity())
								bb.rewind()
								parser.write(bb, null, UDPPacket0Heartbeat)
								socket.send(DatagramPacket(rcvBuffer, bb.position(), addr))
							}
							prevPacketTime = discoveryPacketTime
						}
					}
					received = DatagramPacket(rcvBuffer, rcvBuffer.size)
					socket.receive(received)
					bb.limit(received.length)
					bb.rewind()
					val connection = synchronized(connections) { connectionsByAddress[received.socketAddress] }
					parser.parse(bb, connection)
						.filterNotNull()
						.forEach { processPacket(received, it, connection) }

					queues.forEach { (t, p) ->
						val q = p.firstOrNull() ?: return@forEach
						if (q.ran) return@forEach

						val device = connectionsByAddress[t.first] ?: run {
							p.removeFirst()
							LogManager.info("[TrackerServer] Device ${t.first} not connected, so can't communicate with it")
							return@forEach
						}
						actualSetConfigFlag(device, t.second, q.expectedState, t.third)
						if (!device.timedOut) q.ran = true
					}
				} catch (ignored: SocketTimeoutException) {
				} catch (e: Exception) {
					LogManager.warning(
						"[TrackerServer] Error parsing packet ${packetToString(received)}",
						e,
					)
				}
				if (lastKeepup + 500 < System.currentTimeMillis()) {
					lastKeepup = System.currentTimeMillis()
					synchronized(connections) {
						for (conn in connections) {
							bb.limit(bb.capacity())
							bb.rewind()
							parser.write(bb, conn, UDPPacket1Heartbeat)
							socket.send(DatagramPacket(rcvBuffer, bb.position(), conn.address))
							if (conn.lastPacket + 1000 < System.currentTimeMillis()) {
								if (!conn.timedOut) {
									conn.timedOut = true
									LogManager.info("[TrackerServer] Tracker timed out: $conn")
								}
							} else {
								for (value in conn.trackers.values) {
									if (value.status == TrackerStatus.DISCONNECTED ||
										value.status == TrackerStatus.TIMED_OUT
									) {
										value.status = TrackerStatus.OK
									}
								}
								conn.timedOut = false
							}

							if (conn.serialBuffer.isNotEmpty() &&
								conn.lastSerialUpdate + 500L < System.currentTimeMillis()
							) {
								serialBuffer2
									.append('[')
									.append(conn.name)
									.append("] ")
									.append(conn.serialBuffer)
								println(serialBuffer2)
								serialBuffer2.setLength(0)
								conn.serialBuffer.setLength(0)
							}

							if (conn.lastPingPacketTime + 500 < System.currentTimeMillis()) {
								conn.lastPingPacketId = random.nextInt()
								conn.lastPingPacketTime = System.currentTimeMillis()
								bb.limit(bb.capacity())
								bb.rewind()
								bb.putInt(10)
								bb.putLong(0)
								bb.putInt(conn.lastPingPacketId)
								socket.send(DatagramPacket(rcvBuffer, bb.position(), conn.address))
							}
						}
					}
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
		} finally {
			Util.close(socket)
		}
	}

	private fun processPacket(received: DatagramPacket, packet: UDPPacket, connection: UDPDevice?) {
		val tracker: Tracker?
		when (packet) {
			is UDPPacket0Heartbeat, is UDPPacket1Heartbeat, is UDPPacket25SetConfigFlag -> {}

			is UDPPacket3Handshake -> setUpNewConnection(received, packet)

			is RotationPacket -> {
				var rot = packet.rotation
				rot = AXES_OFFSET.times(rot)
				tracker = connection?.getTracker(packet.sensorId)
				if (tracker == null) return
				tracker.setRotation(rot)
				if (packet is UDPPacket23RotationAndAcceleration) {
					// Switch x and y around to adjust for different axes
					tracker.setAcceleration(Vector3(packet.acceleration.y, packet.acceleration.x, packet.acceleration.z))
				}
				tracker.dataTick()
			}

			is UDPPacket17RotationData -> {
				tracker = connection?.getTracker(packet.sensorId)
				if (tracker == null) return
				var rot17 = packet.rotation
				rot17 = AXES_OFFSET * rot17
				when (packet.dataType) {
					UDPPacket17RotationData.DATA_TYPE_NORMAL -> {
						tracker.setRotation(rot17)
						tracker.dataTick()
						// tracker.calibrationStatus = rotationData.calibrationInfo;
						// Not implemented in server
					}

					UDPPacket17RotationData.DATA_TYPE_CORRECTION -> {
// 						tracker.rotMagQuaternion.set(rot17);
// 						tracker.magCalibrationStatus = rotationData.calibrationInfo;
// 						tracker.hasNewCorrectionData = true;
						// Not implemented in server
					}
				}
			}

			is UDPPacket18MagnetometerAccuracy -> {}

			is UDPPacket4Acceleration -> {
				tracker = connection?.getTracker(packet.sensorId)
				if (tracker == null) return
				// Switch x and y around to adjust for different axes
				tracker.setAcceleration(Vector3(packet.acceleration.y, packet.acceleration.x, packet.acceleration.z))
			}

			is UDPPacket10PingPong -> {
				if (connection == null) return
				if (connection.lastPingPacketId == packet.pingId) {
					for (t in connection.trackers.values) {
						t.ping = (System.currentTimeMillis() - connection.lastPingPacketTime).toInt() / 2
						t.dataTick()
					}
				} else {
					LogManager.debug(
						"[TrackerServer] Wrong ping id ${packet.pingId} != ${connection.lastPingPacketId}",
					)
				}
			}

			is UDPPacket11Serial -> {
				if (connection == null) return
				println("[${connection.name}] ${packet.serial}")
			}

			is UDPPacket12BatteryLevel -> connection?.trackers?.values?.forEach {
				it.batteryVoltage = packet.voltage
				it.batteryLevel = packet.level * 100
			}

			is UDPPacket13Tap -> {
				tracker = connection?.getTracker(packet.sensorId)
				if (tracker == null) return
				LogManager.info(
					"[TrackerServer] Tap packet received from ${tracker.name}: ${packet.tap}",
				)
			}

			is UDPPacket14Error -> {
				LogManager.severe(
					"[TrackerServer] Error received from ${received.socketAddress}: ${packet.errorNumber}",
				)
				tracker = connection?.getTracker(packet.sensorId)
				if (tracker == null) return
				tracker.status = TrackerStatus.ERROR
			}

			is UDPPacket15SensorInfo -> {
				if (connection == null) return
				val magStatus = packet.sensorConfig?.magStatus ?: MagnetometerStatus.NOT_SUPPORTED
				setUpSensor(
					connection,
					packet.sensorId,
					packet.sensorType,
					packet.sensorStatus,
					magStatus,
				)
				// Send ack
				bb.limit(bb.capacity())
				bb.rewind()
				parser.writeSensorInfoResponse(bb, connection, packet)
				socket.send(DatagramPacket(rcvBuffer, bb.position(), connection.address))
				LogManager.info(
					"[TrackerServer] Sensor info for ${connection.descriptiveName}/${packet.sensorId}: ${packet.sensorStatus}, mag $magStatus",
				)
			}

			is UDPPacket19SignalStrength -> connection?.trackers?.values?.forEach {
				it.signalStrength = packet.signalStrength
			}

			is UDPPacket20Temperature -> {
				tracker = connection?.getTracker(packet.sensorId) ?: return
				tracker.temperature = packet.temperature
			}

			is UDPPacket21UserAction -> {
				if (connection == null) return
				var name = ""
				when (packet.type) {
					UDPPacket21UserAction.RESET_FULL -> {
						name = "Full reset"
						VRServer.instance.resetHandler.sendStarted(ResetType.Full)
						VRServer.instance.resetTrackersFull(RESET_SOURCE_NAME)
					}

					UDPPacket21UserAction.RESET_YAW -> {
						name = "Yaw reset"
						VRServer.instance.resetHandler.sendStarted(ResetType.Yaw)
						VRServer.instance.resetTrackersYaw(RESET_SOURCE_NAME)
					}

					UDPPacket21UserAction.RESET_MOUNTING -> {
						name = "Mounting reset"
						VRServer
							.instance
							.resetHandler
							.sendStarted(ResetType.Mounting)
						VRServer.instance.resetTrackersMounting(RESET_SOURCE_NAME)
					}

					UDPPacket21UserAction.PAUSE_TRACKING -> {
						name = "Pause tracking toggle"
						VRServer.instance.togglePauseTracking(RESET_SOURCE_NAME)
					}
				}

				LogManager.info(
					"[TrackerServer] User action from ${connection.descriptiveName} received. $name performed.",
				)
			}

			is UDPPacket22FeatureFlags -> {
				if (connection == null) return
				// Respond with server flags
				bb.limit(bb.capacity())
				bb.rewind()
				parser.write(bb, connection, packet)
				socket.send(DatagramPacket(rcvBuffer, bb.position(), connection.address))
				connection.firmwareFeatures = packet.firmwareFeatures
			}

			is UDPPacket24AckConfigChange -> {
				if (connection == null) return
				val queue = queues[Triple(connection.address, packet.configType, packet.sensorId)] ?: run {
					LogManager.severe("[TrackerServer] Error, acknowledgment of config change that we don't have in our queue.")
					return
				}
				val changed = queue.removeFirst()
				changed.channel.resume(true)
				val trackers = if (SensorSpecificPacket.isGlobal(packet.sensorId)) {
					connection.trackers.values.toList()
				} else {
					listOf(connection.getTracker(packet.sensorId) ?: return)
				}
				LogManager.info("[TrackerServer] Acknowledged config change on ${connection.descriptiveName} (${trackers.map { it.trackerNum }.joinToString()}). Config changed on ${packet.configType}")
			}

			is UDPPacket26Log -> {
				if (connection == null) return
				connection.logMessages.add(packet.message)
			}

			is UDPPacket200ProtocolChange -> {}
		}
	}

	fun getConnections(): List<UDPDevice?> = connections

	// FIXME: for some reason it ends up disconnecting after 30 seconds have passed instead of immediately
	fun disconnectDevice(device: UDPDevice) {
		synchronized(connections) {
			connections.remove(device)
		}
		synchronized(connectionsByAddress) {
			connectionsByAddress.filter { (_, dev) -> dev.id == device.id }.keys.forEach(
				connectionsByAddress::remove,
			)
		}
		device.trackers.forEach { (_, tracker) ->
			tracker.status = TrackerStatus.DISCONNECTED
		}

		LogManager.info(
			"[TrackerServer] Forcefully disconnected ${device.hardwareIdentifier} device.",
		)
	}

	companion object {
		/**
		 * Change between IMU axes and OpenGL/SteamVR axes
		 */
		private val AXES_OFFSET = fromRotationVector(-FastMath.HALF_PI, 0f, 0f)
		private const val RESET_SOURCE_NAME = "TrackerServer"
		private fun packetToString(packet: DatagramPacket?): String {
			val sb = StringBuilder()
			sb.append("DatagramPacket{")
			if (packet == null) {
				sb.append("null")
			} else {
				sb.append(packet.address.toString())
				sb.append(packet.port)
				sb.append(',')
				sb.append(packet.length)
				sb.append(',')
				sb.append(ArrayUtils.toString(packet.data))
			}
			sb.append('}')
			return sb.toString()
		}
	}
}
