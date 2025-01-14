package dev.slimevr.ios.logging;

import org.robovm.apple.foundation.Foundation;


public class FoundationLogPrintStream extends LoggingPrintStream {

	@Override
	public void log(String text) {
		Foundation.log(text);
	}

}
