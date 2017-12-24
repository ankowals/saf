package libs.libCore.modules;

import cucumber.api.java.Before;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.io.PrintWriter;
import java.io.StringWriter;

public class HooksGlobal {

    private SharedContext ctx;

    // PicoContainer injects class SharedContext
    public HooksGlobal(SharedContext ctx) {
        this.ctx = ctx;
    }

    private static boolean dunit = false;

    @Before(order=10)
    public void beforeAll() {
        if( ! dunit ) {

            //afterAll hook
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {

                    // do the afterAll stuff...
                    Log.info("*** All features executed");
                    Storage Storage = ctx.Object.get("Storage", Storage.class);
                    Boolean closeWebDriver = Storage.get("Environment.Active.WebDrivers.CloseBrowserAfterScenario");
                    if ( ! closeWebDriver ) {
                        EventFiringWebDriver Page = ctx.Object.get("Page", EventFiringWebDriver.class);
                        if ( Page != null ) {
                            Log.debug("Driver cleanup started");
                            Page.close();
                            Page.quit();
                            Log.debug("Driver cleanup done");
                        }
                    }



                    Log.info("Test Suite execution ENDED!");
                }
            });

            //beforeAll hook
            // do the beforeAll stuff...
            Log.info("Test Suite execution STARTED!");
            PropertyReader.readSystemProperties();
            Log.info("*************************");
            Log.info("*** Running features *** ");
            Log.info("*************************");

            //redirect StdOut and StdErr to the logger so we can catch logs written by other tools
            System.setOut(
                    IoBuilder.forLogger(LogManager.getLogger("libs.libCore.modules"))
                            .setLevel(Level.DEBUG).setBuffered(false)
                            .buildPrintStream()
            );
            System.setErr(
                    IoBuilder.forLogger(LogManager.getLogger("libs.libCore.modules"))
                            .setLevel(Level.WARN).setBuffered(false)
                            .buildPrintStream()
            );

            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread t, Throwable e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String stacktrace = sw.toString();
                    //WA to not use Log.error and do not throw fail in addition
                    Logger logger = LogManager.getLogger("libs.libCore.modules");
                    logger.error(stacktrace);
                }
            });

            //
            dunit = true;
        }
    }
}
