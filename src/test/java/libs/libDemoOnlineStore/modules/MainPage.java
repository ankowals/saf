package libs.libDemoOnlineStore.modules;

import libs.libCore.modules.BasePage;
import libs.libCore.modules.Log;
import libs.libCore.modules.SharedContext;
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
        String url = Storage.get("Environment.Active.Web.url");
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
