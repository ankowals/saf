package steps.ReqResIn;

import cucumber.api.java.en.Given;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import modules.core.Log;
import modules.core.SharedContext;

import java.util.HashMap;

import static io.restassured.RestAssured.given;

public class ReqResInSteps {

    private SharedContext ctx;

    // PicoContainer injects class BaseTest
    public ReqResInSteps (SharedContext ctx) {
        this.ctx = ctx;
    }

    @Given("service is available")
    public void service_is_available() {
        Log.debug("* Step started service_is_available");
        String url = ctx.env.readProperty("REST_url");
        //get test data storage from ctx obj
        HashMap<String, Object> testDataMap = ctx.obj.get("TestData",HashMap.class);
        String sExpectedCode = (String) testDataMap.get("statusOK");

        //parse string to int
        Integer expectedCode = Integer.parseInt(sExpectedCode);
        given().when().log().all().get(url).then().statusCode(expectedCode);
    }



}
