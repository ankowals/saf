package libs.libCore.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;

import java.io.File;
import java.util.List;
import java.util.Map;

public class CoreDbSteps extends BaseSteps {

    /**
     * Loads data from csv file to data base<br>
     * It uses csv file as an input and TestData.{filename}TypeMapping object<br>
     * TypeMapping shall contain mapping columns to type for particular csv<br>
     * Type mapping shall be in the form of a List. For example<br>
     * inputTypeMapping : ["NUMERIC","VARCHAR","VARCHAR"], where file name is input.csv<br>
     * input.csv file shall be located in subdirectory resources
     *
     * @param url String, connection string of the database that shall be used
     * @param params Map, table that contains sql query and template name
     */
    @Given("^load into a table in (.+) database$")
    public void load_data_from_csv_file_into_a_table(String url, Map<String, String> params) {
        String connectionString = StepCore.checkIfInputIsVariable(url);

        String fileName = "";
        String tableName = "";

        //handle params
        if ( ! params.isEmpty() ) {

            for (Map.Entry<String, String> row : params.entrySet()) {
                String param_name = row.getKey();
                String param_value = StepCore.checkIfInputIsVariable(row.getValue());

                if ( param_name.equals("file") ) {
                    fileName = StepCore.checkIfInputIsVariable(param_value);
                    fileName = fileName.replaceAll(".csv$","");
                }
                if ( param_name.equals("table") ) {
                    tableName = StepCore.checkIfInputIsVariable(param_value);
                }
            }
        }

        File input = new File(FileCore.getCurrentFeatureDirPath() + "/resources/" + fileName + ".csv");
        StepCore.attachFileToReport(fileName + ".csv", "text/csv", input.getAbsolutePath());
        Log.debug("Path to csv input file is " + input.getAbsolutePath());
        SqlCore.insertFromFile(connectionString, input, tableName,true, "TestData." + fileName + "TypeMapping");
    }

    /**
     * Executes sql query in desired database and compares results with a template.<br>
     * Sql query and template name shall be passed to this step as a table.
     *
     * @param url String, connection string of the database that shall be used
     * @param params Map, table that contains sql query and template name
     */
    @Then("^verify in (.+) database$")
    public void verify_in_database(String url, Map<String, String> params) {

        String connectionString = StepCore.checkIfInputIsVariable(url);

        String query = "";
        String templateName = "";
        boolean templateComparison  = false;

        //handle params
        if ( ! params.isEmpty() ) {

            for (Map.Entry<String, String> row : params.entrySet()) {
                String param_name = row.getKey();
                String param_value = StepCore.checkIfInputIsVariable(row.getValue());

                if ( param_name.equals("query") ) {
                    query = StepCore.checkIfInputIsVariable(param_value);
                }
                if ( param_name.equals("template") ) {
                    templateName = param_value;
                    if ( ! templateName.equals("") ){
                        templateComparison = true;
                    }
                }
            }
        }

        String queryAfterReplacement = StepCore.replaceInString(query);
        List<Map<String,Object>> list = SqlCore.selectList(connectionString, queryAfterReplacement);

        SqlCore.printList(list);
        String resName = "sqlSelectQueryResults";
        File results = SqlCore.writeListToFile(list,resName,"txt");

        StepCore.attachFileToReport(resName + ".txt","text/plain", results.getAbsolutePath());
        scenarioCtx.put(resName, String.class, results.getAbsolutePath());

        if (templateComparison) {
            StepCore.compareWithTemplate(templateName, results.getAbsolutePath());
        }

    }



    /**
     * Executes update sql statement in desired database.<br>
     * Multiple statements can be provided if more than one update shall be executed.
     *
     * @param url String, connection string of the database that shall be used
     * @param params Map, table that contains sql statement
     */
    @When("^edit in (.+) database$")
    public void edit_in_database(String url, List<String> params) {

        String connectionString = StepCore.checkIfInputIsVariable(url);

        //handle params
        if ( ! params.isEmpty() ) {
            for (String row : params) {
                String statement = StepCore.checkIfInputIsVariable(row);

                String statementAfterReplacement = StepCore.replaceInString(statement);
                Integer result = SqlCore.update(connectionString, statementAfterReplacement);
                if (result == 0){
                    Log.warn("No rows were updated!");
                } else {
                    Log.debug(result + " rows were updated");
                }
            }
        }
    }


    /**
     * Executes insert sql statement in desired database.<br>
     * Multiple statements can be provided if more than one insert shall be executed.
     *
     * @param url String, connection string of the database that shall be used
     * @param params Map, table that contains sql statement
     */
    @When("^insert into (.+) database$")
    public void insert_into_database(String url, List<String> params) {

        String connectionString = StepCore.checkIfInputIsVariable(url);

        //handle params
        if ( ! params.isEmpty() ) {
            for (String row : params) {
                String statement = StepCore.checkIfInputIsVariable(row);
                String statementAfterReplacement = StepCore.replaceInString(statement);
                SqlCore.insert(connectionString, statementAfterReplacement);
                Log.warn("Insert statement was executed but not feedback was provided how many rows were inserted!");
            }
        }

    }


    /**
     * Executes delete sql statement in desired database.<br>
     * Multiple statements can be provided if more than one delete shall be executed.
     *
     * @param url String, connection string of the database that shall be used
     * @param params Map, table that contains sql statement
     */
    @When("^delete from (.+) database$")
    public void delete_from_database(String url, List<String> params) {

        String connectionString = StepCore.checkIfInputIsVariable(url);

        //handle params
        if ( ! params.isEmpty() ) {

            for (String row : params) {
                String statement = StepCore.checkIfInputIsVariable(row);
                String statementAfterReplacement = StepCore.replaceInString(statement);
                Integer result = SqlCore.delete(connectionString, statementAfterReplacement);
                if ( result == 0 ){
                    Log.warn("No rows were deleted!");
                } else {
                    Log.debug(result + " rows were deleted");
                }
            }
        }

    }




}
