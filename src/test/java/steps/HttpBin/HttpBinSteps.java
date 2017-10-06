package steps.HttpBin;

import cucumber.api.java.en.Given;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import modules.core.BaseSteps;
import modules.core.Log;
import modules.core.SharedContext;

import java.io.File;

import static io.restassured.RestAssured.given;

public class HttpBinSteps extends BaseSteps {

    // PicoContainer injects class SharedContext
    public HttpBinSteps(SharedContext ctx) {
        super(ctx);
    }

    @Given("^simple POST request (.*?)$")
    public void simple_POST_request(String name) {
        Log.info("* Step started simple_POST_request");

        String url = Environment.readProperty("REST_url") + "/post";

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

        //store response as ctx object so it can be verify by other steps and attach it to report
        ctx.Object.put("response",Response.class, response);
        StepCore.attachMessageToReport("Json response", response.prettyPrint().toString());
    }


}
