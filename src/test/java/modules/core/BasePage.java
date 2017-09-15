package modules.core;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class BasePage {
    protected SharedContext ctx;

    // PicoContainer injects class SharedContext
    public BasePage (SharedContext ctx) {
        this.ctx = ctx;
        checkStatus();
    }

    /**
     * Checks if page is loaded using java script
     */
    public void checkStatus(){
        jsWaitForPageToLoad();
    }


    /**
     * Checks if page title contains specified string
     *
     * @param pageTitle title of the page
     * @return boolean
     *
     */
    public Boolean titleContains(String pageTitle){
        if(ctx.driver.getTitle().contains(pageTitle)){
            return true;
        }else{
            return false;
        }
    }


    /**
     * Waits for page to load anc checks if page title contains
     *
     * @param pageTitle title of the page
     *
     */
    public void waitForPageLoadAndTitleContains(String pageTitle) {
        Log.debug("Going to wait for page load and check title");
        Integer timeout = Integer.parseInt(ctx.env.readProperty("browser_timeout"));
        WebDriverWait wait = new WebDriverWait(ctx.driver, timeout, 1000);
        wait.until(ExpectedConditions.titleContains(pageTitle));
    }

    /**
     * Executes js to check if page is loaded.
     * Does this in a loop and check document.readState every 1 second.
     */
    public void jsWaitForPageToLoad() {
        Log.debug("Going to wait for page load");
        Integer timeOutInSeconds = Integer.parseInt(ctx.env.readProperty("browser_timeout"));
        JavascriptExecutor js = ctx.driver;
        String jsCommand = "return document.readyState";
        // Validate readyState before doing any waits
        if (ctx.driver.executeScript(jsCommand).toString().equals("complete")) {
            return;
        }

        for (int i = 0; i < timeOutInSeconds; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.error("Something went wrong during js wait for page load execution");
                Log.error(e.getMessage());
            }
            if (js.executeScript(jsCommand).toString().equals("complete")) {
                break;
            }
        }
    }

    /**
     * Waits for the element to be present in the DOM.
     *
     * @param locator web element locator, usually css selector or xpath
     *
     */
    public void waitForElementToBePresent(By locator) {
        Log.debug("Going to wait an element " + locator.toString() + " to be present");
        Integer seconds = Integer.parseInt(ctx.env.readProperty("browser_timeout"));
        WebDriverWait wait = new WebDriverWait(ctx.driver, seconds);
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Waits for the element to be visible in the viewport and web driver can interact with it.
     *
     * @param locator web element locator, usually css selector or xpath
     *
     */
    public void waitForElementToBeVisible(By locator) {
        Log.debug("Going to wait for an element " + locator.toString() + " to be visible");
        Integer seconds = Integer.parseInt(ctx.env.readProperty("browser_timeout"));
        WebDriverWait wait = new WebDriverWait(ctx.driver, seconds);
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }


    /**
     * Waits for an element to not be visible or present. Waits for max 1 second.
     * Implicit wait timer is ignored.
     *
     * @param locator web element locator, usually css selector or xpath
     *
     */
    public void waitForElementToBeRemoved(By locator) {
        Log.debug("Going to wait for an element " + locator.toString() + " to be removed");
        turnOffImplicitWaits();
        WebDriverWait wait = new WebDriverWait(ctx.driver, 1);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        turnOnImplicitWaits();
    }

    /**
     * Checks if element is removed and returns true or false.
     * Implicit wait timer is ignored.
     *
     * @param locator web element locator, usually css selector or xpath
     * @return boolean
     *
     */
    protected boolean checkIfElemnetIsRemoved(By locator) {
        turnOffImplicitWaits();
        boolean result = false;
        try {
            result = ExpectedConditions.invisibilityOfElementLocated(locator).apply(ctx.driver);
        }
        finally {
            turnOnImplicitWaits();
        }
        return result;
    }

    /**
     * Turns off implicit wait timer for web driver
     * helper function
     */
    private void turnOffImplicitWaits() {
        ctx.driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
    }

    /**
     * Turns on implicit wait timer for web driver
     * helper function
     */
    private void turnOnImplicitWaits() {
        Integer seconds = Integer.parseInt(ctx.env.readProperty("browser_timeout"));
        ctx.driver.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
    }

    /**
     * Looks for a visible OR invisible element via the provided locator for up
     * to maxWaitTime. Returns as soon as the element is found.
     *
     * @param byLocator
     * @param maxWaitTime - In seconds
     * @return
     *
     */
    public WebElement findElementWithFluentTimeout(final By byLocator, int maxWaitTime) {
        Log.debug("Going to wait for an element " + byLocator.toString() + " to be present with timeout " + maxWaitTime);
        if (ctx.driver == null) {
            Log.fatal("Driver not started or null!");
        }
        FluentWait<EventFiringWebDriver> wait = new FluentWait<>(ctx.driver).withTimeout(maxWaitTime, TimeUnit.SECONDS)
                .pollingEvery(200, TimeUnit.MILLISECONDS);
        try {
            return wait.until((EventFiringWebDriver webDriver) -> {
                List<WebElement> elems = ctx.driver.findElements(byLocator);
                if (elems.size() > 0) {
                    return elems.get(0);
                } else {
                    Log.error("Element " + byLocator + " not found");
                    return null;
                }
            });
        } catch (Exception e) {
            Log.error("Timeout! Element " + byLocator + " not found");
            return null;
        }
    }

}
