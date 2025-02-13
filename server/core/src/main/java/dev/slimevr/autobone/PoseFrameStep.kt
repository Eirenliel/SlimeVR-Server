package dev.slimevr.autobone

import dev.slimevr.config.AutoBoneConfig
import dev.slimevr.config.ConfigManager
import dev.slimevr.poseframeformat.PoseFrames
import dev.slimevr.poseframeformat.player.TrackerFramesPlayer
import dev.slimevr.tracking.processor.HumanPoseManager
import java.util.function.Consumer
import kotlin.random.Random

class PoseFrameStep<T>(
	val config: AutoBoneConfig,
	/** The config to initialize skeletons. */
	serverConfig: ConfigManager? = null,
	val frames: PoseFrames,
	/** The consumer run before each epoch. */
	val preEpoch: Consumer<PoseFrameStep<T>>? = null,
	/** The consumer run for each step. */
	val onStep: Consumer<PoseFrameStep<T>>,
	/** The consumer run after each epoch. */
	val postEpoch: Consumer<PoseFrameStep<T>>? = null,
	/** The current epoch. */
	var epoch: Int = 0,
	/** The current frame cursor position in [frames] for skeleton1. */
	var cursor1: Int = 0,
	/** The current frame cursor position in [frames] for skeleton2. */
	var cursor2: Int = 0,
	randomSeed: Long = 0,
	val data: T,
) {
	var maxFrameCount = frames.maxFrameCount

	val framePlayer1 = TrackerFramesPlayer(frames)
	val framePlayer2 = TrackerFramesPlayer(frames)

	val trackers1 = framePlayer1.trackers.toList()
	val trackers2 = framePlayer2.trackers.toList()

	val skeleton1 = HumanPoseManager(trackers1)
	val skeleton2 = HumanPoseManager(trackers2)

	val random = Random(randomSeed)

	init {
		// Load server configs into the skeleton
		if (serverConfig != null) {
			skeleton1.loadFromConfig(serverConfig)
			skeleton2.loadFromConfig(serverConfig)
		}
		// Disable leg tweaks and IK solver, these will mess with the resulting positions
		skeleton1.setLegTweaksEnabled(false)
		skeleton2.setLegTweaksEnabled(false)
	}

	fun setCursors(cursor1: Int, cursor2: Int, updatePlayerCursors: Boolean) {
		this.cursor1 = cursor1
		this.cursor2 = cursor2

		if (updatePlayerCursors) {
			updatePlayerCursors()
		}
	}

	fun updatePlayerCursors() {
		framePlayer1.setCursors(cursor1)
		framePlayer2.setCursors(cursor2)
		skeleton1.update()
		skeleton2.update()
	}
}
