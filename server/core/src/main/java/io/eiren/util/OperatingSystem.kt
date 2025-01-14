package io.eiren.util

import java.io.File
import java.nio.file.Path
import java.util.*
import kotlin.io.path.Path

enum class OperatingSystem(
	val descriptor: String,
	private val aliases: Array<String>,
) {
	LINUX("linux", arrayOf("linux", "unix")),
	WINDOWS("windows", arrayOf("win")),
	OSX("osx", arrayOf("mac")),
	UNKNOWN("unknown", arrayOf()),
	;

	companion object {
		val currentPlatform: OperatingSystem by lazy {
			val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
			entries.find { os -> os.aliases.any { alias -> osName.contains(alias) } }?.let {
				return@lazy it
			}
			UNKNOWN
		}
		fun getJavaExecutable(forceConsole: Boolean): String {
			val separator = System.getProperty("file.separator")
			val path = System.getProperty("java.home") + separator + "bin" + separator
			return if (currentPlatform == WINDOWS) {
				if (!forceConsole && File(path + "javaw.exe").isFile) path + "javaw.exe" else path + "java.exe"
			} else {
				path + "java"
			}
		}

		val socketDirectory: String
			get() {
				var dir = System.getenv("SLIMEVR_SOCKET_DIR")
				if (dir != null) return dir
				if (currentPlatform == LINUX) {
					dir = System.getenv("XDG_RUNTIME_DIR")

					// add /app/$FLATPAK_ID if running in flatpak
					// see https://docs.flatpak.org/en/latest/sandbox-permissions.html
					val flatpak_id = System.getenv("FLATPAK_ID")
					if (flatpak_id != null) dir += "/app/" + flatpak_id

					if (dir != null) return dir
				}
				return System.getProperty("java.io.tmpdir")
			}

		fun resolveConfigDirectory(identifier: String): Path? = when (currentPlatform) {
			LINUX -> System.getenv("XDG_CONFIG_HOME")?.let { Path(it, identifier) }
				?: System.getenv("HOME")?.let { Path(it, ".config", identifier) }

			WINDOWS -> System.getenv("AppData")?.let { Path(it, identifier) }

			OSX -> System.getenv("HOME")?.let { Path(it, "Library", "Application Support", identifier) }

			UNKNOWN -> null
		}

		fun resolveLogDirectory(identifier: String): Path? = when (currentPlatform) {
			LINUX -> System.getenv("XDG_DATA_HOME")?.let { Path(it, identifier, "logs") }
				?: System.getenv("HOME")?.let { Path(it, ".local", "share", identifier, "logs") }

			WINDOWS -> System.getenv("AppData")?.let { Path(it, identifier, "logs") }

			OSX -> System.getenv("HOME")?.let { Path(it, "Library", "Logs", identifier) }

			UNKNOWN -> null
		}
	}
}
