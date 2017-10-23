package libs.libCore.modules;

public class BaseSteps {

    protected SharedContext ctx;
    protected Macro Macro;
    protected StepCore StepCore;
    protected PageCore PageCore;
    protected SqlCore SqlCore;
    protected Storage Storage;
    protected FileCore FileCore;
    protected ExecutorCore ExecutorCore;
    protected AssertCore AssertCore;

    // PicoContainer injects class SharedContext
    public BaseSteps (SharedContext ctx) {
        this.ctx = ctx;
        this.Macro = ctx.Object.get("Macro",Macro.class);
        this.StepCore = ctx.Object.get("StepCore",StepCore.class);
        this.PageCore = ctx.Object.get("PageCore",PageCore.class);
        this.SqlCore = ctx.Object.get("SqlCore",SqlCore.class);
        this.Storage = ctx.Object.get("Storage", Storage.class);
        this.FileCore = ctx.Object.get("FileCore",FileCore.class);
        this.ExecutorCore = ctx.Object.get("ExecutorCore", ExecutorCore.class);
        this.AssertCore = ctx.Object.get("AssertCore", AssertCore.class);
    }

}
