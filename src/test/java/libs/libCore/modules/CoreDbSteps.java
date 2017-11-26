package libs.libCore.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import libs.libCore.modules.BaseSteps;
import libs.libCore.modules.Log;
import libs.libCore.modules.SharedContext;

import java.io.File;
import java.util.List;
import java.util.Map;

public class CoreDbSteps extends BaseSteps {

    // PicoContainer injects class SharedContext
    public CoreDbSteps(SharedContext ctx) {
        super(ctx);
    }

    /**
     * Loads data from csv file to data base
     * It uses csv file as an input and TestData.<filename>TypeMapping object
     * TypeMapping shall contain mapping columns to type for particular csv
     * Type mapping shall be in the form of a List. For example
     * inputTypeMapping : ["NUMERIC","VARCHAR","VARCHAR"], where input file name is input.csv
     * Input file shall be located in subdirectory input
     *
     * @param fileName, String, name of the input csv file that contains input data (without .csv extension)
     * @param tableName, String, name of the table to which data shall be loaded
     */
    @Given("^data from (.*?) csv file is loaded to table (.*?)$")
    public void data_from_csv_file_is_loaded_to_table(String fileName, String tableName) throws Throwable {
        Log.info("* Step started data_from_csv_file_is_loaded_to_table");

        String path = ctx.Object.get("CurrentFeatureDir", String.class);

        File input = new File(path + "/input/" + fileName + ".csv");
        StepCore.attachFileToReport(fileName+".csv", "text/csv", input.getAbsolutePath());
        Log.debug("Path to csv input file is " + input.getAbsolutePath());
        SqlCore.insertFromFile(input,tableName,true, "TestData." + fileName + "TypeMapping");
    }


    /**
     * Executes sql select query
     * Results are printed to the log and attached as a file attachment to the report
     * They are available as a ctx.Object for further processing and validation
     *
     * Uses following objects:
     *  ctx.Object.queryResultName
     *
     * @param sQuery, String, query to be executed (can also be defined in test data config, in that case
     *                this parameter act as query identifier)
     * @param queryResultName, String, name of the context object that is
     *                         going to be used to store path to the select query results
     */
    @When("^select query (.*?) is executed and results stored as (.*?)$")
    public void select_query_is_executed(String sQuery, String queryResultName) throws Throwable {
        Log.info("* Step started select_query_is_executed");

        String query = StepCore.checkIfInputIsVariable(sQuery);
        List<Map<String,Object>> list = SqlCore.selectList(query);

        SqlCore.printList(list);
        File results = SqlCore.writeListToFile(list,queryResultName,"txt");

        StepCore.attachFileToReport(queryResultName + ".txt","text/plain", results.getAbsolutePath());
        ctx.Object.put(queryResultName, String.class, results.getAbsolutePath());
    }


    /**
     * Verifies sql select query results by executing template comparison
     *
     * Uses following objects:
     *  ctx.Object.queryResultName
     *
     * @param queryResultName, String, name of the context Object that stores the query results
     * @param templateName, String, name of the template used for comparison
     */
    @Then("^validate that select query result (.*?) is like (.*) template$")
    public void validate_that_select_query_result_is_like_template(String queryResultName, String templateName) throws Throwable {
        Log.info("* Step started validate_that_select_query_result_is_like_template");

        String path = ctx.Object.get(queryResultName, String.class);
        StepCore.compareWithTemplate(templateName, path);
    }

    /**
     * Creates table backup in the dB
     *
     * @param tableName, String, name of the table
     * @param backupName, String, name of the backup table
     */
    @When("^create table (.*?) backup with name (.*)$")
    public void create_table_backup_with_name(String tableName, String backupName) throws Throwable {
        Log.info("* Step started create_table_backup_with_name");

        String table = StepCore.checkIfInputIsVariable(tableName);
        String backup = StepCore.checkIfInputIsVariable(backupName);

        Integer numOfRowsInTable = SqlCore.selectScalar("SELECT COUNT(*) FROM " + table);

        String query = "SELECT INTO " + backup + " FROM " + table;
        SqlCore.selectList(query);

        Integer numOfRowsInBackup = SqlCore.selectScalar("SELECT COUNT(*) FROM " + backup);

        if ( ! numOfRowsInTable.equals(numOfRowsInBackup)){
            Log.error("Table backup failed! Number of rows in " + table + " " + numOfRowsInTable +
                " is not equal to number of rows in " + backup + " " + numOfRowsInBackup);
        }
    }

    /**
     * Verifies that content of 2 tables A and B is the same
     *
     * @param tableAName, String, name of the table A
     * @param tableBName, String, name of the table B
     */
    @Then("^verify that content of table (.*?) equals content of table (.*)$")
    public void verify_that_content_of_table_equals_content_of_table(String tableAName, String tableBName) throws Throwable {
        Log.info("* Step started create_table_backup_with_name");

        String tableA = StepCore.checkIfInputIsVariable(tableAName);
        String tableB = StepCore.checkIfInputIsVariable(tableBName);

        String query = "SELECT * FROM TABLE " + tableA + " EXCEPT SELECT * FROM TABLE " + tableB;
        List<Map<String,Object>> list = SqlCore.selectList(query);

        if ( list.size() > 0 ){
            SqlCore.printList(list);
            Log.error("Content of table " + tableA + " is different than content of table " + tableB);
        }
    }

}
