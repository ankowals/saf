package modules.pages.DemoOnlineStore;

import modules.core.BasePage;
import modules.core.Log;
import modules.core.SharedContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class CheckoutPage extends BasePage {

    public CheckoutPage(SharedContext ctx) {
        super(ctx);
        if(! isLoaded("Checkout | ONLINE STORE")){
            Log.error("Checkout Page not loaded!");
        }
    }

    public Boolean isLoaded(String pageTitle){
        return PageCore.titleContains(pageTitle);
    }

    //selectors
    private static final By TotalPriceElementSelector = By.xpath("//*[@class='yourtotal']/descendant::span[@class='pricedisplay']");
    private static final By ProductTotalPriceElementSelector = By.xpath("//*[contains(@class, 'wpsc_product_price')]/span[@class='pricedisplay']");
    private static final By ProductNameElementSelector = By.xpath("//*[contains(@class, 'wpsc_product_name')]/a");

    /**
     * Retrieves total price of all products in the cart without currency sign
     *
     * @return      String, price
     *
     */
    public String getTotalPrice() {
        Log.debug("Extracting total price of all products in the cart");
        WebElement totalPrice = PageCore.findElement(TotalPriceElementSelector);

        return totalPrice.getText().replace("$","");
    }

    /**
     * Retrieves total price of products of each type in the cart
     *
     * @return     price per product type as an ArrayList
     *
     */
    public ArrayList<String> getTotalPricePerProduct() {
        Log.debug("Extracting total price per product in the cart");
        ArrayList result = new ArrayList();
        List<WebElement> priceOfProducts = PageCore.findElements(ProductTotalPriceElementSelector);
        List<WebElement> nameOfProducts = PageCore.findElements(ProductNameElementSelector);

        Log.debug("Found " + priceOfProducts.size() + " types of products in the cart");
        for (int i = 0; i < priceOfProducts.size(); i++) {
            Log.debug("[" + i + "] Total price of product type " + nameOfProducts.get(i).getText() + " is " + priceOfProducts.get(i).getText());
            result.add(priceOfProducts.get(i).getText().replace("$",""));
        }

        return result;
    }



}
