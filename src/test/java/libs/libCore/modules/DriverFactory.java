package libs.libCore.modules;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.internal.BuildInfo;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.util.concurrent.TimeUnit;

public class DriverFactory {

    private SharedContext ctx;
    private Storage Storage;
    private FileCore FileCore;
    private WebDriver dr;

    // PicoContainer injects class SharedContext
    public DriverFactory (SharedContext ctx) {
        this.ctx = ctx;
        this.Storage = ctx.Object.get("Storage", Storage.class);
        this.FileCore = ctx.Object.get("FileCore",FileCore.class);
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
        String browser = Storage.get("Environment.Active.Web.browser");
        if (browser.equalsIgnoreCase("chrome")) {
            String path = Storage.get("Environment.Active.WebDrivers.Chrome.path");
            System.setProperty("webdriver.chrome.driver", FileCore.getProjectPath() + "/" + path);
            dr = new ChromeDriver();
        } else if (browser.equalsIgnoreCase("firefox")) {
            String path = Storage.get("Environment.Active.WebDrivers.FireFox.path");
            System.setProperty("webdriver.gecko.driver", FileCore.getProjectPath() + "/" + path);

            DesiredCapabilities capabilities = DesiredCapabilities.firefox();
            capabilities.setCapability("marionette", true);

            dr = new FirefoxDriver(capabilities);
        } else if (browser.equalsIgnoreCase("ie")) {
            String path = Storage.get("Environment.Active.WebDrivers.InternetExplorer.path");
            System.setProperty("webdriver.ie.driver", FileCore.getProjectPath() + "/" + path);
            dr = new InternetExplorerDriver();
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

        Log.debug("Removing all cookies");
        driver.manage().deleteAllCookies();

        Log.debug("Setting browser width and height");
        String WidthXHeight = Storage.get("Environment.Active.Web.size");
        if ( WidthXHeight == null
                || WidthXHeight.equals("")
                || WidthXHeight.contains("M")
                || WidthXHeight.contains("m") ) {
            //default is set to maximise browser window
            Log.debug("Going to set max dimensions of browser window");
            driver.manage().window().maximize();
        } else {
            //expected format is width x height
            String tmp = StringUtils.deleteWhitespace(WidthXHeight).trim();
            String[] dimensions = tmp.split("[xX]");
            Integer width = null;
            Integer height = null;
            try {
                width = Integer.parseInt(dimensions[0]);
                height = Integer.parseInt(dimensions[1]);
            } catch (NumberFormatException e) {
                Log.error("", e);
            }
            Log.debug("Going to set browser window width " + width + " and height " + height);
            driver.manage().window().setSize(new Dimension(width, height));
        }

        Integer timeout = Storage.get("Environment.Active.Web.timeout");
        driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
        Dimension brDim = driver.manage().window().getSize();
        Log.info("Browser width x height is " + brDim.getWidth() + " x " + brDim.getHeight());

        return driver;
    }
}
