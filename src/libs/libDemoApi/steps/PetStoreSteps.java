package libs.libDemoApi.steps;

import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import libs.libCore.modules.BaseSteps;
import libs.libDemoApi.modules.ApiClient;
import libs.libDemoApi.modules.PetApi;
import libs.libDemoApi.modules.Category;
import libs.libDemoApi.modules.Pet;
import org.testng.Assert;

import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static io.restassured.config.RestAssuredConfig.config;
import static libs.libDemoApi.modules.GsonObjectMapper.gson;

public class PetStoreSteps extends BaseSteps {

    @Given("^add pet$")
    public void add_pet(){
        PetApi api = ApiClient.api(ApiClient.Config.apiConfig().reqSpecSupplier(
                () -> new RequestSpecBuilder().setConfig(config().objectMapperConfig(objectMapperConfig().defaultObjectMapper(gson())))
                        .addFilter(new RequestLoggingFilter())
                        .addFilter(new ResponseLoggingFilter())
                        .setBaseUri("https://petstore.swagger.io/v2"))).pet();

        Pet pet = new Pet();
        pet.category(new Category().name("dog")).name("Lena").id(1l);

        api.addPet().body(pet).execute(r -> r.prettyPeek());
    }

    @When("^get pet$")
    public void get_pet(){
        PetApi api = ApiClient.api(ApiClient.Config.apiConfig().reqSpecSupplier(
                () -> new RequestSpecBuilder().setConfig(config().objectMapperConfig(objectMapperConfig().defaultObjectMapper(gson())))
                        .addFilter(new RequestLoggingFilter())
                        .addFilter(new ResponseLoggingFilter())
                        .setBaseUri("https://petstore.swagger.io/v2"))).pet();

        Long petId = 1l;
        Response response = api.getPetById().petIdPath(petId).execute(r -> r.prettyPeek());
        ValidatableResponse validatableResponse = response.then();
        scenarioCtx.put("response",ValidatableResponse.class, validatableResponse);
        StepCore.attachMessageToReport("Json response", response.prettyPrint());
    }

    @Then("^verify that pet name is (.+)$")
    public void verify_that_pet_name_is(String expectedName){
        ValidatableResponse response = scenarioCtx.get("response",ValidatableResponse.class);
        String name = response.extract().path("name");

        Assert.assertEquals(name, expectedName);
    }


    @Given("^add pet with validation filter$")
    public void add_pet_with_validation_filter(){
        OpenApiValidationFilter validationFilter = new OpenApiValidationFilter("http://petstore.swagger.io/v2/swagger.json");

        PetApi api = ApiClient.api(ApiClient.Config.apiConfig().reqSpecSupplier(
                () -> new RequestSpecBuilder().setConfig(config().objectMapperConfig(objectMapperConfig().defaultObjectMapper(gson())))
                        .addFilter(new RequestLoggingFilter())
                        .addFilter(new ResponseLoggingFilter())
                        .addFilter(validationFilter)
                        .setBaseUri("https://petstore.swagger.io/v2"))).pet();

        Pet pet = new Pet();
        pet.category(new Category().name("dog")).name("Lena").id(1l);

        api.addPet().body(pet).execute(r -> r.prettyPeek());
    }

}
