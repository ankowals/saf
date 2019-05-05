package libs.libDemoRemote.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.ExecResult;
import libs.libCore.modules.Log;

import java.io.File;

public class DemoRemoteSteps extends BaseSteps {

    @Given("^host (.+) is alive$")
    public void host_is_alive(String host) {
        String cmd = "echo " + host + " is alive";
        Log.debug("Command to execute is " + cmd);
        ExecResult result = SshCore.execute(host, cmd, 10);
        Log.debug("Result is " + result.getStdOut().trim());
        Log.debug("Exit code is " + result.getExitCode());
        String output = result.getStdOut().trim();
        if ( ! output.equalsIgnoreCase(cmd.substring(5)) ) {
            Log.error("host " + host + " is not accessible");
        }
    }

    @When("^list files in users home directory$")
    public void list_files_in_users_home_directory(){
        String cmd = "ls";
        Log.debug("Command to execute is " + cmd);
        ExecResult result = SshCore.execute("node1", cmd, 10);
        Log.debug("Result is " + result.getStdOut());
        Log.debug("Exit code is " + result.getExitCode());
    }


    @When("^switch user to root$")
    public void switch_user_to_root(){
        String userChangeCmd = "su - root";
        String passOfUserCmd = "vagrant";
        String validateCmd = "whoami";

        String node = "node1";
        SshCore.startShell(node, 40);
        SshCore.executeInShell(node, "", "\\$");
        SshCore.executeInShell(node, userChangeCmd, "Password");
        SshCore.executeInShell(node, passOfUserCmd, "root@");
        SshCore.executeInShell(node, validateCmd, "root");
        SshCore.stopShell(node);
    }

    @When("^execute sample command in an interactive shell$")
    public void check_command_exit_status_code_when_in_shell(){
        String node = "node1";
        SshCore.startShell(node, 10);
        SshCore.executeInShell(node,"", "\\$");
        SshCore.executeInShell(node, "test -e postinstall.sh;echo $?", "0");
        SshCore.stopShell(node);
    }

    @When("^download file from remote host$")
    public void download_file_from_remote_host(){
        String node = "node1";
        boolean isAlive = SshCore.checkThatNodeIsAlive(node);
        if ( ! isAlive ) {
            Log.error("Host node1 is not available");
        }

        Log.debug("Checking that file is present on the remote host");
        String pathToFile = "postinstall.sh";
        boolean isAvailable = SshCore.checkThatFileExists(node, pathToFile);
        if ( ! isAvailable ){
            Log.error("File " + pathToFile + " not found!");
        }

        Log.debug("Download file via scp");

        File file = SshCore.downloadFileViaScp(node,pathToFile,FileCore.getTempDir().getAbsolutePath());
        Log.debug("Path to file is " + file.getAbsolutePath());
        StepCore.attachFileToReport("postinstall.sh", "text/plain", file.getAbsolutePath());
    }

}