package libs.libCore.steps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import libs.libCore.modules.*;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import java.io.File;

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

        String browser = Storage.get("Environment.Active.Web.browser");
        EventFiringWebDriver driver = new DriverFactory(ctx).create(browser);
        ctx.Object.put("Page", EventFiringWebDriver.class, driver);

        PageCore pageCore = new PageCore(ctx);
        ctx.Object.put("PageCore", PageCore.class, pageCore);
        PageCore = ctx.Object.get("PageCore", PageCore.class);
        Log.debug("Web driver created");
    }

    /**
     * Opens browser of particular type
     *
     * @param browser, String, describes browser type
     */
    @Given("^open browser of type (.+)$")
    public void open_browser_of_type(String browser) throws Throwable {
        Log.info("* Step started open_browser_of_type");

        EventFiringWebDriver driver = new DriverFactory(ctx).create(browser);
        ctx.Object.put("Page", EventFiringWebDriver.class, driver);

        PageCore pageCore = new PageCore(ctx);
        ctx.Object.put("PageCore", PageCore.class, pageCore);
        PageCore = ctx.Object.get("PageCore", PageCore.class);
        Log.debug("Web driver created");
    }

    /**
     * Opens jdbc connection to database
     */
    @Given("^open db$")
    public void open_db() throws Throwable {
        Log.info("* Step started open_db");

        //Connection connection = new DBConnector(ctx).create();
        //ctx.Object.put("Sql", Connection.class, connection);

        //SqlCore sqlCore = new SqlCore(ctx);
        //ctx.Object.put("SqlCore", SqlCore.class, sqlCore);
        //SqlCore = ctx.Object.get("SqlCore", SqlCore.class);
        Log.debug("Create new db connection");
        SqlCore.open();
        Log.debug("Connected to the data base");
    }

    /**
     * Opens ssh connection to remote host
     */
    @Given("^open ssh to (.+)$")
    public void open_ssh_to(String node) throws Throwable {
        Log.info("* Step started open_ssh_to");

        Log.debug("Create new ssh client");
        SshCore.createClient(node);

        //SshCore sshCore = new SshCore(ctx);
        //ctx.Object.put("SshCore", SshCore.class, sshCore);
        //SshCore = ctx.Object.get("SshCore", SshCore.class);
        Log.debug("Connected to " + node);
    }

    /**
     * Loads configuration from a particular file {}
     *
     * @param arg1, String, file path relative to features directory (shall start without separator)
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

    /**
     * Sets particular value of a key in the storage
     *
     * @param storageName, String, name of the storage
     * @param, value, String, value to be set
     */
    @And("^set (.+) in storage (.+)$")
    public void set_in_storage(String storageName, String value) throws Throwable {
        Log.info("* Step started set_in_storage");
        Storage.set(storageName, value);
        Storage.get(storageName);
    }


    @And("^write storage (.+) with id (.+) to file$")
    public void write_storage_to_file(String storageName, String StorageId) throws Throwable {
        Log.info("* Step started write_storage_to_file");
        Storage.writeToFile(storageName, StorageId);
    }


    @And("^read storage (.+) with id (.+) from file$")
    public void read_storage_to_file(String storageName, String StorageId) throws Throwable {
        Log.info("* Step started read_storage_to_file");
        Storage.readFromFile(storageName, StorageId);
    }


    @And("^pause execution$")
    public void pause_execution() throws Throwable {
        Log.info("* Step started pause execution");

        File workingDir = FileCore.createTempDir();
        String autoItPath = Storage.get("Environment.Active.apps.autoIt");
        String scriptsPath = Storage.get("Environment.Active.libCoreScripts.path");
        Integer timeout = Storage.get("Environment.Active.PauseDuration");

        String cmd = autoItPath + " " + FileCore.getProjectPath() + "\\" + scriptsPath + "\\pause.exe" + " " + Integer.toString(timeout);

        Log.debug("Calling autoIt pause script with timeout " + timeout + " seconds");

        ExecutorCore.execute(cmd, workingDir, timeout+3, true);

        Log.debug("Pause canceled or timeout. Resuming execution");
    }

}