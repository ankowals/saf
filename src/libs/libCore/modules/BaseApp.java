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
    protected AssertCore AssertCore;
    protected WinRMCore WinRMCore;
    protected WinRSCore WinRSCore;

    public BaseApp () {
        this.scenarioCtx = ThreadContext.getContext("Scenario");
        this.globalCtx = ThreadContext.getContext("Global");
        this.StepCore = scenarioCtx.get("StepCore",StepCore.class);
        this.SqlCore = scenarioCtx.get("SqlCore",SqlCore.class);
        this.Storage = scenarioCtx.get("Storage", Storage.class);
        this.FileCore = scenarioCtx.get("FileCore",FileCore.class);
        this.AssertCore = scenarioCtx.get("AssertCore", AssertCore.class);
        this.WinRMCore = scenarioCtx.get("WinRMCore", WinRMCore.class);
        this.WinRSCore = scenarioCtx.get("WinRSCore", WinRSCore.class);
        this.WiniumCore = scenarioCtx.get("WiniumCore", WiniumCore.class);

        WiniumCore.getSessionId();
    }

}