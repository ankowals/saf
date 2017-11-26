package libs.libCore.modules;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;
import cucumber.api.CucumberOptions;

@RunWith(CucumberWithLogger.class)
@CucumberOptions(
        plugin = {"ru.yandex.qatools.allure.cucumberjvm.AllureReporter"},
        features = "src/test/java/features",
        //format = "libs.libCore.modules.JunitPrintingFormatter",
        glue = "libs")

public class TestRunner {}