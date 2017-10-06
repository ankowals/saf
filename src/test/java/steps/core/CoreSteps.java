package steps.core;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import modules.core.*;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.io.File;
import java.sql.Connection;

public class CoreSteps extends BaseSteps {

    // PicoContainer injects class SharedContext
    public CoreSteps(SharedContext ctx) {
        super(ctx);
    }

    /**
     * Opens browser of particular type as defined in the environment configuration
     */
    @Given("^open browser$")
    public void open_browser() throws Throwable {
        Log.info("* Step started open_browser");

        EventFiringWebDriver driver = new DriverFactory(ctx).create();
        ctx.Object.put("Page", EventFiringWebDriver.class, driver);

        PageCore pageCore = new PageCore(ctx);
        ctx.Object.put("PageCore", PageCore.class, pageCore);
        Log.debug("Web driver created");
    }

    @Given("^open db$")
    public void open_db() throws Throwable {
        Log.info("* Step started open_db");

        Connection connection = new DBConnector(ctx).create();
        ctx.Object.put("Sql", Connection.class, connection);

        SqlCore sqlCore = new SqlCore(ctx);
        ctx.Object.put("SqlCore", SqlCore.class, sqlCore);
        Log.debug("Connected to the data base");
    }

    /**
     * Loads configuration from a particular file {}
     * File path shall be relative to features directory and shall start without separator
     */
    @And("^configuration data from \"(.*?)\" is loaded$")
    public void load_local_test_data(String arg1) throws Throwable {
        Log.info("* Step started load_local_test_data");

        String path = FileCore.getFeaturesPath() + File.separator + arg1;
        ConfigReader Config = new ConfigReader(ctx);
        Config.create(path);

        Log.debug("Configuration from " + path + " loaded");
    }

    /**
     * Triggers macro evaluation for TestData storage and Expected data storage
     */
    @And("^macro evaluation is done$")
    public void eval_macro() throws Throwable {
        Log.info("* Step started eval_macro");

        Log.info("<- evaluating macros ->");
        Macro.eval("TestData");
        Macro.eval("Expected");

        Log.debug("Test data storage after macro evaluation is");
        Storage.print("TestData");
    }

    @And("^set (.+) in storage (.+)$")
    public void set_in_storage(String storageName, String value) throws Throwable {
        Log.info("* Step started set_in_storage");
        Storage.set(storageName, value);
        Storage.get(storageName);
    }

}