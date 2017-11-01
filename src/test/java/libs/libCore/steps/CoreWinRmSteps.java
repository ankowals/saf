package libs.libCore.steps;

import cucumber.api.java.en.Given;
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;
import libs.libCore.modules.SharedContext;

public class CoreWinRmSteps extends BaseSteps {

    // PicoContainer injects class SharedContext
    public CoreWinRmSteps(SharedContext ctx) {
        super(ctx);
    }

    /**
     * Tests if a host is alive
     * @param hostName
     */
    @Given("^windows host (.+) is alive$")
    public void windows_host_is_alive(String hostName) throws Throwable {
        Log.info("* Step started windows_host_is_alive");

        String cmd = "Write-Host " + hostName + " is alive";

        Log.debug("Create new winRM session");
        WinRMCore.createClient(hostName);
        Log.debug("Command to execute via winRM is " + cmd);
        WinRmToolResponse result = WinRMCore.executePs(cmd, 10);

        Log.debug("Result is " + result.getStdOut());
        Log.debug("Exit code is " + result.getStatusCode());
        Log.debug("Error is " + result.getStdErr());
        WinRMCore.closeClient();

        String output = result.getStdOut().replaceAll("(\\r|\\n)", "");
        output = output.trim();
        Log.debug("Result is " + output);

        if ( ! output.equalsIgnoreCase(cmd.substring(5)) ) {
            Log.error("host " + hostName + " is not accessible");
        }
    }


}
