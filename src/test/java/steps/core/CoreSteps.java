package steps.core;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import modules.core.*;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class CoreSteps extends BaseSteps {

    // PicoContainer injects class SharedContext
    public CoreSteps(SharedContext ctx) {
        super(ctx);
    }

    /**
     * Opens browser of particular type as defined in the environment configuration
     */
    @Given("^open browser$")
    public void open_browser() throws Throwable {
        Log.info("* Step started open_browser");

        EventFiringWebDriver driver = new DriverFactory(ctx).create();
        ctx.Object.put("Page", EventFiringWebDriver.class, driver);

        PageCore pageCore = new PageCore(ctx);
        ctx.Object.put("PageCore", PageCore.class, pageCore);
        Log.debug("Web driver created");
    }

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

    /**
     * Loads configuration from a particular file {}
     *
     * @param arg1, String, file path relative to features directory (shall start without separator)
     */
    @And("^configuration data from \"(.*?)\" is loaded$")
    public void load_local_test_data(String arg1) throws Throwable {
        Log.info("* Step started load_local_test_data");

        String path = FileCore.getFeaturesPath() + File.separator + arg1;
        ConfigReader Config = new ConfigReader(ctx);
        Config.create(path);

        Log.debug("Configuration from " + path + " loaded");
    }

    /**
     * Triggers macro evaluation for TestData storage and Expected data storage
     */
    @And("^macro evaluation is done$")
    public void eval_macro() throws Throwable {
        Log.info("* Step started eval_macro");

        Log.info("<- evaluating macros ->");
        Macro.eval("TestData");
        Macro.eval("Expected");

        Log.debug("Test data storage after macro evaluation is");
        Storage.print("TestData");
    }

    /**
     * Sets particular value of a key in the storage
     *
     * @param storageName, String, name of the storage
     * @param, value, String, value to be set
     */
    @And("^set (.+) in storage (.+)$")
    public void set_in_storage(String storageName, String value) throws Throwable {
        Log.info("* Step started set_in_storage");
        Storage.set(storageName, value);
        Storage.get(storageName);
    }

    /**
    ************************************
    ************************************
    *************** REST ***************
    ************************************
    ************************************
    **/

    /**
     * Verifies that service is available
     * This is just a sanity check.
     * It triggers GET request towards defined url
     * It checks that http response code is 200 (OK)
     *
     * Uses following objects:
     *  Expected.statusOK
     *  env.REST_url
     *
     */
    @Given("^service is available$")
    public void service_is_available() {
        Log.info("* Step started service_is_available");

        String url = Environment.readProperty("REST_url");
        Integer expectedCode = Storage.get("Expected.statusOK");
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

    /**
     * Triggers http post request with json body. Content of the body comes from the file.
     * ValidatableResponse is available as a context Object with name response.
     *
     * Uses following objects:
     *  ctx.Object.response
     *  Environment.REST_url
     *  Environment.Rest_url_post_path
     *
     * @param name, String, name of the template that contains http body of the request
     */
    @When("^json post request (.*?) is sent$")
    public void json_post_request_is_sent(String name) {
        Log.info("* Step started json_post_request_is_sent");

        String url = Environment.readProperty("REST_url");
        String path = Environment.readProperty("Rest_url_post_path");

        url = url + path;

        File file = StepCore.evaluateTemplate(name);

        //build specification and use file template as a body content
        RequestSpecification request = given()
                .body(file)
                .with()
                .contentType("application/json");

        //trigger request and log it (it will be added as attachment to the report)
        Response response = request
                .when()
                .log()
                .all()
                .post(url);

        //store response as ctx object so it can be verified by other steps and attach it to report
        ValidatableResponse vResp = response.then();
        ctx.Object.put("response",ValidatableResponse.class, vResp);
        StepCore.attachMessageToReport("Json response", response.prettyPrint());
    }


    /**
     * Triggers http post request with xml body (soap). Content of the body comes from the file.
     * ValidatableResponse is available as a context Object with name response.
     *
     * Uses following objects:
     *  ctx.Object.response
     *  Environment.REST_url
     *
     * @param name, String, name of the template that contains http body of the request
     * @param actionHeader, String, soap action that will be set in the header
     */
    @When("^xml post request (.*?) with soap action header (.*?) is sent$")
    public void xml_post_request_is_sent(String name, String actionHeader) {
        Log.info("* Step started xml_post_request_is_sent");

        String url = Environment.readProperty("REST_url");
        File file = StepCore.evaluateTemplate(name);
        String sAction = StepCore.checkIfInputIsVariable(actionHeader);
        String sFile = FileCore.readToString(file);

        //build specification and use file template as a body content
        RequestSpecification request = given()
                .header("SOAPAction", sAction)
                .body(sFile)
                .with()
                .contentType("text/xml");

        //trigger request and log it (it will be added as attachment to the report)
        Response response = request
                .when()
                .log()
                .all()
                .post(url);

        //store response as ctx object so it can be verified by other steps and attach it to report
        ValidatableResponse vResp = response.then();
        ctx.Object.put("response",ValidatableResponse.class, vResp);
        StepCore.attachMessageToReport("Xml response", response.prettyPrint());
    }

    /**
     * Verifies that particular key xml/json body response contains expected value
     * Multiple different comparisons can be executed. Following actions are supported
     * equalTo, containsString, containsInAnyOrder, greaterThan, lessThan
     *
     * @param table, DataTable, it shall contains 3 columns key, action, expected
     */
    @Then("^verify that rest response body has$")
    public void verify_that_response_has(List<Map<String, String>> table) {
        Log.info("* Step started verify_that_response_has");

        ValidatableResponse response = ctx.Object.get("response",ValidatableResponse.class);
        //ValidatableResponse vResp = response.then();

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
                AssertCore.validatableResponseBodyTableAssertion(response, key, action, expectedValue);
            }
        }
    }


}