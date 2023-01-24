package dev.slimevr.vr.trackers;

import solarxr_protocol.datatypes.BodyPart;

import java.lang.reflect.Modifier;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;


/**
 * Represents a position on the body that a tracker could be placed. Any bone is
 * a valid position.
 *
 * TrackerPosition intentionally lacks a numerical id to avoid breakage.
 */
public enum TrackerPosition {
	// @formatter:off
	HMD("HMD", TrackerRole.HMD, BodyPart.HEAD),
	NECK("body:neck", TrackerRole.NECK, BodyPart.NECK),
	CHEST("body:chest", TrackerRole.CHEST, BodyPart.CHEST),
	WAIST("body:waist", Optional.empty(), BodyPart.WAIST),
	HIP("body:hip", TrackerRole.WAIST, BodyPart.HIP),
	LEFT_UPPER_LEG("body:left_upper_leg", TrackerRole.LEFT_KNEE, BodyPart.LEFTUPPERLEG),
	RIGHT_UPPER_LEG("body:right_upper_leg", TrackerRole.RIGHT_KNEE, BodyPart.RIGHTUPPERLEG),
	LEFT_LOWER_LEG("body:left_lower_leg", Optional.empty(), BodyPart.LEFTLOWERLEG),
	RIGHT_LOWER_LEG("body:right_lower_leg", Optional.empty(), BodyPart.RIGHTLOWERLEG),
	LEFT_FOOT("body:left_foot", TrackerRole.LEFT_FOOT, BodyPart.LEFTFOOT),
	RIGHT_FOOT("body:right_foot", TrackerRole.RIGHT_FOOT, BodyPart.RIGHTFOOT),
	LEFT_CONTROLLER("body:left_controller", TrackerRole.LEFT_CONTROLLER, BodyPart.LEFTCONTROLLER),
	RIGHT_CONTROLLER("body:right_controller", TrackerRole.RIGHT_CONTROLLER, BodyPart.RIGHTCONTROLLER),
	LEFT_LOWER_ARM("body:left_lower_arm", TrackerRole.LEFT_ELBOW, BodyPart.LEFTLOWERARM),
	RIGHT_LOWER_ARM("body:right_lower_arm", TrackerRole.RIGHT_ELBOW, BodyPart.RIGHTLOWERARM),
	LEFT_UPPER_ARM("body:left_upper_arm", TrackerRole.LEFT_SHOULDER, BodyPart.LEFTUPPERARM),
	RIGHT_UPPER_ARM("body:right_upper_arm", TrackerRole.RIGHT_SHOULDER, BodyPart.RIGHTUPPERARM),
	LEFT_HAND("body:left_hand", TrackerRole.LEFT_HAND, BodyPart.LEFTHAND),
	RIGHT_HAND("body:right_hand", TrackerRole.RIGHT_HAND, BodyPart.RIGHTHAND),
	LEFT_SHOULDER("body:left_shoulder", TrackerRole.LEFT_SHOULDER, BodyPart.LEFTSHOULDER),
	RIGHT_SHOULDER("body:right_shoulder", TrackerRole.RIGHT_SHOULDER, BodyPart.RIGHTSHOULDER);
	// @formatter:on

	public static final TrackerPosition[] values = values();

	public final String designation;
	public final Optional<TrackerRole> trackerRole;
	/** The associated `BodyPart` */
	public final byte bodyPart;

	TrackerPosition(String designation, TrackerRole nullableTrackerRole, byte bodyPart) {
		this(designation, Optional.ofNullable(nullableTrackerRole), bodyPart);
	}

	TrackerPosition(String designation, Optional<TrackerRole> trackerRole, byte bodyPart) {
		this.designation = designation;
		this.trackerRole = trackerRole;
		this.bodyPart = bodyPart;
	}

	/** Indexed by `BodyPart` int value. EFFICIENCY FTW */
	private static final TrackerPosition[] byBodyPart;
	static {
		// Determine maximum value of BodyPart. Kinda hacky, but this will
		// prevent breakage if the max value changes.
		int max = 0;
		for (var field : BodyPart.class.getFields()) {
			if (!Modifier.isStatic(field.getModifiers()) || !field.getType().equals(int.class)) {
				continue;
			}
			try {
				var v = field.getInt(null);
				max = Math.max(max, v);
			} catch (IllegalAccessException e) {
				// unreachable
				java.util.logging.Logger.getGlobal().log(Level.SEVERE, "Reached unreachable code");
			}
		}
		byBodyPart = new TrackerPosition[max + 1];

		for (var tp : TrackerPosition.values) {
			byBodyPart[tp.bodyPart] = tp;
		}
	}

	private static final Map<String, TrackerPosition> byDesignation = new HashMap<>();
	private static final EnumMap<TrackerRole, TrackerPosition> byTrackerRole = new EnumMap<>(
		TrackerRole.class
	);
	static {
		for (TrackerPosition tp : values()) {
			byDesignation.put(tp.designation.toLowerCase(), tp);
			tp.trackerRole.ifPresent((tr) -> byTrackerRole.put(tr, tp));
		}
	}

	/**
	 * Gets the `TrackerPosition` by its string designation.
	 *
	 * @return Returns an optional as not all strings are valid designators.
	 */
	public static Optional<TrackerPosition> getByDesignation(String designation) {
		if (designation == null) {
			return Optional.empty();
		}

		// Support old configs.
		if (
			designation.equalsIgnoreCase("body:left_leg")
				|| designation.equalsIgnoreCase("body:left_knee")
		) {
			designation = "body:left_upper_leg";
		} else if (
			designation.equalsIgnoreCase("body:right_leg")
				|| designation.equalsIgnoreCase("body:right_knee")
		) {
			designation = "body:right_upper_leg";
		} else if (designation.equalsIgnoreCase("body:left_ankle")) {
			designation = "body:left_lower_leg";
		} else if (designation.equalsIgnoreCase("body:right_ankle")) {
			designation = "body:right_lower_leg";
		} else if (designation.equalsIgnoreCase("body:left_forearm")) {
			designation = "body:left_lower_arm";
		} else if (designation.equalsIgnoreCase("body:right_forearm")) {
			designation = "body:right_lower_arm";
		} else if (designation.equalsIgnoreCase("body:left_upperarm")) {
			designation = "body:left_upper_arm";
		} else if (designation.equalsIgnoreCase("body:right_upperarm")) {
			designation = "body:right_upper_arm";
		}

		return Optional.ofNullable(byDesignation.get(designation.toLowerCase()));
	}

	public static Optional<TrackerPosition> getByTrackerRole(TrackerRole role) {
		return Optional.ofNullable(byTrackerRole.get(role));
	}

	public static Optional<TrackerPosition> getByBodyPart(int bodyPart) {
		if (bodyPart <= 0 || bodyPart >= TrackerPosition.byBodyPart.length) {
			return Optional.empty();
		}
		return Optional.of(TrackerPosition.byBodyPart[bodyPart]);
	}

}
