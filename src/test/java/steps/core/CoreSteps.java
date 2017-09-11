package steps.core;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import modules.core.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CoreSteps {

    private SharedContext ctx;

    // PicoContainer injects class SharedContext
    public CoreSteps(SharedContext ctx) {
        this.ctx = ctx;
    }

    @Given("^open browser$")
    public void open_browser() throws Throwable {
        Log.debug("* Step started open_browser");
        ctx.driver = new DriverManger(ctx).getDriver();
        Log.info("Web driver created");
        //String url = ctx.env.readProperty("WEB_url");
        //Log.info("Going to open " + url);
        //ctx.driver.get(url);
    }

    @And("^test data from \"(.*?)\" is loaded$")
    public void load_local_test_data(String arg1) throws Throwable {
        Log.debug("* Step started load_local_test_data");
        //String featuresPath = ctx.env.readProperty("features_dir");
        //String path = System.getProperty("user.dir") + featuresPath + "//" + arg1;
        String path = FeatureProvider.getFeaturesPath() + File.separator + arg1;
        ctx.config.create(path);

        HashMap<String, Object> testDataMap = ctx.obj.get("TestData",HashMap.class);

        for (Map.Entry<String, Object> entry : testDataMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Log.debug(key + " = " + value);
        }
    }

    @And("^macro evaluation is done$")
    public void eval_macro() throws Throwable {
        Log.debug("* Step started eval_macro");
        Log.info("<- evaluating macros ->");
        ctx.macro.eval("TestData");

        Log.info("Test data storage after macro evaluation is");
        HashMap<String, Object> testDataMap = ctx.obj.get("TestData",HashMap.class);

        for (Map.Entry<String, Object> entry : testDataMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Log.debug(key + " = " + value);
        }
    }
}