package modules.core;

public class BasePage {

    protected SharedContext ctx;
    protected PropertyReader Environment;
    protected StepCore StepCore;
    protected PageCore PageCore;
    protected SqlCore SqlCore;
    protected Storage Storage;
    protected FileCore FileCore;
    protected AssertCore AssertCore;

    // PicoContainer injects class SharedContext
    public BasePage (SharedContext ctx) {
        this.ctx = ctx;
        this.Environment = ctx.Object.get("Environment",PropertyReader.class);
        this.StepCore = ctx.Object.get("StepCore",StepCore.class);
        this.PageCore  = ctx.Object.get("PageCore",PageCore.class);
        this.SqlCore = ctx.Object.get("SqlCore",SqlCore.class);
        this.Storage = ctx.Object.get("Storage", Storage.class);
        this.FileCore = ctx.Object.get("FileCore",FileCore.class);
        this.AssertCore = ctx.Object.get("AssertCore", AssertCore.class);

        PageCore.waitForPageToLoad();
    }

    public Boolean isLoaded(String pageTitle){
        return PageCore.titleContains(pageTitle);
    }

}
