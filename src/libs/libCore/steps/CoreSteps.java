package libs.libCore.steps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import libs.libCore.modules.*;

import java.io.File;

public class CoreSteps extends BaseSteps{

    /**
     * Opens browser of particular type as defined in the environment configuration
     */
    @Given("^open browser$")
    public void open_browser(){
        WebDriverObjectPool webDriverPool = globalCtx.get("WebDriverObjectPool", WebDriverObjectPool.class);
        String browser = Storage.get("Environment.Active.Web.browser");
        webDriverPool.checkOut(browser);
        PageCore pageCore = new PageCore();
        scenarioCtx.put("PageCore", PageCore.class, pageCore);
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
        String scriptsPath = Storage.get("Environment.Active.libCoreScripts.path");
        int timeout = Storage.get("Environment.Active.PauseDuration");

        String cmd = FileCore.getProjectPath() +
                File.separator + scriptsPath + File.separator + "pause.exe" + " " +
                timeout;

        Log.debug("Calling autoIt pause script with timeout " + timeout + " seconds");
        ExecutorCore.execute(cmd, workingDir, timeout + 1);
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
     * Masks input using an encoding algorithm so it can't be easily read in the log file
     * @param input String, input string to be encoded
     */
    @And("^encode string (.+)$")
    public void encode_string(String input) {
       String output = StepCore.encodeString(input);
        Log.debug("Encoded input is " + output);
    }


    /**
     * Opens a gui app on a windows remote host without additional arguments
     *
     * @param node String,
     * @param path String, path to the executable file
     */
    @Given("^on remote host (.+), open an app from (.+)$")
    public void on_remote_host_open_an_app_from(String node, String path) {
        String pathToApp = StepCore.checkIfInputIsVariable(path);
        Storage.set("Environment.Active.App.path", pathToApp);
        WiniumDriverObjectPool winiumDriverPool = globalCtx.get("WiniumDriverObjectPool", WiniumDriverObjectPool.class);
        winiumDriverPool.checkOut(node);
    }


    /**
     * Opens a gui app on a windows host with additional arguments
     *
     * @param path String, path to the executable file
     * @param args String, app arguments
     */
    @Given("^open an app (.+) with additional arguments (.+)$")
    public void open_an_app_with_additional_arguments(String path, String args) {
        String pathToApp = StepCore.checkIfInputIsVariable(path);
        String argsToApp = StepCore.checkIfInputIsVariable(args);
        Storage.set("Environment.Active.App.path", pathToApp);
        Storage.set("Environment.Active.App.args", argsToApp);
        WiniumDriverObjectPool winiumDriverPool = globalCtx.get("WiniumDriverObjectPool", WiniumDriverObjectPool.class);
        winiumDriverPool.checkOut("localhost");
    }


    /**
     * Opens a gui app on a windows host without additional arguments
     *
     * @param path String, path to the executable file
     */
    @Given("^open an app from (.+)$")
    public void open_an_app_from(String path) {
        String pathToApp = StepCore.checkIfInputIsVariable(path);
        Storage.set("Environment.Active.App.path", pathToApp);
        WiniumDriverObjectPool winiumDriverPool = globalCtx.get("WiniumDriverObjectPool", WiniumDriverObjectPool.class);
        winiumDriverPool.checkOut("localhost");
    }

}