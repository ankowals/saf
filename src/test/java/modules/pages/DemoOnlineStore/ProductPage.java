package modules.pages.DemoOnlineStore;

import modules.core.BasePage;
import modules.core.Log;
import modules.core.SharedContext;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.util.List;

public class ProductPage extends BasePage {

    public ProductPage(SharedContext ctx) {
        super(ctx);
        if(! isLoaded("Product Category | ONLINE STORE")){
            Log.fatal("Product Page not loaded!");
        }
    }

    public Boolean isLoaded(String pageTitle){
        return titleContains(pageTitle);
    }

    //selectors
    private static final By addInputSelector = By.xpath("//*[contains(@class, 'product_view_')]/descendant::input[contains(@class,'wpsc_buy_button')]");
    private static final By productElementSelector = By.xpath("//*[contains(@class, 'product_view_')]");
    private static final By fancyPopUpSelector = By.xpath("//*[@id='fancy_notification']");
    private static final By continueShoppingButtonSelector = By.xpath("//*[@id='fancy_notification_content']/a[2]");
    private static final By productTitleSelector = By.xpath("//*[contains(@class, 'product_view_')]/descendant::a[@class='wpsc_product_title']");
    private static final By currentPriceSelector = By.xpath("//*[contains(@class, 'product_view_')]/descendant::*[contains(@class,'currentprice')]");
    private static final By goToCheckoutButtonSelector = By.xpath("//*[@id='fancy_notification_content']/a[1]");
    private static final By popUpOverlayElementSelector = By.xpath("//div[@class='popup']");

    /**
     * helper function
     * Search for a particular product on the page and adds it to the cart
     *
     * @param       name name of the product that shall be added to the cart
     *
     */
    private void handleProduct(String name){
        Log.debug("Looking for products on page");
        List<WebElement> displayedProdcuts = ctx.driver.findElements(productElementSelector);

        Log.debug("Found " + displayedProdcuts.size() + " products on page");
        List<WebElement> productTitles = ctx.driver.findElements(productTitleSelector);
        List<WebElement> currentPrices = ctx.driver.findElements(currentPriceSelector);
        Integer index = null;
        for (int i = 0; i < displayedProdcuts.size(); i++) {
            String displayedName = productTitles.get(i).getText().trim();
            Log.debug("[" + i + "] Product is " + displayedName + " and price is " + currentPrices.get(i).getText() );
            if(StringUtils.equalsIgnoreCase(name, displayedName)){
                index = i;
            }
        }

        if(index==null){
            Log.fatal("Product with such name " + name + " is not available");
        }

        Log.debug("Add product " + productTitles.get(index).getText() + " to the cart");
        List<WebElement> addInputs = ctx.driver.findElements(addInputSelector);
        addInputs.get(index).click();

        waitForElementToBeVisible(fancyPopUpSelector);
    }

    /**
     * Adds product to the cart and clicks "ContinueShopping button"
     *
     * @param       name name of the product that shall be added to the cart
     *
     */
    public void addToCart(String name){

        handleProduct(name);

        //click continue
        WebElement continueShoppingButton = ctx.driver.findElement(continueShoppingButtonSelector);
        continueShoppingButton.click();
        waitForElementToBeRemoved(popUpOverlayElementSelector);
    }

    /**
     * Adds product to the cart and goes to Checkout Page
     *
     * @param       name name of the product that shall be added to the cart
     *
     * @return      CheckoutPage
     */
    public CheckoutPage addToCartAndCheckout(String name){

        handleProduct(name);

        //click checkout
        WebElement goToCheckoutButton = ctx.driver.findElement(goToCheckoutButtonSelector);
        goToCheckoutButton.click();
        jsWaitForPageToLoad();

        return new CheckoutPage(ctx);
    }

}
