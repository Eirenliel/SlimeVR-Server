package io.eiren.util.logging;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LogManager {

	private static AtomicBoolean initialized = new AtomicBoolean(false);

	public static Logger global = Logger.getLogger("");
	public static final IGLog log = new DefaultGLog(global);
	public static ConsoleHandler handler;

	public static void initialize(File mainLogDir)
		throws SecurityException, IOException {
		if (initialized.getAndSet(true))
			return;
		FileLogFormatter loc = new FileLogFormatter();
		if (mainLogDir != null) {
			// Ensure the log directory exists
			if (!mainLogDir.exists())
				mainLogDir.mkdirs();

			FileLogHandler fileHandler = new FileLogHandler(
				mainLogDir.toPath(),
				"slimevr-server",
				DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"),
				25 * 1000000,
				2
			);
			fileHandler.setFormatter(loc);
			global.addHandler(fileHandler);
		}
	}

	public static void replaceMainHandler(ConsoleHandler newHandler) {
		handler.close();
		global.removeHandler(handler);
		handler = newHandler;
		global.addHandler(newHandler);
	}

	public static void addHandler(Handler add) {
		global.addHandler(add);
	}

	public static void removeHandler(Handler remove) {
		global.removeHandler(remove);
	}

	public static void enablePreciseTimestamp() {
		handler.setFormatter(new PreciseConsoleLogFormatter());
	}

	public static void info(String message) {
		log.info(message);
	}

	public static void severe(String message) {
		log.severe(message);
	}

	public static void warning(String message) {
		log.warning(message);
	}

	public static void debug(String message) {
		log.debug(message);
	}

	public static void info(String message, Throwable t) {
		log.info(message, t);
	}

	public static void severe(String message, Throwable t) {
		log.severe(message, t);
	}

	public static void warning(String message, Throwable t) {
		log.warning(message, t);
	}

	public static void debug(String message, Throwable t) {
		log.debug(message, t);
	}

	public static void log(Level level, String message) {
		log.log(level, message);
	}

	public static void log(Level level, String message, Throwable t) {
		log.log(level, message, t);
	}

	public static void closeLogger() {
		for (Handler handler : global.getHandlers()) {
			handler.close();
			removeHandler(handler);
		}
	}

	static {
		boolean hasConsoleHandler = false;
		for (Handler h : global.getHandlers()) {
			if (h instanceof ConsoleHandler) {
				handler = (ConsoleHandler) h;
				hasConsoleHandler = true;
			}
		}
		if (!hasConsoleHandler) {
			handler = new ConsoleHandler();
			global.addHandler(handler);
		}
		handler.setFormatter(new ShortConsoleLogFormatter());

		System.setOut(new PrintStream(new LoggerOutputStream(log, Level.INFO), true));
		System.setErr(new PrintStream(new LoggerOutputStream(log, Level.SEVERE), true));
	}
}
