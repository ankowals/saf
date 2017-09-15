package modules.core;

import org.openqa.selenium.support.events.EventFiringWebDriver;

public class DriverManger {

    //private  EventFiringWebDriver driver;
    private SharedContext ctx;

    // PicoContainer injects class SharedContext
    /**
     * Puts driver into the context
     *
     * @param ctx context
     */
    public DriverManger (SharedContext ctx) {
        this.ctx = ctx;
        ctx.driver = new DriverFactory(ctx).create();
    }

    /**
     * Retrieves particular driver instance
     * Sets implicit web driver timer
     *
     * @return web driver instance
     *
     */
    public EventFiringWebDriver getDriver() {
        return ctx.driver;
    }

}
