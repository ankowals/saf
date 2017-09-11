package steps.core;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;
import cucumber.api.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"ru.yandex.qatools.allure.cucumberjvm.AllureReporter"},
        features = "src/test/resources/features",
        glue = "steps")
public class TestRunner {}