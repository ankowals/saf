package modules.core;

import java.util.HashMap;
import java.util.Map;

public class Context {

    private final Map<String, Map<Class<?>, Object>> internalMap;

    public Context() {
        internalMap = new HashMap<>();
    }

    public <T> void put(String textKey, Class<T> typeKey, T value) {
        Map<Class<?>, Object> mapForTextKey = getMapForTextKey(textKey);
        mapForTextKey.put(typeKey, value);
        Log.debug("Ctx object " + textKey + " of type " + typeKey.toString() + " created or modified");
    }

    private Map<Class<?>, Object> getMapForTextKey(String textKey) {
        if (!internalMap.containsKey(textKey))
            internalMap.put(textKey, new HashMap<>());
        return internalMap.get(textKey);
    }

    public <T> T get(String textKey, Class<T> typeKey) {
        Object untypedValue = getUntyped(textKey, typeKey);
        // the cast can not fail because:
        // - if there is no value for those keys, the argument is null,
        //      which can always be cast
        // - if there is a value for those keys, `put` made sure that
        //      its type matches the type used as a key
        return typeKey.cast(untypedValue);
    }

    private Object getUntyped(String textKey, Class<?> typeKey) {
        if (internalMap.containsKey(textKey))
            return internalMap.get(textKey).get(typeKey);
        else
            Log.warn("Ctx obj with key " + textKey + " does not exists or null!");
            return null;
    }
}