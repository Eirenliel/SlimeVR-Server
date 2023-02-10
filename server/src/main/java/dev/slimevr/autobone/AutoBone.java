package dev.slimevr.autobone;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import dev.slimevr.VRServer;
import dev.slimevr.autobone.errors.*;
import dev.slimevr.config.AutoBoneConfig;
import dev.slimevr.poserecorder.PoseFrameIO;
import dev.slimevr.poserecorder.PoseFrameTracker;
import dev.slimevr.poserecorder.PoseFrames;
import dev.slimevr.tracking.processor.BoneType;
import dev.slimevr.tracking.processor.HumanPoseManager;
import dev.slimevr.tracking.processor.TransformNode;
import dev.slimevr.tracking.processor.config.SkeletonConfigManager;
import dev.slimevr.tracking.processor.config.SkeletonConfigOffsets;
import dev.slimevr.tracking.processor.skeleton.HumanSkeleton;
import dev.slimevr.tracking.trackers.TrackerRole;
import io.eiren.util.StringUtils;
import io.eiren.util.collections.FastList;
import io.eiren.util.logging.LogManager;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;


public class AutoBone {

	private static final File saveDir = new File("AutoBone Recordings");
	private static final File loadDir = new File("Load AutoBone Recordings");

	// This is filled by reloadConfigValues()
	public final EnumMap<BoneType, Float> offsets = new EnumMap<>(
		BoneType.class
	);

	public final FastList<BoneType> adjustOffsets = new FastList<>(
		new BoneType[] {
			BoneType.HEAD,
			BoneType.NECK,
			BoneType.CHEST,
			BoneType.WAIST,
			BoneType.HIP,

			// This now works when using body proportion error! It's not the
			// best still but it is somewhat functional
			BoneType.LEFT_HIP,

			BoneType.LEFT_UPPER_LEG,
			BoneType.LEFT_LOWER_LEG,
		}
	);

	public final FastList<BoneType> heightOffsets = new FastList<>(
		new BoneType[] {
			BoneType.NECK,
			BoneType.CHEST,
			BoneType.WAIST,
			BoneType.HIP,

			BoneType.LEFT_UPPER_LEG,
			BoneType.RIGHT_UPPER_LEG,
			BoneType.LEFT_LOWER_LEG,
			BoneType.RIGHT_LOWER_LEG,
		}
	);

	public final FastList<SkeletonConfigOffsets> legacyHeightConfigs = new FastList<>(
		new SkeletonConfigOffsets[] {
			SkeletonConfigOffsets.NECK,
			SkeletonConfigOffsets.CHEST,
			SkeletonConfigOffsets.WAIST,
			SkeletonConfigOffsets.HIP,
			SkeletonConfigOffsets.UPPER_LEG,
			SkeletonConfigOffsets.LOWER_LEG,
		}
	);

	public final EnumMap<SkeletonConfigOffsets, Float> legacyConfigs = new EnumMap<>(
		SkeletonConfigOffsets.class
	);

	protected final VRServer server;

	// #region Error functions
	public SlideError slideError = new SlideError();
	public OffsetSlideError offsetSlideError = new OffsetSlideError();
	public FootHeightOffsetError footHeightOffsetError = new FootHeightOffsetError();
	public BodyProportionError bodyProportionError = new BodyProportionError();
	public HeightError heightError = new HeightError();
	public PositionError positionError = new PositionError();
	public PositionOffsetError positionOffsetError = new PositionOffsetError();
	// #endregion

	private final Random rand = new Random();

	private final AutoBoneConfig config;

	public AutoBone(VRServer server) {
		this.config = server.getConfigManager().getVrConfig().getAutoBone();
		this.server = server;
		reloadConfigValues();
	}

	// Mean square error function
	protected static float errorFunc(float errorDeriv) {
		return 0.5f * (errorDeriv * errorDeriv);
	}

	public static File getLoadDir() {
		return loadDir;
	}

	public float computeBoneOffset(
		BoneType bone,
		Function<SkeletonConfigOffsets, Float> getOffset
	) {
		return switch (bone) {
			case HEAD -> getOffset.apply(SkeletonConfigOffsets.HEAD);
			case NECK -> getOffset.apply(SkeletonConfigOffsets.NECK);
			case CHEST -> getOffset.apply(SkeletonConfigOffsets.CHEST);
			case WAIST -> getOffset.apply(SkeletonConfigOffsets.WAIST);
			case HIP -> getOffset.apply(SkeletonConfigOffsets.HIP);
			case LEFT_HIP, RIGHT_HIP -> getOffset.apply(SkeletonConfigOffsets.HIPS_WIDTH)
				/ 2f;
			case LEFT_UPPER_LEG, RIGHT_UPPER_LEG -> getOffset
				.apply(SkeletonConfigOffsets.UPPER_LEG);
			case LEFT_LOWER_LEG, RIGHT_LOWER_LEG -> getOffset
				.apply(SkeletonConfigOffsets.LOWER_LEG);
			default -> -1f;
		};

	}

	public void reloadConfigValues() {
		reloadConfigValues(null);
	}

	public void reloadConfigValues(List<PoseFrameTracker> trackers) {
		// Remove all previous values
		offsets.clear();

		// Get current or default skeleton configs
		HumanPoseManager skeleton = getHumanPoseManager();
		Function<SkeletonConfigOffsets, Float> getOffset = skeleton != null
			? skeleton::getOffset
			: new SkeletonConfigManager(false)::getOffset;

		for (BoneType bone : adjustOffsets) {
			float offset = computeBoneOffset(bone, getOffset);
			if (offset > 0f) {
				offsets.put(bone, offset);
			}
		}
	}

	public Vector3f getBoneDirection(
		HumanPoseManager skeleton,
		BoneType node,
		boolean rightSide,
		Vector3f buffer
	) {
		if (buffer == null) {
			buffer = new Vector3f();
		}

		switch (node) {
			case LEFT_HIP, RIGHT_HIP -> node = rightSide ? BoneType.RIGHT_HIP : BoneType.LEFT_HIP;
			case LEFT_UPPER_LEG, RIGHT_UPPER_LEG -> node = rightSide
				? BoneType.RIGHT_UPPER_LEG
				: BoneType.LEFT_UPPER_LEG;
			case LEFT_LOWER_LEG, RIGHT_LOWER_LEG -> node = rightSide
				? BoneType.RIGHT_LOWER_LEG
				: BoneType.LEFT_LOWER_LEG;
		}

		TransformNode relevantTransform = skeleton.getTailNodeOfBone(node);
		return relevantTransform.worldTransform
			.getTranslation()
			.subtract(relevantTransform.getParent().worldTransform.getTranslation(), buffer)
			.normalizeLocal();
	}

	public float getDotProductDiff(
		HumanPoseManager skeleton1,
		HumanPoseManager skeleton2,
		BoneType node,
		boolean rightSide,
		Vector3f offset
	) {
		Vector3f normalizedOffset = offset.normalize();

		Vector3f boneRotation = new Vector3f();
		getBoneDirection(skeleton1, node, rightSide, boneRotation);
		float dot1 = normalizedOffset.dot(boneRotation);

		getBoneDirection(skeleton2, node, rightSide, boneRotation);
		float dot2 = normalizedOffset.dot(boneRotation);

		return dot2 - dot1;
	}

	/**
	 * A simple utility method to get the {@link HumanSkeleton} from the
	 * {@link VRServer}
	 *
	 * @return The {@link HumanSkeleton} associated with the {@link VRServer},
	 * or null if there is none available
	 * @see {@link VRServer}, {@link HumanSkeleton}
	 */
	private HumanPoseManager getHumanPoseManager() {
		return server != null ? server.humanPoseManager : null;
	}

	public void applyAndSaveConfig() {
		if (!applyAndSaveConfig(getHumanPoseManager())) {
			// Unable to apply to skeleton, save directly
			// saveConfigs();
		}
	}

	public boolean applyConfig(
		BiConsumer<SkeletonConfigOffsets, Float> configConsumer,
		Map<BoneType, Float> offsets
	) {
		if (configConsumer == null || offsets == null) {
			return false;
		}

		try {
			Float headOffset = offsets.get(BoneType.HEAD);
			if (headOffset != null) {
				configConsumer.accept(SkeletonConfigOffsets.HEAD, headOffset);
			}
			Float neckOffset = offsets.get(BoneType.NECK);
			if (neckOffset != null) {
				configConsumer.accept(SkeletonConfigOffsets.NECK, neckOffset);
			}

			Float chestOffset = offsets.get(BoneType.CHEST);
			Float waistOffset = offsets.get(BoneType.WAIST);
			Float hipOffset = offsets.get(BoneType.HIP);
			if (chestOffset != null) {
				configConsumer
					.accept(SkeletonConfigOffsets.CHEST, chestOffset);
			}
			if (waistOffset != null) {
				configConsumer.accept(SkeletonConfigOffsets.WAIST, waistOffset);
			}
			if (hipOffset != null) {
				configConsumer.accept(SkeletonConfigOffsets.HIP, hipOffset);
			}

			Float hipWidthOffset = offsets.get(BoneType.LEFT_HIP);
			if (hipWidthOffset == null) {
				hipWidthOffset = offsets.get(BoneType.RIGHT_HIP);
			}
			if (hipWidthOffset != null) {
				configConsumer
					.accept(SkeletonConfigOffsets.HIPS_WIDTH, hipWidthOffset * 2f);
			}

			Float upperLegOffset = offsets.get(BoneType.LEFT_UPPER_LEG);
			if (upperLegOffset == null) {
				upperLegOffset = offsets.get(BoneType.RIGHT_UPPER_LEG);
			}
			Float lowerLegOffset = offsets.get(BoneType.LEFT_LOWER_LEG);
			if (lowerLegOffset == null) {
				lowerLegOffset = offsets.get(BoneType.RIGHT_LOWER_LEG);
			}

			if (upperLegOffset != null) {
				configConsumer
					.accept(SkeletonConfigOffsets.UPPER_LEG, upperLegOffset);
			}
			if (lowerLegOffset != null) {
				configConsumer.accept(SkeletonConfigOffsets.LOWER_LEG, lowerLegOffset);
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean applyConfig(BiConsumer<SkeletonConfigOffsets, Float> configConsumer) {
		return applyConfig(configConsumer, offsets);
	}

	public boolean applyConfig(
		Map<SkeletonConfigOffsets, Float> skeletonConfig,
		Map<BoneType, Float> offsets
	) {
		if (skeletonConfig == null) {
			return false;
		}

		return applyConfig(skeletonConfig::put, offsets);
	}

	public boolean applyConfig(Map<SkeletonConfigOffsets, Float> skeletonConfig) {
		return applyConfig(skeletonConfig, offsets);
	}

	public boolean applyConfig(
		HumanPoseManager humanPoseManager,
		Map<BoneType, Float> offsets
	) {
		if (humanPoseManager == null) {
			return false;
		}

		return applyConfig(humanPoseManager::setOffset, offsets);
	}

	public boolean applyConfig(HumanPoseManager humanPoseManager) {
		return applyConfig(humanPoseManager, offsets);
	}

	public boolean applyAndSaveConfig(HumanPoseManager humanPoseManager) {
		if (humanPoseManager == null) {
			return false;
		}

		if (!applyConfig(humanPoseManager))
			return false;

		humanPoseManager.saveConfig();
		server.getConfigManager().saveConfig();

		LogManager.info("[AutoBone] Configured skeleton bone lengths");
		return true;
	}

	public Float getConfig(BoneType config) {
		return offsets.get(config);
	}

	public <T> float sumSelectConfigs(
		List<T> selection,
		Function<T, Float> configs
	) {
		float sum = 0f;

		for (T config : selection) {
			Float length = configs.apply(config);
			if (length != null) {
				sum += length;
			}
		}

		return sum;
	}

	public <T> float sumSelectConfigs(
		List<T> selection,
		Map<T, Float> configs
	) {
		return sumSelectConfigs(selection, configs::get);
	}

	public float sumSelectConfigs(
		List<SkeletonConfigOffsets> selection,
		HumanPoseManager humanPoseManager
	) {
		return sumSelectConfigs(selection, humanPoseManager::getOffset);
	}

	public float getLengthSum(Map<BoneType, Float> configs) {
		return getLengthSum(configs, null);
	}

	public float getLengthSum(
		Map<BoneType, Float> configs,
		Map<BoneType, Float> configsAlt
	) {
		float length = 0f;

		if (configsAlt != null) {
			for (Entry<BoneType, Float> config : configsAlt.entrySet()) {
				// If there isn't a duplicate config
				if (!configs.containsKey(config.getKey())) {
					length += config.getValue();
				}
			}
		}

		for (Float boneLength : configs.values()) {
			length += boneLength;
		}

		return length;
	}

	public float getTargetHeight(PoseFrames frames) {
		float targetHeight;
		// Get the current skeleton from the server
		HumanPoseManager humanPoseManager = getHumanPoseManager();
		if (humanPoseManager != null) {
			// If there is a skeleton available, calculate the target height
			// from its configs
			targetHeight = sumSelectConfigs(legacyHeightConfigs, humanPoseManager);
			LogManager
				.warning(
					"[AutoBone] Target height loaded from skeleton (Make sure you reset before running!): "
						+ targetHeight
				);
		} else {
			// Otherwise if there is no skeleton available, attempt to get the
			// max HMD height from the recording
			float hmdHeight = frames.getMaxHmdHeight();
			if (hmdHeight <= 0.50f) {
				LogManager
					.warning(
						"[AutoBone] Max headset height detected (Value seems too low, did you not stand up straight while measuring?): "
							+ hmdHeight
					);
			} else {
				LogManager.info("[AutoBone] Max headset height detected: " + hmdHeight);
			}

			// Estimate target height from HMD height
			targetHeight = hmdHeight;
		}

		return targetHeight;
	}

	public AutoBoneResults processFrames(PoseFrames frames, Consumer<Epoch> epochCallback)
		throws AutoBoneException {
		return processFrames(frames, -1f, epochCallback);
	}

	public AutoBoneResults processFrames(
		PoseFrames frames,
		float targetHeight,
		Consumer<Epoch> epochCallback
	) throws AutoBoneException {
		return processFrames(frames, true, targetHeight, epochCallback);
	}

	public AutoBoneResults processFrames(
		PoseFrames frames,
		boolean calcInitError,
		float targetHeight,
		Consumer<Epoch> epochCallback
	) throws AutoBoneException {
		final int frameCount = frames.getMaxFrameCount();

		final PoseFrames frames1 = new PoseFrames(frames);
		final PoseFrames frames2 = new PoseFrames(frames);

		List<PoseFrameTracker> trackers1 = frames1.getTrackers();
		List<PoseFrameTracker> trackers2 = frames2.getTrackers();

		// Reload configs and detect chest tracker from the first frame
		reloadConfigValues(trackers1);

		final HumanPoseManager skeleton1 = new HumanPoseManager(
			trackers1
		);
		final HumanPoseManager skeleton2 = new HumanPoseManager(
			trackers2
		);

		EnumMap<BoneType, Float> intermediateOffsets = new EnumMap<>(
			offsets
		);

		AutoBoneTrainingStep trainingStep = new AutoBoneTrainingStep(
			targetHeight,
			skeleton1,
			skeleton2,
			frames,
			intermediateOffsets
		);

		skeleton1.setLegTweaksEnabled(false);
		skeleton2.setLegTweaksEnabled(false);

		// If target height isn't specified, auto-detect
		if (targetHeight < 0f) {
			targetHeight = getTargetHeight(frames);
		}

		StatsCalculator errorStats = new StatsCalculator();

		// Epoch loop, each epoch is one full iteration over the full dataset
		for (int epoch = calcInitError ? -1 : 0; epoch < this.config.numEpochs; epoch++) {
			float adjustRate = epoch >= 0
				? (this.config.initialAdjustRate
					* FastMath.pow(this.config.adjustRateMultiplier, epoch))
				: 0f;

			int[] randomFrameIndices = null;
			if (config.randomizeFrameOrder) {
				randomFrameIndices = new int[frameCount];

				int zeroPos = -1;
				for (int i = 0; i < frameCount; i++) {
					int index = rand.nextInt(frameCount);

					if (i > 0) {
						while (index == zeroPos || randomFrameIndices[index] > 0) {
							index = rand.nextInt(frameCount);
						}
					} else {
						zeroPos = index;
					}

					randomFrameIndices[index] = i;
				}
			}

			// Iterate over the frames using a cursor and an offset for
			// comparing frames a
			// certain number of frames apart
			for (
				int cursorOffset = this.config.minDataDistance;
				cursorOffset <= this.config.maxDataDistance
					&& cursorOffset < frameCount;
				cursorOffset++
			) {
				for (
					int frameCursor = 0; frameCursor < frameCount - cursorOffset;
					frameCursor += config.cursorIncrement
				) {
					int frameCursor2 = frameCursor + cursorOffset;

					applyConfig(skeleton1);
					applyConfig(skeleton2);

					if (config.randomizeFrameOrder) {
						trainingStep
							.setCursors(
								randomFrameIndices[frameCursor],
								randomFrameIndices[frameCursor2]
							);
					} else {
						trainingStep.setCursors(frameCursor, frameCursor2);
					}

					frames1.setCursors(trainingStep.getCursor1());
					frames2.setCursors(trainingStep.getCursor2());

					skeleton1.update();
					skeleton2.update();

					float totalLength = getLengthSum(offsets);
					float curHeight = sumSelectConfigs(heightOffsets, offsets);
					trainingStep.setCurrentHeight(curHeight);

					float errorDeriv = getErrorDeriv(trainingStep);
					float error = errorFunc(errorDeriv);

					// In case of fire
					if (Float.isNaN(error) || Float.isInfinite(error)) {
						// Extinguish
						LogManager
							.warning(
								"[AutoBone] Error value is invalid, resetting variables to recover"
							);
						reloadConfigValues(trackers1);

						// Reset error sum values
						errorStats.reset();

						// Continue on new data
						continue;
					}

					// Store the error count for logging purposes
					errorStats.addValue(errorDeriv);

					float adjustVal = error * adjustRate;

					// If there is no adjustment whatsoever, skip this
					if (adjustVal == 0f) {
						continue;
					}

					Vector3f slideLeft = skeleton2
						.getComputedTracker(TrackerRole.LEFT_FOOT).position
							.subtract(
								skeleton1.getComputedTracker(TrackerRole.LEFT_FOOT).position
							);

					Vector3f slideRight = skeleton2
						.getComputedTracker(TrackerRole.RIGHT_FOOT).position
							.subtract(
								skeleton1
									.getComputedTracker(TrackerRole.RIGHT_FOOT).position
							);

					intermediateOffsets.putAll(offsets);
					for (Entry<BoneType, Float> entry : offsets.entrySet()) {
						// Skip adjustment if the epoch is before starting (for
						// logging only)
						if (epoch < 0) {
							break;
						}

						float originalLength = entry.getValue();
						boolean isHeightVar = heightOffsets.contains(entry.getKey());

						float leftDotProduct = getDotProductDiff(
							skeleton1,
							skeleton2,
							entry.getKey(),
							false,
							slideLeft
						);

						float rightDotProduct = getDotProductDiff(
							skeleton1,
							skeleton2,
							entry.getKey(),
							true,
							slideRight
						);

						float dotLength = originalLength
							* ((leftDotProduct + rightDotProduct) / 2f);

						// Scale by the ratio for smooth adjustment and more
						// stable results
						float curAdjustVal = (adjustVal * -dotLength) / totalLength;
						float newLength = originalLength + curAdjustVal;

						// No small or negative numbers!!! Bad algorithm!
						if (newLength < 0.01f) {
							continue;
						}

						// Apply new offset length
						intermediateOffsets.put(entry.getKey(), newLength);
						applyConfig(skeleton1, intermediateOffsets);
						applyConfig(skeleton2, intermediateOffsets);

						// Update the skeleton poses for the new offset length
						skeleton1.update();
						skeleton2.update();

						float newHeight = isHeightVar ? curHeight + curAdjustVal : curHeight;
						trainingStep.setCurrentHeight(newHeight);

						float newErrorDeriv = getErrorDeriv(trainingStep);

						if (newErrorDeriv < errorDeriv) {
							entry.setValue(newLength);
						}

						// Reset the length to minimize bias in other variables,
						// it's applied later
						intermediateOffsets.put(entry.getKey(), originalLength);
						applyConfig(skeleton1, intermediateOffsets);
						applyConfig(skeleton2, intermediateOffsets);
					}

					if (config.scaleEachStep) {
						float stepHeight = sumSelectConfigs(heightOffsets, offsets);

						if (stepHeight > 0f) {
							float stepHeightDiff = targetHeight - stepHeight;
							for (Entry<BoneType, Float> entry : offsets.entrySet()) {
								// Only height variables
								if (
									entry.getKey() == BoneType.NECK
										|| !heightOffsets.contains(entry.getKey())
								)
									continue;

								float length = entry.getValue();

								// Multiply the diff by the length to height
								// ratio
								float adjVal = stepHeightDiff * (length / stepHeight);

								// Scale the length to fit the target height
								entry.setValue(Math.max(length + (adjVal / 2f), 0.01f));
							}
						}
					}
				}
			}

			// Calculate average error over the epoch
			if (
				epoch <= 0
					|| epoch >= (config.numEpochs - 1)
					|| (epoch + 1) % config.printEveryNumEpochs == 0
			) {
				LogManager
					.info(
						"[AutoBone] Epoch "
							+ (epoch + 1)
							+ " average error: "
							+ errorStats.getMean()
							+ " (SD "
							+ errorStats.getStandardDeviation()
							+ ")"
					);
			}

			applyConfig(legacyConfigs);
			if (epochCallback != null) {
				epochCallback
					.accept(new Epoch(epoch + 1, this.config.numEpochs, errorStats, legacyConfigs));
			}
		}

		float finalHeight = sumSelectConfigs(heightOffsets, offsets);
		LogManager
			.info(
				"[AutoBone] Target height: "
					+ targetHeight
					+ " New height: "
					+ finalHeight
			);

		return new AutoBoneResults(finalHeight, targetHeight, errorStats, legacyConfigs);
	}

	protected float getErrorDeriv(AutoBoneTrainingStep trainingStep) throws AutoBoneException {
		float sumError = 0f;

		if (this.config.slideErrorFactor > 0f) {
			sumError += slideError.getStepError(trainingStep) * this.config.slideErrorFactor;
		}

		if (this.config.offsetSlideErrorFactor > 0f) {
			sumError += offsetSlideError.getStepError(trainingStep)
				* this.config.offsetSlideErrorFactor;
		}

		if (this.config.footHeightOffsetErrorFactor > 0f) {
			sumError += footHeightOffsetError.getStepError(trainingStep)
				* this.config.footHeightOffsetErrorFactor;
		}

		if (this.config.bodyProportionErrorFactor > 0f) {
			sumError += bodyProportionError.getStepError(trainingStep)
				* this.config.bodyProportionErrorFactor;
		}

		if (this.config.heightErrorFactor > 0f) {
			sumError += heightError.getStepError(trainingStep) * this.config.heightErrorFactor;
		}

		if (this.config.positionErrorFactor > 0f) {
			sumError += positionError.getStepError(trainingStep)
				* this.config.positionErrorFactor;
		}

		if (this.config.positionOffsetErrorFactor > 0f) {
			sumError += positionOffsetError.getStepError(trainingStep)
				* this.config.positionOffsetErrorFactor;
		}

		return sumError;
	}

	public String getLengthsString() {
		final StringBuilder configInfo = new StringBuilder();
		this.offsets.forEach((key, value) -> {
			if (configInfo.length() > 0) {
				configInfo.append(", ");
			}

			configInfo
				.append(key.toString())
				.append(": ")
				.append(StringUtils.prettyNumber(value * 100f, 2));
		});

		return configInfo.toString();
	}

	public void saveRecording(PoseFrames frames, File recordingFile) {
		if (saveDir.isDirectory() || saveDir.mkdirs()) {
			LogManager
				.info("[AutoBone] Exporting frames to \"" + recordingFile.getPath() + "\"...");
			if (PoseFrameIO.writeToFile(recordingFile, frames)) {
				LogManager
					.info(
						"[AutoBone] Done exporting! Recording can be found at \""
							+ recordingFile.getPath()
							+ "\"."
					);
			} else {
				LogManager
					.severe(
						"[AutoBone] Failed to export the recording to \""
							+ recordingFile.getPath()
							+ "\"."
					);
			}
		} else {
			LogManager
				.severe(
					"[AutoBone] Failed to create the recording directory \""
						+ saveDir.getPath()
						+ "\"."
				);
		}
	}

	public void saveRecording(PoseFrames frames, String recordingFileName) {
		saveRecording(frames, new File(saveDir, recordingFileName));
	}

	public void saveRecording(PoseFrames frames) {
		File recordingFile;
		int recordingIndex = 1;
		do {
			recordingFile = new File(saveDir, "ABRecording" + recordingIndex++ + ".pfr");
		} while (recordingFile.exists());

		saveRecording(frames, recordingFile);
	}

	public List<Pair<String, PoseFrames>> loadRecordings() {
		List<Pair<String, PoseFrames>> recordings = new FastList<>();
		if (loadDir.isDirectory()) {
			File[] files = loadDir.listFiles();
			if (files != null) {
				for (File file : files) {
					if (
						file.isFile()
							&& org.apache.commons.lang3.StringUtils
								.endsWithIgnoreCase(file.getName(), ".pfr")
					) {
						LogManager
							.info(
								"[AutoBone] Detected recording at \""
									+ file.getPath()
									+ "\", loading frames..."
							);
						PoseFrames frames = PoseFrameIO.readFromFile(file);

						if (frames == null) {
							LogManager
								.severe("Reading frames from \"" + file.getPath() + "\" failed...");
						} else {
							recordings.add(Pair.of(file.getName(), frames));
						}
					}
				}
			}
		}

		return recordings;
	}

	public AutoBoneConfig getConfig() {
		return config;
	}

	public class Epoch {

		public final int epoch;
		public final int totalEpochs;
		public final StatsCalculator epochError;
		public final EnumMap<SkeletonConfigOffsets, Float> configValues;

		public Epoch(
			int epoch,
			int totalEpochs,
			StatsCalculator epochError,
			EnumMap<SkeletonConfigOffsets, Float> configValues
		) {
			this.epoch = epoch;
			this.totalEpochs = totalEpochs;
			this.epochError = epochError;
			this.configValues = configValues;
		}

		@Override
		public String toString() {
			return "Epoch: " + epoch + ", Epoch Error: " + epochError;
		}
	}

	public class AutoBoneResults {

		public final float finalHeight;
		public final float targetHeight;
		public final StatsCalculator epochError;
		public final EnumMap<SkeletonConfigOffsets, Float> configValues;

		public AutoBoneResults(
			float finalHeight,
			float targetHeight,
			StatsCalculator epochError,
			EnumMap<SkeletonConfigOffsets, Float> configValues
		) {
			this.finalHeight = finalHeight;
			this.targetHeight = targetHeight;
			this.epochError = epochError;
			this.configValues = configValues;
		}

		public float getHeightDifference() {
			return FastMath.abs(targetHeight - finalHeight);
		}
	}
}
