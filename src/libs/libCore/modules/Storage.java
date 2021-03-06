package libs.libCore.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class Storage {

    private final static Object MUTEX = new Object();//guarantees synchronisation on a MUTEX object thus avoids concurrent file modification
    private Context context;
    private FileCore FileCore;
    private ConfigReader ConfigReader;
    private final String STORAGE_FILE_NAME = "SAF_Persistent_Storage_File.json";

    public Storage(Context context, FileCore fileCore, ConfigReader configReader) {
        this.context = context;
        this.FileCore = fileCore;
        this.ConfigReader = configReader;
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

        int idx = 0;
        String[] t_textKey = textKey.split("[.]");
        String StorageName = t_textKey[idx];

        HashMap<String, Object> Storage = context.get(StorageName, HashMap.class);

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
                        int iIndex = Integer.valueOf(sIndex);
                        if (iIndex == 0) {
                            ArrayList<Object> tValue = new ArrayList<>();
                            tValue.add(null);
                            Storage.put(key, tValue);
                        } else {
                            Log.error("Can't set " + textKey + " to " + value + ". Key does not exists or null!");
                        }
                    } else {
                        Storage.put(key, null);
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
            int index = Integer.valueOf(key.substring(key.indexOf("[") + 1, key.indexOf("]")));
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


    ///**
    // * Prints current content of a storage with name {} to the log file
    // *
    // * @param name of the storage
    // */
    //public void print(String name) {
    //    HashMap<String, Object> dataMap = get(name);
    //    if ( dataMap != null ) {
    //        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
    //            String[] tmp = entry.getValue().getClass().getName().split(Pattern.quote(".")); // Split on period.
    //            String type = tmp[2];
    //            Log.debug("(" + type + ")" + entry.getKey() + " = " + entry.getValue().toString());
    //        }
    //    }
    //}


    /**
     * Retrieves particular key value from the storage.
     * Usage is get("StorageName.key1.nestedKey2[2]")
     *
     * @param path path to the value in the storage
     *
     * @return value from storage
     */
    public <T> T get(String path) {
        // if no dots in the path return just the storage ->
        // for example "TestData" was entered but not "TestData.key1"
        if ( path == null ){
            Log.warn("Key null not found!");
            return null;
        }
        if ( ! path.contains(".") ) {
            Object value = context.get(path, HashMap.class);
            if ( value != null) {
                Log.debug("Value of " + path + " is " + value);
            } else {
                Log.warn("Key " + path + " not found!");
            }
            return (T) value;
        }

        //get hashmap with particular storage if it exists else return null
        String[] tmp = path.split("\\.");
        Object value = context.get(tmp[0], HashMap.class);

        if ( value != null ) {
            //String sTmp = "";
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < tmp.length; i++) {
                sb.append(".");
                sb.append(tmp[i]);
            }

            //iterate over elements
            String[] elements = sb.toString().substring(1).split("\\.");
            for (String element : elements) {
                String ename = element.split("\\[")[0];

                if (AbstractMap.class.isAssignableFrom(value.getClass())) {
                    value = ((AbstractMap<String, Object>) value).get(ename);
                    if ( value != null ) {
                        if (element.contains("[")) {
                            if (List.class.isAssignableFrom(value.getClass())) {
                                int index = Integer.valueOf(element.substring(element.indexOf("[") + 1, element.indexOf("]")));
                                value = ((List<Object>) value).get(index);
                                if ( value == null ){
                                    Log.warn("Key " + path + " not found!");
                                    return null;
                                }
                            } else {
                                Log.warn("Key " + path + " not found!");
                                return null;
                            }
                        }
                    } else {
                        Log.warn("Key " + path + " not found!");
                        return null;
                    }
                } else {
                    Log.warn("Key " + path + " not found!");
                    return null;
                }
            }
        }

        Log.debug("Value of " + path + " is " + value);
        return (T) value;

    }

    public void writeToFile(String name, String identifier) {
        synchronized (MUTEX) {
            Log.debug("Flushing current content of the storage " + name + " to the file");
            if (name == null || name.equals("")) {
                Log.error("Storage name null or empty!");
            }
            if (identifier == null || identifier.equals("")) {
                Log.error("identifier null or empty!");
            }

            HashMap<String, Object> dataMap = context.get(name, HashMap.class);
            if (dataMap != null) {

                //Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Gson gson = new GsonBuilder().create();
                String content = gson.toJson(dataMap);

                String tmpDirPath = FileCore.getTempDir().getAbsolutePath();
                File storageFile = new File(tmpDirPath + File.separator + STORAGE_FILE_NAME);

                if (!storageFile.exists()) {
                    FileCore.writeToFile(storageFile, identifier + "=" + content + System.getProperty("line.separator"));
                    Log.debug("Storage file " + storageFile.getAbsolutePath() + " created");
                } else {
                    //overwrite line with identifier if exist else add new line
                    Boolean overwriteWasDone = false;
                    List<String> lines = FileCore.readLines(storageFile);

                    //just in case file exists but is empty
                    if (lines.size() == 0) {
                        FileCore.writeToFile(storageFile, identifier + "=" + content + System.getProperty("line.separator"));
                        Log.debug("Storage file " + storageFile.getAbsolutePath() + " updated");
                    }

                    for (int i = 0; i < lines.size(); i++) {
                        if (lines.get(i).startsWith(identifier + "={")) {
                            lines.set(i, identifier + "=" + content);
                            overwriteWasDone = true;
                        }
                    }

                    if (overwriteWasDone.equals(true)) {
                        FileCore.removeFile(storageFile);
                        for (int i = 0; i < lines.size(); i++) {
                            FileCore.appendToFile(storageFile, lines.get(i) + System.getProperty("line.separator"));
                        }
                    } else {
                        FileCore.appendToFile(storageFile, identifier + "=" + content + System.getProperty("line.separator"));
                    }
                    Log.debug("Storage file " + storageFile.getAbsolutePath() + " updated");
                }
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

        String tmpDirPath = FileCore.getTempDir().getAbsolutePath();
        File storageFile = new File(tmpDirPath + File.separator + STORAGE_FILE_NAME);

        if ( storageFile.exists() ) {
            String content = null;
            List<String> lines = FileCore.readLines(storageFile);
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
            ConfigReader.create(file.getAbsolutePath());
            //clean up
            if ( file.exists() ) {
                FileCore.removeFile(file);
            }
        } else {
            Log.error( "Storage file " + storageFile.getAbsolutePath()
                    + " does not exists!" + " Please make sure that step "
                    + " 'write storage (.+) with id (.+) to file'"
                    + " was executed" );
        }
   }

}