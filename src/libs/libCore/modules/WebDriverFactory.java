package libs.libCore.modules;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.BuildInfo;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WebDriverFactory {

    private Context scenarioCtx;
    private Storage Storage;
    private FileCore FileCore;

    public WebDriverFactory() {
        this.scenarioCtx = GlobalCtxSingleton.getInstance().get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class).checkOut();
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
     *  @return EventFiringWebDriver
     */
    public EventFiringWebDriver create(String browser){
        Log.debug("Going to create new web browser driver");

        if (browser.equalsIgnoreCase("chrome")) {
            String path = Storage.get("Environment.Active.WebDrivers.Chrome.path");
            System.setProperty("webdriver.chrome.driver", FileCore.getProjectPath() + File.separator + path);
            System.setProperty("webdriver.chrome.verboseLogging", "false");

            ChromeOptions options = new ChromeOptions();

            //Disable extensions and hide infobars
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-infobars");

            Map<String, Object> prefs = new HashMap<>();

            //Enable Flash
            prefs.put("profile.default_content_setting_values.plugins", 1);
            prefs.put("profile.content_settings.plugin_whitelist.adobe-flash-player", 1);
            prefs.put("profile.content_settings.exceptions.plugins.*,*.per_resource.adobe-flash-player", 1);

            //Hide save credentials prompt
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            options.setExperimentalOption("prefs", prefs);
            options.setExperimentalOption("useAutomationExtension", false);

            //add support for Selenium Grid
            boolean isGridUsed = Storage.get("Environment.Active.Selenium.useGrid");
            if ( isGridUsed ){
                String hub = Storage.get("Environment.Active.Selenium.hub");

                DesiredCapabilities capabilities = DesiredCapabilities.chrome();
                capabilities.setCapability(ChromeOptions.CAPABILITY, options);
                capabilities.setBrowserName("chrome");

                try{
                    Log.debug("Going to use Selenium Grid");
                    return createEventFiringWebDriver(new RemoteWebDriver(new URL(hub), capabilities));
                } catch (MalformedURLException e) {
                    Log.error(e.getMessage());
                }
            }

            return createEventFiringWebDriver(new ChromeDriver(options));
        } else if (browser.equalsIgnoreCase("firefox")) {
            String path = Storage.get("Environment.Active.WebDrivers.FireFox.path");
            System.setProperty("webdriver.firefox.marionette", FileCore.getProjectPath() + File.separator + path);

            //add support for Selenium Grid
            boolean isGridUsed = Storage.get("Environment.Active.Selenium.useGrid");
            if ( isGridUsed ) {
                String hub = Storage.get("Environment.Active.Selenium.hub");

                DesiredCapabilities capabilities = DesiredCapabilities.firefox();
                capabilities.setBrowserName("firefox");

                try{
                    Log.debug("Going to use Selenium Grid");
                    return createEventFiringWebDriver(new RemoteWebDriver(new URL(hub), capabilities));
                } catch (MalformedURLException e) {
                    Log.error(e.getMessage());
                }
            }

            return createEventFiringWebDriver(new FirefoxDriver());
        } else if (browser.equalsIgnoreCase("ie")) {
            String path = Storage.get("Environment.Active.WebDrivers.InternetExplorer.path");
            System.setProperty("webdriver.ie.driver", FileCore.getProjectPath() + File.separator + path);

            //add support for Selenium Grid
            boolean isGridUsed = Storage.get("Environment.Active.Selenium.useGrid");
            if ( isGridUsed ) {
                String hub = Storage.get("Environment.Active.Selenium.hub");

                DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
                capabilities.setBrowserName("ie");

                try{
                    Log.debug("Going to use Selenium Grid");
                    return createEventFiringWebDriver(new RemoteWebDriver(new URL(hub), capabilities));
                } catch (MalformedURLException e) {
                    Log.error(e.getMessage());
                }
            }

            return createEventFiringWebDriver(new InternetExplorerDriver());
        } else {
            Log.error( "Can't read browser type or wrong name provided." +
                    "Supported browser types are: chrome, firefox, ie" );
        }

        return null;
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
     *  @param driver WebDriver
     *
     *  @retrun EventFiringWebDriver
     */
    private EventFiringWebDriver createEventFiringWebDriver(WebDriver driver) {
        Log.debug("Driver name used is " + driver.getClass().getName());

        BuildInfo bd = new BuildInfo();
        Log.debug("Driver build revision " + bd.getBuildRevision());
        Log.debug("Driver build time " + bd.getBuildTime());
        Log.debug("Driver build release label " + bd.getReleaseLabel());

        EventFiringWebDriver eventFiringWebDriver = new EventFiringWebDriver(driver);
        WebDriverEventListener handler = new WebDriverCustomEventListener();
        eventFiringWebDriver.register(handler);

        //we have to put it here because if something goes not ok
        //during browser manipulation driver won't be closed
        scenarioCtx.put("SeleniumWebDriver", EventFiringWebDriver.class, eventFiringWebDriver);

        Log.debug("Removing all cookies");
        eventFiringWebDriver.manage().deleteAllCookies();

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
            int width = 480;
            int height = 640;
            try {
                width = Integer.parseInt(dimensions[0]);
                height = Integer.parseInt(dimensions[1]);
            } catch (NumberFormatException e) {
                Log.error(e.getMessage());
            }
            Log.debug("Going to set browser window width " + width + " and height " + height);
            driver.manage().window().setSize(new Dimension(width, height));
        }

        Integer timeout = Storage.get("Environment.Active.Web.timeout");
        driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
        Dimension brDim = driver.manage().window().getSize();
        Log.debug("Browser width x height is " + brDim.getWidth() + " x " + brDim.getHeight());

        return eventFiringWebDriver;
    }


}