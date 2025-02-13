package dev.slimevr.poseframeformat

import dev.slimevr.config.SkeletonConfig
import dev.slimevr.poseframeformat.trackerdata.TrackerFrame
import dev.slimevr.poseframeformat.trackerdata.TrackerFrames
import io.eiren.util.logging.LogManager
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * PoseFrameStream File IO, designed to handle the internal PoseFrames format with
 * the new file format for streaming and storing additional debugging info
 */
object PfsIO {
	private fun writeRecordingDef(stream: DataOutputStream, frameInterval: Float) {
		stream.writeByte(PfsPackets.RECORDING_DEFINITION.id)
		stream.writeFloat(frameInterval)
	}

	private fun writeTrackerDef(stream: DataOutputStream, id: Int, name: String) {
		stream.writeByte(PfsPackets.TRACKER_DEFINITION.id)
		stream.writeByte(id)
		stream.writeUTF(name)
	}

	private fun writeTrackerFrame(stream: DataOutputStream, id: Int, frameIndex: Int, frame: TrackerFrame) {
		stream.writeByte(PfsPackets.TRACKER_FRAME.id)
		stream.writeByte(id)
		stream.writeInt(frameIndex)
		// Write frame data (same format as PFR)
		PfrIO.writeFrame(stream, frame)
	}

	private fun writeBodyProportions(stream: DataOutputStream, skeletonConfig: SkeletonConfig) {
		stream.writeByte(PfsPackets.PROPORTIONS_CONFIG.id)
		// HMD height
		stream.writeFloat(skeletonConfig.hmdHeight)
		// Floor height
		stream.writeFloat(skeletonConfig.floorHeight)
		// Write config map
		stream.writeShort(skeletonConfig.offsets.size)
		for ((key, value) in skeletonConfig.offsets) {
			stream.writeUTF(key)
			stream.writeFloat(value)
		}
	}

	fun writeFrames(stream: DataOutputStream, frames: PoseFrames) {
		// Give trackers IDs (max 255)
		val trackers = frames.frameHolders.mapIndexed { i, t -> i to t }

		// Write recording definition
		writeRecordingDef(stream, frames.frameInterval)

		// Write tracker definitions
		for (tracker in trackers) {
			writeTrackerDef(stream, tracker.first, tracker.second.name)
		}

		// Write tracker frames
		for (i in 0 until frames.maxFrameCount) {
			for (tracker in trackers) {
				// If the tracker has a frame at the index
				val frame = tracker.second.tryGetFrame(i)
				if (frame != null) {
					writeTrackerFrame(stream, tracker.first, i, frame)
				}
			}
		}
	}

	fun tryWriteFrames(stream: DataOutputStream, frames: PoseFrames): Boolean = try {
		writeFrames(stream, frames)
		true
	} catch (e: Exception) {
		LogManager.severe("[PfsIO] Error writing frame to stream.", e)
		false
	}

	fun writeToFile(file: File, frames: PoseFrames) {
		DataOutputStream(
			BufferedOutputStream(FileOutputStream(file)),
		).use { writeFrames(it, frames) }
	}

	fun tryWriteToFile(file: File, frames: PoseFrames): Boolean = try {
		writeToFile(file, frames)
		true
	} catch (e: Exception) {
		LogManager.severe("[PfsIO] Error writing frames to file.", e)
		false
	}

	fun readFrame(stream: DataInputStream, poseFrames: PoseFrames, trackers: MutableMap<Int, TrackerFrames>) {
		val packetId = stream.readUnsignedByte()
		val packetType = PfsPackets.byId[packetId]

		when (packetType) {
			null -> {
				throw IOException("Encountered unknown packet ID ($packetId) while deserializing PFS stream.")
			}

			PfsPackets.RECORDING_DEFINITION -> {
				// Unused, useful for debugging
				val frameInterval = stream.readFloat()
				poseFrames.frameInterval = frameInterval
				LogManager.debug("[PfsIO] Frame interval: $frameInterval s")
			}

			PfsPackets.TRACKER_DEFINITION -> {
				val trackerId = stream.readUnsignedByte()
				val name = stream.readUTF()

				// Get or make tracker and set its name
				trackers.getOrPut(trackerId) {
					TrackerFrames(name)
				}.name = name
			}

			PfsPackets.TRACKER_FRAME -> {
				val trackerId = stream.readUnsignedByte()
				val tracker = trackers.getOrPut(trackerId) {
					// If tracker doesn't exist yet, make one
					TrackerFrames()
				}
				val frameNum = stream.readInt()
				val frame = PfrIO.readFrame(stream)

				tracker.frames.add(frameNum, frame)
			}

			PfsPackets.PROPORTIONS_CONFIG -> {
				// Unused, useful for debugging

				val hmdHeight = stream.readFloat()
				val floorHeight = stream.readFloat()
				LogManager.debug("[PfsIO] HMD height: $hmdHeight, Floor height: $floorHeight")

				// Currently just prints JSON format config to console
				val configCount = stream.readUnsignedShort()
				val sb = StringBuilder("[PfsIO] Body proportion configs ($configCount): {")
				for (i in 0 until configCount) {
					if (i > 0) {
						sb.append(", ")
					}
					sb.append(stream.readUTF())
					sb.append(": ")
					sb.append(stream.readFloat())
				}
				sb.append('}')

				LogManager.debug(sb.toString())
			}
		}
	}

	fun readFrames(stream: DataInputStream): PoseFrames {
		val poseFrames = PoseFrames()
		val trackers = mutableMapOf<Int, TrackerFrames>()

		while (true) {
			try {
				readFrame(stream, poseFrames, trackers)
			} catch (_: EOFException) {
				// Reached end of stream, stop reading and return the recording
				// LogManager.debug("[PfsIO] Reached end of PFS stream.", e)
				break
			}
		}

		poseFrames.frameHolders.addAll(trackers.values)
		return poseFrames
	}

	fun tryReadFrames(stream: DataInputStream): PoseFrames? = try {
		readFrames(stream)
	} catch (e: Exception) {
		LogManager.severe("[PfsIO] Error reading frames from stream.", e)
		null
	}

	fun readFromFile(file: File): PoseFrames =
		DataInputStream(BufferedInputStream(FileInputStream(file))).use { readFrames(it) }

	fun tryReadFromFile(file: File): PoseFrames? = try {
		readFromFile(file)
	} catch (e: Exception) {
		LogManager.severe("[PfsIO] Error reading frames from file.", e)
		null
	}
}
