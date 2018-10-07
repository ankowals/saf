package libs.libCore.steps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import libs.libCore.modules.*;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import java.io.File;

public class CoreSteps extends BaseSteps {

    /**
     * Opens browser of particular type as defined in the environment configuration
     */
    @Given("^open browser$")
    public void open_browser() {

        //Just in case Skilui is in use -> we don't want to allow to open new browser window
        //if tests are run in parallel on same host
        if ( Storage.get("Environment.Active.WebDrivers.useSikuli") ){
            StepCore.activateBrowserLock();
            StepCore.lockBrowser();
        }


        String browser = Storage.get("Environment.Active.Web.browser");
        EventFiringWebDriver driver = new DriverFactory().create(browser);
        Boolean closeWebDriver = Storage.get("Environment.Active.WebDrivers.CloseBrowserAfterScenario");
        if ( closeWebDriver ) {
            scenarioCtx.put("Page", EventFiringWebDriver.class, driver);
        } else {
            globalCtx.put("Page", EventFiringWebDriver.class, driver);
        }

        PageCore pageCore = new PageCore();
        scenarioCtx.put("PageCore", PageCore.class, pageCore);
        PageCore = scenarioCtx.get("PageCore", PageCore.class);
        Log.debug("Web driver created");
    }


    /**
     * Opens browser of particular type
     *
     * @param browser, String, describes browser type
     */
    @Given("^open browser of type (.+)$")
    public void open_browser_of_type(String browser) {

        EventFiringWebDriver driver = new DriverFactory().create(browser);
        Boolean closeWebDriver = Storage.get("Environment.Active.WebDrivers.CloseBrowserAfterScenario");
        if ( closeWebDriver ) {
            scenarioCtx.put("Page", EventFiringWebDriver.class, driver);
        } else {
            globalCtx.put("Page", EventFiringWebDriver.class, driver);
        }

        PageCore pageCore = new PageCore();
        scenarioCtx.put("PageCore", PageCore.class, pageCore);
        PageCore = scenarioCtx.get("PageCore", PageCore.class);
        Log.debug("Web driver created");
    }


    /**
     * Opens jdbc connection to database
     */
    @Given("^open db$")
    public void open_db() {
        Log.debug("Create new db connection");
        SqlCore.open();
        Log.debug("Connected to the data base");
    }


    /**
     * Opens ssh connection to remote host
     *
     * @param node String, node identifier from ssh configuration
     *
     */
    @Given("^open ssh to (.+)$")
    public void open_ssh_to(String node) {

        Log.debug("Create new ssh client");
        SshCore.createClient(node);
        Log.debug("Connected to " + node);
    }

    /**
     * Loads configuration from a particular file {}
     *
     * @param arg1, String, file path relative to features directory (shall start without separator)
     */
    @And("^load configuration data from (.*?)$")
    public void load_local_test_data(String arg1) {

        String path = FileCore.getFeaturesPath() + File.separator + arg1;
        ConfigReader Config = new ConfigReader();
        Config.create(path);

        Log.debug("Configuration from " + path + " loaded");
    }


    /**
     * Triggers macro evaluation for TestData storage and Expected data storage
     */
    @And("^evaluate macros$")
    public void eval_macro() {

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
     * @param value, String, value to be set
     */
    @And("^set (.+) to (.+)$")
    public void set_to(String storageName, String value) {

        Object val = StepCore.checkIfInputIsVariable(value);

        Storage.set(storageName, val);
        Storage.get(storageName);
    }


    /**
     * write storage content to the file and assigns an identifier to it
     *
     * @param storageName, String, name of the storage
     * @param StorageId, String, identifier to be used
     */
    @And("^write storage (.+) with id (.+) to file$")
    public void write_storage_to_file(String storageName, String StorageId) {
        Storage.writeToFile(storageName, StorageId);
    }


    /**
     * reads storage content from a file using provided identifier
     *
     * @param storageName, String, name of the storage
     * @param StorageId, String, identifier to be used
     */
    @And("^read storage (.+) with id (.+) from file$")
    public void read_storage_to_file(String storageName, String StorageId){
        Storage.readFromFile(storageName, StorageId);
    }


    /**
     * pauses scenario execution for defined amount of time<br>
     * Timeout is defined in configuration as Environment.Active.PauseDuration<br>
     * Default is 300 seconds (5 minutes)
     *
     */
    @And("^pause execution$")
    public void pause_execution() {

        File workingDir = FileCore.createTempDir();
        String autoItPath = Storage.get("Environment.Active.apps.autoIt");
        String scriptsPath = Storage.get("Environment.Active.libCoreScripts.path");
        Integer timeout = Storage.get("Environment.Active.PauseDuration");

        String cmd = autoItPath + " " + FileCore.getProjectPath() +
                File.separator + scriptsPath + File.separator + "pause.exe" + " " +
                Integer.toString(timeout);

        Log.debug("Calling autoIt pause script with timeout " + timeout + " seconds");

        ExecutorCore.execute(cmd, workingDir, timeout+3, true);

        Log.debug("Pause canceled or timeout. Resuming execution");
    }


    /**
     * waits for defined time duration
     *
     * @param seconds Integer, timeout
     *
     */
    @And("^wait for (.+) seconds$")
    public void wait_for_seconds(String seconds) {
        Integer sec = StepCore.checkIfInputIsVariable(seconds);
        StepCore.sleep(sec);
    }


    /**
     * Opens a gui app on a windows remote host without additional arguments
     *
     * @param node String,
     * @param pathToApp String, path to the executable file
     */
    @Given("^on remote host (.+) open an app from (.+)")
    public void on_remote_host_open_an_app_from(String node, String pathToApp) {
        WinRSCore.startApp(node, pathToApp, "");
    }


    /**
     * Opens a gui app on a windows host without additional arguments
     *
     * @param pathToApp String, path to the executable file
     * @param args String, app arguments
     */
    @Given("^open an app from (.+) with args (.+)")
    public void open_an_app_from(String pathToApp, String args) {
        ExecutorCore.startApp(pathToApp, args);
        StepCore.sleep(2);
    }

}