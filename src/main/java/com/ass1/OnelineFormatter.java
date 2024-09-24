
package com.ass1;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class OnelineFormatter extends Formatter {

	private String format = "[%1$tF %1$tT.%1$tL] [%2$7s] %3$s %n";

	// private String context = "";

	public OnelineFormatter(String context) {
		// this.context = context;
	}

	@Override
	public synchronized String format(LogRecord lr) {
		return String.format(format, new Date(lr.getMillis()), lr.getLevel().getLocalizedName(),
				lr.getMessage());
		// return String.format(format, new Date(lr.getMillis()),
		// lr.getLevel().getLocalizedName(), this.context,
		// lr.getMessage());
	}
}
