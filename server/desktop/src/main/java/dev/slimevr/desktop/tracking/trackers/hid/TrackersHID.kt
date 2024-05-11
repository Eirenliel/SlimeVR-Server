package dev.slimevr.desktop.tracking.trackers.hid

import dev.slimevr.VRServer
import dev.slimevr.tracking.trackers.Device
import dev.slimevr.tracking.trackers.Tracker
import dev.slimevr.tracking.trackers.Tracker.Companion.axisOffset
import dev.slimevr.tracking.trackers.TrackerStatus
import dev.slimevr.tracking.trackers.udp.IMUType
import io.eiren.util.logging.LogManager
import io.github.axisangles.ktmath.Quaternion
import io.github.axisangles.ktmath.Vector3
import org.hid4java.HidDevice
import org.hid4java.HidManager
import org.hid4java.HidServices
import org.hid4java.HidServicesListener
import org.hid4java.HidServicesSpecification
import org.hid4java.event.HidServicesEvent
import org.hid4java.jna.HidApi
import org.hid4java.jna.HidDeviceInfoStructure
import java.util.function.Consumer
import kotlin.experimental.and

private const val HID_TRACKER_RECEIVER_VID = 0x2FE3
private const val HID_TRACKER_RECEIVER_PID = 0x5652

/**
 * Receives trackers data by UDP using extended owoTrack protocol.
 */
class TrackersHID(name: String, private val trackersConsumer: Consumer<Tracker>) :
	Thread(name),
	HidServicesListener {
	private val devices: MutableList<HIDDevice> = mutableListOf()
	private val devicesBySerial: MutableMap<String, MutableList<Int>> = HashMap()
	private val devicesByHID: MutableMap<HidDevice, MutableList<Int>> = HashMap()
	private val hidServicesSpecification = HidServicesSpecification()
	private val hidServices: HidServices

	init {
		hidServicesSpecification.setAutoStart(false)
		hidServices = HidManager.getHidServices(hidServicesSpecification)
		hidServices.addHidServicesListener(this)
		val dataReadThread = Thread(dataReadRunnable)
		dataReadThread.isDaemon = true
		dataReadThread.name = "hid4java data reader"
		dataReadThread.start()
		// We use hid4java but actually do not start the service ever, because it will just enumerate everything and cause problems
		// Do enumeration ourself
		val deviceEnumerateThread = Thread(deviceEnumerateRunnable)
		deviceEnumerateThread.isDaemon = true
		deviceEnumerateThread.name = "hid4java device enumerator"
		deviceEnumerateThread.start()
	}

	private fun checkConfigureDevice(hidDevice: HidDevice) {
		if (hidDevice.vendorId == HID_TRACKER_RECEIVER_VID && hidDevice.productId == HID_TRACKER_RECEIVER_PID) { // TODO: Use correct ids
			if (hidDevice.isClosed) {
				check(hidDevice.open()) { "Unable to open device" }
			}
			// TODO: Configure the device here
			val serial = hidDevice.serialNumber ?: "Unknown HID Device"
			// val product = hidDevice.product
			// val manufacturer = hidDevice.manufacturer
			this.devicesBySerial[serial]?.let {
				this.devicesByHID[hidDevice] = it
				synchronized(this.devices) {
					for (id in it) {
						val device = this.devices[id]
						for (value in device.trackers.values) {
							if (value.status == TrackerStatus.DISCONNECTED) value.status = TrackerStatus.OK
						}
					}
				}
				LogManager.info("[TrackerServer] Linked HID device reattached: $serial")
				return
			}

			// TODO: Need to check that the hidDevice is the same or different?
			// TODO: Get firmware and manufacturer from the device?
			val list: MutableList<Int> = mutableListOf()
			this.devicesBySerial[serial] = list
			this.devicesByHID[hidDevice] = list
			LogManager.info("[TrackerServer] (Probably) Compatible HID device detected: $serial")
		}
	}

	private fun removeDevice(hidDevice: HidDevice) {
		this.devicesByHID[hidDevice]?.let {
			synchronized(this.devices) {
				for (id in it) {
					val device = this.devices[id]
					for (value in device.trackers.values) {
						if (value.status == TrackerStatus.OK) {
							value.status =
								TrackerStatus.DISCONNECTED
						}
					}
				}
			}
			this.devicesByHID.remove(hidDevice)
			LogManager.info("[TrackerServer] Linked HID device removed: ${hidDevice.serialNumber}")
		}
	}

	private fun setUpSensor(device: HIDDevice, trackerId: Int, sensorType: IMUType, sensorStatus: TrackerStatus) {
		// LogManager.info("[TrackerServer] Sensor $trackerId for ${device.name}, status $sensorStatus")
		var imuTracker = device.getTracker(trackerId)
		if (imuTracker == null) {
			imuTracker = Tracker(
				device,
				VRServer.getNextLocalTrackerId(),
				device.name + "/" + trackerId,
				"IMU Tracker #" + VRServer.currentLocalTrackerId,
				null,
				trackerNum = trackerId,
				hasRotation = true,
				hasAcceleration = true,
				userEditable = true,
				imuType = sensorType,
				allowFiltering = true,
				needsReset = true,
				needsMounting = true,
				usesTimeout = false,
			)
			// usesTimeout false because HID trackers aren't "Disconnected" unless receiver is physically removed probably
			// TODO: Could tracker maybe use "Timed out" status without marking as disconnecting?
			device.trackers[trackerId] = imuTracker
			trackersConsumer.accept(imuTracker)
			imuTracker.status = sensorStatus
			LogManager
				.info(
					"[TrackerServer] Added sensor $trackerId for ${device.name}, type $sensorType",
				)
		}
	}

	private fun deviceIdLookup(hidDevice: HidDevice, deviceId: Int, deviceList: MutableList<Int>): HIDDevice {
		synchronized(this.devices) {
			deviceList.map { this.devices[it] }.find { it.hidId == deviceId }?.let { return it }
			val device = HIDDevice(deviceId)
			// server wants tracker to be unique, so use combination of hid serial and full id
			device.name = hidDevice.serialNumber ?: "Unknown HID Device"
			device.name += "-$deviceId"
			device.manufacturer = "HID Device" // TODO:
			this.devices.add(device)
			deviceList.add(this.devices.size - 1)
			VRServer.instance.deviceManager.addDevice(device) // actually add device to the server
			return device
		}
	}

	@get:Synchronized
	private val dataReadRunnable: Runnable
		get() = Runnable {
			while (true) {
				try {
					sleep(0) // Possible performance impact
				} catch (e: InterruptedException) {
					currentThread().interrupt()
					break
				}
				dataRead() // not in try catch?
			}
		}

	@get:Synchronized
	private val deviceEnumerateRunnable: Runnable
		get() = Runnable {
			try {
				sleep(100) // Delayed start
			} catch (e: InterruptedException) {
				currentThread().interrupt()
				return@Runnable
			}
			while (true) {
				try {
					sleep(1000)
				} catch (e: InterruptedException) {
					currentThread().interrupt()
					break
				}
				deviceEnumerate() // not in try catch?
			}
		}

	private fun dataRead() {
		synchronized(devicesByHID) {
			var devicesPresent = false
			val q = intArrayOf(0, 0, 0, 0)
			val a = intArrayOf(0, 0, 0)
			for ((hidDevice, deviceList) in devicesByHID) {
				val dataReceived: Array<Byte> = try {
					hidDevice.read(80, 0) // Read up to 80 bytes
				} catch (e: NegativeArraySizeException) {
					continue // Skip devices with read error (Maybe disconnected)
				}
				devicesPresent = true // Even if the device has no data
				if (dataReceived.isNotEmpty()) {
					// Process data
					// TODO: make this less bad
					if (dataReceived.size % 20 != 0) {
						LogManager.info("[TrackerServer] Malformed HID packet, ignoring")
						continue // Don't continue with this data
					}
					val packetCount = dataReceived.size / 20
					var i = 0
					while (i < packetCount * 20) {
						// dataReceived[i] //for later
						val idCombination = dataReceived[i + 1].toInt()
						val rssi = -dataReceived[i + 2].toInt()
						val battery = dataReceived[i + 3].toInt()
						// ushort big endian
						val battery_mV = dataReceived[i + 5].toUByte().toInt() shl 8 or dataReceived[i + 4].toUByte().toInt()
						// Q15: 1 is represented as 0x7FFF, -1 as 0x8000
						// The sender can use integer saturation to avoid overflow
						for (j in 0..3) { // quat received as fixed Q15
							// Q15 as short big endian
							q[j] = dataReceived[i + 6 + j * 2 + 1].toInt() shl 8 or dataReceived[i + 6 + j * 2].toUByte().toInt()
						}
						for (j in 0..2) { // accel received as fixed 7, in m/s
							// Q15 as short big endian
							a[j] = dataReceived[i + 14 + j * 2 + 1].toInt() shl 8 or dataReceived[i + 14 + j * 2].toUByte().toInt()
						}
						val trackerId = idCombination and 0b1111
						val deviceId = (idCombination shr 4) and 0b1111
						val device = deviceIdLookup(hidDevice, deviceId, deviceList)
						// server wants tracker to be unique, so use combination of hid serial and full id
						setUpSensor(device, trackerId, IMUType.UNKNOWN, TrackerStatus.OK)
						val tracker = device.getTracker(trackerId)!!

						tracker.signalStrength = rssi
						// tracker.batteryVoltage = if (battery and 128 == 128) 5f else 0f // Charge status
						tracker.batteryVoltage = battery_mV.toFloat() * 0.001f
						tracker.batteryLevel = (battery and 127).toFloat()
						// The data comes in the same order as in the UDP protocol
						// x y z w -> w x y z
						var rot = Quaternion(q[3].toFloat(), q[0].toFloat(), q[1].toFloat(), q[2].toFloat())
						val scaleRot = 1 / (1 shl 15).toFloat() // compile time evaluation
						rot *= scaleRot // no division
						rot = axisOffset(rot)
						tracker.setRotation(rot)
						// TODO: I think the acceleration is wrong???
						// Yes it was. And rotation was wrong too.
						// At lease we have fixed the fixed point decoding.
						val scaleAccel = 1 / (1 shl 7).toFloat() // compile time evaluation
						var acceleration = Vector3(a[0].toFloat(), a[1].toFloat(), a[2].toFloat()).times(scaleAccel) // no division
						acceleration = axisOffset(acceleration)
						tracker.setAcceleration(acceleration)
						tracker.dataTick()
						i += 20
					}
					// LogManager.info("[TrackerServer] HID received $packetCount tracker packets")
				}
			}
			if (!devicesPresent) {
				sleep(10) // No hid device, "empty loop" so sleep to save the poor cpu
			}
		}
	}

	private fun deviceEnumerate() {
		var root: HidDeviceInfoStructure? = null
		try {
			root = HidApi.enumerateDevices(HID_TRACKER_RECEIVER_VID, HID_TRACKER_RECEIVER_PID) // TODO: change to proper vendorId and productId, need to enum all appropriate productId
		} catch (e: Throwable) {
			LogManager.severe("[TrackerServer] Couldn't enumerate HID devices", e)
		}
		val hidDeviceList: MutableList<HidDevice> = mutableListOf()
		if (root != null) {
			var hidDeviceInfoStructure: HidDeviceInfoStructure? = root
			do {
				hidDeviceList.add(HidDevice(hidDeviceInfoStructure, null, hidServicesSpecification))
				hidDeviceInfoStructure = hidDeviceInfoStructure?.next()
			} while (hidDeviceInfoStructure != null)
			HidApi.freeEnumeration(root)
		}
		synchronized(devicesByHID) {
			// Work on devicesByHid and add/remove as necessary
			val removeList: MutableList<HidDevice> = devicesByHID.keys.toMutableList()
			removeList.removeAll(hidDeviceList)
			hidDeviceList.removeAll(devicesByHID.keys) // addList
			for (device in removeList) {
				removeDevice(device)
			}
			for (device in hidDeviceList) {
				checkConfigureDevice(device)
			}
		}
	}

	override fun run() { // Doesn't seem to run
	}

	fun getDevices(): List<Device> = devices

	// We don't use these
	override fun hidDeviceAttached(event: HidServicesEvent) {
	}

	override fun hidDeviceDetached(event: HidServicesEvent) {
	}

	override fun hidFailure(event: HidServicesEvent) {
	}

	override fun hidDataReceived(p0: HidServicesEvent?) {
	}

	companion object {
		private const val resetSourceName = "TrackerServer"
	}
}
