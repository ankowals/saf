package libs.libCore.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;

import java.io.File;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class CoreRestSteps extends BaseSteps {

    /**
     * Verifies that service is available<br>
     * This is just a sanity check.<br>
     * It triggers GET request towards defined url<br>
     * It checks that http response code is 200 (OK)<br><br>
     *
     * Uses following objects:<br>
     *  Expected.statusOK<br>
     *  env.REST_url
     *
     */
    @Given("^service is available$")
    public void service_is_available(){

        String url = Storage.get("Environment.Active.Rest.url");
        Integer expectedCode = Storage.get("Expected.statusOK");
            given()
                    .when()
                    .log()
                    .all()
                    .get(url)
                    .then()
                    .statusCode(expectedCode);
    }

    /**
     * Triggers http post request with json body. Content of the body comes from the file.<br>
     * ValidatableResponse is available as a context Object with name response.<br><br>
     *
     * Uses following objects:<br>
     *  scenarioCtx.response<br>
     *  Environment.REST_url<br>
     *  Environment.Rest_url_post_path
     *
     * @param name, String, name of the template that contains http body of the request
     */
    @When("^send json post request (.*?)$")
    public void send_json_post_request(String name) {
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
        scenarioCtx.put("response",ValidatableResponse.class, vResp);
        StepCore.attachMessageToReport("Json response", response.prettyPrint());
    }


    /**
     * Triggers http post request with xml body (soap). Content of the body comes from the file.<br>
     * ValidatableResponse is available as a context Object with name response.<br><br>
     *
     * Uses following objects:<br>
     *  scenarioCtx.response<br>
     *  Environment.REST_url
     *
     * @param name, String, name of the template that contains http body of the request
     * @param actionHeader, String, soap action that will be set in the header
     */
    @When("^send xml post request (.*?) with soap action header (.*?)$")
    public void send_xml_post_request_with_soap_action_header(String name, String actionHeader) {

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
        scenarioCtx.put("response",ValidatableResponse.class, vResp);
        StepCore.attachMessageToReport("Xml response", response.prettyPrint());
    }


    /**
     * Verifies that response status code is {}<br><br>
     *
     * Uses following objects:<br>
     *  scenarioCtx.response
     *
     * @param input  status code or value from storage
     *
     */
    @Then("^verify that status code is (.+)$")
    public void verify_that_status_code_is(String input){
        Integer statusCode = StepCore.checkIfInputIsVariable(input);
        ValidatableResponse response = scenarioCtx.get("response",ValidatableResponse.class);

        int code = response.extract().statusCode();

        if ( code != statusCode){
            Log.error("Wrong status code received! Expected was " + statusCode + " but got " + code);
        }

    }


    /**
     * Verifies that particular key xml/json body response contains expected value<br>
     * Multiple different comparisons can be executed. Following actions are supported<br>
     * equalTo, containsString, containsInAnyOrder, greaterThan, lessThan<br>
     *
     * @param table, DataTable, it shall contains 3 columns key, action, expected
     */
    @Then("^verify that rest response body has$")
    public void verify_that_response_has(List<Map<String, String>> table) {

        ValidatableResponse response = scenarioCtx.get("response",ValidatableResponse.class);

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
                RestCore.validatableResponseBodyTableAssertion(response, key, action, expectedValue);
            }
        }
    }

}