package dev.slimevr.poseframeformat

import dev.slimevr.VRServer
import dev.slimevr.poseframeformat.trackerdata.TrackerFrames
import dev.slimevr.tracking.trackers.Tracker
import dev.slimevr.util.ann.VRServerThread
import io.eiren.util.collections.FastList
import io.eiren.util.logging.LogManager
import org.apache.commons.lang3.tuple.Pair
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.function.Consumer

class PoseRecorder(private val server: VRServer) {
	inner class RecordingProgress(val frame: Int, val totalFrames: Int)

	private var poseFrame: PoseFrames? = null
	private var numFrames = -1
	private var frameCursor = 0
	private var frameRecordingInterval = 60L
	private var nextFrameTimeMs = -1L
	private var currentRecording: CompletableFuture<PoseFrames>? = null
	private var currentFrameCallback: Consumer<RecordingProgress>? = null
	var trackers = FastList<Pair<Tracker, TrackerFrames>>()

	init {
		server.addOnTick { onTick() }
	}

	@VRServerThread
	fun onTick() {
		if (numFrames <= 0) {
			return
		}
		val poseFrame = poseFrame
		val trackers: List<Pair<Tracker, TrackerFrames>> = trackers
		if (poseFrame == null) {
			return
		}
		if (frameCursor >= numFrames) {
			// If done and hasn't yet, send finished recording
			stopFrameRecording()
			return
		}
		val curTime = System.currentTimeMillis()
		if (curTime < nextFrameTimeMs) {
			return
		}
		nextFrameTimeMs += frameRecordingInterval

		// To prevent duplicate frames, make sure the frame time is always in
		// the future
		if (nextFrameTimeMs <= curTime) {
			nextFrameTimeMs = curTime + frameRecordingInterval
		}

		// Make sure it's synchronized since this is the server thread
		// interacting with
		// an unknown outside thread controlling this class
		synchronized(this) {
			// A stopped recording will be accounted for by an empty "trackers"
			// list
			val cursor = frameCursor++
			for (tracker in trackers) {
				// Add a frame for each tracker
				tracker.right.addFrameFromTracker(cursor, tracker.left)
			}

			currentFrameCallback?.accept(RecordingProgress(frameCursor, numFrames))

			// If done, send finished recording
			if (frameCursor >= numFrames) {
				stopFrameRecording()
			}
		}
	}

	@Synchronized
	fun startFrameRecording(
		numFrames: Int,
		intervalMs: Long,
		trackers: List<Tracker?> = server.allTrackers,
		frameCallback: Consumer<RecordingProgress>? = null,
	): Future<PoseFrames> {
		require(numFrames >= 1) { "numFrames must at least have a value of 1." }
		require(intervalMs >= 1) { "intervalMs must at least have a value of 1." }
		require(trackers.isNotEmpty()) { "trackers must have at least one entry." }
		cancelFrameRecording()
		val poseFrame = PoseFrames(trackers.size)
		poseFrame.frameInterval = intervalMs / 1000f

		// Update tracker list
		this.trackers.ensureCapacity(trackers.size)
		for (tracker in trackers) {
			// Ignore null and internal trackers
			if (tracker == null || tracker.isInternal) {
				continue
			}

			// Create a tracker recording
			val trackerFrames = TrackerFrames(tracker, numFrames)
			poseFrame.frameHolders.add(trackerFrames)

			// Pair tracker with recording
			this.trackers.add(Pair.of(tracker, trackerFrames))
		}
		require(this.trackers.isNotEmpty()) { "trackers must have at least one valid tracker." }

		this.poseFrame = poseFrame
		frameCursor = 0
		this.numFrames = numFrames
		frameRecordingInterval = intervalMs
		nextFrameTimeMs = -1L

		LogManager
			.info(
				"[PoseRecorder] Recording $numFrames samples at a $intervalMs ms frame interval",
			)

		currentFrameCallback = frameCallback
		val internalCurrentRecording = CompletableFuture<PoseFrames>()
		currentRecording = internalCurrentRecording
		return internalCurrentRecording
	}

	@Synchronized
	private fun internalStopFrameRecording(cancel: Boolean) {
		val currentRecording = currentRecording
		if (currentRecording != null && !currentRecording.isDone) {
			val currentFrames = poseFrame
			if (cancel || currentFrames == null) {
				// If it's supposed to be cancelled or there's actually no recording,
				// then cancel the recording and return nothing
				currentRecording.cancel(true)
			} else {
				// Stop the recording, returning the frames recorded
				currentRecording.complete(currentFrames)
			}
		}
		numFrames = -1
		frameCursor = 0
		trackers.clear()
		poseFrame = null
	}

	@Synchronized
	fun stopFrameRecording() {
		internalStopFrameRecording(false)
	}

	@Synchronized
	fun cancelFrameRecording() {
		internalStopFrameRecording(true)
	}

	@get:Synchronized
	val isReadyToRecord: Boolean
		get() = server.trackersCount > 0

	@get:Synchronized
	val isRecording: Boolean
		get() = numFrames > frameCursor

	@Synchronized
	fun hasRecording(): Boolean = currentRecording != null

	@get:Synchronized
	val framesAsync: Future<PoseFrames>?
		get() = currentRecording

	@get:Throws(ExecutionException::class, InterruptedException::class)
	@get:Synchronized
	val frames: PoseFrames?
		get() {
			return currentRecording?.get()
		}
}
