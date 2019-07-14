package libs.libDemoUI.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;
import libs.libDemoUI.modules.WebPOM.MainPage;
import libs.libDemoUI.modules.WebPOM.ProductPage;
import libs.libDemoUI.modules.WebPOM.ResultsPage;

public class UIDemoWebSteps extends BaseSteps {

    //create global variables for this class
    MainPage main;
    ProductPage product;
    ResultsPage results;

    /**
     * navigates to demo page in the web browser
     */
    @Given("^in the browser, navigate to a demo shop web page$")
    public void in_the_browser_navigate_to_a_demo_shop_web_page() {
        //load main page in the web browser
        main = new MainPage();
        //navigate to dresses sub page
        product = main.goToDresses();
    }

    /**
     * extracts page title and takes screenshot of a page
     */
    @Given("^in the browser, extract page title$")
    public void in_the_browser_extract_page_title() {

        //attach screenshot to the report
        byte[] screenshot = PageCore.takeScreenshot();
        StepCore.attachScreenshotToReport("ProductPage", screenshot);

        String title = product.extractPageTitle();

        Log.debug("Page title is " + title);

        //share page title with other steps
        scenarioCtx.put("pageTitle", String.class, title);
    }


    @When("in the browser, search for product with name {testdata}")
    public void in_the_browser_search_for_product_with_name(String name) {
        main = new MainPage();
        results = main.searchForProduct(name);
    }

    @Then("in the browser, verify that {testdata} results were returned")
    public void in_the_brwoser_verify_that_results_were_returned(Integer expectedResults) {
        int actualResutls = results.getNumberOfResults();

        if ( actualResutls != expectedResults ){
            Log.error("Expected to find " + expectedResults + " but found " + actualResutls + "!");
        }
    }


}