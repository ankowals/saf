package libs.libDemoOnlineStore.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.testng.Assert;

//Without usage of BasePage and Page Object Model

public class GoogleSearchSteps extends BaseSteps {

    @Given("^navigate to google search$")
    public void navigate_to_google_search(){
        EventFiringWebDriver driver = scenarioCtx.get("SeleniumWebDriver", EventFiringWebDriver.class);
        driver.get("http://www.google.pl");
        StepCore.sleep(2);
    }

    @When("^enter (.+) into search input$")
    public void enter_into_search_input(String input){
        EventFiringWebDriver driver = scenarioCtx.get("SeleniumWebDriver", EventFiringWebDriver.class);
        WebElement element = driver.findElement(By.id("lst-ib"));
        Log.debug("Entering text " + input);
        element.sendKeys(input);
        element.submit();
        StepCore.sleep(2);
    }

    @Then("verify that first result text is (.+)$")
    public void verify_that_first_result_text_is(String input){
        EventFiringWebDriver driver = scenarioCtx.get("SeleniumWebDriver", EventFiringWebDriver.class);
        WebElement element = driver.findElement(By.xpath("(//h3)[1]"));
        Log.debug("Get text of the element identified " + By.id("(//h3)[1]"));
        Assert.assertEquals(element.getText(), input);

        Log.debug("TRALALALA");
/*
        if ( ! element.getText().equals(input) ){
            Log.error("Expected text " + input + " not found! Instead found " + element.getText());
        }
*/
    }

    @Then("verify that element (.+) is present on the page$")
    public void verify_that_element_is_present_on_the_page(String input){
        EventFiringWebDriver driver = scenarioCtx.get("SeleniumWebDriver", EventFiringWebDriver.class);
        WebElement element = driver.findElement(By.xpath(input));
        //such xpath does not exists so we shall get an error from Selenium that element can't be found

        Log.debug("TRALALALA");

    }

}
