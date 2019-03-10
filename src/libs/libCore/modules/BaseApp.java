package libs.libCore.modules;

@SuppressWarnings("unchecked")
public class BaseApp {

    protected Context scenarioCtx;
    protected Context globalCtx;
    protected StepCore StepCore;
    protected WiniumCore WiniumCore;
    protected SqlCore SqlCore;
    protected Storage Storage;
    protected FileCore FileCore;
    protected WinRSCore WinRSCore;
    protected StringCore StringCore;

    public BaseApp () {
        this.globalCtx = GlobalCtxSingleton.getInstance();
        this.scenarioCtx = globalCtx.get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class).checkOut();
        this.StepCore = scenarioCtx.get("StepCore",StepCore.class);
        this.StringCore = scenarioCtx.get("StingCore",StringCore.class);
        this.SqlCore = scenarioCtx.get("SqlCore",SqlCore.class);
        this.Storage = scenarioCtx.get("Storage", Storage.class);
        this.FileCore = scenarioCtx.get("FileCore",FileCore.class);
        this.WinRSCore = scenarioCtx.get("WinRSCore", WinRSCore.class);
        this.WiniumCore = scenarioCtx.get("WiniumCore", WiniumCore.class);

        WiniumCore.getSessionId();
    }

}
