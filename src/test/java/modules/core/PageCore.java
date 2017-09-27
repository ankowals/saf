package modules.core;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PageCore {

    protected SharedContext ctx;
    protected PropertyReader Environment;
    protected EventFiringWebDriver Page;

    // PicoContainer injects class SharedContext
    public PageCore (SharedContext ctx) {
        this.ctx = ctx;
        this.Environment = ctx.Object.get("Environment",PropertyReader.class);
        this.Page = ctx.Object.get("Page",EventFiringWebDriver.class);
    }

    /**
     * Checks if page title contains specified string
     *
     * @param pageTitle title of the page
     * @return boolean
     */
    public Boolean titleContains(String pageTitle){
        if(Page.getTitle().contains(pageTitle)){
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
    public void waitUntilTitleContains(String pageTitle) {
        Log.debug("Going to wait for page load and check title");
        Integer timeout = Integer.parseInt(Environment.readProperty("browser_timeout"));
        WebDriverWait wait = new WebDriverWait(Page, timeout, 1000);
        try {
            wait.until(ExpectedConditions.titleContains(pageTitle));
        } catch (TimeoutException e) {
            Log.error("", e);
        }
    }

    /**
     * Executes js to check if page is loaded.
     * Does this in a loop and check document.readState every 1 second.
     */
    public void waitForPageToLoad() {
        Log.debug("Going to wait for page load");
        Integer timeOutInSeconds = Integer.parseInt(Environment.readProperty("browser_timeout"));
        JavascriptExecutor js = Page;
        String jsCommand = "return document.readyState";
        // Validate readyState before doing any waits
        if (js.executeScript(jsCommand).toString().equals("complete")) {
            return;
        }

        for (int i = 0; i < timeOutInSeconds; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.error( "", e );
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
        Log.debug("Going to wait for an element identified " + locator.toString() + " to be present");
        Integer seconds = Integer.parseInt(Environment.readProperty("browser_timeout"));
        WebDriverWait wait = new WebDriverWait(Page, seconds);
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (TimeoutException e) {
            Log.error("", e);
        }
    }

    /**
     * Waits for the element to be visible in the viewport and web driver can interact with it.
     *
     * @param locator web element locator, usually css selector or xpath
     *
     */
    public void waitForElementToBeVisible(By locator) {
        Log.debug("Going to wait for an element identified " + locator.toString() + " to be visible");
        Integer seconds = Integer.parseInt(Environment.readProperty("browser_timeout"));
        WebDriverWait wait = new WebDriverWait(Page, seconds);
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            Log.error("", e);
        }
    }


    /**
     * Waits for an element to not be visible or present. Waits for max 1 second.
     * Implicit wait timer is ignored.
     *
     * @param locator web element locator, usually css selector or xpath
     *
     */
    public void waitForElementToBeRemoved(By locator) {
        Log.debug("Going to wait for an element identified " + locator.toString() + " to be removed");
        turnOffImplicitWaits();
        WebDriverWait wait = new WebDriverWait(Page, 1);
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            Log.error("", e);
        }
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
            result = ExpectedConditions.invisibilityOfElementLocated(locator).apply(Page);
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
        Page.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
    }

    /**
     * Turns on implicit wait timer for web driver
     * helper function
     */
    private void turnOnImplicitWaits() {
        Integer seconds = Integer.parseInt(Environment.readProperty("browser_timeout"));
        Page.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
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
        Log.debug("Going to wait for an element identified " + byLocator.toString() + " to be present with timeout " + maxWaitTime);
        if (Page == null) {
            Log.error("Web Driver not started or null!");
        }
        FluentWait<EventFiringWebDriver> wait = new FluentWait<>(Page).withTimeout(maxWaitTime, TimeUnit.SECONDS)
                .pollingEvery(200, TimeUnit.MILLISECONDS);
        try {
            return wait.until((EventFiringWebDriver webDriver) -> {
                List<WebElement> elems = Page.findElements(byLocator);
                if (elems.size() > 0) {
                    return elems.get(0);
                } else {
                    Log.warn("Element identified " + byLocator + " not found");
                    return null;
                }
            });
        } catch (Exception e) {
            Log.warn("Timeout! Element identified " + byLocator + " not found");
            return null;
        }
    }

    public WebElement findElement(By locator) {

        ExecutionTimer t_FindBy = new ExecutionTimer();
        Log.debug("Looking for an element identified " + locator);
        WebElement element = Page.findElement(locator);
        t_FindBy.end();
        Log.debug("Element found after " + t_FindBy.duration()  + " ms");

        return element;
    }

    public List<WebElement> findElements(By locator) {

        Integer count = 0;

        ExecutionTimer t_FindBy = new ExecutionTimer();
        Log.debug("Looking for elements identified " + locator);
        List<WebElement> elements = Page.findElements(locator);
        t_FindBy.end();
        if (elements != null ) {
            count = elements.size();
        }
        Log.debug(count + " elements found after " + t_FindBy.duration()  + " ms");

        return elements;
    }

    public String getTitle() {
        return Page.getTitle();
    }

    public void open(String url) {
        try {
            Page.get(url);
        } catch (Exception e) {
            Log.error("", e);
        }
    }

    public void close() {
        try {
            Page.close();
        } catch (Exception e) {
            Log.error("", e);
        }
    }

    public String getCurrentUrl() {
        return Page.getCurrentUrl();
    }

    public void maximizeWindow () {
        Page.manage().window().maximize();
        Log.debug("Window maximized");
    }

    public Object executeJs (String script) {

        Log.debug("Going to execute js");
        ExecutionTimer t_FindBy = new ExecutionTimer();
        Object result = Page.executeScript(script);
        t_FindBy.end();
        Log.debug("Js execution done in " + t_FindBy.duration()  + " ms");

        return result;
    }

    public void dragAndDrop (WebElement from, WebElement to) {

        Actions builder = new Actions(Page);
        Action dragAndDrop = builder.clickAndHold(from)
                                    .moveToElement(to)
                                    .release(to)
                                    .build();
        dragAndDrop.perform();
    }

    public void doubleClick (WebElement element) {

        Actions builder = new Actions(Page);
        Action doubleClick = builder.moveToElement(element)
                                    .doubleClick()
                                    .build();

        doubleClick.perform();
    }

    public void hoverOver (WebElement element) {

        Actions builder = new Actions(Page);
        Action hoverOver = builder.moveToElement(element).build();

        hoverOver.perform();
    }

    public void hoverOverAndClick (WebElement element) {

        Actions builder = new Actions(Page);
        Action hoverOver = builder.moveToElement(element).click().build();

        hoverOver.perform();
    }

    public String getElementSource (WebElement element) {
        return (String)((JavascriptExecutor)Page).executeScript("return arguments[0].innerHTML;", element);
    }

    public void scrollTo (By locator) {
        WebElement element = Page.findElement(locator);
        ((JavascriptExecutor) Page).executeScript("arguments[0].scrollIntoView(true);", element);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Log.error( "", e );
        }
    }

    public void switchToIFrame (By locator) {
        WebElement iFrame = Page.findElement(locator);
        Page.switchTo().defaultContent(); // you are now outside any frame
        try {
            Page.switchTo().frame(iFrame);
        } catch (NoSuchFrameException e){
            Log.error( "", e );
        }

        Log.debug("Switched to iFrame located by " + locator);
    }

    public void switchToWindow(Integer id) {
        Set handles = Page.getWindowHandles();
        String[] individualHandle = new String[handles.size()];
        Iterator it = handles.iterator();
        int i = 0;
        while (it.hasNext()) {
            individualHandle[i] = (String) it.next();
            i++;
        }

        try {
            Page.switchTo().window(individualHandle[id]);
        } catch (NoSuchWindowException e){
            Log.error( "", e );
        }

        Page.switchTo().defaultContent();
        Log.debug("Switched to window identified by " + individualHandle[id]);
    }


    public void switchTab() {
        Page.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"\t");
        Page.switchTo().defaultContent();
        Log.debug("Switched to new tab");
    }

    public void openNewTab() {
        Page.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"t");
        Page.switchTo().defaultContent();
        Log.debug("New tab open");
    }

    public Alert waitForAlert() {
        Log.debug("Going to wait for an alert window");
        Integer seconds = Integer.parseInt(Environment.readProperty("browser_timeout"));
        WebDriverWait wait = new WebDriverWait(Page, seconds);
        try {
            wait.until(ExpectedConditions.alertIsPresent());
        } catch (TimeoutException e) {
            Log.error("", e);
        }

        return Page.switchTo().alert();
    }

    public byte[] takeScreenshot() {
        byte[] screenshot = null;
        try {
            screenshot = ((TakesScreenshot) Page).getScreenshotAs(OutputType.BYTES);
        } catch (WebDriverException e) {
            Log.error( "Screenshot can't be taken. Make sure that driver was started!", e );
        }

        return screenshot;
    }

}
