package modules.core;

import org.apache.commons.lang.math.NumberUtils;
import java.util.*;
import java.util.regex.Pattern;

import static java.lang.Math.toIntExact;

public class Storage {

    private SharedContext ctx;

    // PicoContainer injects class SharedContext
    public Storage(SharedContext ctx) {
        this.ctx = ctx;
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

        Integer idx = 0;
        String[] t_textKey = textKey.split("[.]");
        String StorageName = t_textKey[idx];

        HashMap<String, Object> Storage = ctx.Object.get(StorageName,HashMap.class);

        if ( Storage == null ) {
            Log.error("Can't set " + textKey + " to " + value + ". Storage does not exists or null!");
        }

        for(idx = 1; idx < t_textKey.length; idx++) {
            String key = t_textKey[idx].split("\\[")[0];
            Log.debug("Key is " + key);
            if ( Storage.get(key) == null ) {
                Log.error("Can't set " + textKey + " to " + value + ". Key does not exists or null!");
            }

            Storage = parseMap(Storage, t_textKey[idx], value);

        }
    }

    private <T> HashMap parseMap (HashMap Storage, String key, T value) {
        String tKey = key.split("\\[")[0];
        if ( Storage.get(tKey) instanceof Map ) {
            Storage = (HashMap<String, Object>) Storage.get(key);
        } else if (Storage.get(tKey) instanceof List )  {
            Integer index = Integer.valueOf(key.substring(key.indexOf("[") + 1, key.indexOf("]")));
            ArrayList<Object> t_Array = (ArrayList<Object>) Storage.get(tKey);
            if (t_Array.get(index) instanceof Map) {
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
        HashMap<String, Object> dataMap = ctx.Object.get(name,HashMap.class);
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
            return null;
        } else {
            //if no dots in the path return just the storage ->
            // for example "TestData" was entered but not "TestData.key1"
            if ( !path.contains(".") ) {
                Object value = ctx.Object.get(path, HashMap.class);
                return (T) value;
            }

            //get hashmap with particular storage if it exists else return null
            String[] tmp = path.split("\\.");
            Object value = ctx.Object.get(tmp[0], HashMap.class);
            if (value != null) {
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

                        if (element.contains("[")) {
                            if (List.class.isAssignableFrom(value.getClass())) {
                                Integer index = Integer.valueOf(element.substring(element.indexOf("[") + 1, element.indexOf("]")));
                                value = ((List<Object>) value).get(index);
                            } else {
                                return null;
                            }
                        }
                    } else {
                        return null;
                    }
                }
            }

            return (T) value;

        }
    }

}
