package dev.slimevr.ios.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;


public abstract class LoggingPrintStream extends PrintStream {
	StringBuffer st = new StringBuffer();

	//

	public LoggingPrintStream() {
		super(new OutputStream() {
			@Override
			public void write(int arg0) throws IOException {
			}
		});
	}

	public abstract void log(String text);

	public void write(char ch) {
		if (ch == 0xa) {} else
			st.append(ch);
	}

	@Override
	public void flush() {
		if (st.length() > 0) {
			log(st.toString());
			st.setLength(0);
		}
	}

	//

	@Override
	public void print(char[] s) {
		for (char ch : s)
			write(ch);
	}

	@Override
	public void print(boolean b) {
		print(b + "");
	}

	@Override
	public void print(char c) {
		write(c);
	}

	@Override
	public void print(double d) {
		print(d + "");
	}

	@Override
	public void print(float f) {
		print(f + "");
	}

	@Override
	public void print(int i) {
		print(i + "");
	}

	public void print(long l) {
		print(l + "");
	}

	@Override
	public void print(Object obj) {
		print((obj + "").toCharArray());
	}

	@Override
	public void print(String s) {
		print((s + "").toCharArray());
	}

	@Override
	public void println() {
		flush();
	}

	@Override
	public void println(boolean x) {
		print(x);
		flush();
	}

	@Override
	public void println(char x) {
		print(x);
		flush();
	}

	@Override
	public void println(char[] x) {
		print(x);
		flush();
	}

	@Override
	public void println(double x) {
		print(x);
		flush();
	}

	@Override
	public void println(float x) {
		print(x);
		flush();
	}

	@Override
	public void println(int x) {
		print(x);
		flush();
	}

	@Override
	public void println(long x) {
		print(x);
		flush();
	}

	@Override
	public void println(Object x) {
		print(x);
		flush();
	}

	@Override
	public void println(String x) {
		print(x);
		flush();
	}

}
