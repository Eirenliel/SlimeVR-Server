package dev.slimevr.osc

import com.illposed.osc.MessageSelector
import com.illposed.osc.OSCBundle
import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCMessageEvent
import com.illposed.osc.OSCMessageListener
import com.illposed.osc.OSCSerializeException
import com.illposed.osc.messageselector.OSCPatternAddressMessageSelector
import com.illposed.osc.transport.OSCPortIn
import com.illposed.osc.transport.OSCPortOut
import com.jme3.math.FastMath
import com.jme3.system.NanoTimer
import dev.slimevr.VRServer
import dev.slimevr.bridge.ISteamVRBridge
import dev.slimevr.config.VRCOSCConfig
import dev.slimevr.tracking.processor.HumanPoseManager
import dev.slimevr.tracking.trackers.Device
import dev.slimevr.tracking.trackers.Tracker
import dev.slimevr.tracking.trackers.TrackerPosition
import dev.slimevr.tracking.trackers.TrackerStatus
import io.eiren.util.collections.FastList
import io.eiren.util.logging.LogManager
import io.github.axisangles.ktmath.EulerAngles
import io.github.axisangles.ktmath.EulerOrder
import io.github.axisangles.ktmath.Quaternion
import io.github.axisangles.ktmath.Vector3
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.Timer
import java.util.TimerTask

private const val OFFSET_SLERP_FACTOR = 0.5f // Guessed from eyeing VRChat

/**
 * VRChat OSCTracker documentation: https://docs.vrchat.com/docs/osc-trackers
 */
class VRCOSCHandler(
	private val server: VRServer,
	private val humanPoseManager: HumanPoseManager,
	private val steamvrBridge: ISteamVRBridge?,
	private val config: VRCOSCConfig,
	private val computedTrackers: List<Tracker>,
) : OSCHandler {
	private var oscReceiver: OSCPortIn? = null
	private var oscSender: OSCPortOut? = null
	private var oscMessage: OSCMessage? = null
	private var vrcHmd: Tracker? = null
	private var headTracker: Tracker? = null
	private var oscTrackersDevice: Device? = null
	private val oscArgs = FastList<Float?>(3)
	private val trackersEnabled: BooleanArray = BooleanArray(computedTrackers.size)
	private var lastPortIn = 0
	private var lastPortOut = 0
	private var lastAddress: InetAddress? = null
	private var timeAtLastError: Long = 0
	private val timer = Timer()
	private var listenTrackers = false
	private var receivingPositionOffset = Vector3.NULL
	private var postReceivingPositionOffset = Vector3.NULL
	private var receivingRotationOffset = Quaternion.IDENTITY
	private var receivingRotationOffsetGoal = Quaternion.IDENTITY
	private val postReceivingOffset = EulerAngles(EulerOrder.YXZ, 0f, FastMath.PI, 0f).toQuaternion()
	private var timeAtLastReceivedRotationOffset = System.currentTimeMillis()
	private var fpsTimer: NanoTimer? = null
	private val accessoryTrackers: MutableList<Tracker> = FastList()

	init {
		refreshSettings(false)
	}

	override fun refreshSettings(refreshRouterSettings: Boolean) {
		// Sets which trackers are enabled and force head and hands to false
		for (i in computedTrackers.indices) {
			if (computedTrackers[i].trackerPosition !== TrackerPosition.HEAD || computedTrackers[i].trackerPosition !== TrackerPosition.LEFT_HAND || computedTrackers[i].trackerPosition !== TrackerPosition.RIGHT_HAND) {
				trackersEnabled[i] = config
					.getOSCTrackerRole(
						computedTrackers[i].trackerPosition!!.trackerRole!!,
						false
					)
			} else {
				trackersEnabled[i] = false
			}
		}

		// Stops listening and closes OSC port
		val wasListening = oscReceiver != null && oscReceiver!!.isListening
		if (wasListening) {
			oscReceiver!!.stopListening()
		}
		val wasConnected = oscSender != null && oscSender!!.isConnected
		if (wasConnected) {
			try {
				oscSender!!.close()
			} catch (e: IOException) {
				LogManager.severe("[VRCOSCHandler] Error closing the OSC sender: $e")
			}
		}
		if (config.enabled) {
			// Instantiates the OSC receiver
			try {
				val port = config.portIn
				oscReceiver = OSCPortIn(
					OSCStatic.serializer,
					port
				)
				if (lastPortIn != port || !wasListening) {
					LogManager.info("[VRCOSCHandler] Listening to port $port")
				}
				lastPortIn = port
			} catch (e: IOException) {
				LogManager
					.severe(
						"[VRCOSCHandler] Error listening to the port " +
							config.portIn +
							": " +
							e
					)
			}

			// Starts listening for VRC or OSCTrackers messages
			if (oscReceiver != null) {
				val listener = OSCMessageListener { event: OSCMessageEvent -> handleReceivedMessage(event) }
				val vrcSelector: MessageSelector = OSCPatternAddressMessageSelector(
					"/avatar/parameters/Upright"
				)
				val trackersPositionSelector: MessageSelector = OSCPatternAddressMessageSelector(
					"/tracking/trackers/*/position"
				)
				val trackersRotationSelector: MessageSelector = OSCPatternAddressMessageSelector(
					"/tracking/trackers/*/rotation"
				)
				oscReceiver!!.dispatcher.addListener(vrcSelector, listener)
				oscReceiver!!.dispatcher.addListener(trackersPositionSelector, listener)
				oscReceiver!!.dispatcher.addListener(trackersRotationSelector, listener)
				listenTrackers = false
				oscReceiver!!.startListening()
				// Delay so we can actually detect if SteamVR is running
				scheduleStartListeningSteamVR(1000)
			}

			// Instantiate the OSC sender
			try {
				val address = InetAddress.getByName(config.address)
				val port = config.portOut
				oscSender = OSCPortOut(
					OSCStatic.serializer,
					InetSocketAddress(address, port)
				)
				if (lastPortOut != port && lastAddress !== address || !wasConnected) {
					LogManager
						.info(
							"[VRCOSCHandler] Sending to port " +
								port +
								" at address " +
								address.toString()
						)
				}
				lastPortOut = port
				lastAddress = address
				oscSender!!.connect()
			} catch (e: IOException) {
				LogManager
					.severe(
						"[VRCOSCHandler] Error connecting to port " +
							config.portOut +
							" at the address " +
							config.address +
							": " +
							e
					)
			}
		}
		if (refreshRouterSettings) server.oSCRouter.refreshSettings(false)
	}

	private fun scheduleStartListeningSteamVR(delay: Long) {
		val resetTask: TimerTask = object : TimerTask() {
			override fun run() {
				listenTrackers = true
			}
		}
		timer.schedule(resetTask, delay)
	}

	private fun handleReceivedMessage(event: OSCMessageEvent) {
		if (listenTrackers) {
			if (event.message.address.equals("/avatar/parameters/Upright")) {
				// Receiving HMD data from VRChat
				if (steamvrBridge != null && !steamvrBridge.isConnected()) {
					if (vrcHmd == null) {
						val vrcDevice = server.deviceManager.createDevice("VRChat OSC", null, "VRChat")
						server.deviceManager.addDevice(vrcDevice)
						vrcHmd = Tracker(
							device = vrcDevice,
							id = VRServer.getNextLocalTrackerId(),
							name = "VRC HMD",
							displayName = "VRC HMD",
							trackerPosition = TrackerPosition.HEAD,
							trackerNum = 0,
							hasPosition = true,
							userEditable = false,
							isComputed = true,
							usesTimeout = true
						)
						vrcDevice.trackers[0] = vrcHmd!!
						server.registerTracker(vrcHmd!!)
					}

					// Sets HMD status to OK
					vrcHmd!!.status = TrackerStatus.OK

					// Sets the HMD y position to
					// the vrc Upright parameter (0-1) * the user's height
					vrcHmd!!
						.position = Vector3(
						0f,
						event
							.message
							.arguments[0] as Float * humanPoseManager.userHeightFromConfig,
						0f
					)
					vrcHmd!!.dataTick()
				}
			} else {
				// Receiving OSC Trackers data
				if (oscTrackersDevice == null) {
					// Instantiate OSC Trackers device
					oscTrackersDevice = server.deviceManager.createDevice("OSC Tracker", null, "OSC Trackers")
					server.deviceManager.addDevice(oscTrackersDevice!!)
				}

				// Extract the x in "/tracking/trackers/x.../..."
				val trackerStringValue = event.message.address.toString().subSequence(19, 20)
				if (trackerStringValue == "h") {
					// Head data
					val slimeHead = headTracker
					if (event.message.address.toString() == "/tracking/trackers/head/position") {
						// Position offset
						receivingPositionOffset = Vector3(
							event.message.arguments[0] as Float,
							event.message.arguments[1] as Float,
							-(event.message.arguments[2] as Float)
						)

						if (slimeHead != null && slimeHead.hasPosition) {
							postReceivingPositionOffset = slimeHead.position
						}
					} else {
						// Rotation offset
						val (w, x, y, z) = EulerAngles(EulerOrder.YXZ, event.message.arguments[0] as Float * FastMath.DEG_TO_RAD, event.message.arguments[1] as Float * FastMath.DEG_TO_RAD, event.message.arguments[2] as Float * FastMath.DEG_TO_RAD).toQuaternion()
						receivingRotationOffsetGoal = Quaternion(w, -x, -y, z).inv()

						receivingRotationOffsetGoal = if (slimeHead != null && slimeHead.hasRotation) {
							slimeHead.getRotation().project(Vector3.POS_Y).unit() * receivingRotationOffsetGoal
						} else {
							receivingRotationOffsetGoal
						}

						// If greater than 300ms, snap to rotation
						if (System.currentTimeMillis() - timeAtLastReceivedRotationOffset > 300) {
							receivingRotationOffset = receivingRotationOffsetGoal
						}

						// Update time variable
						timeAtLastReceivedRotationOffset = System.currentTimeMillis()
					}
				} else {
					// Trackers data (1-8)
					val trackerId = trackerStringValue[0].digitToInt()
					var tracker = oscTrackersDevice!!.trackers[trackerId]

					if (tracker == null) {
						tracker = Tracker(
							device = oscTrackersDevice,
							id = VRServer.getNextLocalTrackerId(),
							name = "OSC Tracker #$trackerId",
							displayName = "OSC Tracker #$trackerId",
							trackerNum = trackerId,
							trackerPosition = null,
							hasRotation = true,
							hasPosition = true,
							userEditable = true,
							isComputed = true,
							needsReset = true,
							usesTimeout = true
						)
						oscTrackersDevice!!.trackers[trackerId] = tracker
						server.registerTracker(tracker)
					}

					// Sets the tracker status to OK
					tracker.status = TrackerStatus.OK

					if (event.message.address.toString() == "/tracking/trackers/$trackerId/position") {
						tracker.position = receivingRotationOffset.sandwich(
							Vector3(
								event.message.arguments[0] as Float,
								event.message.arguments[1] as Float,
								-(event.message.arguments[2] as Float)
							) - receivingPositionOffset
						) + postReceivingPositionOffset
					} else {
						val (w, x, y, z) = EulerAngles(EulerOrder.YXZ, event.message.arguments[0] as Float * FastMath.DEG_TO_RAD, event.message.arguments[1] as Float * FastMath.DEG_TO_RAD, event.message.arguments[2] as Float * FastMath.DEG_TO_RAD).toQuaternion()
						val rot = Quaternion(w, -x, -y, z)
						tracker.setRotation(receivingRotationOffset * rot * postReceivingOffset)
					}

					tracker.dataTick()
				}
			}
		}
	}

	override fun update() {
		// Update current time
		val currentTime = System.currentTimeMillis().toFloat()

		// Gets timer from vrServer
		if (fpsTimer == null) {
			fpsTimer = VRServer.instance.fpsTimer
		}
		// Update received trackers' offset rotation slerp
		if (receivingRotationOffset != receivingRotationOffsetGoal) {
			receivingRotationOffset = receivingRotationOffset.interpR(receivingRotationOffsetGoal, OFFSET_SLERP_FACTOR * (fpsTimer?.timePerFrame ?: 1f))
		}

		// Send OSC data
		if (oscSender != null && oscSender!!.isConnected) {
			// Create new bundle
			val bundle = OSCBundle()

			// Send computed trackers as OSC Trackers
			for (i in computedTrackers.indices) {
				if (trackersEnabled[i]) {
					// Send regular trackers' positions
					val (x, y, z) = computedTrackers[i].position
					oscArgs.clear()
					oscArgs.add(x)
					oscArgs.add(y)
					oscArgs.add(-z)
					bundle.addPacket(
						OSCMessage(
							"/tracking/trackers/${getVRCOSCTrackersId(computedTrackers[i].trackerPosition)}/position",
							oscArgs.clone()
						)
					)

					// Send regular trackers' rotations
					val (w, x1, y1, z1) = computedTrackers[i].getRotation()
					// We flip the X and Y components of the quaternion because
					// we flip the z direction when communicating from
					// our right-handed API to VRChat's left-handed API.
					// X quaternion represents a rotation from y to z
					// Y quaternion represents a rotation from z to x
					// When we negate the z direction, X and Y quaternion
					// components must be negated.
					val (_, x2, y2, z2) = Quaternion(w, -x1, -y1, z1).toEulerAngles(EulerOrder.YXZ)
					oscArgs.clear()
					oscArgs.add(x2 * FastMath.RAD_TO_DEG)
					oscArgs.add(y2 * FastMath.RAD_TO_DEG)
					oscArgs.add(z2 * FastMath.RAD_TO_DEG)
					bundle.addPacket(
						OSCMessage(
							"/tracking/trackers/${getVRCOSCTrackersId(computedTrackers[i].trackerPosition)}/rotation",
							oscArgs.clone()
						)
					)
				}
				if (computedTrackers[i].trackerPosition === TrackerPosition.HEAD) {
					// Send HMD position
					val (x, y, z) = computedTrackers[i].position
					oscArgs.clear()
					oscArgs.add(x)
					oscArgs.add(y)
					oscArgs.add(-z)
					bundle.addPacket(
						OSCMessage(
							"/tracking/trackers/head/position",
							oscArgs.clone()
						)
					)
				}
			}

			// Send accessory trackers for custom avatars
			for (i in accessoryTrackers.indices) {
				if (accessoryTrackers[i].trackerPosition == TrackerPosition.ACCESSORY && accessoryTrackers[i].status.sendData) {
					val (_, x, y, z) = accessoryTrackers[i].getRotation().toEulerAngles(EulerOrder.YZX)
					// Note: we must send the rotations as -1 to 1
					// https://creators.vrchat.com/avatars/animator-parameters/#parameter-types
					// Horizontal
					oscArgs.clear()
					oscArgs.add(x / FastMath.PI)
					bundle.addPacket(OSCMessage("/avatar/parameters/SVRAccessory${accessoryTrackers[i].accessoryId}X", oscArgs.clone()))
					// Vertical
					oscArgs.clear()
					oscArgs.add(y / FastMath.PI)
					bundle.addPacket(OSCMessage("/avatar/parameters/SVRAccessory${accessoryTrackers[i].accessoryId}Y", oscArgs.clone()))
					// Twist
					oscArgs.clear()
					oscArgs.add(z / FastMath.PI)
					bundle.addPacket(OSCMessage("/avatar/parameters/SVRAccessory${accessoryTrackers[i].accessoryId}Z", oscArgs.clone()))
				}
			}

			try {
				oscSender!!.send(bundle)
			} catch (e: IOException) {
				// Avoid spamming AsynchronousCloseException too many
				// times per second
				if (currentTime - timeAtLastError > 100) {
					timeAtLastError = System.currentTimeMillis()
					LogManager.warning("[VRCOSCHandler] Error sending OSC message to VRChat: $e")
				}
			} catch (e: OSCSerializeException) {
				if (currentTime - timeAtLastError > 100) {
					timeAtLastError = System.currentTimeMillis()
					LogManager.warning("[VRCOSCHandler] Error sending OSC message to VRChat: $e")
				}
			}
		}
	}

	private fun getVRCOSCTrackersId(trackerPosition: TrackerPosition?): Int {
		// The order doesn't matter and changing it
		// won't break anything except make debugging harder
		// between different versions. They just need to range from 1-8
		return when (trackerPosition) {
			TrackerPosition.HIP -> 1
			TrackerPosition.LEFT_FOOT -> 2
			TrackerPosition.RIGHT_FOOT -> 3
			TrackerPosition.LEFT_UPPER_LEG -> 4
			TrackerPosition.RIGHT_UPPER_LEG -> 5
			TrackerPosition.UPPER_CHEST -> 6
			TrackerPosition.LEFT_UPPER_ARM -> 7
			TrackerPosition.RIGHT_UPPER_ARM -> 8
			else -> -1
		}
	}

	fun setHeadTracker(headTracker: Tracker?) {
		this.headTracker = headTracker
	}

	/**
	 * Sends the expected HMD rotation upon reset to align the trackers in VRC
	 */
	fun yawAlign(headRot: Quaternion) {
		if (oscSender != null && oscSender!!.isConnected) {
			val (_, _, y, _) = headRot.toEulerAngles(EulerOrder.YXZ)
			oscArgs.clear()
			oscArgs.add(0f)
			oscArgs.add(-y * FastMath.RAD_TO_DEG)
			oscArgs.add(0f)
			oscMessage = OSCMessage(
				"/tracking/trackers/head/rotation",
				oscArgs
			)
			try {
				oscSender!!.send(oscMessage)
			} catch (e: IOException) {
				LogManager
					.warning("[VRCOSCHandler] Error sending OSC message to VRChat: $e")
			} catch (e: OSCSerializeException) {
				LogManager
					.warning("[VRCOSCHandler] Error sending OSC message to VRChat: $e")
			}
		}
	}

	/**
	 * Adds an accessory tracker to the list of trackers to send.
	 *
	 * @param tracker the accessory tracker
	 */
	fun addAccessoryTracker(tracker: Tracker) {
		if (!accessoryTrackers.contains(tracker)) {
			accessoryTrackers.add(tracker)
		}
	}

	override fun getOscSender(): OSCPortOut {
		return oscSender!!
	}

	override fun getPortOut(): Int {
		return lastPortOut
	}

	override fun getAddress(): InetAddress {
		return lastAddress!!
	}

	override fun getOscReceiver(): OSCPortIn {
		return oscReceiver!!
	}

	override fun getPortIn(): Int {
		return lastPortIn
	}
}
