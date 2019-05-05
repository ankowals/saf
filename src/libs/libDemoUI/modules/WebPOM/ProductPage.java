package libs.libDemoUI.modules.WebPOM;

import libs.libCore.modules.BasePage;
import libs.libCore.modules.Log;

public class ProductPage extends BasePage {

    public ProductPage() {
        if(! isLoaded("Dresses - My Store")){
            Log.error("Product Page not loaded!");
        }
    }

    /**
     * Extracts page title
     *
     * @return      String
     */
    public String extractPageTitle(){
        return PageCore.getTitle();
    }
}