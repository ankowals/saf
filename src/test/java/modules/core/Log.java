package modules.core;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.status.StatusLogger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.fail;

public class Log {

	static {

		//turn off warning related to missing configuration
		StatusLogger.getLogger().setLevel(Level.OFF);

		//configure a layout and appender programmatically
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
		String pattern = LocalDateTime.now().format(formatter);

		LoggerContext context = LoggerContext.getContext(false);
		Configuration config = context.getConfiguration();

		PatternLayout layout = PatternLayout.newBuilder()
				.withConfiguration(config)
				.withPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%-5level] %msg%n")
				.build();

		Appender appender = FileAppender.newBuilder()
				.setConfiguration(config)
				.withName("File-Appender")
				.withLayout(layout)
				.withFileName("target/"+pattern+"_FK_Prototype.log")
				.build();

		Appender appender2 = ConsoleAppender.newBuilder()
				.setConfiguration(config)
				.withName("Console-Appender")
				.withLayout(layout)
				.build();

		appender.start();
		appender2.start();
		config.addAppender(appender);
		config.addAppender(appender2);

		//define a logger, associate the appender to it, and update the configuration
		AppenderRef ref = AppenderRef.createAppenderRef("File-Appender", null, null);
		AppenderRef ref2 = AppenderRef.createAppenderRef("Console-Appender", null, null);
		AppenderRef[] refs = new AppenderRef[] { ref, ref2 };

		LoggerConfig loggerConfig = LoggerConfig
				.createLogger(false, Level.ALL, "modules.core", "true", refs, null, config, null);
		loggerConfig.addAppender(appender, null, null);
		loggerConfig.addAppender(appender2, null, null);
		config.addLogger("modules.core", loggerConfig);
		context.updateLoggers();
	}

    private static Logger Log = LogManager.getLogger("modules.core");

	public static void info(String message) {
		Log.info(message);
	}

	public static void warn(String message) {
		Log.warn(message);
	}

	public static void error(String message) {
		Log.error(message);
		fail(message);
	}

	public static void error(String message, Throwable e) {
		Log.error(message, e);
		fail(e.getMessage());
	}

	public static void debug(String message) {
		Log.debug(message);
	}
}