package libs.libCore.modules;

public class BaseSteps {

    protected Context globalCtx;
    protected Context scenarioCtx;
    protected Macro Macro;
    protected StepCore StepCore;
    protected PageCore PageCore;
    protected Storage Storage;
    protected FileCore FileCore;
    protected SshCore SshCore;
    protected ExecutorCore ExecutorCore;
    protected SqlCore SqlCore;
    protected CloudDirectorCore CloudDirectorCore;
    protected RestCore RestCore;
    protected PdfCore PdfCore;
    protected CsvCore CsvCore;

    public BaseSteps() {
        this.globalCtx = GlobalCtxSingleton.getInstance();
        this.scenarioCtx = globalCtx.get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class).checkOut();
        this.Macro = scenarioCtx.get("Macro",Macro.class);
        this.StepCore = scenarioCtx.get("StepCore",StepCore.class);
        this.PageCore = scenarioCtx.get("PageCore",PageCore.class);
        this.Storage = scenarioCtx.get("Storage", Storage.class);
        this.FileCore = scenarioCtx.get("FileCore", FileCore.class);
        this.SshCore = scenarioCtx.get("SshCore", SshCore.class);
        this.ExecutorCore = scenarioCtx.get("ExecutorCore", ExecutorCore.class);
        this.SqlCore = scenarioCtx.get("SqlCore",SqlCore.class);
        this.CloudDirectorCore = scenarioCtx.get("CloudDirectorCore", CloudDirectorCore.class);
        this.RestCore = scenarioCtx.get("RestCore", RestCore.class);
        this.PdfCore = scenarioCtx.get("PdfCore", PdfCore.class);
        this.CsvCore = scenarioCtx.get("CsvCore", CsvCore.class);
    }

}