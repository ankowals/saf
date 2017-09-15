package modules.core;

import com.google.gson.*;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.SimpleFileVisitor;
import java.text.NumberFormat;
import java.util.*;

public class ConfigReader {

    private SharedContext ctx;

    // PicoContainer injects class SharedContext
    public ConfigReader(SharedContext ctx) {
        this.ctx = ctx;
    }


    /**
     * Creates or updates existing storage based on the config file content
     * Content file structure will be checked in case it is not parsable
     * an error will be indicated in the log
     * Content shall be in the form of json
     *
     * @param path path to the config file
     */
    public void create(String path) {

        JsonElement root = null;
        JsonObject object = null;
        //FileReader file = null;
        String sFile = null;
        HashMap<String, Object> result = null;

        //check that file exists
        //try {
        //    file = new FileReader(path);
        //} catch (FileNotFoundException e) {
        //    Log.error("Configuration file " + path + " not found!");
        //    Log.error(e.getMessage());
        //}

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            Log.error("Configuration file " + path + " not found!");
            Log.error(e.getMessage());
        }

        try {
            try {
                sFile = IOUtils.toString(inputStream);
                if(!sFile.startsWith("{")) {
                    sFile = "{" + sFile + "}";
                }
            } catch (IOException e) {
                Log.error("Configuration file " + path + " not found!");
                Log.error(e.getMessage());
            }
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.error("Configuration file " + path + " not found!");
                Log.error(e.getMessage());
            }
        }

        //read the JSON file and make sure that format is correct
        try {
            //root = new JsonParser().parse(file);
            root = new JsonParser().parse(sFile);
        } catch (JsonSyntaxException e1) {
                Log.error("Typo in file " + path);
                Log.error(e1.getMessage());
        }

        //read each entry and create new shared object for it
        if(root.isJsonObject()){
            Set<Map.Entry<String, JsonElement>> entries = root.getAsJsonObject().entrySet();//will return members of your object
            for (Map.Entry<String, JsonElement> entry: entries) {
                try {
                    object = root.getAsJsonObject().get(entry.getKey()).getAsJsonObject();
                } catch (NullPointerException e1) {
                    Log.error("Configuration file " + path + " not found!");
                    Log.error(e1.getMessage());
                }

                result = parseObject(object);

                //if ctx object already exists overwrite/update its content else create new one
                HashMap<String, Object> tmpMap = ctx.obj.get(entry.getKey(),HashMap.class);
                if (tmpMap == null ) {
                    ctx.obj.put(entry.getKey(), HashMap.class, result);
                } else {
                    tmpMap.putAll(result);
                    ctx.obj.put(entry.getKey(), HashMap.class, tmpMap);
                }
            }

        }
    }

    /**
     * Parses json object
     * helper function used to parse config files content
     * Parsing is done to HashMap
     * Values can be of type: Long, Double, String, Boolean, ListArray, HashMap
     *
     * @param object jsonObject extractet from the config file
     * @return HashMap
     *
     */
    private HashMap<String, Object> parseObject(JsonObject object) {
        Set<Map.Entry<String, JsonElement>> set = object.entrySet();
        Iterator<Map.Entry<String, JsonElement>> iterator = set.iterator();
        HashMap<String, Object> map = new HashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonElement> entry = iterator.next();
            String key = entry.getKey();
            if (entry.getValue().isJsonPrimitive()) {
                if(entry.getValue().getAsJsonPrimitive().isNumber()){
                    Number num = null;
                    try {
                        num = NumberFormat.getInstance(Locale.getDefault()).parse(entry.getValue().getAsString());
                    } catch (Exception e) {
                        Log.error("Not able to parse String to Number for " + key + " : " + entry.getValue().getAsString());
                        Log.error(e.getMessage());
                    }
                    if (num != null){
                        map.put(key, num);
                    }
                }else if(entry.getValue().getAsJsonPrimitive().isString()){
                    map.put(key, entry.getValue().getAsString());
                }else if(entry.getValue().getAsJsonPrimitive().isBoolean()){
                    map.put(key, entry.getValue().getAsBoolean());
                }else{
                    Log.warn("Didn't recognized type of primitive data. Going to put null value!");
                    map.put(key, null);
                }
            } else if (entry.getValue().isJsonArray()){
                ArrayList<Object> tmpArray = new ArrayList(); //missing support for array of arrays and array of maps
                for (int i = 0; i < entry.getValue().getAsJsonArray().size(); i++) {
                    if(entry.getValue().getAsJsonArray().get(i).isJsonPrimitive()){
                        if(entry.getValue().getAsJsonArray().get(i).getAsJsonPrimitive().isBoolean()){
                            tmpArray.add(entry.getValue().getAsJsonArray().get(i).getAsBoolean());
                        }else if(entry.getValue().getAsJsonArray().get(i).getAsJsonPrimitive().isString()){
                            tmpArray.add(entry.getValue().getAsJsonArray().get(i).getAsString());
                        }else if(entry.getValue().getAsJsonArray().get(i).getAsJsonPrimitive().isNumber()){
                            Number num = null;
                            try {
                                num = NumberFormat.getInstance(Locale.getDefault()).parse(entry.getValue().getAsJsonArray().get(i).getAsString());
                            } catch (Exception e) {
                                Log.error("Not able to parse String to Number for " + key + " : " + entry.getValue().getAsJsonArray().get(i).getAsString());
                                Log.error(e.getMessage());
                            }
                            if (num != null){
                                tmpArray.add(num);
                            }
                        }else{
                            Log.warn("Didn't recognized type of data in an array. Going to put null value!");
                            tmpArray.add(null);
                        }
                    }
                }
                map.put(key, tmpArray);
            }else if(entry.getValue().isJsonObject()){
                HashMap<String, Object> tMap = parseObject(entry.getValue().getAsJsonObject());
                map.put(key, tMap);
            }
            else {
                Log.warn("Didn't recognized type of object data. Going to put null value!");
                map.put(key, null);
            }
        }
        return map;
    }

}
