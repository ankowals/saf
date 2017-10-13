package steps.Db;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import modules.core.BaseSteps;
import modules.core.Log;
import modules.core.SharedContext;

import java.io.File;
import java.util.List;
import java.util.Map;

public class DbSteps extends BaseSteps {

    // PicoContainer injects class SharedContext
    public DbSteps(SharedContext ctx) {
        super(ctx);
    }

    @When("^simple select is executed$")
    public void simple_select_is_executed_with_db_utils(){
        Log.info("* Step started simple_select_is_executed");

        List<Map<String,Object>> list = SqlCore.selectList("SELECT * FROM Dept");

        SqlCore.printList(list);
        File results = SqlCore.writeListToFile(list,"SqlResult","txt");

        ctx.Object.put("SqlResults",File.class, results);
    }

    @Then("^validate that result is like (.*)$")
    public void validate_that_result_is_like(String templateName) throws Throwable {
        Log.info("* Step started validate_that_result_is_like");

        File toCompare = ctx.Object.get("SqlResults",File.class);
        String path = toCompare.getAbsolutePath();

        StepCore.attachFileToReport("SqlQueryResult.txt","text/plain",path);
        StepCore.compareWithTemplate(templateName, path);
    }


    @When("^data from (.*?) csv file is loaded to table (.*?)$")
    public void data_from_csv_file_is_loaded(String fileName, String tableName){
        Log.info("* Step started data_from_csv_file_is_loaded");

        File input = new File(FileCore.getCurrentFeatureDirPath() + "/input/" +fileName+".csv");
        SqlCore.insertFromFile(input,tableName,true, "TestData."+fileName+"TypeMapping");
    }

}
