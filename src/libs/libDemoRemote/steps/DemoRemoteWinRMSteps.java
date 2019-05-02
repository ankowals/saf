package libs.libDemoRemote.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;
import org.testng.Assert;

import java.io.File;

public class DemoRemoteWinRMSteps extends BaseSteps {

    @Given("^remote host (.+) is accessible via winRM$")
    public void remote_host_is_accessible_via_winRM(String node) {
        String address = Storage.get("Environment.Active.WinRM." + node + ".host");

        if ( address == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }

        WinRSCore.awaitForHostAvailability(node);
    }

    @When("^on remote host (.+) get service (.+) status$")
    public void on_remote_host_get_service_status(String node, String service){
        String address = Storage.get("Environment.Active.WinRM." + node + ".host");

        if ( address == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }

        String serviceName = StepCore.checkIfInputIsVariable(service);

        String status = WinRSCore.getServiceStatus(node, serviceName, 60);
        scenarioCtx.put("Service_" + serviceName, String.class, status);

    }

    @Then("^verify that service (.+) status is (.+)$")
    public void verify_that_service_status_is(String service, String expectedStatus){
        String serviceName = StepCore.checkIfInputIsVariable(service);
        String expected = StepCore.checkIfInputIsVariable(expectedStatus);
        String status = scenarioCtx.get("Service_" + serviceName, String.class);

        if (status == null){
            Log.error("Service " + serviceName + " status not known! Please make sure that step " +
                    "'on remote host (.+) get service (.+) status' has been executed");
        }

        Assert.assertEquals(status, expected);
    }

}