package steps.core;

import modules.core.*;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import ru.yandex.qatools.allure.annotations.Attachment;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class HooksSteps {

    private SharedContext ctx;
    private StepCore StepCore;
    private PageCore PageCore;
    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    // PicoContainer injects class SharedContext
    public HooksSteps(SharedContext ctx) {
        this.ctx = ctx;
    }

    @Before
    /**
     * Creates new driver and new test storage
     * Loads global and local test data/macro/results data configuration
     * Delete all cookies at the start of each scenario to avoid shared state between tests
     **/
    public void startUp(Scenario scenario) {

        //this is used to add per scenario log to report
        long logging_start = System.nanoTime();
        Log.addAppender(out,scenario.getName()+logging_start);

        String[] tId = scenario.getId().split(";");
        Log.info("*** Feature id: " + tId[0] + " ***");
        Log.info("***");
        Log.info("***");
        Log.info("*** Scenario with name: " + scenario.getName() + " started! ***");
        Log.info("***");
        Log.info("***");

        /* Global resources load */
        Log.info("Started resources initialisation");
        Log.info("<- creating shared context ->");
        ctx.Object = new Context();
        ctx.Object.put("FeatureId",String.class, tId[0]);
        Storage storage = new Storage(ctx);
        ctx.Object.put("Storage", Storage.class, storage);

        Macro macro = new Macro(ctx);
        ctx.Object.put("Macro", Macro.class, macro);

        FileCore fileCore = new FileCore(ctx);
        ctx.Object.put("FileCore", FileCore.class, fileCore);

        FileCore FileCore = ctx.Object.get("FileCore", FileCore.class);

        ExecutorCore executorCore = new ExecutorCore(ctx);
        ctx.Object.put("ExecutorCore", ExecutorCore.class, executorCore);

        AssertCore assertCore = new AssertCore(ctx);
        ctx.Object.put("AssertCore", AssertCore.class, assertCore);

        StepCore step = new StepCore(ctx);
        ctx.Object.put("StepCore", StepCore.class, step);

        Macro Macro = ctx.Object.get("Macro", Macro.class);
        StepCore = ctx.Object.get("StepCore", StepCore.class);

        Log.info("<- checking environment configuration ->");
        PropertyReader env = new PropertyReader(ctx);
        ctx.Object.put("Environment", PropertyReader.class, env);

        PropertyReader Environment = ctx.Object.get("Environment", PropertyReader.class);
        Environment.readSystemProperties();

        Log.info("<- creating test data and macro storage ->");
        ConfigReader Config = new ConfigReader(ctx);

        String globalConfigDir = FileCore.getGlobalConfigPath();
        Log.debug("Global configuration directory is " + globalConfigDir);

        ArrayList<String> globalConfigFiles = FileCore.searchForFile(globalConfigDir,".config");
        if(globalConfigFiles.size()!=0) {
            Log.debug("Following config files were found inside ");
            for (String globalConfigFile : globalConfigFiles) {
                Log.debug(globalConfigFile);
            }
            for (String globalConfigFile : globalConfigFiles) {
                Config.create(globalConfigFile);
            }
        }

        Log.info("<- configuring logger for rest operations ->");
        ToLoggerPrintStream loggerPrintStream = new ToLoggerPrintStream( Log.getLogger() );
        RestAssured.config = RestAssured.config().logConfig(
                                 new LogConfig( loggerPrintStream.getPrintStream(), true ) );
        Log.info("Finished resources initialisation");

        /* Local resources load */
        Log.info("<- Started local config load ->");
        String featureDir = FileCore.getCurrentFeatureDirPath();
        Log.debug("Feature dir is " + featureDir);
        if( featureDir != null ){
            ctx.Object.put("FeatureFileDir",String.class,featureDir);

            ArrayList<String> localConfigFiles = FileCore.searchForFile(featureDir,".config");
            if(localConfigFiles.size()!=0) {
                Log.debug("Following config files were found inside ");
                for (String localConfigFile : localConfigFiles) {
                    Log.debug(localConfigFile);
                }
                for (String localConfigFile : localConfigFiles) {
                    Config.create(localConfigFile);
                }
            }else{
                Log.warn("No local config files found!");
            }
        }

        if(Environment.readProperty("do_macro_eval_in_hooks").equalsIgnoreCase("true")){
            Log.info("<- evaluating macros ->");
            Macro.eval("TestData");
            Macro.eval("Expected");
        }

        Log.info("Test data storage is");
        Storage Storage = ctx.Object.get("Storage", Storage.class);
        Storage.print("TestData");

        Log.info("<- Finished local config load ->");
    }

    @After
    /**
     * Embed a screenshot in test report if test is marked as failed
     * Get browser/driver logs if any
     * Attach scenario log to the report
     * Close web driver and jdbc connection
     **/
    public void tearDown(Scenario scenario) {

        Log.info("*** Scenario with name: " + scenario.getName() + " ended! ***");

        EventFiringWebDriver Page = ctx.Object.get("Page", EventFiringWebDriver.class);
        if (Page != null) {
            Log.debug("Browser console logs are available below");
            for (LogEntry logEntry : Page.manage().logs().get("browser").getAll()) {
                Log.debug("" + logEntry);
            }
            Log.debug("Driver logs are available below");
            for (LogEntry logEntry : Page.manage().logs().get("driver").getAll()) {
                Log.debug("" + logEntry);
            }
        }

        Log.info("Started resources clean up");

        // Close web driver connection
        if (Page != null) {

            if(scenario.isFailed()) {
                Log.debug("Try to take a screenshot");
                PageCore = ctx.Object.get("PageCore", PageCore.class);
                byte[] screenshot = PageCore.takeScreenshot();
                String name = StringUtils.remove(scenario.getName(),"-");
                if ( name.length() > 256 ) {
                    name = name.substring(0, 255);
                }
                StepCore.attachScreenshotToReport(name,screenshot);
            }

            Log.debug("Driver cleanup started");
            Page.close();
            Page.quit();
            Log.debug("Driver cleanup done");
        }

        Connection Sql = ctx.Object.get("Sql", Connection.class);
        // Close DB connection
        if ( Sql != null ) {
            try {
                Log.debug("Db connection cleanup started");
                Sql.close();
                Log.debug("Db connection cleanup done");
            } catch (SQLException e) {
                Log.error("", e);
            }
        }

        Log.info("Finished resources clean up");

        //this is used to add per scenario log to report
        attachLogToReport(out);
        out.reset();
    }

    @Attachment(value="Log", type="text/plain")
    public byte[] attachLogToReport(ByteArrayOutputStream out){
        return out.toByteArray();
    }

}
