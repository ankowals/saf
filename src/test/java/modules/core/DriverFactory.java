package modules.core;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.internal.BuildInfo;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.util.concurrent.TimeUnit;

public class DriverFactory {

    private SharedContext ctx;
    private PropertyReader Environment;
    private WebDriver dr;

    // PicoContainer injects class SharedContext
    public DriverFactory (SharedContext ctx) {
        this.ctx = ctx;
        this.Environment = ctx.Object.get("Environment",PropertyReader.class);
    }

    /**
     * Creates web driver based on the browser name defined in configuration
     *
     * Uses following objects:
     *  env.browser
     *  env.path_to_browser_driver
     */
    public EventFiringWebDriver create(){
        Log.debug("Going to create new driver");
        String browser = Environment.readProperty("browser");
        if (browser.equalsIgnoreCase("chrome")) {
            String path = Environment.readProperty("path_to_chrome_driver");
            System.setProperty("webdriver.chrome.driver", path);
            dr = new ChromeDriver();
        } else {
            Log.error( "Can't read browser type or wrong name provided." +
                            "Supported browser types are: chrome" );
        }

        EventFiringWebDriver driver = EventFiringWebDriver(dr);
        //ctx.Object.put("Page", EventFiringWebDriver.class, driver);
        return driver;
    }

    /**
     * Creates EventFiringWebDriver. This gives us possibility to use EventHandler class
     * for much more detailed logging.
     * helper function
     * Sets implicit web driver timer
     *
     * Uses following objects:
     *  env.browser_timeout
     *
     *  @param dr web driver
     *  @retrun driver
     */
    private EventFiringWebDriver EventFiringWebDriver(WebDriver dr) {
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
        Integer timeout = Integer.parseInt(Environment.readProperty("browser_timeout"));
        driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
        Dimension brDim = driver.manage().window().getSize();
        Log.info("Browser width x height is " + brDim.getWidth() + " x " + brDim.getHeight());

        return driver;
    }
}
