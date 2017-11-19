package libs.libRemoteExecution.steps;

import cucumber.api.java.en.When;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;
import libs.libCore.modules.SSHResult;
import libs.libCore.modules.SharedContext;

import java.io.File;

public class RemoteExecutionSteps extends BaseSteps {

    // PicoContainer injects class SharedContext
    public RemoteExecutionSteps(SharedContext ctx) {
        super(ctx);
    }

    @When("^list files in users home directory$")
    public void list_files_in_users_home_directory() throws Throwable {
        Log.info("* Step started list_files_in_users_home_directory");

        String singleCmd = "ls";

        Log.debug("Create new client and connect to node1");
        SshCore.createClient("node1");
        Log.debug("Command to execute vis ssh is " + singleCmd);
        SSHResult result = SshCore.execute(singleCmd, 10);
        Log.debug("Result is " + result.getStdout());
        Log.debug("Exit code is " + result.getExitCode());
        SshCore.closeClient();
    }

    @When("^switch user to root$")
    public void switch_user_to_root() throws Throwable {
        Log.info("* Step started switch_user_to_root");

        String userChangeCmd = "su - root";
        String passOfUserCmd = "vagrant";
        String validateCmd = "whoami";

        Log.debug("Create new client and connect to node1");
        SshCore.createClient("node1");
        SshCore.startShell(20);
        SshCore.executeInShell("", "$");
        Log.debug("Command to execute vis ssh is " + userChangeCmd);
        SshCore.executeInShell(userChangeCmd, "Password");
        Log.debug("Command to execute vis ssh is " + passOfUserCmd);
        SshCore.executeInShell(passOfUserCmd, "root@");
        Log.debug("Command to execute vis ssh is " + validateCmd);
        SshCore.executeInShell(validateCmd, "root");
        SshCore.closeShell();
        SshCore.closeClient();
    }

    @When("^check command exit status code when in shell$")
    public void check_command_exit_status_code_when_in_shell() throws Throwable {
        Log.info("* Step started check_command_exit_status_code_when_in_shell");

        Log.debug("Create new client and connect to node1");
        SshCore.createClient("node1");
        SshCore.startShell(10);
        SshCore.executeInShell("", "$");
        SSHResult result = SshCore.executeInShell("test -e postinstall.sh;echo $?", "0");
        Log.debug(result.getStdout().replaceAll("(\\r|\\n)", "").trim());

        //if ( ! result.getStdout().replaceAll("(\\r|\\n)", "").trim().equals("0") ) {
        //    Log.error();
        //}

        SshCore.closeShell();
        SshCore.closeClient();
    }

    @When("^check that file exists on remote node$")
    public void check_that_file_exists_on_remote_node() throws Throwable {
        Log.info("* Step started check_that_file_exists_on_remote_node");

        Log.debug("Check that node is alive");
        Boolean isAlive = SshCore.checkThatNodeIsAlive("node1");

        if ( ! isAlive ) {
            Log.error("Host node1 is not available");
        }

        Log.debug("Check that file is present on the remote host");
        String pathToFile = "postinstall.sh";
        Boolean isAvailable = SshCore.checkThatFileExists("node1", pathToFile);

        if ( ! isAvailable ){
            Log.error("File postinstall.sh was not found");
        }

        Log.debug("Download file via scp");
        File file = SshCore.downloadFileViaScp("node1","postinstall.sh","C:\\Users\\akowa\\Documents\\Projects\\FK_Prototype");
        Log.debug("Path to file is " + file.getAbsolutePath());
    }

}
