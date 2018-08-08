package libs.libCore.modules;

public class BaseApp {

    protected SharedContext ctx;
    protected StepCore StepCore;
    protected WiniumCore WiniumCore;
    protected SqlCore SqlCore;
    protected Storage Storage;
    protected FileCore FileCore;
    protected AssertCore AssertCore;
    protected WinRMCore WinRMCore;
    protected WinRSCore WinRSCore;

    // PicoContainer injects class SharedContext
    public BaseApp (SharedContext ctx) {
        this.ctx = ctx;
        this.StepCore = ctx.Object.get("StepCore",StepCore.class);
        this.SqlCore = ctx.Object.get("SqlCore",SqlCore.class);
        this.Storage = ctx.Object.get("Storage", Storage.class);
        this.FileCore = ctx.Object.get("FileCore",FileCore.class);
        this.AssertCore = ctx.Object.get("AssertCore", AssertCore.class);
        this.WinRMCore = ctx.Object.get("WinRMCore", WinRMCore.class);
        this.WinRSCore = ctx.Object.get("WinRSCore", WinRSCore.class);
        this.WiniumCore = ctx.Object.get("WiniumCore", WiniumCore.class);

        WiniumCore.getSessionId();
    }

}