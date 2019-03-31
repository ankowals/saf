package libs.libDemoUI.steps;

import cucumber.api.java.en.When;
import libs.libCore.modules.BaseSteps;
import libs.libDemoUI.modules.AppPOM.MainPage;

public class UIDemoAppSteps extends BaseSteps {

    @When("^in notepad, verify that page title is (.+)$")
    public void in_notepad_write_page_title(String expectedTitle) {

        //check if input coming from a config
        String expTitle = StepCore.checkIfInputIsVariable(expectedTitle);

        //write text to the notepad
        MainPage main = new MainPage();

        StepCore.sleep(2);

        //attach screenshot to the report
        byte[] screenshot = WiniumCore.takeScreenshot();
        StepCore.attachScreenshotToReport("AppPage", screenshot);

        String currentTitle = main.readContent();
        main.verifyThatContentIs(currentTitle.trim(), expTitle);

    }

}