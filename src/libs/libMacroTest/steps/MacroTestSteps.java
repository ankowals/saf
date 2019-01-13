package libs.libMacroTest.steps;

import cucumber.api.java.en.Given;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;

import java.io.File;

public class MacroTestSteps extends BaseSteps {

    @Given("evaluate template (.+)")
    public void evaluate_template(String templateName){
        File file = StepCore.evaluateTemplate(templateName);
        String content = FileCore.readToString(file);
        Log.debug("Content of the template is");
        Log.debug(content);
    }

}
