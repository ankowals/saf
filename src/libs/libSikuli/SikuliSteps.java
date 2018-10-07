package libs.libSikuli;

import cucumber.api.java.en.When;
import libs.libCore.modules.BaseSteps;
import org.sikuli.script.Screen;

public class SikuliSteps extends BaseSteps {

    @When("^test sikuli$")
    public void test_sikuli() throws Throwable{
        Screen screen = new Screen();
        screen.click("C:\\Users\\akowa\\Documents\\Projects\\FK_Prototype\\src\\test\\java\\resources\\sikuli\\WinSCP_Icon.png");
    }


}