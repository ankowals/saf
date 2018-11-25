package libs.libCore.steps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import libs.libCore.modules.*;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.io.File;

public class CoreSteps extends BaseSteps{

    /**
     * Opens browser of particular type as defined in the environment configuration
     */
    @Given("^open browser$")
    public void open_browser(){
        WebDriverObjectPool webDriverPool = globalCtx.get("WebDriverObjectPool", WebDriverObjectPool.class);
        String browser = Storage.get("Environment.Active.Web.browser");
        EventFiringWebDriver driver = webDriverPool.checkOut(browser);

        scenarioCtx.put("SeleniumWebDriver", EventFiringWebDriver.class, driver);
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
        Integer timeout = Storage.get("Environment.Active.PauseDuration");

        String cmd = FileCore.getProjectPath() +
                File.separator + scriptsPath + File.separator + "pause.exe" + " " +
                Integer.toString(timeout);

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


    @And("^encode string (.+)$")
    public void encode_string(String input) {
       String output = StepCore.encodeString(input);
        Log.debug("Encoded input is " + output);
    }


    @And("^decode string (.+)$")
    public void decode_string(String input) {
        String output = StepCore.decodeString(input);
        Log.debug("Decoded input is " + output);
    }

}