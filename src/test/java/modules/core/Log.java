package modules.core;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.Assert;

import java.io.OutputStream;

import static org.junit.Assert.fail;

public class Log {

    private static Logger Log = LogManager.getLogger(Log.class.getName());

    public static Logger getLogger() {
    	return Log;//LogManager.getLogger("file"); //used to redirect logs from RestAssured to a file log
	}

	// Need to create these methods, so that they can be called
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

    public static void addAppender(final OutputStream outputStream, final String outputStreamName) {
        LoggerContext context = LoggerContext.getContext(false);
        Configuration config = context.getConfiguration();
        PatternLayout layout = PatternLayout.newBuilder().withPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%-5level] %msg%n").build();

        Appender appender = OutputStreamAppender.createAppender(layout, null, outputStream, outputStreamName, false, true);
        appender.start();
        config.addAppender(appender);

        Level level = null;
        Filter filter = null;
        //for (LoggerConfig loggerConfig : config.getLoggers().values()) {
        //    loggerConfig.addAppender(appender, level, filter);
        //}
        config.getRootLogger().addAppender(appender, level, filter);
        context.updateLoggers();
    }

}