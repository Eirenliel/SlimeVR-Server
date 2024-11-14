package dev.slimevr.tracking.processor.config

import dev.slimevr.VRServer.Companion.instance
import dev.slimevr.autobone.errors.BodyProportionError
import dev.slimevr.autobone.errors.BodyProportionError.Companion.proportionLimitMap
import dev.slimevr.config.ConfigManager
import dev.slimevr.tracking.processor.BoneType
import dev.slimevr.tracking.processor.HumanPoseManager
import io.github.axisangles.ktmath.Vector3
import java.util.*

class SkeletonConfigManager(
	private val autoUpdateOffsets: Boolean,
	private val humanPoseManager: HumanPoseManager? = null,
) {
	private val configOffsets: EnumMap<SkeletonConfigOffsets, Float> = EnumMap(
		SkeletonConfigOffsets::class.java,
	)
	private val configToggles: EnumMap<SkeletonConfigToggles, Boolean?> = EnumMap(
		SkeletonConfigToggles::class.java,
	)
	private val configValues: EnumMap<SkeletonConfigValues, Float?> = EnumMap(
		SkeletonConfigValues::class.java,
	)

	private var changedToggles: BooleanArray = BooleanArray(SkeletonConfigToggles.values.size)
	private var changedValues: BooleanArray = BooleanArray(SkeletonConfigValues.values.size)

	private val nodeOffsets: EnumMap<BoneType, Vector3> = EnumMap(
		BoneType::class.java,
	)

	var userHeightFromOffsets: Float = calculateUserHeight()
		private set

	init {
		if (humanPoseManager?.isSkeletonPresent != false) {
			updateSettingsInSkeleton()

			if (autoUpdateOffsets) {
				computeAllNodeOffsets()
			}
		}
	}

	fun updateSettingsInSkeleton() {
		if (humanPoseManager == null) return

		for (config in SkeletonConfigToggles.values) {
			val configToggle = configToggles[config]
			humanPoseManager.updateToggleState(config, configToggle ?: config.defaultValue)
		}

		for (config in SkeletonConfigValues.values) {
			val configValue = configValues[config]
			humanPoseManager.updateValueState(config, configValue ?: config.defaultValue)
		}
	}

	fun updateNodeOffsetsInSkeleton() {
		if (humanPoseManager == null) return

		for (config in BoneType.values) {
			val nodeOffset = nodeOffsets[config]
			if (nodeOffset != null) humanPoseManager.updateNodeOffset(config, nodeOffset)
		}
	}

	fun setOffset(
		config: SkeletonConfigOffsets,
		newValue: Float?,
		computeOffsets: Boolean,
	) {
		if (newValue != null) {
			configOffsets[config] = newValue
		} else {
			configOffsets.remove(config)
		}

		// Re-compute the affected offsets
		if (computeOffsets && autoUpdateOffsets && config.affectedOffsets != null) {
			for (offset in config.affectedOffsets) {
				computeNodeOffset(offset)
			}
		}

		// Re-calculate user height
		userHeightFromOffsets = calculateUserHeight()
	}

	fun setOffset(config: SkeletonConfigOffsets, newValue: Float?) {
		setOffset(config, newValue, true)
	}

	fun getOffset(config: SkeletonConfigOffsets?): Float {
		if (config == null) {
			return 0f
		}

		val configOffset = configOffsets[config]
		return configOffset ?: config.defaultValue
	}

	private fun calculateUserHeight(): Float {
		var height = 0f
		for (offset in HEIGHT_OFFSETS) {
			height += getOffset(offset)
		}
		return height
	}

	fun setToggle(config: SkeletonConfigToggles, newValue: Boolean?) {
		if (newValue != null) {
			if (configToggles[config] != null && (newValue != configToggles[config])) {
				changedToggles[config.id - 1] = true
			}
			configToggles[config] = newValue
		} else {
			configToggles.remove(config)
		}

		// Updates in skeleton
		humanPoseManager?.updateToggleState(config, newValue ?: config.defaultValue)
	}

	fun getToggle(config: SkeletonConfigToggles?): Boolean {
		if (config == null) {
			return false
		}

		val configToggle = configToggles[config]
		return configToggle ?: config.defaultValue
	}

	fun setValue(config: SkeletonConfigValues, newValue: Float?) {
		if (newValue != null) {
			if (configValues[config] != null && (newValue != configValues[config])) {
				changedValues[config.id - 1] = true
			}
			configValues[config] = newValue
		} else {
			configValues.remove(config)
		}

		// Updates in skeleton
		humanPoseManager?.updateValueState(config, newValue ?: config.defaultValue)
	}

	fun getValue(config: SkeletonConfigValues?): Float {
		if (config == null) {
			return 0f
		}

		val configValue = configValues[config]
		return configValue ?: config.defaultValue
	}

	protected fun setNodeOffset(nodeOffset: BoneType, x: Float, y: Float, z: Float) {
		var offset = nodeOffsets[nodeOffset]

		if (offset == null) {
			offset = Vector3(x, y, z)
			nodeOffsets[nodeOffset] = offset
		} else {
			offset = Vector3(x, y, z)
		}

		// Updates in skeleton
		humanPoseManager?.updateNodeOffset(nodeOffset, offset)
	}

	fun computeNodeOffset(nodeOffset: BoneType) {
		when (nodeOffset) {
			BoneType.HEAD -> setNodeOffset(nodeOffset, 0f, 0f, getOffset(SkeletonConfigOffsets.HEAD))

			BoneType.NECK -> setNodeOffset(nodeOffset, 0f, -getOffset(SkeletonConfigOffsets.NECK), 0f)

			BoneType.UPPER_CHEST -> setNodeOffset(
				nodeOffset,
				0f,
				-getOffset(SkeletonConfigOffsets.UPPER_CHEST),
				0f,
			)

			BoneType.CHEST_TRACKER -> setNodeOffset(
				nodeOffset,
				0f,
				-getOffset(SkeletonConfigOffsets.CHEST_OFFSET) -
					getOffset(SkeletonConfigOffsets.CHEST),
				-getOffset(SkeletonConfigOffsets.SKELETON_OFFSET),
			)

			BoneType.CHEST -> setNodeOffset(nodeOffset, 0f, -getOffset(SkeletonConfigOffsets.CHEST), 0f)

			BoneType.WAIST -> setNodeOffset(nodeOffset, 0f, -getOffset(SkeletonConfigOffsets.WAIST), 0f)

			BoneType.HIP -> setNodeOffset(nodeOffset, 0f, -getOffset(SkeletonConfigOffsets.HIP), 0f)

			BoneType.HIP_TRACKER -> setNodeOffset(
				nodeOffset,
				0f,
				-getOffset(SkeletonConfigOffsets.HIP_OFFSET),
				-getOffset(SkeletonConfigOffsets.SKELETON_OFFSET),
			)

			BoneType.LEFT_HIP -> setNodeOffset(
				nodeOffset,
				-getOffset(SkeletonConfigOffsets.HIPS_WIDTH) / 2f,
				0f,
				0f,
			)

			BoneType.RIGHT_HIP -> setNodeOffset(
				nodeOffset,
				getOffset(SkeletonConfigOffsets.HIPS_WIDTH) / 2f,
				0f,
				0f,
			)

			BoneType.LEFT_UPPER_LEG, BoneType.RIGHT_UPPER_LEG -> setNodeOffset(
				nodeOffset,
				0f,
				-getOffset(SkeletonConfigOffsets.UPPER_LEG),
				0f,
			)

			BoneType.LEFT_KNEE_TRACKER, BoneType.RIGHT_KNEE_TRACKER, BoneType.LEFT_FOOT_TRACKER, BoneType.RIGHT_FOOT_TRACKER -> setNodeOffset(
				nodeOffset,
				0f,
				0f,
				-getOffset(SkeletonConfigOffsets.SKELETON_OFFSET),
			)

			BoneType.LEFT_LOWER_LEG, BoneType.RIGHT_LOWER_LEG -> setNodeOffset(
				nodeOffset,
				0f,
				-getOffset(SkeletonConfigOffsets.LOWER_LEG),
				-getOffset(SkeletonConfigOffsets.FOOT_SHIFT),
			)

			BoneType.LEFT_FOOT, BoneType.RIGHT_FOOT -> setNodeOffset(
				nodeOffset,
				0f,
				0f,
				-getOffset(SkeletonConfigOffsets.FOOT_LENGTH),
			)

			BoneType.LEFT_UPPER_SHOULDER -> setNodeOffset(
				nodeOffset,
				0f,
				0f,
				0f,
			)

			BoneType.RIGHT_UPPER_SHOULDER -> setNodeOffset(
				nodeOffset,
				0f,
				0f,
				0f,
			)

			BoneType.LEFT_SHOULDER -> setNodeOffset(
				nodeOffset,
				-getOffset(SkeletonConfigOffsets.SHOULDERS_WIDTH) / 2f,
				-getOffset(SkeletonConfigOffsets.SHOULDERS_DISTANCE),
				0f,
			)

			BoneType.RIGHT_SHOULDER -> setNodeOffset(
				nodeOffset,
				getOffset(SkeletonConfigOffsets.SHOULDERS_WIDTH) / 2f,
				-getOffset(SkeletonConfigOffsets.SHOULDERS_DISTANCE),
				0f,
			)

			BoneType.LEFT_UPPER_ARM, BoneType.RIGHT_UPPER_ARM -> setNodeOffset(
				nodeOffset,
				0f,
				-getOffset(SkeletonConfigOffsets.UPPER_ARM),
				0f,
			)

			BoneType.LEFT_LOWER_ARM, BoneType.RIGHT_LOWER_ARM -> setNodeOffset(
				nodeOffset,
				0f,
				-getOffset(SkeletonConfigOffsets.LOWER_ARM),
				0f,
			)

			BoneType.LEFT_HAND, BoneType.RIGHT_HAND -> setNodeOffset(
				nodeOffset,
				0f,
				-getOffset(SkeletonConfigOffsets.HAND_Y),
				-getOffset(SkeletonConfigOffsets.HAND_Z),
			)

			BoneType.LEFT_ELBOW_TRACKER, BoneType.RIGHT_ELBOW_TRACKER -> setNodeOffset(
				nodeOffset,
				0f,
				-getOffset(SkeletonConfigOffsets.ELBOW_OFFSET),
				0f,
			)

			else -> {}
		}
	}

	fun computeAllNodeOffsets() {
		for (offset in BoneType.values) {
			computeNodeOffset(offset)
		}
	}

	fun setOffsets(
		configOffsets: Map<SkeletonConfigOffsets, Float>?,
		computeOffsets: Boolean,
	) {
		configOffsets?.forEach { (key: SkeletonConfigOffsets, value: Float?) ->
			// Do not recalculate the offsets, these are done in bulk at the
			// end
			setOffset(key, value, false)
		}

		if (computeOffsets && autoUpdateOffsets) {
			computeAllNodeOffsets()
		}
	}

	fun setOffsets(
		configOffsets: Map<SkeletonConfigOffsets, Float>?,
	) {
		setOffsets(configOffsets, true)
	}

	fun setOffsets(skeletonConfigManager: SkeletonConfigManager) {
		// Don't recalculate node offsets, just re-use them from skeletonConfig
		setOffsets(
			skeletonConfigManager.configOffsets,
			false,
		)

		// Copy skeletonConfig's nodeOffsets as the configs are all the same
		skeletonConfigManager.nodeOffsets.forEach { (key: BoneType, value: Vector3) ->
			setNodeOffset(key, value.x, value.y, value.z)
		}
	}

	fun resetOffsets() {
		if (humanPoseManager != null) {
			for (config in SkeletonConfigOffsets.values) {
				resetOffset(config)
			}
		} else {
			configOffsets.clear()
			if (autoUpdateOffsets) {
				computeAllNodeOffsets()
			}
		}
	}

	fun resetToggles() {
		configToggles.clear()

		// Updates in skeleton
		if (humanPoseManager != null) {
			for (config in SkeletonConfigToggles.values) {
				humanPoseManager.updateToggleState(config, config.defaultValue)
			}
		}

		// Remove from config to use default if they change in the future.
		Arrays.fill(changedToggles, false)
		for (value in SkeletonConfigToggles.values) {
			instance.configManager
				.vrConfig
				.skeleton
				.getToggles()
				.remove(value.configKey)
			// Set default in skeleton
			setToggle(value, value.defaultValue)
		}
	}

	fun resetValues() {
		configValues.clear()

		// Updates in skeleton
		if (humanPoseManager != null) {
			for (config in SkeletonConfigValues.values) {
				humanPoseManager.updateValueState(config, config.defaultValue)
			}
		}

		// Remove from config to use default if they change in the future.
		Arrays.fill(changedValues, false)
		for (value in SkeletonConfigValues.values) {
			instance.configManager
				.vrConfig
				.skeleton
				.getValues()
				.remove(value.configKey)
			// Set default in skeleton
			setValue(value, value.defaultValue)
		}
	}

	fun resetAllConfigs() {
		resetOffsets()
		resetToggles()
		resetValues()
	}

	fun resetOffset(config: SkeletonConfigOffsets?) {
		if (config == null) {
			return
		}

		when (config) {
			SkeletonConfigOffsets.UPPER_CHEST, SkeletonConfigOffsets.CHEST, SkeletonConfigOffsets.WAIST, SkeletonConfigOffsets.HIP, SkeletonConfigOffsets.UPPER_LEG, SkeletonConfigOffsets.LOWER_LEG -> {
				val height = (
					humanPoseManager!!.hmdHeight /
						BodyProportionError.eyeHeightToHeightRatio
					)
				if (height > 0.5f) { // Reset only if floor level seems right,
					val proportionLimiter = proportionLimitMap[config]
					if (proportionLimiter != null) {
						setOffset(
							config,
							height * proportionLimiter.targetRatio,
						)
					} else {
						setOffset(config, null)
					}
				} else { // if floor level is incorrect
					setOffset(config, null)
				}
			}

			else -> setOffset(config, null)
		}
	}

	fun loadFromConfig(configManager: ConfigManager) {
		val skeletonConfig = configManager.vrConfig.skeleton

		// Load offsets
		val offsets = skeletonConfig.getOffsets()
		for (configValue in SkeletonConfigOffsets.values) {
			val offset = offsets[configValue.configKey]
			if (offset != null) {
				// Do not recalculate the offsets, these are done in bulk at the
				// end
				setOffset(configValue, offset, false)
			}
		}

		// Load toggles
		val toggles = skeletonConfig.getToggles()
		for (configValue in SkeletonConfigToggles.values) {
			val toggle = toggles[configValue.configKey]
			if (toggle != null) {
				setToggle(configValue, toggle)
			} else {
				humanPoseManager?.updateToggleState(configValue, configValue.defaultValue)
			}
		}

		// Load values
		val values = skeletonConfig.getValues()
		for (configValue in SkeletonConfigValues.values) {
			val value = values[configValue.configKey]
			if (value != null) {
				setValue(configValue, value)
			} else {
				humanPoseManager?.updateValueState(configValue, configValue.defaultValue)
			}
		}

		// Updates all offsets
		if (autoUpdateOffsets) {
			computeAllNodeOffsets()
		}
	}

	fun save() {
		val skeletonConfig = instance.configManager
			.vrConfig
			.skeleton

		// Write all possible values to keep consistent even if defaults changed
		for (value in SkeletonConfigOffsets.values) {
			skeletonConfig.getOffsets()[value.configKey] = getOffset(value)
		}

		// Only write changed values to keep using defaults if not changed
		for (value in SkeletonConfigToggles.values) {
			if (changedToggles[value.id - 1]) skeletonConfig.getToggles()[value.configKey] = getToggle(value)
		}

		// Only write changed values to keep using defaults if not changed
		for (value in SkeletonConfigValues.values) {
			if (changedValues[value.id - 1]) skeletonConfig.getValues()[value.configKey] = getValue(value)
		}
	}

	companion object {
		val HEIGHT_OFFSETS: Array<SkeletonConfigOffsets> = arrayOf(
			SkeletonConfigOffsets.NECK,
			SkeletonConfigOffsets.UPPER_CHEST,
			SkeletonConfigOffsets.CHEST,
			SkeletonConfigOffsets.WAIST,
			SkeletonConfigOffsets.HIP,
			SkeletonConfigOffsets.UPPER_LEG,
			SkeletonConfigOffsets.LOWER_LEG,
		)

		const val FLOOR_OFFSET: Float = 0.05f
	}
}
