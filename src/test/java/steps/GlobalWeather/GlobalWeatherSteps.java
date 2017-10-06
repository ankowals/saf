package steps.GlobalWeather;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.restassured.path.json.JsonPath;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import modules.core.BaseSteps;
import modules.core.Log;
import modules.core.SharedContext;

import java.io.File;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GlobalWeatherSteps extends BaseSteps {

    // PicoContainer injects class SharedContext
    public GlobalWeatherSteps(SharedContext ctx) {
        super(ctx);
    }

    @Given("^simple XML request (.*?)$")
    public void simple_XML_request(String name) {
        Log.info("* Step started simple_POST_request");

        String url = Environment.readProperty("REST_url");

        File file = StepCore.evaluateTemplate(name);

        String sFile = FileCore.readToString(file);
        //build specification and use file template as a body content
        RequestSpecification request = given()
                                        .header("SOAPAction", "http://www.webserviceX.NET/GetCitiesByCountry")
                                        .body(sFile)
                                        .with()
                                        .contentType("text/xml");

        //trigger request and log it (it will be added as attachment to the report)
        Response response = request
                                .when()
                                .log()
                                .all()
                                .post(url);

        //store response as ctx object so it can be verify by other steps and attach it to report
        ctx.Object.put("response",Response.class, response);
        StepCore.attachMessageToReport("Xml response", response.prettyPrint().toString());
    }


    @Then("^verify that response has$")
    public void verify_that_response_has(Map<String,String> input) {
        Log.info("* Step started verify_that_response_has");

        Response response = ctx.Object.get("response",Response.class);
        ValidatableResponse vResp = response.then();

        for (Map.Entry<String, String> field : input.entrySet()) {
            String[] list = field.getKey().split(",");
            if (list.length != 2) {
                Log.error("Wrong configuration. Please define field name and assert action!");
            }
            String key = list[0].trim();
            String action = list[1].trim();

            Log.debug("Key is " + key);
            Log.debug("Action is " + action);

            Object expectedValue = StepCore.checkIfInputIsVariable(field.getValue());
            String type = expectedValue.getClass().getName();

            String cType = vResp.extract().path(key).getClass().getName();

            Log.debug("Expected value is " + expectedValue);
            Log.debug("Its type is " + type);

            Log.debug("Current is " + vResp.extract().path(key));
            Log.debug("Its type is " + cType);

            if (action.equalsIgnoreCase("equalTo")){
                try {
                    vResp.body(key, equalTo(expectedValue));
                } catch (AssertionError e) {
                    Log.error("", e);
                }
            } else if (action.equalsIgnoreCase("containsString")){
                try {
                    vResp.body(key, containsString(expectedValue.toString()));
                } catch (AssertionError e) {
                    Log.error("", e);
                }
            } else if (action.equalsIgnoreCase("containsInAnyOrder")){
                try {
                    vResp.body(key, containsInAnyOrder(expectedValue));
                } catch (AssertionError e) {
                    Log.error("", e);
                }
            } else if (action.equalsIgnoreCase("greaterThan")){
                if (cType.contains("Int")) {
                    try {
                        vResp.body(key, greaterThan((int) (long) expectedValue));
                    } catch (AssertionError e) {
                        Log.error("", e);
                    }
                } else if (cType.contains("Long")) {
                    try {
                        vResp.body(key, greaterThan((Long) expectedValue));
                    } catch (AssertionError e) {
                        Log.error("", e);
                    }
                } else if (cType.contains("Double")) {
                    try {
                        vResp.body(key, greaterThan((Double) expectedValue));
                    } catch (AssertionError e) {
                        Log.error("", e);
                    }
                } else if (cType.contains("Float")) {
                    try {
                        vResp.body(key, greaterThan((Float) expectedValue));
                    } catch (AssertionError e) {
                        Log.error("", e);
                    }
                } else {
                    Log.error("Type not supported for greaterThen comparison. Please use one of Int, Long, Double, Float");
                }
            } else if (action.equalsIgnoreCase("lessThan")){
                if (cType.contains("Int")) {
                    try {
                        vResp.body(key, lessThan((int) (long) expectedValue));
                    } catch (AssertionError e) {
                        Log.error("", e);
                    }
                } else if (cType.contains("Long")) {
                    try {
                        vResp.body(key, lessThan((Long) expectedValue));
                    } catch (AssertionError e) {
                        Log.error("", e);
                    }
                } else if (cType.contains("Double")) {
                    try {
                        vResp.body(key, lessThan((Double) expectedValue));
                    } catch (AssertionError e) {
                        Log.error("", e);
                    }
                } else if (cType.contains("Float")) {
                    try {
                        vResp.body(key, lessThan((Float) expectedValue));
                    } catch (AssertionError e) {
                        Log.error("", e);
                    }
                } else {
                    Log.error("Type not supported for lessThan comparison. Please use one of Int, Long, Double, Float");
                }
            } else {
                Log.error("Action " + action + " not supported. Please use one of " +
                    " equalTo, containsInAnyOrder, containsString, greaterThan, lessThan");
            }

        }
    }

}
