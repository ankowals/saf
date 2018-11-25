package libs.libExecutorExample.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.ExecResult;
import libs.libCore.modules.Log;

import java.io.File;

public class ExecutorExampleSteps extends BaseSteps {

    @Given("^execute sample command$")
    public void execute_sample_command() {
        File workingDir = FileCore.getTempDir();
        ExecResult out = ExecutorCore.execute("cmd.exe /c java -version", workingDir, 10);

        Log.warn("Error is " + out.getStdErr());
        Log.debug("Output is " + out.getStdOut());
        Log.debug("ExitValue is " + out.getExitCode());
    }

    @Given("^execute sample command in background$")
    public void execute_sample_command_in_background() {
        File workingDir = FileCore.getTempDir();
        //ExecResult out = ExecutorCore.execute("cmd.exe start /b java -version", workingDir, 10);

        ExecResult out = ExecutorCore.execute("Powershell.exe Start-Job {start-sleep 20};Write-Host 'Let us check if job was started';Get-job", workingDir, 10);

        Log.warn("Error is " + out.getStdErr());
        Log.debug("Output is " + out.getStdOut());
        Log.debug("ExitValue is " + out.getExitCode());
    }

    @Given("^execute error command$")
    public void execute_error_command() {
        File workingDir = FileCore.getTempDir();
        ExecResult out = ExecutorCore.execute("cmd.exe /c del I_do_NOT_EXIST", workingDir, 10);

        Log.warn("Error is " + out.getStdErr());
        Log.debug("Output is " + out.getStdOut());
        Log.debug("ExitValue is " + out.getExitCode());
    }


    @Given("^execute loop command$")
    public void execute_loop_command(){
        File workingDir = FileCore.getTempDir();

        String cmd = "1..100 | % {Write-Host $(get-date) '$_ testaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaaa $_';Start-Sleep -m 20}";
        ExecResult out =  ExecutorCore.execute("Powershell.exe " + cmd, workingDir, 301);

        Log.warn("Error is " + out.getStdErr());
        Log.debug("Output is " + out.getStdOut());
        Log.debug("ExitValue is " + out.getExitCode());
    }



    @Given("^create text file$")
    public void new_text_file_is_created() {
        String cmd = "powershell.exe " +
                "\"$stream = [System.IO.StreamWriter] " +
                "'t2.txt';" +
                "$line = 'testTestTESTtestTestTESTtestTestTESTtestTestTESTtestTestTESTtestTestTESTtestTestTEST';" +
                "1..100000 | % {$stream.WriteLine($_.ToString() + $line + $_.ToString())};" +
                "$stream.close()\"";

        File workingDir = FileCore.createTempDir();
        String sWorkingDirPath = workingDir.getAbsolutePath();
        scenarioCtx.put("WorkingDir", String.class, sWorkingDirPath);

        ExecResult out = ExecutorCore.execute(cmd, workingDir, 100);

        Log.warn("Error is " + out.getStdErr());
        Log.debug("Output is " + out.getStdOut());
        Log.debug("ExitValue is " + out.getExitCode());
    }

    @When("^read the file$")
    public void read_the_file(){
        String path = scenarioCtx.get("WorkingDir", String.class);
        String cmd = "powershell.exe 'Get-Content -Path " + path + "\\t2.txt'";

        File workingDir = FileCore.createTempDir();
        ExecResult out = ExecutorCore.execute(cmd, workingDir, 100);

        Log.warn("Error is " + out.getStdErr());
        Log.debug("Output is " + out.getStdOut());
        Log.debug("ExitValue is " + out.getExitCode());
    }

}