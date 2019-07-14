package libs.libDemoUI.modules.WebPOM;

import libs.libCore.modules.BasePage;
import libs.libCore.modules.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class MainPage extends BasePage {

    public MainPage() {
        load();
    }

    //selectors
    private static final By dressesButtonSelector = By.xpath("(//*[@class='sf-with-ul' and @title='Dresses'])[last()]");
    private static final By searchInput = By.xpath("//*[@id='search_query_top']");
    private static final By searchButton = By.xpath("//button[@name='submit_search']");

    private void load(){
        String url = Storage.get("Environment.Active.Web.url");
        PageCore.open(url);
        PageCore.waitForPageToLoad();
        if(! isLoaded("My Store")){
            Log.error("Main page not loaded!");
        }
    }

    /**
     * Navigates to dresses page
     *
     * @return      ProductPage
     *
     */
    public ProductPage goToDresses(){
        Log.debug("Click 'Dresses' button");
        WebElement dressesButton = PageCore.findElement(dressesButtonSelector);
        dressesButton.click();

        //await for page
        PageCore.waitUntilTitleContains("Dresses - My Store");

        return new ProductPage();
    }

    public ResultsPage searchForProduct(String product){
        Log.debug("Searching for " + product);

        WebElement searchIn = PageCore.findElement(searchInput);
        searchIn.clear();
        searchIn.sendKeys(product);

        WebElement searchBtn = PageCore.findElement(searchButton);
        searchBtn.click();

        //await for page
        PageCore.waitUntilTitleContains("Search - My Store");

        return new ResultsPage();


    }

}