package com.ass1;

import java.io.IOException;
import java.util.logging.*;

public class LoggerUtil {

	private static final String LOG_DIR = "log/";

	public static Logger createLogger(String loggerName, String fileTarget, String context) {
		return LoggerUtil.createLogger(loggerName, fileTarget, context, Level.CONFIG);
	}

	public static Logger createLogger(String loggerName, String fileTarget, String context, Level level) {
		Logger logger = Logger.getLogger(loggerName);
		logger.setUseParentHandlers(false);

		Formatter fmt = new OnelineFormatter(context);

		try {
			FileHandler fileHandler = new FileHandler(LOG_DIR + fileTarget + "_" + context + ".log", true);
			fileHandler.setFormatter(fmt);
			fileHandler.setLevel(Level.ALL);

			logger.addHandler(fileHandler);
		} catch (IOException e) {
			System.err.println(
					"Failed to initialize file logger for " + loggerName + "/" + context + ": "
							+ e.getMessage());
		}

		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(fmt);
		consoleHandler.setLevel(level);

		logger.addHandler(consoleHandler);

		logger.setLevel(Level.ALL);

		return logger;
	}

	public static Logger deriveLogger(Logger logger, String context) {
		return LoggerUtil.deriveLogger(logger, context, logger.getLevel());
	}

	public static Logger deriveLogger(Logger logger, String context, Level level) {
		Logger derivedLogger = Logger.getLogger(logger.getName() + "." + context);
		derivedLogger.setUseParentHandlers(false);

		Formatter fmt = new OnelineFormatter(context);

		for (Handler handler : logger.getHandlers()) {
			handler.setFormatter(fmt);
			derivedLogger.addHandler(handler);
		}

		derivedLogger.setLevel(level);

		return derivedLogger;
	}
}
