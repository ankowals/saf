package libs.libDemoOnlineStore.modules;

import libs.libCore.modules.Log;

public class CustomPluginToBeExecutedAtTheEndOfTheScenario {

    public void load(){
        Log.debug("Hey I am your custom logic that you can run at the end of a scenario!!!!!");
    }

}
