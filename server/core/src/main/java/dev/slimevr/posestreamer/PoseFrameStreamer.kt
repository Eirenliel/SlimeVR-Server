package dev.slimevr.posestreamer

import dev.slimevr.poseframeformat.PfrIO.readFromFile
import dev.slimevr.poseframeformat.PoseFrames
import dev.slimevr.poseframeformat.player.TrackerFramesPlayer
import dev.slimevr.tracking.processor.HumanPoseManager
import java.io.File

class PoseFrameStreamer(poseFrames: PoseFrames) : PoseStreamer() {
	val trackerFramesPlayer: TrackerFramesPlayer = TrackerFramesPlayer(poseFrames)
	val humanPoseManager: HumanPoseManager = HumanPoseManager(trackerFramesPlayer.trackers.toList())

	constructor(path: String) : this(File(path))
	constructor(file: File) : this(readFromFile(file))

	init {
		skeleton = humanPoseManager.skeleton
	}

	@Synchronized
	fun streamAllFrames() {
		for (i in 0 until trackerFramesPlayer.maxFrameCount) {
			trackerFramesPlayer.setCursors(i)
			humanPoseManager.update()
			captureFrame()
		}
	}
}
