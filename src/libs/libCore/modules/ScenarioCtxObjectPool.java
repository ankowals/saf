package libs.libCore.modules;

import java.util.HashMap;
import java.util.Map;

public class ScenarioCtxObjectPool {

    private Map<String, Context> available = new HashMap<>();

    private String getThreadName(){
        String name = Thread.currentThread().getName();

        return name;
    }

    private Context create(){
        return new ScenarioContext();
    }

    public synchronized Context checkOut(){
        if ( ! available.containsKey(getThreadName()) ) {
            available.put(getThreadName(), create());
        }
        return available.get(getThreadName());
    }

    public synchronized void checkIn(){
        available.remove(getThreadName());
    }

}