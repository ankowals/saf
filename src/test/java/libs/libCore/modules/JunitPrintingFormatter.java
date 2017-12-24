package libs.libCore.modules;

import gherkin.formatter.Formatter;
import gherkin.formatter.model.*;

import java.util.List;

/**
 * Not used currently. Prints path to the feature file when test is executed via cucumber junit runner
 */

public class JunitPrintingFormatter implements Formatter {

    public void feature(Feature feature) {}

    public void scenario(Scenario scenario) {}

    public void background(Background background) {}

    public void step(Step step) {}

    public void close() {}

    public void eof() {}

    public void scenarioOutline(ScenarioOutline scenarioOutline) {}

    public void startOfScenarioLifeCycle(Scenario scenario) {}

    public void endOfScenarioLifeCycle(Scenario scenario) {}

    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
        throw new UnsupportedOperationException();
    }

    public void examples(Examples examples) {}

    public void done() {}

    public void uri(String uri) {
        Log.debug("Uri is " + uri);
    }

}
