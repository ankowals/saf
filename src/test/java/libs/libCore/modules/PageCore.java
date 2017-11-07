package libs.libCore.modules;

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
    protected Storage Storage;
    protected EventFiringWebDriver Page;

    // PicoContainer injects class SharedContext
    public PageCore (SharedContext ctx) {
        this.ctx = ctx;
        this.Storage = ctx.Object.get("Storage", Storage.class);
        this.Page = ctx.Object.get("Page", EventFiringWebDriver.class);
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
        Integer timeout = Storage.get("Environment.Active.Web.timeout");
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
        Integer timeOutInSeconds = Storage.get("Environment.Active.Web.timeout");
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
        Integer seconds = Storage.get("Environment.Active.Web.timeout");
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
        Integer seconds = Storage.get("Environment.Active.Web.timeout");
        WebDriverWait wait = new WebDriverWait(Page, seconds);
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            Log.error("", e);
        }
    }


    /**
     * Waits for an element to not be visible or present. Waits for max of browser_timeout second.
     * Implicit wait timer is ignored.
     *
     * @param locator By, web element locator - usually css selector or xpath
     */
    public void waitForElementToBeRemoved(By locator) {
        Log.debug("Going to wait for an element identified " + locator.toString() + " to be removed");
        turnOffImplicitWaits();
        Integer timeOut = Storage.get("Environment.Active.Web.timeout");
        WebDriverWait wait = new WebDriverWait(Page, timeOut);
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
        Integer seconds = Storage.get("Environment.Active.Web.timeout");
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


    /**
     * Tries to find an element on the web page identified by locator like xpath, css or others
     *
     * @param locator By, locator used to identify web element
     * @return WebElement
     */
    public WebElement findElement(By locator) {

        ExecutionTimer t_FindBy = new ExecutionTimer();
        Log.debug("Looking for an element identified " + locator);
        WebElement element = Page.findElement(locator);
        t_FindBy.end();
        Log.debug("Element found after " + t_FindBy.duration()  + " ms");

        return element;
    }


    /**
     * Tries to find many elements on the web page identified by locator like xpath, css or others
     *
     * @param locator By, locator used to identify web element
     * @return List<WebElement>
     */
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


    /**
     * retrieves web page title
     *
     * @return String
     */
    public String getTitle() {
        return Page.getTitle();
    }


    /**
     * navigates to provided url in the current browser window
     *
     * @param url String
     */
    public void open(String url) {
        try {
            Page.get(url);
        } catch (Exception e) {
            Log.error("", e);
        }
    }


    /**
     * closes currently active web browser window or tab
     *
     */
    public void close() {
        try {
            Page.close();
        } catch (Exception e) {
            Log.error("", e);
        }
    }


    /**
     * retrieves current url of the web page open in the browser
     *
     * @return String
     */
    public String getCurrentUrl() {
        return Page.getCurrentUrl();
    }


    /**
     * makes browser window full width and high
     *
     */
    public void maximizeWindow () {
        Page.manage().window().maximize();
        Log.debug("Window maximized");
    }


    /**
     * executes provided java script on the web page
     *
     * @param script String, script that shall be executed
     * @return Object
     */
    public Object executeJs (String script) {

        Log.debug("Going to execute js");
        ExecutionTimer t_FindBy = new ExecutionTimer();
        Object result = Page.executeScript(script);
        t_FindBy.end();
        Log.debug("Js execution done in " + t_FindBy.duration()  + " ms");

        return result;
    }


    /**
     * simulates drag 'n drop action
     *
     * @param from WebElement, web element to drag (click and hold)
     * @param to WebElement, web element where to drop the previous one
     */
    public void dragAndDrop (WebElement from, WebElement to) {

        Actions builder = new Actions(Page);
        Action dragAndDrop = builder.clickAndHold(from)
                                    .moveToElement(to)
                                    .release(to)
                                    .build();
        dragAndDrop.perform();
    }


    /**
     * simulates double click on an element
     *
     * @param element WebElement, web element on which double click shall be performed
     */
    public void doubleClick (WebElement element) {

        Actions builder = new Actions(Page);
        Action doubleClick = builder.moveToElement(element)
                                    .doubleClick()
                                    .build();

        doubleClick.perform();
    }


    /**
     * simulates mouse hover action
     *
     * @param element WebElement, web element on which mouse hover shall be done
     */
    public void hoverOver (WebElement element) {

        Actions builder = new Actions(Page);
        Action hoverOver = builder.moveToElement(element).build();

        hoverOver.perform();
    }


    /**
     * simulates mouse hover action and clicks the element
     *
     * @param element WebElement, web element on which mouse hover shall be done and click performed
     */
    public void hoverOverAndClick (WebElement element) {

        Actions builder = new Actions(Page);
        Action hoverOver = builder.moveToElement(element).click().build();

        hoverOver.perform();
    }


    /**
     * retrieves html source code of specified element
     *
     * @param element WebElement
     * @return String
     */
    public String getElementSource (WebElement element) {
        return (String)((JavascriptExecutor)Page).executeScript("return arguments[0].innerHTML;", element);
    }


    /**
     * scrolls view port until particular element on the page
     *
     * @param locator By, locator of web element like xpath or css selector
     */
    public void scrollTo (By locator) {
        WebElement element = Page.findElement(locator);
        ((JavascriptExecutor) Page).executeScript("arguments[0].scrollIntoView(true);", element);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Log.error( "", e );
        }
    }


    /**
     * switches focus to particular iframe identified by locator like xpath or css selector
     *
     * @param locator By, locator of web element like xpath or css selector
     */
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


    /**
     * switches focus to particular browser window identified by numeric id
     *
     * @param id Integer, window identifier
     */
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


    /**
     * switches focus to next tab open in the browser
     * Simulates pressing of CTRL+Tab keys combination
     */
    public void switchTab() {
        Page.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"\t");
        Page.switchTo().defaultContent();
        Log.debug("Switched to new tab");
    }


    /**
     * opens new tab open in the browser
     * Simulates pressing of CTRL+t keys combination
     */
    public void openNewTab() {
        Page.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"t");
        Page.switchTo().defaultContent();
        Log.debug("New tab open");
    }


    /**
     * opens new browser window
     * Simulates pressing of CTRL+n keys combination
     */
    public void openNewWindow() {
        Page.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"n");
        Log.debug("New window open");
    }


    /**
     * waits for an Alert pop up
     * Simulates pressing of CTRL+t keys combination
     * Switches focus to the alert pop Up
     */
    public Alert waitForAlert() {
        Log.debug("Going to wait for an alert window");
        Integer seconds = Storage.get("Environment.Active.Web.timeout");
        WebDriverWait wait = new WebDriverWait(Page, seconds);
        try {
            wait.until(ExpectedConditions.alertIsPresent());
        } catch (TimeoutException e) {
            Log.error("", e);
        }

        return Page.switchTo().alert();
    }


    /**
     * takes screenshot of the web browser window
     */
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
