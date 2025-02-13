package dev.slimevr.autobone

import dev.slimevr.SLIMEVR_IDENTIFIER
import dev.slimevr.VRServer
import dev.slimevr.autobone.errors.*
import dev.slimevr.config.AutoBoneConfig
import dev.slimevr.config.SkeletonConfig
import dev.slimevr.poseframeformat.PfrIO
import dev.slimevr.poseframeformat.PfsIO
import dev.slimevr.poseframeformat.PoseFrames
import dev.slimevr.tracking.processor.BoneType
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

class AutoBone(private val server: VRServer) {
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

	val globalConfig: AutoBoneConfig = server.configManager.vrConfig.autoBone
	val globalSkeletonConfig: SkeletonConfig = server.configManager.vrConfig.skeleton

	init {
		loadConfigValues()
	}

	private fun loadConfigValues() {
		// Remove all previous values
		offsets.clear()

		// Get current or default skeleton configs
		val skeleton = server.humanPoseManager
		// Still compensate for a null skeleton, as it may not be initialized yet
		val getOffset: Function<SkeletonConfigOffsets, Float> =
			if (skeleton != null) {
				Function { key: SkeletonConfigOffsets -> skeleton.getOffset(key) }
			} else {
				val defaultConfig = SkeletonConfigManager(false)
				Function { config: SkeletonConfigOffsets ->
					defaultConfig.getOffset(config)
				}
			}
		for (bone in adjustOffsets) {
			val offset = getOffset.apply(bone)
			if (offset > 0f) {
				offsets[bone] = offset
			}
		}
	}

	/**
	 * Computes the local tail position of the bone after rotation.
	 */
	fun getBoneLocalTail(
		skeleton: HumanPoseManager,
		boneType: BoneType,
	): Vector3 {
		val bone = skeleton.getBone(boneType)
		return bone.getTailPosition() - bone.getPosition()
	}

	/**
	 * Computes the direction of the bone tail's movement between skeletons 1 and 2.
	 */
	fun getBoneLocalTailDir(
		skeleton1: HumanPoseManager,
		skeleton2: HumanPoseManager,
		boneType: BoneType,
	): Vector3? {
		val boneOff = getBoneLocalTail(skeleton2, boneType) - getBoneLocalTail(skeleton1, boneType)
		val boneOffLen = boneOff.len()
		return if (boneOffLen > MIN_SLIDE_DIST) boneOff / boneOffLen else null
	}

	/**
	 * Predicts how much the provided config should be affecting the slide offsets
	 * of the left and right ankles.
	 */
	fun getSlideDot(
		skeleton1: HumanPoseManager,
		skeleton2: HumanPoseManager,
		config: SkeletonConfigOffsets,
		slideL: Vector3?,
		slideR: Vector3?,
	): Float {
		var slideDot = 0f
		// Used for right offset if not a symmetric bone
		var boneOffL: Vector3? = null

		if (slideL != null) {
			boneOffL = getBoneLocalTailDir(skeleton1, skeleton2, config.affectedOffsets[0])

			if (boneOffL != null) {
				slideDot += slideL.dot(boneOffL)
			}
		}

		if (slideR != null) {
			// IMPORTANT: This assumption for acquiring BoneType only works if
			// SkeletonConfigOffsets is set up to only affect one BoneType, make sure no
			// changes to SkeletonConfigOffsets goes against this assumption, please!
			val boneOffR = if (SYMM_CONFIGS.contains(config)) {
				getBoneLocalTailDir(skeleton1, skeleton2, config.affectedOffsets[1])
			} else if (slideL != null) {
				// Use cached offset if slideL was used
				boneOffL
			} else {
				// Compute offset if missing because of slideL
				getBoneLocalTailDir(skeleton1, skeleton2, config.affectedOffsets[0])
			}

			if (boneOffR != null) {
				slideDot += slideR.dot(boneOffR)
			}
		}

		return slideDot / 2f
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
		// Still compensate for a null skeleton, as it may not be initialized yet
		@Suppress("SENSELESS_COMPARISON")
		if (config.useSkeletonHeight && humanPoseManager != null) {
			// If there is a skeleton available, calculate the target height
			// from its configs
			targetHeight = humanPoseManager.userHeightFromConfig
			LogManager
				.warning(
					"[AutoBone] Target height loaded from skeleton (Make sure you reset before running!): $targetHeight",
				)
		} else {
			// Otherwise if there is no skeleton available, attempt to get the
			// max HMD height from the recording
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
		skeletonConfig: SkeletonConfig = globalSkeletonConfig,
		epochCallback: Consumer<Epoch>? = null,
	): AutoBoneResults {
		check(frames.frameHolders.isNotEmpty()) { "Recording has no trackers." }
		check(frames.maxFrameCount > 0) { "Recording has no frames." }

		// Load current values for adjustable configs
		loadConfigValues()

		// Set the target heights either from config or calculate them
		val targetHmdHeight = if (skeletonConfig.userHeight > MIN_HEIGHT) {
			skeletonConfig.userHeight
		} else {
			calcTargetHmdHeight(frames, config)
		}
		check(targetHmdHeight > MIN_HEIGHT) { "Configured height ($targetHmdHeight) is too small (<= $MIN_HEIGHT)." }

		// Set up the current state, making all required players and setting up the
		// skeletons appropriately
		val trainingStep = AutoBoneStep(
			config = config,
			targetHmdHeight = targetHmdHeight,
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

		val slideL = skeleton2.getComputedTracker(TrackerRole.LEFT_FOOT).position -
			skeleton1.getComputedTracker(TrackerRole.LEFT_FOOT).position
		val slideLLen = slideL.len()
		val slideLUnit: Vector3? = if (slideLLen > MIN_SLIDE_DIST) slideL / slideLLen else null

		val slideR = skeleton2.getComputedTracker(TrackerRole.RIGHT_FOOT).position -
			skeleton1.getComputedTracker(TrackerRole.RIGHT_FOOT).position
		val slideRLen = slideR.len()
		val slideRUnit: Vector3? = if (slideRLen > MIN_SLIDE_DIST) slideR / slideRLen else null

		val intermediateOffsets = EnumMap(offsets)
		for (entry in intermediateOffsets.entries) {
			// Skip adjustment if the epoch is before starting (for logging only) or
			// if there are no BoneTypes for this value
			if (trainingStep.curEpoch < 0 || entry.key.affectedOffsets.isEmpty()) {
				break
			}
			val originalLength = entry.value

			// Calculate the total effect of the bone based on change in rotation
			val slideDot = getSlideDot(
				skeleton1,
				skeleton2,
				entry.key,
				slideLUnit,
				slideRUnit,
			)
			val dotLength = originalLength * slideDot

			// Scale by the total effect of the bone
			val curAdjustVal = adjustVal * -dotLength
			if (curAdjustVal == 0f) {
				continue
			}

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
			if (PfsIO.tryWriteToFile(recordingFile, frames)) {
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
			recordingFile = File(saveDir, "ABRecording${recordingIndex++}.pfs")
		} while (recordingFile.exists())
		saveRecording(frames, recordingFile)
	}

	fun loadRecordings(): FastList<Pair<String, PoseFrames>> {
		val recordings = FastList<Pair<String, PoseFrames>>()

		loadDir.listFiles()?.forEach { file ->
			if (!file.isFile) return@forEach

			val frames = if (file.name.endsWith(".pfs", ignoreCase = true)) {
				LogManager.info("[AutoBone] Loading PFS recording from \"${file.path}\"...")
				PfsIO.tryReadFromFile(file)
			} else if (file.name.endsWith(".pfr", ignoreCase = true)) {
				LogManager.info("[AutoBone] Loading PFR recording from \"${file.path}\"...")
				PfrIO.tryReadFromFile(file)
			} else {
				return@forEach
			}

			if (frames == null) {
				LogManager.severe("[AutoBone] Failed to load recording from \"${file.path}\".")
			} else {
				recordings.add(Pair.of(file.name, frames))
				LogManager.info("[AutoBone] Loaded recording from \"${file.path}\".")
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
		const val MIN_SLIDE_DIST = 0.002f
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

		private val SYMM_CONFIGS = arrayOf(
			SkeletonConfigOffsets.HIPS_WIDTH,
			SkeletonConfigOffsets.SHOULDERS_WIDTH,
			SkeletonConfigOffsets.SHOULDERS_DISTANCE,
			SkeletonConfigOffsets.UPPER_ARM,
			SkeletonConfigOffsets.LOWER_ARM,
			SkeletonConfigOffsets.UPPER_LEG,
			SkeletonConfigOffsets.LOWER_LEG,
			SkeletonConfigOffsets.FOOT_LENGTH,
		)
	}
}
