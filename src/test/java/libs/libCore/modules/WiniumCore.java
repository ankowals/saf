package libs.libCore.modules;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.winium.DesktopOptions;
import org.openqa.selenium.winium.WiniumDriver;
import winium.elements.desktop.ComboBox;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WiniumCore {

    private SharedContext ctx;
    private libs.libCore.modules.Storage Storage;
    private libs.libCore.modules.WinRSCore WinRSCore;
    private libs.libCore.modules.StepCore StepCore;

    private WiniumDriver App;

    // PicoContainer injects class SharedContext
    public WiniumCore(SharedContext ctx) {
        this.ctx = ctx;
        this.WinRSCore = ctx.Object.get("WinRSCore",WinRSCore.class);
        this.Storage = ctx.Object.get("Storage",Storage.class);
        this.StepCore = ctx.Object.get("StepCore",StepCore.class);
        //this.App = ctx.Object.get("App", WiniumDriver.class);
    }


    /**
     * Starts an app on local or remote host
     *
     * @param node String, node identifier, or localhost
     * @param pathToApp String, path to an app that shall be started
     * @param args String, additional arguments that shall be passed to an app
     */
    public void startApp(String node, String pathToApp, String args){

        String host = "localhost";
        if ( node.toLowerCase().equals("localhost") || node.toLowerCase().equals("") ){
            Log.debug("Going to start an app on a local host");
        } else {
            Log.debug("Going to start an app on a remote host " + node);
            host = Storage.get("Environment.Active.WinRM." + node + ".host");
            if ( host == null ){
                Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
            }
            Log.debug("Starting winium resources");
            ctx.Object.put("WiniumRemoteNodeId", String.class, node);
            String rdpProcessId = WinRSCore.uploadAndStartWiniumDriver(node);
            ctx.Object.put("WiniumRemoteRDPProcessId", String.class, rdpProcessId);
            WinRSCore.minimizeAllWindows (node, "minimizeWindowsTask");
        }

        String port = Storage.get("Environment.Active.WebDrivers.WiniumDesktop.port");
        String url = "http://" + host + ":" + port;

        DesktopOptions options = new DesktopOptions();
        options.setApplicationPath(pathToApp);
        if ( (args != null) && (args.length() > 0) ) {
            options.setArguments(args);
        }
        //WiniumDriver driver = null;
        try {
            App = new WiniumDriver(new URL(url), options);
            //setTimeout does not work:/
            //App.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        } catch (MalformedURLException e){
            Log.error("", e);
        }

        Log.debug("Started an app from " + pathToApp);
        ctx.Object.put("App", WiniumDriver.class, App);
    }


    /**
     * Closes winium resources on a remote host
     *
     */
    public void closeWiniumResources(){

        WiniumDriver driver = ctx.Object.get("App", WiniumDriver.class);
        String rdpProcessId = ctx.Object.get("WiniumRemoteRDPProcessId", String.class);
        String node = ctx.Object.get("WiniumRemoteNodeId", String.class);

        if ( driver != null ) {
            Log.debug("Try to close an application");
            try {
                driver.close();
                ctx.Object.put("App", WiniumDriver.class, null);
            } catch (WebDriverException e){
                Log.warn("Application is already closed");
            }
        }

        if ( rdpProcessId != null ) {
            Log.debug("Killing Winium driver process on remote host " + node);
            String cmd = "Get-CimInstance Win32_Process | Where {$_.name -match '.*Winium.*'} | Select Caption, CommandLine, ProcessId | Format-list";
            String result = WinRSCore.executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);

            if ( result.contains("Winium.Desktop.Driver.exe") ) {
                Log.debug("Closing Winium.Desktop.Driver.exe");
                String[] tmp = StringUtils.deleteWhitespace(result.trim()).split("ProcessId:");
                String processId = tmp[tmp.length - 1].trim();
                cmd = "Stop-Process -Id " + processId + " -Force -passThru";
                WinRSCore.executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);

                Log.debug("Deleting scheduled task used to run Winium driver");

                String script = "tempTask.ps1";
                cmd = "schtasks /DELETE /TN 'TempAutomationTask' /F;";
                WinRSCore.transferScript(node, cmd, script);

                result = WinRSCore.executeSingleCommandOnVM("Powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -InputFormat None -File \"" + script + "\"", node, 120);
                if (!result.contains("SUCCESS:")) {
                    Log.error("Failed to remove scheduled task TempAutomationTask");
                }

                WinRSCore.killRdpSession(rdpProcessId);
            }
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
        WebElement element = App.findElement(locator);
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
        Log.debug("Looking for an elements identified " + locator);
        List<WebElement> elements = App.findElements(locator);
        t_FindBy.end();
        Log.debug("Elements found after " + t_FindBy.duration()  + " ms");

        return elements;
    }


    public void awaitForAnElementWithName(String name, Integer timeout){
        Log.debug("Waiting for an element with name " + name + " with timeout of " + timeout + " seconds");

        if ( timeout < 0 ){
            getScreenshotAndAttachItToReport("Element_not_found");
            Log.error("An element with name " + name + " not found");
        }

        Boolean isFinished = false;

        try {
            List<WebElement> elements = findElements(By.name(name));

            if ( elements.size() > 0 ){
                for (WebElement element : elements){
                    isFinished = true;
                    Log.debug("Element with " + name + "has been found");
                    break;
                }
            }

        } catch (NoSuchElementException e){
            //ciii... do nothing
        }

        if ( ! isFinished ){
            StepCore.sleep(1);
            awaitForAnElementWithName(name, timeout -1);
        }

    }


    public void awaitForTabItem(String name, Integer timeout){
        Log.debug("Waiting for tab item with name " + name + " with timeout of " + timeout + " seconds");

        if ( timeout < 0 ){
            getScreenshotAndAttachItToReport("Tab_not_found");
            Log.error("Tab item with name " + name + " not found");
        }

        Boolean isFinished = false;

        try {
            List<WebElement> elements = findElements(By.name(name));

            if ( elements.size() > 0 ){
                for (WebElement element : elements){
                    String role = "";
                    try{
                        role = element.getAttribute("ControlType");
                    }catch(WebDriverException e) {
                        break;
                        //window was-recreated we shall skipp this loop run
                    }
                    if ( role != null ) {
                        Log.debug("Found element with name " + name + " and role " + role);
                        if ( role.contains("Tab") ) {
                            isFinished = true;
                            Log.debug("Tab item with name " + name + "has been found");
                            break;
                        }
                    }
                }
            }
        } catch (NoSuchElementException e){
            //ciii... do nothing
        }

        if ( ! isFinished ){
            StepCore.sleep(1);
            awaitForTabItem(name, timeout -1);
        }
    }


    public void awaitForElementWithNameIdentifiedBy(By locator, String name, Integer timeout){
        Log.debug("Waiting for element identified  " + locator + " and name " + name + " with timeout of " + timeout + " seconds");

        if ( timeout < 0 ){
            getScreenshotAndAttachItToReport("Element_not_found");
            Log.error("Element identified " + locator + " with name " + name + " not found");
        }

        Boolean isFinished = false;

        try {
            List<WebElement> elements = findElements(locator);

            if ( elements.size() > 0 ){
                for (WebElement element : elements){
                    String text = "";
                    try{
                        text = element.getAttribute("Name");
                    }catch(WebDriverException e) {
                        break;
                        //window was-recreated we shall skipp this loop run
                    }
                    if ( text != null ) {
                        Log.debug("Found element identified " + locator);
                        if ( text.contains(name) ) {
                            isFinished = true;
                            Log.debug("Element identified " + locator + " and required name " + name +
                                    " has been found");
                            break;
                        }
                    }
                }
            }
        } catch (NoSuchElementException e){
            //ciii... do nothing
        }

        if ( ! isFinished ){
            StepCore.sleep(1);
            awaitForElementWithNameIdentifiedBy(locator, name,timeout -1);
        }
    }


    public void clickButtonWithNameInWindowIdentifiedBy(String name, By locator) {
        Log.debug("About to click a button with name " + name + " in window of identified " + locator);

        ExecutionTimer t_FindBy = new ExecutionTimer();
        Boolean isFound = false;

        Integer i = 0;
        while( i < 30 ) {
            Log.debug("Looking for a window identified " + locator);
            WebElement window = null;
            try {
                window = findElement(locator);
            } catch (NoSuchElementException e) {
                Log.warn("Window identified " + locator + " not found");
                //ciii.. do nothing
            }

            if ( window != null ) {
                Log.debug("Looking for buttons in window identified " + locator);
                List<WebElement> buttons = window.findElements(By.className("Button"));

                Log.debug("Number of buttons found is " + buttons.size());
                if (buttons.size() != 0) {
                    for (WebElement button : buttons) {
                        String text = "";
                        try{
                            text = button.getAttribute("Name");
                        }catch(WebDriverException e) {
                            break;
                            //window was-recreated we shall skipp this loop run
                        }
                        Boolean isEnabled = button.isEnabled();
                        Log.debug("Button name is " + text + " and isEnabled state is " + isEnabled);
                        if (text.contains(name) && isEnabled) {
                            button.click();
                            StepCore.sleep(1);
                            isFound = true;
                            t_FindBy.end();
                            Log.debug("Clicked button with name " + name + " after " + t_FindBy.duration() + " ms");
                            break;
                        }
                    }
                }
            }

            if( isFound ){
                break;
            }

            StepCore.sleep(1);
            i++;
        }

        if ( ! isFound ) {
            getScreenshotAndAttachItToReport("Button_not_found");
            Log.error("Button with name " + name + " and in state enabled not found in window identified "
                    + locator + "!");
        }

    }


    public Boolean checkIfElementIdentifiedByIsVisible(By locator){
        Log.debug("Checking if element identified " + locator + " is visible");

        Boolean result = false;
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

        Boolean result =  false;
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


    public void clickTabItemWithName(String name) {
        Log.debug("About to click a tab with name " + name);

        ExecutionTimer t_FindBy = new ExecutionTimer();
        Boolean isFound = false;
        List<WebElement> tabs = findElements(By.name(name));

        Log.debug("Number of tabs found is " + tabs.size());
        if (tabs.size() == 0){
            Log.error("Tabs where not found in this window!");
        }

        for (WebElement tab : tabs){
            String text = tab.getAttribute("Name");
            String role = tab.getAttribute("ControlType");
            Boolean isEnabled = tab.isEnabled();
            if (role.contains("Tab")) {
                Log.debug("Tab name is " + text + " and isEnabled state is " + isEnabled);
                if ( text.contains(name) && isEnabled ) {
                    tab.click();
                    StepCore.sleep(1);
                    isFound = true;
                    t_FindBy.end();
                    Log.debug("Clicked tab with name " + name + " after " + t_FindBy.duration() + " ms");
                    break;
                }
            }
        }
        if ( ! isFound ){
            Log.error("Tab with name " + name + " and in state enabled not found in this window!");
        }
    }


    public void awaitForButtonToBeEnabledInWindowIdentifiedBy(String name, By locator, Integer timeout){
        Log.debug("Waiting for button with name " + name + " to be visible and enabled in window identified "
                + locator + " with timeout of " + timeout + " seconds");

        ExecutionTimer t_FindBy = new ExecutionTimer();
        Integer cnt = (timeout + 60)/60;
        Integer i = 0;
        Boolean isFinished = false;
        while ( i < cnt ) {
            Log.debug("Looking for an window identified " + locator);
            WebElement window = findElement(locator);
            List<WebElement> buttons = window.findElements(By.className("Button"));
            Log.debug("Number of buttons found is " + buttons.size());

            for (WebElement button : buttons){
                String text = "";
                try{
                    text = button.getAttribute("Name");
                }catch(WebDriverException e) {
                    break;
                    //window was-recreated we shall skipp this loop run
                }
                Boolean isEnabled = button.isEnabled();
                Log.debug("Button name is " + text + " and its state is " + isEnabled);
                if (isEnabled && text.contains(name)){
                    isFinished = true;
                }
            }

            if (isFinished){
                t_FindBy.end();
                Log.debug("Found enabled button with name " + name + " after " + t_FindBy.duration()  + " ms");
                break;
            }

            StepCore.sleep(60);
            i++;
        }

        if ( i == cnt){
            getScreenshotAndAttachItToReport("Enabled_button_not_found");
            Log.error("Timeout! Button " + name + " and in state enabled not found!");
        }
    }


    public Boolean checkIfElementWithNameIdentifiedByIsPresent(By locator, String name, Integer timeout){
        Log.debug("Checking if element identified " + locator + " and name " + name + " with timeout of " + timeout + " seconds is present");

        if ( timeout < 0 ){
            Log.warn("Element not present!");
            return false;
        }

        Boolean isFinished = false;

        try {
            List<WebElement> elements = findElements(locator);

            if ( elements.size() > 0 ){
                for (WebElement element : elements){
                    String text = element.getAttribute("Name");
                    if ( text != null ) {
                        Log.debug("Found element identified " + locator + " with name " + text);
                        if ( text.contains(name) ) {
                            isFinished = true;
                            Log.debug("Element identified " + locator + " and required name " + name);

                            return true;
                        }
                    }
                }
            }
        } catch (NoSuchElementException e){
            //ciii... do nothing
        }

        if ( ! isFinished ){
            StepCore.sleep(1);
            checkIfElementWithNameIdentifiedByIsPresent(locator, name, timeout - 1);
        }

        return false;
    }


    public void moveByOffsetAndClick(WebElement element, Integer xOffset, Integer yOffset){
        Log.debug("About to move to element " + element + " by x offset " + xOffset +
                " and y offset " + yOffset + " and click");
        ExecutionTimer t_FindBy = new ExecutionTimer();
        new Actions(App).moveToElement(element, 0, 0)
                .moveByOffset(xOffset, yOffset)
                .click()
                .build().perform();
        t_FindBy.end();
        Log.debug("Click executed after " + t_FindBy.duration()  + " ms");
    }


    public byte[] takeScreenshot() {
        byte[] screenshot = null;
        try {
            screenshot = ((TakesScreenshot) App).getScreenshotAs(OutputType.BYTES);
        } catch (WebDriverException e) {
            Log.error( "Screenshot can't be taken. Make sure that driver was started!", e );
        }

        return screenshot;
    }

    public void getScreenshotAndAttachItToReport(String name){
        byte[] screenshot = takeScreenshot();
        if (name.length() > 256) {
            name = name.substring(0, 255);
        }
        StepCore.attachScreenshotToReport(name, screenshot);
    }


}
