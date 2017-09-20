package com.crud.rest.configuration;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

public class CustomLogger {

	private static String logFilePath;

	public static void setLogFilePath(String logFilePathPassed) {
		logFilePath = logFilePathPassed;
	}

	public static void logInfo(String logStatement) {
		Log(LoggerType.Info, logStatement);
	}

	public static void logDebug(String logStatement) {
		Log(LoggerType.Debug, logStatement);
	}

	public static void logError(String logStatement) {
		Log(LoggerType.Error, logStatement);
	}

	public static void logWarn(String logStatement) {
		Log(LoggerType.Warn, logStatement);
	}

	public static void logFatal(String logStatement) {
		Log(LoggerType.Fatal, logStatement);
	}

	private static void Log(LoggerType loggerType, String logStatement) {

		RollingFileAppender roller = new RollingFileAppender();
		roller.setAppend(true);
		roller.setFile(logFilePath);
		roller.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p - %m%n"));
		roller.setMaxFileSize("1GB");
		roller.activateOptions();

		Logger logger = Logger.getRootLogger();
		logger.addAppender(roller);
		logger.setLevel(Level.ALL);

		System.out.println(logStatement);

		if (loggerType == LoggerType.Debug)
			logger.debug(logStatement);
		if (loggerType == LoggerType.Info)
			logger.info(logStatement);
		if (loggerType == LoggerType.Warn)
			logger.warn(logStatement);
		if (loggerType == LoggerType.Error)
			logger.error(logStatement);
		if (loggerType == LoggerType.Fatal)
			logger.fatal(logStatement);

		logger.getLoggerRepository().shutdown();
	}

	private enum LoggerType {
		Debug, Info, Warn, Error, Fatal
	}

}