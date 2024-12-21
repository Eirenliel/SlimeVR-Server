package dev.slimevr.autobone

import dev.slimevr.SLIMEVR_IDENTIFIER
import dev.slimevr.VRServer
import dev.slimevr.autobone.errors.*
import dev.slimevr.config.AutoBoneConfig
import dev.slimevr.poseframeformat.PoseFrameIO
import dev.slimevr.poseframeformat.PoseFrames
import dev.slimevr.tracking.processor.HumanPoseManager
import dev.slimevr.tracking.processor.config.SkeletonConfigManager
import dev.slimevr.tracking.processor.config.SkeletonConfigOffsets
import dev.slimevr.tracking.trackers.TrackerRole
import io.eiren.util.OperatingSystem
import io.eiren.util.StringUtils
import io.eiren.util.collections.FastList
import io.eiren.util.logging.LogManager
import io.github.axisangles.ktmath.Vector3
import org.apache.commons.lang3.tuple.Pair
import java.io.File
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import kotlin.math.*

class AutoBone(server: VRServer) {
	// This is filled by loadConfigValues()
	val offsets = EnumMap<SkeletonConfigOffsets, Float>(
		SkeletonConfigOffsets::class.java,
	)
	val adjustOffsets = FastList(
		arrayOf(
			SkeletonConfigOffsets.HEAD,
			SkeletonConfigOffsets.NECK,
			SkeletonConfigOffsets.UPPER_CHEST,
			SkeletonConfigOffsets.CHEST,
			SkeletonConfigOffsets.WAIST,
			SkeletonConfigOffsets.HIP,
			// HIPS_WIDTH now works when using body proportion error! It's not the
			// best still, but it is somewhat functional
			SkeletonConfigOffsets.HIPS_WIDTH,
			SkeletonConfigOffsets.UPPER_LEG,
			SkeletonConfigOffsets.LOWER_LEG,
		),
	)

	var estimatedHeight: Float = 1f

	// The total height of the normalized adjusted offsets
	var adjustedHeightNormalized: Float = 1f

	private val server: VRServer

	// #region Error functions
	var slideError = SlideError()
	var offsetSlideError = OffsetSlideError()
	var footHeightOffsetError = FootHeightOffsetError()
	var bodyProportionError = BodyProportionError()
	var heightError = HeightError()
	var positionError = PositionError()
	var positionOffsetError = PositionOffsetError()
	// #endregion

	private val rand = Random()

	val globalConfig: AutoBoneConfig

	init {
		globalConfig = server.configManager.vrConfig.autoBone
		this.server = server
		loadConfigValues()
	}

	private fun loadConfigValues() {
		// Remove all previous values
		offsets.clear()

		// Get current skeleton configs
		for (bone in adjustOffsets) {
			val offset = server.humanPoseManager.getOffset(bone)
			if (offset > 0f) {
				offsets[bone] = offset
			}
		}
	}

	fun getBoneDirection(
		skeleton: HumanPoseManager,
		configOffset: SkeletonConfigOffsets,
		rightSide: Boolean,
	): Vector3 {
		// IMPORTANT: This assumption for acquiring BoneType only works if
		// SkeletonConfigOffsets is set up to only affect one BoneType, make sure no
		// changes to SkeletonConfigOffsets goes against this assumption, please!
		val boneType = when (configOffset) {
			SkeletonConfigOffsets.HIPS_WIDTH, SkeletonConfigOffsets.SHOULDERS_WIDTH,
			SkeletonConfigOffsets.SHOULDERS_DISTANCE, SkeletonConfigOffsets.UPPER_ARM,
			SkeletonConfigOffsets.LOWER_ARM, SkeletonConfigOffsets.UPPER_LEG,
			SkeletonConfigOffsets.LOWER_LEG, SkeletonConfigOffsets.FOOT_LENGTH,
			->
				if (rightSide) configOffset.affectedOffsets[1] else configOffset.affectedOffsets[0]

			else -> configOffset.affectedOffsets[0]
		}
		return skeleton.getBone(boneType).getGlobalRotation().toRotationVector()
	}

	fun getDotProductDiff(
		skeleton1: HumanPoseManager,
		skeleton2: HumanPoseManager,
		configOffset: SkeletonConfigOffsets,
		rightSide: Boolean,
		offset: Vector3,
	): Float {
		val normalizedOffset = offset.unit()
		val dot1 = normalizedOffset.dot(getBoneDirection(skeleton1, configOffset, rightSide))
		val dot2 = normalizedOffset.dot(getBoneDirection(skeleton2, configOffset, rightSide))
		return dot2 - dot1
	}

	fun applyConfig(
		humanPoseManager: HumanPoseManager,
		offsets: Map<SkeletonConfigOffsets, Float> = this.offsets,
	) {
		for ((offset, value) in offsets) {
			humanPoseManager.setOffset(offset, value)
		}
	}

	@JvmOverloads
	fun applyAndSaveConfig(humanPoseManager: HumanPoseManager? = this.server.humanPoseManager): Boolean {
		if (humanPoseManager == null) return false
		applyConfig(humanPoseManager)
		humanPoseManager.saveConfig()
		server.configManager.saveConfig()
		LogManager.info("[AutoBone] Configured skeleton bone lengths")
		return true
	}

	fun calcTargetHmdHeight(
		frames: PoseFrames,
		config: AutoBoneConfig = globalConfig,
	): Float {
		val targetHeight: Float
		// Get the current skeleton from the server
		val humanPoseManager = server.humanPoseManager
		if (config.useSkeletonHeight) {
			// calculate the target height from the skeleton configs
			targetHeight = humanPoseManager.userHeightFromConfig
			LogManager
				.warning(
					"[AutoBone] Target height loaded from skeleton (Make sure you reset before running!): $targetHeight",
				)
		} else {
			// Otherwise, attempt to get the max HMD height from the recording
			val hmdHeight = frames.maxHmdHeight
			if (hmdHeight <= MIN_HEIGHT) {
				LogManager
					.warning(
						"[AutoBone] Max headset height detected (Value seems too low, did you not stand up straight while measuring?): $hmdHeight",
					)
			} else {
				LogManager.info("[AutoBone] Max headset height detected: $hmdHeight")
			}

			// Estimate target height from HMD height
			targetHeight = hmdHeight
		}
		return targetHeight
	}

	private fun updateRecordingScale(trainingStep: AutoBoneStep, scale: Float) {
		trainingStep.framePlayer1.setScales(scale)
		trainingStep.framePlayer2.setScales(scale)
		trainingStep.skeleton1.update()
		trainingStep.skeleton2.update()
	}

	@Throws(AutoBoneException::class)
	fun processFrames(
		frames: PoseFrames,
		config: AutoBoneConfig = globalConfig,
		epochCallback: Consumer<Epoch>? = null,
	): AutoBoneResults {
		check(frames.frameHolders.isNotEmpty()) { "Recording has no trackers." }
		check(frames.maxFrameCount > 0) { "Recording has no frames." }

		// Load current values for adjustable configs
		loadConfigValues()

		// Set the target heights either from config or calculate them
		val targetHmdHeight = if (config.targetHmdHeight > 0f) {
			config.targetHmdHeight
		} else {
			calcTargetHmdHeight(frames, config)
		}
		val targetFullHeight = if (config.targetFullHeight > 0f) {
			config.targetFullHeight
		} else {
			targetHmdHeight / BodyProportionError.eyeHeightToHeightRatio
		}
		check(targetHmdHeight > MIN_HEIGHT) { "Configured height ($targetHmdHeight) is too small (<= $MIN_HEIGHT)." }

		// Set up the current state, making all required players and setting up the
		// skeletons appropriately
		val trainingStep = AutoBoneStep(
			config = config,
			targetHmdHeight = targetHmdHeight,
			targetFullHeight = targetFullHeight,
			frames = frames,
			epochCallback = epochCallback,
			serverConfig = server.configManager,
		)

		// Initialize the frame order randomizer with a repeatable seed
		rand.setSeed(config.randSeed)

		// Normalize the skeletons and get the normalized height for adjusted offsets
		scaleSkeleton(trainingStep.skeleton1)
		scaleSkeleton(trainingStep.skeleton2)
		adjustedHeightNormalized = sumAdjustedHeightOffsets(trainingStep.skeleton1)

		// Normalize offsets based on the initial normalized skeleton
		scaleOffsets()

		// Apply the initial normalized config values
		applyConfig(trainingStep.skeleton1)
		applyConfig(trainingStep.skeleton2)

		// Initialize normalization to the set target height (also updates skeleton)
		estimatedHeight = targetHmdHeight
		updateRecordingScale(trainingStep, 1f / targetHmdHeight)

		if (config.useFrameFiltering) {
			// Calculate the initial frame errors and recording stats
			val frameErrors = FloatArray(frames.maxFrameCount)
			val frameStats = StatsCalculator()
			val recordingStats = StatsCalculator()
			for (i in 0 until frames.maxFrameCount) {
				frameStats.reset()
				for (j in 0 until frames.maxFrameCount) {
					if (i == j) continue

					trainingStep.setCursors(
						i,
						j,
						updatePlayerCursors = true,
					)

					frameStats.addValue(getErrorDeriv(trainingStep))
				}
				frameErrors[i] = frameStats.mean
				recordingStats.addValue(frameStats.mean)
				// LogManager.info("[AutoBone] Frame: ${i + 1}, Mean error: ${frameStats.mean} (SD ${frameStats.standardDeviation})")
			}
			LogManager.info("[AutoBone] Full recording mean error: ${frameStats.mean} (SD ${frameStats.standardDeviation})")

			// Remove outlier frames
			val sdMult = 1.4f
			val mean = recordingStats.mean
			val sd = recordingStats.standardDeviation * sdMult
			for (i in frameErrors.size - 1 downTo 0) {
				val err = frameErrors[i]
				if (err < mean - sd || err > mean + sd) {
					for (frameHolder in frames.frameHolders) {
						frameHolder.frames.removeAt(i)
					}
				}
			}
			trainingStep.maxFrameCount = frames.maxFrameCount

			// Calculate and print the resulting recording stats
			recordingStats.reset()
			for (i in 0 until frames.maxFrameCount) {
				frameStats.reset()
				for (j in 0 until frames.maxFrameCount) {
					if (i == j) continue

					trainingStep.setCursors(
						i,
						j,
						updatePlayerCursors = true,
					)

					frameStats.addValue(getErrorDeriv(trainingStep))
				}
				recordingStats.addValue(frameStats.mean)
			}
			LogManager.info("[AutoBone] Full recording after mean error: ${frameStats.mean} (SD ${frameStats.standardDeviation})")
		}

		// Epoch loop, each epoch is one full iteration over the full dataset
		for (epoch in (if (config.calcInitError) -1 else 0) until config.numEpochs) {
			// Set the current epoch to process
			trainingStep.curEpoch = epoch

			// Process the epoch
			internalEpoch(trainingStep)
		}

		// Scale the normalized offsets to the estimated height for the final result
		for (entry in offsets.entries) {
			entry.setValue(entry.value * estimatedHeight)
		}

		LogManager
			.info(
				"[AutoBone] Target height: ${trainingStep.targetHmdHeight}, Final height: $estimatedHeight",
			)
		if (trainingStep.errorStats.mean > config.maxFinalError) {
			throw AutoBoneException("The final epoch error value (${trainingStep.errorStats.mean}) has exceeded the maximum allowed value (${config.maxFinalError}).")
		}

		return AutoBoneResults(
			estimatedHeight,
			trainingStep.targetHmdHeight,
			trainingStep.errorStats,
			offsets,
		)
	}

	private fun internalEpoch(trainingStep: AutoBoneStep) {
		// Pull frequently used variables out of trainingStep to reduce call length
		val config = trainingStep.config
		val frameCount = trainingStep.maxFrameCount
		val errorStats = trainingStep.errorStats
		val epoch = trainingStep.curEpoch

		// Set the current adjust rate based on the current epoch
		trainingStep.curAdjustRate = decayFunc(config.initialAdjustRate, config.adjustRateDecay, epoch)

		var randomFrameIndices: IntArray? = null
		if (config.randomizeFrameOrder) {
			randomFrameIndices = IntArray(frameCount)
			var zeroPos = -1
			for (i in 0 until frameCount) {
				var index = rand.nextInt(frameCount)
				if (i > 0) {
					while (index == zeroPos || randomFrameIndices[index] > 0) {
						index = rand.nextInt(frameCount)
					}
				} else {
					zeroPos = index
				}
				randomFrameIndices[index] = i
			}
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
				if (config.randomizeFrameOrder && randomFrameIndices != null) {
					trainingStep
						.setCursors(
							randomFrameIndices[frameCursor],
							randomFrameIndices[frameCursor2],
							updatePlayerCursors = true,
						)
				} else {
					trainingStep.setCursors(
						frameCursor,
						frameCursor2,
						updatePlayerCursors = true,
					)
				}

				// Process the iteration
				internalIter(trainingStep)

				// Move on to the next iteration
				frameCursor += config.cursorIncrement
			}
			cursorOffset++
		}

		// Calculate average error over the epoch
		if (epoch <= 0 || epoch >= config.numEpochs - 1 || (epoch + 1) % config.printEveryNumEpochs == 0) {
			LogManager
				.info(
					"[AutoBone] Epoch: ${epoch + 1}, Mean error: ${errorStats.mean} (SD ${errorStats.standardDeviation}), Adjust rate: ${trainingStep.curAdjustRate}",
				)
			LogManager
				.info(
					"[AutoBone] Target height: ${trainingStep.targetHmdHeight}, Estimated height: $estimatedHeight",
				)
		}

		if (trainingStep.epochCallback != null) {
			// Scale the normalized offsets to the estimated height for the callback
			val scaledOffsets = EnumMap(offsets)
			for (entry in scaledOffsets.entries) {
				entry.setValue(entry.value * estimatedHeight)
			}
			trainingStep.epochCallback.accept(Epoch(epoch + 1, config.numEpochs, errorStats, scaledOffsets))
		}
	}

	private fun internalIter(trainingStep: AutoBoneStep) {
		// Pull frequently used variables out of trainingStep to reduce call length
		val skeleton1 = trainingStep.skeleton1
		val skeleton2 = trainingStep.skeleton2

		// Scaling each step used to mean enforcing the target height, so keep that
		// behaviour to retain predictability
		if (!trainingStep.config.scaleEachStep) {
			// Try to estimate a new height by calculating the height with the lowest
			// error between adding or subtracting from the height
			val maxHeight = trainingStep.targetHmdHeight + 0.2f
			val minHeight = trainingStep.targetHmdHeight - 0.2f

			trainingStep.currentHmdHeight = estimatedHeight
			val heightErrorDeriv = getErrorDeriv(trainingStep)
			val heightAdjust = errorFunc(heightErrorDeriv) * trainingStep.curAdjustRate

			val negHeight = (estimatedHeight - heightAdjust).coerceIn(minHeight, maxHeight)
			updateRecordingScale(trainingStep, 1f / negHeight)
			trainingStep.currentHmdHeight = negHeight
			val negHeightErrorDeriv = getErrorDeriv(trainingStep)

			val posHeight = (estimatedHeight + heightAdjust).coerceIn(minHeight, maxHeight)
			updateRecordingScale(trainingStep, 1f / posHeight)
			trainingStep.currentHmdHeight = posHeight
			val posHeightErrorDeriv = getErrorDeriv(trainingStep)

			if (negHeightErrorDeriv < heightErrorDeriv && negHeightErrorDeriv < posHeightErrorDeriv) {
				estimatedHeight = negHeight
				// Apply the negative height scale
				updateRecordingScale(trainingStep, 1f / negHeight)
			} else if (posHeightErrorDeriv < heightErrorDeriv) {
				estimatedHeight = posHeight
				// The last estimated height set was the positive adjustment, so no need to apply it again
			} else {
				// Reset to the initial scale
				updateRecordingScale(trainingStep, 1f / estimatedHeight)
			}
		}

		// Update the heights used for error calculations
		trainingStep.currentHmdHeight = estimatedHeight

		val errorDeriv = getErrorDeriv(trainingStep)
		val error = errorFunc(errorDeriv)

		// In case of fire
		if (java.lang.Float.isNaN(error) || java.lang.Float.isInfinite(error)) {
			// Extinguish
			LogManager
				.warning(
					"[AutoBone] Error value is invalid, resetting variables to recover",
				)
			// Reset adjustable config values
			loadConfigValues()

			// Reset error sum values
			trainingStep.errorStats.reset()

			// Continue on new data
			return
		}

		// Store the error count for logging purposes
		trainingStep.errorStats.addValue(errorDeriv)
		val adjustVal = error * trainingStep.curAdjustRate

		// If there is no adjustment whatsoever, skip this
		if (adjustVal == 0f) {
			return
		}

		val slideLeft = skeleton2
			.getComputedTracker(TrackerRole.LEFT_FOOT).position -
			skeleton1.getComputedTracker(TrackerRole.LEFT_FOOT).position

		val slideRight = skeleton2
			.getComputedTracker(TrackerRole.RIGHT_FOOT).position -
			skeleton1.getComputedTracker(TrackerRole.RIGHT_FOOT).position

		val intermediateOffsets = EnumMap(offsets)
		for (entry in intermediateOffsets.entries) {
			// Skip adjustment if the epoch is before starting (for logging only) or
			// if there are no BoneTypes for this value
			if (trainingStep.curEpoch < 0 || entry.key.affectedOffsets.isEmpty()) {
				break
			}
			val originalLength = entry.value

			val leftDotProduct = getDotProductDiff(
				skeleton1,
				skeleton2,
				entry.key,
				false,
				slideLeft,
			)
			val rightDotProduct = getDotProductDiff(
				skeleton1,
				skeleton2,
				entry.key,
				true,
				slideRight,
			)

			// Calculate the total effect of the bone based on change in rotation
			val dotLength = originalLength * ((leftDotProduct + rightDotProduct) / 2f)

			// Scale by the total effect of the bone
			val curAdjustVal = adjustVal * -dotLength
			val newLength = originalLength + curAdjustVal

			// No small or negative numbers!!! Bad algorithm!
			if (newLength < 0.01f) {
				continue
			}

			// Apply new offset length
			skeleton1.setOffset(entry.key, newLength)
			skeleton2.setOffset(entry.key, newLength)
			scaleSkeleton(skeleton1, onlyAdjustedHeight = true)
			scaleSkeleton(skeleton2, onlyAdjustedHeight = true)

			// Update the skeleton poses for the new offset length
			skeleton1.update()
			skeleton2.update()

			val newErrorDeriv = getErrorDeriv(trainingStep)
			if (newErrorDeriv < errorDeriv) {
				// Apply the adjusted length to the current adjusted offsets
				entry.setValue(newLength)
			}

			// Reset the skeleton values to minimize bias in other variables, it's applied later
			applyConfig(skeleton1)
			applyConfig(skeleton2)
		}

		// Update the offsets from the adjusted ones
		offsets.putAll(intermediateOffsets)

		// Normalize the scale, it will be upscaled to the target height later
		// We only need to scale height offsets, as other offsets are not affected by height
		scaleOffsets(onlyHeightOffsets = true)

		// Apply the normalized offsets to the skeleton
		applyConfig(skeleton1)
		applyConfig(skeleton2)
	}

	/**
	 * Sums only the adjusted height offsets of the provided HumanPoseManager
	 */
	private fun sumAdjustedHeightOffsets(humanPoseManager: HumanPoseManager): Float {
		var sum = 0f
		SkeletonConfigManager.HEIGHT_OFFSETS.forEach {
			if (!adjustOffsets.contains(it)) return@forEach
			sum += humanPoseManager.getOffset(it)
		}
		return sum
	}

	/**
	 * Sums only the height offsets of the provided offset map
	 */
	private fun sumHeightOffsets(offsets: EnumMap<SkeletonConfigOffsets, Float> = this.offsets): Float {
		var sum = 0f
		SkeletonConfigManager.HEIGHT_OFFSETS.forEach {
			sum += offsets[it] ?: return@forEach
		}
		return sum
	}

	private fun scaleSkeleton(humanPoseManager: HumanPoseManager, targetHeight: Float = 1f, onlyAdjustedHeight: Boolean = false) {
		// Get the scale to apply for the appropriate offsets
		val scale = if (onlyAdjustedHeight) {
			// Only adjusted height offsets
			val adjHeight = sumAdjustedHeightOffsets(humanPoseManager)
			// Remove the constant from the target, leaving only the target for adjusted height offsets
			val adjTarget = targetHeight - (humanPoseManager.userHeightFromConfig - adjHeight)
			// Return only the scale for adjusted offsets
			adjTarget / adjHeight
		} else {
			targetHeight / humanPoseManager.userHeightFromConfig
		}

		val offsets = if (onlyAdjustedHeight) SkeletonConfigManager.HEIGHT_OFFSETS else SkeletonConfigOffsets.values
		for (offset in offsets) {
			if (onlyAdjustedHeight && !adjustOffsets.contains(offset)) continue
			humanPoseManager.setOffset(offset, humanPoseManager.getOffset(offset) * scale)
		}
	}

	private fun scaleOffsets(offsets: EnumMap<SkeletonConfigOffsets, Float> = this.offsets, targetHeight: Float = adjustedHeightNormalized, onlyHeightOffsets: Boolean = false) {
		// Get the scale to apply for the appropriate offsets
		val scale = targetHeight / sumHeightOffsets(offsets)

		for (entry in offsets.entries) {
			if (onlyHeightOffsets && !SkeletonConfigManager.HEIGHT_OFFSETS.contains(entry.key)) continue
			entry.setValue(entry.value * scale)
		}
	}

	@Throws(AutoBoneException::class)
	private fun getErrorDeriv(trainingStep: AutoBoneStep): Float {
		val config = trainingStep.config
		var sumError = 0f
		if (config.slideErrorFactor > 0f) {
			sumError += slideError.getStepError(trainingStep) * config.slideErrorFactor
		}
		if (config.offsetSlideErrorFactor > 0f) {
			sumError += (
				offsetSlideError.getStepError(trainingStep) *
					config.offsetSlideErrorFactor
				)
		}
		if (config.footHeightOffsetErrorFactor > 0f) {
			sumError += (
				footHeightOffsetError.getStepError(trainingStep) *
					config.footHeightOffsetErrorFactor
				)
		}
		if (config.bodyProportionErrorFactor > 0f) {
			sumError += (
				bodyProportionError.getStepError(trainingStep) *
					config.bodyProportionErrorFactor
				)
		}
		if (config.heightErrorFactor > 0f) {
			sumError += heightError.getStepError(trainingStep) * config.heightErrorFactor
		}
		if (config.positionErrorFactor > 0f) {
			sumError += (
				positionError.getStepError(trainingStep) *
					config.positionErrorFactor
				)
		}
		if (config.positionOffsetErrorFactor > 0f) {
			sumError += (
				positionOffsetError.getStepError(trainingStep) *
					config.positionOffsetErrorFactor
				)
		}
		return sumError
	}

	val lengthsString: String
		get() {
			val configInfo = StringBuilder()
			offsets.forEach { (key, value) ->
				if (configInfo.isNotEmpty()) {
					configInfo.append(", ")
				}
				configInfo
					.append(key.configKey)
					.append(": ")
					.append(StringUtils.prettyNumber(value * 100f, 2))
			}
			return configInfo.toString()
		}

	fun saveRecording(frames: PoseFrames, recordingFile: File) {
		if (saveDir.isDirectory || saveDir.mkdirs()) {
			LogManager
				.info("[AutoBone] Exporting frames to \"${recordingFile.path}\"...")
			if (PoseFrameIO.tryWriteToFile(recordingFile, frames)) {
				LogManager
					.info(
						"[AutoBone] Done exporting! Recording can be found at \"${recordingFile.path}\".",
					)
			} else {
				LogManager
					.severe(
						"[AutoBone] Failed to export the recording to \"${recordingFile.path}\".",
					)
			}
		} else {
			LogManager
				.severe(
					"[AutoBone] Failed to create the recording directory \"${saveDir.path}\".",
				)
		}
	}

	fun saveRecording(frames: PoseFrames, recordingFileName: String) {
		saveRecording(frames, File(saveDir, recordingFileName))
	}

	fun saveRecording(frames: PoseFrames) {
		var recordingFile: File
		var recordingIndex = 1
		do {
			recordingFile = File(saveDir, "ABRecording${recordingIndex++}.pfr")
		} while (recordingFile.exists())
		saveRecording(frames, recordingFile)
	}

	fun loadRecordings(): FastList<Pair<String, PoseFrames>> {
		val recordings = FastList<Pair<String, PoseFrames>>()
		if (!loadDir.isDirectory) return recordings
		val files = loadDir.listFiles() ?: return recordings
		for (file in files) {
			if (!file.isFile || !file.name.endsWith(".pfr", ignoreCase = true)) continue

			LogManager
				.info(
					"[AutoBone] Detected recording at \"${file.path}\", loading frames...",
				)
			val frames = PoseFrameIO.tryReadFromFile(file)
			if (frames == null) {
				LogManager.severe("Reading frames from \"${file.path}\" failed...")
			} else {
				recordings.add(Pair.of(file.name, frames))
			}
		}
		return recordings
	}

	inner class Epoch(
		val epoch: Int,
		val totalEpochs: Int,
		val epochError: StatsCalculator,
		val configValues: EnumMap<SkeletonConfigOffsets, Float>,
	) {
		override fun toString(): String = "Epoch: $epoch, Epoch error: $epochError"
	}

	inner class AutoBoneResults(
		val finalHeight: Float,
		val targetHeight: Float,
		val epochError: StatsCalculator,
		val configValues: EnumMap<SkeletonConfigOffsets, Float>,
	) {
		val heightDifference: Float
			get() = abs(targetHeight - finalHeight)
	}

	companion object {
		const val MIN_HEIGHT = 0.4f
		const val AUTOBONE_FOLDER = "AutoBone Recordings"
		const val LOADAUTOBONE_FOLDER = "Load AutoBone Recordings"

		// FIXME: Won't work on iOS and Android, maybe fix resolveConfigDirectory more than this
		val saveDir = File(
			OperatingSystem.resolveConfigDirectory(SLIMEVR_IDENTIFIER)?.resolve(
				AUTOBONE_FOLDER,
			)?.toString() ?: AUTOBONE_FOLDER,
		)
		val loadDir = File(
			OperatingSystem.resolveConfigDirectory(SLIMEVR_IDENTIFIER)?.resolve(
				LOADAUTOBONE_FOLDER,
			)?.toString() ?: LOADAUTOBONE_FOLDER,
		)

		// Mean square error function
		private fun errorFunc(errorDeriv: Float): Float = 0.5f * (errorDeriv * errorDeriv)

		private fun decayFunc(initialAdjustRate: Float, adjustRateDecay: Float, epoch: Int): Float = if (epoch >= 0) initialAdjustRate / (1 + (adjustRateDecay * epoch)) else 0.0f
	}
}
