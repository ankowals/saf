package libs.libCore.modules;

import java.util.concurrent.ConcurrentHashMap;

public class GlobalContext extends Context {

    GlobalContext(){
        this.internalMap = new ConcurrentHashMap<>();
    }

}
