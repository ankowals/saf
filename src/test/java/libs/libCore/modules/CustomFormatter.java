package libs.libCore.modules;

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Match;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;

import java.util.List;


/**
 *
 * Created to enhance logging functionality.<br>
 * Used to unify logging and make sure that when test runs from IDE log in the file and in the console is same.<br>
 * Overridden methods can be used to extend logging functionality.<br>
 * To make use of this class please add new environment variable under default cucumber java plugin configuration <br>
 * cucumber.options=cucumber.options= --plugin org.jetbrains.plugins.cucumber.java.run.CucumberJvmSMFormatter --monochrome --plugin libs.libCore.modules.CustomFormatter
 *
 */
public class CustomFormatter implements Formatter, Reporter {

    static {
        StatusLogger.getLogger().setLevel(Level.OFF);
    }
    private static Logger Log = LogManager.getLogger("libs.libCore.modules");

    @Override
    public void syntaxError(String s, String s1, List<String> list, String s2, Integer integer) {

    }

    @Override
    public void uri(String s) {

    }

    @Override
    public void feature(Feature feature) {

    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {

    }

    @Override
    public void examples(Examples examples) {

    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {

    }

    @Override
    public void background(Background background) {

    }

    @Override
    public void scenario(Scenario scenario) {

    }

    @Override
    public void step(Step step) {

    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {

    }

    @Override
    public void done() {

    }

    @Override
    public void close() {

    }

    @Override
    public void eof() {

    }

    @Override
    public void before(Match match, Result result) {

    }

    /**
     * Used for logging exceptions in to log file.
     * @param  result an object containing information about executed test.
     */
    @Override
    public void result(Result result) {
        //Log.error(result.getError());
        if (result.getErrorMessage() != null){
            Log.error(result.getErrorMessage());
        }
    }

    @Override
    public void after(Match match, Result result) {

    }

    @Override
    public void match(Match match) {

    }

    @Override
    public void embedding(String s, byte[] bytes) {

    }

    @Override
    public void write(String s) {

    }
}
