package libs.libCore.modules;

@SuppressWarnings("unchecked")
public class BaseSteps {

    protected Context scenarioCtx;
    protected Context globalCtx;
    protected Macro Macro;
    protected StepCore StepCore;
    protected PageCore PageCore;
    protected SqlCore SqlCore;
    protected Storage Storage;
    protected FileCore FileCore;
    protected ExecutorCore ExecutorCore;
    protected AssertCore AssertCore;
    protected PdfCore PdfCore;
    protected SshCore SshCore;
    protected WinRMCore WinRMCore;
    protected WinRSCore WinRSCore;
    protected CloudDirectorCore CloudDirectorCore;
    protected WiniumCore WiniumCore;

    public BaseSteps ()  {
        this.scenarioCtx = ThreadContext.getContext("Scenario");
        this.globalCtx = ThreadContext.getContext("Global");
        this.Macro = scenarioCtx.get("Macro",Macro.class);
        this.StepCore = scenarioCtx.get("StepCore",StepCore.class);
        this.PageCore = scenarioCtx.get("PageCore",PageCore.class);
        this.SqlCore = scenarioCtx.get("SqlCore",SqlCore.class);
        this.Storage = scenarioCtx.get("Storage", Storage.class);
        this.FileCore = scenarioCtx.get("FileCore",FileCore.class);
        this.ExecutorCore = scenarioCtx.get("ExecutorCore", ExecutorCore.class);
        this.AssertCore = scenarioCtx.get("AssertCore", AssertCore.class);
        this.PdfCore = scenarioCtx.get("PdfCore", PdfCore.class);
        this.SshCore = scenarioCtx.get("SshCore", SshCore.class);
        this.WinRMCore = scenarioCtx.get("WinRMCore", WinRMCore.class);
        this.WinRSCore = scenarioCtx.get("WinRSCore", WinRSCore.class);
        this.CloudDirectorCore = scenarioCtx.get("CloudDirectorCore", CloudDirectorCore.class);
        this.WiniumCore = scenarioCtx.get("WiniumCore", WiniumCore.class);
    }

}