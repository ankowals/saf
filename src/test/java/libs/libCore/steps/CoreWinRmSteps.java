package libs.libCore.steps;

import cucumber.api.java.en.Given;
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;
import libs.libCore.modules.SharedContext;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;

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
        WinRmToolResponse result = WinRMCore.executePs(cmd, 3);

        Log.debug("Result is " + result.getStdOut());
        Log.debug("Exit code is " + result.getStatusCode());
        Log.debug("Error is " + result.getStdErr());
        WinRMCore.closeClient();

        String output = result.getStdOut().replaceAll("(\\r|\\n)", "");
        output = output.trim();
        Log.debug("Result is " + output);

        if ( ! output.equalsIgnoreCase(cmd.substring(11)) ) {
            Log.error("host " + hostName + " is not accessible");
        }
    }

    @Given("^execute via WinRS on node (.+)$")
    public void execute_via_WinRS_on_node(String node) throws Throwable {
        Log.info("* Step started execute_via_WinRS_on_node");

        File workingDir = FileCore.createTempDir();

        String address = Storage.get("Environment.Active.WinRM." + node + ".host");
        Integer port = Storage.get("Environment.Active.WinRM." + node + ".port");
        String user = Storage.get("Environment.Active.WinRM." + node + ".user");
        String passwd = Storage.get("Environment.Active.WinRM." + node + ".password");

        String cmd = "'Hostname'";
        String invocation = "winrs -r:http://" + address + ":" + port + " -u:" + user + " -p:" + passwd;
        cmd =  invocation + " " + cmd;

        ByteArrayOutputStream out = ExecutorCore.execute(cmd, workingDir, 20, true);
        Log.debug("Output is ");
        Log.debug(new String(out.toByteArray(), Charset.defaultCharset()));

    }

}
