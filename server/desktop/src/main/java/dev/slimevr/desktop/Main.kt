@file:JvmName("Main")

package dev.slimevr.desktop

import dev.slimevr.Keybinding
import dev.slimevr.SLIMEVR_IDENTIFIER
import dev.slimevr.CONFIG_FILENAME
import dev.slimevr.VRServer
import dev.slimevr.bridge.ISteamVRBridge
import dev.slimevr.desktop.platform.SteamVRBridge
import dev.slimevr.desktop.platform.linux.UnixSocketBridge
import dev.slimevr.desktop.platform.windows.WindowsNamedPipeBridge
import dev.slimevr.desktop.serial.DesktopSerialHandler
import dev.slimevr.desktop.tracking.trackers.hid.TrackersHID
import dev.slimevr.tracking.trackers.Tracker
import io.eiren.util.OperatingSystem
import io.eiren.util.collections.FastList
import io.eiren.util.logging.LogManager
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.lang3.SystemUtils
import java.io.File
import java.io.IOException
import java.lang.System
import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.JOptionPane
import kotlin.concurrent.thread
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.pathString
import kotlin.system.exitProcess

val VERSION =
	(GIT_VERSION_TAG.ifEmpty { GIT_COMMIT_HASH }) +
		if (GIT_CLEAN) "" else "-dirty"

fun main(args: Array<String>) {
	System.setProperty("awt.useSystemAAFontSettings", "on")
	System.setProperty("swing.aatext", "true")

	val parser: CommandLineParser = DefaultParser()
	val formatter = HelpFormatter()
	val options = Options()
	options.addOption("h", "help", false, "Show help")
	options.addOption("V", "version", false, "Show version")
	val cmd: CommandLine = try {
		parser.parse(options, args, true)
	} catch (e: org.apache.commons.cli.ParseException) {
		formatter.printHelp("slimevr.jar", options)
		exitProcess(1)
	}

	if (cmd.hasOption("help")) {
		formatter.printHelp("slimevr.jar", options)
		exitProcess(0)
	}
	if (cmd.hasOption("version")) {
		println("SlimeVR Server $VERSION")
		exitProcess(0)
	}

	if (cmd.args.isEmpty()) {
		System.err.println("No command specified, expected 'run'")
		exitProcess(1)
	}
	if (!cmd.args[0].equals("run", true)) {
		System.err.println("Unknown command: ${cmd.args[0]}, expected 'run'")
		exitProcess(1)
	}

	val dir = OperatingSystem.resolveLogDirectory(SLIMEVR_IDENTIFIER)?.toFile()?.absoluteFile
		?: File("").absoluteFile
	try {
		LogManager.initialize(dir)
	} catch (e1: java.lang.Exception) {
		e1.printStackTrace()
	}
	LogManager.info("Using log folder: $dir")
	LogManager.info("Running version $VERSION")
	if (!SystemUtils.isJavaVersionAtLeast(org.apache.commons.lang3.JavaVersion.JAVA_17)) {
		LogManager.severe("SlimeVR start-up error! A minimum of Java 17 is required.")
		JOptionPane
			.showMessageDialog(
				null,
				"SlimeVR start-up error! A minimum of Java 17 is required.",
				"SlimeVR: Java Runtime Mismatch",
				JOptionPane.ERROR_MESSAGE,
			)
		LogManager.closeLogger()
		return
	}
	try {
		// This is disabled because the config can't be read at this point
		// ServerSocket(6969).close()
		ServerSocket(21110).close()
	} catch (e: IOException) {
		LogManager
			.severe(
				"SlimeVR start-up error! Required ports are busy. " +
					"Make sure there is no other instance of SlimeVR Server running.",
			)
		JOptionPane
			.showMessageDialog(
				null,
				"SlimeVR start-up error! Required ports are busy. " +
					"Make sure there is no other instance of SlimeVR Server running.",
				"SlimeVR: Ports are busy",
				JOptionPane.ERROR_MESSAGE,
			)
		LogManager.closeLogger()
		return
	}
	try {
		val configDir = resolveConfig()
		LogManager.info("Using config dir: $configDir")
		val vrServer = VRServer(
			::provideSteamVRBridge,
			::provideFeederBridge,
			{ _ -> DesktopSerialHandler() },
			configPath = configDir,
		)
		vrServer.start()

		// Start service for USB HID trackers
		TrackersHID(
			"Sensors HID service",
		) { tracker: Tracker -> vrServer.registerTracker(tracker) }

		Keybinding(vrServer)
		val scanner = thread {
			while (true) {
				if (readln() == "exit") {
					vrServer.interrupt()
					break
				}
			}
		}
		vrServer.join()
		scanner.join()
		LogManager.closeLogger()
		exitProcess(0)
	} catch (e: Throwable) {
		e.printStackTrace()
		exitProcess(1)
	}
}

fun provideSteamVRBridge(
	server: VRServer,
	computedTrackers: List<Tracker>,
): ISteamVRBridge? {
	val driverBridge: SteamVRBridge?
	if (OperatingSystem.currentPlatform == OperatingSystem.WINDOWS) {
		// Create named pipe bridge for SteamVR driver
		driverBridge = WindowsNamedPipeBridge(
			server,
			"steamvr",
			"SteamVR Driver Bridge",
			"""\\.\pipe\SlimeVRDriver""",
			computedTrackers,
		)
	} else if (OperatingSystem.currentPlatform == OperatingSystem.LINUX) {
		var linuxBridge: SteamVRBridge? = null
		try {
			linuxBridge = UnixSocketBridge(
				server,
				"steamvr",
				"SteamVR Driver Bridge",
				Paths.get(OperatingSystem.socketDirectory, "SlimeVRDriver")
					.toString(),
				computedTrackers,
			)
		} catch (ex: Exception) {
			LogManager.severe(
				"Failed to initiate Unix socket, disabling driver bridge...",
				ex,
			)
		}
		driverBridge = linuxBridge
		if (driverBridge != null) {
			// Close the named socket on shutdown, or otherwise it's not going to get removed
			Runtime.getRuntime().addShutdownHook(
				Thread {
					try {
						(driverBridge as? UnixSocketBridge)?.close()
					} catch (e: Exception) {
						throw RuntimeException(e)
					}
				},
			)
		}
	} else {
		driverBridge = null
	}

	return driverBridge
}

fun provideFeederBridge(
	server: VRServer,
): ISteamVRBridge? {
	val feederBridge: SteamVRBridge?
	when (OperatingSystem.currentPlatform) {
		OperatingSystem.WINDOWS -> {
			// Create named pipe bridge for SteamVR input
			feederBridge = WindowsNamedPipeBridge(
				server,
				"steamvr_feeder",
				"SteamVR Feeder Bridge",
				"""\\.\pipe\SlimeVRInput""",
				FastList(),
			)
		}

		OperatingSystem.LINUX -> {
			feederBridge = UnixSocketBridge(
				server,
				"steamvr_feeder",
				"SteamVR Feeder Bridge",
				Paths.get(OperatingSystem.socketDirectory, "SlimeVRInput")
					.toString(),
				FastList(),
			)
		}

		else -> {
			feederBridge = null
		}
	}

	return feederBridge
}

fun resolveConfig(): String {
	// If config folder exists, then save config on relative path
	if (Path("config/").exists()) {
		return CONFIG_FILENAME
	}

	val configFile = OperatingSystem.resolveConfigDirectory(SLIMEVR_IDENTIFIER)?.resolve(CONFIG_FILENAME) ?: return CONFIG_FILENAME
	if (!configFile.exists() && Path(CONFIG_FILENAME).exists()) {
		LogManager.info("Moved local config file to appdata folder")
		Files.move(Path(CONFIG_FILENAME), configFile)
	}
	return configFile.pathString
}
