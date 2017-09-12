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
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
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

    @Given("^a book exists with an isbn$")
    public void a_book_exists_with_isbn() {
        Log.debug("* Step started a_book_exists_with_isbn");
        HashMap<String, Object> testDataMap = ctx.obj.get("TestData",HashMap.class);
        String isbn = (String) testDataMap.get("isbn");

        String isbn2 = ctx.step.get("TestData.isbn");

        RequestSpecification request = given().param("q", "isbn:" + isbn);
        ctx.obj.put("request",RequestSpecification.class, request);
    }

    @When("^a user retrieves the book by isbn$")
    public void a_user_retrieves_the_book_by_isbn(){
        Log.debug("* Step started a_user_retrieves_the_book_by_isbn");
        String url = ctx.env.readProperty("REST_url");
        RequestSpecification request = ctx.obj.get("request",RequestSpecification.class);
        Response response = request.when().log().all().get(url);
        ctx.obj.put("response",Response.class, response);
        ctx.step.attachMessageToReport("Json response", response.prettyPrint().toString());
    }

    @Then("^the status code is (.*)$")
    public void verify_status_code(String input){
        Log.debug("* Step started verify_status_code");

        String statusCode = ctx.step.checkIfInputIsVariableAndReturnString(input);
        Integer code = Integer.parseInt(statusCode);

        Response response = ctx.obj.get("response",Response.class);
        ValidatableResponse json = response.then().statusCode(code);
        ctx.obj.put("json",ValidatableResponse.class, json);
    }

    @And("^response includes the following in any order$")
    public void response_contains_in_any_order(Map<String,String> responseFields){
        Log.debug("* Step started response_contains_in_any_order");
        ValidatableResponse json = ctx.obj.get("json",ValidatableResponse.class);
        for (Map.Entry<String, String> field : responseFields.entrySet()) {
            if(StringUtils.isNumeric(field.getValue())){
                json.body(field.getKey(), containsInAnyOrder(Integer.parseInt(field.getValue())));
            }
            else{
                json.body(field.getKey(), containsInAnyOrder(field.getValue()));
            }
        }
    }

    @And("^response includes the following$")
    public void response_equals(Map<String,String> responseFields){
        Log.debug("* Step started response_equals");
        ValidatableResponse json = ctx.obj.get("json",ValidatableResponse.class);
        for (Map.Entry<String, String> field : responseFields.entrySet()) {
            if(StringUtils.isNumeric(field.getValue())){
                json.body(field.getKey(), equalTo(Integer.parseInt(field.getValue())));
            }
            else{
                json.body(field.getKey(), equalTo(field.getValue()));
            }
        }
    }

}