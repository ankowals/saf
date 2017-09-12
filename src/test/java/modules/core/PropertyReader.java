package modules.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

public class PropertyReader {

    private Properties properties = new Properties();
    InputStream inputStream = null;
    String configDir = FeatureProvider.getGlobalConfigPath();
    String default_env = "env";

    public PropertyReader() {
        loadProperties(configDir + "//" + default_env + ".properties");
        String act_env = checkActiveEnv();
        if ( act_env != null && !act_env.equals(default_env) && !act_env.equals("") ) {
            loadProperties(configDir + "//" + act_env + ".properties");
        }
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
            readProperties();
        } catch (IOException e) {
            Log.error("Configuration file " + path + " not found!");
            Log.error(e.getMessage());
        }
    }

    public String readProperty(String key) {
        if (properties.getProperty(key) == null || properties.getProperty(key).isEmpty()) {
            Log.fatal("Property " + key + " not set!");
        }
        String value = properties.getProperty(key);
        Log.debug("Environment property " + key + " = " + value);
        return value;
    }

    public void readSystemProperties(){
        Properties p = System.getProperties();
        //Enumeration keys = p.keys();
        Log.debug("--- System properties are as follows ---");
        Log.debug("os.arch:" + p.get("os.arch"));
        Log.debug("os.name:" + p.get("os.name"));
        Log.debug("user.name:" + p.get("user.name"));
        Log.debug("user.home:" + p.get("user.home"));
        Log.debug("user.dir:" + p.get("user.dir"));
        Log.debug("user.timezone:" + p.get("user.timezone"));
        Log.debug("java.runtime.name:" + p.get("java.runtime.name"));
        Log.debug("java.version:" + p.get("java.version"));
        Log.debug("java.vm.version:" + p.get("java.vm.version"));
        Log.debug("java.io.tmpdir:" + p.get("java.io.tmpdir"));
        Log.debug("java.home:" + p.get("java.home"));
        //while (keys.hasMoreElements()) {
        //    String key = (String)keys.nextElement();
        //    String value = (String)p.get(key);
        //    Log.debug(key + ": " + value);
        //}
        Log.debug("--- end ---");
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
