package libs.libCore.modules;

import java.util.HashMap;
import java.util.Map;

public class Context {

    private final Map<String, Map<Class<?>, Object>> internalMap;

    public Context() {
        internalMap = new HashMap<>();
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
        Log.debug("Ctx object " + textKey + " of type " + typeKey.toString() + " created or modified");
    }

    /**
     * Gets internal map of object types for particular object name
     * helper function
     *
     * If object already exist its content will be updated else it will be created
     * There can be multiple object with the same name but of different types
     *
     * @param textKey object name
     * @return HashMap
     */
    private Map<Class<?>, Object> getMapForTextKey(String textKey) {
        if (!internalMap.containsKey(textKey))
            internalMap.put(textKey, new HashMap<>());
        return internalMap.get(textKey);
    }

    /**
     * Retrieves object of particular type and name and does cast types
     *
     * @param textKey object name
     * @param typeKey object type
     *
     * @return object from context
     */
    public <T> T get(String textKey, Class<T> typeKey) {
        Object untypedValue = getUntyped(textKey, typeKey);
        // the cast can not fail because:
        // - if there is no value for those keys, the argument is null,
        //      which can always be cast
        // - if there is a value for those keys, `put` made sure that
        //      its type matches the type used as a key
        return typeKey.cast(untypedValue);
    }


    /**
     * Retrieves object with particular name and type
     * helper function
     *
     * @param textKey object name
     * @return HashMap
     */
    private Object getUntyped(String textKey, Class<?> typeKey) {
        if (internalMap.containsKey(textKey)) {
            return internalMap.get(textKey).get(typeKey);
        } else {
            Log.warn("Ctx obj with key " + textKey + " does not exists or null!");
            return null;
        }
    }

    //public void clear () {
    //    internalMap.clear();
    //}

}