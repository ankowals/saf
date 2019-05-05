package libs.libDemoApi.steps;

import cucumber.api.java.en.When;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import libs.libCore.modules.BaseSteps;

import java.io.File;

import static io.restassured.RestAssured.given;

public class CountryInfoSteps extends BaseSteps {


    @When("get country info via soap api")
    public void get_country_info_via_soap_api(){
        String url = Storage.get("Environment.Active.Rest.url");
        File file = StepCore.evaluateTemplate("FullCountryInfo");
        String content = FileCore.readToString(file);

        //build specification and use file template as a body content
        RequestSpecification request = given()
                .body(content)
                .with()
                .contentType("text/xml; charset=utf-8");

        //trigger request and log it (it will be added as attachment to the report)
        Response response = request
                .when()
                .log()
                .all()
                .post(url);

        //store response as ctx object so it can be verified by other steps and attach it to report
        ValidatableResponse vResp = response.then();
        scenarioCtx.put("response",ValidatableResponse.class, vResp);
        StepCore.attachMessageToReport("Xml response", response.prettyPrint());
    }
}
