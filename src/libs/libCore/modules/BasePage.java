package libs.libCore.modules;

@SuppressWarnings("unchecked")
public class BasePage {

    protected Context scenarioCtx;
    protected Context globalCtx;
    protected StepCore StepCore;
    protected PageCore PageCore;
    protected SqlCore SqlCore;
    protected Storage Storage;
    protected FileCore FileCore;
    protected AssertCore AssertCore;

    public BasePage () {
        this.scenarioCtx = ThreadContext.getContext("Scenario");
        this.globalCtx = ThreadContext.getContext("Global");
        this.StepCore = scenarioCtx.get("StepCore",StepCore.class);
        this.PageCore  = scenarioCtx.get("PageCore",PageCore.class);
        this.SqlCore = scenarioCtx.get("SqlCore",SqlCore.class);
        this.Storage = scenarioCtx.get("Storage", Storage.class);
        this.FileCore = scenarioCtx.get("FileCore",FileCore.class);
        this.AssertCore = scenarioCtx.get("AssertCore", AssertCore.class);

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