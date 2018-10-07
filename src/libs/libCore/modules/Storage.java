package libs.libCore.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.math.NumberUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class Storage {

    private Context scenarioCtx;
    private FileCore FileCore;
    private ConfigReader Config;
    private static final String TMP_DIR_PATH = System.getProperty("java.io.tmpdir");
    private static final File STORAGE_FILE = new File(TMP_DIR_PATH + "//" + "SAF_Persistent_Storage_File.json");

    public Storage() {
        this.scenarioCtx = ThreadContext.getContext("Scenario");
        this.FileCore = scenarioCtx.get("FileCore",FileCore.class);
        this.Config = scenarioCtx.get("Config", ConfigReader.class);
    }

    /**
     * sets value of particular key in TestData storage
     * if key does not exists it will be created
     *
     * @param textKey object name
     * @param value object value
     *
     */
    public <T> void set (String textKey, T value) {
        Log.debug("Try to set " + textKey + " to " + value);

        Integer idx = 0;
        String[] t_textKey = textKey.split("[.]");
        String StorageName = t_textKey[idx];

        HashMap<String, Object> Storage = scenarioCtx.get(StorageName, HashMap.class);

        if ( Storage == null ) {
            Log.error("Can't set " + textKey + " to " + value + ". Storage does not exists or null!");
        }

        for(idx = 1; idx < t_textKey.length; idx++) {
            String key = t_textKey[idx].split("\\[")[0];
            //if key does not exist lets create one
            if ( Storage.get(key) == null ) {
                if ( idx < t_textKey.length-1 ) {
                    if ( t_textKey[idx].contains("[") ){
                        String sIndex = t_textKey[idx].split("\\[")[1].replace("]","");
                        if ( sIndex.equals("0") ) {
                            ArrayList<Object> tValue = new ArrayList<>();
                            HashMap<String, Object> tInnerValue = new HashMap<>();
                            tValue.add(tInnerValue);
                            Storage.put(key, tValue);
                        } else {
                            Log.error("Can't set " + textKey + " to " + value + ". Key does not exists or null!");
                        }
                    } else {
                        HashMap<String, Object> tValue = new HashMap<>();
                        Storage.put(key, tValue);
                    }
                } else {
                    if ( t_textKey[idx].contains("[") ) {
                        String sIndex = t_textKey[idx].split("\\[")[1].replace("]", "");
                        Integer iIndex = Integer.valueOf(sIndex);
                        if (iIndex == 0) {
                            ArrayList<Object> tValue = new ArrayList<>();
                            tValue.add(null);
                            Storage.put(key, tValue);
                        } else {
                            Log.error("Can't set " + textKey + " to " + value + ". Key does not exists or null!");
                        }
                    } else {
                        Object tValue = null;
                        Storage.put(key, tValue);
                    }
                }
                //Log.error("Can't set " + textKey + " to " + value + ". Key does not exists or null!");
            }

            Storage = parseMap(Storage, t_textKey[idx], value);

        }
        Log.debug(textKey + " was set to " + value);
    }


    /**
     * helper function used by set method
     *
     * @param Storage HashMap
     * @param key String
     * @param value T
     *
     * @return HashMap
     */
    private <T> HashMap parseMap (HashMap Storage, String key, T value) {
        String tKey = key.split("\\[")[0];
        if ( Storage.get(tKey) instanceof Map ) {
            Storage = (HashMap<String, Object>) Storage.get(key);
        } else if (Storage.get(tKey) instanceof List ) {
            Integer index = Integer.valueOf(key.substring(key.indexOf("[") + 1, key.indexOf("]")));
            ArrayList<Object> t_Array = (ArrayList<Object>) Storage.get(tKey);
            if (t_Array.size() - index == 0) {
                HashMap<String, Object> tMap = new HashMap<>();
                t_Array.add(tMap);
                Storage = (HashMap<String, Object>) t_Array.get(index);
            } else if (t_Array.size() - index < 0) {
                Log.error("Can't set " + Storage + "." + key + " to " + value + ". Key does not exists or null!");
            } else if (t_Array.get(index) instanceof Map) {
                Storage = (HashMap<String, Object>) t_Array.get(index);
            } else {
                t_Array.set(index, value);
            }
        } else {
            Storage.put(tKey, value);
        }

        return Storage;
    }

     /**
     * returns type of value
     * helper function
     *
     * @param value object value
     *
     * @return type of value
     */
    private <T> Class<?> getType(T value){
        if(value.getClass().getName().contains("String")){
            return String.class;
        }
        if(value.getClass().getName().contains("Double")){
            return Double.class;
        }
        if(value.getClass().getName().contains("Integer")){
            return Integer.class;
        }
        if(value.getClass().getName().contains("Long")){
            return Long.class;
        }
        if(value.getClass().getName().contains("ArrayList")){
            return ArrayList.class;
        }
        if(value.getClass().getName().contains("HashMap")){
            return HashMap.class;
        }
        if(value.getClass().getName().contains("Boolean")){
            return Boolean.class;
        }
        Log.error("Type of object " + value.getClass().getName() + " not supported!");
        return null;
    }

    /**
     * Prints current content of a storage with name {} to the log file
     *
     * @param name of the storage
     */
    public void print(String name) {
        Log.debug("Going to view the current state of " + name + " storage");
        HashMap<String, Object> dataMap = scenarioCtx.get(name,HashMap.class);
        if ( dataMap != null ) {
            Log.info("--- start ---");
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                String[] tmp = entry.getValue().getClass().getName().split(Pattern.quote(".")); // Split on period.
                String type = tmp[2];
                Log.info("(" + type + ")" + entry.getKey() + " = " + entry.getValue().toString());
            }
            Log.info("--- end ---");
        }
    }


    /**
     * Retrieves particular key value from the storage.
     * Usage is get("StorageName.key1.nestedKey2[2]")
     *
     * @param path path to the value in the storage
     *
     * @return value from storage
     */
    public <T> T get(String path) {
        //do not check if storage exists if we are dealing with a number
        if ( NumberUtils.isNumber(path) ) {
            Log.warn("Value of " + path + " is null");
            return null;
        } else {
            // if no dots in the path return just the storage ->
            // for example "TestData" was entered but not "TestData.key1"
            if ( ! path.contains(".") ) {
                Object value = scenarioCtx.get(path, HashMap.class);
                Log.debug("Value of " + path + " is " + value);
                return (T) value;
            }

            //get hashmap with particular storage if it exists else return null
            String[] tmp = path.split("\\.");
            Object value = scenarioCtx.get(tmp[0], HashMap.class);

            if ( value != null ) {
                String sTmp = "";
                for (int i = 1; i < tmp.length; i++) {
                    sTmp = sTmp + "." + tmp[i];
                }

                //iterate over elements
                String[] elements = sTmp.substring(1).split("\\.");
                for (String element : elements) {
                    String ename = element.split("\\[")[0];

                    if (AbstractMap.class.isAssignableFrom(value.getClass())) {
                        value = ((AbstractMap<String, Object>) value).get(ename);
                        if ( value != null ) {
                            if (element.contains("[")) {
                                if (List.class.isAssignableFrom(value.getClass())) {
                                    Integer index = Integer.valueOf(element.substring(element.indexOf("[") + 1, element.indexOf("]")));
                                    value = ((List<Object>) value).get(index);
                                    if ( value == null ){
                                        Log.warn("Value of " + path + " is null");
                                        return null;
                                    }
                                } else {
                                    Log.warn("Value of " + path + " is null");
                                    return null;
                                }
                            }
                        } else {
                            Log.warn("Value of " + path + " is null");
                            return null;
                        }
                    } else {
                        Log.warn("Value of " + path + " is null");
                        return null;
                    }
                }
            }

            Log.debug("Value of " + path + " is " + value);
            return (T) value;

        }
    }

    public synchronized void writeToFile(String name, String identifier) {
        Log.debug("Flushing current content of the storage " + name + " to the file");
        if ( name == null || name.equals("") ){
            Log.error("Storage name null or empty!");
        }
        if ( identifier == null || identifier.equals("") ){
            Log.error("identifier null or empty!");
        }

        HashMap<String, Object> dataMap = scenarioCtx.get(name,HashMap.class);
        if ( dataMap != null ) {

            //Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Gson gson = new GsonBuilder().create();
            String content = gson.toJson(dataMap);

            if ( ! STORAGE_FILE.exists() ) {
                FileCore.writeToFile(STORAGE_FILE, identifier + "=" + content + System.getProperty("line.separator"));
                Log.debug("Storage file " + STORAGE_FILE.getAbsolutePath() + " created");
            } else {
                //overwrite line with identifier if exist else add new line
                Boolean overwriteWasDone = false;
                List<String> lines = FileCore.readLines(STORAGE_FILE);

                //just in case file exists but is empty
                if ( lines.size() == 0 ) {
                    FileCore.writeToFile(STORAGE_FILE, identifier + "=" + content + System.getProperty("line.separator"));
                    Log.debug("Storage file " + STORAGE_FILE.getAbsolutePath() + " updated");
                }

                for (int i = 0; i < lines.size(); i++) {
                    if ( lines.get(i).startsWith(identifier+"={") ) {
                        lines.set( i, identifier + "=" + content );
                        overwriteWasDone = true;
                    }
                }

                if ( overwriteWasDone.equals(true) ) {
                    FileCore.removeFile(STORAGE_FILE);
                    for (int i = 0; i < lines.size(); i++) {
                        FileCore.appendToFile(STORAGE_FILE, lines.get(i) + System.getProperty("line.separator"));
                    }
                } else {
                    FileCore.appendToFile(STORAGE_FILE, identifier + "=" + content + System.getProperty("line.separator"));
                }
                Log.debug("Storage file " + STORAGE_FILE.getAbsolutePath() + " updated");
            }
        }
    }

    public void readFromFile(String name, String identifier) {
        Log.debug("Loading current content of the storage " + name + " from file");
        if ( name == null || name.equals("") ){
            Log.error("Storage name null or empty!");
        }
        if ( identifier == null || identifier.equals("") ){
            Log.error("identifier null or empty!");
        }

        if ( STORAGE_FILE.exists() ) {
            String content = null;
            List<String> lines = FileCore.readLines(STORAGE_FILE);
            for (int i = 0; i < lines.size(); i++) {
                if ( lines.get(i).startsWith(identifier+"={") ) {
                    content = lines.get(i);
                    break;
                }
            }

            if ( content == null ){
                Log.error("Content of Storage with identifier " + identifier + " was not found! " +
                    "Please make sure that step 'write storage (.+) with id (.+) to file' was executed!");
            }

            String sJson = name + " : " + content.substring(identifier.length()+1);
            File file = FileCore.createTempFile(name + "_" + identifier + "_","config");
            FileCore.appendToFile(file, sJson);
            Config.create(file.getAbsolutePath());
            //clean up
            if ( file.exists() ) {
                FileCore.removeFile(file);
            }
        } else {
            Log.error( "Storage file " + TMP_DIR_PATH + "//"
                    + "SAF_Persistent_Storage_File.json"
                    + " does not exists!" + " Please make sure that step "
                    + " 'write storage (.+) with id (.+) to file'"
                    + " was executed" );
        }
   }

}