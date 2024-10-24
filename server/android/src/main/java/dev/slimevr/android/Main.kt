@file:JvmName("Main")

package dev.slimevr.android

import android.content.Context
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import dev.slimevr.Keybinding
import dev.slimevr.VRServer
import dev.slimevr.CONFIG_FILENAME
import dev.slimevr.android.serial.AndroidSerialHandler
import io.eiren.util.logging.LogManager
import io.ktor.http.CacheControl
import io.ktor.http.CacheControl.Visibility
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.CachingOptions
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cachingheaders.CachingHeaders
import io.ktor.server.routing.routing
import java.io.File
import java.time.ZonedDateTime
import kotlin.concurrent.thread
import kotlin.system.exitProcess

lateinit var vrServer: VRServer
	private set
val vrServerInitialized: Boolean
	get() = ::vrServer.isInitialized

fun main(activity: AppCompatActivity) {
	// Host the web GUI server
	embeddedServer(Netty, port = 34536) {
		routing {
			install(CachingHeaders) {
				options { _, _ ->
					CachingOptions(CacheControl.NoStore(Visibility.Public), ZonedDateTime.now())
				}
			}
			staticResources("/", "web-gui", "index.html")
		}
	}.start(wait = false)

	thread(start = true, name = "Main VRServer Thread") {
		try {
			LogManager.initialize(activity.filesDir)
		} catch (e1: java.lang.Exception) {
			e1.printStackTrace()
		}

		try {
			vrServer = VRServer(
				configPath = File(activity.filesDir, CONFIG_FILENAME).absolutePath,
				serialHandlerProvider = { _ -> AndroidSerialHandler(activity) },
				acquireMulticastLock = {
					val wifi = activity.getSystemService(Context.WIFI_SERVICE) as WifiManager
					val lock = wifi.createMulticastLock("slimevr-jmdns-multicast-lock")
					lock.setReferenceCounted(true)
					lock.acquire()
				},
			)
			vrServer.start()
			Keybinding(vrServer)
			vrServer.join()
			LogManager.closeLogger()
			exitProcess(0)
		} catch (e: Throwable) {
			e.printStackTrace()
			exitProcess(1)
		}
	}
}
