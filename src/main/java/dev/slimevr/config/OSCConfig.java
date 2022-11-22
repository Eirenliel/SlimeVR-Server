package dev.slimevr.config;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import dev.slimevr.config.serializers.BooleanMapDeserializer;
import dev.slimevr.vr.trackers.TrackerRole;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


public class OSCConfig {

	// Is OSC enabled for the app
	private boolean enabled = false;

	// Port to receive OSC messages from
	private int portIn = 9001;

	// Port to send out OSC messages at
	private int portOut = 9000;

	// Address to send out OSC messages at
	private String address = "127.0.0.1";

	// Which trackers' data to send
	@JsonDeserialize(using = BooleanMapDeserializer.class)
	@JsonSerialize(keyUsing = StdKeySerializers.StringKeySerializer.class)
	public Map<String, Boolean> trackers = new HashMap<>();
	private final TrackerRole[] defaultRoles = new TrackerRole[] { TrackerRole.WAIST,
		TrackerRole.LEFT_FOOT, TrackerRole.RIGHT_FOOT };

	public OSCConfig() {
		// Initialize default tracker role settings
		for (TrackerRole role : defaultRoles) {
			setOSCTrackerRole(
				role,
				getOSCTrackerRole(role, true)
			);
		}

	}

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean value) {
		enabled = value;
	}

	public int getPortIn() {
		return portIn;
	}

	public void setPortIn(int portIn) {
		this.portIn = portIn;
	}

	public int getPortOut() {
		return portOut;
	}

	public void setPortOut(int portOut) {
		this.portOut = portOut;
	}

	public InetAddress getAddress() {
		try {
			return InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean getOSCTrackerRole(TrackerRole role, boolean def) {
		return trackers.getOrDefault(role.name().toLowerCase(), def);
	}

	public void setOSCTrackerRole(TrackerRole role, boolean val) {
		this.trackers.put(role.name().toLowerCase(), val);
	}
}
