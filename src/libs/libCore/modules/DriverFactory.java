package libs.libCore.modules;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.BuildInfo;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Helper class.
 * Used to start selenium web driver by providing a path to it.
 */

@SuppressWarnings("unchecked")
public class DriverFactory {

    private Context scenarioCtx;
    private Storage Storage;
    private FileCore FileCore;
    private WebDriver dr;

    public DriverFactory () {
        this.scenarioCtx = ThreadContext.getContext("Scenario");
        this.Storage = scenarioCtx.get("Storage", Storage.class);
        this.FileCore = scenarioCtx.get("FileCore",FileCore.class);
    }

    /**
     * Creates web driver based on the browser name defined in configuration
     *
     * Uses following objects:
     *  Environment.Active.WebDrivers.Chrome.path
     *  Environment.Active.WebDrivers.FireFox.path
     *  Environment.Active.WebDrivers.InternetExplorer.path
     *
     *  @param browser String, browser name
     *
     *  @return EventFiringWebDriver
     */
    public EventFiringWebDriver create(String browser){
        Log.debug("Going to create new driver");

        if ( browser == null ) {
            Log.error("Browser type null or empty!");
        }

        if (browser.equalsIgnoreCase("chrome")) {
            String path = Storage.get("Environment.Active.WebDrivers.Chrome.path");
            System.setProperty("webdriver.chrome.driver", FileCore.getProjectPath() + File.separator + path);
            System.setProperty("webdriver.chrome.verboseLogging", "false");

            ChromeOptions options = new ChromeOptions();

            //Disable extensions and hide infobars
            options.addArguments("--disable-extnesions");
            options.addArguments("disbale-infobars");

            Map<String, Object> prefs = new HashMap<>();

            //Enable Flash
            prefs.put("profile.default_content_setting_values.plugins", 1);
            prefs.put("profile.content_settings.plugin_whitelist.adobe-flash-player", 1);
            prefs.put("profile.content_settings.exceptions.plugins.*,*.per_resource.adobe-flash-player", 1);

            //Hide save credentials prompt
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            options.setExperimentalOption("prefs", prefs);

            dr = new ChromeDriver(options);
        } else if (browser.equalsIgnoreCase("firefox")) {
            String path = Storage.get("Environment.Active.WebDrivers.FireFox.path");
            System.setProperty("webdriver.firefox.marionette", FileCore.getProjectPath() + File.separator + path);

            dr = new FirefoxDriver();
        } else if (browser.equalsIgnoreCase("ie")) {
            String path = Storage.get("Environment.Active.WebDrivers.InternetExplorer.path");
            System.setProperty("webdriver.ie.driver", FileCore.getProjectPath() + File.separator + path);
            dr = new InternetExplorerDriver();
        } else {
            Log.error( "Can't read browser type or wrong name provided." +
                            "Supported browser types are: chrome, firefox, ie" );
        }

        EventFiringWebDriver driver = EventFiringWebDriver(dr);

        return driver;
    }

    /**
     * Creates EventFiringWebDriver. This gives us possibility to use EventHandler class
     * for much more detailed logging.
     * helper function
     * Sets implicit web driver timer
     *
     * Uses following objects:
     *  Environment.Active.Web.timeout
     *
     *  @param dr WebDriver
     *
     *  @retrun EventFiringWebDriver
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

        //we have to put it here because if something goes not ok
        //during browser manipulation driver won't be closed
        scenarioCtx.put("Page", EventFiringWebDriver.class, driver);

        Log.debug("Removing all cookies");
        driver.manage().deleteAllCookies();

        Log.debug("Setting browser width and height");
        String WidthXHeight = Storage.get("Environment.Active.Web.size");
        if ( WidthXHeight == null
                || WidthXHeight.equals("")
                || StringUtils.containsIgnoreCase(WidthXHeight, "Max")) {
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
