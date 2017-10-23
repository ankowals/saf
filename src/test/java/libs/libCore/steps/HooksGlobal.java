package libs.libCore.steps;

import cucumber.api.java.Before;
import libs.libCore.modules.Log;
import libs.libCore.modules.PropertyReader;
import libs.libCore.modules.SharedContext;
import libs.libCore.modules.Storage;
import org.openqa.selenium.support.events.EventFiringWebDriver;

public class HooksGlobal {

    private SharedContext ctx;

    // PicoContainer injects class SharedContext
    public HooksGlobal(SharedContext ctx) {
        this.ctx = ctx;
    }

    //https://automationpanda.com/2017/03/03/cucumber-jvm-global-hook-workarounds/
    //https://zsoltfabok.com/blog/2012/09/cucumber-jvm-hooks/
    //https://github.com/cucumber/cucumber-jvm/issues/515
    //https://github.com/cucumber/cucumber-jvm/tree/master/examples/java-webbit-websockets-selenium/src/test/java/cucumber/examples/java/websockets
    private static boolean dunit = false;

    @Before
    public void beforeAll() {
        if(!dunit) {

            //after All hook
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {

                    // do the afterAll stuff...
                    Log.info("Executing global afterAll hook");
                    EventFiringWebDriver Page = ctx.Object.get("Page", EventFiringWebDriver.class);
                    Storage Storage = ctx.Object.get("Storage", Storage.class);
                    Boolean closeWebDriver = Storage.get("Environment.Active.WebDrivers.CloseBrowserAfterScenario");
                    if ( ! closeWebDriver ) {
                        if (Page != null) {
                            Log.debug("Driver cleanup started");
                            Page.close();
                            Page.quit();
                            Log.debug("Driver cleanup done");
                        }
                    }

                    Log.info("Global after all hook executed");
                    Log.info("Test Suite execution ENDED!");
                }
            });

            //before All hook
            // do the beforeAll stuff...
            Log.info("Executing global beforeAll hook");
            PropertyReader.readSystemProperties();
            Log.info("Global before all hook executed");
            Log.info("Test Suite execution STARTED!");

            //
            dunit = true;
        }
    }
}
