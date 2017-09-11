package modules.core;

import org.openqa.selenium.support.events.EventFiringWebDriver;

public class DriverManger {

    //private  EventFiringWebDriver driver;
    private SharedContext ctx;

    // PicoContainer injects class SharedContext
    public DriverManger (SharedContext ctx) {
        this.ctx = ctx;
        ctx.driver = new DriverFactory(ctx).create();
    }

    public EventFiringWebDriver getDriver() {
        return ctx.driver;
    }

}
