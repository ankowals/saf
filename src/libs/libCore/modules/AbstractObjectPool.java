package libs.libCore.modules;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractObjectPool<T> {

    protected Map<String, Set<T>> available;
    protected Map<String, Set<T>> inUse;

    protected abstract T create(String identifier);

    protected Boolean validate(T instance){
        if ( instance != null ){
            return true;
        }
        return false;
    }

    private void updateMap(Map<String, Set<T>> map, String identifier, T instance){
        if ( ! map.containsKey(identifier) ){
            Set<T> tmp = new HashSet<>();
            tmp.add(instance);
            map.put(identifier, tmp);
        } else {
            Set<T> tmp = map.get(identifier);
            tmp.add(instance);
        }
    }

    protected void close(T instance) {}

    public synchronized T checkOut(String identifier) {
        if ( available.isEmpty() || available.get(identifier).isEmpty() ) {
            T instance = create(identifier);
            updateMap(available, identifier, instance);
        }
        T instance = available.get(identifier).iterator().next();
        available.get(identifier).remove(instance);
        updateMap(inUse, identifier + "_" + Thread.currentThread().getName(), instance);

        return instance;
    }

    public synchronized void checkIn(String identifier, T instance) {
        inUse.get(identifier + "_" + Thread.currentThread().getName()).remove(instance);
        if ( validate(instance) ) {
            available.get(identifier).add(instance);
        }
    }

    public void closeAll(){
        for (Map.Entry<String, Set<T>> entry : inUse.entrySet()) {
            Set<T> tmp = entry.getValue();
            for (T instance : tmp) {
                close(instance);
            }
        }
        for (Map.Entry<String, Set<T>> entry : available.entrySet()) {
            Set<T> tmp = entry.getValue();
            for (T instance : tmp) {
                close(instance);
            }
        }
    }

    public synchronized void checkInAllPerThread(){
        for (Map.Entry<String, Set<T>> entry : inUse.entrySet()) {
            String key = entry.getKey();
            if ( key.contains(Thread.currentThread().getName()) ) {
                Set<T> instances = entry.getValue();
                for (T instance : instances) {
                    key = key.replace("_" + Thread.currentThread().getName(), "");
                    checkIn(key, instance);
                }
            }
        }
    }

    public synchronized void closeAllPerThread(){
        for (Map.Entry<String, Set<T>> entry : inUse.entrySet()) {
            String key = entry.getKey();
            if ( key.contains(Thread.currentThread().getName()) ) {
                Set<T> instances = entry.getValue();
                for (T instance : instances) {
                    close(instance);
                }
                inUse.remove(key);
            }
        }
        for (Map.Entry<String, Set<T>> entry : available.entrySet()) {
            String key = entry.getKey();
            if ( key.contains(Thread.currentThread().getName()) ) {
                Set<T> instances = entry.getValue();
                for (T instance : instances) {
                    close(instance);
                }
                available.remove(key);
            }
        }
    }

}