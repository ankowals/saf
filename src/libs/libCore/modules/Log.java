package libs.libCore.modules;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.io.IoBuilder;
import org.apache.logging.log4j.status.StatusLogger;
import static org.testng.Assert.fail;

public class Log {

    //a class initializer used to initialize logger.
	static {
		//turn off warning related to missing configuration
		StatusLogger.getLogger().setLevel(Level.OFF);

		//required otherwise child threads will not be logged correctly in the scenario logs
        //for example web driver will be created in a child thread of scenario thread
		System.setProperty("isThreadContextMapInheritable","true");

		//redirect StdOut and StdErr to the logger so we can catch logs written by other tools like Selenium, RestAssured etc.
		System.setOut(
				IoBuilder.forLogger(LogManager.getLogger("libs.libCore.modules"))
						.setLevel(Level.DEBUG).buildPrintStream()
		);
		System.setErr(IoBuilder.forLogger(LogManager.getLogger("libs.libCore.modules"))
				.setLevel(Level.WARN).buildPrintStream()
		);
	}

    //assign logger to global variable Log
    private static Logger Log = LogManager.getLogger("libs.libCore.modules");


    /**
     * logs message with WARN severity
     *
     * Shall be used to inform the user that result of some action is acceptable
     * but not necessary as expected
     *
     * @param message String, text to be written to the log file
     */
	public static void warn(String message) {
		String threadId = String.valueOf(Thread.currentThread().getId());
		ThreadContext.put("TId", threadId);
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
		String threadId = String.valueOf(Thread.currentThread().getId());
		ThreadContext.put("TId", threadId);
		fail(message);
	}


    /**
     * logs message with DEBUG severity
     *
     * Shall be used to provided to the user any information that can be useful
     *
     * @param message String, text to be written to the log file
     */
    public static void debug(String message) {
		String threadId = String.valueOf(Thread.currentThread().getId());
		ThreadContext.put("TId", threadId);
    	Log.debug(message);
	}
}