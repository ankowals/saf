package libs.libCore.modules;

@SuppressWarnings("unchecked")
public class BasePage {

    protected Context globalCtx;
    protected Context scenarioCtx;
    protected StepCore StepCore;
    protected PageCore PageCore;
    protected SqlCore SqlCore;
    protected Storage Storage;
    protected FileCore FileCore;
    protected RestCore RestCore;
    protected CsvCore CsvCore;

    public BasePage () {
        this.globalCtx = GlobalCtxSingleton.getInstance();
        this.scenarioCtx = globalCtx.get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class).checkOut();
        this.StepCore = scenarioCtx.get("StepCore",StepCore.class);
        this.PageCore  = scenarioCtx.get("PageCore",PageCore.class);
        this.SqlCore = scenarioCtx.get("SqlCore",SqlCore.class);
        this.Storage = scenarioCtx.get("Storage", Storage.class);
        this.FileCore = scenarioCtx.get("FileCore",FileCore.class);
        this.RestCore = scenarioCtx.get("RestCore", RestCore.class);
        this.CsvCore = scenarioCtx.get("CsvCore", CsvCore.class);

        PageCore.waitForPageToLoad();
    }

    /**
     * Checks if Page was correctly loaded in the web browser by verifying page title
     *
     * @param pageTitle, String, expected title of the page
     * @return Boolean
     */
    public Boolean isLoaded(String pageTitle){
        return PageCore.titleContains(pageTitle);
    }

}