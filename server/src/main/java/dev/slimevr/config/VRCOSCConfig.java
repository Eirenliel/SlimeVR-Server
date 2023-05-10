package dev.slimevr.config;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import dev.slimevr.config.serializers.BooleanMapDeserializer;
import dev.slimevr.tracking.trackers.TrackerRole;

import java.util.HashMap;
import java.util.Map;


public class VRCOSCConfig extends OSCConfig {

	// Which trackers' data to send
	@JsonDeserialize(using = BooleanMapDeserializer.class)
	@JsonSerialize(keyUsing = StdKeySerializers.StringKeySerializer.class)
	public Map<String, Boolean> trackers = new HashMap<>();

	public VRCOSCConfig() {
		super.setPortIn(9001);
		super.setPortOut(9000);
		getOSCTrackerRole(TrackerRole.WAIST, getOSCTrackerRole(TrackerRole.WAIST, true));
		getOSCTrackerRole(TrackerRole.LEFT_FOOT, getOSCTrackerRole(TrackerRole.WAIST, true));
		getOSCTrackerRole(TrackerRole.RIGHT_FOOT, getOSCTrackerRole(TrackerRole.WAIST, true));
	}

	public boolean getOSCTrackerRole(TrackerRole role, boolean def) {
		return trackers.getOrDefault(role.name().toLowerCase(), def);
	}

	public void setOSCTrackerRole(TrackerRole role, boolean val) {
		this.trackers.put(role.name().toLowerCase(), val);
	}
}
