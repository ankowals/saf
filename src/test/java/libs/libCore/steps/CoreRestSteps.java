package libs.libCore.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import groovy.util.slurpersupport.NodeChildren;
import io.restassured.internal.path.xml.NodeChildrenImpl;
import io.restassured.path.xml.XmlPath;
import io.restassured.path.xml.element.Node;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;
import libs.libCore.modules.SharedContext;

import java.io.File;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class CoreRestSteps extends BaseSteps {

    // PicoContainer injects class SharedContext
    public CoreRestSteps(SharedContext ctx) {
        super(ctx);
    }

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
    public void service_is_available() throws Throwable {
        Log.info("* Step started service_is_available");

        String url = Storage.get("Environment.Active.Rest.url");
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
    public void json_post_request_is_sent(String name) throws Throwable {
        Log.info("* Step started json_post_request_is_sent");

        String url = Storage.get("Environment.Active.Rest.url");
        String path = Storage.get("Environment.Active.Rest.url_post_suffix");

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
    public void xml_post_request_is_sent(String name, String actionHeader) throws Throwable {
        Log.info("* Step started xml_post_request_is_sent");

        String url = Storage.get("Environment.Active.Rest.url");
        File file = StepCore.evaluateTemplate(name);
        String sAction = StepCore.checkIfInputIsVariable(actionHeader);
        String sFile = FileCore.readToString(file);

        //build specification and use file template as a body content
        RequestSpecification request = given()
                .header("SOAPAction", sAction)
                .body(sFile)
                .with()
                .contentType("text/xml; charset=utf-8");

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
     * Verifies that response status code is {}
     *
     * Uses following objects:
     *  ctx.Object.response
     *
     * @param input  status code or value from storage
     *
     */
    @Then("^verify that status code is (.+)$")
    public void verify_that_status_code_is(String input){
        Log.info("* Step started verify_that_status_code_is");

        Integer statusCode = StepCore.checkIfInputIsVariable(input);
        ValidatableResponse response = ctx.Object.get("response",ValidatableResponse.class);

        try {
            response.statusCode(statusCode);
        } catch (AssertionError e) {
            Log.error("", e);
        }

    }


    /**
     * Verifies that particular key xml/json body response contains expected value
     * Multiple different comparisons can be executed. Following actions are supported
     * equalTo, containsString, containsInAnyOrder, greaterThan, lessThan
     *
     * @param table, DataTable, it shall contains 3 columns key, action, expected
     */
    @Then("^verify that rest response body has$")
    public void verify_that_response_has(List<Map<String, String>> table) throws Throwable {
        Log.info("* Step started verify_that_response_has");

        ValidatableResponse response = ctx.Object.get("response",ValidatableResponse.class);

        if ( table.size() == 0 ) {
            Log.error("Wrong format of table used in step. Please make sure that table contains "
                    + " header like: | key | action | expected_value | "
                    + " and rows with values for each column");
        }

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
