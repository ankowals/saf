package libs.libDemoDocuments.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;

import java.io.File;
import java.util.List;
import java.util.Map;

public class DocumentsHandlingSteps extends BaseSteps {

    @Given("^read document to (.+)$")
    public void read_document_to(String variable){
        String path = FileCore.getCurrentFeatureDirPath() + File.separator + "resources" + File.separator + "demoDocument.pdf";

        File file = new File(path);
        if (! file.exists()) {
            Log.error("File " + path + " does not exists!");
        }

        Log.debug("Reading pdf file " + file.getAbsolutePath());
        List<String> lines = PdfCore.readLines(file);

        Log.debug("File content is");
        for (String line : lines) {
            Log.debug(line);
        }

        //expose pdf content as a ctx variable
        scenarioCtx.put(variable, String.class, StringCore.join(lines, System.lineSeparator()));
    }

    @When("write content of (.+) to csv file (.+)")
    public void write_content_of_to_file(String content, String path){
        String contentToWrite = scenarioCtx.get(content, String.class);

        File file = FileCore.createTempFile("PdfContent", ".csv");
        FileCore.writeToFile(file, contentToWrite);
        Storage.set(path, file.getAbsolutePath());
    }

    @Then("verify csv file (.+) content")
    public void verify_csv_file_content(String path, List<Map<String, String>> table){
        String pathToFile = Storage.get(path);
        String content = FileCore.readToString(new File(pathToFile));

        if ( table.size() == 0 ) {
            Log.error("Wrong format of table used in step. Please make sure that table contains "
                    + " header like: | key | action | expected_value | "
                    + " and rows with values for each column");
        }

        //get rows
        for (int i = 0; i < table.size(); i++) {
            Map<String, String> row = table.get(i);

            Log.debug("Row is " + row);
            //get columns
            String key = null;
            String action = null;
            Object expectedValue = null;
            for (Map.Entry<String, String> column : row.entrySet()) {
                //get name of the column
                String name = column.getKey();
                //get value of that column for current row
                String valueInRow = column.getValue();

                //assign values from columns to variables
                if ( name.equalsIgnoreCase("key") ){
                    key = valueInRow;
                    continue;
                }
                if ( name.equalsIgnoreCase("action") ){
                    action = valueInRow;
                    continue;
                }
                if ( name.equalsIgnoreCase("expected") ){
                    expectedValue = StepCore.checkIfInputIsVariable(valueInRow);
                }

                //execute simple error handling
                if(key == null){
                    Log.error("key in verify step table does not exist or null!");
                }
                if(action == null){
                    Log.error("key in verify step table does not exist or null!");
                }
                if (key.equals("")) {
                    Log.error("key in verify step table is an empty string!");
                }
                if (action.equals("")) {
                    Log.error("action in verify step table is an empty string!");
                }

                //execute comparison
                CsvCore.verifyValueInParticularRowAndColumn(key, action, expectedValue.toString(), content);
            }
        }
    }

}