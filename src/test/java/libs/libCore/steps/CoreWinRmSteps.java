package libs.libCore.steps;

import com.google.common.base.Joiner;
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
     *
     * @param hostName String, remote node identifier
     */
    @Given("^windows host (.+) is alive$")
    public void windows_host_is_alive(String hostName) throws Throwable {
        Log.info("* Step started windows_host_is_alive");

        String cmd = "Write-Host " + hostName + " is alive";

        Log.debug("Create new winRM session");
        WinRMCore.createClient(hostName);
        //Log.debug("Command to execute via winRM is " + cmd);
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

    /**
     * Execute command on a remote host via winRS on a remote host
     *
     * @param node String, remote node identifier
     * @throws Throwable
     */
    @Given("^execute via WinRS on node (.+)$")
    public void execute_via_WinRS_on_node(String node) throws Throwable {
        Log.info("* Step started execute_via_WinRS_on_node");

        File workingDir = FileCore.createTempDir();

        String address = Storage.get("Environment.Active.WinRM." + node + ".host");
        Integer port = Storage.get("Environment.Active.WinRM." + node + ".port");
        String user = Storage.get("Environment.Active.WinRM." + node + ".user");
        String passwd = Storage.get("Environment.Active.WinRM." + node + ".password");

        //String cmd = "'Hostname'";
        //C:\Users\akowa>winrs -r:http://127.0.0.1:55985 -u:vagrant -p:vagrant cmd.exe /c "echo 'Test' > test.bat"
        //net use z: \\localhost\c$\Users\vagrant\Music /persistent:no
        //copy z:\toJestPlikTekstowy.txt C:\Users\vagrant\Documents
        String cmd = "'net use z: \\\\localhost\\c$\\Users\\vagrant\\Music /persistent:no'";
        String invocation = "winrs -r:http://" + address + ":" + port + " -u:" + user + " -p:" + passwd;
        cmd =  invocation + " " + cmd;

        ByteArrayOutputStream out = ExecutorCore.execute(cmd, workingDir, 20, true);
        Log.debug("Output is ");
        Log.debug(new String(out.toByteArray(), Charset.defaultCharset()));

    }

    /**
     * Mounts shared drive on a remote host via winRS
     *
     * @param path String, unc path to the shared location
     * @param drive String, drive letter
     * @param host String, remote host identifier
     *
     * @throws Throwable
     */
    @Given("^mount path (.+) as network drive (.+) via WinRS on remote node (.+)$")
    public void mount_path_as_network_drive_via_WinRS_on_remote_node(String path, String drive, String host) throws Throwable {
        Log.info("* Step started mount_path_as_network_drive_via_WinRS_on_remote_node");

        String bs = "\\u005C";
        String dq = "\\u0022";

        path = "\\\\localhost\\c$\\Users\\vagrant\\Music";

        //String cmd = "New-PSDrive –Name " + "\"" + drive + "\"" + " –PSProvider FileSystem –Root " + "\"" + path + "\"";

        //String cmd = "net use z: " + path + " /persistent:no";

        //String cmd = "net use x: " + path + " /persistent:no";
        //String cmd2 = "type toJestPlikTekstowy.txt";
        //String cmd2 = "copy x:\\toJestPlikTekstowy.txt C:\\Users\\vagrant\\Documents";
        //List<String> cmdList = new ArrayList<String>();
        //cmdList.add(cmd);
        //cmdList.add(cmd2);

        String cmd = Joiner.on("\n").join(
                "net use z: " + path + " /persistent:no",
                "copy z:\\toJestPlikTekstowy.txt C:\\Users\\vagrant\\Documents");


        //Log.debug("Create new winRM session");
        WinRMCore.createClient(host);
        //Log.debug("Command to execute via winRM is " + cmd);

        //WinRmToolResponse result = WinRMCore.executeCommand(cmdList, 3);


        //WinRmToolResponse result = WinRMCore.executePs(cmd, 3);

        WinRmToolResponse result = WinRMCore.executeBatchScriptFromString(cmd, "mount.bat","");
        //WinRmToolResponse result = WinRMCore.executePsScriptFromString(cmd, "mount.ps1","");

        Log.debug("Result is " + result.getStdOut());
        Log.debug("Exit code is " + result.getStatusCode());
        Log.debug("Error is " + result.getStdErr());

        WinRMCore.closeClient();

        String output = result.getStdOut().replaceAll("(\\r|\\n)", "");
        output = output.trim();
        Log.debug("Result is " + output);

    }

}
