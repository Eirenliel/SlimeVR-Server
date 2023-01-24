package dev.slimevr.vr.processor.skeleton

enum class SkeletonConfigOffsets(
	val id: UByte,
	@JvmField val configKey: String,
	@JvmField val defaultValue: Float,
	@JvmField val affectedOffsets: Array<BoneType>,
) {
	HEAD(
		1u,
		"headShift",
		0.1f,
		arrayOf(BoneType.HEAD),
	),
	NECK(
		2u,
		"neckLength",
		0.1f,
		arrayOf(BoneType.NECK),
	),
	CHEST(
		3u,
		"chestLength",
		0.32f,
		arrayOf(
			BoneType.CHEST,
			BoneType.CHEST_TRACKER,
			BoneType.LEFT_SHOULDER,
			BoneType.RIGHT_SHOULDER,
		),
	),
	CHEST_OFFSET(
		4u,
		"chestOffset",
		0.0f,
		arrayOf(BoneType.CHEST_TRACKER),
	),
	WAIST(
		5u,
		"waistLength",
		0.20f,
		arrayOf(BoneType.WAIST),
	),
	HIP(
		6u,
		"hipLength",
		0.04f,
		arrayOf(BoneType.HIP),
	),
	HIP_OFFSET(
		7u,
		"hipOffset",
		0.0f,
		arrayOf(BoneType.HIP_TRACKER),
	),
	HIPS_WIDTH(
		8u,
		"hipsWidth",
		0.26f,
		arrayOf(BoneType.LEFT_HIP, BoneType.RIGHT_HIP),
	),
	UPPER_LEG(
		9u,
		"upperLegLength",
		0.42f,
		arrayOf(BoneType.LEFT_UPPER_LEG, BoneType.RIGHT_UPPER_LEG),
	),
	LOWER_LEG(
		10u,
		"lowerLegLength",
		0.50f,
		arrayOf(BoneType.LEFT_LOWER_LEG, BoneType.RIGHT_LOWER_LEG),
	),
	FOOT_LENGTH(
		11u,
		"footLength",
		0.05f,
		arrayOf(BoneType.LEFT_FOOT, BoneType.RIGHT_FOOT),
	),
	FOOT_SHIFT(
		12u,
		"footShift",
		-0.05f,
		arrayOf(BoneType.LEFT_LOWER_LEG, BoneType.RIGHT_LOWER_LEG),
	),
	SKELETON_OFFSET(
		13u,
		"skeletonOffset",
		0.0f,
		arrayOf(
			BoneType.CHEST_TRACKER,
			BoneType.HIP_TRACKER,
			BoneType.LEFT_KNEE_TRACKER,
			BoneType.RIGHT_KNEE_TRACKER,
			BoneType.LEFT_FOOT_TRACKER,
			BoneType.RIGHT_KNEE_TRACKER,
		),
	),
	SHOULDERS_DISTANCE(
		14u,
		"shouldersDistance",
		0.08f,
		arrayOf(BoneType.LEFT_SHOULDER, BoneType.RIGHT_SHOULDER),
	),
	SHOULDERS_WIDTH(
		15u,
		"shouldersWidth",
		0.36f,
		arrayOf(BoneType.LEFT_SHOULDER, BoneType.RIGHT_SHOULDER),
	),
	UPPER_ARM(
		16u,
		"upperArmLength",
		0.25f,
		arrayOf(BoneType.LEFT_UPPER_ARM, BoneType.RIGHT_UPPER_ARM),
	),
	LOWER_ARM(
		17u,
		"lowerArmLength",
		0.25f,
		arrayOf(BoneType.LEFT_LOWER_ARM, BoneType.RIGHT_LOWER_ARM),
	),
	CONTROLLER_Y(
		18u,
		"controllerDistanceY",
		0.035f,
		arrayOf(
			BoneType.LEFT_CONTROLLER,
			BoneType.RIGHT_CONTROLLER,
			BoneType.LEFT_HAND,
			BoneType.RIGHT_HAND,
		),
	),
	CONTROLLER_Z(
		19u,
		"controllerDistanceZ",
		0.13f,
		arrayOf(
			BoneType.LEFT_CONTROLLER,
			BoneType.RIGHT_CONTROLLER,
			BoneType.LEFT_HAND,
			BoneType.RIGHT_HAND,
		),
	),
	ELBOW_OFFSET(
		20u,
		"elbowOffset",
		0.0f,
		arrayOf(BoneType.LEFT_ELBOW_TRACKER, BoneType.RIGHT_ELBOW_TRACKER),
	),
	;

	companion object {
		@JvmStatic
		fun getById(id: UByte): SkeletonConfigOffsets? = byId[id]
	}
}

private val byId = SkeletonConfigOffsets.values().associateBy { it.id }
