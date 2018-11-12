package libs.libExecutorExample.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;

import java.io.File;

public class ExecutorExampleSteps extends BaseSteps {

    @Given("^execute sample command$")
    public void execute_sample_command() {
        File workingDir = FileCore.getTempDir();
        String out = ExecutorCore.execute("java -version", workingDir, 10, true);

        Log.debug("Output is");
        Log.debug(out);
    }


    @Given("^execute loop command$")
    public void execute_loop_command(){
        File workingDir = FileCore.getTempDir();

        String cmd = "1..1000 | % {Write-Host $(get-date) '$_ testaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaaa $_';Start-Sleep -m 20}";
        ExecutorCore.execute("Powershell.exe " + cmd, workingDir, 301, true);
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

        String out = ExecutorCore.execute(cmd, workingDir, 100, true);

        Log.debug("Output is ");
        Log.debug(out);
    }

    @When("^read the file$")
    public void read_the_file(){
        String path = scenarioCtx.get("WorkingDir", String.class);
        String cmd = "powershell.exe 'Get-Content -Path " + path + "\\t2.txt'";

        File workingDir = FileCore.createTempDir();
        String out = ExecutorCore.execute(cmd, workingDir, 100, true);

        Log.debug("Output is ");
        Log.debug(out);
    }

}