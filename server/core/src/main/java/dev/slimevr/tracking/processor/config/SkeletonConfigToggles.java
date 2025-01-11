package dev.slimevr.tracking.processor.config;

import java.util.HashMap;
import java.util.Map;


public enum SkeletonConfigToggles {

	EXTENDED_SPINE_MODEL(1, "Extended spine model", "extendedSpine", true),
	EXTENDED_PELVIS_MODEL(2, "Extended pelvis model", "extendedPelvis", true),
	EXTENDED_KNEE_MODEL(3, "Extended knee model", "extendedKnee", true),
	FORCE_ARMS_FROM_HMD(4, "Force arms from HMD", "forceArmsFromHMD", true),
	FLOOR_CLIP(5, "Floor clip", "floorClip", true),
	SKATING_CORRECTION(6, "Skating correction", "skatingCorrection", true),
	VIVE_EMULATION(7, "Vive emulation", "viveEmulation", false),
	TOE_SNAP(8, "Toe Snap", "toeSnap", false),
	FOOT_PLANT(9, "Foot Plant", "footPlant", true),
	SELF_LOCALIZATION(10, "Self Localization", "selfLocalization", false),
	USE_POSITION(11, "Use Position", "usePosition", true),
	ENFORCE_CONSTRAINTS(12, "Enforce Constraints", "enforceConstraints", true),
	CORRECT_CONSTRAINTS(13, "Correct Constraints", "correctConstraints", true),;

	public static final SkeletonConfigToggles[] values = values();
	private static final Map<String, SkeletonConfigToggles> byStringVal = new HashMap<>();

	private static final Map<Number, SkeletonConfigToggles> byIdVal = new HashMap<>();

	static {
		for (SkeletonConfigToggles configVal : values()) {
			byIdVal.put(configVal.id, configVal);
			byStringVal.put(configVal.stringVal.toLowerCase(), configVal);
		}
	}

	public final int id;
	public final String stringVal;
	public final String configKey;
	public final boolean defaultValue;

	SkeletonConfigToggles(int id, String stringVal, String configKey, boolean defaultValue) {
		this.id = id;
		this.stringVal = stringVal;
		this.configKey = configKey;

		this.defaultValue = defaultValue;
	}

	public static SkeletonConfigToggles getByStringValue(String stringVal) {
		return stringVal == null ? null : byStringVal.get(stringVal.toLowerCase());
	}

	public static SkeletonConfigToggles getById(int id) {
		return byIdVal.get(id);
	}
}
