package libs.libCore.modules;

import cucumber.api.PickleStepTestStep;
import cucumber.api.Result;
import cucumber.api.event.*;
import cucumber.api.event.EventHandler;
import cucumber.api.formatter.Formatter;
import io.qameta.allure.Attachment;
import io.restassured.RestAssured;
import io.restassured.config.DecoderConfig;
import io.restassured.config.LogConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.io.IoBuilder;
import org.apache.logging.log4j.status.StatusLogger;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.winium.WiniumDriver;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.config.ConnectionConfig.connectionConfig;
import static io.restassured.config.HttpClientConfig.httpClientConfig;
import static io.restassured.config.RedirectConfig.redirectConfig;
import static io.restassured.config.SSLConfig.sslConfig;

@SuppressWarnings("unchecked")
public class CustomFormatter implements Formatter {

    static {
        StatusLogger.getLogger().setLevel(Level.OFF);
    }
    private static Logger Log = LogManager.getLogger("libs.libCore.modules");

    private StepCore StepCore;
    private PageCore PageCore;
    private Storage Storage;

    private EventHandler<TestStepFinished> stepFinishedHandler = new EventHandler<TestStepFinished>() {
        @Override
        public void receive(TestStepFinished event) {
            if ( event.result.getErrorMessage() != null ){
                Log.error(event.result.getErrorMessage());
            }
            handleTestStepFinished(event);
        }
    };

    private EventHandler<TestStepStarted> stepStartedHandler = new EventHandler<TestStepStarted>() {
        @Override
        public void receive(TestStepStarted event) {
            handleTestStepStarted(event);
        }
    };

    private EventHandler<TestCaseStarted> scenarioStartedHandler = new EventHandler<TestCaseStarted>() {
        @Override
        public void receive(TestCaseStarted event) {
            handleTestCaseStarted(event);
        }
    };

    private EventHandler<TestCaseFinished> scenarioFinishedHandler = new EventHandler<TestCaseFinished>() {
        @Override
        public void receive(TestCaseFinished event) {
            handleTestCaseFinished(event);
        }
    };

    private EventHandler<TestRunStarted> runStartedHandler = new EventHandler<TestRunStarted>() {
        @Override
        public void receive(TestRunStarted event) {
            handleTestRunStarted(event);
        }
    };

    private EventHandler<TestRunFinished> runFinishedHandler = new EventHandler<TestRunFinished>() {
        @Override
        public void receive(TestRunFinished event) {
            handleTestRunFinished(event);
        }
    };

    @Override
    public void setEventPublisher(EventPublisher publisher){
        publisher.registerHandlerFor(TestStepStarted.class, stepStartedHandler);
        publisher.registerHandlerFor(TestStepFinished.class, stepFinishedHandler);
        publisher.registerHandlerFor(TestCaseStarted.class, scenarioStartedHandler);
        publisher.registerHandlerFor(TestCaseFinished.class, scenarioFinishedHandler);
        publisher.registerHandlerFor(TestRunStarted.class, runStartedHandler);
        publisher.registerHandlerFor(TestRunFinished.class, runFinishedHandler);
    }

    private void handleTestRunStarted(TestRunStarted event){

        PropertyReader.readSystemProperties();
        Log.info("");
        Log.info("");
        Log.info("Test Suite execution STARTED!");

        //redirect StdOut and StdErr to the logger so we can catch logs written by other tools
        System.setOut(
                IoBuilder.forLogger(LogManager.getLogger("libs.libCore.modules"))
                        .setLevel(Level.DEBUG).buildPrintStream()
        );
        System.setErr(IoBuilder.forLogger(LogManager.getLogger("libs.libCore.modules"))
                .setLevel(Level.WARN).buildPrintStream()
        );

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String stacktrace = sw.toString();
                //WA to not use Log.error and do not throw fail in addition
                Logger logger = LogManager.getLogger("libs.libCore.modules");
                logger.error(stacktrace);
            }
        });

        ThreadContext.initializeContext("Global");

        Log.info("");
        Log.info("");
        Log.info("+-------------------------------------------------------------+");
        Log.info("*** Running features *** ");
        Log.info("+-------------------------------------------------------------+");
        Log.info("");

    }

    private void handleTestRunFinished(TestRunFinished event){

        Log.info("");
        Log.info("+-------------------------------------------------------------+");
        Log.info("*** All features executed ***");
        Log.info("+-------------------------------------------------------------+");
        Log.info("");

        Context threadCtx = ThreadContext.getContext("Global");
        EventFiringWebDriver Page = threadCtx.get("Page", EventFiringWebDriver.class);
        if ( Page != null ) {
            Log.debug("Driver cleanup started");
            Page.close();
            Page.quit();
            Log.debug("Driver cleanup done");
        }

        //close Winium driver connection
        WiniumDriver App = threadCtx.get("App", WiniumDriver.class);
        if ( App != null ) {
            Log.debug("Driver cleanup started");
            Log.debug("Try to close an application");
            try {
                App.close();
            } catch (WebDriverException e){
                Log.warn("Application is already closed");
            }
            Log.debug("Driver cleanup done");
        }


    }

    private void handleTestStepStarted(TestStepStarted event){
        if(event.testStep instanceof PickleStepTestStep){
            PickleStepTestStep ev = (PickleStepTestStep)event.testStep;
            Log.info("");
            Log.info("+-------------------------------------------------------------+");
            Log.info("* Step started " + ev.getStepText());
            Log.info("+-------------------------------------------------------------+");
            Log.info("");
        }

        Context scenarioCtx = ThreadContext.getContext("Scenario");
        Integer stepCounter = scenarioCtx.get("ScenarioStepsCounter", Integer.class);
        stepCounter++;
        scenarioCtx.put("ScenarioStepsCounter", Integer.class, stepCounter);

    }

    private void handleTestStepFinished(TestStepFinished event){
        Context scenarioCtx = ThreadContext.getContext("Scenario");
        String testCaseName = scenarioCtx.get("ScenarioName", String.class);
        Integer scenarioStepsListSize = scenarioCtx.get("scenarioStepsListSize", Integer.class);
        Integer scenarioStepsCounter = scenarioCtx.get("ScenarioStepsCounter", Integer.class);

        //detect last step of a scenario
        //it has to be used instead of TestCaseFinished event because otherwise scenario log
        //will not be properly attached to the report
        if ( scenarioStepsCounter.equals(scenarioStepsListSize) ){
            Log.info("");
            Log.info("+-------------------------------------------------------------+");
            Log.info("*** Scenario with name: " + testCaseName + " ended! ***");
            Log.info("+-------------------------------------------------------------+");
            Log.info("");

            //get web driver
            EventFiringWebDriver Page = scenarioCtx.get("Page", EventFiringWebDriver.class);
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
                if ( event.result.is(Result.Type.FAILED) ) {
                    Log.debug("Try to take a screenshot");
                    //reload page core
                    try {
                        PageCore = scenarioCtx.get("PageCore", PageCore.class);
                        byte[] screenshot = PageCore.takeScreenshot();
                        String name = StringUtils.remove(testCaseName, "-");
                        if (name.length() > 256) {
                            name = name.substring(0, 255);
                        }
                        StepCore.attachScreenshotToReport(name, screenshot);
                    } catch ( NullPointerException e ){
                        Log.warn("Driver not usable. Can't take screenshot");
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        Log.warn(sw.toString());
                    }
                }
            }

            //get Winium driver
            WiniumDriver App = scenarioCtx.get("App", WiniumDriver.class);
            //take screenshot if scenario fails
            if (App != null) {
                //this may fail if app open one a remote host and we try to take screenshot even though RDP session is not open
                if ( event.result.is(Result.Type.FAILED) ) {
                    Log.debug("Try to take a screenshot");
                    //reload winium core
                    try {
                        WiniumCore WiniumCore = new WiniumCore();
                        byte[] screenshot = WiniumCore.takeScreenshot();
                        String name = StringUtils.remove(testCaseName, "-");
                        if (name.length() > 256) {
                            name = name.substring(0, 255);
                        }
                        StepCore.attachScreenshotToReport(name, screenshot);
                    } catch ( NullPointerException | WebDriverException e ){
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

            //close Winium driver connection
            Boolean closeWiniumAppDriver = Storage.get("Environment.Active.WebDrivers.WiniumDesktop.CloseAppAfterScenario");
            if ( closeWiniumAppDriver ) {
                Log.debug("Driver cleanup started");
                if ( App != null ) {
                    Log.debug("Try to close an application");
                    try {
                        App.close();
                        scenarioCtx.put("App", WiniumDriver.class, null);
                    } catch (WebDriverException e){
                        Log.warn("Application is already closed");
                        scenarioCtx.put("App", WiniumDriver.class, null);
                    }
                }

                WinRSCore WinRSCore = scenarioCtx.get("WinRSCore", WinRSCore.class);
                WinRSCore.closeWiniumResources();
                Log.debug("Driver cleanup done");
            }

            // Close DB connection
            SqlCore SqlCore = scenarioCtx.get("SqlCore", SqlCore.class);
            SqlCore.close();

            //Close ssh connection
            SshCore SshCore = scenarioCtx.get("SshCore", SshCore.class);
            SshCore.closeClient();

            //Close winRM connection
            WinRMCore WinRMCore = scenarioCtx.get("WinRMCore", WinRMCore.class);
            WinRMCore.closeClient();

            Log.info("Finished resources clean up");
            //this is used to add per scenario log to the report
            ByteArrayOutputStream out = scenarioCtx.get("ScenarioLogAppender", ByteArrayOutputStream.class);
            attachLogToReport(out);
        }

    }

    private void handleTestCaseStarted(TestCaseStarted event) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        //this is used to add per scenario log to report with unique name
        long logging_start = System.nanoTime();

        //initialize Logger class, without this line log for the first scenario will not be attached
        Log.info("");

        //add appender to attach log for particular scenario to the report
        addAppender(out, event.testCase.getName() + logging_start);

        String fileContent = "";
        try {
            fileContent = new String(Files.readAllBytes((new File(event.testCase.getUri())).toPath()), Charset.forName("UTF-8"))
                    .trim()
                    .replaceAll("(?m)^#.*$", "");
        } catch (IOException e){
            Log.error("", e);
        }

        Pattern pattern = Pattern.compile("(?im)^Feature:(.*?)$");
        Matcher matcher = pattern.matcher(fileContent);

        String featureName = "";
        int count = 0;
        while (matcher.find()){
            count++;
            featureName = matcher.group(1).trim();
        }

        if ( count > 1){
            Log.error("More than 1 feature defined in a feature file " + event.testCase.getUri() + "!");
        }

        Log.info("+-------------------------------------------------------------+");
        Log.info("*** Feature id: " + event.testCase.getUri() + " ***");
        Log.info("+-------------------------------------------------------------+");
        Log.info("");
        Log.info("+-------------------------------------------------------------+");
        Log.info("*** Feature with name: " +  featureName + " started! ***");
        Log.info("+-------------------------------------------------------------+");
        Log.info("");
        Log.info("");

        //start scenario
        Log.info("");
        Log.info("");
        Log.info("+-------------------------------------------------------------+");
        Log.info("*** Scenario with name: " + event.testCase.getName() + " started! ***");
        Log.info("+-------------------------------------------------------------+");
        Log.info("");
        Log.info("");

        //initialize scenario context
        ThreadContext.initializeContext("Scenario");
        Context scenarioCtx = ThreadContext.getContext("Scenario");

        //intialize step counter
        scenarioCtx.put("ScenarioStepsCounter", Integer.class, 0);

        // put feature file uri to context so it can be passed to FileCore
        scenarioCtx.put("FeatureUri", String.class, event.testCase.getUri());

        //put scenario name into context so it can be used in test step finished event
        scenarioCtx.put("ScenarioName", String.class, event.testCase.getName());

        //extract test steps so we can detect last step of scenario and attach scenario log
        Integer scenarioStepsListSize = event.testCase.getTestSteps().size();
        scenarioCtx.put("scenarioStepsListSize", Integer.class, scenarioStepsListSize);

        /* Global resources load */
        Log.info("Started resources initialisation");

        FileCore fileCore = new FileCore();
        scenarioCtx.put("FileCore", FileCore.class, fileCore);

        ConfigReader Config = new ConfigReader();
        scenarioCtx.put("Config", ConfigReader.class, Config);

        Storage storage = new Storage();
        scenarioCtx.put("Storage", Storage.class, storage);

        PropertyReader env = new PropertyReader();
        scenarioCtx.put("Environment", PropertyReader.class, env);

        Macro macro = new Macro();
        scenarioCtx.put("Macro", Macro.class, macro);

        ExecutorCore executorCore = new ExecutorCore();
        scenarioCtx.put("ExecutorCore", ExecutorCore.class, executorCore);

        AssertCore assertCore = new AssertCore();
        scenarioCtx.put("AssertCore", AssertCore.class, assertCore);

        PdfCore pdfCore = new PdfCore();
        scenarioCtx.put("PdfCore", PdfCore.class, pdfCore);

        SshCore sshCore = new SshCore();
        scenarioCtx.put("SshCore", SshCore.class, sshCore);

        WinRMCore winRmCore = new WinRMCore();
        scenarioCtx.put("WinRMCore", WinRMCore.class, winRmCore);

        SqlCore sqlCore = new SqlCore();
        scenarioCtx.put("SqlCore", SqlCore.class, sqlCore);

        StepCore step = new StepCore();
        scenarioCtx.put("StepCore", StepCore.class, step);

        CloudDirectorCore cloudDirectorCore = new CloudDirectorCore();
        scenarioCtx.put("CloudDirectorCore", CloudDirectorCore.class, cloudDirectorCore);

        WinRSCore winRSCore = new WinRSCore();
        scenarioCtx.put("WinRSCore", WinRSCore.class, winRSCore);

        //get resources from ctx object
        FileCore FileCore = scenarioCtx.get("FileCore", FileCore.class);
        Macro Macro = scenarioCtx.get("Macro", Macro.class);
        StepCore = scenarioCtx.get("StepCore", StepCore.class);
        Storage = scenarioCtx.get("Storage", Storage.class);

        Log.info("<- reading default configuration ->");
        String defaultConfigDir = FileCore.getProjectPath() + File.separator + "libs" + File.separator + "libCore" + File.separator + "config";
        Log.debug("Default configuration directory is " + defaultConfigDir);

        ArrayList<String> defaultConfigFiles = FileCore.searchForFile(defaultConfigDir,".config");
        if(defaultConfigFiles.size()!=0) {
            for (String configFile : defaultConfigFiles) {
                Config.create(configFile);
            }
        }

        Log.info("<- reading global configuration ->");
        String globalConfigDir = FileCore.getGlobalConfigPath();
        Log.debug("Global configuration directory is " + globalConfigDir);

        Boolean useProjectConfig = Storage.get("Environment.Default.UseProjectConfig");
        if ( useProjectConfig ){
            Log.debug("Looking for a project.config file");
            String projFilePath = globalConfigDir + File.separator + "project.config";
            File projFile = new File(projFilePath);
            if (projFile.exists() && !projFile.isDirectory()) {
                Log.debug("Reading project global configuration from " + projFilePath);
                Config.create(projFilePath);
            } else {
                Log.error(projFilePath + " does not exists!");
            }
        } else {
            Log.warn("Project.config usage switched off. Going to read global configuration in alphabetical order");
            ArrayList<String> globalConfigFiles = FileCore.searchForFile(globalConfigDir, ".config");
            if (globalConfigFiles.size() != 0) {
                for (String configFile : globalConfigFiles) {
                    Config.create(configFile);
                }
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
            scenarioCtx.put("FeatureFileDir", String.class, featureDir);

            ArrayList<String> localConfigFiles = FileCore.searchForFile(featureDir,".config");
            if( localConfigFiles.size()!= 0 ) {
                for ( String configFile : localConfigFiles ) {
                    Config.create(configFile);
                }
            }else{
                Log.warn("No local config files found!");
            }
        }

        //all global and local configuration loaded.
        //prepare environment config
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
        String cmd_arg  = System.getProperty("ctx.Environment.Active.name");
        if ( cmd_arg != null ) {
            Log.info("Property Environment.Active.name overwritten by CMD arg -Dctx.Environment.Active.name=" + cmd_arg);
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

        //
        // Use with caution! inappropriate usage may cause run time exception
        //
        Log.debug("Checking provided command line switches");
        Properties props = System.getProperties();
        Set<Object> propsSet = props.keySet();
        Integer nrOfSwitches = 0;
        for(Object key : propsSet ){
            if ( key.toString().contains("ctx.TestData.") ||
                    key.toString().contains("ctx.Environment.") ||
                    key.toString().contains("ctx.Expected.") ){

                Log.debug("Trying to overwrite value of " + key.toString().substring(4,key.toString().length()) + " due to usage of command line switch -D" + key.toString());

                Object obj = StepCore.checkIfInputIsVariable(props.get(key.toString()).toString());
                Log.debug("Class of " + key.toString().substring(4,key.toString().length()) + " is " + obj.getClass().getName());

                Storage.set(key.toString().substring(4,key.toString().length()), obj);
                nrOfSwitches++;
            }
        }
        if ( nrOfSwitches == 0 ){
            Log.warn("No command line switches found");
        }
        //
        //
        //

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

        Integer responseTimeout = Storage.get("Environment.Active.Rest.responseTimeout");
        if ( responseTimeout != null ){
            Log.debug("Setting CoreConnectionPNames.CONNECTION_TIMEOUT and CoreConnectionPNames.SO_TIMEOUT");
            RestAssured.config = RestAssured.config().httpClient(
                    httpClientConfig().setParam("http.connection.timeout", responseTimeout * 1000)
            );
            RestAssured.config = RestAssured.config().httpClient(
                    httpClientConfig().setParam("http.socket.timeout", responseTimeout * 1000)
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

        //evaluate macros
        if( doMacroEval ){
            Log.info("Evaluating macros in TestData and Expected objects");
            Macro.eval("TestData");
            Macro.eval("Expected");
        }

        //allow to use values from one entity in other entities of Storage
        evaluateConfigEntities(Storage.get("Environment.Active"));
        evaluateConfigEntities(Storage.get("TestData"));
        evaluateConfigEntities(Storage.get("Expected"));

        //add information about used test environment to the report only if environment.properties file does not exist
        String targetDirPath = FileCore.getProjectPath().substring(0, FileCore.getProjectPath().length() - 14) + File.separator + "target";
        File allureEnvironment = new File(targetDirPath + File.separator + "allure-results" + File.separator + "environment.properties");

        if ( ! Files.exists(allureEnvironment.toPath()) ) {
            Log.info("Adding environment information to the report");
            //allow to use config entities concatenation in storage, evaluate them and change to final value
            //evaluateConfigEntities(Storage.get("Environment.Active.WriteToReport"));

            //create properties list from a hashmap
            //give possibility to write each line of a list as a separate property???
            List<String> lines = new ArrayList<>();
            HashMap<String, String> envPropMap = Storage.get("Environment.Active.WriteToReport");
            for (Map.Entry<String, String> entry : envPropMap.entrySet()) {
                //do not display keys with empty values in the report
                if ( ! entry.getValue().equals("") ) {
                    lines.add(entry.getKey() + "=" + entry.getValue().replace("\\", "\\\\"));
                }
            }

            if ( lines.size() > 0 ) {
                File allureResultsDir = new File(targetDirPath + File.separator + "allure-results");
                //create allure-results directory if not exists yet
                if ( ! Files.exists(allureResultsDir.toPath()) ) {
                    try {
                        Files.createDirectory(allureResultsDir.toPath());
                    } catch (IOException e) {
                        Log.error("", e);
                    }
                }

                //write lines into environment.properties file
                try {
                    Files.write(allureEnvironment.toPath(), lines);
                } catch (IOException e) {
                    Log.error("", e);
                }
            }
        }

        //add links to issues and tests into to the report only if allure.properties file does not exist
        File allureProperties = new File(targetDirPath + File.separator + "allure-results" + File.separator + "allure.properties");
        if ( ! Files.exists(allureProperties.toPath()) ) {
            Log.info("Adding issue tracker and test tracker information to the report");
            List<String> lines = new ArrayList<>();
            String issueTrackerUrlPattern = Storage.get("Environment.Active.IssueTrackerUrlPattern");
            String testTrackerUrlPattern = Storage.get("Environment.Active.TestTrackerUrlPattern");
            lines.add("allure.results.directory=target/allure-results");
            lines.add("allure.link.tms.pattern=" + testTrackerUrlPattern.trim() + "{}");
            lines.add("allure.link.issue.pattern=" + issueTrackerUrlPattern.trim() + "{}");

            if ( lines.size() > 0 ) {
                File allureResultsDir = new File(targetDirPath + File.separator + "allure-results");
                //create allure-results directory if not exists yet
                if ( ! Files.exists(allureResultsDir.toPath()) ) {
                    try {
                        Files.createDirectory(allureResultsDir.toPath());
                    } catch (IOException e) {
                        Log.error("", e);
                    }
                }

                //write lines into environment.properties file
                try {
                    Files.write(allureProperties.toPath(), lines);
                } catch (IOException e) {
                    Log.error("", e);
                }
            }
        }

        //print storage
        Log.info("-- Following configuration Environment.Active is going to be used --");
        for (HashMap.Entry<String, Object> entry : finalEnvConfig.entrySet()) {
            String[] tmp = entry.getValue().getClass().getName().split(Pattern.quote(".")); // Split on period.
            String type = tmp[2];
            Log.info( "(" + type + ")" + entry.getKey() + " = " + entry.getValue() );
        }
        Log.info("-- end --");

        //Log.info("Test data storage is");
        Storage.print("TestData");

        Log.info("<- Finished local config load ->");
        Log.info("");
        Log.info("+-------------------------------------------------------------+");
        Log.info("*** Running steps for scenario: " + event.testCase.getName());
        Log.info("+-------------------------------------------------------------+");
        Log.info("");

        scenarioCtx.put("ScenarioLogAppender", ByteArrayOutputStream.class, out);

    }

    private void handleTestCaseFinished(TestCaseFinished event){
        Context scenarioCtx = ThreadContext.getContext("Scenario");
        //this is used to add per scenario log to the report
        ByteArrayOutputStream out = scenarioCtx.get("ScenarioLogAppender", ByteArrayOutputStream.class);
        out.reset();

        ThreadContext.removeContext("Scenario");
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


    /**
     * helper function used to merge maps from a configuration files
     * it merges nested maps and tables by doing so called deep merge
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


    /**
     * helper function used to substitute values in a storage that contains keys to other attributes
     * Values that contain ${ctx. string will be subject of substitution
     *
     * @param map, Map
     *
     */
    private void evaluateConfigEntities (HashMap<String, Object> map) {
        for (HashMap.Entry<String, Object> entry : map.entrySet()){

            if ( entry.getValue() instanceof HashMap ) {
                evaluateConfigEntities((HashMap) entry.getValue());
            } else if ( entry.getValue() instanceof ArrayList ) {
                for (int i=0; i < ((ArrayList) entry.getValue()).size(); i++) {
                    if ( ((ArrayList) entry.getValue()).get(i).getClass().getName().contains("String") ) {
                        String tmp = (String) ((ArrayList) entry.getValue()).get(i);
                        if ( tmp.contains("${ctx.") ) {
                            String newVal = replaceInString (tmp);
                            ((ArrayList) entry.getValue()).set(i, newVal);
                        }
                    } else if (((ArrayList) entry.getValue()).get(i) instanceof HashMap) {
                        evaluateConfigEntities( (HashMap<String, Object>) ((ArrayList) entry.getValue()).get(i) );
                    }
                }
            } else {
                if (entry.getValue().getClass().getName().contains("String")) {
                    String tmp = (String) entry.getValue();
                    if ( tmp.contains("${ctx.") ) {
                        String newVal = replaceInString (tmp);
                        map.put(entry.getKey(), newVal);
                    }
                }
            }
        }
    }


    /**
     * helper function used replace config value in a Storage
     *
     * @param input, String
     *
     */
    private String replaceInString (String input) {
        //Log.debug("Input is " + input);
        Integer beignIdx = input.indexOf("${");
        Integer endIdx = input.indexOf("}", beignIdx);

        if (beignIdx != -1) {
            if ( endIdx == -1 ){
                Log.error("Typo in config value " + input + "! Missing closing bracket }. Can't do variable substitution!");
            }

            String toReplace = input.substring(beignIdx+2, endIdx);
            String toCheck = toReplace;
            if ( toReplace.startsWith("ctx.") ){
                toCheck = toReplace.substring(4);
            }
            String result = StepCore.checkIfInputIsVariable(toCheck).toString();

            if (  ! toReplace.equals("ctx." + result) ) {
                return replaceInString(input.replace("${" + toReplace + "}", result));
            }
        }

        return input;
    }


}