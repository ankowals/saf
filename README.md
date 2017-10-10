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
	a way to execute any command on remote host over ssh
	a way to manage and configure test environment
	a way to manage and configure test data
	a way to prepare/calculate test data at runtime (macros)
	a way to manage and code a set of common modules/step/functions to be used for testing purposes
	a way to downlaod any 3rd party symptoms from SUT like logs, trace files
	a way to log any activity dony by the framework
	a way to report test status
	a way to attach logs/files/messages/screenshots to the report
	a way to start test from IDE and command line as well as an ability to overwrite parameters when test suite started from cmd
	a way to automatically deploy the framework under windows (dependency management)
	a way to share the code/tests between testers to increae re-usability (version control system)
	a way to monitor and indicate quality of commited tests (see SonarQube for example )
	a way to support PageObject model for web automation purposes
	a way to share common data between steps (dependency injection)
	a way to schedule test execution (see Jenkins/TeamCity)
	a way to generate test documentation automatically
	a way to manage multiple projects (version control system)
	a way to pause test execution and allow for manual intervention (integrate autoIT pause script)
	a way to integrate with test/requirement management tool (like for example jira, so we can have links to tests/epic/stories in the test report)
	a way to intgrate with incident management tool (like for example jira, so we can have at least links to defects that affect particular test in the report and maybe their status etc.)
	a way to write simmple step defs (for example usage of try-catch blocks is ugly)
	a way to manage templates
	
	a way to integrate with configuration management tool can be a nice addon (like Puppet, Ansible or Chef)
	a way to re-run failed test can be a nice addon
	a way to execute test remotely can be a nice addon

----------------------------------

Where are we now?
	
	(in progress) documentation update
	(in progress) a way to execute tests related to  
		(done) - rest json/xml (soap) => RestAssured integrated 
		(done) - gui (web/native) => Selenium WebDriver integrated for chrome
		(done) - sql => jdbc integrated for oracle
		(to do) - mobile => Appium integration
		(to do) - pdf validation 
		- others??
		
		further enhancements:
			- add support for more browsers
			- add support for setting of browser width/high
			- enhance logging to log more exceptions
			- add more helper functions
			
	----------------------------------	
		
	(to do) a way to execute any command on remote host over ssh	
	(to do) a way to downlaod any 3rd party symptoms from SUT like logs, trace files
	(to do) a way to monitor and indicate quality of commited tests (see SonarQube for example )
	(to do) a way to schedule test execution (see Jenkins/TeamCity)
	(to do) a way to generate test documentation automatically => add new login categories, use scenrio outline with path to feature and log file after test execution	
	(to do) a way to pause test execution and allow for manual intervention => via autoIt script executed from a step def?	
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
	
----------------------------------

How can we use test automation framework?

	to automate functional tests
	to automate acceptance tests (after any deployment)
	to automate integration tests
	to automate regression tests
	to execute sanity chcecks and make sure that SUT configuration is correct (like all urls are reachable, ports open, interfaces are up & apps are running, login is possible for each user etc.)
	to gether symptoms like traces/logs/tickets/events from multiple systems under test
	to execute test system bringup and feed it with configruation data before test starts
	to setup test environment using configuration management sytsem before test suite execution
	to restore the system to the state before test started
	to move configuration data between test systems
	to use tests implementation as test documentation
	to describe system behavior via tests implementation (using Gherkin) - living documentation
	to use automated equipment for any not strictly test related activities like for example automate mobile phones to detect changes in the offer from a telco operator:)
	
	load genration/performance checks are out of scope
	parallel test execution is out of scope for now
	
	For parallel test execution one can use capabilities of framework or use multiple VMs to deploy multiple SUTs, framework instances, scheduler instances etc... 
	in that case it has to be ensured that tests are seperated from each other 
		- subsequent test does not depend on the result of previous test 
		- tests are using separate test data/config data (do not operate on the same config data at the same time to avoid concurent modifciation)

----------------------------------

Installation instructions


	1 install java jdk
	2 set an environment variable, for example JDK_HOME=C:\Program Files\Java\jdk1.8.0_144
	3 verify java installation; in cmd issue java -version
	4 download and extract maven binary
	5 set an environment variable, for example M2_HOME=C:\apache-maven-3.5.0
	6 append %M2_HOME%\bin to PATH variable
	7 verify maven installation; in cmd issue mvn -version
	8 install intlliJ community edition -> this is our IDE in which tests can be written
	9 download Selenium Chrome driver and other drivers if needed and put it for example in C:\SeleniumWebdrivers
	10 download JDBC oracle driver and other drivers if needed and put it for example in C:\JdbcDrivers
	11 in intelliJ go to Files->Settings->Plugins->Browse repositories and install Cucumber for Java plugin
	12 restart or log out and log in so changes done to Path variable will be visible
	13 install git
	14 clone the repo for example to C:\Documents\Projects\SAF
	15 in case of issues with JDK not found fix its path in pom.xml file under <executable> tag

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
					<source>1.${java.version}</source>
					<target>1.${java.version}</target>
                    <fork>true</fork>
                    <executable>C:\Program Files\Java\jdk1.8.0_144\bin\javac</executable>
                </configuration>
            </plugin>

	16 Fix path to web drivers in /src/resources/config/environment/default.properties
	17 Fix path to jdbc drivers in /src/resources/config/environment/default.properties
	18 Change the port number if needed (default is 8082) for jetty to see allure report after test execution
	

----------------------------------


How to imoprt project in IntelliJ?

	1 open the IDE and click “Import Project”
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
					- modules
						- core
						- pages.DemoOnlieStore
					- steps
						- core
						- DemoOnlieStore
				- resources
					- config
					- features
						- Rest
						- Web
							- DemoOnlieShop
		- target



Dir src/test/java/modules contains all methods needed to run the test.

Dir src/test/java/steps contains all step defs needed to run the test.



Subdirectory core cotnains saf freamework steps and methods. It is mandatory to have it in each project.

Other subdriectories contains project specific stuff like page obejct models etc. They are optional and can be added as git submodules for example.



Dir src/test/resources contains configuration files (*.properites and *.config) as well as features files (cotntainers for tests).



Dir traget/ will be used to store results of test execution.

 

--------------------------------



General concepts



We follow BDD apporach. Reason is very simple. It is usually much easier for testers to write automated tests (following Gherking principles). In large projects (with large and separate teams of testers, analysts, devs) BDD main adventage so called common language to describe sytsem behaviours can be rarely seen but it is still giving testers the benefit of simpler tests implementation.

Tests are called Scenarios. They are grouped in Features.

Features act as containers for Scenarios.

We try to keep 1 Feature per 1 file.

Feature file name shall be same like Feature name.

Scenario names shall be unique per feature.



Test execution shall look like this

	     ConfigurationData ------->	System Under Test 
						^	
	TestData & ExpectedData -----> | execution engine | -----> ResultData 	=> test result (OK/NOK)
						^
				    	user actions (steps)



Test result is the result of comparison between ResultData & ExpectedData


Framework will execute each scenario in a feture file.

Each scenario execution looks similar.



First TestData storage and ExpectedData storage will be created. 

Macro evaluation will be done.

Execution engine will connect to SUT and execute any step (action) that is described in the scenario.

Last step is to verify recieved test resutls against expected resutls from the storage.

Test report will be created.



TestData/ExpectedData can be passed to the steps directly in a feature file or can be taken from a *.config file.

Global configuration is available but it can be overwritten/updated by local config.

Config files will be loaded automatically as long as feature file name is same as feature name defined inside the feature file file!!!

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



To make installation and deployment easy so called project dependency and management tool is used. It is called maven.

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


On top of that macro support, test data management, configuration files support, Page Object Model support and more was added.

Project and test structure is also enforced to keep things consistent.



--------------------------------



To run a test from windows cmd please execute


	cd <install_dir>
	mvn clean test -Dcucumber.options="--tags @bookByIsbn"
	mvn site
	mvn jetty:run
	go to http://localhost:8082


Please note that usage of clean keywords ensures that artifacts from previous test execution are removed first.

One can also use IntelliJ to run a feature file. In that case only log file will be created.

To generate a report from test please execute mvn site, mvn jetty:run to run jetty and check the report in the browser under http://localhost:<port>.

It is possible to overwrite active_env property from the command line. In that case project specific config as specified by the CMD argument will be used during test execution. To do so please execute a test for example like below

	mvn clean test -Dactive_env="bookByIsbn" -Dcucumber.options="--tags @bookByIsbn"

In this particular case a default environment (SUT) configuration will be loaded and later on it will be overwritten by config available in a file bookByIsbn.properties. Cucumber option --tags can be used to run only a subset of tests that are tagged with @bookByIsbn tag.

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

As can be seen test data/expected data can be either hardcoded in the feature file or taken from configruation file. 

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

--------------------------------



Details of what is happening during test execution



Cucmber runner is available in src/test/java/steps/core/TestRunner.class

It contains cucumber options like glue path (path to steps definitions), features path and allure report plugin.

Its content shall not be changed for test purposes.



Before each scenario execution so called @Before and @After hooks are run.

In @Before hook we create context, read framework and SUT configurtion, create test data storage, evaluate macros and initialize helper modules (Core modules).

It will also find local configuration files and load them for usage in steps.There is no need to do that in seperate steps or Background scenario.

In an @After hook we try to close the resources like for example web driver, Sql connection or take a screenshot if test failed.

As a last step we are attaching log from the scenario to the test report.

Hooks implementation can be found under src/test/java/steps/core/HooksSteps.



After @Before method execution cucumber-jvm will execute each step.

Steps shall be implemented under steps directory. Please use seperete package for your project steps and group them to make files management easier when project grows.



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



During this phase a file called /src/resources/config/environment/default.properties will be checked for framework and SUT configuration.

It contains default settings and can be overwritten with project specific ones.

Recommendation is to use project specific file to keep there System Under Test settings and framework settings shall stay in default.properties.

Property active_env points to project specific SUT configuration file. For example we can have in default.properties



	# Default Configuration
	# ### pointer to active project configuration that can overwrite the defaults
	active_env=bookByIsbn
	# ### webDriver specific configuration
	path_to_chrome_driver=C:\\SeleniumWebdrivers\\chromedriver.exe
	browser=chrome
	browser_timeout=10


And in bookByIsbn.properties



	# reqResIn project Configuration
	REST_url=https://www.googleapis.com/books/v1/volumes



In this way multiple systems under test can be configured.

Now it is time to read test data configuration from *.config files.

Global configuration is available under /src/resources/config/testdata

Files under this directory are checked and evaluated. New storage is created based on their content.

An example of test data configuration is below (content of /src/resources/config/testdata/testdata.config file)

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

Configuration files can include conent for other configuration files. To do so please use #include directive and provide reltive path to the other config file. Path shall be relative to project resources directory. An example is visible below.

Content of file resources/features/Web/test1/test1.config is

	#include "features/Web/test4/test45.config"
	
	TestData:{
	    "search_sentence" : "cucumber",
	    notAnInteger : 3.5123,
	    nowy_wpis : test
	       }

In that case first file from #include directive will be read and processed. Order of the includes is important. They are always processed first before other content of a config file.


Simialr for macros. They are read from *.config file and stored for future usage.

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



File /src/test/java/core/Macro.class contains methods to calculate macros based on their definitions and evaluate test storage.

Global test data and macro configuration can be overwritten by local configuration files available under the same directory as feature file.



For example directory /src/resources/features/Rest/GetBookByISBN/config can contain 2 files



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

Please note that macros have to be evaluated by calling of .eval(String storage_name) method from Macro.class in each step were such evaluaton shall be done.
To use previously defined macros one can put into the test data storage such macro as a value of particular key, for example

	TestData={
	    "a to test na makro" : mcr.isbn,
	    NOW_TimeStamp : mcr.testMacro3
	    }

Data types supported in test data configuration are

	String,
	Long,
	Double,
	HashMap,
	ArraList,
	Boolean



Each step execution is marked in a log with a "* Step started" string to make it easier to find it. For example



	[DEBUG] 2017-09-11 12:32:28.208 [main] Log - * Step started a_user_retrieves_the_book_by_isbn



--------------------------------



How to write step and share data between steps?

Thanks to dependency injection there is a way to share objects between steps and modules. In SharedContext.class available under /src/test/java/modules/core so called Context Object was defined.

To grant access to it please make sure that your Steps calss extends BaseStep calss and create a constructor like below

	public class DemoOnlieSteps extends BaseSteps {

	    private SharedContext ctx;

	    // PicoContainer injects class SharedContext
	    public DemoOnlieSteps (SharedContext ctx) {
		this.ctx = ctx;
	    }
	    
	 }
	 
Where DemoOnlineSteps is a class that contains project specific steps to handle web automation for particular page.
In this way we can pass same instance of ctx between steps and modules. With this approach we can use methods defined for objects available in ctx variable.
BaseSteps calss define a set of helpers modules to make writing new step defs much easier. They are called as below
Environment, Macro, StepCore, PageCore, SqlCore, Storage, FileCore, ExecutorCore. They contain a set of methods that can be used to do common things in steps like creating files, evaluating macros, reading environment configuration, evaluating templates, attaching files to the report etc.

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
From now one testDataMap and its values can be used in the step.
Other and uch simpler way to retrieve a particular value from the storage is String isbn = Storage.get("TestData.isbn");
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
Please note that step def input parameter is of type String. method checkIfInputIsVariable can return an object of different type (for example Long, Double, String, Boolean). Please do not hardcode Maps or Lists directly in the feature files, rather define them in a config file and use such config pointer in the feature. For example instead of writing "Then expected result is {key1:1, key2:2}" write "Then expected result is Expected.resultMap" 

It is assumed that user knowns what type is expected otherwise we can get type missmatch exception.

If there is a need to read any environment property one can use in a step Environment module. An example below

        if(Environment.readProperty("do_macro_eval_in_hooks").equalsIgnoreCase("true")){
            Log.info("<- evaluating macros ->");
            Macro.eval("TestData");
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
    
Please use FileCore module for any operation that shall be done on files.
Please use ExecutorCore module for any commands that shall be executed on the local host.
Please use SqlCore for any command that shall be executed in the dB under test.
Please use PageCore for any action that shall be done on the web page.
Please use Macro for macro evaluations.
Please use Storage to read/write new values to the storage.
Please use Environment to read any SUT configuration value.
Please use ctx.Object to pass objects between step defs.


--------------------------------


How to write Page Object Model for web automation purposes?


Let us have a look at an example of a MainPage that can be used for web automation purposes. It comes from /src/test/java/modules/pages/DemoOnlineStore.

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

When access to SUT configruation is needed use Environment.readProperty(input) method, for example

	Environment.readProperty("WEB_url");
	
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
