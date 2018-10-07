package libs.libBookByIsbn.steps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

public class BookByIsbnSteps extends BaseSteps {

    /**
     * Creates Request Specification and stores it as request scenarioCtx
     */
    @Given("^a book exists with an isbn$")
    public void a_book_exists_with_isbn() {
        String isbn = Storage.get("TestData.isbn");
        RequestSpecification request = given().param("q", "isbn:" + isbn);
        scenarioCtx.put("request",RequestSpecification.class, request);
    }

    /**
     * Triggers http GET request as specified in RequestSpecification
     * Response is stored as response ctx.obj and attached to the report
     */
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


    /**
     * Verifies that response includes some fields {} and their value contains {}
     * Input requires a table
     *
     * @param responseFields content of a table that contains key and expected value pairs to verify
     */
    @And("^response includes the following in any order$")
    public void response_contains_in_any_order(Map<String,String> responseFields){
        ValidatableResponse json = scenarioCtx.get("json",ValidatableResponse.class);
        for (Map.Entry<String, String> field : responseFields.entrySet()) {
            Object expectedValue = StepCore.checkIfInputIsVariable(field.getValue());
            String type = expectedValue.getClass().getName();
            if(type.contains("Int")){
                Integer iExpVal = (int) expectedValue;
                Log.debug("Expected is " + field.getKey() + "=" + iExpVal);
                Log.debug("Current is " + json.extract().path(field.getKey()));
                json.body(field.getKey(), containsInAnyOrder(iExpVal));
            }
            else {
                String sExpVal = (String) expectedValue;
                Log.debug("Expected is " + field.getKey() + "=" + sExpVal);
                Log.debug("Current is " + json.extract().path (field.getKey()));
                json.body(field.getKey(), containsInAnyOrder(sExpVal));
            }
        }
    }

    /**
     * Verifies that response includes some fields {} nad their value equals to {}
     * Input requires a table
     *
     * @param responseFields content of a table that contains key and expected value pairs to verify
     *
     */
    @And("^response includes the following$")
    public void response_includes_the_following(Map<String,String> responseFields){
        ValidatableResponse json = scenarioCtx.get("json",ValidatableResponse.class);
        for (Map.Entry<String, String> field : responseFields.entrySet()) {
            Object expectedValue = StepCore.checkIfInputIsVariable(field.getValue());
            String type = expectedValue.getClass().getName();
            if(type.contains("Int")){
                Integer iExpVal = (int) expectedValue;
                Log.debug("Expected is " + field.getKey() + "=" + iExpVal.intValue());
                Log.debug("Current is " + json.extract().path(field.getKey()));
                json.body(field.getKey(), equalTo(iExpVal.intValue()));
            }
            else {
                String sExpVal = (String) expectedValue;
                Log.debug("Expected is " + field.getKey() + "=" + sExpVal);
                Log.debug("Current is " + json.extract().path (field.getKey()));
                json.body(field.getKey(), equalTo(sExpVal));
            }
        }
    }

}