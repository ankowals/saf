package steps.BookByIsbn;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import modules.core.Log;
import modules.core.SharedContext;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

public class BookByIsbn {

    private SharedContext ctx;

    // PicoContainer injects class SharedContext
    public BookByIsbn(SharedContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Creates Request Specification and stores it as request ctx.obj
     *
     * Uses following objects:
     *  TestData.isbn
     *
     */
    @Given("^a book exists with an isbn$")
    public void a_book_exists_with_isbn() {
        Log.info("* Step started a_book_exists_with_isbn");

        String isbn = ctx.step.get("TestData.isbn");
        RequestSpecification request = given().param("q", "isbn:" + isbn);
        ctx.obj.put("request",RequestSpecification.class, request);
    }

    /**
     * Triggers http GET request as specified in RequestSpecification
     * Response is stored as response ctx.obj and attached to the report
     *
     * Uses following objects:
     *  ctx.obj.request
     *
     */
    @When("^a user retrieves the book by isbn$")
    public void a_user_retrieves_the_book_by_isbn(){
        Log.info("* Step started a_user_retrieves_the_book_by_isbn");

        String url = ctx.env.readProperty("REST_url");
        RequestSpecification request = ctx.obj.get("request",RequestSpecification.class);
        Response response = request.when().log().all().get(url);
        ctx.obj.put("response",Response.class, response);
        ctx.step.attachMessageToReport("Json response", response.prettyPrint().toString());
    }


    /**
     * Verifies that response status code is {}
     * Creates new object ValidatableResponse and stores it as json ctx.obj
     *
     * Uses following objects:
     *  ctx.obj.response
     *
     * @param input  status code or value from storage
     *
     */
    @Then("^the status code is (.*)$")
    public void verify_status_code(String input){
        Log.info("* Step started verify_status_code");

        Long statusCode = ctx.step.checkIfInputIsVariable(input);
        Integer code = statusCode.intValue();

        Response response = ctx.obj.get("response",Response.class);
        ValidatableResponse json = response.then().statusCode(code);
        ctx.obj.put("json",ValidatableResponse.class, json);
    }

    /**
     * Verifies that response includes some fields {} and their value contains {}
     * Input requires a table
     *
     * Uses following objects:
     *  ctx.obj.json
     *
     * @param responseFields content of a table that contains key and expected value pairs to verify
     *
     */
    @And("^response includes the following in any order$")
    public void response_contains_in_any_order(Map<String,String> responseFields){
        Log.info("* Step started response_contains_in_any_order");

        ValidatableResponse json = ctx.obj.get("json",ValidatableResponse.class);
        for (Map.Entry<String, String> field : responseFields.entrySet()) {
            Object expectedValue = ctx.step.checkIfInputIsVariable(field.getValue());
            String type = expectedValue.getClass().getName();
            if(type.contains("Long")){
                Long lExpVal = (Long) expectedValue;
                Log.debug("lExpVal is " + lExpVal.intValue());
                json.body(field.getKey(), containsInAnyOrder(lExpVal.intValue()));
            }
            else {
                String sExpVal = (String) expectedValue;
                json.body(field.getKey(), containsInAnyOrder(sExpVal));
            }
        }
    }

    /**
     * Verifies that response includes some fields {} nad their value equals to {}
     * Input requires a table
     *
     * Uses following objects:
     *  ctx.obj.json
     *
     * @param responseFields content of a table that contains key and expected value pairs to verify
     *
     */
    @And("^response includes the following$")
    public void response_equals(Map<String,String> responseFields){
        Log.info("* Step started response_equals");

        ValidatableResponse json = ctx.obj.get("json",ValidatableResponse.class);
        for (Map.Entry<String, String> field : responseFields.entrySet()) {
            Object expectedValue = ctx.step.checkIfInputIsVariable(field.getValue());
            String type = expectedValue.getClass().getName();
            if(type.contains("Long")){
                Long lExpVal = (Long) expectedValue;
                Log.debug("lExpVal is " + lExpVal.intValue());
                json.body(field.getKey(), equalTo(lExpVal.intValue()));
            }
            else {
                String sExpVal = (String) expectedValue;
                json.body(field.getKey(), equalTo(sExpVal));
            }
        }
    }

}