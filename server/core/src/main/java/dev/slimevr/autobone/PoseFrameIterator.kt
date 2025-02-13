package dev.slimevr.autobone

import kotlin.random.Random

object PoseFrameIterator {
	fun <T> iterateFrames(
		step: PoseFrameStep<T>,
	) {
		check(step.frames.frameHolders.isNotEmpty()) { "Recording has no trackers." }
		check(step.maxFrameCount > 0) { "Recording has no frames." }

		// Epoch loop, each epoch is one full iteration over the full dataset
		for (epoch in (if (step.config.calcInitError) -1 else 0) until step.config.numEpochs) {
			// Set the current epoch to process
			step.epoch = epoch
			// Process the epoch
			epoch(step)
		}
	}

	private fun randomIndices(count: Int, random: Random): IntArray {
		val randIndices = IntArray(count)

		var zeroPos = -1
		for (i in 0 until count) {
			var index = random.nextInt(count)
			if (i > 0) {
				while (index == zeroPos || randIndices[index] > 0) {
					index = random.nextInt(count)
				}
			} else {
				zeroPos = index
			}
			randIndices[index] = i
		}

		return randIndices
	}

	private fun <T> epoch(step: PoseFrameStep<T>) {
		val config = step.config
		val frameCount = step.maxFrameCount

		// Perform any setup that needs to be done before the current epoch
		step.preEpoch?.accept(step)

		val randIndices = if (config.randomizeFrameOrder) {
			randomIndices(step.maxFrameCount, step.random)
		} else {
			null
		}

		// Iterate over the frames using a cursor and an offset for comparing
		// frames a certain number of frames apart
		var cursorOffset = config.minDataDistance
		while (cursorOffset <= config.maxDataDistance &&
			cursorOffset < frameCount
		) {
			var frameCursor = 0
			while (frameCursor < frameCount - cursorOffset) {
				val frameCursor2 = frameCursor + cursorOffset

				// Then set the frame cursors and apply them to both skeletons
				if (config.randomizeFrameOrder && randIndices != null) {
					step
						.setCursors(
							randIndices[frameCursor],
							randIndices[frameCursor2],
							updatePlayerCursors = true,
						)
				} else {
					step.setCursors(
						frameCursor,
						frameCursor2,
						updatePlayerCursors = true,
					)
				}

				// Process the iteration
				step.onStep.accept(step)

				// Move on to the next iteration
				frameCursor += config.cursorIncrement
			}
			cursorOffset++
		}

		step.postEpoch?.accept(step)
	}
}
