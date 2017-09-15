package steps.ReqResIn;

import cucumber.api.java.en.Given;
import modules.core.Log;
import modules.core.SharedContext;

import static io.restassured.RestAssured.given;

public class ReqResInSteps {

    private SharedContext ctx;

    // PicoContainer injects class BaseTest
    public ReqResInSteps (SharedContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Verifies if service is available
     * This is just a sanity check.
     * It triggers GET request towards defined url
     *
     * Uses following objects:
     *  Expected.statusOK
     *  env.REST_url
     *
     */
    @Given("service is available")
    public void service_is_available() {
        Log.info("* Step started service_is_available");

        String url = ctx.env.readProperty("REST_url");
        Long statusCode = ctx.step.get("Expected.statusOK");
        Integer expectedCode = statusCode.intValue();
        given().when().log().all().get(url).then().statusCode(expectedCode);
    }



}
