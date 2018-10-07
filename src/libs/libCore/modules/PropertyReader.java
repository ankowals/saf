package libs.libCore.modules;

import java.util.Properties;

@SuppressWarnings("unchecked")
public class PropertyReader {

    private Context scenarioCtx;
    private FileCore FileCore;

    public PropertyReader () {
        this.scenarioCtx = ThreadContext.getContext("Scenario");
        this.FileCore = scenarioCtx.get("FileCore",FileCore.class);
    }


    /**
     * Prints system properties to the log file
     */
    public static void readSystemProperties(){
        Properties p = System.getProperties();
        Log.info("--- System properties are as follows ---");
        Log.info("os.arch:" + p.get("os.arch"));
        Log.info("os.name:" + p.get("os.name"));
        Log.info("user.name:" + p.get("user.name"));
        Log.info("user.home:" + p.get("user.home"));
        Log.info("user.dir:" + p.get("user.dir"));
        Log.info("user.timezone:" + p.get("user.timezone"));
        Log.info("java.runtime.name:" + p.get("java.runtime.name"));
        Log.info("java.version:" + p.get("java.version"));
        Log.info("java.vm.version:" + p.get("java.vm.version"));
        Log.info("java.io.tmpdir:" + p.get("java.io.tmpdir"));
        Log.info("java.home:" + p.get("java.home"));
        //while (keys.hasMoreElements()) {
        //    String key = (String)keys.nextElement();
        //    String value = (String)p.get(key);
        //    Log.debug(key + ": " + value);
        //}
        Log.info("--- end ---");
    }

}