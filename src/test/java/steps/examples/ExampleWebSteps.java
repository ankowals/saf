package steps.examples;

import modules.core.Log;

import modules.core.SharedContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import ru.yandex.qatools.allure.Allure;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ExampleWebSteps {

    private SharedContext ctx;

    // PicoContainer injects class BaseTest
    public ExampleWebSteps (SharedContext ctx) {
        this.ctx = ctx;
    }

    @When("^I open seleniumframework website$")
    public void i_open_seleniumframework_website() throws Throwable {
        Log.debug("* Step started i_open_seleniumframework_website");
        String url = ctx.env.readProperty("WEB_url");
        Log.info("Going to open " + url);
        ctx.driver.get(url);
    }

    @Then("^I validate title and URL$")
    public void i_print_title_and_URL() throws Throwable {
        Log.debug("* Step started i_print_title_and_URL");
        Log.info("Going to validate an Url");
        Log.debug("Page title is " + ctx.driver.getTitle());
        Log.debug("Current url is " + ctx.driver.getCurrentUrl());
        assertEquals("Google",ctx.driver.getTitle());
        assertThat(ctx.driver.getCurrentUrl(),containsString("www.google.pl"));
    }

    @When("^I open google page$")
    public void i_open_google_website() throws Throwable {
        Log.debug("* Step started i_open_google_website");
        String url = ctx.env.readProperty("WEB_url");
        Log.info("Going to open " + url);
        ctx.driver.get(url);
    }

    @Then("^I check for input element$")
    public void i_check_for_input_elmement() throws Throwable {
        Log.debug("* Step started i_check_for_input_elmement");
        Log.info("Going to locate input element");
        WebElement element = ctx.driver.findElement(By.id("lst-ib"));
    }

    @Then("^I search for text$")
    public void i_search_for() throws Throwable {
        Log.debug("* Step started i_search_for");
        Log.info("Going to search for");
        WebElement element = ctx.driver.findElement(By.id("lst-ib"));

        HashMap<String, Object> testDataMap = ctx.obj.get("TestData",HashMap.class);
        String sVal = (String) testDataMap.get("search_sentence");

        Log.debug("Entering text " + sVal);
        element.sendKeys(sVal);
        element.submit();
    }

    @Then("^I search for text (.*)$")
    public void i_search_for2(String input) throws Throwable {
        Log.debug("* Step started i_search_for2");
        Log.info("Going to search for");
        WebElement element = ctx.driver.findElement(By.id("lst-ib"));

        Long lVal = ctx.step.checkIfInputIsVariable(input);

        String sVal = lVal.toString();

        Log.debug("Show me sVal " + sVal);

        element.sendKeys(sVal);
        element.submit();
    }

    @Then("^attach sample file to report$")
    public void attache_file_to_report() throws Throwable {
        Log.debug("* Step attache_file_to_report");
        Log.info("Going to attach file");
        ctx.step.attachFileToReport("SimpleTextAttachment", "text/plain", "C:\\Users\\akowa\\Documents\\przykladowy_plik_tekstowt.txt");
        ctx.step.attachFileToReport("PdfAttachment", "application/pdf", "C:\\Users\\akowa\\Documents\\API_design.pdf");
        ctx.step.attachMessageToReport("Some name", "Some random message");
    }


}