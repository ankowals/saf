package steps.Executor;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import modules.core.BaseSteps;
import modules.core.Log;
import modules.core.SharedContext;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;

public class ExecutorSteps extends BaseSteps {

    // PicoContainer injects class SharedContext
    public ExecutorSteps(SharedContext ctx) {
        super(ctx);
    }

    @Given("^execute sample command$")
    public void execute_sample_command() throws Throwable {
        Log.info("* StepCore started execute_sample_command");

        String cmd = "java -version";

        String sWorkingDirPath = FileCore.createTempDir();
        File workingDir = new File(sWorkingDirPath);

        ByteArrayOutputStream out = ExecutorCore.execute(cmd, workingDir, 10, true);

        Log.debug("Output is ");
        Log.debug(new String(out.toByteArray(), Charset.defaultCharset()));
    }

    @Given("^new text file is created$")
    public void new_text_file_is_created() throws Throwable {
        Log.info("* StepCore started new_text_file_is_created");

        String cmd = "powershell.exe " +
                "\"$stream = [System.IO.StreamWriter] " +
                "'t2.txt';" +
                "$line = 'test';" +
                "1..100000 | % {$stream.WriteLine($line)};" +
                "$stream.close()\"";

        String sWorkingDirPath = FileCore.createTempDir();
        ctx.Object.put("WorkingDir", String.class, sWorkingDirPath);
        File workingDir = new File(sWorkingDirPath);

        ByteArrayOutputStream out = ExecutorCore.execute(cmd, workingDir, 10, true);

        Log.debug("Output is ");
        Log.debug(new String(out.toByteArray(), Charset.defaultCharset()));
    }

    @When("^read the file$")
    public void read_the_file() throws Throwable {
        Log.info("* StepCore started read_the_file");

        String path = ctx.Object.get("WorkingDir", String.class);
        String cmd = "powershell.exe 'Get-Content -Path " + path + "\\t2.txt'";

        String sWorkingDirPath = FileCore.createTempDir();
        File workingDir = new File(sWorkingDirPath);

        ByteArrayOutputStream out = ExecutorCore.execute(cmd, workingDir, 10, true);

        Log.debug("Output is ");
        Log.debug(new String(out.toByteArray(), Charset.defaultCharset()));
    }

}
