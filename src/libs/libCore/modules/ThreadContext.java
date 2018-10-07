package libs.libCore.modules;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// add possibility to assign name to the context like feature+scenario to identify them in a unique way

public class ThreadContext {

    protected static Map<String, HashMap<String, Context>> threadContexts =
            new ConcurrentHashMap<>();

    private static String types[] = {"Global", "Scenario"};

    private static void validateType(String type){
        // Convert String Array to List
        List<String> list = Arrays.asList(types);
        if ( !list.contains(type) ){
            Log.error("Not supported context type! Please use one of Global or Scenario!");
        }
    }

    private static String getThreadNameAndId(String type){
        validateType(type);

        String name = Thread.currentThread().getName();
        String id = Long.toString(Thread.currentThread().getId());

        if (type.equals("Global")){
             return "main_1";
        }

        return name + "_" + id;
    }

    public static void initializeContext(String type){
        HashMap<String, Context> ctxMap = threadContexts.get(getThreadNameAndId(type));
        if ( ctxMap == null ){
            ctxMap = new HashMap<>();
        }
        ctxMap.put(type, new Context());
        threadContexts.put(getThreadNameAndId(type),ctxMap);
        Log.info("New thread context od type " + type + " created for thread with id " + getThreadNameAndId(type));
    }

    public static Context getContext(String type){
        HashMap<String, Context> ctxMap = threadContexts.get(getThreadNameAndId(type));
        Log.info("Thread context of type " + type + " retrieved for thread with id " + getThreadNameAndId(type));
        return ctxMap.get(type);
    }

    public static void removeContext(String type){
        HashMap<String, Context> ctxMap = threadContexts.get(getThreadNameAndId(type));
        ctxMap.remove(type);
        Log.info("Thread context of type " + type + " removed for thread with id " + getThreadNameAndId(type));
    }

}