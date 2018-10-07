package libs.libCore.modules;

import cucumber.api.junit.Cucumber;

import org.junit.runner.RunWith;
import cucumber.api.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"io.qameta.allure.cucumber3jvm.AllureCucumber3Jvm", "libs.libCore.modules.CustomFormatter"},
        features = "src/features",
        glue = "libs")

public class TestRunner {}