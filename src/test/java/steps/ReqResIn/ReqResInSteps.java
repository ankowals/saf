package steps.ReqResIn;

import cucumber.api.java.en.Given;
import modules.core.BaseSteps;
import modules.core.Log;
import modules.core.SharedContext;

import java.io.File;

import static io.restassured.RestAssured.given;

public class ReqResInSteps extends BaseSteps {

    // PicoContainer injects class SharedContext
    public ReqResInSteps (SharedContext ctx) {
        super(ctx);
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
    @Given("^service is available$")
    public void service_is_available() {
        Log.info("* Step started service_is_available");

        String url = Environment.readProperty("REST_url");
        Long statusCode = Storage.get("Expected.statusOK");
        Integer expectedCode = statusCode.intValue();
        given().when().log().all().get(url).then().statusCode(expectedCode);
    }

}
