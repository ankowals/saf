package libs.libCore.modules;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.winium.WiniumDriver;
import winium.elements.desktop.ComboBox;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public class WiniumCore {

    private Context scenarioCtx;
    private StepCore StepCore;
    private WiniumDriver driver;

    // PicoContainer injects class SharedContext
    public WiniumCore() {
        this.scenarioCtx = GlobalCtxSingleton.getInstance().get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class).checkOut();
        this.StepCore = scenarioCtx.get("StepCore",StepCore.class);
        this.driver = scenarioCtx.get("WiniumDesktopDriver", WiniumDriver.class);
    }

    public String getSessionId(){
        Log.debug("Session id is " + driver.getSessionId().toString());

        return driver.getSessionId().toString();

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
        WebElement element = driver.findElement(locator);
        t_FindBy.end();
        Log.debug("Element found after " + t_FindBy.duration()  + " ms");

        return element;
    }


    /**
     * Tries to find all elements on the web page identified by locator like xpath, css or others
     *
     * @param locator By, locator used to identify web element
     * @return WebElement
     */
    public List<WebElement> findElements(By locator) {

        ExecutionTimer t_FindBy = new ExecutionTimer();
        Log.debug("Looking for elements identified " + locator);
        List<WebElement> elements = driver.findElements(locator);
        t_FindBy.end();
        Log.debug("Elements found after " + t_FindBy.duration()  + " ms");

        return elements;
    }


    public Boolean awaitForAnElement(By locator, Integer timeout, Integer interval, Boolean isEnabled){
        Log.debug("Awaiting for an element identified " + locator + " with timeout of " + timeout + " and interval " + interval + " and of isEnabled state " + isEnabled);

        //focusNewWindow();

        boolean result = false;
        WebElement element = null;

        if ( timeout < 0 ){
            //focusNewWindow();
            Log.warn("An element identified " + locator + " not found!");
            return false;
        }

        try {
            element = findElement(locator);
        } catch ( WebDriverException e){
            //ciii... do nothing
        }

        if ( element != null && !isEnabled ){
            result = true;
        }

        if ( element != null && isEnabled){
            boolean currentState = false;
            try {
                currentState = element.isEnabled();
            }catch (WebDriverException e){
                //ciii... do nothing
            }
            if (currentState){
                result = true;
            }
        }

        if ( ! result ){
            StepCore.sleep(interval);
            //focusNewWindow();
            return awaitForAnElement(locator, timeout - interval, interval, isEnabled);
        }

        return true;
    }


    public void clickAnElement(By locator){
        Log.debug("About to click an element identified " + locator);
        WebElement element = findElement(locator);
        element.click();
        Log.debug("Element clicked");
    }

    public void enterIntoAnElement(By locator, String text){
        Log.debug("About to enter text " + text + " into an element identified " + locator);

        ExecutionTimer t_FindBy = new ExecutionTimer();

        WebElement element = findElement(locator);
        Log.debug("About to clear input identified " + locator + " content");
        element.clear();
        StepCore.sleep(1);
        Log.debug("Content of input identified " + locator + " has been cleared");
        element.sendKeys(text);
        StepCore.sleep(1);

        t_FindBy.end();
        Log.debug("Text has been entered after " + t_FindBy.duration() + " ms");
    }


    public Boolean checkIfAnElementIsPresent(By locator){
        Log.debug("Checking if an element identified " + locator + " is present");

        boolean result = false;

        ExecutionTimer t_FindBy = new ExecutionTimer();
        try {
            WebElement element = findElement(locator);
            if ( element != null ){
                t_FindBy.end();
                Log.debug("Element identified " + locator + " has been found after " + t_FindBy.duration()  + " ms");
                result = true;
            }
        } catch (Exception e) {
            Log.warn("Element identified " + locator + " not found!");
            //ciii... do nothing
        }

        return result;
    }


    public Boolean checkIfItemWithTextIsPresentInComboBoxIdentifiedBy(By locator, String text){
        Log.debug("Looking for combo box identified " + locator);

        boolean result =  false;
        WebElement comboBox = findElement(locator);

        Log.debug("Opening combo box identified " + locator);
        ComboBox combo = new ComboBox(comboBox);
        combo.expand();

        Log.debug("Looking for an item with text " + text + " in a combo box identified " + locator);

        try {
            findElement(By.name(text));
            result = true;
        } catch (Exception e){
            Log.warn("No item with text " + text + " has been found in ComboBox identified " + locator);
        }

        Log.debug("Closing combo box identified " + locator);
        combo.collapse();
        StepCore.sleep(3);

        return result;

    }


    public void selectItemWithTextFromComboBoxIdentifiedBy(By locator, String text){
        Log.debug("Looking for combo box identified " + locator);

        WebElement comboBox = findElement(locator);

        Log.debug("Opening combo box identified " + locator);
        ComboBox combo = new ComboBox(comboBox);
        combo.expand();

        Log.debug("About to click on an item with text " + text + " in a combo box identified " + locator);
        findElement(By.name(text)).click();
        Log.debug("An entry with text " + text + " has been chosen");
    }


    public void moveByOffsetAndClick(WebElement element, Integer xOffset, Integer yOffset){
        Log.debug("About to move to element " + element + " by x offset " + xOffset +
                " and y offset " + yOffset + " and click");

        ExecutionTimer t_FindBy = new ExecutionTimer();
        new Actions(driver).moveToElement(element, 0, 0)
                .moveByOffset(xOffset, yOffset)
                .click()
                .build().perform();
        t_FindBy.end();
        Log.debug("Click executed after " + t_FindBy.duration()  + " ms");
    }


    public byte[] takeScreenshot() {
        byte[] screenshot = null;

        try {
            screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (WebDriverException e) {
            Log.warn("Screenshot can't be taken. Make sure that driver was started!");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.warn(sw.toString());
        }

        return screenshot;
    }

    public void getScreenshotAndAttachItToReport(String name){
        byte[] screenshot = takeScreenshot();
        if (name.length() > 256) {
            name = name.substring(0, 255);
        }
        if ( screenshot != null ) {
            StepCore.attachScreenshotToReport(name, screenshot);
        }
    }

    //returns an element based on index
    private <T> T nthElement(Iterable<T> data, int n){
        int index = 0;
        for(T element : data){
            if(index == n){
                return element;
            }
            index++;
        }
        return null;
    }

    //new window becomes first element in windows list [that is not the case if there is a relationship parent->child]
    public void focusWindow(Integer windowIdentifier){
        getWindowHandles();
        Object count = nthElement(driver.getWindowHandles(), windowIdentifier);
        if ( count != null ) {
            Log.debug("Going to switch to window with handle " + count.toString());
            driver.switchTo().window(count.toString());
        }
    }


    //get windows handles,
    //for example powershell or windows cmd windows are returned here but we do not want to switch to them!
    private List<String> getWindowHandles(){
        Set<String> handles = driver.getWindowHandles();

        List<String> handlesList = new ArrayList<>();
        for ( Object handle : handles ){
            Log.debug("Window handle " + handle.toString() + " is to be added to window handles list ");
            handlesList.add(handle.toString());
        }

        Log.debug("Found " + handlesList.size() + " window handles");
        if ( handlesList.size() > 0 ) {
            Log.debug("Parent window handle is " + handlesList.get(0));
            String currentHandle = "";
            try {
                currentHandle = driver.getWindowHandle();
            } catch (WebDriverException e){
                Log.warn("Current window handle couldn't be retrieved!");
            }
            Log.debug("Current window handle is " + currentHandle);
        }

        return handlesList;
    }


    public void closeApplication(){
        driver.close();
    }

}