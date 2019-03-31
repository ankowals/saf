package libs.libDemoApi.steps;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;

import java.io.File;

import static io.restassured.RestAssured.given;

public class ReqRestInSteps extends BaseSteps {

    /**
     * Creates user in a ReqResIn service
     */
    @When("^create user via http post$")
    public void create_user_via_http_post() {
        String url = Storage.get("Environment.Active.Rest.url");
        String path = Storage.get("Environment.Active.Rest.UserServiceSuffix");
        url = url + path;
        File file = StepCore.evaluateTemplate("createUser");
        String content = FileCore.readToString(file);

        //build specification and use file template as a body content
        RequestSpecification request = given()
                .body(content)
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
     * extracts user id from the response
     *
     * @param id String, name of the variable that holds id of the user
     */
    @Then("^extract user id as (.+)$")
    public void extract_user_id(String id) {
        ValidatableResponse response = scenarioCtx.get("response",ValidatableResponse.class);
        String userId = response.extract().path("id");

        if ( userId == null || Integer.parseInt(userId) <= 0 ) {
            Log.error("UserId was not found in the response!");
        }

        globalCtx.put(id, String.class, userId);
        Storage.set(id, userId);
    }


    /**
     * Triggers http get request to retrieve user details
     *
     * @param id String, name of the variable that holds id of the user
     */
    @When("^retrieve details of a user with id (.+)$")
    public void retrieve_details_of_a_user_with_id(String id){
        String url = Storage.get("Environment.Active.Rest.url");
        String path = Storage.get("Environment.Active.Rest.UserServiceSuffix");

        //get user id from the TestData storage
        String identifier = StepCore.checkIfInputIsVariable(id);
        Log.debug("User id from the TestData storage is " + identifier);

        //get user id from the global variable
        String userId = globalCtx.get(id,String.class);

        Log.debug("userId is " + userId);
        url = url + path + "/" + userId;

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
     * Triggers http put request to modify data of particular user
     *
     * @param id String, name of the variable that holds id of the user
     */
    @When("^modify user with id (.+)$")
    public void modify_user_with_id(String id) {
        String url = Storage.get("Environment.Active.Rest.url");
        String path = Storage.get("Environment.Active.Rest.UserServiceSuffix");

        String identifier = StepCore.checkIfInputIsVariable(id);

        url = url + path + "/" + identifier;

        File file = StepCore.evaluateTemplate("modifyUser");
        String content = FileCore.readToString(file);

        //build specification and use file template as a body content
        RequestSpecification request = given()
                .body(content)
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
     * Triggers http delete to remove particular user.
     *
     * @param id String, name of the variable that holds id of the user
     */
    @When("^delete user with id (.+)$")
    public void delete_user_with_id(String id){
        String url = Storage.get("Environment.Active.Rest.url");
        String path = Storage.get("Environment.Active.Rest.UserServiceSuffix");

        String identifier = StepCore.checkIfInputIsVariable(id);

        Log.debug("userId is " + identifier);
        url = url + path + "/" + identifier;

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