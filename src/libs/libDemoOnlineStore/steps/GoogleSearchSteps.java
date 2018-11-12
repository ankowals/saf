package libs.libDemoOnlineStore.steps;

import cucumber.api.java.en.Given;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.EventFiringWebDriver;

//Without usage of BasePage and Page Object Model

public class GoogleSearchSteps extends BaseSteps {

    @Given("^navigate to google search$")
    public void navigate_to_google_search() throws Throwable{
        EventFiringWebDriver driver = scenarioCtx.get("SeleniumWebDriver", EventFiringWebDriver.class);
        driver.get("http://www.google.pl");
        Thread.sleep(2000);
    }

    @Given("^enter (.+) into search input$")
    public void enter_into_search_input(String input) throws Throwable{
        EventFiringWebDriver driver = scenarioCtx.get("SeleniumWebDriver", EventFiringWebDriver.class);
        WebElement element = driver.findElement(By.id("lst-ib"));
        Log.debug("Entering text " + input);
        element.sendKeys(input);
        element.submit();
        Thread.sleep(2000);
    }

}
