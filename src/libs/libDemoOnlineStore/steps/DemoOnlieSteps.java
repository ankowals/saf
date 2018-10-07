package libs.libDemoOnlineStore.steps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;
import libs.libDemoOnlineStore.modules.CheckoutPage;
import libs.libDemoOnlineStore.modules.MainPage;
import libs.libDemoOnlineStore.modules.ProductPage;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class DemoOnlieSteps extends BaseSteps {

    //create global variables for this class
    MainPage main;
    ProductPage product;
    CheckoutPage checkout;

    /**
     * Opens web page with url taken from environment configuration
     */
    @When("^open main page$")
    public void i_open_main_page() {
        main = new MainPage();
    }

    /**
     * Navigates to all products page
     */
    @And("^navigate to all products page$")
    public void navigate_to_all_products(){
        product = main.goToAllProduct();
    }

    /**
     * Adds product {} to the cart.
     *
     * @param productName  name or value from storage
     *
     */
    @And("^add product (.*) to cart$")
    public void add_product_to_cart(String productName){
        String input = StepCore.checkIfInputIsVariable(productName);
        product.addToCart(input);
    }

    /**
     * navigates to Checkout page
     */
    @And("^navigate to checkout page$")
    public void navigate_to_checkout_page(){
        checkout = product.goToCheckout();
    }


    /**
     * Verifies that SubTotal field equals sub of total price per product type
     * on Checkout page.
     *
     * Attaches screenshot to the report
     */
    @Then("^verify that SubTotal value equals sum of totals per product type$")
    public void verify_sum_of_totals_per_product_type_equals_subTotal(){
        String totalPrice = checkout.getTotalPrice();
        ArrayList<String> totalPerProductType = checkout.getTotalPricePerProduct();

        Double sum = 0d;
        for(String price : totalPerProductType){
            sum = sum + Double.valueOf(price);
        }

        byte[] screenshot = PageCore.takeScreenshot();
        StepCore.attachScreenshotToReport("Checkout_Products_Price_View", screenshot);

        Log.debug("Sum per product type is " + sum);
        Log.debug("Sub-Total is " + totalPrice);

        try {
            assertEquals("Sub-Total value is different than sum of price per product type",
                    Double.valueOf(totalPrice),
                    sum);
        } catch ( AssertionError e ) {
            Log.error("", e);
        }

        if ( ! Double.valueOf(totalPrice).equals(sum) ) {
            Log.error("Sub-Total value is different than sum of price per product type");
        }
    }
}