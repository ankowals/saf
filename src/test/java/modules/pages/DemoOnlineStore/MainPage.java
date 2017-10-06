package modules.pages.DemoOnlineStore;

import modules.core.BasePage;
import modules.core.Log;
import modules.core.SharedContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class MainPage extends BasePage {

    public MainPage(SharedContext ctx) {
        super(ctx);
        if(! isLoaded("ONLINE STORE | Toolsqa Dummy Test site")){
            load();
        }
    }

    //selectors
    private static final By allProductsSelector = By.xpath("(//*[@id='main-nav']/ul/li)[last()]");

    public MainPage load(){
        String url = Environment.readProperty("WEB_url");
        PageCore.open(url);

        return new MainPage(ctx);
    }

    /**
     * Navigates to all products page
     *
     * @return      ProductPage
     *
     */
    public ProductPage goToAllProduct(){
        Log.debug("Click 'All Products' button");
        WebElement allProductButton = PageCore.findElement(allProductsSelector);
        allProductButton.click();

        PageCore.waitUntilTitleContains("Product Category | ONLINE STORE");

        return new ProductPage(ctx);
    }

}
