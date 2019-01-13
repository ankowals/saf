package libs.libCore.modules;

import com.rits.cloning.Cloner;
import cucumber.api.PickleStepTestStep;
import cucumber.api.event.*;
import cucumber.api.event.EventHandler;
import io.restassured.RestAssured;
import io.restassured.config.DecoderConfig;
import io.restassured.config.LogConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static io.restassured.config.ConnectionConfig.connectionConfig;
import static io.restassured.config.HttpClientConfig.httpClientConfig;
import static io.restassured.config.RedirectConfig.redirectConfig;

@SuppressWarnings("unchecked")
public class CustomEventListener implements ConcurrentEventListener {

    public CustomEventListener(){
        //required otherwise child threads will not be logged correctly in the scenario logs
        //for example web driver will be created in a child thread of scenario thread
        System.setProperty("isThreadContextMapInheritable","true");
    }

    private EventHandler<TestStepFinished> stepFinishedHandler = new EventHandler<TestStepFinished>() {
        @Override
        public void receive(TestStepFinished event) {
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
        Logger logger = LogManager.getLogger("libs.libCore.modules");
        logger.info("+----------------------------+");
        logger.info("+--- Features run started ---+");
        logger.info("+----------------------------+");

        readSystemProperties();

        Log.debug("Creating global context");
        Context globalCtx = GlobalCtxSingleton.getInstance();

        //creating scenario context pool
        ScenarioCtxObjectPool scenarioCtxPool = new ScenarioCtxObjectPool();
        globalCtx.put("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class, scenarioCtxPool);

        //creating web driver pool
        WebDriverObjectPool webDriverPool = new WebDriverObjectPool();
        globalCtx.put("WebDriverObjectPool", WebDriverObjectPool.class, webDriverPool);

        //creating ssh client pool
        SshClientObjectPool sshClientObjectPool = new SshClientObjectPool();
        globalCtx.put("SshClientObjectPool", SshClientObjectPool.class, sshClientObjectPool);

        //creating jdbc driver pool
        JdbcDriverObjectPool jdbcDriverObjectPool = new JdbcDriverObjectPool();
        globalCtx.put("JdbcDriverObjectPool", JdbcDriverObjectPool.class, jdbcDriverObjectPool);

        WiniumDriverObjectPool winiumDriverPool = new WiniumDriverObjectPool();
        globalCtx.put("WiniumDriverObjectPool", WiniumDriverObjectPool.class, winiumDriverPool);

        //read default and global configuration once
        FileCore fileCore = new FileCore();
        String projPath = fileCore.getProjectPath();

        ConfigReader configReader = new ConfigReader(globalCtx, projPath);

        Storage storage = new Storage(globalCtx, fileCore, configReader);

        Log.debug("Reading libCore default configuration");
        String defaultConfigDir = projPath + File.separator + "libs" + File.separator + "libCore" + File.separator + "config";
        Log.debug("Default configuration directory is " + defaultConfigDir);
        readConfigFiles(fileCore, defaultConfigDir, configReader);

        Log.debug("Reading global project configuration");
        String globalConfigDir = projPath + File.separator + "config";
        String projFilePath = globalConfigDir + File.separator + "project.config";
        File projFile = new File(projFilePath);
        if (projFile.exists() && !projFile.isDirectory()) {
            Log.debug("Reading project global configuration from " + projFilePath);
            configReader.create(projFilePath);
        } else {
            Log.error(projFilePath + " does not exists!");
        }

        //setting default RestAssured config it can be overwritten later on in TestCaseStarted event only if modified
        Log.debug("Adjusting RestAssured config");
        adjustRestAssuredConfig(storage, "Default");
        HashMap<String, Object> restAssuredDefaultConfiguration = storage.get("Environment.Default.Rest");

        //pass deep clone of a default and global configuration to each scenario
        HashMap<String, Object> tMap = new HashMap<>();
        for (String key : globalCtx.internalMap.keySet()) {
            if ( !key.endsWith("ObjectPool") ){
                tMap.putIfAbsent(key, globalCtx.internalMap.get(key).get(HashMap.class));
            }
        }

        globalCtx.put("ProjectDefaultConfiguration", HashMap.class, tMap);
        globalCtx.put("RestAssuredDefaultConfiguration", HashMap.class, restAssuredDefaultConfiguration);
    }

    private void handleTestRunFinished(TestRunFinished event){
        Logger logger = LogManager.getLogger("libs.libCore.modules");
        logger.info("+------------------------------+");
        logger.info("+--- All scenarios executed ---+");
        logger.info("+------------------------------+");

        Log.debug("Cleaning up global resources");
        Context globalCtx = GlobalCtxSingleton.getInstance();

        //closing all web drivers in the web driver pool
        Log.debug("Closing web drivers");
        WebDriverObjectPool webDriverPool = globalCtx.get("WebDriverObjectPool", WebDriverObjectPool.class);
        webDriverPool.closeAll();

        //closing all ssh clients in the ssh client pool
        Log.debug("Closing ssh clients");
        SshClientObjectPool sshClientObjectPool = globalCtx.get("SshClientObjectPool", SshClientObjectPool.class);
        sshClientObjectPool.closeAll();

        //closing all jdbc drivers in the jdbc driver pool
        Log.debug("Closing jdbc connections");
        JdbcDriverObjectPool jdbcDriverObjectPool = globalCtx.get("JdbcDriverObjectPool", JdbcDriverObjectPool.class);
        jdbcDriverObjectPool.closeAll();

        //closing all winium drivers in the winium driver pool
        Log.debug("Closing winium drivers");
        WiniumDriverObjectPool winiumDriverPool = globalCtx.get("WiniumDriverObjectPool", WiniumDriverObjectPool.class);
        winiumDriverPool.closeAll();

        logger.info("+-----------------------------+");
        logger.info("+--- Features run finished ---+");
        logger.info("+-----------------------------+");
        Log.debug("");
        Log.debug("");
        Log.debug("");
    }

    private void handleTestStepStarted(TestStepStarted event){
        Logger logger = LogManager.getLogger("libs.libCore.modules");
        if(event.testStep instanceof PickleStepTestStep) {
            PickleStepTestStep ev = (PickleStepTestStep) event.testStep;
            String stepFiller = StringUtils.repeat("-", ev.getStepText().length());
            String threadFiller = StringUtils.repeat("-", Long.toString(Thread.currentThread().getId()).length());
            logger.info("+-----------------" + stepFiller + "------------------" + threadFiller + "-----+");
            logger.info("+--- Step started " + ev.getStepText() + " in thread with id " + Thread.currentThread().getId() +  " ---+");
            logger.info("+-----------------" + stepFiller + "------------------" + threadFiller + "-----+");
        }

        Context globalCtx = GlobalCtxSingleton.getInstance();
        ScenarioCtxObjectPool scenarioCtxPool = globalCtx.get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class);
        Context scenarioCtx = scenarioCtxPool.checkOut();

        Integer stepCounter = scenarioCtx.get("ScenarioStepsCounter", Integer.class);
        stepCounter++;
        scenarioCtx.put("ScenarioStepsCounter", Integer.class, stepCounter);
    }

    private void handleTestStepFinished(TestStepFinished event){
        Logger logger = LogManager.getLogger("libs.libCore.modules");
        if ( event.result.getErrorMessage() != null ){
            logger.error(event.result.getErrorMessage());
        }

        String stepFiller = StringUtils.repeat("-", event.result.getStatus().toString().length());
        logger.info("+---------------------------" + stepFiller + "----+");
        logger.info("+--- Step ended with status " + event.result.getStatus() + " ---+");
        logger.info("+---------------------------" + stepFiller + "----+");

        Context globalCtx = GlobalCtxSingleton.getInstance();
        ScenarioCtxObjectPool scenarioCtxPool = globalCtx.get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class);
        Context scenarioCtx = scenarioCtxPool.checkOut();

        Integer scenarioStepsListSize = scenarioCtx.get("scenarioStepsListSize", Integer.class);
        Integer scenarioStepsCounter = scenarioCtx.get("ScenarioStepsCounter", Integer.class);

        if ( scenarioStepsCounter.equals(scenarioStepsListSize) ){
            String testCaseName = scenarioCtx.get("ScenarioName", String.class);

            //in case scenario has failed or was successfully executed clean up scenario resources
            //and attach log to the report

            String scenarioFiller = StringUtils.repeat("-", testCaseName.length());
            logger.info("+-----------------------" + scenarioFiller + "----------+");
            logger.info("+--- Scenario with name " + testCaseName + " ended ---+");
            logger.info("+-----------------------" + scenarioFiller + "----------+");

            Storage storage = scenarioCtx.get("Storage", Storage.class);
            StepCore stepCore = scenarioCtx.get("StepCore", StepCore.class);
            FileCore fileCore = scenarioCtx.get("FileCore", FileCore.class);

            //USE WITH CAUTION!!!
            //execute custom logic at the end of the scenario
            //can be used to for example to attach some logs to a report etc. even if scenario was unsuccessful
            //avoid doing any validation checks here because error will not be thrown!
            String className = storage.get("Environment.Default.Plugins.handleTestStepFinished");
            if ( className != null && !className.equals("") ){

                // Create a new ClassLoader
                ClassLoader classLoader = ClassLoader.getSystemClassLoader();

                try {
                    // Load the target class using its name
                    Class aClass = classLoader.loadClass(className);
                    logger.info("Plugin detected! Loading " + aClass.getName() + " class!");

                    // Create a new instance from the loaded class
                    Constructor constructor = aClass.getConstructor();
                    Object aClassObject = constructor.newInstance();

                    // Getting the target method from the loaded class and invoke it using its name
                    Method method = aClass.getMethod("load");
                    logger.info("Invoking method with name " + method.getName());
                    method.invoke(aClassObject);
                } catch (Exception e){
                    logger.error(e.getMessage());
                }
            }

            Log.debug("Cleaning up scenario resources");

            //close web driver and take screenshot in case scenario has failed
            //has to be done here otherwise screenshot will not be attached to allure report
            Log.debug("Returning web driver to the pool");
            WebDriverObjectPool webDriverPool = globalCtx.get("WebDriverObjectPool", WebDriverObjectPool.class);
            boolean closeAfterScenario = storage.get("Environment.Active.WebDrivers.CloseBrowserAfterScenario");
            if (!closeAfterScenario) {
                webDriverPool.checkInAllPerThread(event, testCaseName, stepCore);
            } else {
                Log.debug("Closing web browser");
                webDriverPool.closeAllPerThread(event, testCaseName, stepCore);
            }

            //return all ssh client used by particular thread to the pool
            Log.debug("Returning ssh clients to the pool");
            SshClientObjectPool sshClientObjectPool = globalCtx.get("SshClientObjectPool", SshClientObjectPool.class);
            sshClientObjectPool.checkInAllPerThread();

            //return all jdbc drivers used by particular thread to the pool
            Log.debug("Returning jdbc connections to the pool");
            JdbcDriverObjectPool jdbcDriverObjectPool = globalCtx.get("JdbcDriverObjectPool", JdbcDriverObjectPool.class);
            jdbcDriverObjectPool.checkInAllPerThread();

            //close winium driver and take screenshot in case scenario has failed
            //has to be done here otherwise screenshot will not be attached to allure report
            Log.debug("Returning winium driver to the pool");
            WiniumDriverObjectPool winiumDriverPool = globalCtx.get("WiniumDriverObjectPool", WiniumDriverObjectPool.class);
            closeAfterScenario = storage.get("Environment.Active.WebDrivers.WiniumDesktop.CloseAppAfterScenario");
            if ( !closeAfterScenario ) {
                winiumDriverPool.checkInAllPerThread(event, testCaseName, stepCore);
            } else {
                winiumDriverPool.closeAllPerThread(event, testCaseName, stepCore);
            }

            //update allure properties to give possibility to overwrite them during test execution
            String targetDirPath = fileCore.getProjectPath().replaceAll("src$", "target");
            updateAllureProperties(storage, targetDirPath);

            //attach log file to allure report
            Log.debug("Removing scenario context");
            Log.debug("Attaching scenario log to the report");
            stepCore.attachFileToReport("Log", "text/plain", scenarioCtx.get("ScenarioLogFileName", String.class));
        }

    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        Logger logger = LogManager.getLogger("libs.libCore.modules");
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String stacktrace = sw.toString();
                //WA to not use Log.error and do not throw fail in addition
                logger.error(stacktrace);
            }
        });

        long threadId = Thread.currentThread().getId();

        //prepare log dir file name
        String featurePath = event.testCase.getUri();
        String featureFileName = createLogDirName(featurePath);

        //this is used to create log dir per feature and log file per scenario in a feature
        //file name + its path can't be longer than 257 characters (MAX_PATH under win 260 but 3 chars are gone for drive letter)
        String logFileName =  createLogFileName(threadId, event);

        //pass log dir name and log file name to the Log4j2 context
        ThreadContext.put("logFileName", logFileName);
        ThreadContext.put("logDirName", featureFileName);

        //let's make it look nice in the log file
        int idx = featurePath.indexOf("src/features/");
        if ( idx == -1 ){
            Log.error("Feature path not parsable! Please check directory structure!");
        }
        String relativeFeaturePath = featurePath.substring(idx+1);
        if ( ! relativeFeaturePath.startsWith("s") ){
            relativeFeaturePath = featurePath.substring(idx);
        }

        //verify that there is only 1 feature in a feature file
        verifyOnlyOneFeatureExistPerFile(featurePath);

        String scenarioFiller = StringUtils.repeat("-", event.testCase.getName().length());
        String featureFiller = StringUtils.repeat("-", (relativeFeaturePath).length());

        //start scenario
        logger.info("+---------------" + featureFiller + "----+");
        logger.info("+--- Feature id " + relativeFeaturePath + " ---+");
        logger.info("+---------------" + featureFiller + "----+");
        Log.debug("***");
        String threadFiller = StringUtils.repeat("-", Long.toString(threadId).length());
        logger.info("+----------------------" + scenarioFiller + "----------------------------" + threadFiller + "----+");
        logger.info("+--- Scenario with name " + event.testCase.getName() + " started in thread with id " + threadId + " ---+");
        logger.info("+----------------------" + scenarioFiller + "----------------------------" + threadFiller + "----+");

        Log.debug("Started scenario resources initialisation");
        Context globalCtx = GlobalCtxSingleton.getInstance();
        ScenarioCtxObjectPool scenarioCtxPool = globalCtx.get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class);
        Context scenarioCtx = scenarioCtxPool.checkOut();

        //get project global configuration
        HashMap<String, Object> tMap = globalCtx.get("ProjectDefaultConfiguration", HashMap.class);

        //make a deep copy of global config and put each key and value paris into scenario context
        Cloner cloner = new Cloner();
        HashMap<String, Object> globalConfig = cloner.deepClone(tMap);

        //we can change global config per scenario because deep instead of shallow copy is in use
        //cast of object to hashmap has to be done here
        for (Map.Entry<String, Object> entry : globalConfig.entrySet()) {
            scenarioCtx.put(entry.getKey(), HashMap.class, (HashMap)entry.getValue());
        }

        //pass path to the feature file to fileCore
        scenarioCtx.put("FeatureUri", String.class, featurePath);
        //initialize step counter
        scenarioCtx.put("ScenarioStepsCounter", Integer.class, 0);
        //put scenario name into context so it can be used in test step finished event
        scenarioCtx.put("ScenarioName", String.class, event.testCase.getName());
        //extract test steps so we can detect last step of scenario and attach scenario log to the report
        Integer scenarioStepsListSize = event.testCase.getTestSteps().size();
        scenarioCtx.put("scenarioStepsListSize", Integer.class, scenarioStepsListSize);

        FileCore fileCore = new FileCore();
        scenarioCtx.put("FileCore", FileCore.class, fileCore);

        ConfigReader configReader = new ConfigReader(scenarioCtx, fileCore.getProjectPath());
        scenarioCtx.put("ConfigReader", ConfigReader.class, configReader);

        Storage storage = new Storage(scenarioCtx, fileCore, configReader);
        scenarioCtx.put("Storage", Storage.class, storage);

        Macro macro = new Macro();
        scenarioCtx.put("Macro", Macro.class, macro);

        ExecutorCore executorCore = new ExecutorCore();
        scenarioCtx.put("ExecutorCore", ExecutorCore.class, executorCore);

        CsvCore csvCore = new CsvCore();
        scenarioCtx.put("CsvCore", CsvCore.class, csvCore);

        RestCore restCore = new RestCore();
        scenarioCtx.put("RestCore", RestCore.class, restCore);

        PdfCore pdfCore = new PdfCore();
        scenarioCtx.put("PdfCore", PdfCore.class, pdfCore);

        SshCore sshCore = new SshCore();
        scenarioCtx.put("SshCore", SshCore.class, sshCore);

        SqlCore sqlCore = new SqlCore();
        scenarioCtx.put("SqlCore", SqlCore.class, sqlCore);

        StepCore stepCore = new StepCore();
        scenarioCtx.put("StepCore", StepCore.class, stepCore);

        CloudDirectorCore cloudDirectorCore = new CloudDirectorCore();
        scenarioCtx.put("CloudDirectorCore", CloudDirectorCore.class, cloudDirectorCore);

        WinRSCore winRSCore = new WinRSCore();
        scenarioCtx.put("WinRSCore", WinRSCore.class, winRSCore);

        //PageCore is created when new driver is instantiated via WebDriverFactory
        //WiniumCore is created when new driver is instantiated via WiniumDriverFactory

        Log.debug("Finished scenario resources initialisation");

        Log.debug("Reading local configuration");
        String featureDir = fileCore.getCurrentFeatureDirPath();
        Log.debug("Feature dir is " + featureDir);
        if( featureDir != null ){
            scenarioCtx.put("FeatureFileDir", String.class, featureDir);
            readConfigFiles(fileCore, featureDir, configReader);
        }

        Log.debug("Creating active environment configuration");
        Map<String, Object> finalEnvConfig = storage.get("Environment.Active");
        createActiveEnvironmentConfig(finalEnvConfig, storage, configReader);

        // Use with caution! inappropriate usage may cause run time exception
        Log.debug("Checking provided command line switches");
        overwriteStorageDataUsingCmdSwitch(stepCore, storage);

        //checking if RestAssured configuration was modified by local config
        Log.debug("Checking if default RestAssured config shall be adjusted");
        HashMap<String, Object> defaultRestAssuredConfig = globalCtx.get("RestAssuredDefaultConfiguration", HashMap.class);
        HashMap<String, Object> scenarioRestAssuredConfig = storage.get("Environment.Active.Rest");

        if ( ! defaultRestAssuredConfig.equals(scenarioRestAssuredConfig) ){
            Log.debug("Adjusting RestAssured config due to changes done via local config");
            adjustRestAssuredConfig(storage, "Active");
        }

        //check if macro evaluation shall be done in hooks
        Boolean doMacroEval = storage.get("Environment.Active.MacroEval");
        if( doMacroEval ) {
            Log.debug("Evaluating macros in TestData object");
            macro.eval("TestData");
            Log.debug("Evaluating macros in Expected object");
            macro.eval("Expected");
            Log.debug("Evaluating macros in Expected object");
            macro.eval("Environment");
        }

        //allow to use values from one entity in other entities of Storage
        evaluateConfigEntities(storage.get("TestData"), stepCore);
        evaluateConfigEntities(storage.get("Expected"), stepCore);
        evaluateConfigEntities(storage.get("Environment.Active"), stepCore);

        //print storage
        Log.debug("*** Following TestData configuration is going to be used");
        storage.print("TestData");
        Log.debug("*** Following Expected configuration is going to be used");
        storage.print("Expected");
        Log.debug("*** Following configuration Environment.Active is going to be used ***");
        storage.print("Environment.Active");

        //update allure properties
        String targetDirPath = fileCore.getProjectPath().replaceAll("src$", "target");
        updateAllureProperties(storage, targetDirPath);

        logger.info("+-------------------------------" + scenarioFiller + "-------------------" + threadFiller + "----+");
        logger.info("+--- Running steps for scenario " + event.testCase.getName() + " in thread with id " + threadId + " ---+");
        logger.info("+-------------------------------" + scenarioFiller + "-------------------" + threadFiller + "----+");
        scenarioCtx.put("ScenarioLogFileName", String.class, fileCore.getProjectPath().replaceAll("src$","target")
                + File.separator + "logs"
                + File.separator + featureFileName
                + File.separator + logFileName + ".log");
    }

    private void handleTestCaseFinished(TestCaseFinished event){
        ThreadContext.remove("logFileName");
        ThreadContext.remove("logDirName");
        Context globalCtx = GlobalCtxSingleton.getInstance();
        ScenarioCtxObjectPool scenarioCtxPool = globalCtx.get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class);
        scenarioCtxPool.checkIn();
    }


    private String createLogFileName(Long threadId, TestCaseStarted event){
        int availableLength = 69 - Long.toString(System.nanoTime()).length() - Long.toString(threadId).length() - 3;
        int scenarioNameLength = event.testCase.getName().replaceAll("\\s+", "_").length();

        //create log file name
        String logFileName = event.testCase.getName().replaceAll("\\s+", "_") + "_" + System.nanoTime() +
                "_" + threadId;
        if ( availableLength < scenarioNameLength  ){
            logFileName = event.testCase.getName().replaceAll("\\s+", "_").substring(0, availableLength)
                    + "_" + System.nanoTime() + "_" + threadId;
        }

        return logFileName.replaceAll("[^A-Za-z0-9_]","");
    }

    private String createLogDirName(String featurePath){
        String featureFileName = new File(featurePath).getName().replaceAll("\\s+", "_")
                .replaceAll("[^\\w\\s]","").replace("feature","");
        int maxLength = (featureFileName.length() < 50)?featureFileName.length():50;
        return featureFileName.substring(0, maxLength).replaceAll("[^A-Za-z0-9_]","");
    }

    private void readSystemProperties(){
        Properties p = System.getProperties();
        Log.debug("System properties");
        Log.debug("os.arch=" + p.get("os.arch"));
        Log.debug("os.name=" + p.get("os.name"));
        Log.debug("user.name=" + p.get("user.name"));
        Log.debug("user.home=" + p.get("user.home"));
        Log.debug("user.dir=" + p.get("user.dir"));
        Log.debug("user.timezone=" + p.get("user.timezone"));
        Log.debug("java.runtime.name=" + p.get("java.runtime.name"));
        Log.debug("java.version=" + p.get("java.version"));
        Log.debug("java.vm.version=" + p.get("java.vm.version"));
        Log.debug("java.io.tmpdir=" + p.get("java.io.tmpdir"));
        Log.debug("java.home=" + p.get("java.home"));
    }

    private void readConfigFiles(FileCore fileCore, String dir, ConfigReader configReader){
        List<String> configFiles = fileCore.searchForFile(dir,".config");
        if(configFiles.size()!=0) {
            for (String configFile : configFiles) {
                configReader.create(configFile);
            }
        }else{
            Log.warn("No config files found in " + dir + "!");
        }
    }


    /**
     * helper function used to substitute values in a storage that contains keys to other attributes
     * Values that contain ${ctx. string will be subject of substitution
     *
     * @param map, Map
     *
     */
    private void evaluateConfigEntities (Map<String, Object> map, StepCore stepCore) {
        for (Map.Entry<String, Object> entry : map.entrySet()){

            if ( entry.getValue() instanceof Map ) {
                evaluateConfigEntities((Map) entry.getValue(), stepCore);
            } else if ( entry.getValue() instanceof List ) {
                for (int i=0; i < ((List) entry.getValue()).size(); i++) {
                    if ( ((List) entry.getValue()).get(i).getClass().getName().contains("String") ) {
                        String tmp = (String) ((List) entry.getValue()).get(i);
                        if ( tmp.contains("${ctx.") || tmp.contains("${mcr.") ) {
                            String newVal = stepCore.replaceInString(tmp);
                            ((List) entry.getValue()).set(i, newVal);
                        }
                    } else if (((List) entry.getValue()).get(i) instanceof Map) {
                        evaluateConfigEntities((Map<String, Object>) ((List) entry.getValue()).get(i), stepCore);
                    }
                }
            } else {
                if (entry.getValue().getClass().getName().contains("String")) {
                    String tmp = (String) entry.getValue();
                    if ( tmp.contains("${ctx.") || tmp.contains("${mcr.") ) {
                        String newVal = stepCore.replaceInString(tmp);
                        map.put(entry.getKey(), newVal);
                    }
                }
            }
        }
    }

    private synchronized void updateAllureProperties(Storage storage, String targetDirPath){
        File allureEnvironment = new File(targetDirPath + File.separator + "allure-results" + File.separator + "environment.properties");
        File allureProperties = new File(targetDirPath + File.separator + "allure-results" + File.separator + "allure.properties");
        File allureResultsDir = new File(targetDirPath + File.separator + "allure-results");

        //add information about used test environment to the report
        if ( ! Files.exists(allureEnvironment.toPath()) ) {
            Log.debug("Adding environment information to the report");
            Map<String, String> environmentProperties = storage.get("Environment.Active.WriteToReport");
            writeAllurePropertiesToFile(allureEnvironment, allureResultsDir, environmentProperties);
        }

        //add links to issues and tests into to the report
        if ( ! Files.exists(allureProperties.toPath()) ) {
            Log.debug("Adding issue tracker and test tracker information to the report");
            String issueTrackerUrlPattern = storage.get("Environment.Active.IssueTrackerUrlPattern");
            String testTrackerUrlPattern = storage.get("Environment.Active.TestTrackerUrlPattern");
            Map<String, String> trackerProperties = new HashMap<>();
            trackerProperties.put("allure.results.directory", "target" + File.separator + "allure-results");
            trackerProperties.put("allure.link.tms.pattern", testTrackerUrlPattern.trim() + "{}");
            trackerProperties.put("allure.link.issue.pattern", issueTrackerUrlPattern.trim() + "{}");
            writeAllurePropertiesToFile(allureProperties, allureResultsDir, trackerProperties);
        }
    }

    private void writeAllurePropertiesToFile(File targetFile, File allureResultsDir, Map<String, String> propertiesMap){
        //create properties list from a hashmap
        List<String> lines = new ArrayList<>();
        if (propertiesMap == null){
            Log.error("Allure properties map null or does not exists!");
        }

        for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
            //do not display keys with empty values in the report
            if ( ! entry.getValue().equals("") ) {
                lines.add(entry.getKey() + "=" + entry.getValue().replace("\\", "\\\\"));
            }
        }

        if ( lines.size() > 0 ) {
            //create allure-results directory if not exists yet
            if ( ! Files.exists(allureResultsDir.toPath()) ) {
                try {
                    Files.createDirectory(allureResultsDir.toPath());
                    Log.debug("Allure results directory " + allureResultsDir.getAbsolutePath() + " created");
                } catch (IOException e) {
                    Log.error(e.getMessage());
                }
            }

            //write lines into environment.properties or allure properties file
            try {
                Files.write(targetFile.toPath(), lines);
                Log.debug("Allure properties file " + targetFile.getAbsolutePath() + " modified");
            } catch (IOException e) {
                Log.error(e.getMessage());
            }
        }

    }

    private void overwriteStorageDataUsingCmdSwitch(StepCore stepCore, Storage storage){
        Properties props = System.getProperties();
        Set<Object> propsSet = props.keySet();
        int nrOfSwitches = 0;
        for(Object key : propsSet ){
            if ( key.toString().contains("ctx.TestData.") ||
                    key.toString().contains("ctx.Environment.") ||
                    key.toString().contains("ctx.Expected.") ){

                Log.debug("Trying to overwrite value of " + key.toString().substring(4,key.toString().length()) + " due to usage of command line switch -D" + key.toString());

                Object obj = stepCore.checkIfInputIsVariable(props.get(key.toString()).toString());
                Log.debug("Class of " + key.toString().substring(4,key.toString().length()) + " is " + obj.getClass().getName());

                storage.set(key.toString().substring(4,key.toString().length()), obj);
                nrOfSwitches++;
            }
        }
        if ( nrOfSwitches == 0 ){
            Log.warn("No command line switches found");
        }
    }


    private void adjustRestAssuredConfig(Storage storage, String configType){
        RestAssured.reset();

        int maxConnections = storage.get("Environment." + configType + ".Rest.http_maxConnections");
        Log.debug("Setting http.maxConnections to " + maxConnections);
        System.setProperty("http.maxConnections", "" + maxConnections);

        Boolean closeIdleConnectionsAfterEachResponseAfter = storage.get("Environment." + configType + ".Rest.closeIdleConnectionsAfterEachResponseAfter");
        if ( closeIdleConnectionsAfterEachResponseAfter ) {
            int idleTime = storage.get("Environment." + configType + ".Rest.closeIdleConnectionsAfterEachResponseAfter_idleTime");
            Log.debug("Setting closeIdleConnectionsAfterEachResponseAfter=true with idleTime " + idleTime);
            RestAssured.config.connectionConfig(
                    connectionConfig().closeIdleConnectionsAfterEachResponseAfter(
                            idleTime,
                            TimeUnit.SECONDS)
            );
        }

        Boolean reuseHttpClientInstance = storage.get("Environment." + configType + ".Rest.reuseHttpClientInstance");
        if ( reuseHttpClientInstance ) {
            Log.debug("Setting reuseHttpClientInstance=true");
            RestAssured.config.httpClient(
                    httpClientConfig().reuseHttpClientInstance()
            );
        }

        Boolean relaxedHTTPSValidation = storage.get("Environment." + configType + ".Rest.relaxedHTTPSValidation");
        if ( relaxedHTTPSValidation ) {
            Log.debug("Setting relaxedHTTPSValidation=true");
            RestAssured.useRelaxedHTTPSValidation();
        }

        Boolean followRedirects = storage.get("Environment." + configType + ".Rest.followRedirects");
        if ( followRedirects ) {
            Log.debug("Setting followRedirects=true");
            RestAssured.config.redirect(
                    redirectConfig().followRedirects(true)
            );
        }

        Integer responseTimeout = storage.get("Environment." + configType + ".Rest.responseTimeout");
        Log.debug("Setting CoreConnectionPNames.CONNECTION_TIMEOUT and CoreConnectionPNames.SO_TIMEOUT");
        RestAssured.config.httpClient(
            httpClientConfig().setParam("http.connection.timeout", responseTimeout * 1000)
        );
        RestAssured.config.httpClient(
            httpClientConfig().setParam("http.socket.timeout", responseTimeout * 1000)
        );

        RestAssured.config.decoderConfig(
                DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"));

        //configuring logger for rest operations
        ToLoggerPrintStream loggerPrintStream = new ToLoggerPrintStream();

        RestAssured.config.logConfig(
                new LogConfig( loggerPrintStream.getPrintStream(), true )
        );
    }

    private void createActiveEnvironmentConfig(Map<String, Object> finalEnvConfig, Storage storage, ConfigReader configReader){
        Map<String, Object> defaultEnvConfig = storage.get("Environment.Default");
        Map<String, Object> sshConfig = storage.get("Ssh");
        Map<String, Object> winRmConfig = storage.get("WinRM");
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

        //check if cmd argument ctx.Environment.Active.name was provided and Environment.Active.name property shall be overwritten
        String cmd_arg  = System.getProperty("ctx.Environment.Active.name");
        if ( cmd_arg != null ) {
            Log.debug("Property Environment.Active.name overwritten by CMD arg -Dctx.Environment.Active.name=" + cmd_arg);
            storage.set("Environment.Active.name", cmd_arg);
        }
        //read name of the environment that shall be activated
        Log.debug("Reading active environment configuration");
        String actEnvName = storage.get("Environment.Active.name");
        if ( actEnvName == null || actEnvName.equals("") || actEnvName.equalsIgnoreCase("default") ) {
            Log.debug("Environment.Active.name not set! Fallback to Environment.Default");
        } else {
            //check if config with such name exists else fallback to default
            Map<String, Object> activeEnvConfig = storage.get("Environment." + actEnvName);
            if ( activeEnvConfig == null || activeEnvConfig.size() == 0 ){
                Log.error("Environment config with name " + actEnvName + " not found or empty!");
            }
            //merge default and active
            configReader.deepMerge(defaultEnvConfig, activeEnvConfig);
            defaultEnvConfig = storage.get("Environment.Default");
        }
        //create final
        configReader.deepMerge(finalEnvConfig, defaultEnvConfig);
    }

    private void verifyOnlyOneFeatureExistPerFile(String featurePath){
        String fileContent = "";
        try {
            fileContent = new String(Files.readAllBytes((new File(featurePath)).toPath()), Charset.forName("UTF-8"))
                    .trim()
                    .replaceAll("(?m)^#.*$", "");
        } catch (IOException e){
            Log.error(e.getMessage());
        }

        Pattern pattern = Pattern.compile("(?im)^Feature:(.*?)$");
        Matcher matcher = pattern.matcher(fileContent);

        int count = 0;
        while (matcher.find()){
            count++;
        }

        if ( count > 1){
            Log.error("More than 1 feature defined in a feature file " + featurePath + "!");
        }
    }


}