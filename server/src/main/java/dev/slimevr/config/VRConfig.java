package dev.slimevr.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import com.github.jonpeterson.jackson.module.versioning.JsonVersionedModel;
import dev.slimevr.config.serializers.BridgeConfigMapDeserializer;
import dev.slimevr.config.serializers.TrackerConfigMapDeserializer;
import dev.slimevr.tracking.trackers.Tracker;

import java.util.HashMap;
import java.util.Map;


@JsonVersionedModel(
	currentVersion = "8", defaultDeserializeToVersion = "8", toCurrentConverterClass = CurrentVRConfigConverter.class
)
public class VRConfig {

	private final ServerConfig server = new ServerConfig();

	private final FiltersConfig filters = new FiltersConfig();

	private final DriftCompensationConfig driftCompensation = new DriftCompensationConfig();

	private final OSCConfig oscRouter = new OSCConfig();

	private final VRCOSCConfig vrcOSC = new VRCOSCConfig();

	private final VMCConfig vmc = new VMCConfig();

	private final AutoBoneConfig autoBone = new AutoBoneConfig();

	private final KeybindingsConfig keybindings = new KeybindingsConfig();

	private final SkeletonConfig skeleton = new SkeletonConfig();

	private final LegTweaksConfig legTweaks = new LegTweaksConfig();

	private final TapDetectionConfig tapDetection = new TapDetectionConfig();

	@JsonDeserialize(using = TrackerConfigMapDeserializer.class)
	@JsonSerialize(keyUsing = StdKeySerializers.StringKeySerializer.class)
	private final Map<String, TrackerConfig> trackers = new HashMap<>();

	@JsonDeserialize(using = BridgeConfigMapDeserializer.class)
	@JsonSerialize(keyUsing = StdKeySerializers.StringKeySerializer.class)
	private final Map<String, BridgeConfig> bridges = new HashMap<>();

	private final OverlayConfig overlay = new OverlayConfig();

	public VRConfig() {

	}


	public ServerConfig getServer() {
		return server;
	}

	public FiltersConfig getFilters() {
		return filters;
	}

	public DriftCompensationConfig getDriftCompensation() {
		return driftCompensation;
	}

	public OSCConfig getOscRouter() {
		return oscRouter;
	}

	public VRCOSCConfig getVrcOSC() {
		return vrcOSC;
	}

	public VMCConfig getVMC() {
		return vmc;
	}

	public AutoBoneConfig getAutoBone() {
		return autoBone;
	}

	public KeybindingsConfig getKeybindings() {
		return keybindings;
	}

	public Map<String, TrackerConfig> getTrackers() {
		return trackers;
	}

	public Map<String, BridgeConfig> getBridges() {
		return bridges;
	}

	public SkeletonConfig getSkeleton() {
		return skeleton;
	}

	public LegTweaksConfig getLegTweaks() {
		return legTweaks;
	}

	public TapDetectionConfig getTapDetection() {
		return tapDetection;
	}

	public OverlayConfig getOverlay() {
		return overlay;
	}

	public TrackerConfig getTracker(Tracker tracker) {
		TrackerConfig config = trackers.get(tracker.getName());
		if (config == null) {
			config = new TrackerConfig(tracker);
			trackers.put(tracker.getName(), config);
		}
		return config;
	}

	public void readTrackerConfig(Tracker tracker) {
		TrackerConfig config = getTracker(tracker);
		tracker.readConfig(config);
	}

	public void writeTrackerConfig(Tracker tracker) {
		TrackerConfig tc = getTracker(tracker);
		tracker.writeConfig(tc);
	}

	public BridgeConfig getBridge(String bridgeKey) {
		BridgeConfig config = bridges.get(bridgeKey);
		if (config == null) {
			config = new BridgeConfig();
			bridges.put(bridgeKey, config);
		}
		return config;
	}
}

