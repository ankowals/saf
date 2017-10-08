package modules.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

public class PropertyReader {

    private SharedContext ctx;
    private FileCore FileCore;

    private Properties properties = new Properties();
    private InputStream inputStream = null;
    private String configDir;
    private String default_env = "default";

    // PicoContainer injects class SharedContext
    public PropertyReader (SharedContext ctx) {
        this.ctx = ctx;
        this.FileCore = ctx.Object.get("FileCore",FileCore.class);
        this.configDir = FileCore.getGlobalConfigPath() + "//" + "environment";

        loadProperties(configDir + "//" + default_env + ".properties");
        String act_env = checkActiveEnv();
        if ( act_env != null && !act_env.equals(default_env) && !act_env.equals("") ) {
            loadProperties(configDir + "//" + act_env + ".properties");
        }
        readProperties();

    }

    private void loadProperties(String path) {
        try {
            inputStream = new FileInputStream(path);
            Log.debug("Going to load properties from " + path);
            Properties tmp = new Properties();
            tmp.load(inputStream);
            Enumeration em = tmp.keys();
            while(em.hasMoreElements()){
                String str = (String)em.nextElement();
                properties.setProperty(str,tmp.getProperty(str));
            }
        } catch (IOException e) {
            Log.error( "Configuration file " + path + " not found!", e );
        }
    }

    public String readProperty(String key) {
        if (properties.getProperty(key) == null || properties.getProperty(key).isEmpty()) {
            Log.error("Property " + key + " not set!");
        }
        String value = properties.getProperty(key);
        Log.info("Environment property " + key + " = " + value);
        return value;
    }

    public void readSystemProperties(){
        Properties p = System.getProperties();
        //Enumeration keys = p.keys();
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

    public void readProperties() {
        Log.info("--- Following properties where found ---");
        Enumeration em = properties.keys();
        while(em.hasMoreElements()){
            String str = (String)em.nextElement();
            Log.info(str + ": " + properties.get(str));
        }
        Log.info("--- end ---");
    }

    public String checkActiveEnv() {
        String result = null;
        //check if active_env property provided as a cmd line argument if not check what is available in default env configuration file
        String cmd_arg  = System.getProperty("active_env");
        if ( cmd_arg != null ){
            result = cmd_arg;
            properties.setProperty("active_env" , result);
            Log.info("Overwritten by CMD arg -Dactive_env=" + cmd_arg);
            Log.info("Using environment configuration from " + result);
        } else if (properties.getProperty("active_env") == null
                || properties.getProperty("active_env").isEmpty()
                || properties.getProperty("active_env") == ""
                || properties.getProperty("active_env").equals(default_env)){
            Log.info("Using default environment configuration");
        } else {
            result = properties.getProperty("active_env");
            Log.info("Using environment configuration from " + result);
        }

        return result;
    }
}
