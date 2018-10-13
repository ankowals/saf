# saf
simple automation framework for learning purposes


----------------------------------


What do we want from a test automation framework?


	a way to execute e2e test using keyword driven approach (Gherkin and BDD are good enough)
	a way to execute tests related to  
		- rest json/xml (soap)
		- gui (web/native)
		- sql
		- mobile
		- pdf validation
		- others??
	a way to integrate any 3rd party app by execution of any command on local host 
	a way to execute any command on remote host over ssh/winRM
	a way to manage and configure test environment
	a way to manage and configure test data
	a way to prepare/calculate test data at runtime (macros)
	a way to manage and code a set of common modules/step/functions to be used for testing purposes
	a way to download any 3rd party symptoms from SUT like logs, trace files
	a way to log any activity done by the framework
	a way to report tests status
	a way to attach logs/files/messages/screenshots to the report
	a way to start test from IDE and command line as well as an ability to overwrite parameters when test suite was started from cmd
	a way to automatically deploy the framework under windows (dependency management + private repo)
	a way to share the code/tests between testers to increase re-usability (version control system, re-usable libraries of keywords)
	a way to monitor and indicate quality of committed tests (see SonarQube for example)
	a way to support PageObject model for web automation purposes
	a way to share common data between steps (dependency injection)
	a way to schedule test execution (see Jenkins/TeamCity)
	a way to generate test documentation automatically
	a way to manage multiple projects (version control system)
	a way to pause test execution and allow for manual intervention (integrate autoIT pause script)
	a way to integrate with test/requirement management tool (like for example jira, so we can have links to tests/epic/stories in the test report)
	a way to integrate with incident management tool (like for example jira, so we can have at least links to defects that affect particular test in the report and maybe their status etc.)
	a way to write simple step defs (libraries of methods support because for example usage of try-catch blocks is ugly)
	a way to manage templates
	a way to manage resources (make sure that open connections will be closed when test is over)
	a way to generate steps documentation (javadoc)
	
	a way to integrate with configuration(infrastructure) management tool can be a nice addon (like Puppet, Ansible or Chef)
	a way to integrate VM/container management tool like Docker or Vagrant can be a nice addon
	a way to re-run failed tests can be a nice addon
	a way to execute tests remotely can be a nice addon
	a way to encrypt/decrypt test data can be a nice addon
	a way to integrate with network protocols simulator can be a nice addon (see Seagull: an Open Source Multi-protocol traffic generator)

	to make maintenance easier we do not want smart coding, advanced java features
	
----------------------------------

Where are we?
	
	Currently working on
	
	(in progress) a way to execute tests related to  
		(to do) - mobile => Appium integration
		(to do) - better Sikuli integration for image based automation

		(to do) a way to downlaod any 3rd party symptoms from SUT like logs, trace files (create step def to run tcpdump on unix hosts or tshark/rawCap on windows hosts)
		(to do) a way to monitor and indicate quality of committed tests (see SonarQube for example )
		(to do) a way to generate test documentation automatically => add new logging categories (like atmn(category, message)), use scenario outline with path to feature and log file after test execution		
		(to do) add more tests examples
	
	----------------------------------	
		
	ToDo before 1.0:		   			   
		1) prepare step to trigger IE via autoIT script to handle windows authentication with Selenium (autoIT script available)	
		2) move documentation to pdf file and wiki, add screenshots of test executed in cmd as well as in IDE, add screenshots of tests reports as well as tests logs
		3) add git usage documentation (documentation almost complete) and xpath creation documentation (documentation available)
		4) describe how to extract table content and parse it to a list of maps using jsoup lib

	----------------------------------	

What is done?

	(done) a way to execute e2e test using Gherkin language (BDD) => cucumber-jvm integrated
	(done) a way to manage and configure test environment => via json configuration files
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
	(done) a way to share common data between steps => instead of dependency injection and pico container integration static factory pattern is used (CustomFormatter plugin)
	(done) a way to manage multiple projects (version control system) => separate repo per project maybe git submodules can be used?
	(done) a way to write simmple step defs (for example usage of try-catch blocks is ugly) => via PageCore, FileCore, StepCore, SqlCore, Macro, Storage, Environment and others models
	(done) a way to manage templates => via CoreStep model
	(done) a way to intergate any 3rd party app by execution of any command on local host => via ExecutorCore
	(done) a way to integrate with test/requirement management tool (like for example jira, so we can have links to tests/epic/stories in the test report) => via allure integration
	(done) a way to intgrate with incident management tool (like for example jira, so we can have at least links to defects that affect particular test in the report and maybe their status etc.) => via allure integration
	(done) a way to pause test execution and allow for manual intervention => via autoIt script executed from a step def
	(done) a way to manage resources (make sure that open connections will be closed when test is over) => via RestAssured config and closing of ssh, winrm, jdbc, web driver in scenario hooks
	(done) a way to execute any command on remote host over ssh/winRM
		(done) ssh/scp/sftp support => sshj library integrated and expectit-core library integrated
		(done) winRM support => winRm4j library integrated and winRS can be called via ExecutorCore and WinRSCore	
	(done) a way to schedule test execution (see Jenkins/TeamCity) => Jenkins integration description available in the readme file
	
----------------------------------


How can we use test automation framework?

	to automate functional tests
	to automate acceptance tests (after any deployment)
	to automate integration tests
	to automate regression tests
	to automate smoke tests (sanity checks) and make sure that SUT configuration is correct (like all urls are reachable, ports open, interfaces are up & apps are running, login is possible for each user etc.)
	to gather symptoms like traces/logs/tickets/events from multiple components of a system under test
	to execute test system bring up and feed it with configuration data before test starts
	to set up a test environment (infrastructure/configuration management) before test suite execution
	to restore the system to the state before test started
	to move configuration data between test systems
	to describe system behaviour via tests implementation (using Gherkin) -> use tests as a living documentation
	to use automated equipment for any not strictly test related activities like for example automate mobile phones to detect changes in the offer from a telco operator:)
	
	load generation/performance checks are out of scope
	parallel test execution is out of scope for now (shall be simple to add with Cucumber 4.x)
	
	For parallel test execution one can use capabilities of framework or better use multiple VMs to deploy multiple SUTs or framework instances (copies of the same project), CI instances/jobs instances etc... 
	in that case it has to be ensured that tests are separated from each other 
		- subsequent test does not depend on the result of previous test 
		- tests are using separate test data/config data (do not operate on the same config data at the same time to avoid concurrent modification, using unique identifiers in test data)


----------------------------------


Please find below list of libraries/plugins/software used for automation and their current versions. 

	Name, Version,  License 
	Java JDK, 1.8, Oracle Binary Code License 
	Maven, 3.5.2, Apache License Version 2.0 
	IntelliJ IDEA CE, 2018.2.3, Apache License Version 2.0 
	Cucumber for Java Plugin for IntelliJ, 182.3934, Apache License Version 2.0 
	cucumber-java, 3.0.2, MIT License 
	cucumber-junit, 3.0.2, MIT License 
	Selenium-java, 3.14.0, Apache License Version 2.0 
	Log4j-core, 2.11.1, Apache License Version 2.0 
	Log4j-iostreams, 2.11.1, Apache License Version 2.0 
	Slf4j-nop, 1.7.25, MIT License 
	Rest-assured, 3.1.1, Apache License Version 2.0 
	Json-path, 3.1.1, Apache License Version 2.0 
	Xml-path, 3.1.1, Apache License Version 2.0 
	commons-lang3, 3.8, Apache License Version 2.0 
	commons-io, 2.6, Apache License Version 2.0 
	commons-dbutils, 1.7, Apache License Version 2.0 
	commons-exec, 1.3, Apache License Version 2.0 
	pdfbox, 2.0.11, Apache License Version 2.0 
	opencsv, 4.2, Apache License Version 2.0 
	sshj, 0.26.0, Apache License Version 2.0 
	expectit-core, 0.9.0, Apache License Version 2.0 
	winrm4j, 0.5.0, Apache License Version 2.0 
	allure-cucumber3-jvm, 2.7.0, Apache License Version 2.0 
	allure-maven, 2.9, Apache License Version 2.0 
	winium-webdriver, 0.1.0-1, Mozilla Public License 2.0 
	winium-elements-desktop, 0.2.0-1, Mozilla Public License 2.0 
	sikuliXApi, 1.1.1, MIT License
	gson, 2.8.5, Apache License Version 2.0
	Maven-surefire-plugin, 2.20, Apache License Version 2.0 
	aspectjweaver, 1.8.10, Eclipse Public License - v 1.0 
	Maven-compiler-pluign, 3.7.0, Apache License Version 2.0 
	Jetty-maven-pluign, 9.2.10.v20150310, Apache License Version 2.0 
	Allure-maven-plugin, 2.5, Apache License Version 2.0 
	Jtds (jdbcDriver), 1.3.1, GNU LGPL 
	Chrome.Driver, 2.42, BSD 3-Clause "New" or "Revised" License 
	Winium.Desktop.Driver, 1.6.0, Mozilla Public License 2.0 
	7zip, 9.2.0, GNU LGPL + BSD 3-clause 
	Jenkins (standard install plugin set + PostBuildScript, Sidebar Link plugins), 2.138.1, MIT License 
	Artifactory, 6.0.2, GNU Affero GPL v3 


----------------------------------


Minimum requirements

windows 8, 
Java JDK 8, 
Maven 3.5, 
Chrome web browser


-------------------------


Installation instructions


	1 install java jdk ( download it from http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html ) 
 
		to verify java installation in cmd issue 'java –version' 
 
	2 download and extract maven binary ( download Binary Zip archive from https://maven.apache.org/download.cgi )
	3 set a system wide environment variable M2_HOME, for example M2_HOME=C:\apache-maven3.5.0 
	4 append %M2_HOME%\bin to PATH variable
	
		to verify maven installation in cmd issue 'mvn –version' 
	
	5 install IDE called IntelliJ IDEA Community Edition ( download it from https://www.jetbrains.com/idea/download/#section=windows ) 
	6 in IntelliJ go to Files -> Settings -> Plugins -> Browse repositories and install 'Cucumber for Java plugin' 
 
		REMARK: any other IDE plugin is not required and does not have to be installed! 
 
		It is possible to install this plugin from welcome screen. Please choose Configure -> Plugins 
		Click 'Install JetBrains plugin…' button 
		Search for 'Cucumber for Java' and install it
		
Optionally user can execute steps below. Especially for web automation case or jdbc/windows native apps automation.	   
	   
	7 install git ( download from https://git-scm.com/download/win )
	8 clone the repo for example to C:\Documents\Projects\SAF	
	9 download Selenium Chrome driver and other drivers if needed ( download from https://sites.google.com/a/chromium.org/chromedriver/downloads )
	10 put web drivers in <project dir>\src\resources, for example in C:\Documents\Projects\SAF\src\resources
	11 download JDBC oracle driver and other drivers if needed ( download from http://www.oracle.com/technetwork/apps-tech/jdbc-112010-090769.html ) 
	12 put odbc drivers in <project dir>\src\resources, for example in C:\Documents\Projects\SAF\src\resources
	13 Fix relative path (relative to project dir) to web drivers in \src\config\framework\framework.config
	14 Fix relative path (relative to project dir) path to jdbc drivers in \src\config\framework\framework.config
	15 Install autoIt ( download from https://www.autoitscript.com/site/autoit/downloads/ )


----------------------------------

	
Set basic project environment properties in setting.xml file

	1 configure path to JDK in setting.xml file under <jdk.path> tag, for example 
 
		<properties>   
			<jdk.path>C:/Program Files/Java/jdk1.8.0_144/bin/javac</jdk.path>   
			...  
		</properties>
		
	2 configure port number in setting.xml file under <jetty.port> tag, for example 
		<properties>      
			<jetty.start_port>8082</jetty.start_port>   
			<jetty.stop_port>8081</jetty.stop_port>   
			...  
		</properties> 
 
	3 optionally add private maven repository to active profile in setting.xml or in pom.xml (see how to add private maven repository chapter for more details)

	
REMARK: at any point in time user can instruct maven to import setting.xml configuration. 
	Go File -> Settings… 	
	Override user settings file for maven under Settings -> Build, Execution, Deployment -> Maven 
	
----------------------------------


How to import project in IntelliJ?

	1 open the IDE and click "Import Project" (or access it via File->New->Project from Existing Sources)
	2 point it to the location where your project is
	3 select "Import project from external model", select "Maven" and hit Next
	4 go with default options and click Next
	5 click environment variables button in the lower right corner and modify User settings file (it should point to your setting.xml)
	6 the project is recognized as maven project and click Next
	7 in case intelliJ is not able to locate your JDK, click "plus" icon in Select Project SDK window and point to the JDK installed on your machine, click Next
	8 enter the name of project and click Finish
	9 go to Run -> Edit Configurations... and configure 'Cucumber for Java' plugin to use additional formatter 
	10 add new environment variable cucumber.options under Default configuration of the plugin. 
		It’s value shall be --plugin org.jetbrains.plugins.cucumber.java.run.CucumberJvm3SMFormatter --monochrome --plugin libs.libCore.modules.CustomFormatter  

		
REMARK: without CustomFormatter plugin tests will not execute correctly! Please make sure that steps described in point 9 and 10 will be executed!
	
----------------------------------


Dir structure shall be like this


	Project

		- src
			- config
				- environment
				- framework
				- testdata
				project.config
			- features
				- Web
					- feature1
					- feature2
					...
				- Rest
					...
			- libs
				- libCore
					- config
					- doc
					- modules
					- resources
					- steps
					- templates
				- libProject1
				- libProject2
				...
			- resources
				chromedriver.exe
				ojdbc6.jar
				...
		- target
		pom.xml
		setting.xml
		README.md



Dir src/libs/LIB_NAME/modules contains methods needed to run the test. 

Dir src/libs/LIB_NAME/steps contains step defs defintion and implementation.



Subdirectory libCore cotnains saf freamework steps and methods. It is mandatory to have it in each project!

Other subdriectories contains project specific stuff like page obejct models etc. They are optional and can be added as git submodules for example.



Dir src/resources contains additional resources used by tests like drivers, 3rd party apps etc.

Dir src/config contains configuration files (*.config) 

Dir src/features contains features files (cotntainers for tests).

Dir traget will be used to store results of test execution like for example test report.

File pom.xml contains project properties and dependencies.  It shall not be changed by the end users.

File setting.xml contains end user project environment properties. This file can be moved outside project dir but in such case a full path to it shall be provided when calling mvn command.

File project.config contains project test configuration


--------------------------------



General concepts



We follow BDD apporach. Reason is very simple. It is usually much easier for testers to write automated tests following Gherking principles. In large projects (with large and separate teams of testers, analysts, devs) BDD main adventage (so called common language to describe system behaviours) can be rarely implemented but BDD is still giving testers the benefit of simpler tests creation. They can use step defs to write tests in plain english language.

Tests are called Scenarios. They are grouped in Features. They are build using step defs.

Features act as containers for Scenarios.

Please keep 1 Feature per 1 file.

Feature file name can and usually shall be same like Feature name. Feature name has to be unique.

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
Config files will be loaded automatically but feature names have to be unique! Best is to keep feature file name and feature name the same.
For example file myTestFeature.feature shall contain
	
	Feature: myTestFeature
		
		Scenario: myTestScenario1
		
		...


Log file will be created in target dir with a timestamp for each run, for example target/2017-09-11_103158_SAF
When run is done via mvn test command in addition to that a test report dir will be created, for example target/site
Report can be viewed in the browser.



--------------------------------



How the framework is build?



Java is used for learning purposes.

To make installation and deployment easy so called project build and dependency management tool is used. It is called maven.
It will automatically download all needed libraries so there is no need to find them manually.
Maven configuration is available in so called pom.xml file and setting.xml file. Pom.xml contains not just dependencies but also plugins.
Thanks to this maven can be used to start our tests from command line. For this purpose so called surefire plugin is used.

For logging purposes log4j2 library is used. For BDD cucumber-jvm and junit libraries are used. For reporting purposes allure library is used. 

Configuration files are in json format. We need to parse the data available inside. For json parsing gson library is used.

When steps are executed we need to pass the same instance of a class to them, for example webdriver instance, test data storage, output of step def execution etc. To make it possible we are using CustomFormatter plugin. Without it for example each step will open a new browser window.

For web automation Selenium WebDriver library is used. For windows native apps automation Winium library is used. For api automation RestAssured library is used. To read Csv files openCSV library is used. 

To better handle command execution and sql execution Commons-exec and commons-dBUtils libraries from Appache are used. Same for better handling of files and string manipulations (Commons-io and Commons-lang). 

To read/write pdf files pdfBox2 library is used. 

To have possibility to pause test execution autoIt scirpt is used. 

To manage remote hosts via ssh and transfer files via scp/sftp sshj and expectit-core libraries are used. To mangae windows remotes winrm4j library is used.

On top of that macro support, test data management, configuration files support, Page Object Model support and more was added.
Project and test structure is also enforced to keep things consistent.


--------------------------------


How to setup private maven repository?

To make distribution and usage of 3rd party libraries compliant with corporate process or policy we can setup a private maven repository. Such repository can act as a proxy and caching server which operates in internal network and from which testers can fetch dependencies.  
To make this job easier we can use so called repository manager called Artifactory (it’s open-source version is more than enough for our purposes). 
To run it please follow steps described below.  
 
	1 Download an open source version from https://jfrog.com/open-source/. Please download the zip package. 
	2 Unpack it to desired directory, for example C:\Users\superuser\Downloads\jfrog-artifactory-oss6.0.2 
	3 Install Java SE version 8 or newer 
	4 Go to artifactory-oss-6.0.2\bin 
	5 Run artifactory.bat 
 
This will start artifiactory with its default configuration. Artifactory Gui can be accessed under http://<host_name>:8081/artifactory  
Default login and passwords are admin/password. To modify default 8081 port please change tomcat configuration under /tomcat/conf/server.xml.  
 
Please create default repository configuration using configuration wizard for maven. Created repositories will act as proxy and cache repository. 
 
Following repositories are going to be created libs-snapshot-local, libs-release-local, jcenter, libs-snapshot, libs-release. 
 
Cached artifacts will be stored in jcenter-cache repository. Artifactory repositories are keeping artifacts database under artifactory-oss-6.0.2\data subdirectory. 
Every times tester’s workstation will ask to download new artifact first jcenter-cache will be checked and in case artifact will be missing jcenter repository will be checked. 
 
In addition user can create so called local repositories. This means user has a possibility to upload particular artifacts to such local repository and it can be used in internal network only. Please use Deploy button to add local artifacts/repos. 
 
To make use of internal repository managed by artifactory we need to re-configure maven settings on tester’s workstation. Pom.xml or setting.xml file can be updated to include following entries. 

		<repositories> 
			<repository>
				<snapshots>
					<enabled>false</enabled>
				</snapshots>         
				<id>central</id>         
				<name>libs-release</name>         
				<url>http://<host_name>:8081/artifactory/libs-release</url>     
			</repository>     
			<repository>         
				<snapshots />         
				<id>snapshots</id>         
				<name>libs-snapshot</name>         
				<url>http://<host_name>:8081/artifactory/libs-snapshot</url>     
			</repository> 
		</repositories> 
		<pluginRepositories>     
			<pluginRepository>
				<snapshots>
					<enabled>false</enabled> 
				</snapshots>         
				<id>central</id>         
				<name>libs-release</name>         
				<url>http://<host_name>:8081/artifactory/libs-release</url>     
			</pluginRepository>     
			<pluginRepository>         
				<snapshots />         
				<id>snapshots</id>         
				<name>libs-snapshot</name>         
				<url>http://<host_name>:8081/artifactory/libs-snapshot</url>     
			</pluginRepository> 
		</pluginRepositories> 
 
Where host_name is the name of the computer where our internal repository is located. No other changes to pom.xml/setting.xml are needed. From now one maven will try to fetch artifacts from this internal repository. 
 
REMARK: jcentral repository can be made offline. This means that only artifacts from its cache are going to be served. In case something is missing an error will be thrown but no connection to maven central repository will be created. Cache will not be removed when Artifactory will be killed! 
 
To make jcentral repository local only. Please follow steps below.
 
	1 Login as admin to Artifactory 
	2 Open Admin menu and go to Remote Repositories 
	3 Open jcenter 
	4 Scroll down Basic properties and mark it as offline. Save it. 
 
 
REMARK: Global offline mode exists as well. In this case remote repositories serve as caches only and do not proxy remote artifacts. It can be enabled by going into Admin tab under Configuration | General 


--------------------------------



Usage




To run a test from windows cmd please execute

	cd <install_dir>
	mvn clean test -fae -s setting.xml -Dcucumber.options="--tags @bookByIsbn"
	mvn site -s setting.xml
	mvn jetty:run -s setting.xml
	go to http://localhost:8082
	

Please note that usage of clean keyword ensures that artifacts from previous test execution are removed first.

To run a test case using scenario or feature file name please execute
	
	mvn clean test -fae -s setting.xml -Dcucumber.options="--name '^Test 1 \- dummy test name\, which shows how to use regex \- 2 define a test name at run time\.$'" 

Please note that commands provided above can be executed from a batch file. This is exactly what is happening when Jenkins is in use. 	
	
One can also use IntelliJ to run a feature file. In that case only log file will be created.
To run a test from IntelliJ a cucumber plugin is used. Please click with right mouse button on the feature file name and choose 'Run'.
In case of an exception indicating that step defs were not found please double check plugin configuration. To do so go to Run menu in the toolbar and choose Edit configurations. Select cucumber java and make sure that glue points to the correct directory or package (shall be 'libs').
If test runs fine via mvn test command but not using IntelliJ this indicates missconfigruation of cucumber-jvm plugin.

To generate a report from test please execute 'mvn site -s setting.xml'. After this command execute 'mvn jetty:run -s setting.xml' to run jetty. Check the report in the browser under http://localhost:port (default port is 8082).

It is possible to overwrite active environment name property property from the command line. In that case project specific config as specified by the CMD argument will be used during test execution. To do so please execute a test for example like below

	mvn clean test -s setting.xml -fae -Dctx.Environment.Active.name="bookByIsbn" -Dcucumber.options="--tags @bookByIsbn"

In this particular case a default environment (SUT) configuration will be loaded and later on it will be overwritten by config available in a file src\test\java\config\environment\bookByIsbn.config. Cucumber option --tags can be used to run only a subset of tests that are tagged with @bookByIsbn tag.

It is possible to set browser width and height via command line argument. To do so please execute test using command like below

	mvn clean test -s setting.xml -fae -Dctx.Environment.Active.name="demoOnline" -Dctx.Environment.Active.Web.size="800 x 640" -Dcucumber.options="--tags @demoOnline"

Argument -Dctx.Environment.Active.Web.size will be used to set browser dimensions.


REMARK: it is possible to pass TestData/Environment/Expected data valus via CLi. In such case they will overwrite values taken from configuration file. To do so please use -Dctx.TestData.key=value flag. For example
	call mvn test -s setting.xml -fae -Dctx.Environment.Active.WinRM.VM1.host=my_new_host_name -Dcucumber.options="--tags @execute_test_on_virtual_machine_my_new_host_name"




How to run tests in parallel?



Usage from command line allows us to run tests in parallel. Please note that in such case a careful test design shall be considered. End user needs to take care about test data and configuration data. Situation where one test can influence other one has to be avoided. For example employees/subscribers shall have unique identifiers etc. If one test modifies SUT configuration we shall make sure that at the same time it will not affect other tests. In addition if Sikuli is in use please remember that if one app covers other one it may fail to locate desired element on the screen (consider a case where multiple RDP or web browser windows are open on same host). 
To run tests in parallel please create multiple copies of your project. In such case  you can run tests in each project at same time, for example using batch files.  
In exactly same way we can create multiple Jenkins jobs, where each job is using a copy of same project. 
In this way multiple jobs can be triggered at same time.



----------------------------------



How to run it from Jenkins?



Goal of this instruction is to setup a Jenknins instance so user can schedule test execution and check the resutls.
Description below allows to setup a new project with a job that can be started and which results can be viewed in the web browser.

	Steps to setup Jenkins

	1 Download latest Jenkins for Windows from https://jenkins.io/
	2 Install it
	3 Go to installation directory by default C:\ProgramFiles (x86)\Jenkins and edit jenkins.xml file
	
		search "--httpPort=8080" and replace the "8080" with the new port number that you wish to use, for example 8083
	
	4 Restart Jenkins service
		
		open command prompt
		type "services.msc"
		right click on the "Jenkins" line and select "Restart"
		type http://localhost:8083/ in your browser to test the change

	5 Proceed according to the instructions on the screen
	6 Install suggested plugins
	7 Create admin user
	8 Go to Manage Jenkins -> Configure System -> Advanced and change default path to workspace directory to ${JENKINS_HOME}/workspace/${ITEM_FULL_NAME}
	9 Go to Manage Jenkins -> Configure System -> Advanced and modify number of executors to decide how many jobs can be run in parallel
	10 Go to Manage Jenkins -> Manage Plugins -> Available tab and install PostBuildScript plugin to run tasks when the job execution is over 
	11 Go back to main view and click Create New Jobs as Freestyle project
	12 Thick "This project is parametrized" checkbox
	13 Add String parameter PROJECTDIR with default value pointing to your project directory, for example "C:\Users\akowa\Documents\Projects\FK_Prototype"
	14 Thick trim the string box
	15 Add String parameter ACTIVEENV with default value empty
	16 Thick trim the string box
	17 Add build step "Execute Windows batch command"
	
		echo STEP 1 PRE BUILD
		echo Stop jetty server so we can clean target directory

		cd %PROJECTDIR%
		call mvn jetty:stop -s setting.xml 
		call mvn clean -s setting.xml
		
	18 Add build step "Execute Windows batch command"
	
		echo STEP 2 BUILD
		echo run tests

		cd %PROJECTDIR%
		call mvn test -s setting.xml -fae -Dctx.Environment.Active.name="%ACTIVEENV%" -Dcucumber.options="--tags @test1" && call mvn -s setting.xml site
		call mvn test -s setting.xml -fae -Dctx.Environment.Active.name="%ACTIVEENV%" -Dcucumber.options="--tags @test2" && call mvn -s setting.xml site
		
		
	19 Add build step "Execute Windows batch command"
	
		echo STEP 3 POST BUILD
		echo copy test report to workspace %WORKSAPCE% for archiving

		cd %PROJECTDIR%
		xcopy * "%WORKSPACE%\%JOB_NAME%_%BUILD_NUMBER%" /e /i /h /Y
		
	20 Add build step "Execute Windows batch command"
	
		echo STEP 4 POST BUILD
		echo generate test report

		cd %PROJECTDIR%
		call mvn site -s setting.xml
		
	21 Add build step "Execute Windows batch command"
	
		echo STEP 5 POST BUILD
		echo start jetty to display test report

		set BUILD_ID=
		echo cd %PROJECTDIR% > %TEMP%\startJetty.bat
		echo mvn jetty:run -s setting.xml >> %TEMP%\startJetty.bat

		start /B "" cmd /C %* %TEMP%\startJetty.bat
		
	22 Apply and save the changes
	23 Schedule the job
	24 Watch it runs
	25 View results via allure report


	REMARK: tests will be run in order defined in batch file mentioned above in step 18

How to allow desktop interaction?

By default Jenkins runs as a service under Windows host. This means it can be managed by going to services.msc. Unfortunately this will not allow for desktop interaction. This means that when a browser is open via Jenkins job or RDP session is open by Jenkins job user will not see it on the host. 
 
In general this is great because it allows to use this host when jobs are getting executed without a fear that user actions like mouse clicking or typing will influence test execution. Unfortunately Sikuli usage requires desktop integration. Currently it is used to handle flash based elements in Navigator UI of Wfc. 
 
To enable desktop integration we can run Jenkins from cmd as a current user. To do so please follow steps described below 

	1 Stop Jenkins service via services.msc 
	2 Change startup type from Automatic to Manual 
	3 Create a batch file with following content and run it 
 
		cd "C:\Program Files (x86)\Jenkins "
		java -jar Jenkins.war --httpPort=8083 
 
		REMARK: 8083 number shall be changed to desired one  
  
	4 When Jenkins is started from CMD it will create a new directory in user’s home dir, for example C:\Users\superuser\.jenkins
	5 Stop Jenkins instance that was run from CMD 
	6 Synchronize content of directory C:\Program Files (x86)\Jenkins with C:\Users\superuser\.jenkins 
	7 Start Jenkins again using batch file it will contain same configuration like previously used when it was started as a service from Program Files and desktop interaction will be allowed 

	
How to add links to reports to Jenkins dashboard?

It is possible to show links to test reports on a Jenkins dashboard. Reports will be accessible only after job execution is done. It is very convenient for the end user to always check the job status via Jenkins dashboard and be able to go directly to the test report when the test execution is over.

To add the links to the dashboard we can use a plugin called Sidebar links. To install it 

	1 Go to Manage Jenkins -> Manage Plugins 
	2 Click Available tab and install Sidebar Link plugin 
	3 Go to Manage Jenkins -> Configure System and add required links to test reports 	
	
How to configure e-mail notifications?

	1 Go to Manage Jenkins -> Configure System and add system admin e-mail address (it will be used as a sender) 	
	2 Modify Extended E-Mail configuration and add 
		SMTP server: <smtp_server_host_name>  
		Default user E-mail suffix: @<domain_name> 	
	3 If required default e-mail content and recipients can be configured as well. 
	4 Configure E-Mail notification section 
	5 Configure particular job to trigger e-mail notification for example when the job status is success. 
		a) Add new Post-build action ‘Editable Email Notification’ 	
		b) Change the default content or recipients configuration if required
		c) Add trigger for notification. To do so please click on the 'Advanced Settings…' button and select 'Add Trigger' -> Success. Set 'Send to' to Recipient List. 


		
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


Feature files or scenarios can be tagged. Use tags ("@tagName") and cucumber options to execute a set particular tests, for example



	mvn clean test -s setting.xml -fae -Dcucumber.options="--tags @bookByIsbn"



To pass data from configruation file use test data storage name and pass field after dot. For example TestData.statusOK. Of course step def needs to support this (more details below).

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

		features/Rest/GetBookByIsbn/
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



Cucumber runner is available in src/libs/libCore/modules/TestRunner.class
It contains cucumber options like glue path (path to steps definitions), features path and allure report plugin.
There shall be no need to change it parameters.

	package libs.libCore.modules;

	import cucumber.api.junit.Cucumber;
	import org.junit.runner.RunWith;
	import cucumber.api.CucumberOptions;

	@RunWith(Cucumber.class)
	@CucumberOptions(
		plugin = {"io.qameta.allure.cucumber3jvm.AllureCucumber3Jvm", "libs.libCore.modules.CustomFormatter"},
		features = "src/features",
		glue = "libs")
	public class TestRunner {}



Before each scenario execution so called @Before and @After hooks are run.
In scenario @Before hook we create context, read framework and SUT configurtion, create test data storage, evaluate macros and initialize helper modules (Core modules).
We will also find local configuration files and load them for usage in steps. There is no need to do that in seperate steps or Background scenario.
In an @After hook we try to close the resources like for example web driver, Sql connection or take a screenshot if test failed.
As a last step we are attaching log from the scenario to the test report.
Hooks implementation can be found under src/libs/libCore/modules/CustomFormatter.class.



After @Before method execution cucumber-jvm will execute each step.


Steps shall be implemented under src/libs/LIB_NAME/steps directory. Please use seperete package for your project steps and group them to make files management easier when project grows.

There is also possibility to execute some actions before the whole test suite (a set of feature files) will be executed. There are 2 additional global hooks available. So called beforeAll and afterAll hook. They can be used to initialize logger, print system properties or try to close the resources like web drivers etc.
Global hooks implementation can be found under src/libs/libCore/modules/CustomFormatter.class.


REMARK: Please note that cucumber-jvm hooks (methods annotated with @Before, @After, @BeforeStep, @AfterStep) are not used any more. Instead hooks are implemented in CustomFormatter plugin.



Each new scenario start will be indicated in the log as follows



	2018-09-19 20:32:19.318 [INFO ] +-------------------------------------------------------------+
	2018-09-19 20:32:19.318 [INFO ] *** Feature id: C:/Users/akowa/Documents/Projects/Cucumber2/src/features/Web/DemoOnlineShop/DemoOnlineShop.feature ***
	2018-09-19 20:32:19.318 [INFO ] +-------------------------------------------------------------+
	2018-09-19 20:32:19.318 [INFO ] 
	2018-09-19 20:32:19.318 [INFO ] +-------------------------------------------------------------+
	2018-09-19 20:32:19.318 [INFO ] *** Feature with name: DemoOnlineShop started! ***
	2018-09-19 20:32:19.318 [INFO ] +-------------------------------------------------------------+
	2018-09-19 20:32:19.318 [INFO ] 
	2018-09-19 20:32:19.318 [INFO ] 
	2018-09-19 20:32:19.318 [INFO ] +-------------------------------------------------------------+
	2018-09-19 20:32:19.318 [INFO ] *** Scenario with name: Verify sum of 2 items equals total price started! ***
	2018-09-19 20:32:19.318 [INFO ] +-------------------------------------------------------------+



@Before hook method execution will be visible like below



	2018-09-19 20:32:19.318 [INFO ] Started resources initialisation
	2018-09-19 20:32:19.333 [DEBUG] Ctx object FileCore of type class libs.libCore.modules.FileCore created or modified
	2018-09-19 20:32:19.333 [DEBUG] Ctx object Config of type class libs.libCore.modules.ConfigReader created or modified
	2018-09-19 20:32:19.333 [DEBUG] Ctx object Storage of type class libs.libCore.modules.Storage created or modified



----------------------------------



Environment



During this phase files available in /src/config will be checked for framework and SUT configuration.
They contain global environment configuration as well as global test data configuration. Each setting can be overwritten later on during test execution by local test configuration.
Recommendation is to use project specific file to keep there System Under Test settings and framework settings shall stay in separate file.

Property Environment.Active.name available in src/config/environment/active.config indicates which SUT configuration shall be used. For example we can have in default.properties



	Environment={

	    Active : {
		name : "reqResIn"
	    }

	}


And in src/config/environment/reqResIn.config



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


File src/config/environment/default.config contains global Default configuration.

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
Please note that this configuration is divided into 2 parts. Second part contains configuration specific for the framework like paths to the drivers etc. It can be found in src/config/framework/framework.config. All settings can be put into one file but usually it is easier to manage complex configurations if they are logically splited between few files.

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
		
		and others (see below and into libs/libCore/config/default.config for more details)

Entity scripts.path can be use to indicate a path relative to project directory where some autoIT or shell scripts can be found for example.
Entity apps can be used to group together any 3rd party apps that can be called by the step defs like for example autoIt, tshark, mergecap etc.

In this way multiple systems under test can be configured.

Please note that in libs/libCore/config user can find default configruation that can be overwriten by global project config available in src/config/project.config file. For this reason please do not change anything in libs/libCore/config files.

Now it is time to read test data configuration from *.config files.
Everything that is written below applies also to environment configuration files behaviour.

From now on in case there is a need to access any configuration parameter one can use in the step def Storage.get() method. For example

    @When("^xml post request (.*?) with soap action header (.*?) is sent$")
    public void xml_post_request_is_sent(String name, String actionHeader) {

        String url = Storage.get("Environment.Active.Rest.url");
	...
	
Storage.get("Environment.Active.Rest.url") method returns url value from active configuration. Please note that is is assigned to variable of type String. In case Storage.get() returns other type of data than String we may encounter ClassCastException.
	

REMARK: Please note that it is possible to pass variables into the config files. Content of storage is re-evaluated in CustomFormatter before scenario run. Please see an example below.
Having 2 entities like TestData.Entity1.Key1 and Environment.Active.WinRM.VM1.host user can create new third config entity dummyEntity.
			
			TestData: {
				Entity1: {
					Key1: "${ctx.Environment.Active.WinRM.VM1.host}"
					}
				}	
	
			Environment: {
				Active: {
					WinRM: {
						VM1: {
							host: "myDummyHostName"
							}
						}
					}	
				}
				
			DummyEntity: {
				key1: "${ctx.TestData.Entity1.Key1} is going to be used together with ${ctx.Environment.Active.WinRM.VM1.host}"
				}	
				
REMARK: Each entity visible below can be placed in a seperate configuration file!			


				
How to add environment information to test report?



To show anything in Environment section of test report please create following entitiy in Environment configuration.


	Environment:{
		Default: {
			WriteToReport: {
				Key1: "This is dummy text",
				Key2: "${ctx.TestData.This.Is.Dummy.Value.Taken.From.Config}"
				}
			}
		}



How to add link to issue/test description available in test/issue management solution into the test report?



To show links into issue or test in scenario report please add following section into the Environment configuration

	Environment:{
		Default: {
			IssueTrackerUrlPattern: "https://jira.my.company.com/browse",
			TestTrackerUrlPattern: "https://jira.my.company.com/browse",
			}
		}

Please add approperiate tag into the feature file, for example where @issue contains defect number or @tmsLink contains test case id from test management software

		@issue=<ISSUE-NUMBER> @tmsLink=<TEST-CASE-ID>
		Scenario: affected scenario name		
		
REMARK: For other tags and more information please see https://docs.qameta.io/allure/#_cucumber_jvm		
		
			
----------------------------------



TestData



Global project configuration is available under src/config/project.config directory.
Files included in it are checked and evaluated. New storage is created based on their content.
An example of project.config file is below


	#include "config/framework/framework.config"
	#include "config/environment/winrm.config"
	#include "config/environment/default.config"
	#include "config/environment/active.config"
	#include "libs/libProject1/config/testdata.config"
	#include "libs/libProject2/config/testdata.config"
	#include "config/testdata/cloud.config"
	#include "config/testdata/database.config"
	#include "config/testdata/release.config"
	#include "config/testdata/testdata.config"
	#include "config/testdata/expected.config"

	TestData:{}

	
Additional configuration files will be read in the order they are mentioned in project.config. In such way default configuration from other libraries can be included.

Please remember that order of processing is as follows 
	- first config files under libs/libCore/config
	- then project.config under src/config
	- then local config files in feature file directory
	
An example of test data configuration is below (content of src/config/testdata/testdata.config file)

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


An example of a log is below



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

Content of file src/features/Web/test1/test1.config is

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



Simialr for macros. They are read from *.config files and stored for future usage.

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



File /src/libs/libCore/modules/Macro.class contains methods to calculate macros based on their definitions and evaluate test storage.

Global test data and macro configuration can be overwritten by local configuration files available under the same directory as a feature file.



For example directory /src/features/Rest/GetBookByISBN/config can contain 2 files



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
Macro can be used to return a unix timestamp or a date in specified format or a random value or a value of a week/month start/end date.
They can be concatenated with a specific prefix or suffix. 
Macro values are always returned as strings. 

Please note that macros have to be evaluated by calling of Macro.eval(String storage_name) method from Macro.class in each step were such evaluaton shall be done. They are evaluated once by default in @Before hook but this can be turned off in the configruation.
To use previously defined macros one can put into the test data storage such macro as a value of particular key, for example

	TestData={
	    "a to test na makro" : mcr.isbn,
	    NOW_TimeStamp : mcr.testMacro3
	    }

Following macro types are supported currently
           
	date,
    timestamp,
    random,
    startOfWeek,
    endOfWeek,
    startOfMonth,
    endOfMonth		
		
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

Each step execution is marked in a log with a "* Step started" string to make it easier to find it. There is no need to call any logging method for it explicitly in the step definition. This will be done in before step hook by CustomFormatter plugin. For example


	2018-09-19 20:32:21.059 [INFO ] +-------------------------------------------------------------+
	2018-09-19 20:32:21.059 [INFO ] * Step started open browser
	2018-09-19 20:32:21.059 [INFO ] +-------------------------------------------------------------+


--------------------------------



How to write step and share data between steps?



Thanks to CustomFormatter plugin usage there is a way to share objects between steps and modules. In ThreadContext.class available under /src/libs/libCore/modules so called Context Object was defined.
There are 3 types of Context Global and Scenario context. Global context is created at the beginning of test run and exists as long as jvm exists. It is created main thread only. It can be used to pass data between features.
Scenario context is created when new scenario execution starts and it is destroyed when scenario execution ends. It is created per thread. It can be used pass data between steps in same scenario.

To grant access to it please make sure that your Steps class extends BaseStep class

	public class DemoOnlieSteps extends BaseSteps {

	    
	 }
	 
Where DemoOnlineSteps is a class that contains project specific steps to handle web automation for particular page.
In this way we can pass same instance of scnearioCtx and globalCtx between steps and modules. With this approach we can use methods defined for objects available in ctx variable.
BaseSteps class define a set of helpers modules to make writing new step defs much easier. They are called as below
Macro, StepCore, PageCore, SqlCore, Storage, FileCore, ExecutorCore, PdfCore, SshCore, WinRMCore, WinRSCore, WiniumCore, CloudDirectorCore. They contain a set of methods that can be used to do common things in steps like creating files, evaluating macros, reading environment configuration, evaluating templates, attaching files to the report etc.

For example lets have a look at 2 steps below

    @Given("^a book exists with an isbn$")
    public void a_book_exists_with_isbn() {
        String isbn = Storage.get("TestData.isbn");
        RequestSpecification request = given().param("q", "isbn:" + isbn);
        scenarioCtx.put("request",RequestSpecification.class, request);
    }

    @When("^a user retrieves the book by isbn$")
    public void a_user_retrieves_the_book_by_isbn(){
        
		String url = Storage.get("Environment.Active.Rest.url");
		RequestSpecification request = scenarioCtx.get("request",RequestSpecification.class);
        
		Response response = request.when().log().all().get(url);
        ValidatableResponse response2 = response.then();
		
        scenarioCtx.put("response",Response.class, response);
        scenarioCtx.put("response",ValidatableResponse.class, response2);
        scenarioCtx.put("json",ValidatableResponse.class, response2);
        
		StepCore.attachMessageToReport("Json response", response.prettyPrint());
    }
    
To retrieve test data storage one can write HashMap<String, Object> testDataMap = scenarioCtx.get("TestData",HashMap.class);
From now on testDataMap and its values can be used in the step.
Other and much simpler way to retrieve a particular value from the storage is String isbn = Storage.get("TestData.isbn");
Nested objects can be provided using dots like for example Storage.get("TestData.isbn.some_nested_key[0]") etc.

scenarioCtx  and globalCtx are buckets to which we can throw anything and later on we can retrieve it. This is useful to share data between steps that are defined in different class. For example

	scenarioCtx.put("request",RequestSpecification.class, request);

This metohd puts an object of type RequestSpecification to scenarioCtx bucket with name "request". Later on another step can retrieve it like below

	RequestSpecification request = scenarioCtx.get("request",RequestSpecification.class);

StepCore contains a set of helper functions that can be used when writing step defs. For example there are functions that can be used to add something as an attachment to the test report.

There is also one more method that can be used to check if step input is a variable from the configuration file. See an example below.

    @Then("^the status code is (.*)$")
    public void verify_status_code(String input){

        Integer code = StepCore.checkIfInputIsVariable(input);

        Response response = scenarioCtx.get("response",Response.class);
        ValidatableResponse json = response.then().statusCode(code);
        scenarioCtx.put("json",ValidatableResponse.class, json);
    }

In the feature file one can write

	Then the status code is TestData.statusOK

where

	TestData:{
	    "statusOK" : 200
	    }
	
or

	Then the status code is 200
	
In both cases step shall pass.
Please note that step def input parameter is of type String. method checkIfInputIsVariable can return an object of different type (for example Int, Long, Double, String, Boolean). Please do not hardcode Maps or Lists directly in the feature files, rather define them in a config file and use such config pointer in the feature. For example instead of writing "Then expected result is {key1:1, key2:2}" write "Then expected result is Expected.resultMap" 

It is assumed that user knowns what type is expected otherwise we can get type missmatch exception. It is also possible to use a more general type of Object.

If there is a need to read any environment property one can use in a step Storage module. An example below

        Boolean doMacroEval = Storage.get("Environment.Active.MacroEval");
        if ( doMacroEval == null ){
            Log.error("Environment.Active.MacroEval null or empty!");
        }
	
Similar for macro evaluation. It is enough to just call Macro.eval(input) method. Where input is the name of storage (of type HashMap), for example

        if( doMacroEval ){
            Log.info("Evaluating macros in TestData and Expected objects");
            Macro.eval("TestData");
            Macro.eval("Expected");
        }

In case a step shall handle multiple input parameters please use tables in a feature file. In the step input will be provided as a Map.

Later on each input parameter and its value can be retrieved in a loop. See an example below.

	@And("^response includes the following in any order$")
	    public void response_contains_in_any_order(Map<String,String> responseFields){

		ValidatableResponse json = scenarioCtx.get("json",ValidatableResponse.class);
		for (Map.Entry<String, String> field : responseFields.entrySet()) {
		    Object expectedValue = StepCore.checkIfInputIsVariable(field.getValue());
		    String type = expectedValue.getClass().getName();
		    if(type.contains("Long")){
				Long lExpVal = (Long) expectedValue;
				Log.debug("Expected is " + field.getKey() + "=" + lExpVal.intValue());
				Log.debug("Current is " + json.extract().path(field.getKey()));
				json.body(field.getKey(), containsInAnyOrder(lExpVal.intValue()));
		    else {
				String sExpVal = (String) expectedValue;
				Log.debug("Expected is " + field.getKey() + "=" + sExpVal);
				Log.debug("Current is " + json.extract().path (field.getKey()));
			    json.body(field.getKey(), containsInAnyOrder(sExpVal));
		    }
		}
	}

Please use javadoc to document each step in the library. An example below

    /**
     * Verifies that response status code is {}
     * Creates new object ValidatableResponse and stores it as json ctx.obj
     *
     * Uses following objects:
     *  scenarioCtx.response
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
        scenarioCtx.put("json",ValidatableResponse.class, json);
    }
    
This gives tester a chance to discover new steps in the library by looking into it html documentation.	
	
Please use StepCore module for template management/compariosn, to attach file to the report etc. 
   
Please use FileCore module for any operation that shall be done on files.

Please use ExecutorCore module for any commands that shall be executed on the local host.

Please use SqlCore for any command that shall be executed in the dB under test.

Please use PageCore for any action that shall be done on the web page.

Please use Macro for macro evaluations.

Please use Storage to read/write new values to the storage.

Please use scenarioCtx to pass objects between step defs.

Please use PdfCore module for pdf manipulation.

Please use SshCore module for ssh/scp/sftp execution.

Please use WinRmCore module for winrm execution.

Please use WinRSCore module for remote windows management via winRS client

Please use CloudDriectoreCore module for virtual machine management via vmware cloud director

Please use WiniumCore module for any action that shall be done in a windows application written for win8 and below (although windows 10 sdk is supported by winium it maybe better to use appium)

It is also possible to write test data into a file. In this way it can be read later on and used during other test execution. Even though this creates dependecies between tests it maybe useful some times. Please consider following example we run a long lasting test. Action that has to be trigger takes 10 minutes to execute. In this case there is no point to wait for its results. Instead it maybe desired to divide the test into 2 parts (feature files). One will be called to trigger the action. Second one can contain validation steps.
Second feature can be executed few minutes later and in the meantime other test can run.
In such case we have to extract test data that was used in the first part of the test (first feature file).

See an example below to understand how to write test data storage (or any other storage) to a text file.

	@reqResIn
	Feature: ReqResIn

	  Scenario: Tigger Post request to create single user

	      ...
	      And write storage TestData with id ReqRestInScenario1 to file
	      And read storage TestData with id ReqRestInScenario1 from file
	      ....

Steps that are involved will create a file that can contain the storage with identifier, like

	id1={key1:"value1", ...}
	id2={key2:"value2", ...}

Later on such storage can be retrived using the identifier and used during scenario execution.
File will be created in temporary directory on a file system. Usually it is C:\Users\<user name>\AppData\Local\Temp\SAF_Persistent_Storage_File.json

Users also have a possibility to pass data between scenarios and features using so called globalCtx. This is not recommended because it creates dependencies between tests and usually there exists a better way to write the test than using such feature, for example add Background scenario or enhance Given steps. 
An usecase where it can be usefull is to use globalCtx like a simple cache. For example where we need to connect to remote SUT to retrieve a data. Instead of making a call to a remote host in each scenario/step we can do it once put the result into globalCtx and retirve it from it at any time. In this way we can reduce number of remote calls to SUT if required.
To use this capability please see an example below.

	@reqResIn
	Feature: ReqResIn

	  Scenario: Tigger Post request to create single user

	    Given service is available
	    When json post request createUser is sent
	    Then extract user id as userId

	  Scenario: Trigger a Get request to get a single user and validate the response

	    Given service is available
	    When json get single user with id userId request is sent
	    Then verify that rest response body has
	      | key                        | action            | expected        |
	      | data.id                    | equalTo           | Expected.userId |

Step 'extract user id as userId' extracts user id in first scenario and stores its value in the globalCtx context (implemented using ThreadContext and static factory pattern, Context and CustomFormatter plugin).
      
    @Then("^extract user id as (.+)$")
    public void extract_user_id(String identifier) {
        ValidatableResponse response = scenarioCtx.get("response",ValidatableResponse.class);
        String userId = response.extract().path("id");

        if ( userId == null || Integer.parseInt(userId) <= 0 ) {
            Log.error("UserId was not found in the response!");
        }

        globalCtx.put(identifier,String.class,userId);
        Storage.set("Expected.userId", userId);
    }
 
In this case user id will be stored as a string. Class type has to be provided.
To store the value in a global cache one can use method like below

       globalCtx.put(identifier,String.class,userId);

It can be retrieved later on in the next Scenario or different Feature. See an example of step def implementation below.

    @When("^json get single user with id (.+) request is sent$")
    public void json_get_request_is_sent(String id) {

        String url = Storage.get("Environment.Active.Rest.url");
        String path = Storage.get("Environment.Active.Rest.url_get_suffix");

        String userId = globalCtx.get(id, String.class);

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
        scenarioCtx.put("response",ValidatableResponse.class, vResp);
        StepCore.attachMessageToReport("Json response", response.prettyPrint());
    }
 
Method below are used to retrieve the value
 
  
        String userId = globalCtx.get(id, String.class);


--------------------------------



How to write Page Object Model for web automation purposes?


Let us have a look at an example of a MainPage that can be used for web automation purposes. It comes from /src/libs/libDemoOnlineStore/modules.


	public class MainPage extends BasePage {

		public MainPage() {
			super();
			load();
		}

		//selectors
		private static final By allProductsSelector = By.xpath("(//*[@id='main-nav']/ul/li)[last()]");

		private void load(){
			String url = Storage.get("Environment.Active.Web.url");
			PageCore.open(url);
			PageCore.waitForPageToLoad();
			if(! isLoaded("ONLINE STORE | Toolsqa Dummy Test site")){
				Log.error("Main page not loaded!");
			}
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

			return new ProductPage();
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

	return new ProductPage();
	
With this approach steps class can be build like in an example below

	public class DemoOnlieSteps extends BaseSteps {

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
		public void i_open_main_page() {
			//instantiate MainPage to open url in the browser
			main = new MainPage();
		}

		/**
		 * Navigates to all products page
		 */
		@And("^navigate to all products page$")
		public void navigate_to_all_products() {
			product = main.goToAllProduct();
		}

		/**
		 * Adds product {} to the cart.
		 *
		 * @param productName  name or value from storage
		 *
		 */
		@And("^add product (.*) to cart$")
		public void add_product_to_cart(String productName) {
			String input = StepCore.checkIfInputIsVariable(productName);
			product.addToCart(input);
		}

		/**
		 * navigates to Checkout page
		 */
		@And("^navigate to checkout page$")
		public void navigate_to_checkout_page() {
			checkout = product.goToCheckout();
		}


		/**
		 * Verifies that SubTotal field equals sub of total price per product type
		 * on Checkout page.
		 *
		 * Attaches screenshot to the report
		 */
		@Then("^verify that SubTotal value equals sum of totals per product type$")
		public void verify_sum_of_totals_per_product_type_equals_subTotal() {
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

			assertEquals("Sub-Total value is different than sum of price per product type",
				Double.valueOf(totalPrice),
				sum);

			if ( ! Double.valueOf(totalPrice).equals(sum) ) {
				Log.error("Sub-Total value is different than sum of price per product type");
			}
		}
	}


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

Please note that the web browser has to be explicitly open using step open browser. It is part of the CoreSteps and its content can be found there.

    /**
     * Opens browser of particular type as defined in the environment configuration
     */
    @Given("^open browser$")
    public void open_browser(){...}

The main purpose of this step is to create new selenium web driver that can be used in tests.
Browser type can be provided via configuration. For details please see parameters mentioned below in src/config/framework/framework.config as well as in src/config/environment/default.config

	Environment={

	    Default : {

		WebDrivers : {
		    CloseBrowserAfterScenario : true,
		    Chrome : {
			path : "src\\resources\\chromedriver.exe"
		    },
		    FireFox : {
			path : "src\\resources\\geckodriver.exe"
		    },
		    InternetExplorer : {
			path : "src\\resources\\IEDriverServer.exe"
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


We have to provide a path to a webDriver, browser type that shall be used in test and implicit timeout that will be used to report an exception if particular element will not be found on the page for amount of seconds defined.
Url that will be open can be defined as Environment.Default.Web.url.
It is also possible to provide browser width and height. To indicate max dimensions please use keyword "Max" else define them as String with format "width x height", for example "1024 x 960".


--------------------------------


How to write POM for windows UI automation?

In exactly same way like in case of web POM. Except we do not need to uase BasePage any more instead we can use BaseApp.
We can use WiniumCore to search/await or interact with any elements of the Gui.

What is different in comparison to web automation? Main difference is that apps can run on local host or remote host. In case remote host is in use we need to setup and remove connection (RDP/VNC) that will open windows os desktop.
This can also be done using WiniumCore and WinRSCore combaintation. Please see an example below.

	@Given("^on remote host (.+) login to an app as user (.+) with password (.+)$")
	public void on_remote_host_login_to__an_app(String node, String user, String pass) {

		String pathToApp = "C:\\path_to_my_app\\start_app.exe";

		WinRSCore.awaitForHostAvailability(node);
		WinRSCore.awaitForHostRdpAvailability(node);

		Log.debug("Closing any running instance of an app");
		String cmd = "foreach($proc in Get-Process | Where {$_.ProcessName -like 'an app'}){Stop-Process $proc}";
		WinRSCore.executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);

		WiniumCore.startApp(node, pathToApp, "");

		login = new AnAppLoginPage();
		main = login.loginToAnApp(user, pass);

	}

Please note that startApp method gets a node parameter which poinst to remote host configured in WinRM.config. If an app shall be started on the tester's workstation please use step like below
	
	 /**
     * Opens a gui app on a windows host without additional arguments
     *
     * @param pathToApp String, path to the executable file
     */
    @Given("^open an app from (.+) with args (.+)")
    public void open_an_app_from(String pathToApp, String args) {
        ExecutorCore.startApp(pathToApp, args);
        StepCore.sleep(2);
    }
	
In case an app shall be open on a remote host following step can be used
	
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
	
	
Any additional parameters that shall be passed to an *.exe file can be providedas a third input for startApp method.
	
Second thing that is different in comparison to web ui automation is the fact that we have to inspect UI elements using a dedicated application called inspector. It is part of windows sdk and can be downloaded from https://developer.microsoft.com/en-us/windows/downloads/sdk-archive.
Please use SDK version appropriate for particular winOS, for example windows 8.1. For windows 10 it is better to use Appium instead of Winium.
After installation with default settings inspect.exe can be found in C:\Program Files (x86)\Windows Kits\8.1\bin\x86 directory.

As locators user can use xpaths, automation ids, classes etc... exactly like in case of web UI automation.	



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
     *  scenarioCtx.response
     *  Environment.Active.Rest.url
     *  Environment.Active.Rest.url_post_suffix
     *
     * @param name, String, name of the template that contains http body of the request
     */
    @When("^json post request (.*?) is sent$")
    public void json_post_request_is_sent(String name) {

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
		scenarioCtx.put("response",ValidatableResponse.class, vResp);
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
     *  scenarioCtx.response
     *  Environment.Active.Rest.url
     *
     * @param name, String, name of the template that contains http body of the request
     * @param actionHeader, String, soap action that will be set in the header
     */
    @When("^xml post request (.*?) with soap action header (.*?) is sent$")
    public void xml_post_request_is_sent(String name, String actionHeader) {

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
        scenarioCtx.put("response",ValidatableResponse.class, vResp);
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

        String url = Storage.get("Environment.Active.Rest.url");
        Long statusCode = Storage.get("Expected.statusOK");
        Integer expectedCode = statusCode.intValue();
        given().when().log().all().get(url).then().statusCode(expectedCode);
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

    @Then("^verify that rest response has$")
    public void verify_that_response_has(List<Map<String, String>> table) {

		//get json or xml response
        Response response = scenarioCtx.get("response",Response.class);
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

User can change it if needed for a particular project. To do so please edit following parameters in configuration file, for example under src/config/environment/default.config. Default settings are visible below.

	Environment:{

	    Default: {

		    Rest: {
		    closeIdleConnectionsAfterEachResponseAfter: true,
		    closeIdleConnectionsAfterEachResponseAfter_idleTime: 10,
		    reuseHttpClientInstance: false,
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

As can be seen it uses regex and variable parts so we need to evaluate it before comparison will be done.
Now we just need to write a step that will read the content of csv and load it to our dB. Such step can look like this

    @When("^data from (.*?) csv file is loaded to table (.*?)$")
    public void data_from_csv_file_is_loaded(String fileName, String tableName){

        File input = new File(FileCore.getCurrentFeatureDirPath() + "/input/" +fileName+".csv");
        SqlCore.insertFromFile(input,tableName,true, "TestData."+fileName+"TypeMapping");
    }

Before we can load the data dB connection have to be open. For this we will use a step from CoreSteps.

    /**
     * Opens jdbc connection to database
     */
    @Given("^open db$")
    public void open_db() {
        ...
    }
    
It will open a new resource (connection) towards selected dB. Db can be set via environment configruation. See parameters mentioned below in src/config/framework/framework.config as well as src/config/environment/default.config
 
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

        List<Map<String,Object>> list = SqlCore.selectList("SELECT * FROM Dept");

        SqlCore.printList(list);
        File results = SqlCore.writeListToFile(list,"SqlResult","txt");

        scenarioCtx.put("SqlResults",File.class, results);
    }
    
Results will be stored as scenarioCtx object SqlResults for validation pruposes which can be done by other step def. They will be printed to the console and to a file for the purpose of template comparison.
To make writing of such steps as simple as possible please use SqlCore module.
Becuase our data set is very small we will use template compariosn

    @Then("^validate that result is like (.*)$")
    public void validate_that_result_is_like(String templateName) {

        File toCompare = scenarioCtx.get("SqlResults",File.class);
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

DB connection will be automatically closed in @After hook (implemented in CustomFormatter plugin).



--------------------------------



How to use executor to run system commands or 3rd party apps?



It is often needed and desired to have a possibility to execute any system command on a local host. Usually this can be used to trigger powershell commands or batch scripts on windows host but it maybe used to integrate any 3rd party application as well like wireshark/tshark/rawcap to catch network traces or autoIT to have a possibility to automate application under windows etc.

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
    public void execute_sample_command() {

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
    public void new_text_file_is_created() {

        String cmd = "powershell.exe " +
                "\"$stream = [System.IO.StreamWriter] " +
                "'t2.txt';" +
                "$line = 'testTestTESTtestTestTESTtestTestTESTtestTestTESTtestTestTESTtestTestTESTtestTestTEST';" +
                "1..100000 | % {$stream.WriteLine($line)};" +
                "$stream.close()\"";

        File workingDir = FileCore.createTempDir();
        String sWorkingDirPath = workingDir.getAbsolutePath();
        scenarioCtx.put("WorkingDir", String.class, sWorkingDirPath);

        ByteArrayOutputStream out = ExecutorCore.execute(cmd, workingDir, 10, true);

        Log.debug("Output is ");
        Log.debug(new String(out.toByteArray(), Charset.defaultCharset()));
    }

As can be seen step "new test file is created" will create w temporary directory and call a powershell script inside. It will create a new file with 100000 lines inside.

    @When("^read the file$")
    public void read_the_file(){

        String path = scenarioCtx.get("WorkingDir", String.class);
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
    public void pause_execution() {

        File workingDir = FileCore.createTempDir();
        String autoItPath = Storage.get("Environment.Active.apps.autoIt");
        String scriptsPath = Storage.get("Environment.Active.scripts.path");
        Integer timeout = Storage.get("Environment.Active.PauseDuration");

        String cmd = autoItPath + " " + FileCore.getProjectPath() + "\\" + scriptsPath + "\\pause.exe" + " " + Integer.toString(timeout);

        Log.debug("Calling autoIt pause script with timeout " + timeout + " seconds");

        ExecutorCore.execute(cmd, workingDir, timeout+3, true);

        Log.debug("Pause canceled or timeout. Resuming execution");
    }


Another possibility is to use ExecutorCore to trigger winRS, a command line winRM client from Microsoft. Let us have a look at an example step def that shows how this can be done.

    @Given("^execute via WinRS on node (.+)$")
    public void execute_via_WinRS_on_node(String node) {

        File workingDir = FileCore.createTempDir();

        String address = Storage.get("Environment.Active.WinRM." + node + ".host");
        Integer port = Storage.get("Environment.Active.WinRM." + node + ".port");
        String user = Storage.get("Environment.Active.WinRM." + node + ".user");
        String passwd = Storage.get("Environment.Active.WinRM." + node + ".password");

        String cmd = "'Hostname'";
        String invocation = "winrs -r:http://" + address + ":" + port + " -u:" + user + " -p:" + passwd;
        cmd =  invocation + " " + cmd;

        ByteArrayOutputStream out = ExecutorCore.execute(cmd, workingDir, 20, true);
        Log.debug("Output is ");
        Log.debug(new String(out.toByteArray(), Charset.defaultCharset()));

    }

This step will try to connect to a remote windows host and execute a command Hostname. Result will be name of the remote host.
For connectivity it uses configuration that shall be defined for winRM. Please read below for more details.



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
    public void read_pdf_file_from(String pathToFile) {

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
To use this feature we have to define ssh nodes in the configruation. Configuration is flat. This means that for each node we shall have a seperate entry in the config file. See an excerpt from /src/config/environment/ssh.config below.

	Ssh: {
	    node1: {
		host: "127.0.0.1",
		port: 4567,
		user: "vagrant",
		password: "vagrant"
	    }
	}

Where node1 is an identifier of a ssh node. We can configure multiple nodes in this way. By default port number 22 is going to be used.

User can have 2 options to interact with a node via ssh. It can execute a command in a session. After each command execution session is closed. For the next command new session is created. This is useful in case there are simple commands to be executed or user is interested in the stdout or exit status code. Alternatively user can open a shell and execute multiple commands in an interactive shell session. This is useful when user wants to for example switch to superuser account, run tcpdump, await for command execution etc.
In this case it is possible to define a timeout and expected output in the console. In case command does not return any output to the console user can append echo to make sure that something will be printed or await for a prompt symbol.

SshCore module is provided and contains a set of methods that can be use to manage ssh sessions as well as execute common tasks like for example check that node is accessible, wait for a file to be present on remote host or simply check that file exists.

See some examples below how to start simple session or shell session.

Feature file is

	@ssh
	Feature: Ssh

	  Scenario: list files in users home directory

	    Given list files in users home directory

Step implementation is available below

    @When("^list files in users home directory$")
    public void list_files_in_users_home_directory() {

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
    public void switch_user_to_root() {

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
    public void check_that_file_exists_on_remote_node() {

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
 


--------------------------------



Usage of winRM




To manage windows hosts one can use capabiliteis of winRM. From the end user point of view it works similar to ssh on unix. It allows to execute command on remote hosts and transfer files.


To use this feature we have to define winRM nodes in the configruation. Configuration is flat. This means that for each node we shall have a seperate entry in the config file. See an excerpt from /src/config/environment/winrm.config below.

	WinRM: {
	    node1: {
		host: "127.0.0.1",
		port: 55985,
		user: "Vagrant",
		domain: "UserDomain",
		password: "vagrant",
		useHttps: false,
		workingDirectory: "%TEMP%",
		AuthenticationScheme: "NTLM"
	    }
	}

Where node1 is an identifier of a winRM node. We can configure multiple nodes in this way. By default port number 5985 is going to be used.
Currently winRMCore supports just few authentication methods like NTLM, Basic and Kerberos. Default is NTLM with negotiate. Please not that to make connection available winRM needs to be configured on the remote host. winrm/config/service AllowUnencrypted property shall be set to true. Reason is that winrm uses an application level encryption which is currently not supported. In case this is needed and unencrypted communication is not allowed please use ExecutorCore and trigger winrs. An example is available above. 

Let us have a look how we can implement a simple step def that uses winRM capabilities.

    @Given("^windows host (.+) is alive$")
    public void windows_host_is_alive(String hostName) {

        String cmd = "Write-Host " + hostName + " is alive";

        Log.debug("Create new winRM session");
        WinRMCore.createClient(hostName);
        WinRmToolResponse result = WinRMCore.executePs(cmd, 3);

        Log.debug("Result is " + result.getStdOut());
        Log.debug("Exit code is " + result.getStatusCode());
        Log.debug("Error is " + result.getStdErr());
        WinRMCore.closeClient();

        String output = result.getStdOut().replaceAll("(\\r|\\n)", "");
        output = output.trim();
        Log.debug("Result is " + output);

        if ( ! output.equalsIgnoreCase(cmd.substring(11)) ) {
            Log.error("host " + hostName + " is not accessible");
        }
    }
    
As can be seen user first needs to create a new winRM client by calling WinRMCore.createClient() method. After that we can call one of the execute commands. There are seperate commands for powershell and windows cmd execution. There are also methods that accepts List of commands as an input. They can execute multiple commands instead of just one. When execution is done client shall be closed by calling of WinRMCore.closeClient() method. 

User can access stdOut, stdErr and status code for further verification. 

Please note that there are methods available that can create a script from a string on a remote host and execute it immediately. 
See an example below to better understand how to mount a network share and copy a file from it.

    @Given("^mount path (.+) as network drive (.+) via WinRS on remote node (.+)$")
    public void mount_path_as_network_drive_via_WinRS_on_remote_node(String path, String drive, String host) {

        path = "\\\\localhost\\c$\\Users\\vagrant\\Music";

        String cmd = Joiner.on("\n").join(
                "net use " + drive + ": " + path + " /persistent:no",
                "copy z:\\toJestPlikTekstowy.txt C:\\Users\\vagrant\\Documents");

        WinRMCore.createClient(host);
        WinRmToolResponse result = WinRMCore.executeBatchScriptFromString(cmd, "mount.bat","");

        Log.debug("Result is " + result.getStdOut());
        Log.debug("Exit code is " + result.getStatusCode());
        Log.debug("Error is " + result.getStdErr());

        WinRMCore.closeClient();

        String output = result.getStdOut().replaceAll("(\\r|\\n)", "");
        output = output.trim();
        Log.debug("Result is " + output);

    }

Step above will use net use to mount a network share (in this particular case path is hardcode to be a local folder). When this will be done we want to copy a file called toJestPlikTekstowy.txt to Documents directory of a user. To make this possible we will create a script called mount.bat on a remote host and execute its content immediately via winRM.



Windows remoting is used by WinRSCore module. Its purpose is to allow to do remote windows host mangement from windows workstation where winRS client is installed.
WinRS is a cmd winRM client developed by Microsoft.

 
WinRSCore uses winRM.config as well.

	WinRM: {
	    node1: {
		host: "127.0.0.1",
		port: 55985,
		user: "Vagrant",
		domain: "UserDomain",
		password: "vagrant"
	    }
	}

It connects by default to users home directory on local host and supports whole set of possibilities like files transfer, scheduling tasks execution etc. Please see methods available in winRSCore module for more details.



--------------------------------



Usage of cloud director integration



Basic cloud director integration is implemented. It allows to create new VM in an existing Vapp, as well as restart them, remove them and read their status.
Integration is done via cloud director api. Templates used to trigger api request can be found under libs/libCore/templates/vCloudDirector.

A feature file to create, restart, read status and delete new virtual machine can look like below.

	Feature: test VM creation

		Scenario: create and remove VM using vCloudDirector Soap api

			Given add new vm VM1 to vApp
			When powerOn vm VM1
			Then check vm VM1 status
				And check that DNS entry for remote host VM1 was updated
				And check vm VM1 status
				And reboot vm VM1
				And remove vm VM1 from vApp

Basic environment configuration is available in libs/libCore/config/cloud.config

	Environment:{
		Default: {
			vCloudDirector: {
				host:   "https://my.company.cloud.com",
				org:    "myorg",
				user:   "myuser",
				pass:   "mypass",
				api:    "1.5",

				GuestCustomizationSection: {
					DomainName: "my.company.cloud.com",
					DomainUserName: "myDomainUser",
					DomainUserPassword: "myDomainPassword",
					AdminPassword: "myAdminPassword"
					}
				}
			}
		}
			
Here user can setup login details and domain/admin password used by remote host.
Virtual machiens details can be provided in TestData, for example like below.

TestData: {

    VM1: {
        VAppTemplate:       "myVappTemplate",           #name of the vApp template
        CatalogItem:        "myCatalogItem",           	#name of the item available in the catalog that holds VApp templates
        Vdc:                "myVdc",                    #name of the vdc
        VApp:               "myVapp",                	#name of the vApp where vm shall be deployed (case sensitive)
        Catalog:            "myCatalog",                #name of the catalog
        VmTemplate:         "myVmTemplate",           	#name of the vm template from vApp template
        NewVmName:          "myVmName",            		#name of the vm to be deployed in vApp
        Network:            "myNetworkName",            #name of the network available in vdc
        ip_allocation_mode: "DHCP"

        #Memory:             "16384",                   #16 GB
        #Cpu:                "4"                        #number of available cpu
        #DiskSize:           "204800"                   #200 GB
    },

    VM2: {
        Vdc:                "myVdc",                    #name of the vdc
        VApp:               "myVapp",                   #name of the vApp where vm shall be deployed
        NewVmName:          "myVmName"                  #name of the vm to be deployed in vApp
       }

}

VM1 data is required to create new VM. VM2 data is required to restart/read it.
To understand it better please have a look below how cloud director works.

All virtual machines are created as a part of so called vApp. vApps are just containers for one or more virtual machines. Available resources (like cpu, memory, storage space, networks) are grouped together in so called VirtualDataCenters (vdc). This means that each vApp has to belong to 1 vdc. 
Each virtual machine has to belong to the network available in VDC.  
To make the whole process easier it is possible to create new virtual machines and vApps using templates. Templates are grouped together in the catalogs for easier management. Catalog contains items that group together one or more templates. 
 
From technical point of view new virtual machine creation process means that we want to assign new virtual machine created from a template available in the catalog (in one of the items) that belongs to particular vdc and assign it to an existing vApp. 
			
			
--------------------------------



Usage of sikuli

Sikuli uses image compariosn algorithms for automation purposes. Due to this fact there was not special sikuli integration done. It is recommended only to use this tool when Winium/Selenium will not be able to handle autoamtion (for example flash based elemnets automation).
Few usage examples are however provided below.
Sikuli shall be used together with Selenium or Winium driver to handle special cases only.

First we need to set a variable which contains path to the images we would like to use with Sikuli, for example

	String pathToImages = FileCore.getProjectPath() + File.separator + "libs" + File.separator +
        "libProject1" + File.separator + "resources" + File.separator + "sikuliImages" +
        File.separator;

Now we can instansiate Screen object like below.

		Screen screen = new Screen();

		Log.debug("About to click into the image Icon1.png");
	
		Integer sw = screen.getBounds().width;
		Integer sh = screen.getBounds().height;
		Log.debug("Screen size is " + sw + " x " + sh);
		
		screen.setRect(sw-sw/3,0,sw/3, sh).highlight().wait(new Pattern(pathToImages + "Icon1.png").similar(0.7f), 5);
		screen.setRect(sw-sw/3,0,sw/3, sh).highlight().hover(new Pattern(pathToImages + "Icon1.png").similar(0.7f));
		
		StepCore.sleep(1);
		screen.setRect(sw-sw/3,0,sw/3, sh).highlight().doubleClick();
		StepCore.sleep(1);

As can be seen we can extract information about screen size and use it to reduce screen part which shall be checked by sikuli.
It is nice to indicate that screen part to the user using red border. 
We can await for an element, click it, doubleclick it or use try catch block to get a screen shot and attach it to the report.
Please note that when selenium/winium driver is in use we do not need to use try/catch block to get a screenshot in case of failure. It will be automatically attached to the report.
Default similarity is 0.7. Reducing it to 0.5 or below usually does not make any sense.	
	
What else is possible with Sikuli? 

	- we can hover mouse over an element

		screen.wait(new Pattern(pathToImages + "Icon2.png"), 2);
		screen.mouseMove(0,30);
		screen.click(new Pattern(pathToImages + "Icon2.png"));
		screen.mouseMove(30,0);
		StepCore.sleep(1);

	- we can type
	
		screen.type("Q");
		
	- we can scroll using mouse wheel

		screen.wheel(WHEEL_DOWN, 40);	

and much, much more... please see sikuliX doxumentation and tutorials for more details.		


REMARK: if Sikuli is in use please remember that if one app covers other one it may fail to locate desired element on the screen (consider a case where multiple RDP or web browser windows are open on same host). 