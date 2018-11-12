package libs.libCore.modules;

public class GlobalCtxSingleton {
    private static class StaticHolder {
        static final Context INSTANCE = new GlobalContext();
    }

    public static Context getInstance() {
        return StaticHolder.INSTANCE;
    }
}