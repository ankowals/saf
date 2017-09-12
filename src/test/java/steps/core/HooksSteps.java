package steps.core;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import modules.core.*;
import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.logging.LogEntry;
import ru.yandex.qatools.allure.annotations.Attachment;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class HooksSteps {

    private SharedContext ctx;
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
        ctx.config = new ConfigReader(ctx);
        ctx.macro = new Macro(ctx);
        ctx.step = new StepUtil(ctx);
        Log.info("<- checking environment configuration ->");
        ctx.env = new PropertyReader();
        ctx.env.readSystemProperties();

        Log.info("<- creating shared context ->");
        ctx.obj = new Context();

        Log.info("<- creating test data and macro storage ->");
        String globalConfigDir = FeatureProvider.getGlobalConfigPath();
        Log.debug("Global configuration directory is " + globalConfigDir);
        ArrayList<String> globalConfigFiles = FeatureProvider.searchForFile(globalConfigDir,".config");
        if(globalConfigFiles.size()!=0) {
            Log.debug("Following config files were found inside ");
            for (String globalConfigFile : globalConfigFiles) {
                Log.debug(globalConfigFile);
            }
            for (String globalConfigFile : globalConfigFiles) {
                ctx.config.create(globalConfigFile);
            }
        }
        ctx.step.printTestData();

        Log.info("<- configuring logger for rest operations ->");
        ToLoggerPrintStream loggerPrintStream = new ToLoggerPrintStream( Log.getLogger() );
        RestAssured.config = RestAssured.config().logConfig(
                                 new LogConfig( loggerPrintStream.getPrintStream(), true ) );
        Log.info("Finished resources initialisation");

        /* Local resources load */
        Log.info("<- Started local config load ->");
        String path = FeatureProvider.getFeaturesPath();
        ArrayList<String> featurePaths = FeatureProvider.getSpecificFeature(path,tId[0], ".feature");

        if(featurePaths.size()==0){
            Log.warn("Currently used feature file path not found. Please make sure that Feature file and Feature name are same");
            Log.warn("Local config files are not going to be loaded automatically");
        }

        if (featurePaths.size()>1){
            Log.warn("Found more than 1 feature that meats criteria: name. Please fix feature files or feature names convention");
            for (String filePath : featurePaths) {
                Log.warn(filePath);
            }
            Log.warn("Local config files are not going to be loaded automatically");
        }

        if(featurePaths.size()==1){
            Log.debug("Found feature file path is " + featurePaths.get(0));
            String featureDir = FilenameUtils.getFullPathNoEndSeparator(featurePaths.get(0));
            Log.debug("Feature dir is " + featureDir);

            ArrayList<String> localConfigFiles = FeatureProvider.searchForFile(featureDir,".config");
            if(localConfigFiles.size()!=0) {
                Log.debug("Following config files were found inside ");
                for (String localConfigFile : localConfigFiles) {
                    Log.debug(localConfigFile);
                }
                for (String localConfigFile : localConfigFiles) {
                    ctx.config.create(localConfigFile);
                }
            }
        }

        if(ctx.env.readProperty("do_macro_eval_in_hooks").equalsIgnoreCase("true")){
            Log.info("<- evaluating macros ->");
            ctx.macro.eval("TestData");
        }

        Log.info("Test data storage after local config load is");
        ctx.step.printTestData();

        Log.info("<- Finished local config load ->");
    }

    @After
    /**
     * Embed a screenshot in test report if test is marked as failed
     * Get browser/driver logs is any
     * Attach scenario log to the report
     **/
    public void tearDown(Scenario scenario) {

        Log.info("*** Scenario with name: " + scenario.getName() + " ended! ***");

        if (ctx.driver != null) {
            Log.debug("Browser console logs are available below");
            for (LogEntry logEntry : ctx.driver.manage().logs().get("browser").getAll()) {
                Log.debug("" + logEntry);
            }
            Log.debug("Driver logs are available below");
            for (LogEntry logEntry : ctx.driver.manage().logs().get("driver").getAll()) {
                Log.debug("" + logEntry);
            }
        }

        Log.info("Started resources clean up");
        if (ctx.driver != null) {

            if(scenario.isFailed()) {
                Log.debug("Try to take a screenshot");
                ctx.step.attachScreenshotToReport(scenario.getName());
            }

            Log.debug("Driver cleanup started");
            ctx.driver.quit();
            Log.debug("Driver cleanup done");
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
