package libs.libCore.modules;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public class PageCore {

    private Context scenarioCtx;
    private Storage Storage;
    private EventFiringWebDriver driver;

    //should depend on the driver to avoid null pointer exceptions!
    public PageCore () {
        this.scenarioCtx = GlobalCtxSingleton.getInstance().get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class).checkOut();
        this.Storage = scenarioCtx.get("Storage", Storage.class);
        this.driver = scenarioCtx.get("SeleniumWebDriver", EventFiringWebDriver.class);
    }

    /**
     * Returns driver. Can be null if not instantiated
     *
     * @return WebDriver
     */
    public WebDriver getDriver(){
        return driver;
    }


    /**
     * Checks if page title contains specified string
     *
     * @param pageTitle title of the page
     * @return boolean
     */
    public boolean titleContains(String pageTitle){
        return StringUtils.containsIgnoreCase(driver.getTitle(),pageTitle);
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
        WebDriverWait wait = new WebDriverWait(driver, timeout, 1000);
        try {
            wait.until(ExpectedConditions.titleContains(pageTitle));
        } catch (TimeoutException e) {
            Log.error(e.getMessage());
        }
    }


    /**
     * Executes js to check if page is loaded.
     * Does this in a loop and checks document.readState every 1 second.
     */
    public void waitForPageToLoad() {
        if ( driver == null ){
            Log.error("WebDriver null! Please make sure that step 'open browser' was executed!");
        }
        Log.debug("Going to wait for page load");
        Integer timeOutInSeconds = Storage.get("Environment.Active.Web.timeout");
        JavascriptExecutor js = driver;
        String jsCommand = "return document.readyState";
        // Validate readyState before doing any waits
        if (js.executeScript(jsCommand).toString().equals("complete")) {
            return;
        }

        for (int i = 0; i < timeOutInSeconds; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //don't do anything
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
        WebDriverWait wait = new WebDriverWait(driver, seconds);
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (TimeoutException e) {
            Log.error(e.getMessage());
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
        WebDriverWait wait = new WebDriverWait(driver, seconds);
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            Log.error(e.getMessage());
        }
    }


    /**
     * Waits for an element to be not visible. Waits for max of browser_timeout second.
     * Implicit wait timer is ignored.
     *
     * @param locator By, web element locator - usually css selector or xpath
     */
    public void waitForElementToBeNotVisible(By locator) {
        Log.debug("Going to wait for an element identified " + locator.toString() + " to be not visible");
        turnOffImplicitWaits();
        Integer timeOut = Storage.get("Environment.Active.Web.timeout");
        WebDriverWait wait = new WebDriverWait(driver, timeOut);
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            Log.error(e.getMessage());
        }
        turnOnImplicitWaits();
    }


    /**
     * Waits for an element to be not present. Waits for max of browser_timeout second.
     * Implicit wait timer is ignored.
     *
     * @param locator By, web element locator - usually css selector or xpath
     */
    public void waitForElementToBeNotPresent(By locator) {
        Log.debug("Going to wait for an element identified " + locator.toString() + " to be not present");
        turnOffImplicitWaits();
        Integer timeOut = Storage.get("Environment.Active.Web.timeout");
        WebDriverWait wait = new WebDriverWait(driver, timeOut);
        try {
            wait.until(ExpectedConditions.numberOfElementsToBe(locator,0));
        } catch (TimeoutException e) {
            Log.error(e.getMessage());
        }
        turnOnImplicitWaits();
    }


    /**
     * Checks if element is not visible and returns true or false.
     * Implicit wait timer is ignored.
     *
     * @param locator web element locator, usually css selector or xpath
     * @return boolean
     *
     */
    protected boolean checkIfElemnetIsNotVisible(By locator) {
        turnOffImplicitWaits();
        boolean result;
        try {
            result = ExpectedConditions.invisibilityOfElementLocated(locator).apply(driver);
        }
        finally {
            turnOnImplicitWaits();
        }
        return result;
    }


    /**
     * Checks if element is visible and returns true or false.
     * Implicit wait timer is ignored.
     *
     * @param locator web element locator, usually css selector or xpath
     * @return boolean
     *
     */
    protected boolean checkIfElemnetIsVisible(By locator) {
        turnOffImplicitWaits();
        boolean result = false;
        try {
            WebElement elem = ExpectedConditions.visibilityOfElementLocated(locator).apply(driver);
            if ( elem != null ) {
                result = true;
            }
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
        driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
    }


    /**
     * Turns on implicit wait timer for web driver
     * helper function
     */
    private void turnOnImplicitWaits() {
        Integer seconds = Storage.get("Environment.Active.Web.timeout");
        driver.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
    }


    /**
     * Tries to find an element on the web page identified by locator like xpath, css or others
     *
     * @param locator By, locator used to identify web element
     * @return WebElement
     */
    public WebElement findElement(By locator) {
        return driver.findElement(locator);
    }


    /**
     * Tries to find many elements on the web page identified by locator like xpath, css or others
     *
     * @param locator By, locator used to identify web element
     * @return List<WebElement>
     */
    public List<WebElement> findElements(By locator) {
        return driver.findElements(locator);
    }


    /**
     * retrieves web page title
     *
     * @return String
     */
    public String getTitle() {
        return driver.getTitle();
    }


    /**
     * Retrieves value of WebElement attribute<br>
     *     Returns an empty string in case of an exception
     *
     * @param locator By, locator used to identify web element
     * @param attribute String, name of the attribute of an element
     * @return String
     */
    public String getAttribute(By locator, String attribute) {
        WebElement elem = findElement(locator);
        try {
            Log.debug("Going to extract an attribute " + attribute + " of an element identified " + locator);
            return elem.getAttribute(attribute);
        } catch (StaleElementReferenceException e) {
            Log.warn(e.getMessage());
            return "";
        }
    }


    /**
     * navigates to provided url in the current browser window
     *
     * @param url String
     */
    public void open(String url) {
        try {
            driver.get(url);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
    }


    /**
     * closes currently active web browser window or tab
     *
     */
    public void close() {
        try {
            driver.close();
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
    }


    /**
     * retrieves current url of the web page open in the browser
     *
     * @return String
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }


    /**
     * makes browser window full width and high
     *
     */
    public void maximizeWindow () {
        driver.manage().window().maximize();
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
        Object result = driver.executeScript(script);
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
        ExecutionTimer t_FindBy = new ExecutionTimer();
        Actions builder = new Actions(driver);
        Action dragAndDrop = builder.clickAndHold(from)
                                    .moveToElement(to)
                                    .release(to)
                                    .build();
        dragAndDrop.perform();
        t_FindBy.end();
        Log.debug("Drag and drop action performed within " + t_FindBy.duration()  + " ms");
    }


    /**
     * simulates double click on an element
     *
     * @param element WebElement, web element on which double click shall be performed
     */
    public void doubleClick (WebElement element) {
        Actions builder = new Actions(driver);
        Action doubleClick = builder.moveToElement(element)
                                    .doubleClick()
                                    .build();

        doubleClick.perform();
        Log.debug("Double click performed");
    }


    /**
     * simulates mouse hover action
     *
     * @param element WebElement, web element on which mouse hover shall be done
     */
    public void hoverOver (WebElement element) {
        ExecutionTimer t_FindBy = new ExecutionTimer();
        Actions builder = new Actions(driver);
        Action hoverOver = builder.moveToElement(element).build();

        hoverOver.perform();
        t_FindBy.end();
        Log.debug("Mouse hover over " + element.toString() + " executed within " + t_FindBy.duration()  + " ms");
    }


    /**
     * simulates mouse hover action and clicks the element
     *
     * @param element WebElement, web element on which mouse hover shall be done and click performed
     */
    public void hoverOverAndClick (WebElement element) {
        ExecutionTimer t_FindBy = new ExecutionTimer();
        Actions builder = new Actions(driver);
        Action hoverOver = builder.moveToElement(element).click().build();

        hoverOver.perform();
        t_FindBy.end();
        Log.debug("Mouse hover over " + element.toString() + " and click executed within " + t_FindBy.duration()  + " ms");
    }


    /**
     * retrieves html source code of specified element
     *
     * @param element WebElement
     * @return String
     */
    public String getElementSource (WebElement element) {
        return (String)((JavascriptExecutor) driver).executeScript("return arguments[0].innerHTML;", element);
    }


    /**
     * scrolls view port until particular element on the page
     *
     * @param locator By, locator of web element like xpath or css selector
     */
    public void scrollTo (By locator) {
        ExecutionTimer t_FindBy = new ExecutionTimer();
        WebElement element = driver.findElement(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            //don't do anything
        }
        t_FindBy.end();
        Log.debug("Scroll to element identified " + locator.toString() + " executed within " + t_FindBy.duration()  + " ms");
    }


    /**
     * switches focus to particular iframe identified by locator like xpath or css selector
     *
     * @param locator By, locator of web element like xpath or css selector
     */
    public void switchToIFrame (By locator) {
        WebElement iFrame = driver.findElement(locator);
        try {
            driver.switchTo().frame(iFrame);
        } catch (NoSuchFrameException e){
            Log.error(e.getMessage());
        }

        Log.debug("Switched to iFrame located by " + locator);
    }


    /**
     * switches focus to default content
     */
    public void switchFromIFrameToDefaultContent () {
        driver.switchTo().defaultContent(); // you are now outside any frame
        Log.debug("Switched to default content");
    }


    /**
     * switches focus to particular browser window identified by numeric id
     *
     * @param id Integer, window identifier
     */
    public void switchToWindow(Integer id) {
        Set handles = driver.getWindowHandles();
        String[] individualHandle = new String[handles.size()];
        Iterator it = handles.iterator();
        int i = 0;
        while (it.hasNext()) {
            individualHandle[i] = (String) it.next();
            i++;
        }

        try {
            driver.switchTo().window(individualHandle[id]);
        } catch (NoSuchWindowException e){
            Log.error(e.getMessage());
        }

        driver.switchTo().defaultContent();
        Log.debug("Switched to window identified by " + individualHandle[id]);
    }


    /**
     * switches focus to next tab open in the browser
     * Simulates pressing of CTRL+Tab keys combination
     */
    public void switchTab() {
        driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"\t");
        driver.switchTo().defaultContent();
        Log.debug("Switched to new tab");
    }


    /**
     * opens new tab in the browser
     * Simulates pressing of CTRL+t keys combination
     */
    public void openNewTab() {
        driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"t");
        driver.switchTo().defaultContent();
        Log.debug("New tab open");
    }


    /**
     * opens new browser window
     * Simulates pressing of CTRL+n keys combination
     */
    public void openNewWindow() {
        driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"n");
        Log.debug("New window open");
    }


    /**
     * waits for an Alert pop up
     * Simulates pressing of CTRL+t keys combination
     * Switches focus to the alert pop Up
     *
     * @return Alert
     */
    public Alert waitForAlert() {
        Log.debug("Going to wait for an alert window");
        Integer seconds = Storage.get("Environment.Active.Web.timeout");
        WebDriverWait wait = new WebDriverWait(driver, seconds);
        try {
            wait.until(ExpectedConditions.alertIsPresent());
        } catch (TimeoutException e) {
            Log.error(e.getMessage());
        }

        return driver.switchTo().alert();
    }


    /**
     * takes screenshot of the web browser window
     *
     * @return byte[]
     */
    public byte[] takeScreenshot() {
        byte[] screenshot = null;
        try {
            screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (WebDriverException e) {
            Log.error("Screenshot can't be taken. Make sure that driver was started! " + e.getMessage());
        }

        return screenshot;
    }

}