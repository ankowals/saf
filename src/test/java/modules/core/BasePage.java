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

    public void checkStatus(){
        jsWaitForPageToLoad();
    }

    public Boolean titleContains(String pageTitle){
        if(ctx.driver.getTitle().contains(pageTitle)){
            return true;
        }else{
            return false;
        }
    }

    public void waitForPageLoadAndTitleContains(String pageTitle) {
        Log.debug("Going to wait for page load and check title");
        Integer timeout = Integer.parseInt(ctx.env.readProperty("browser_timeout"));
        WebDriverWait wait = new WebDriverWait(ctx.driver, timeout, 1000);
        wait.until(ExpectedConditions.titleContains(pageTitle));
    }

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

    public void waitForElementToBePresent(By locator) {
        Log.debug("Going to wait an element " + locator.toString() + " to be present");
        Integer seconds = Integer.parseInt(ctx.env.readProperty("browser_timeout"));
        WebDriverWait wait = new WebDriverWait(ctx.driver, seconds);
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public void waitForElementToBeVisible(By locator) {
        Log.debug("Going to wait for an element " + locator.toString() + " to be visible");
        Integer seconds = Integer.parseInt(ctx.env.readProperty("browser_timeout"));
        WebDriverWait wait = new WebDriverWait(ctx.driver, seconds);
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }



    public void waitForElementToBeRemoved(By locator) {
        Log.debug("Going to wait for an element " + locator.toString() + " to be removed");
        turnOffImplicitWaits();
        WebDriverWait wait = new WebDriverWait(ctx.driver, 10);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        turnOnImplicitWaits();
    }

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

    private void turnOffImplicitWaits() {
        ctx.driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
    }

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
