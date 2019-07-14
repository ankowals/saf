package libs.libDemoUI.modules.WebPOM;

import libs.libCore.modules.BasePage;
import libs.libCore.modules.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ResultsPage extends BasePage {

    //selectors
    private static final By productListElements = By.xpath("//li[contains(@class, 'ajax_block_product')]");

    public ResultsPage() {
        if(! isLoaded("Search - My Store")){
            Log.error("Results Page not loaded!");
        }
    }

    public int getNumberOfResults(){
        Log.debug("Checking how many products is displayed on the search results page");

        List<WebElement> products = PageCore.findElements(productListElements);

        if ( products != null) {
            return products.size();
        }

        return 0;
    }

}
