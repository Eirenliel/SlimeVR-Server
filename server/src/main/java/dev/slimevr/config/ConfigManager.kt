package dev.slimevr.config

import com.charleskorn.kaml.Yaml
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.github.jonpeterson.jackson.module.versioning.VersioningModule
import dev.slimevr.config.serializers.QuaternionDeserializer
import dev.slimevr.config.serializers.QuaternionSerializer
import io.eiren.util.ann.ThreadSafe
import io.eiren.util.logging.LogManager
import io.github.axisangles.ktmath.Quaternion
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import okio.FileSystem
import okio.Path.Companion.toPath
import java.io.FileNotFoundException
import java.io.IOException

class ConfigManager(private val configPath: String) {
	private val om: ObjectMapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.SPLIT_LINES))
	lateinit var vrConfig: VRConfig
		private set

	init {
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		om.registerModule(VersioningModule())
		val quaternionModule = SimpleModule()
		quaternionModule.addSerializer(Quaternion::class.java, QuaternionSerializer())
		quaternionModule.addDeserializer(Quaternion::class.java, QuaternionDeserializer())
		om.registerModule(quaternionModule)
	}

	fun loadConfig() {
		vrConfig = VRConfig()

		try {
			FileSystem.SYSTEM.read(configPath.toPath()) {
				val config = readUtf8()
				vrConfig = Yaml.default.decodeFromString(config)
			}
		} catch (e: FileNotFoundException) {
			// Config file didn't exist, is not an error
		} catch (e: IOException) {
			// Log the exception
			LogManager.severe("Config failed to load: $e")
			// Make a backup of the erroneous config
			backupConfig()
		}
	}

	fun backupConfig() {
		val cfgFile = configPath.toPath()
		val tmpBakCfgFile = "$configPath.bak.tmp".toPath()
		val bakCfgFile = "$configPath.bak".toPath()
		try {
			FileSystem.SYSTEM.copy(cfgFile, tmpBakCfgFile)
			LogManager.info("Made a backup copy of config to \"$tmpBakCfgFile\"")
		} catch (e: IOException) {
			LogManager.severe(
				"Unable to make backup copy of config from \"$cfgFile\" to \"$tmpBakCfgFile\"",
				e
			)
			return // Abort write
		}
		try {
			FileSystem.SYSTEM.atomicMove(tmpBakCfgFile, bakCfgFile)
		} catch (e: IOException) {
			LogManager.severe(
				"Unable to move backup config from \"$tmpBakCfgFile\" to \"$bakCfgFile\"",
				e
			)
		}
	}

	@ThreadSafe
	@Synchronized
	fun saveConfig() {
		val tmpCfgFile = "$configPath.tmp".toPath()
		val cfgFile = configPath.toPath()

		// Serialize config
		try {
			FileSystem.SYSTEM.write(tmpCfgFile) {
				writeUtf8(Yaml.default.encodeToString(vrConfig))
			}
		} catch (e: IOException) {
			LogManager.severe("Unable to write serialized config to \"$tmpCfgFile\"", e)
			return // Abort write
		}

		// Overwrite old config
		try {
			FileSystem.SYSTEM.atomicMove(tmpCfgFile, cfgFile)
		} catch (e: IOException) {
			LogManager.severe(
				"Unable to move new config from \"$tmpCfgFile\" to \"$cfgFile\"",
				e
			)
		}
	}
}
