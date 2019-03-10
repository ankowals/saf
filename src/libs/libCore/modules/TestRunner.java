package libs.libCore.modules;

import cucumber.api.CucumberOptions;
import cucumber.api.testng.AbstractTestNGCucumberTests;
import org.testng.annotations.DataProvider;

public class TestRunner extends AbstractTestNGCucumberTests {
    @CucumberOptions(
            plugin = {"io.qameta.allure.cucumber4jvm.AllureCucumber4Jvm", "libs.libCore.modules.CustomEventListener"},
            features = "src/features",
            glue = "libs")
    public class RunCukesTest extends AbstractTestNGCucumberTests {
        @Override
        @DataProvider(parallel = true)
        public Object[][] scenarios() {
            return super.scenarios();
        }
    }
}
