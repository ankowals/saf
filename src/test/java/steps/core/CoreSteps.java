package steps.core;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import modules.core.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CoreSteps {

    private SharedContext ctx;

    // PicoContainer injects class SharedContext
    public CoreSteps(SharedContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Opens browser of particular type as defined in the environment configuration
     */
    @Given("^open browser$")
    public void open_browser() throws Throwable {
        Log.info("* Step started open_browser");

        ctx.driver = new DriverManger(ctx).getDriver();
        Log.debug("Web driver created");
    }

    /**
     * Loads configuration from a particular file {}
     * File path shall be relative to features directory and shall start without separator
     */
    @And("^configuration data from \"(.*?)\" is loaded$")
    public void load_local_test_data(String arg1) throws Throwable {
        Log.info("* Step started load_local_test_data");

        String path = FeatureProvider.getFeaturesPath() + File.separator + arg1;
        ctx.config.create(path);

        Log.debug("Configuration from " + path + " loaded");
    }

    /**
     * Triggers macro evaluation for TestData storage and Expected data storage
     */
    @And("^macro evaluation is done$")
    public void eval_macro() throws Throwable {
        Log.info("* Step started eval_macro");

        Log.info("<- evaluating macros ->");
        ctx.macro.eval("TestData");
        ctx.macro.eval("Expected");

        Log.debug("Test data storage after macro evaluation is");
        ctx.step.printStorageData("TestData");
    }
}