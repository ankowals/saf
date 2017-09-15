package modules.core;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.internal.BuildInfo;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.util.concurrent.TimeUnit;

public class DriverFactory {
    private SharedContext ctx;
    private  WebDriver dr;

    // PicoContainer injects class SharedContext
    public DriverFactory (SharedContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Creates web driver based on the browser name defined in configuration
     * Sets implicit web driver timer
     *
     * Uses following objects:
     *  env.browser
     *  env.path_to_browser_driver
     *  env.browser_timeout
     *
     * @return web driver instance
     *
     */
    public  EventFiringWebDriver create(){
        Log.debug("Going to create new driver");
        String browser = ctx.env.readProperty("browser");
        if (browser.equalsIgnoreCase("chrome")) {
            String path = ctx.env.readProperty("path_to_chrome_driver");
            System.setProperty("webdriver.chrome.driver", path);
            dr = new ChromeDriver();
        } else {
            Log.fatal("Can't read browser type or wrong name provided.");
        }

        Log.info("Driver name used is " + dr.getClass().getName());
        BuildInfo bd = new BuildInfo();
        Log.info("Driver build revision " + bd.getBuildRevision());
        Log.info("Driver build time " + bd.getBuildTime());
        Log.info("Driver build release label " + bd.getReleaseLabel());

        EventFiringWebDriver driver = new EventFiringWebDriver(dr);
        EventHandler handler = new EventHandler();
        driver.register(handler);

        driver.manage().deleteAllCookies();
        driver.manage().window().maximize();
        Integer timeout = Integer.parseInt(ctx.env.readProperty("browser_timeout"));
        driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
        Dimension brDim = driver.manage().window().getSize();
        Log.info("Browser width x height is " + brDim.getWidth() + " x " + brDim.getHeight());

        return driver;
    }
}
