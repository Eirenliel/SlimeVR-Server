package dev.slimevr.ios;

import dev.slimevr.ios.logging.FoundationLogPrintStream;
import org.robovm.apple.foundation.*;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIApplicationDelegateAdapter;
import org.robovm.apple.uikit.UIApplicationLaunchOptions;
import org.robovm.apple.uikit.UIWindow;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import dev.slimevr.Keybinding;
import dev.slimevr.VRServer;
import io.eiren.util.logging.LogManager;
import org.robovm.rt.bro.ptr.BytePtr;


public class Main extends UIApplicationDelegateAdapter {
	private UIWindow window;
	private WebviewController rootViewController;

	@Override
	public boolean didFinishLaunching(
		UIApplication application,
		UIApplicationLaunchOptions launchOptions
	) {
		// Set up the view controller.
		rootViewController = new WebviewController();

		// Create a new window at screen size.
		window = new UIWindow();
		// Set the view controller as the root controller for the window.
		window.setRootViewController(rootViewController);
		// Make the window visible.
		window.makeKeyAndVisible();

		return true;
	}

	private static NSURL getAppFolder() {
		try {
			return NSFileManager
				.getDefaultManager()
				.getURLForDirectory(
					NSSearchPathDirectory.DocumentDirectory,
					NSSearchPathDomainMask.UserDomainMask,
					null,
					false
				);
		} catch (NSErrorException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getString(NSURL url) {
		var buffer = new BytePtr();
		boolean test = url.getFileSystemRepresentation(buffer, 1024L);
		if (!test)
			throw new RuntimeException("Couldn't fit URL into buffer");
		return buffer.toStringZ();
	}

	public static void main(String[] args) {
		try (NSAutoreleasePool pool = new NSAutoreleasePool()) {
			UIApplication.main(args, null, Main.class);
		}
	}

	public static void runServer() {
		var thread = new Thread(() -> {
			try {
				LogManager.initialize(new File(getString(getAppFolder())));
				System.setErr(new FoundationLogPrintStream());
				System.setOut(new FoundationLogPrintStream());
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				String sStackTrace = sw.toString();
				Foundation.log("%@\n%@", new NSString(e.toString()), new NSString(sStackTrace));
			}
			try {
				var vrServer = new VRServer(
					getString(
						getAppFolder()
							.newURLByAppendingPathComponent("vrserver.yml")
					)
				);
				vrServer.start();
				new Keybinding(vrServer);
				vrServer.join();
				LogManager.closeLogger();
				System.exit(0);
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				String sStackTrace = sw.toString();
				Foundation.log("%@\n%@", new NSString(e.toString()), new NSString(sStackTrace));
			}
		}, "SlimeVR Main Thread");
		thread.setUncaughtExceptionHandler((th, e) -> {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();
			Foundation.log("%@\n%@", new NSString(e.toString()), new NSString(sStackTrace));
		});
		thread.start();
	}
}
