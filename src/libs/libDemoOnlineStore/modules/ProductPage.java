package libs.libDemoOnlineStore.modules;

import libs.libCore.modules.BasePage;
import libs.libCore.modules.Log;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ProductPage extends BasePage {

    public ProductPage() {
        if(! isLoaded("Product Category | ONLINE STORE")){
            Log.error("Product Page not loaded!");
        }
    }

    //selectors
    private static final By addInputSelector = By.xpath("//*[contains(@class, 'product_view_')]/descendant::input[contains(@class,'wpsc_buy_button')]");
    private static final By productElementSelector = By.xpath("//*[contains(@class, 'product_view_')]");
    private static final By fancyPopUpSelector = By.xpath("//*[contains(@class, 'addtocart') and contains(@style,'display: block')]");
    private static final By notificationSelector = By.xpath("//*[contains(@class, 'addtocart') and contains(@style,'display: block')]/p");
    private static final By productTitleSelector = By.xpath("//*[contains(@class, 'product_view_')]/descendant::a[@class='wpsc_product_title']");
    private static final By currentPriceSelector = By.xpath("//*[contains(@class, 'product_view_')]/descendant::*[contains(@class,'currentprice')]");
    private static final By CheckoutButtonSelector = By.xpath("//*[@id='header_cart']/a");


    /**
     * Adds product to the cart
     *
     * @param       name name of the product that shall be added to the cart
     *
     */
    public void addToCart(String name){

        Log.debug("Looking for products on page");
        List<WebElement> displayedProdcuts = PageCore.findElements(productElementSelector);

        Log.debug("Found " + displayedProdcuts.size() + " products on page");
        List<WebElement> productTitles = PageCore.findElements(productTitleSelector);
        List<WebElement> currentPrices = PageCore.findElements(currentPriceSelector);
        Integer index = null;
        for (int i = 0; i < displayedProdcuts.size(); i++) {
            String displayedName = productTitles.get(i).getText().trim();
            Log.debug("[" + i + "] Product is " + displayedName + " and price is " + currentPrices.get(i).getText() );
            if(StringUtils.equalsIgnoreCase(name, displayedName)){
                index = i;
            }
        }

        if( index == null ){
            Log.error("Product with such name " + name + " is not available");
        }

        Log.debug("Add product " + productTitles.get(index).getText() + " to the cart");
        List<WebElement> addInputs = PageCore.findElements(addInputSelector);
        addInputs.get(index).click();

        PageCore.waitForElementToBeVisible(fancyPopUpSelector);
        WebElement notification = PageCore.findElement(notificationSelector);

        if ( ! notification.getText().equals("Item has been added to your cart!") ) {
            Log.error("Failure! Notification 'Item has been added to your cart!' " +
                    " not found after AddToCart button click");
        }

        PageCore.waitForElementToBeRemoved(fancyPopUpSelector);
    }

    /**
     * Clicks on Checkout button and goes to check out page
     *
     *
     * @return      CheckoutPage
     */
    public CheckoutPage goToCheckout(){


        //click checkout
        WebElement CheckoutButton = PageCore.findElement(CheckoutButtonSelector);
        CheckoutButton.click();
        PageCore.waitForPageToLoad();

        return new CheckoutPage();
    }

}
