package libs.libCore.modules;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import io.restassured.RestAssured;
import io.restassured.config.DecoderConfig;
import io.restassured.config.LogConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import ru.yandex.qatools.allure.annotations.Attachment;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static io.restassured.config.ConnectionConfig.connectionConfig;
import static io.restassured.config.HttpClientConfig.httpClientConfig;
import static io.restassured.config.RedirectConfig.redirectConfig;
import static io.restassured.config.SSLConfig.sslConfig;

public class HooksScenario {

    private SharedContext ctx;
    private StepCore StepCore;
    private PageCore PageCore;
    private Storage Storage;
    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    // PicoContainer injects class SharedContext
    public HooksScenario(SharedContext ctx) {
        this.ctx = ctx;
    }


    /**
     * Creates new driver and new test storage
     * Loads global and local test data/macro/results data configuration
     * Delete all cookies at the start of each scenario to avoid shared state between tests
     **/
    @Before
    public void startUp(Scenario scenario) {

        //this is used to add per scenario log to report with unique name
        long logging_start = System.nanoTime();

        //initialize Logger class, without this line log for the first scenario will not be attached
        Log.info("***");
        Log.info("***");

        //add appender to attach log for particular scenario to the report
        addAppender(out,scenario.getName()+logging_start);

        //start scenario
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

        FileCore fileCore = new FileCore(ctx);
        ctx.Object.put("FileCore", FileCore.class, fileCore);

        ConfigReader Config = new ConfigReader(ctx);
        ctx.Object.put("Config", ConfigReader.class, Config);

        Storage storage = new Storage(ctx);
        ctx.Object.put("Storage", Storage.class, storage);

        PropertyReader env = new PropertyReader(ctx);
        ctx.Object.put("Environment", PropertyReader.class, env);

        Macro macro = new Macro(ctx);
        ctx.Object.put("Macro", Macro.class, macro);

        ExecutorCore executorCore = new ExecutorCore(ctx);
        ctx.Object.put("ExecutorCore", ExecutorCore.class, executorCore);

        AssertCore assertCore = new AssertCore(ctx);
        ctx.Object.put("AssertCore", AssertCore.class, assertCore);

        PdfCore pdfCore = new PdfCore(ctx);
        ctx.Object.put("PdfCore", PdfCore.class, pdfCore);

        SshCore sshCore = new SshCore(ctx);
        ctx.Object.put("SshCore", SshCore.class, sshCore);

        WinRMCore winRmCore = new WinRMCore(ctx);
        ctx.Object.put("WinRMCore", WinRMCore.class, winRmCore);

        SqlCore sqlCore = new SqlCore(ctx);
        ctx.Object.put("SqlCore", SqlCore.class, sqlCore);

        StepCore step = new StepCore(ctx);
        ctx.Object.put("StepCore", StepCore.class, step);

        //get resources from ctx object
        FileCore FileCore = ctx.Object.get("FileCore", FileCore.class);
        Macro Macro = ctx.Object.get("Macro", Macro.class);
        StepCore = ctx.Object.get("StepCore", StepCore.class);
        Storage = ctx.Object.get("Storage", Storage.class);

        Log.info("<- reading default configuration ->");
        String defaultConfigDir = FileCore.getProjectPath() + File.separator + "libs" + File.separator + "libCore" + File.separator + "config";
        Log.debug("Default configuration directory is " + defaultConfigDir);

        ArrayList<String> defaultConfigFiles = FileCore.searchForFile(defaultConfigDir,".config");
        if(defaultConfigFiles.size()!=0) {
            for (String globalConfigFile : defaultConfigFiles) {
                Config.create(globalConfigFile);
            }
        }

        Log.info("<- reading global configuration ->");
        String globalConfigDir = FileCore.getGlobalConfigPath();
        Log.debug("Global configuration directory is " + globalConfigDir);

        ArrayList<String> globalConfigFiles = FileCore.searchForFile(globalConfigDir,".config");
        if(globalConfigFiles.size()!=0) {
            for (String globalConfigFile : globalConfigFiles) {
                Config.create(globalConfigFile);
            }
        }

        //configuring logger for rest operations
        ToLoggerPrintStream loggerPrintStream = new ToLoggerPrintStream();
        Log.info("Finished resources initialisation");

        /* Local resources load */
        Log.info("<- Started local config load ->");
        String featureDir = FileCore.getCurrentFeatureDirPath();
        Log.debug("Feature dir is " + featureDir);
        if( featureDir != null ){
            ctx.Object.put("FeatureFileDir",String.class,featureDir);

            ArrayList<String> localConfigFiles = FileCore.searchForFile(featureDir,".config");
            if(localConfigFiles.size()!=0) {
                for (String localConfigFile : localConfigFiles) {
                    Config.create(localConfigFile);
                }
            }else{
                Log.warn("No local config files found!");
            }
        }

        //all global and local configuration loaded.
        //show default config
        Log.debug("Checking default environment configuration");
        HashMap<String, Object> defaultEnvConfig = Storage.get("Environment.Default");
        HashMap<String, Object> sshConfig = Storage.get("Ssh");
        HashMap<String, Object> winRmConfig = Storage.get("WinRM");
        Map<String, Object> finalEnvConfig = Storage.get("Environment.Active");
        if ( defaultEnvConfig == null || defaultEnvConfig.size() == 0 ){
            Log.error("Default configuration Environment."
                    + " Default not found or empty. Please create it!");
        }
        if ( finalEnvConfig == null ) {
            Log.error("Environment.Active object does not exists or null."
                    + " Please create such entry in global configuration");
        }
        if ( sshConfig == null ) {
            Log.error("Ssh object does not exists or null. Please create it!");
        }
        if ( winRmConfig == null ) {
            Log.error("WinRM object does not exists or null. Please create it!");
        }
        //merge ssh with default
        defaultEnvConfig.put("Ssh", sshConfig);
        //merge winRM with default
        defaultEnvConfig.put("WinRM", winRmConfig);
        //check if cmd argument active_env was provided to overwrite active_env
        String cmd_arg  = System.getProperty("active_env");
        if ( cmd_arg != null ) {
            Log.info("Property Environment.Active.name overwritten by CMD arg -Dactive_env=" + cmd_arg);
            Storage.set("Environment.Active.name", cmd_arg);
        }
        //read name of the environment that shall be activated
        Log.debug("Checking active environment configuration");
        String actEnvName = Storage.get("Environment.Active.name");
        if ( actEnvName == null || actEnvName.equals("") || actEnvName.equalsIgnoreCase("default") ) {
            Log.debug("Environment.Active.name not set! Fallback to Environment.Default");
        } else {
            //check if config with such name exists else fallback to default
            HashMap<String, Object> activeEnvConfig = Storage.get("Environment." + actEnvName);
            if ( activeEnvConfig == null || activeEnvConfig.size() == 0 ){
                Log.error("Environment config with name " + actEnvName + " not found or empty");
            }
            //merge default and active
            deepMerge(defaultEnvConfig, activeEnvConfig);
            defaultEnvConfig = Storage.get("Environment.Default");
        }
        //create final
        deepMerge(finalEnvConfig, defaultEnvConfig);

        //check if cmd argument widthXheight was provided to overwrite active_env
        String cmd_arg2  = System.getProperty("widthXheight");
        if ( cmd_arg2 != null ) {
            Log.info("Property Environment.Active.Web.size overwritten by CMD arg -widthXheight=" + cmd_arg2);
            Storage.set("Environment.Active.Web.size", cmd_arg2);
        }

        Log.info("-- Following configuration Environment.Active is going to be used --");
        for (HashMap.Entry<String, Object> entry : finalEnvConfig.entrySet()) {
            String[] tmp = entry.getValue().getClass().getName().split(Pattern.quote(".")); // Split on period.
            String type = tmp[2];
            Log.info( "(" + type + ")" + entry.getKey() + " = " + entry.getValue() );
        }
        Log.info("-- end --");

        //adjust default RestAssured config
        Log.debug("adjusting RestAssured config");
        int maxConnections = Storage.get("Environment.Active.Rest.http_maxConnections");
        Log.debug("Setting http.maxConnections to " + maxConnections);
        System.setProperty("http.maxConnections", "" + maxConnections);

        Boolean closeIdleConnectionsAfterEachResponseAfter = Storage.get("Environment.Active.Rest.closeIdleConnectionsAfterEachResponseAfter");
        if ( closeIdleConnectionsAfterEachResponseAfter ) {
            int idleTime = Storage.get("Environment.Active.Rest.closeIdleConnectionsAfterEachResponseAfter_idleTime");
            Log.debug("Setting closeIdleConnectionsAfterEachResponseAfter=true with idleTime " + idleTime);
            RestAssured.config = RestAssured.config().connectionConfig(
                    connectionConfig().closeIdleConnectionsAfterEachResponseAfter(
                            idleTime,
                            TimeUnit.SECONDS)
            );
        }

        Boolean reuseHttpClientInstance = Storage.get("Environment.Active.Rest.reuseHttpClientInstance");
        if ( reuseHttpClientInstance ) {
            Log.debug("Setting reuseHttpClientInstance=true");
            RestAssured.config = RestAssured.config().httpClient(
                    httpClientConfig().reuseHttpClientInstance()
            );
        }

        Boolean relaxedHTTPSValidation = Storage.get("Environment.Active.Rest.relaxedHTTPSValidation");
        if ( relaxedHTTPSValidation ) {
            Log.debug("Setting relaxedHTTPSValidation=true");
            RestAssured.config = RestAssured.config().sslConfig(
                    sslConfig().relaxedHTTPSValidation()
            );
        }

        Boolean followRedirects = Storage.get("Environment.Active.Rest.followRedirects");
        if ( followRedirects != null ) {
            Log.debug("Setting followRedirects=" + followRedirects);
            RestAssured.config = RestAssured.config().redirect(
                    redirectConfig().followRedirects(followRedirects)
            );
        }

        RestAssured.config = RestAssured.config().decoderConfig(
                DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"));

        RestAssured.config = RestAssured.config().logConfig(
                new LogConfig( loggerPrintStream.getPrintStream(), true )
        );

        //check if macro evaluation shall be done in hooks
        Boolean doMacroEval = Storage.get("Environment.Active.MacroEval");
        if ( doMacroEval == null ){
            Log.error("Environment.Active.MacroEval null or empty!");
        }
        if( doMacroEval ){
            Log.info("Evaluating macros in TestData and Expected objects");
            Macro.eval("TestData");
            Macro.eval("Expected");
        }

        Log.info("Test data storage is");
        Storage.print("TestData");

        Log.info("<- Finished local config load ->");
        Log.info("***");
        Log.info("*** Running steps for scenario: " + scenario.getName());
        Log.info("***");

    }


    /**
     * Embed a screenshot in test report if test is marked as failed
     * log 3rd party exceptions
     * Get browser/driver logs if any
     * Attach scenario log to the report
     * Close web driver and jdbc connection
     **/
    @After
    public void tearDown(Scenario scenario) {

        //if present log an exception caught by junit and throw by 3rd party lib like selenium or rest assured
        String stacktrace = JunitListenerWithLogger.getStacktrace();
        if ( ! stacktrace.equals("") ) {
            //WA to not use Log.error and do not throw fail in addition
            Logger logger = LogManager.getLogger("libs.libCore.modules");
            logger.error(stacktrace);
        }

        Log.info("*** Scenario with name: " + scenario.getName() + " ended! ***");

        //get web driver
        EventFiringWebDriver Page = ctx.Object.get("Page", EventFiringWebDriver.class);
        if (Page != null) {
            if ( Page.manage().logs().get("browser").getAll().size() > 0 ) {
                Log.debug("Browser console logs are available below");
                for (LogEntry logEntry : Page.manage().logs().get("browser").getAll()) {
                    Log.debug("" + logEntry);
                }
            }
            if ( Page.manage().logs().get("driver").getAll().size() > 0 ) {
                Log.debug("Driver logs are available below");
                for (LogEntry logEntry : Page.manage().logs().get("driver").getAll()) {
                    Log.debug("" + logEntry);
                }
            }
        }

        //take screenshot if scenario fails
        if (Page != null) {
            if ( scenario.isFailed() ) {
                Log.debug("Try to take a screenshot");
                //reload page core
                try {
                    PageCore = ctx.Object.get("PageCore", PageCore.class);
                    byte[] screenshot = PageCore.takeScreenshot();
                    String name = StringUtils.remove(scenario.getName(), "-");
                    if (name.length() > 256) {
                        name = name.substring(0, 255);
                    }
                    StepCore.attachScreenshotToReport(name, screenshot);
                } catch (NullPointerException e){
                    Log.warn("Driver not usable. Can't take screenshot");
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    Log.warn(sw.toString());
                }
            }
        }

        Log.info("Started resources clean up");
        // Close web driver connection
        Boolean closeWebDriver = Storage.get("Environment.Active.WebDrivers.CloseBrowserAfterScenario");
        if ( closeWebDriver ) {
            if (Page != null) {
                Log.debug("Driver cleanup started");
                Page.close();
                Page.quit();
                Log.debug("Driver cleanup done");
            }
        }

        // Close DB connection
        SqlCore SqlCore = ctx.Object.get("SqlCore", SqlCore.class);
        SqlCore.close();

        //Close ssh connection
        SshCore SshCore = ctx.Object.get("SshCore", SshCore.class);
        SshCore.closeClient();

        //Close winRM connection
        WinRMCore WinRMCore = ctx.Object.get("WinRMCore", WinRMCore.class);
        WinRMCore.closeClient();

        Log.info("Finished resources clean up");

        //this is used to add per scenario log to the report
        attachLogToReport(out);
        out.reset();
    }

    @Attachment(value="Log", type="text/plain")
    public byte[] attachLogToReport(ByteArrayOutputStream out){
        return out.toByteArray();
    }


    /**
     * helper function used to add per scenario log to the report
     * it adds additional appender
     *
     * @param outputStream
     * @param outputStreamName
     */
    private static void addAppender(final OutputStream outputStream, final String outputStreamName) {
        LoggerContext context = LoggerContext.getContext(false);
        Configuration config = context.getConfiguration();

        PatternLayout layout = PatternLayout.newBuilder()
                .withPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%-5level] %msg%n")
                .build();

        Appender appender = OutputStreamAppender
                .createAppender(layout, null, outputStream, outputStreamName, false, true);

        appender.start();
        config.addAppender(appender);

        for (LoggerConfig loggerConfig : config.getLoggers().values()) {
            loggerConfig.addAppender(appender, null, null);
        }

        context.updateLoggers();
    }


    // This is fancier than Map.putAll(Map)
    // https://stackoverflow.com/questions/25773567/recursive-merge-of-n-level-maps
    /**
     * helper function used to merge to maps from a configuration files
     * it adds merges nested maps and tables
     *
     * @param original, Map
     * @param newMap, Map
     *
     * @return Map
     */
    private Map deepMerge(Map original, Map newMap) {
        for (Object key : newMap.keySet()) {
            if (newMap.get(key) instanceof Map && original.get(key) instanceof Map) {
                Map originalChild = (Map) original.get(key);
                Map newChild = (Map) newMap.get(key);
                original.put(key, deepMerge(originalChild, newChild));
            } else if (newMap.get(key) instanceof List && original.get(key) instanceof List) {
                List originalChild = (List) original.get(key);
                List newChild = (List) newMap.get(key);
                for (Object each : newChild) {
                    if (!originalChild.contains(each)) {
                        originalChild.add(each);
                    }
                }
            } else {
                original.put(key, newMap.get(key));
            }
        }
        return original;
    }

}
