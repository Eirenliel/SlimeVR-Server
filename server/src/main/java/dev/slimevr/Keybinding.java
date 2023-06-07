package dev.slimevr;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import dev.slimevr.config.KeybindingsConfig;
import io.eiren.util.OperatingSystem;
import io.eiren.util.ann.AWTThread;
import io.eiren.util.logging.LogManager;
import solarxr_protocol.rpc.ResetType;


public class Keybinding implements HotkeyListener {
	private static final String resetSourceName = "Keybinding";

	private static final int FULL_RESET = 1;
	private static final int YAW_RESET = 2;
	private static final int MOUNTING_RESET = 3;
	public final VRServer server;
	public final KeybindingsConfig config;

	@AWTThread
	public Keybinding(VRServer server) {
		this.server = server;

		this.config = server.getConfigManager().getVrConfig().keybindings;

		if (OperatingSystem.getCurrentPlatform() != OperatingSystem.WINDOWS) {
			LogManager
				.info(
					"[Keybinding] Currently only supported on Windows. Keybindings will be disabled."
				);
			return;
		}

		try {
			if (JIntellitype.getInstance() instanceof JIntellitype) {
				JIntellitype.getInstance().addHotKeyListener(this);

				String fullResetBinding = this.config.fullResetBinding;
				JIntellitype.getInstance().registerHotKey(FULL_RESET, fullResetBinding);
				LogManager.info("[Keybinding] Bound full reset to " + fullResetBinding);

				String yawResetBinding = this.config.yawResetBinding;
				JIntellitype.getInstance().registerHotKey(YAW_RESET, yawResetBinding);
				LogManager.info("[Keybinding] Bound yaw reset to " + yawResetBinding);

				String mountingResetBinding = this.config.mountingResetBinding;
				JIntellitype.getInstance().registerHotKey(MOUNTING_RESET, mountingResetBinding);
				LogManager.info("[Keybinding] Bound reset mounting to " + mountingResetBinding);
			}
		} catch (Throwable e) {
			LogManager
				.warning(
					"[Keybinding] JIntellitype initialization failed. Keybindings will be disabled. Try restarting your computer."
				);
		}
	}

	@AWTThread
	@Override
	public void onHotKey(int identifier) {
		switch (identifier) {
			case FULL_RESET -> {
				server.getResetHandler().sendStarted(ResetType.Full);
				server.scheduleResetTrackersFull(resetSourceName, this.config.fullResetDelay);
			}
			case YAW_RESET -> {
				server.getResetHandler().sendStarted(ResetType.Yaw);
				server.scheduleResetTrackersYaw(resetSourceName, this.config.yawResetDelay);
			}
			case MOUNTING_RESET -> {
				server.getResetHandler().sendStarted(ResetType.Mounting);
				server
					.scheduleResetTrackersMounting(
						resetSourceName,
						this.config.mountingResetDelay
					);
			}
		}
	}
}
