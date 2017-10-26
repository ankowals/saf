package libs.libCore.steps;

import cucumber.api.java.en.Given;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;
import libs.libCore.modules.SharedContext;

import java.io.File;
import java.util.List;

public class CorePdfSteps extends BaseSteps {

    // PicoContainer injects class SharedContext
    public CorePdfSteps(SharedContext ctx) {
        super(ctx);
    }

    /**
     * Reads unencrypted pdf file line by line and prints it content to the log file
     *
     * @param pathToFile, String, path to pdf file
     */
    @Given("^read pdf file from (.+)$")
    public void read_pdf_file_from(String pathToFile) throws Throwable {
        Log.info("* Step started read_pdf_file_from");

        String path = StepCore.checkIfInputIsVariable(pathToFile);

        File file = new File(path);
        if (! file.exists()) {
            Log.error("File " + path + " does not exists");
        }

        Log.debug("Reading pdf file " + file.getAbsolutePath());
        List<String> lines = PdfCore.readLines(file);

        for (String line : lines) {
            Log.debug(line);
        }
    }



}
