package steps.DemoOnlineStore;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import modules.core.BaseSteps;
import modules.core.Log;
import modules.core.SharedContext;
import modules.pages.DemoOnlineStore.CheckoutPage;
import modules.pages.DemoOnlineStore.MainPage;
import modules.pages.DemoOnlineStore.ProductPage;
import org.junit.Assert;

import java.util.ArrayList;

public class DemoOnlieSteps extends BaseSteps {

    // PicoContainer injects class SharedContext
    public DemoOnlieSteps (SharedContext ctx) {
        super(ctx);
    }

    //create global variables for this class
    MainPage main;
    ProductPage product;
    CheckoutPage checkout;

    /**
     * Opens web page with url taken from environment configuration
     *
     * Uses following objects:
     * env.WEB_url
     *
     */
    @When("^open main page$")
    public void i_open_main_page() throws Throwable {
        Log.info("* StepCore started i_open_main_page");
        //instantiate MainPage to open url in the browser
        main = new MainPage(ctx);
        main.load();
    }

    /**
     * Navigates to all products page
     */
    @And("^navigate to all products page$")
    public void navigate_to_all_products() throws Throwable{
        Log.info("* StepCore started navigate_to_all_products");
        product = main.goToAllProduct();
    }

    /**
     * Adds product {} to the cart.
     *
     * @param productName  name or value from storage
     *
     */
    @And("^add product (.*) to cart$")
    public void add_product_to_cart(String productName) throws Throwable{
        Log.info("* StepCore started add_product_to_cart");

        String input = StepCore.checkIfInputIsVariable(productName);
        product.addToCart(input);
    }

    /**
     * Adds product {} to the cart and navigates to checkout page.
     *
     * @param productName  name or value from storage
     *
     */
    @And("^add product (.*) to cart and go to checkout$")
    public void add_product_to_cart_and_checkout(String productName) throws Throwable{
        Log.info("* StepCore started add_product_to_cart_and_checkout");

        String input = StepCore.checkIfInputIsVariable(productName);
        checkout = product.addToCartAndCheckout(input);
    }


    /**
     * Verifies that SubTotal field equals sub of total price per product type
     * on Checkout page.
     *
     * Attaches screenshot to the report
     */
    @Then("^verify that SubTotal value equals sum of totals per product type$")
    public void verify_sum_of_totals_per_product_type_equals_subTotal() throws Throwable{
        Log.info("* StepCore started verify_sum_of_totals_per_product_type_equals_subTotal");

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
        if ( ! Double.valueOf(totalPrice).equals(sum) ) {
            Log.error("Sub-Total value is different than sum of price per product type");
        }
    }
}
