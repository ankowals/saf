package libs.libReqResIn.steps;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import static io.restassured.RestAssured.given;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;
import java.io.File;

public class ReqResInSteps extends BaseSteps {


    /**
     * extracts user id from the response
     */
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


    /**
     * Triggers http get request with variable path
     * ValidatableResponse is available as a context Object with name response.
     */
    @When("^json get request single user with id (.+) is sent$")
    public void json_get_request_is_sent(String id){
        String url = Storage.get("Environment.Active.Rest.url");
        String path = Storage.get("Environment.Active.Rest.url_get_suffix");
        String userId = globalCtx.get(id,String.class);

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


    /**
     * Triggers http put request with json body. Content of the body comes from the file.
     * ValidatableResponse is available as a context Object with name response.
     *
     * @param name, String, name of the template that contains http body of the request
     */
    @When("^json put request (.*?) to modify single user with id (.*?) is sent$")
    public void json_put_request_is_sent(String name, String id) {
        String url = Storage.get("Environment.Active.Rest.url");
        String path = Storage.get("Environment.Active.Rest.url_put_suffix");

        url = url + path + id;

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
                .put(url);

        //store response as ctx object so it can be verified by other steps and attach it to report
        ValidatableResponse vResp = response.then();
        scenarioCtx.put("response",ValidatableResponse.class, vResp);
        StepCore.attachMessageToReport("Json response", response.prettyPrint());
    }



    /**
     * Triggers http delete request with variable path
     * ValidatableResponse is available as a context Object with name response.
     */
    @When("^json delete single user request with id (.+) is sent$")
    public void json_delete_request_is_sent(String userId){
        String url = Storage.get("Environment.Active.Rest.url");
        String path = Storage.get("Environment.Active.Rest.url_get_suffix");

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
                .delete(url);

        //store response as ctx object so it can be verified by other steps and attach it to report
        ValidatableResponse vResp = response.then();
        scenarioCtx.put("response",ValidatableResponse.class, vResp);
        StepCore.attachMessageToReport("Json response", response.prettyPrint());
    }


}