package libs.libCore.modules;

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

    //a class initializer used to initialize logger.
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
				.createLogger(false, Level.ALL, "libs.libCore.libs", "true", refs, null, config, null);
		loggerConfig.addAppender(appender, null, null);
		loggerConfig.addAppender(appender2, null, null);
		config.addLogger("libs.libCore.libs", loggerConfig);
		context.updateLoggers();
	}

    //assign logger to global variable Log
    private static Logger Log = LogManager.getLogger("libs.libCore.libs");


    /**
     * logs message with INFO severity
     *
     * Shall be used to indicate when new step def is started or other important action is happening
     *
     * @param message String, text to be written to the log file
     */
	public static void info(String message) {
		Log.info(message);
	}


    /**
     * logs message with WARN severity
     *
     * Shall be used to inform the user that result of some action is acceptable
     * but not necessary as expected
     *
     * @param message String, text to be written to the log file
     */
	public static void warn(String message) {
		Log.warn(message);
	}


    /**
     * logs message with ERROR severity. Marks current step and Scenario as failed.
     *
     * Shall be used every time execution of a step shall be interrupted
     *
     * @param message String, text to be written to the log file
     */
    public static void error(String message) {
		Log.error(message);
		fail(message);
	}


    /**
     * logs message with ERROR severity. Marks current step and Scenario as failed.
     *
     * Shall be used every time execution of a step shall be interrupted and error
     * re-thrown in the log file
     *
     * @param message String, text to be written to the log file
     * @param e Throwable, error received
     */
	public static void error(String message, Throwable e) {
		Log.error(message, e);
		fail(e.getMessage());
	}


    /**
     * logs message with DEBUG severity
     *
     * Shall be used to provided to the user any information that can be useful
     *
     * @param message String, text to be written to the log file
     */
    public static void debug(String message) {
		Log.debug(message);
	}
}