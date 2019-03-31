package libs.libDemoUI.modules.AppPOM;

import libs.libCore.modules.BaseApp;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

public class MainPage extends BaseApp {

    //selectors
    private static final By textInput = By.className("Edit");

    public String readContent(){
        WiniumCore.awaitForAnElement(textInput, 5, 1, false);
        WebElement editWindow = WiniumCore.findElement(textInput);
        return editWindow.getText();
    }

    public void verifyThatContentIs(String current, String expected){
        Assert.assertEquals(current, expected);
    }

}