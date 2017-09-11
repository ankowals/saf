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
        String url = ctx.env.readProperty("WEB_url");
        ctx.driver.get(url);

        return new MainPage(ctx);
    }

    public Boolean isLoaded(String pageTitle){
        return titleContains(pageTitle);
    }

    public ProductPage goToAllProduct(){
        Log.info("Click 'All Products' button");
        WebElement allProductButton = ctx.driver.findElement(allProductsSelector);
        allProductButton.click();

        waitForPageLoadAndTitleContains("Product Category | ONLINE STORE");

        return new ProductPage(ctx);
    }

}
