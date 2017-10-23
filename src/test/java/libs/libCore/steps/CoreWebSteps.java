package libs.libCore.steps;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;
import libs.libCore.modules.SharedContext;

public class CoreWebSteps extends BaseSteps {

    // PicoContainer injects class SharedContext
    public CoreWebSteps(SharedContext ctx) {
        super(ctx);
    }

    /**
     * Opens provided url in the browser
     *
     * @param input, String, url that shall be open in the browser
     *
     */
    @When("^navigate to url (.*?)$")
    public void navigate_to_url(String input) throws Throwable {
        Log.info("* Step started navigate_to_url");

        String url = StepCore.checkIfInputIsVariable(input);

        PageCore.open(url);
        String current = PageCore.getCurrentUrl();

        if ( ! current.equals(url) ) {
            Log.error("Current url " + current + " does not equal to expected url " + url);
        }
    }

    /**
     * Opens url in the browser. Url is taken from configuration.
     *
     * Uses following objects:
     * env.WEB_url
     *
     */
    @When("^navigate to base page$")
    public void navigate_to_base_page() throws Throwable {
        Log.info("* Step started navigate_to_base_page");

        String url = Storage.get("Environment.Active.Web.url");

        PageCore.open(url);
        String current = PageCore.getCurrentUrl();

        if ( ! current.equals(url) ) {
            Log.error("Current url " + current + " does not equal to expected url " + url);
        }
    }


    /**
     * Verifies page title
     *
     * @param input, String, expected title
     *
     */
    @Then("^verify that title is (.*?)$")
    public void verify_that_title_is(String input) throws Throwable {
        Log.info("* Step started verify_that_title_is");

        String expectedTitle = StepCore.checkIfInputIsVariable(input);

        if ( ! PageCore.getTitle().equals(expectedTitle) ) {
            Log.error("Page title verification failed! Expected is " + expectedTitle +
                    " but was " + PageCore.getTitle() );
        }
    }

}
