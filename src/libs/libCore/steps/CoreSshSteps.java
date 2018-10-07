package libs.libCore.steps;

import cucumber.api.java.en.Given;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;
import libs.libCore.modules.SSHResult;

public class CoreSshSteps extends BaseSteps {

    /**
     * Tests if a host is alive
     *
     * @param hostName, String identifier of the host
     */
    @Given("^host (.+) is alive$")
    public void host_is_alive(String hostName) {

        String cmd = "echo " + hostName + " is alive";

        Log.debug("Create new ssh session");
        SshCore.createClient(hostName);
        Log.debug("Command to execute via ssh is " + cmd);
        SSHResult result = SshCore.execute(cmd, 10);
        Log.debug("Result is " + result.getStdout());
        Log.debug("Exit code is " + result.getExitCode());
        SshCore.closeClient();

        String output = result.getStdout().replaceAll("(\\r|\\n)", "");
        output = output.trim();
        Log.debug("Result is " + output);

        if ( ! output.equalsIgnoreCase(cmd.substring(5)) ) {
            Log.error("host " + hostName + " is not accessible");
        }
    }


}
