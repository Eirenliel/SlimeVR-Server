package dev.slimevr.poseframeformat

import dev.slimevr.poseframeformat.trackerdata.TrackerFrame
import dev.slimevr.poseframeformat.trackerdata.TrackerFrames
import dev.slimevr.tracking.trackers.TrackerPosition
import io.eiren.util.collections.FastList

class PoseFrames : Iterable<Array<TrackerFrame?>> {
	val frameHolders: FastList<TrackerFrames>

	/**
	 * Frame interval in seconds
	 */
	var frameInterval: Float = 0.02f

	/**
	 * Creates a [PoseFrames] object with the provided list of
	 * [TrackerFrames]s as the internal [TrackerFrames] list.
	 *
	 * @see [FastList]
	 * @see [TrackerFrames]
	 */
	constructor(frameHolders: FastList<TrackerFrames>) {
		this.frameHolders = frameHolders
	}

	/**
	 * Creates a [PoseFrames] object with the specified initial tracker
	 * capacity.
	 *
	 * @see [PoseFrames]
	 */
	constructor(initialCapacity: Int = 5) {
		frameHolders = FastList(initialCapacity)
	}

	/**
	 * @return The [TrackerFrames] associated with [position] at frame
	 * index [index].
	 */
	fun getTrackerForPosition(position: TrackerPosition, index: Int = 0): TrackerFrames? {
		for (tracker in frameHolders) {
			if (tracker.tryGetFrame(index)?.trackerPosition == position) return tracker
		}
		return null
	}

	// region Data Utilities
	/**
	 * @return The maximum Y value of the [TrackerFrames] associated with the
	 * [TrackerPosition.HEAD] tracker position on the first frame, otherwise `0f` if
	 * no [TrackerFrames] is associated with [TrackerPosition.HEAD] or if there are no
	 * valid positions.
	 * @see [getMaxHeight]
	 */
	val maxHmdHeight: Float
		get() {
			return getMaxHeight(
				getTrackerForPosition(TrackerPosition.HEAD)
					?: return 0f,
			)
		}

	/**
	 * @return The maximum Y value of the [trackerFrames], otherwise `0f` if
	 * there are no valid positions.
	 * @see [TrackerPosition]
	 */
	fun getMaxHeight(trackerFrames: TrackerFrames): Float {
		var maxHeight = 0f
		for (frame in trackerFrames.frames) {
			val framePosition = frame?.tryGetPosition() ?: continue

			if (framePosition.y > maxHeight) {
				maxHeight = framePosition.y
			}
		}

		return maxHeight
	}
	// endregion

	/**
	 * @return The maximum number of [TrackerFrame]s contained within each
	 * [TrackerFrames] in the internal [TrackerFrames] list.
	 * @see [TrackerFrames.frames]
	 * @see [List.size]
	 */
	val maxFrameCount: Int
		get() {
			return frameHolders.maxOfOrNull { tracker -> tracker.frames.size } ?: 0
		}

	/**
	 * Using the provided array buffer, get the [TrackerFrame]s contained
	 * within each [TrackerFrames] in the internal
	 * [TrackerFrames] list at the specified index.
	 *
	 * @return The number of frames written to the buffer.
	 * @see [TrackerFrames.tryGetFrame]
	 */
	fun getFrames(frameIndex: Int, buffer: Array<TrackerFrame?>): Int {
		var frameCount = 0
		for (tracker in frameHolders) {
			if (tracker == null) {
				continue
			}
			val frame = tracker.tryGetFrame(frameIndex) ?: continue
			buffer[frameCount++] = frame
		}
		return frameCount
	}

	/**
	 * Using the provided [List] buffer, get the [TrackerFrame]s
	 * contained within each [TrackerFrames] in the internal
	 * [TrackerFrames] list at the specified index.
	 *
	 * @return The number of frames written to the buffer.
	 * @see [TrackerFrames.tryGetFrame]
	 */
	fun getFrames(frameIndex: Int, buffer: MutableList<TrackerFrame?>): Int {
		var frameCount = 0
		for (tracker in frameHolders) {
			if (tracker == null) {
				continue
			}
			val frame = tracker.tryGetFrame(frameIndex) ?: continue
			buffer[frameCount++] = frame
		}
		return frameCount
	}

	/**
	 * @return The [TrackerFrame]s contained within each
	 * [TrackerFrames] in the internal [TrackerFrames] list at
	 * the specified index.
	 * @see [TrackerFrames.tryGetFrame]
	 */
	fun getFrames(frameIndex: Int): Array<TrackerFrame?> {
		val trackerFrames = arrayOfNulls<TrackerFrame>(frameHolders.size)
		getFrames(frameIndex, trackerFrames)
		return trackerFrames
	}

	override fun iterator(): Iterator<Array<TrackerFrame?>> = PoseFrameIterator(this)

	inner class PoseFrameIterator(private val poseFrame: PoseFrames) : Iterator<Array<TrackerFrame?>> {
		private val trackerFrameBuffer: Array<TrackerFrame?> = arrayOfNulls(poseFrame.frameHolders.size)
		private val maxCursor = poseFrame.maxFrameCount
		private var cursor = 0

		override fun hasNext(): Boolean = frameHolders.isNotEmpty() && cursor < maxCursor

		override fun next(): Array<TrackerFrame?> {
			if (!hasNext()) {
				throw NoSuchElementException()
			}
			poseFrame.getFrames(cursor++, trackerFrameBuffer)
			return trackerFrameBuffer
		}
	}
}
