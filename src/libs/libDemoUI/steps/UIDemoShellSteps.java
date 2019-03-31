package libs.libDemoUI.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.ExecResult;
import libs.libCore.modules.Log;

import java.io.File;

public class UIDemoShellSteps extends BaseSteps {

    @Given("^create file (.+)$")
    public void create_temporary_file(String varName){
        //create temp file
        File file = FileCore.createTempFile("DemoFile","txt");
        String path = file.getAbsolutePath();

        //create new key in the storage
        Storage.set(varName, path);
    }

    @When("^write page title to a file (.+)$")
    public void write_page_title_to_a_file(String fileIdentifier){
        //get path to file
        String path = Storage.get(fileIdentifier);

        //get page title
        String title = scenarioCtx.get("pageTitle", String.class);

        //write text to file
        File workingDir = FileCore.getTempDir();
        String cmd = "cmd.exe /c echo '" + title + "' > " + path;
        Log.debug("Going to execute " + cmd);
        ExecutorCore.execute(cmd, workingDir, 5);

        Log.debug("Checking if it was successful");
        ExecResult result = ExecutorCore.execute("cmd.exe /c type " + path, workingDir, 5);
        Log.debug("Current file content is " + result.getStdOut());

    }

}