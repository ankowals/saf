package libs.libCore.modules;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *    shared context that implements singelton pattern
 *
 */

public class ExecutionContext {

        private static volatile ExecutionContext instance = null;
        private Map<String, Map<Class<?>, Object>> internalMap;

        private ExecutionContext() {
            internalMap = new HashMap<>();
        }

        public static ExecutionContext executionContextObject() {
            // Lazy and thread-safe
            if (instance == null) {
                synchronized(ExecutionContext.class) {
                    if (instance == null) {
                        instance = new ExecutionContext();
                    }
                }
            }

            return instance;
        }

    /**
     * Puts an object into the context in a form of HashMap
     *
     * @param textKey String, object name
     * @param typeKey Class, object type
     * @param value Generic, object value
     */
    public <T> void put(String textKey, Class<T> typeKey, T value) {
        Map<Class<?>, Object> mapForTextKey = getMapForTextKey(textKey);
        mapForTextKey.put(typeKey, value);
        Log.debug("Execution ctx object " + textKey + " of type " + typeKey.toString() + " created or modified");
    }

    private Map<Class<?>, Object> getMapForTextKey(String textKey) {
        if (!internalMap.containsKey(textKey))
            internalMap.put(textKey, new HashMap<>());
        return internalMap.get(textKey);
    }

    /**
     * Retrieves object of particular type and name unchecked cast is done
     *
     * @param textKey object name
     * @param typeKey object type
     *
     * @return object from context
     */
    public <T> T get(String textKey, Class<?> typeKey) {
        if (internalMap.containsKey(textKey)) {
            T result = null;
            try {
                result = (T) internalMap.get(textKey).get(typeKey);
            } catch (ClassCastException e) {
                Log.error("", e);
            }
            return result;
        } else {
            Log.warn("Execution ctx obj with key " + textKey + " does not exists or null!");
            return null;
        }
    }


    /**
     * Sets type of particular object
     *
     * @param type object name
     *
     * @return Class
     */
    public Class<?> setType(String type) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(type);
        } catch (ClassNotFoundException e) {
            Log.error("", e);
        }

        return clazz;
    }

}
