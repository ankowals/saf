# saf
simple automation framework for learning purposes


----------------------------------


What do we want from a test automation framework?



	a way to execute e2e test using Gherkin language (BDD)
	a way to execute tests related to  
		- rest json/xml (soap)
		- gui (web/native)
		- sql
		- mobile
		- pdf validation
		- others??
	a way to intergate any 3rd party app by execution of any command on local host 
	a way to execute any command on remote host over ssh/winRM
	a way to manage and configure test environment
	a way to manage and configure test data
	a way to prepare/calculate test data at runtime (macros)
	a way to manage and code a set of common modules/step/functions to be used for testing purposes
	a way to downlaod any 3rd party symptoms from SUT like logs, trace files
	a way to log any activity dony by the framework
	a way to report tests status
	a way to attach logs/files/messages/screenshots to the report
	a way to start test from IDE and command line as well as an ability to overwrite parameters when test suite started from cmd
	a way to automatically deploy the framework under windows (dependency management)
	a way to share the code/tests between testers to increae re-usability (version control system, re-usable libraries of keywords)
	a way to monitor and indicate quality of commited tests (see SonarQube for example)
	a way to support PageObject model for web automation purposes
	a way to share common data between steps (dependency injection)
	a way to schedule test execution (see Jenkins/TeamCity)
	a way to generate test documentation automatically
	a way to manage multiple projects (version control system)
	a way to pause test execution and allow for manual intervention (integrate autoIT pause script)
	a way to integrate with test/requirement management tool (like for example jira, so we can have links to tests/epic/stories in the test report)
	a way to intgrate with incident management tool (like for example jira, so we can have at least links to defects that affect particular test in the report and maybe their status etc.)
	a way to write simmple step defs (libraries of methods support because for example usage of try-catch blocks is ugly)
	a way to manage templates
	a way to manage resources (make sure that open connections will be closed when test is over)
	
	a way to integrate with configuration(infrastructure) management tool can be a nice addon (like Puppet, Ansible or Chef)
	a way to integrate VM/container managament tool like Docker or Vagrant can be a nice addon
	a way to re-run failed tests can be a nice addon
	a way to execute tests remotely can be a nice addon
	a way to encrypt/decrypt test data can be a nice addon
	a way to integrate with network protocols simulator (like diameter) or others can be a nice addon (see Seagull: an Open Source Multi-protocol traffic generator)

----------------------------------

Where are we now?
	
	(in progress) a way to execute tests related to  
		(done) - rest json/xml (soap) => RestAssured integrated 
		(done) - gui (web/native) => Selenium WebDriver integrated for chrome, ff and ie
		(done) - sql => jdbc integrated for oracle
		(done) - pdf validation => pdfbox2 integrated 
		(to do) - mobile => Appium integration
		
		further enhancements:
			- enhance logging to log more exceptions
			- add more helper functions
			
	(in progress) a way to execute any command on remote host over ssh/winRM
		(done) ssh/scp/sftp support => sshj library integrated and expectit-core library integrated
		(to do) winRM support => winRm4j library integrated and winRS can be called via ExecutorCore
	
	----------------------------------	
		
		
	(to do) a way to downlaod any 3rd party symptoms from SUT like logs, trace files
	(to do) a way to monitor and indicate quality of commited tests (see SonarQube for example )
	(to do) a way to schedule test execution (see Jenkins/TeamCity)
	(to do) a way to generate test documentation automatically => add new logging categories (like atmn(category, message)), use scenrio outline with path to feature and log file after test execution		
	(to do) add more tests examples
	
	----------------------------------	
	
	(done) a way to execute e2e test using Gherkin language (BDD) => cucumber-jvm integrated
	(done) a way to manage and configure test environment => via global and local property files
	(done) a way to manage and configure test data	=> via json configuration files
	(done) a way to prepare/calculate test data at runtime (macros) => macros implemented
	(done) a way to manage and code a set of common modules/step/functions to be used for testing purposes => via core modules and steps
	(done) a way to log any activity dony by the framework => log4j2 integrated
	(done) a way to report test status => allure integrated
	(done) a way to attach logs/files/messages/screenshots to the report => via allure integration
	(done) a way to start test from IDE and command line as well as an ability to overwrite parameters when test suite started from cmd => via InteliiJ cucumber plugin and mvn surefire plugin
	(done) a way to automatically deploy the framework under windows (dependency management) => via maven
	(done) a way to share the code/tests between testers to increae re-usability (version control system) => via this git repo, 
	(done) a way to support PageObject model for web automation purposes => via BasePage implementation
	(done) a way to share common data between steps (dependency injection) => pico container integration
	(done) a way to manage multiple projects (version control system) => separate repo per project maybe git submodules can be used?
	(done) a way to write simmple step defs (for example usage of try-catch blocks is ugly) => via PageCore, FileCore, StepCore, SqlCore, Macro, Storage, Environment and others models
	(done) a way to manage templates => via CoreStep model
	(done) a way to intergate any 3rd party app by execution of any command on local host => via ExecutorCore
	(done) a way to integrate with test/requirement management tool (like for example jira, so we can have links to tests/epic/stories in the test report) => via allure integration
	(done) a way to intgrate with incident management tool (like for example jira, so we can have at least links to defects that affect particular test in the report and maybe their status etc.) => via allure integration
	(done) a way to pause test execution and allow for manual intervention => via autoIt script executed from a step def
	(done) a way to manage resources (make sure that open connections will be closed when test is over) => via RestAssured config and closing of ssh, jdbc, web driver in scenario hooks
	
----------------------------------

How can we use test automation framework?

	to automate functional tests
	to automate acceptance tests (after any deployment)
	to automate integration tests
	to automate regression tests
	to execute sanity chcecks (smoke tests) and make sure that SUT configuration is correct (like all urls are reachable, ports open, interfaces are up & apps are running, login is possible for each user etc.)
	to gether symptoms like traces/logs/tickets/events from multiple components of a system under test
	to execute test system bringup and feed it with configruation data before test starts
	to setup test environment using configuration management system before test suite execution
	to restore the system to the state before test started
	to move configuration data between test systems
	to describe system behavior via tests implementation (using Gherkin) - use tests as a living documentation
	to use automated equipment for any not strictly test related activities like for example automate mobile phones to detect changes in the offer from a telco operator:)
	
	load genration/performance checks are out of scope
	parallel test execution is out of scope for now
	
	For parallel test execution one can use capabilities of framework or use multiple VMs to deploy multiple SUTs, framework instances, scheduler instances etc... 
	in that case it has to be ensured that tests are seperated from each other 
		- subsequent test does not depend on the result of previous test 
		- tests are using separate test data/config data (do not operate on the same config data at the same time to avoid concurent modifciation)

----------------------------------

Installation instructions


	1 install java jdk ( download from https://java.com/en/download/ )
	2 set an environment variable, for example JDK_HOME=C:\Program Files\Java\jdk1.8.0_144
	
	 	to verify java installation in cmd issue java -version
	
	3 download and extract maven binary ( download Binary Zip archive from https://maven.apache.org/download.cgi )
	4 set an environment variable, for example M2_HOME=C:\apache-maven-3.5.0
	5 append %M2_HOME%\bin to PATH variable
	
	 	to verify maven installation in cmd issue mvn -version
	
	6 install IDE intlliJ Community Edition ( download from https://www.jetbrains.com/idea/download/#section=windows )
	7 in intelliJ go to Files->Settings->Plugins->Browse repositories and install Cucumber for Java plugin
	8 configure path to JDK in pom.xml file under <jdk.path> tag

		<properties>
			<jdk.path>C:/Program Files/Java/jdk1.8.0_144/bin/javac</jdk.path>
			...
		</properties>	
	    
	9 configure port number in pom.xml file under <jetty.port> tag
	
		<properties>
			<jetty.port>8082</jetty.port>
			...
		</properties>	    
	   
Optionally user can execute steps below. Especially for web automation case or jdbc/windows native apps automation.	   
	   
	10 install git ( download from https://git-scm.com/download/win )
	11 clone the repo for example to C:\Documents\Projects\SAF	
	12 download Selenium Chrome driver and other drivers if needed ( download from https://sites.google.com/a/chromium.org/chromedriver/downloads )
	13 put web drivers in <project dir>\src\test\java\resources, for example in C:\Documents\Projects\SAF\src\test\java\resources
	14 download JDBC oracle driver and other drivers if needed ( download from http://www.oracle.com/technetwork/apps-tech/jdbc-112010-090769.html ) 
	15 put odbc drivers in <project dir>\src\test\java\resources, for example in C:\Documents\Projects\SAF\src\test\java\resources
	16 Fix relative path (relative to project dir) to web drivers in \src\test\java\config\framework\framework.config
	17 Fix relative path (relative to project dir) path to jdbc drivers in \src\test\java\config\framework\framework.config
	18 Install autoIt ( download from https://www.autoitscript.com/site/autoit/downloads/ )

----------------------------------


How to import project in IntelliJ?

	1 open the IDE and click "Import Project"
	2 point it to the location where your project is
	3 select "Import project from external model", select "Maven" and hit Next
	4 go with default options and click Next
	5 the project is recognized as maven project and click Next
	6 in case intelliJ is not able to locate your JDK, click "plus" icon in Select Project SDK window and point to the JDK installed on your machine, click Next
	7  enter the name of project and click Finish


----------------------------------


Dir structure shall be like this


	Project

		- src
			- test
				-java
					- config
						- environment
						- framework
						- testdata
					- features
						- Web
							- feature1
							- feature2
							...
						- Rest
						...
					- libs
						- libCore
							- modules
							- steps
						- libProject1
						- libProject2
						...
					- resources
						chromedriver.exe
						ojdbc6.jar
						...
		- target
		pom.xml



Dir src/test/java/libs/<lib name>/modules contains methods needed to run the test. 
Dir src/test/java/libs/<lib name>/steps contains step defs defintion and implementation.



Subdirectory libCore cotnains saf freamework steps and methods. It is mandatory to have it in each project!

Other subdriectories contains project specific stuff like page obejct models etc. They are optional and can be added as git submodules for example.



Dir src/test/java/resources contains additional resources used by tests like drivers, 3rd party apps etc.

Dir src/test/java/config contains configuration files (*.config) 

Dir src/test/java/features contains features files (cotntainers for tests).

Dir traget/ will be used to store results of test execution like for example test report.

File pom.xml contains project properties and dependencies. 

--------------------------------



General concepts



We follow BDD apporach. Reason is very simple. It is usually much easier for testers to write automated tests (following Gherking principles). In large projects (with large and separate teams of testers, analysts, devs) BDD main adventage (so called common language to describe sytsem behaviours) can be rarely implemented but BDD is still giving testers the benefit of simpler tests creation. They can use step defs to write tests in plain english language.

Tests are called Scenarios. They are grouped in Features. They are build using step defs.

Features act as containers for Scenarios.

Please keep 1 Feature per 1 file.

Feature file name shall be same like Feature name.

Scenario names shall be unique per feature.

Step defs represents test steps. They are used to execute actions during test. They are called by their names. A set of steps can be used to build a scenario.



Test execution shall look like this

	     ConfigurationData ------->	System Under Test 
						^	
	TestData & ExpectedData -----> | execution engine | -----> ResultData 	=> test result (OK/NOK)
	    & EnvironmentData			^
				    	user actions (steps)


SUT is our system under test. It can be an application that we want to test. It can run locally on the same host where the framework is installed or it can run on remote host.

ConfigurationData is a data that shall be loaded to the SUT before test suite is started. It can contain stuff like links configuration (ip address on the machine, ports, users, passwords, listeners configuration, urls, log level etc.) but it shall also contain business configuration that is applicable for particular application like for example tariff configuration etc.

Usually it is enough to load configuration data once using separate feature file, manually or by using any 3rd party tool that can do it for us.

TestData is a data that shall be given as an input for particular test. For example assuming we are testing different tariffs on particular offer we can give as an input an offer name. Other example can be a test where we would like to login to a web page. In this case part of the test data can be a particular login and password that we are going to use during test execution. Please keep in mind that login and password shall be earlier created on system under test as a part of configuration data.

EnvironmentData describes SUT that shall be used by the framework. It is needed becuase we have to inform it to which application it shall connect, which url shall be open in the browser, which api shall be used etc. We can have multiple SUTs available (dev, test, release etc). It is possible to switch between them easily and execute the same tests on each of them.

ExpectedData describes data that is expected as an outcome of an action performed during test. Actions are performed by the step defs. For example if we are sending http post request towards the system under test we can expect http result code to be 200 (OK). In this case our ExpectedData can be http result code equal to 200.

Execution engine is our framework.

ResultData is the output of test execution. It is everything that can be used for comparison with ExepctedData and is a result of step def execution.

Test result is the result of comparison between ResultData & ExpectedData.




Framework will execute each scenario in a feture file and each step in a scenario.
When one step fails whole scenario is marked as failed and next scenario in a feature is executed.
Each scenario execution looks similar.



First TestData, ExpectedData and EnvironmentData storage (SUT configuration) will be created. 
Macro evaluation will be done.
Execution engine will connect to SUT (as configured in EnvironmentData) and execute any step (action) that is described in the scenario.
Last step is to verify recieved resutls against expected resutls from the storage.
Test report will be created.



TestData/ExpectedData can be passed to the steps directly in a feature file or can be taken from a *.config file.

Global configuration is available but it can be overwritten/updated by local config.
Config files will be loaded automatically as long as feature file name is same as feature name defined inside the feature file file!
For example file myTestFeature.feature shall contain
	
	Feature: myTestFeature
		
		Scenario: myTestScenario1
		
		...


Log file will be created in target dir with a timestamp for each run, for example target/2017-09-11_103158_FK_Prototype
When run is done via mvn test command in addition to that a test report dir will be created, for example target/site
Report can be viewed in the browser.



--------------------------------



How the framework is build?



Java is used for learning purposes.

To make installation and deployment easy so called project build and dependency management tool is used. It is called maven.
It will automatically download all needed libraries so there is no need to hunt them down on your own.
Maven configuration is available in so called pom.xml file. It contains not just dependencies but also plugins.
Thanks to this maven can be used to start our tests from command line. For this purpose so called surefire plugin is used.

For logging purposes log4j2 library is used.
For BDD cucumber-jvm and junit libraries are used.
For reporting purposes allure library is used.



Configuration files are in json format. We need to parse the data available inside.



For json parsing gson library is used.



When steps are executed we need to pass the same instance of a class to them, for example webdriver instance, test data storage, output of step def execution etc.

To make it possible we are using so called dependency injection. Without it for example each step will open a new browser window.



For dependency injection pico-container library is used.
For web automation Selenium WebDriver library is used.
For api automation RestAssured library is used.
To read Csv files openCSV library is used.

To better handle command execution and sql execution Commons-exec and commons-dBUtils libraries from Appache are used. Same for better handling of files and string manipulations (Commons-io and Commons-lang).

To read/write pdf files pdfBox2 library is used.

To have possibility to pause test execution autoIt scirpt is used.

To manage remote hosts via ssh and transfer files via scp/sftp sshj and expectit-core libraries are used.

On top of that macro support, test data management, configuration files support, Page Object Model support and more was added.
Project and test structure is also enforced to keep things consistent.



--------------------------------



Usage




To run a test from windows cmd please execute


	cd <install_dir>
	mvn clean test -Dcucumber.options="--tags @bookByIsbn"
	mvn site
	mvn jetty:run
	go to http://localhost:8082


Please note that usage of clean keywords ensures that artifacts from previous test execution are removed first.

One can also use IntelliJ to run a feature file. In that case only log file will be created.
To run a test from IntelliJ a cucumber plugin is used. Please click with right mouse button on the feature file name and choose 'Run'.
In case of an exception indicating that step defs were not found please double check plugin configuration. To do so go to Run menu in the toolbar and choose Edit configurations. Select cucumber java and make sure that glue points to the correct directory or package.
If test run fine via mvn test command but not using IntelliJ this indicates missconfigruation of cucumber-jvm plugin.

To generate a report from test please execute mvn site, mvn jetty:run to run jetty and check the report in the browser under http://localhost:port.

It is possible to overwrite active_env property from the command line. In that case project specific config as specified by the CMD argument will be used during test execution. To do so please execute a test for example like below

	mvn clean test -Dactive_env="bookByIsbn" -Dcucumber.options="--tags @bookByIsbn"

In this particular case a default environment (SUT) configuration will be loaded and later on it will be overwritten by config available in a file src\test\java\config\environment\bookByIsbn.config. Cucumber option --tags can be used to run only a subset of tests that are tagged with @bookByIsbn tag.

It is possible to set browser width and height via command line argument. To do so please execute test using command like below

	mvn clean test -Dactive_env="demoOnline" -DwidthXheight="800 x 640" -Dcucumber.options="--tags @demoOnline"

Argument -DwidthXheight= will be used to set browser dimensions.

--------------------------------



An example of a feature file


	@bookByIsbn
	Feature: Get book by ISBN
	  Scenario: User calls web service to get a book by its ISBN
	    Given a book exists with an isbn
	    When a user retrieves the book by isbn
	    Then the status code is TestData.statusOK
	      And response includes the following
		| totalItems 	 		                    	| 1 	                |
		| kind					            	| books#volumes	        |
	      And response includes the following in any order
		| items.volumeInfo.title 				| Steve Jobs		|
		| items.volumeInfo.publisher 				| Simon and Schuster	|
		| items.volumeInfo.pageCount 				| 630			|


Feature files can be tagged as well as scenarios. Use tags and cucumber options to execute a particular tests



	mvn clean test -Dcucumber.options="--tags @bookByIsbn"



To pass data from configruation file use test data storage name and pass field after dot. For example TestData.statusOK. Of course step def needs to support this.

To pass multiple parameters to the test one can use tables. Of course step def needs to support it.
As can be seen test data/expected data can be either hardcoded in the feature file or taken from a configruation file. 
It is up to the tester to decide which approach to choose.


An Example of a feature file with data taken from configuration.

	@bookByIsbn
	Feature: Get book by ISBN
	  Scenario: User calls web service to get a book by its ISBN
	    Given a book exists with an isbn
	    When a user retrieves the book by isbn
	    Then the status code is Expected.statusOK
	      And response includes the following
		| totalItems 	 		                    	| Expected.totalItems   |
		| kind					            	| Expected.kind         |
	      And response includes the following in any order
		| items.volumeInfo.title 				| Expected.title	|
		| items.volumeInfo.publisher 				| Expected.publisher	|
		| items.volumeInfo.pageCount 				| Expected.pageCount	|
	
	
where TestData is defined as config/testdata.config

	TestData:{
	    isbn : "9781451648546"
	}

Expected data is defind as config/expected.config

	Expected:{
	    statusOK : 200,
	    totalItems : 1,
	    kind : "books#volumes",
	    title : "Steve Jobs",
	    publisher : "Simon and Schuster",
	    pageCount : 630
	}

Test structure is

	resources/features/Rest/GetBookByIsbn/
						config/
							testdata.config
							expected.config
			
						GetBookByIsbn.feature

Please note that it is possible to use macros in TestData and Expected data definitions.

TestData can be passed to the step explicitly like below

	Given a book exists with an isbn TestData.isbn
	 or
	Given a book exists with an isbn "9781451648546"

TestData can also be passed to the step silently like below

	Given a book exists with an isbn

In this case step def does not expect any parameters. It will read isbn from the configruation by calling a helper method. See below for more details.


--------------------------------



Details of what is happening during test execution



Cucmber runner is available in src/test/java/libs/libCore/steps/TestRunner.class
It contains cucumber options like glue path (path to steps definitions), features path and allure report plugin.
There shall be no need to change it parameters.

	package libs.libCore.steps;

	import cucumber.api.junit.Cucumber;
	import org.junit.runner.RunWith;
	import cucumber.api.CucumberOptions;

	@RunWith(Cucumber.class)
	@CucumberOptions(
		plugin = {"ru.yandex.qatools.allure.cucumberjvm.AllureReporter"},
		features = "src/test/java/features",
		glue = "libs")
	public class TestRunner {}



Before each scenario execution so called @Before and @After hooks are run.
In @Before hook we create context, read framework and SUT configurtion, create test data storage, evaluate macros and initialize helper modules (Core modules).
It will also find local configuration files and load them for usage in steps.There is no need to do that in seperate steps or Background scenario.
In an @After hook we try to close the resources like for example web driver, Sql connection or take a screenshot if test failed.
As a last step we are attaching log from the scenario to the test report.
Hooks implementation can be found under src/test/java/libs/libCore/steps/HooksSteps.class



After @Before method execution cucumber-jvm will execute each step.

Steps shall be implemented under src/test/java/libs/<lib name>/steps directory. Please use seperete package for your project steps and group them to make files management easier when project grows.

There is also possibility to execute some actions before the whole test suite (a set of feature files) will be executed. There are 2 additional global hooks available. So called beforeAll and afterAll hook. They can be used to initialize logger, print system properties or try to close the resources like web drivers etc.

Each new scenario start will be indicated in the log as follows



	[INFO ] 2017-09-11 12:32:27.558 [main] Log - *** Feature id: get-book-by-isbn ***
	[INFO ] 2017-09-11 12:32:27.563 [main] Log - ***
	[INFO ] 2017-09-11 12:32:27.563 [main] Log - ***
	[INFO ] 2017-09-11 12:32:27.563 [main] Log - *** Scenario with name: User calls web service to get a book by its ISBN started! ***
	[INFO ] 2017-09-11 12:32:27.563 [main] Log - ***
	[INFO ] 2017-09-11 12:32:27.564 [main] Log - ***



@Before hook method execution will be visible like below



	[INFO ] 2017-09-11 12:32:27.564 [main] Log - Started resources initialisation
	[INFO ] 2017-09-11 12:32:27.566 [main] Log - <- checking environment configuration ->

----------------------------------

Environment



During this phase a files available in /src/test/java/config will be checked for framework and SUT configuration.
They contain global environment configuration as well as global test data configuration. Each setting can be overwritten later on during test execution by local test configuration.
Recommendation is to use project specific file to keep there System Under Test settings and framework settings shall stay in separate file.

Property Environment.Active.name available in src/test/java/config/environment/active.config indicates which SUT configuration shall be used. For example we can have in default.properties



	Environment={

	    Active : {
		name : "reqResIn"
	    }

	}


And in src/test/java/config/environment/reqResIn.config



	Environment={

	    reqResIn : {

		Rest : {
		    url : "https://reqres.in",
		    url_post_suffix : "/api/users",
		    url_get_suffix : "/api/users/"
		}

	    }

	}

Please note that not the file name but Environment object name is important. If object Environment.Active.name equals "reqResIn" then object Environment.reqResIn has to exist.

Default configuration will be used when Environment.Active.name equals "Default" or is empty or does not exists.
On the other hand object Environment.Active has to exists. It can be empty if it is enough to use default environment configuration.

Project specific configuration will overwrite settings that are defined in Default config and add more if they do not exsits. Deep merge is in use so it is possible to add nested parameters to exsiting configuration. For example assuming that Default contains

	Environment={
	    Default : {
		Rest : {
		    url : "http://default.com"
		}
	     }
	}


Assuming that active configuration points to

	Environment={

	    Active : {
		name : "reqResIn"
	    }

	}
	
Assuming that project specific configuration is


	Environment={

	    reqResIn : {

		Rest : {
		    url_get_suffix : "/api/users/"
		}

	    }

	}
	
Final configuration that can be used in tests is

	Environment={

	    Active : {
		name : "reqResIn",
		Rest : {
		    url : "http://default.com",
		    url_get_suffix : "/api/users/"
		}		
	    }

	}


File src/test/java/config/environment/default.config contains global Default configuration.

	Environment={

	    Default : {

		Web : {
		    browser : "Chrome",
		    timeout : 10,
		    url : "http://www.google.pl",
		    size : "Max" #width x height -> 1024 x 960
		},

		Rest : {
		    url : "http://default.com"
		},

		Jdbc : {
		    url : "jdbc:oracle:thin:scott/oracle@localhost:1521/XE"
		}

	    }

	}

This configuration will be used in case there is no other active configuration specified.
Please note that this configuration is divided into 2 parts. Second part contains configuration specific for the framework like paths to the drivers etc. It can be found in src/test/java/config/framework/framework.config. All settings can be put into one file but usually it is easier to manage complex configurations if they are logically splited between few files.

	Environment:{

	    Default: {

		MacroEval: true,
		PauseDuration: 300,

		WebDrivers: {
		    CloseBrowserAfterScenario: true,
		    Chrome: {
			path: "src\\test\\java\\resources\\webDrivers\\chromedriver.exe"
		    },
		    FireFox: {
			path: "src\\test\\java\\resources\\webDrivers\\geckodriver.exe"
		    },
		    InternetExplorer: {
			path: "src\\test\\java\\resources\\webDrivers\\IEDriverServer.exe"
		    }
		},

		JdbcDrivers: {
		    Oracle: {
			path: "src\\test\\java\\resources\\jdbcDrivers\\ojdbc6.jar"
		    }
		},

		scripts: {
		    path: "src\\test\\java\\resources\\scripts"
		},

		apps: {
		    autoIt: "C:\\Program Files (x86)\\AutoIt3\\AutoIt3.exe"
		}

	    }

	}

Please note that the paths are relative to the project directory.
In addition there are flags available that can be used to indicate 

	- whether macro evaluation shall be done before run of each scenario
		MacroEval : true
	- whether to close the web browser after each scenarion or to keep it open until whole suite will be executed
		CloseBrowserAfterScenario : true
	- pause duration in case manual intervention during test execution is needed
		PauseDuration

Entity scripts.path can be use to indicate a path relative to project directory where some autoIT or shell scripts can be found for example.
Entity apps can be used to group together any 3rd party apps that can be called by the step defs like for example autoIt, wireshark, mergecap etc.

In this way multiple systems under test can be configured.

Please note that in libs/libCore/config user can find default configruation that can be overwriten by global config available in  src/test/java/config/framework or src/test/java/config/environment subdirectories. For this reason please do not change anything in libs/libCore/config files.

Now it is time to read test data configuration from *.config files.
Everything that is written below applies also to environment configuration files behaviour.

From now on in case there is a need to access any configuration parameter one can use in the step def Storage.get() method. For example

    @When("^xml post request (.*?) with soap action header (.*?) is sent$")
    public void xml_post_request_is_sent(String name, String actionHeader) throws Throwable {
        Log.info("* Step started xml_post_request_is_sent");

        String url = Storage.get("Environment.Active.Rest.url");
	...
	
Storage.get("Environment.Active.Rest.url") method returns url value from active configuration. Please note that is is assigned to variable of type String. In case Storage.get() returns other type of data than String we may encounter ClassCastException.
	
----------------------------------

TestData



Global configuration is available under src/test/java/config/testdata directory.
Files under this directory are checked and evaluated. New storage is created based on their content.
An example of test data configuration is below (content of src/test/java/config/testdata/testdata.config file)

	TestData={
	    "search_sentence" : "this is the default entry!",
	    drugi_kluczyk_z_pliku : 2;
	    "trzeci kluczyk z pliku" = ["first element", "second elmenet"],
	    "a to czwarty kluczyk" : {
		test : "a to wartosc z zagniezdzonej mapy",
		test2 : 5,
		test3 : 4.5123,
		test4 : {
		    testx1 : tadam,
		    testx2: tadam2
		    }
		},
	    ostatni : [1,2],
	    notAnInteger : 4.5123,
	    "a to test na makro" : mcr.isbn,
	    DoubleMapa : {
		first: 1,
		 second: 2,
		 third :3
		 }
	    }


An example of log is below



	[INFO ] 2017-09-11 12:32:27.574 [main] Log - <- creating test data storage ->
	[DEBUG] 2017-09-11 12:32:27.575 [main] Log - Project path is C:\Users\akowa\Documents\Projects\FK_Prototype
	[WARN ] 2017-09-11 12:32:27.601 [main] Log - Ctx obj with key TestData does not exists or null!
	[DEBUG] 2017-09-11 12:32:27.602 [main] Log - Ctx object TestData of type class java.util.HashMap created or modified
	[INFO ] 2017-09-11 12:32:27.602 [main] Log - Going to view the current state of test data
	[INFO ] 2017-09-11 12:32:27.602 [main] Log - --- start ---
	[INFO ] 2017-09-11 12:32:27.602 [main] Log - (Long)drugi_kluczyk_z_pliku = 2
	[INFO ] 2017-09-11 12:32:27.602 [main] Log - (String)search_sentence = this is the default entry!
	[INFO ] 2017-09-11 12:32:27.603 [main] Log - (Double)notAnInteger = 4.5123
	[INFO ] 2017-09-11 12:32:27.603 [main] Log - (String)a to test na makro = mcr.isbn
	[INFO ] 2017-09-11 12:32:27.603 [main] Log - (ArrayList)trzeci kluczyk z pliku = [first element, second elmenet]
	[INFO ] 2017-09-11 12:32:27.603 [main] Log - (HashMap)a to czwarty kluczyk = {test4={testx2=kupa, testx1=dupa}, test2=5, test3=4.5123, test=a to wartosc z zagniezdzonej mapy}
	[INFO ] 2017-09-11 12:32:27.603 [main] Log - (ArrayList)ostatni = [1, 2]
	[INFO ] 2017-09-11 12:32:27.603 [main] Log - (HashMap)DoubleMapa = {third=3, first=1, second=2}
	[INFO ] 2017-09-11 12:32:27.604 [main] Log - --- end ---

Configuration files can include conent of other configuration files. To do so please use #include directive and provide reltive path to the other config file. Path shall be relative to project directory. An example is visible below.

Content of file src/test/java/features/Web/test1/test1.config is

	#include "features/Web/test4/test45.config"
	
	TestData:{
	    "search_sentence" : "cucumber",
	    notAnInteger : 3.5123,
	    nowy_wpis : test
	       }

In this case first the file from #include directive will be read and processed. Order of the includes is important. They are always processed first before other content of a config file.

Please note that config files are processed in alphabetical order. It shall be avoided but if certain order of processing is required please use specific config files names like 00_first.config, 01_second.config, 02_third.config etc.

It is also possible to use comments in the configuration files. To do so please start the commet with #, for example

	#this is just a comment exmaple 
	TestData:{
	    "search_sentence" : "cucumber", #this is another comment example
	    notAnInteger : 3.5123,
	    #this is yet another comment example
	    nowy_wpis : test
	       }


----------------------------------


Macros



Simialr for macros. They are read from *.config file and stored for future usage.

Macro can be used to calculate some values at run time and place them in a test data or template. For example to trigger a request with particular timestamp, to enter random/unique value into the web form or to check that log contains a particular date.

Macro works as follows.

Macro definitions are kept in a *.config file under Macro object. For example



	Macro:{
		isbn : {
		    type: date,
		    format: "MM/dd/yyyy HH:mm:ss S",
		    addYears: 1,
		    addMonths: 2,
		    addWeeks : -3,
		    addDays: 4,
		    addHours: -1,
		    addMinutes: -2,
		    addSeconds: 0,
		    addNanos: 3
		},
		testMacro : {
		    type: date,
		    prefix: "Tadam->",
		    suffix: "<-madaT"
		}
	    }



File /src/test/java/libs/libCore/modules/Macro.class contains methods to calculate macros based on their definitions and evaluate test storage.

Global test data and macro configuration can be overwritten by local configuration files available under the same directory as feature file.



For example directory /src/test/java/features/Rest/GetBookByISBN/config can contain 2 files



	macro.config
	testdata.config



Their content can be



	Macro:{
		testMacro : {
		    type: date,
		    prefix: "Local->",
		    suffix: "<-Local"
		},
		testMacro2 : {
		    type: date,
		    prefix: "Local2->",
		    suffix: "<-2Local"
		},
		testMacro3 : {
		    type: timestamp,
		    suffix: "000"
		}
	    }



	TestData:{
	    isbn : "9781451648546",
	    "statusOK" : 200
	    }



Macro defined as above (and overwritten by local config) after evaluation will be

	[INFO ] 2017-09-11 12:32:27.919 [main] Log - <- Started local config load ->
	[DEBUG] 2017-09-11 12:32:27.919 [main] Log - Project path is C:\Users\akowa\Documents\Projects\FK_Prototype
	[DEBUG] 2017-09-11 12:32:27.960 [main] Log - Found feature file path is C:\Users\akowa\Documents\Projects\FK_Prototype\src\test\resources\features\Rest\GetBookByISBN\GetBookByIsbn.feature
	[DEBUG] 2017-09-11 12:32:27.961 [main] Log - Feature dir is C:\Users\akowa\Documents\Projects\FK_Prototype\src\test\resources\features\Rest\GetBookByISBN
	[DEBUG] 2017-09-11 12:32:27.962 [main] Log - Following config files were found inside 
	[DEBUG] 2017-09-11 12:32:27.962 [main] Log - C:\Users\akowa\Documents\Projects\FK_Prototype\src\test\resources\features\Rest\GetBookByISBN\config\macro.config
	[DEBUG] 2017-09-11 12:32:27.962 [main] Log - C:\Users\akowa\Documents\Projects\FK_Prototype\src\test\resources\features\Rest\GetBookByISBN\config\testdata.config
	[DEBUG] 2017-09-11 12:32:27.963 [main] Log - Ctx object Macro of type class java.util.HashMap created or modified
	[DEBUG] 2017-09-11 12:32:27.963 [main] Log - Ctx object TestData of type class java.util.HashMap created or modified
	[DEBUG] 2017-09-11 12:32:27.963 [main] Log - Environment property do_macro_eval_in_hooks = true
	[INFO ] 2017-09-11 12:32:27.963 [main] Log - <- evaluating macros ->
	[DEBUG] 2017-09-11 12:32:27.978 [main] Log - Macro testMacro3 is 1505125947000
	[DEBUG] 2017-09-11 12:32:27.979 [main] Log - Macro testMacro2 is Local2->09/11/2017 12:32:27 9<-2Local
	[DEBUG] 2017-09-11 12:32:27.979 [main] Log - Macro isbn is 10/25/2018 11:30:27 9
	[DEBUG] 2017-09-11 12:32:27.979 [main] Log - Macro testMacro is Local->09/11/2017 12:32:27 9<-Local
	[DEBUG] 2017-09-11 12:32:27.979 [main] Log - Ctx object TestData of type class java.util.HashMap created or modified
	[INFO ] 2017-09-11 12:32:27.980 [main] Log - Test data storage after local config load is
	[INFO ] 2017-09-11 12:32:27.980 [main] Log - Going to view the current state of test data
	[INFO ] 2017-09-11 12:32:27.980 [main] Log - --- start ---
	[INFO ] 2017-09-11 12:32:27.980 [main] Log - (Long)drugi_kluczyk_z_pliku = 2
	[INFO ] 2017-09-11 12:32:27.980 [main] Log - (String)isbn = 9781451648546
	[INFO ] 2017-09-11 12:32:27.980 [main] Log - (Long)statusOK = 200
	[INFO ] 2017-09-11 12:32:27.980 [main] Log - (String)search_sentence = this is the default entry!
	[INFO ] 2017-09-11 12:32:27.980 [main] Log - (Double)notAnInteger = 4.5123
	[INFO ] 2017-09-11 12:32:27.980 [main] Log - (String)a to test na makro = 10/25/2018 11:30:27 9
	[INFO ] 2017-09-11 12:32:27.980 [main] Log - (ArrayList)trzeci kluczyk z pliku = [first element, second elmenet]
	[INFO ] 2017-09-11 12:32:27.981 [main] Log - (HashMap)a to czwarty kluczyk = {test4={testx2=kupa, testx1=dupa}, test2=5, test3=4.5123, test=a to wartosc z zagniezdzonej mapy}
	[INFO ] 2017-09-11 12:32:27.981 [main] Log - (ArrayList)ostatni = [1, 2]
	[INFO ] 2017-09-11 12:32:27.981 [main] Log - (HashMap)DoubleMapa = {third=3, first=1, second=2}
	[INFO ] 2017-09-11 12:32:27.981 [main] Log - --- end ---
	[INFO ] 2017-09-11 12:32:27.982 [main] Log - <- Finished local config load ->



As can be seen different macro defintions are supported.
Macro can be used to return a unix timestamp or a date in specified format.
They can be concatenated with a specific prefix or suffix. 
Macro values are always returned as strings. 

Please note that macros have to be evaluated by calling of Macro.eval(String storage_name) method from Macro.class in each step were such evaluaton shall be done. They are evaluated once by default in @Before hook but this can be turned off in the configruation.
To use previously defined macros one can put into the test data storage such macro as a value of particular key, for example

	TestData={
	    "a to test na makro" : mcr.isbn,
	    NOW_TimeStamp : mcr.testMacro3
	    }

Data types supported in test data configuration are

	String,
	Integer,
	Long,
	Double,
	HashMap,
	ArrayList,
	Boolean


----------------------------------

Templates



TestData can be used in templates. For example if we would like to trigger an http request with json paylod we can take the body content (json structure) from the template. Similar for xml content. In this case please add your template to template directory like in example below.

	features
		- Rest
			- ReqResIn
				- config
					test.data.config
				- template
					createUser.template
				ReqResIn.feature
					
Template files shall have an extension of .template. They can contain a place holder for variable parts. They can be evaluated in a step def by using method StepCore.evaluateTemplate(name).

Template content can be

	{
	    "name": "${ctx.TestData.name}",
	    "job": "${ctx.TestData.job}"
	}

Where testdata.config can be

	TestData:{
	    name : "morpheus",
	    job  : "leader"
	}

After evaluation a new file will be created with content injected from TestData.

	{
	    "name": "mopheus",
	    "job": "leader"
	}

Similar for xml requests. Our template can be 

	<?xml version="1.0" encoding="utf-8"?>
	<soap12:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap12="http://www.w3.org/2003/05/soap-envelope">
	  <soap12:Body>
	    <GetCitiesByCountry xmlns="http://www.webserviceX.NET">
	      <CountryName>${ctx.TestData.countryName}</CountryName>
	    </GetCitiesByCountry>
	  </soap12:Body>
	</soap12:Envelope>

TestData shall contain

	TestData:{
	    countryName : "Poland"
	}

It is also possible to pass macro content to the template. In this case please assign macro to a key in TestData and use this key in the template.

Please note that templates can also be used for comparison purposes and to validate test reults. For example we can compare a body of the message with a template, a trace file, a log/event file content etc. To make such comparions easier there is a set of filters available to filter out not needed parts of trace/body/log before comparison will start. In this case our template body can be much smaller. Available filters are negative to remove the lines, positive to keep the lines that match particular cirteria and a block filter to keep just the lines between specified keywords. 

Template comparison can be invoked by calling StepCore.compareWithTemplate(templateName, pathToFileToCompare) in a step def. In a similar way one can invoke filters on a template or file to compare.

Templates support regular expressions. This means that one can use following template to make sure that the content of the table is as expected.

content expectedOutput.template is

	DEPTNO, DNAME, LOC
	[0-9]{2}, ACCOUNTING, NEW YORK
	20, [A-Z]+, DALLAS
	\d+, SALES, CHICAGO
	${Expected.lastId}, OPERATIONS, ${ctx.Expected.lastCity}
	
where expected.config contains
	
	Expected:{
	     lastId : "40",
	     lastCity : "BOSTON"
	 }

it shall match test result like below

	DEPTNO, DNAME, LOC
	10, ACCOUNTING, NEW YORK
	20, QA, DALLAS
	30, SALES, CHICAGO
	40, OPERATIONS, BOSTON

Each step execution is marked in a log with a "* Step started" string to make it easier to find it. For example



	[DEBUG] 2017-09-11 12:32:28.208 [main] Log - * Step started a_user_retrieves_the_book_by_isbn



--------------------------------



How to write step and share data between steps?



Thanks to dependency injection there is a way to share objects between steps and modules. In SharedContext.class available under /src/test/java/libs/libCore/modules so called Context Object was defined.

To grant access to it please make sure that your Steps class extends BaseStep class and create a constructor like below

	public class DemoOnlieSteps extends BaseSteps {

	    private SharedContext ctx;

	    // PicoContainer injects class SharedContext
	    public DemoOnlieSteps (SharedContext ctx) {
		this.ctx = ctx;
	    }
	    
	 }
	 
Where DemoOnlineSteps is a class that contains project specific steps to handle web automation for particular page.
In this way we can pass same instance of ctx between steps and modules. With this approach we can use methods defined for objects available in ctx variable.
BaseSteps class define a set of helpers modules to make writing new step defs much easier. They are called as below
Macro, StepCore, PageCore, SqlCore, Storage, FileCore, ExecutorCore, PdfCore. They contain a set of methods that can be used to do common things in steps like creating files, evaluating macros, reading environment configuration, evaluating templates, attaching files to the report etc.

For example lets have a look at 2 steps below

    @Given("^a book exists with an isbn$")
    public void a_book_exists_with_isbn() {
        Log.info("* Step started a_book_exists_with_isbn");
	
        String isbn = Storage.get("TestData.isbn");
        RequestSpecification request = given().param("q", "isbn:" + isbn);
        ctx.Object.put("request",RequestSpecification.class, request);
    }

    @When("^a user retrieves the book by isbn$")
    public void a_user_retrieves_the_book_by_isbn(){
        Log.info("* Step started a_user_retrieves_the_book_by_isbn");
	
        String url = Environment.readProperty("REST_url");
        RequestSpecification request = ctx.Object.get("request",RequestSpecification.class);
        Response response = request.when().log().all().get(url);
        ctx.Object.put("response",Response.class, response);
        StepCore.attachMessageToReport("Json response", response.prettyPrint().toString());

    }
    
To retrieve test data storage one can write HashMap<String, Object> testDataMap = ctx.Object.get("TestData",HashMap.class);
From now on testDataMap and its values can be used in the step.
Other and much simpler way to retrieve a particular value from the storage is String isbn = Storage.get("TestData.isbn");
Nested objects can be provided using dots like for example Storage.get("TestData.isbn.some_nested_key[0]") etc.

ctx.Object is a bucket to which we can throw anything and later on we can retrieve it. This is useful to share data between steps that are defined in different class. For example

	ctx.Object.put("request",RequestSpecification.class, request);

This metohd puts an object of type RequestSpecification to ctx.Object bucket with name "request". Later on another step can retrieve it like below

	RequestSpecification request = ctx.Object.get("request",RequestSpecification.class);

StepCore contains a set of helper functions that can be used when writing step defs. For example there are functions that can be used to add something as an attachment to the test report.

There is also one more method that can be used to check if step input is a variable from the configuration file. See an example below.

    @Then("^the status code is (.*)$")
    public void verify_status_code(String input){
        Log.info("* Step started verify_status_code");

        Long statusCode = StepCore.checkIfInputIsVariable(input);
        Integer code = statusCode.intValue();

        Response response = ctx.Object.get("response",Response.class);
        ValidatableResponse json = response.then().statusCode(code);
        ctx.Object.put("json",ValidatableResponse.class, json);
    }

In the feture file one can write

	Then the status code is TestData.statusOK

where

	TestData:{
	    "statusOK" : 200
	    }
	
or

	Then the status code is 200
	
In both cases step shall pass.
Please note that step def input parameter is of type String. method checkIfInputIsVariable can return an object of different type (for example Int, Long, Double, String, Boolean). Please do not hardcode Maps or Lists directly in the feature files, rather define them in a config file and use such config pointer in the feature. For example instead of writing "Then expected result is {key1:1, key2:2}" write "Then expected result is Expected.resultMap" 

It is assumed that user knowns what type is expected otherwise we can get type missmatch exception.

If there is a need to read any environment property one can use in a step Storage module. An example below

        Boolean doMacroEval = Storage.get("Environment.Active.MacroEval");
        if ( doMacroEval == null ){
            Log.error("Environment.Active.MacroEval null or empty!");
        }
	
Similar for macro evaluation. It is enough to just call Macro.eval(input) method. Where input is the name of storage (of type HashMap).

In case a step shall handle multiple input parameters please use tables in a feature file. In the step input will be provided as a Map.

Later on each input parameter and its value can be retrieved in a loop. See an example below.

    /**
     * Verifies that response includes some fields {} and their value contains {}
     * Input requires a table
     *
     * Uses following objects:
     *  ctx.Object.json
     *
     * @param responseFields - Map<String, String>, table that contains key and expected value pairs to verify
     *
     */
	@And("^response includes the following in any order$")
	    public void response_contains_in_any_order(Map<String,String> responseFields){
		Log.info("* StepCore started response_contains_in_any_order");

		ValidatableResponse json = ctx.Object.get("json",ValidatableResponse.class);
		for (Map.Entry<String, String> field : responseFields.entrySet()) {
		    Object expectedValue = StepCore.checkIfInputIsVariable(field.getValue());
		    String type = expectedValue.getClass().getName();
		    if(type.contains("Long")){
			Long lExpVal = (Long) expectedValue;
			Log.debug("Expected is " + field.getKey() + "=" + lExpVal.intValue());
			Log.debug("Current is " + json.extract().path(field.getKey()));

			try {
			    json.body(field.getKey(), containsInAnyOrder(lExpVal.intValue()));
			} catch (AssertionError e) {
			    Log.error("", e);
			}
		    }
		    else {
			String sExpVal = (String) expectedValue;
			Log.debug("Expected is " + field.getKey() + "=" + sExpVal);
			Log.debug("Current is " + json.extract().path (field.getKey()));

			try {
			    json.body(field.getKey(), containsInAnyOrder(sExpVal));
			} catch (AssertionError e) {
			    Log.error("", e);
			}
		    }
		}
	    }

To indicate which method runs in a log file please always add Log.info("* Step started step_method_name");

In this way later on it is easy to find each executed step in the log file. It is enough just to look for "* Step started" keyword

Please use javadoc to document each step in the library. An example below

    /**
     * Verifies that response status code is {}
     * Creates new object ValidatableResponse and stores it as json ctx.obj
     *
     * Uses following objects:
     *  ctx.Object.response
     *
     * @param input - String, status code or value from storage
     *
     */
    @Then("^the status code is (.*)$")
    public void verify_status_code(String input){
        Log.info("* Step started verify_status_code");

        Long statusCode = StepCore.checkIfInputIsVariable(input);
        Integer code = statusCode.intValue();

        Response response = ctx.Object.get("response",Response.class);
        ValidatableResponse json = response.then().statusCode(code);
        ctx.Object.put("json",ValidatableResponse.class, json);
    }
    
Please use StepCore module for template management/compariosn, to attache file to the report etc.    
Please use FileCore module for any operation that shall be done on files.
Please use ExecutorCore module for any commands that shall be executed on the local host.
Please use SqlCore for any command that shall be executed in the dB under test.
Please use PageCore for any action that shall be done on the web page.
Please use Macro for macro evaluations.
Please use Storage to read/write new values to the storage.
Please use ctx.Object to pass objects between step defs.
Please use PdfCore module for pdf manipulation.
Please use SshCore module for ssh/scp/sftp execution.

It is also possible to write test data into a file. In this way it can be read later on and used during other test. Even though this created dependecies between tests it maybe useful some times. Please consider following example we run a long lasting test. Action that has to be trigger takes 10 minutes to execute. In this case there is no point to wait for its results. Instead it maybe desired to divide the test into 2 parts (feature files). One will be called to trigger the action. Second one can contain validation steps.
Second feture can be executed few minutes later and in the meantime other test can run.
In such case we have to extract test data that was used in the first part of the test (first feature file).

See an example below to understand how to write test data storage (or any other storage) to a text file.

	@reqResIn
	Feature: ReqResIn

	  Scenario: Tigger Post request to create single user

	      ...
	      And write storage TestData with id ReqRestInScenario1 to file
	      And read storage TestData with id ReqRestInScenario1 from file
	      ....

Steps that are involved will create a file that can contains the storage with identifier, like

	id1={key1:"value1", ...}
	id2={key2:"value2", ...}

Later on such storage can be retrived using the identifier and used during scenario execution.
File will be created in system temporary directory. Usually C:\Users\<user name>\AppData\Local\Temp\FK_Prototype_Persistent_Storage_File.json

Users also have a possibility to pass data between scenarios and features using so called execution context. This is not recommended and usually there exists a better way to write the test than using such feature, for example add Background scenario or enhance Given steps. 
To use this capability please see an example below.

	@reqResIn
	Feature: ReqResIn

	  Scenario: Tigger Post request to create single user

	    Given service is available
	    When json post request createUser is sent
	    Then extract user id as userId of type String

	  Scenario: Trigger a Get request to get a single user and validate the response

	    Given service is available
	    When json get single user with id userId of type String request is sent
	    Then verify that rest response body has
	      | key                        | action            | expected        |
	      | data.id                    | equalTo           | Expected.userId |

Step 'extract user id as userId of type String' extracts user id in first scenario and stores its value in the execution context (implemented using singelton pattern with lazy intializaton and synchronization).
      
    /**
     * extract user id from the response
     * Creates new object UserId and stores it as UserId ctx.obj
     *
     * Uses following objects:
     *  ctx.Object.response
     *
     */
    @Then("^extract user id as (.+) of type (.+)$")
    public void extract_user_id(String identifier, String type){
        Log.info("* Step started extract_user_id");

        ValidatableResponse response = ctx.Object.get("response",ValidatableResponse.class);
        String userId = response.extract().path("id");

        if ( userId == null || Integer.parseInt(userId) <= 0 ) {
            Log.error("UserId was not found in the response!");
        }

        Class clazz = ExecutionContext.executionContextObject().setType("java.lang." + type);
        ExecutionContext.executionContextObject().put(identifier,clazz,userId);
    }      
 
In this case user id will be stored as a string. Class type has to be provided by the user using method like below

	Class clazz = ExecutionContext.executionContextObject().setType("java.lang." + type);
 
To store the value please use method like below

        ExecutionContext.executionContextObject().put(identifier,clazz,userId);
 
It can be retrieved later on in the next Scenario or different Feature. See an example of step def implementation below.

    /**
     * Triggers http get request with variable path
     * ValidatableResponse is available as a context Object with name response.
     *
     * Uses following objects:
     *  ctx.Object.response
     *  Environment.REST_url
     *  Environment.Rest_url_get_path
     *
     */
    @When("^json get single user with id (.+) of type (.+) request is sent$")
    public void json_get_request_is_sent(String id, String type) throws Throwable {
        Log.info("* Step started json_get_single_user_with_id_is_sent");

        String url = Storage.get("Environment.Active.Rest.url");
        String path = Storage.get("Environment.Active.Rest.url_get_suffix");

        Class clazz = ExecutionContext.executionContextObject().setType("java.lang." + type);
        String userId = ExecutionContext.executionContextObject().get(id,clazz);

        Log.debug("userId is " + userId);
        url = url + path + userId;

        //build specification and use file template as a body content
        RequestSpecification request = given()
                .with()
                .contentType("application/json");

        //trigger request and log it (it will be added as attachment to the report)
        Response response = request
                .when()
                .log()
                .all()
                .get(url);

        //store response as ctx object so it can be verified by other steps and attach it to report
        ValidatableResponse vResp = response.then();
        ctx.Object.put("response",ValidatableResponse.class, vResp);
        StepCore.attachMessageToReport("Json response", response.prettyPrint());
    }
 
 Methods below are used to retrieve the value
 
        Class clazz = ExecutionContext.executionContextObject().setType("java.lang." + type);
        String userId = ExecutionContext.executionContextObject().get(id,clazz);
 
Please keep in mind that it is not recommended to use this feautre as it created dependencies between tests (Scenarios) that shall be avoided as much as possible. 
 
--------------------------------


How to write Page Object Model for web automation purposes?


Let us have a look at an example of a MainPage that can be used for web automation purposes. It comes from /src/test/java/libs/libDemoOnlineStore/modules.

	public class MainPage extends BasePage {

	    public MainPage(SharedContext ctx) {
		super(ctx);
		if(! isLoaded("ONLINE STORE | Toolsqa Dummy Test site")){
		    load();
		}
	    }

	    //selectors
	    private static final By allProductsSelector = By.xpath("(//*[@id='main-nav']/ul/li)[last()]");

	    public MainPage load(){
		String url = Environment.readProperty("WEB_url");
		PageCore.open(url);

		return new MainPage(ctx);
	    }

	    /**
	     * Navigates to all products page
	     *
	     * @return      ProductPage
	     *
	     */
	    public ProductPage goToAllProduct(){
		Log.debug("Click 'All Products' button");
		WebElement allProductButton = PageCore.findElement(allProductsSelector);
		allProductButton.click();

		PageCore.waitUntilTitleContains("Product Category | ONLINE STORE");

		return new ProductPage(ctx);
	    }

	}

Each class that contains methods to be executed on a specific web page shall contain Page in its name for example MainPage.class, ProductPage.class etc

It shall extend BasePage.class. In this way we have access to all the helper methods defined in the BasePage.class. Methods availabe there can be used to await for page load etc.

In the constructor we shall check if the page is loaded and if not decide what to do with it (either load it or write an error and mark test as failed).

Then we can define selectors as global variables in the class. We shall define methods load and isLoaded (if the one from BasePage is not enough -> we can overwrite it).

When access to SUT configruation is needed use Storage.get("Environment.Active." + input) method, for example

	String url = Storage.get("Environment.Active.Web.url");
	
To access the driver just call PageCore.findElement... Use previously defined selectors to find elements on the page and execute actions on them.

To use chaining in the step methods we shall return their class constructor or other Page constructor, for example 

	return new ProductPage(ctx);
	
With this approach steps class can be build like in an example below

	public class DemoOnlieSteps extends BaseSteps {

	    // PicoContainer injects class SharedContext
	    public DemoOnlieSteps (SharedContext ctx) {
		super(ctx);
	    }

	    //create global variables for this class
	    MainPage main;
	    ProductPage product;
	    CheckoutPage checkout;

	    /**
	     * Opens web page with url taken from environment configuration
	     *
	     * Uses following objects:
	     * env.WEB_url
	     *
	     */
	    @When("^open main page$")
	    public void i_open_main_page() throws Throwable {
		Log.info("* StepCore started i_open_main_page");
		//instantiate MainPage to open url in the browser
		main = new MainPage(ctx);
		main.load();
	    }

	    /**
	     * Navigates to all products page
	     */
	    @And("^navigate to all products page$")
	    public void navigate_to_all_products() throws Throwable{
		Log.info("* StepCore started navigate_to_all_products");
		product = main.goToAllProduct();
	    }

	    /**
	     * Adds product {} to the cart.
	     *
	     * @param productName  name or value from storage
	     *
	     */
	    @And("^add product (.*) to cart$")
	    public void add_product_to_cart(String productName) throws Throwable{
		Log.info("* StepCore started add_product_to_cart");

		String input = StepCore.checkIfInputIsVariable(productName);
		product.addToCart(input);
	    }

	    /**
	     * Adds product {} to the cart and navigates to checkout page.
	     *
	     * @param productName  name or value from storage
	     *
	     */
	    @And("^add product (.*) to cart and go to checkout$")
	    public void add_product_to_cart_and_checkout(String productName) throws Throwable{
		Log.info("* StepCore started add_product_to_cart_and_checkout");

		String input = StepCore.checkIfInputIsVariable(productName);
		checkout = product.addToCartAndCheckout(input);
	    }


	    /**
	     * Verifies that SubTotal field equals sub of total price per product type
	     * on Checkout page.
	     *
	     * Attaches screenshot to the report
	     */
	    @Then("^verify that SubTotal value equals sum of totals per product type$")
	    public void verify_sum_of_totals_per_product_type_equals_subTotal() throws Throwable{
		Log.info("* StepCore started verify_sum_of_totals_per_product_type_equals_subTotal");

		String totalPrice = checkout.getTotalPrice();
		ArrayList<String> totalPerProductType = checkout.getTotalPricePerProduct();

		Double sum = 0d;
		for(String price : totalPerProductType){
		    sum = sum + Double.valueOf(price);
		}

		byte[] screenshot = PageCore.takeScreenshot();
		StepCore.attachScreenshotToReport("Checkout_Products_Price_View", screenshot);

		Log.debug("Sum per product type is " + sum);
		Log.debug("Sub-Total is " + totalPrice);

		try {
		    assertEquals("Sub-Total value is different than sum of price per product type",
			    Double.valueOf(totalPrice),
			    sum);
		} catch ( AssertionError e ) {
		    Log.error("", e);
		}

		// or instead of assertEquals we can write simple code below
		//if ( ! Double.valueOf(totalPrice).equals(sum) ) {
		//    Log.error("Sub-Total value is different than sum of price per product type");
		//}
	    }
	}
	
Again we need to pass ctx object to the constructor and later on we can just call methods defined in each Page model.


Steps prepared in this way can be used to write a test. See an example below. File structure is

	features
		Web
			DemoOnlineShop
				config
					testdata.config
				DemoOnlineShop.feature
				

where feature file content is

	@demoOnline
	Feature: DemoOnlineShop

	  Scenario: Verify sum of 2 items equals total price
	    Given open browser
	    When open main page
	      And navigate to all products page
	      And add product TestData.product1 to cart
	      And add product TestData.product2 to cart and go to checkout
	    Then verify that SubTotal value equals sum of totals per product type

testdata content is

	TestData={
	    product1 : "iPhone 5",
	    product2 : "Magic Mouse"
	}

Please note that the web browser has to be explicitly open using step open browser. It is part of the CoreSteps and its content is

    /**
     * Opens browser of particular type as defined in the environment configuration
     */
    @Given("^open browser$")
    public void open_browser() throws Throwable {
        Log.info("* Step started open_browser");

	//create new selenium web driver
        EventFiringWebDriver driver = new DriverFactory(ctx).create();
        ctx.Object.put("Page", EventFiringWebDriver.class, driver);

	//initiate PageCore module
        PageCore pageCore = new PageCore(ctx);
        ctx.Object.put("PageCore", PageCore.class, pageCore);
        Log.debug("Web driver created");
    }

As can be see the main purpose of this step is to create new selenium web driver that can be used in tests.
Browser type can be provided via configuration. For details please see parameters mentioned below in src/test/java/config/framework/framework.config as well as in src/test/java/config/environment/default.config

	Environment={

	    Default : {

		WebDrivers : {
		    CloseBrowserAfterScenario : true,
		    Chrome : {
			path : "src\\test\\java\\resources\\chromedriver.exe"
		    },
		    FireFox : {
			path : "src\\test\\java\\resources\\geckodriver.exe"
		    },
		    InternetExplorer : {
			path : "src\\test\\java\\resources\\IEDriverServer.exe"
		    }
		}
		...




	Environment={

	    Default : {

		Web : {
		    browser : "Chrome",
		    timeout : 10,
		    url : "http://www.google.pl",
		    size : "Max" #width x height -> 1024 x 960
		}
	...


We have to provide path to a webDriver, browser type that shall be used in test and implicit timeout that will be used to report an exception if particular element will not be found on the page for amount of seconds defined.
Url that will be open can be defined as Environment.Default.Web.url.
It is also possible to provide browser width and height. To indicate max dimensions please use keyword "Max" else define them as String with format "width x height", for example "1024 x 960".

--------------------------------


How to write steps for rest/soap api?

What kind of test can/shall be performed for the rest/soap api? Tests that can excercise the most common usecases described as crud (create, read, update, delete). Such operations are executed by different http methods like post, get, put and delete. See table below for more details

	operation	rest method	tests
	create		post		post with all optional and required data
					post with only required data
					post with required data missing
					post with invalid data for parameters
	read		get		get when profile doesn't exist
					get when profile exists
					create profile then get
					update profile then get
					delete prifile then get
	update		put		update existsing profile
					update non-existing profile
					update deleted profile
					update then update again
	delete		delete		delete profile
					delete when there are depenedencies
					delete after delete
					delete non-existing
		
In practice typical rest implementation using http protocol for message transport and json for data transfer.
In case we are dealing with soap api http is used for message transport and xml for data transfer.

RestAssured is integrated which means we can either build rest/soap requests using method exposed by RestAssured or easier use template files to store message body (either json or xml) and read the message body from such template.
This method is usually very easy to use because templates can be created using any other tool available like SopaUi or chrome plugins.
Template support implemented in the framework allows us to put into the template data from configuration files and macros.
The biggest drawback of this method is the fact that when we are dealing with an application that exposes a lot of different services number of templates can grow. So smart management of templates shall be used. For example whenever possible use global template storage. Templates used to excercise one service shall be grouped together under same directory etc.

Please find below an example of step used to build rest post request with json body.

    /**
     * Triggers http post request with json body. Content of the body comes from the file.
     * ValidatableResponse is available as a context Object with name response.
     *
     * Uses following objects:
     *  ctx.Object.response
     *  Environment.Active.Rest.url
     *  Environment.Active.Rest.url_post_suffix
     *
     * @param name, String, name of the template that contains http body of the request
     */
    @When("^json post request (.*?) is sent$")
    public void json_post_request_is_sent(String name) {
        Log.info("* Step started json_post_request_is_sent");

	//read url base
        String url = Storage.get("Environment.Active.Rest.url");
        
	//read url path
	String path = Storage.get("Environment.Active.Rest.url_post_suffix");

	//build url from base and path because it can be different for each kind of request post/put/get/delete etc.
        url = url + path;

	//inject values to the the template
        File file = StepCore.evaluateTemplate(name);

        //build request specification and use file template as a body content, add http headers if any are required here
        RequestSpecification request = given()
                .body(file)
                .with()
                .contentType("application/json");

        //trigger request and log it (it will be added as an attachment to the report)
        Response response = request
                .when()
                .log()
                .all()
                .post(url);

        //store response as ctx object so it can be verified by other steps and attach it to the report
        ValidatableResponse vResp = response.then(); 
	ctx.Object.put("response",ValidatableResponse.class, vResp);
        StepCore.attachMessageToReport("Json response", response.prettyPrint().toString());
    }


where template content can be

	{
	    "name": "${ctx.TestData.name}",
	    "job": "${ctx.TestData.job}"
	}

and TestData content can be

	TestData:{
	    name : "morpheus",
	    job  : "leader"
	}

In case of soap we can build the request in this way

    /**
     * Triggers http post request with xml body (soap). Content of the body comes from the file.
     * ValidatableResponse is available as a context Object with name response.
     *
     * Uses following objects:
     *  ctx.Object.response
     *  Environment.Active.Rest.url
     *
     * @param name, String, name of the template that contains http body of the request
     * @param actionHeader, String, soap action that will be set in the header
     */
    @When("^xml post request (.*?) with soap action header (.*?) is sent$")
    public void xml_post_request_is_sent(String name, String actionHeader) {
        Log.info("* Step started xml_post_request_is_sent");

	//in case multiple services are present each will have its own url available
        String url = Storage.get("Environment.Active.Rest.url");
        
	//inject values to the template
	File file = StepCore.evaluateTemplate(name);
        String sFile = FileCore.readToString(file);
	 
	//add action header or any other http headers if required
	String sAction = StepCore.checkIfInputIsVariable(actionHeader);
       
        //build specification and use file template as a body content
        RequestSpecification request = given()
                .header("SOAPAction", sAction)
                .body(sFile)
                .with()
                .contentType("text/xml");

        //trigger request and log it (it will be added as an attachment to the report)
        Response response = request
                .when()
                .log()
                .all()
                .post(url);

        //store response as ctx object so it can be verified by other steps and attach it to report
        ValidatableResponse vResp = response.then();
        ctx.Object.put("response",ValidatableResponse.class, vResp);
        StepCore.attachMessageToReport("Xml response", response.prettyPrint().toString());
    }

where template content can be

	<?xml version="1.0" encoding="utf-8"?>
	<soap12:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap12="http://www.w3.org/2003/05/soap-envelope">
	  <soap12:Body>
	    <GetCitiesByCountry xmlns="http://www.webserviceX.NET">
	      <CountryName>${ctx.TestData.countryName}</CountryName>
	    </GetCitiesByCountry>
	  </soap12:Body>
	</soap12:Envelope>

and TestData content can be

	TestData:{
	    soapActionHeader : "http://www.webserviceX.NET/GetCitiesByCountry",
	    countryName : "Poland"
	}

In case we would like to trigger any other request like get we have to change the method used by RestAssured. See example below

    /**
     * Verifies that service is available
     * This is just a sanity check.
     * It triggers GET request towards defined url
     * It checks that http response code is 200 (OK)
     *
     * Uses following objects:
     *  Expected.statusOK
     *  Environment.Active.Rest.url
     *
     */
    @Given("^service is available$")
    public void service_is_available() {
        Log.info("* Step started service_is_available");

        String url = Storage.get("Environment.Active.Rest.url");
        Long statusCode = Storage.get("Expected.statusOK");
        Integer expectedCode = statusCode.intValue();
        try {
            given()
                .when()
                .log()
                .all()
                .get(url)
                    .then()
                    .statusCode(expectedCode);
        } catch (AssertionError e) {
            Log.error("", e);
        }
    }


Now we can build a test to trigger http request towards SUT. File structure can be

features
	Rest
		ReqResIn
			config
				testdata.config
				expected.config
			template
				createUser.template
			ReqRestIn.feature
			
Feature content can be

	@reqResIn
	Feature: ReqResIn

	  Scenario: Tigger Post request to create single user

		Given service is available
	    	When json post request createUser is sent

Similar for soap case.

The most important part of the test case will be to validate content of the response. Here we can either compare it with a template or use another step to compare required fields value with the expected ones. This can be done like in example below

	@globalWeather
	Feature: GlobalWeather

	  Scenario: Tigger Post request

	    Given xml post request post1 with soap action header TestData.SoapActionHeader is sent
	    When the status code is Expected.statusOK
	    Then verify that rest response has
	      | GetCitiesByCountryResponse.GetCitiesByCountryResult.NewDataSet.table[1].city | equalTo | Expected.CityName |

Another example is available below

	@bookByIsbn
	Feature: Get book by ISBN
	  Scenario: User calls web service to get a book by its ISBN
	    Given a book exists with an isbn
	    When a user retrieves the book by isbn
	    Then the status code is Expected.statusOK

	      And response includes the following
		| totalItems 	 		                    | Expected.totalItems   |
		| kind					            | Expected.kind         |

	      And response includes the following in any order
		| items.volumeInfo.title 				| Expected.title	|
		| items.volumeInfo.publisher 				| Expected.publisher	|
		| items.volumeInfo.pageCount 				| Expected.pageCount	|

	      And response includes the following
		| totalItems 	        	                	| 1                  |
		| kind					                | books#volumes      |
	      And response includes the following in any order
		| items.volumeInfo.title 				| Steve Jobs	     |
		| items.volumeInfo.publisher 				| Simon and Schuster |
		| items.volumeInfo.pageCount 				| 630	             |

	      And verify that rest response has
		| key                           | action             | expected                        |
		| totalItems                    | lessThan           | Expected.highAmountOfTotalItems |
		| kind                          | containsString     | Expected.partOfKind             |
		| items.volumeInfo.title        | containsInAnyOrder | Expected.title	               |
		| items[0].volumeInfo.pageCount | greaterThan        | Expected.lowPageCount           |
		| items[0].volumeInfo.authors   | containsInAnyOrder | Expected.author                 |

	      And verify that rest response has
		| key                           | action             | expected                |
		| totalItems                    | lessThan           | 99                      |
		| kind                          | containsString     | volumes                 |
		| items.volumeInfo.title        | containsInAnyOrder | Steve Jobs	       |
		| items[0].volumeInfo.pageCount | greaterThan 	     | 110                     |
		| items[0].volumeInfo.authors   | containsInAnyOrder | Walter Isaacson         |
		
Step "verify that rest response has" is part of CoreSteps and implements just few checks that seems to be most useful. Other checks can be added in the future when needed. It is interesteing because it uses a data table to pass multiple parameters to it. It is implemented as follows.

    /**
     * Verifies that particular key xml/json body response contains expected value
     * Multiple different comparisons can be executed. Following actions are supported
     * equalTo, containsString, containsInAnyOrder, greaterThan, lessThan
     *
     * @param table, DataTable, it shall contains 3 columns key, action, expected
     */
    @Then("^verify that rest response has$")
    public void verify_that_response_has(List<Map<String, String>> table) {
        Log.info("* Step started verify_that_response_has");

	//get json or xml response
        Response response = ctx.Object.get("response",Response.class);
        ValidatableResponse vResp = response.then();

        //get rows
        for (int i = 0; i < table.size(); i++) {
            Map<String, String> row = table.get(i);

            Log.debug("Row is " + row);
            //get columns
            String key = null;
            String action = null;
            Object expectedValue = null;
            for (Map.Entry<String, String> column : row.entrySet()) {
                //get name of the column
                String name = column.getKey();
                //get value of that column for current row
                String valueInRow = column.getValue();

                //assign values from columns to variables
                if ( name.equalsIgnoreCase("key") ){
                    key = valueInRow;
                    continue;
                }
                if ( name.equalsIgnoreCase("action") ){
                    action = valueInRow;
                    continue;
                }
                if ( name.equalsIgnoreCase("expected") ){
                    expectedValue = StepCore.checkIfInputIsVariable(valueInRow);
                }

                //execute simple error handling
                if(key == null){
                    Log.error("key in verify step table does not exist or null!");
                }
                if(action == null){
                    Log.error("key in verify step table does not exist or null!");
                }
                if (key.equals("")) {
                    Log.error("key in verify step table is an empty string!");
                }
                if (action.equals("")) {
                    Log.error("action in verify step table is an empty string!");
                }

                //execute comparison
                AssertCore.validatableResponseBodyTableAssertion(vResp, key, action, expectedValue);
            }
        }
    }

As can be seen it uses AssertCore module for validation purposes. It supports simple access to every response parameter using notation key.subkey and takes the response from ctx Object with name "response".

One thing to remember is to always close unused connections so we can avoid situation where SUT will not allow for any more new sessions because the pool of available connections will be used up.
To do so a RestAssured configuration can be adjusted. Please see an excerpt from libs/libCore/HooksSteps.java

Following configuration is used

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
                    new ConnectionConfig().closeIdleConnectionsAfterEachResponseAfter(
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

User can change it if needed for a particular project. To do so please edit following parameters in configuration file, for example under src/test/java/config/environment/default.config. Default settings are visible below.

	Environment:{

	    Default: {

		    Rest: {
		    closeIdleConnectionsAfterEachResponseAfter: true,
		    closeIdleConnectionsAfterEachResponseAfter_idleTime: 10,
		    reuseHttpClientInstance: true,
		    http_maxConnections: 100
		}
		...

--------------------------------


How to write steps for dB tests?

dB support maybe be useful in few cases, for example to load test data directly to dB (this approach is not recomended as dB structure can change with product development -> it is better to use exposed api, usually rest or file/bulk/batch interface to load data to dB) or to test that table data after some transformation is correct (like due to the user interaction via web, or rest client, etl process etc). In addition to that there maybe be a need to support procedure/scripts execution. It seems that the best approach to do it is to simply call a cmd client delivered with particular db, for example sqlplus in case of oracle via executor module.

In case we are working with BI system that maybe used for data analysis purposes it may happen that data will be loaded to the db from multiple sources and modified by so called ETL (extract, transform, load) process. In this case there maybe a need to compare data before and after modification. When amount of data is high (millions of rows) template comparison may not be the optimal solution.
One can consider to use sql capabilitis to backup an existing table and compare its conetent with a table after modification.

A simple feture file that can load data from csv file to the db can look like this

	@db
	Feature: Basic

	  Scenario: Load data from csv file to dB

	    Given open db
	      And data from input csv file is loaded to table Dept
	    When simple select is executed
	    Then validate that result is like expectedOutput

File structure is

	features
		DB
			Basic
				config
					expected.config
					testdata.config
				input
					input.csv
				template
					expectedOutput.template
				Basic.feature

where content of test data is

	TestData : {
	    inputTypeMapping : ["NUMERIC","VARCHAR","VARCHAR"]
	}

It contains list of types used for loading the data from csv file to the table. Each column from csv file has a coresponding data type. File name is used to distinguish between different mappings. For example if input file name is input.csv, mapping instance name shall be inputTypeMapping. Mapping between java and sql types is available for example here https://www.service-architecture.com/articles/database/mapping_sql_and_java_data_types.html

content of expected data is 

	Expected:{
	     lastId : "40",
	     lastCity : "BOSTON"
	 }
	 
content of input file is

	DEPTNO, DNAME, LOC
	10, ACCOUNTING, NEW YORK
	20, RESEARCH, DALLAS
	30, SALES, CHICAGO
	40, OPERATIONS, BOSTON

content of the expectedOutput template is

	DEPTNO, DNAME, LOC
	[0-9]{2}, ACCOUNTING, NEW YORK
	20, [A-Z]+, DALLAS
	\d+, SALES, CHICAGO
	${Expected.lastId}, OPERATIONS, ${ctx.Expected.lastCity}

As can be seen it uses regexp and variable parts so we need to evaluate it before comparison will be done.
Now we just need to write a step that will read the content of csv and load it to our dB. Such step can look like this

    @When("^data from (.*?) csv file is loaded to table (.*?)$")
    public void data_from_csv_file_is_loaded(String fileName, String tableName){
        Log.info("* Step started data_from_csv_file_is_loaded");

        File input = new File(FileCore.getCurrentFeatureDirPath() + "/input/" +fileName+".csv");
        SqlCore.insertFromFile(input,tableName,true, "TestData."+fileName+"TypeMapping");
    }

Before we can load the data dB connection have to be open. For this we will use a step from CoreSteps.

    /**
     * Opens jdbc connection to database
     */
    @Given("^open db$")
    public void open_db() throws Throwable {
        Log.info("* Step started open_db");

        Connection connection = new DBConnector(ctx).create();
        ctx.Object.put("Sql", Connection.class, connection);

        SqlCore sqlCore = new SqlCore(ctx);
        ctx.Object.put("SqlCore", SqlCore.class, sqlCore);
        Log.debug("Connected to the data base");
    }
    
It will open a new resource (connection) towards selected dB. Db can be set via environment configruation. See parameters mentioned below in src/test/java/config/framework/framework.config as well as src/test/java/config/environment/default.config
 
	Environment={

	    Default : {
		    JdbcDrivers : {
		    Oracle : {
			path : "src\\test\\java\\resources\\ojdbc6.jar"
		    }
		}
		...
	


	Environment={

	    Default : {
		    Jdbc : {
		    url : "jdbc:oracle:thin:scott/oracle@localhost:1521/XE"
		}

 
First of them points to the directory with the jdbc drivers and second one configures connection url that is going to be used. String jdbc:oracle indicates that oracle driver shall be used.
 
Now let's try to execute a simple select statement to extract previously inserted data

    @When("^simple select is executed$")
    public void simple_select_is_executed_with_db_utils(){
        Log.info("* Step started simple_select_is_executed");

        List<Map<String,Object>> list = SqlCore.selectList("SELECT * FROM Dept");

        SqlCore.printList(list);
        File results = SqlCore.writeListToFile(list,"SqlResult","txt");

        ctx.Object.put("SqlResults",File.class, results);
    }
    
Results will be stored as ctx Object SqlResults for validation pruposes which can be done by other step def. They will be printed to the console and to a file for the purpose of template comparison.
To make writing of such steps as simple as possible please use SqlCore module.
Becuase our data set is very small we will use template compariosn

    @Then("^validate that result is like (.*)$")
    public void validate_that_result_is_like(String templateName) throws Throwable {
        Log.info("* Step started validate_that_result_is_like");

        File toCompare = ctx.Object.get("SqlResults",File.class);
        String path = toCompare.getAbsolutePath();

        StepCore.attachFileToReport("SqlQueryResult.txt","text/plain",path);
        StepCore.compareWithTemplate(templateName, path);
    }
    
It maybe a better idea for a huge data set to compare 2 tables in sql. For example by executing query like below
 
 	select from table a
	except
	select from table b 

Where table a and table b are tables we would like to compare. If no rows is returned this means that both tables a and b are the same.

In case table a was modified by some ETL we can backup it by executing

	select * into a_backup from a
	
After that we can compare content of a table before and after modification using previously privide query. Of course it is also possible to use select column1, column2... instead of select * if all we want to do is to compare just selected columns.

DB connection will be automatically closed in @After hook.

 
--------------------------------


How to use executor to run system commands or 3rd party apps?



It is often needed and desired to have a possibility to execute any system command on a local host. Usually this can be used to trigger powershell commands or batch scripts on windows host but it maybe used to integrate any 3rd party application as well like wireshark to catch network traces or autoIT to have a possibility to automate application under windows etc.

Such possibility is available by means of ExecutorCore module. It contains functions that allows us to call any command or app.
Lets have a look at very simple step def that checks which java version is installed on the system.

Feature file can look like that

	@exec
	Feature: SimpleCommand

	  Scenario: Execute simple command on local host

	    Given execute sample command
	    
where file structure is like

	features
		Executor
			test1
				SimpleCommand.feature
				
Our "execute sample command" step def can look like

    @Given("^execute sample command$")
    public void execute_sample_command() throws Throwable {
        Log.info("* Step started execute_sample_command");

        String cmd = "java -version";

        File workingDir = FileCore.createTempDir();

        ByteArrayOutputStream out = ExecutorCore.execute(cmd, workingDir, 10, true);

        Log.debug("Output is ");
        Log.debug(new String(out.toByteArray(), Charset.defaultCharset()));
    }
    
As can be seen we are calling execute method from ExecutorCore module. It expects a working directory in which command shall be executed, command to be executed itself, timeout and a flag indicating whether to call this command in the background or not.
This method returns the output (StdOut and StdErr). So it is very easy to print it to the log or attach it to a file.

Output of the command execution can also be exposed as a context Object for furter validation and processing. Let's have a look at more complex example where a powershell command is used to generate a file and then read it.

Our feature file can look like this

	@exec
	Feature: ReadHugeFile

	  Scenario: Execute simple command on local host

	    Given new text file is created
	    When read the file
	    
File structure is

	features
		Executor
			test2
				ReadHugeFile.feature
				
Steps that are used can look like follows

    @Given("^new text file is created$")
    public void new_text_file_is_created() throws Throwable {
        Log.info("* Step started new_text_file_is_created");

        String cmd = "powershell.exe " +
                "\"$stream = [System.IO.StreamWriter] " +
                "'t2.txt';" +
                "$line = 'testTestTESTtestTestTESTtestTestTESTtestTestTESTtestTestTESTtestTestTESTtestTestTEST';" +
                "1..100000 | % {$stream.WriteLine($line)};" +
                "$stream.close()\"";

        File workingDir = FileCore.createTempDir();
        String sWorkingDirPath = workingDir.getAbsolutePath();
        ctx.Object.put("WorkingDir", String.class, sWorkingDirPath);

        ByteArrayOutputStream out = ExecutorCore.execute(cmd, workingDir, 10, true);

        Log.debug("Output is ");
        Log.debug(new String(out.toByteArray(), Charset.defaultCharset()));
    }

As can be seen step "new test file is created" will create w temporary directory and call a powershell script inside. It will create a new file with 100000 lines inside.

    @When("^read the file$")
    public void read_the_file() throws Throwable {
        Log.info("* Step started read_the_file");

        String path = ctx.Object.get("WorkingDir", String.class);
        String cmd = "powershell.exe 'Get-Content -Path " + path + "\\t2.txt'";

        File workingDir = FileCore.createTempDir();

        ByteArrayOutputStream out = ExecutorCore.execute(cmd, workingDir, 10, true);

        Log.debug("Output is ");
        Log.debug(new String(out.toByteArray(), Charset.defaultCharset()));
    }

Path to this directory can be given as a context Object to next step "read the file" that can read that file using powershell and print its content to the log file.


Pause test execution


ExecutorCore can be used to run 3rd party apps like autoIT framework which can be used to automate windows applications or create a pop up window to inform user about status of some action. One of the ideas how this can be used is to create a pause step def. It can pasue execution of a scenario and inform user about it. User can wait for a timeout or manually cancel the pause message box. An usage example is below.

	@exec
	Feature: SimpleCommand

	  Scenario: Execute simple command on local host

	    Given execute sample command
	    And pause execution
	    And execute sample command


Step def can be implemented like below.

    @And("^pause execution$")
    public void pause_execution() throws Throwable {
        Log.info("* Step started pause execution");

        File workingDir = FileCore.createTempDir();
        String autoItPath = Storage.get("Environment.Active.apps.autoIt");
        String scriptsPath = Storage.get("Environment.Active.scripts.path");
        Integer timeout = Storage.get("Environment.Active.PauseDuration");

        String cmd = autoItPath + " " + FileCore.getProjectPath() + "\\" + scriptsPath + "\\pause.exe" + " " + Integer.toString(timeout);

        Log.debug("Calling autoIt pause script with timeout " + timeout + " seconds");

        ExecutorCore.execute(cmd, workingDir, timeout+3, true);

        Log.debug("Pause canceled or timeout. Resuming execution");
    }


--------------------------------


Read pdf file




PdfCore can be used to read/write pdf files. It such possibility maybe useful to validate pdf files content. They maybe created as an output of a test. For example in case test shall validate content of an invoice etc.
Step that reads pdf file can look like this

    /**
     * Reads unencrypted pdf file line by line and prints it content to the log file
     *
     * @param pathToFile, String, path to pdf file
     */
    @Given("^read pdf file from (.+)$")
    public void read_pdf_file_from(String pathToFile) throws Throwable {
        Log.info("* Step started read_pdf_file_from");

        String path = StepCore.checkIfInputIsVariable(pathToFile);

        File file = new File(path);
        if (! file.exists()) {
            Log.error("File " + path + " does not exists");
        }

        Log.debug("Reading pdf file " + file.getAbsolutePath());
        List<String> lines = PdfCore.readLines(file);

        for (String line : lines) {
            Log.debug(line);
        }
    }
    
Output of PdfCore.readLines(file) can be used in other step for validation purposes. Feature file can look like this


	@pdf
	Feature: ReadPdf

	  Scenario: read pdf file content and print it to the log file

	    Given read pdf file from TestData.file


--------------------------------



Usage of ssh/scp/sftp




User can use ssh to execute commands on remote unix hosts. Scp can be used to transfer files between tester's workstation and sytsem under test as well as sftp/ftp.
To use this feature we have to define ssh nodes in the configruation. Configuration is falt. This means that for each node we shall have a seperate entry in the config file. See an excerpt from /src/test/java/config/environment/default.config below.

	Environment:{

	    Default: {

		    Ssh: {
		    node1: {
			host: "127.0.0.1",
			port: 4567,
			user: "vagrant",
			password: "vagrant"
		    }
		}
		...

Where node1 is an identifier of an ssh node. We can configure multiple nodes in this way. By default port number 22 is going to be used.

User can have 2 options to interact with a node via ssh. It can execute a command in a session. After each command execution session is closed. For the next command new session is created. This is useful in case there are simple commands to be executed or user is interested in the stdout or exit status code. Alternatively user can open a shell and execute multiple commands in an interactive shell session. This is useful when user wants to for example switch to superuser account, run tcpdump, await for command execution etc.
In this case it is possible to define a timeout and expected output in the console. In case command does not return any output to the console user can append echo to make sure that something will be printed or await for a prompt symbol.

SshCore module is provided and contains a set of methods that can be use to manage ssh sessions as well as execute common tasks like for example check that node is accessible, wait for a file to be present on remote host or simply check that file exists.

See some example below how to start simple session or shell session.

Feature file is

	@ssh
	Feature: Ssh

	  Scenario: list files in users home directory

	    Given list files in users home directory

Step implementation is available below

    @When("^list files in users home directory$")
    public void list_files_in_users_home_directory() throws Throwable {
        Log.info("* Step started list_files_in_users_home_directory");

        String singleCmd = "ls";

        Log.debug("Create new client and connect to node1");
        SshCore.createClient("node1");
        Log.debug("Command to execute vis ssh is " + singleCmd);
        SSHResult result = SshCore.execute(singleCmd, 10);
        Log.debug("Result is " + result.getStdout());
        Log.debug("Exit code is " + result.getExitCode());
        SshCore.closeClient();
    }
    
As can be seen user can open new ssh session by calling SshCore.createClient(node), where node is taken from configuration object Environment.Active.Ssh. Please note that only 1 connection can be open at a time. That is fine because ssh execution is very quick. So we can connect to each node one by one and execute commands on each of them in a sequence. Please remember to close the client when ssh is not needed any more.
 

Similar for shell sessions. See an example below.
Feature file is

	@ssh
	Feature: Ssh

	  Scenario: switch user to root

	    Given switch user to root
	    
Where step can be implemeted like below

    @When("^switch user to root$")
    public void switch_user_to_root() throws Throwable {
        Log.info("* Step started switch_user_to_root");

        String userChangeCmd = "su - root";
        String passOfUserCmd = "vagrant";
        String validateCmd = "whoami";

        Log.debug("Create new client and connect to node1");
        SshCore.createClient("node1");
        SshCore.startShell(10);
        SshCore.executeInShell("", "$");
        Log.debug("Command to execute vis ssh is " + userChangeCmd);
        SshCore.executeInShell(userChangeCmd, "Password");
        Log.debug("Command to execute vis ssh is " + passOfUserCmd);
        SshCore.executeInShell(passOfUserCmd, "root@");
        Log.debug("Command to execute vis ssh is " + validateCmd);
        SshCore.executeInShell(validateCmd, "root");
        SshCore.closeShell();
        SshCore.closeClient();
    }
    
In this case user needs to create new client and open a session. From that point he can execute commands in a shell. Stdout can also be assigned to variable of type SSHResult if user would like to access it.

Now let us have a look how we can download a file using scp. 
Feature file is

	@ssh
	Feature: Ssh

	  Scenario: download file from users home dir

	    Given check that file exists on remote node

Step can be implemented like below.

    @When("^check that file exists on remote node$")
    public void check_that_file_exists_on_remote_node() throws Throwable {
        Log.info("* Step started check_that_file_exists_on_remote_node");

        Log.debug("Check that node is alive");
        Boolean isAlive = SshCore.checkThatNodeIsAlive("node1");

        if ( ! isAlive ) {
            Log.error("Host node1 is not available");
        }

        Log.debug("Check that file is present on the remote host");
        String pathToFile = "postinstall.sh";
        Boolean isAvailable = SshCore.checkThatFileExists("node1", pathToFile);

        if ( ! isAvailable ){
            Log.error("File postinstall.sh was not found");
        }

        Log.debug("Download file via scp");
        File file = SshCore.downloadFileViaScp("node1","postinstall.sh","C:\\Users\\akowa\\Documents\\Projects\\FK_Prototype");
        Log.debug("Path to file is " + file.getAbsolutePath());
    }

First it checks that node is alive. Then it checks if file is available and uses scp to download it to the directory on the local file system.

Please note that SshCore contains methods to download/upload files via scp and sftp.
 
