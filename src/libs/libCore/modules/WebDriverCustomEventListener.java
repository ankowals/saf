package libs.libCore.modules;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverEventListener;

/**
 * helper class used to enhance logging during web driver actions execution
 *
 */
public class WebDriverCustomEventListener implements WebDriverEventListener{

    private ExecutionTimer t_FindBy;

    @Override
    public void beforeAlertAccept(WebDriver webDriver) {
        Log.debug("Going to accept an alert");
    }

    @Override
    public void afterAlertAccept(WebDriver webDriver) {
        Log.debug("Alert accepted");
    }

    @Override
    public void afterAlertDismiss(WebDriver webDriver) {
        Log.debug("Alert dismissed");
    }

    @Override
    public void beforeAlertDismiss(WebDriver webDriver) {
        Log.debug("Going to dismiss an alert");
    }

    @Override
    public void beforeNavigateTo(String s, WebDriver webDriver) {

    }

    @Override
    public void afterNavigateTo(String s, WebDriver webDriver) {

    }

    @Override
    public void beforeNavigateBack(WebDriver webDriver) {

    }

    @Override
    public void afterNavigateBack(WebDriver webDriver) {

    }

    @Override
    public void beforeNavigateForward(WebDriver webDriver) {

    }

    @Override
    public void afterNavigateForward(WebDriver webDriver) {

    }

    @Override
    public void beforeNavigateRefresh(WebDriver webDriver) {

    }

    @Override
    public void afterNavigateRefresh(WebDriver webDriver) {

    }

    @Override
    public void beforeFindBy(By by, WebElement webElement, WebDriver webDriver) {
        Log.debug("Looking for an element identified " + by);
        t_FindBy = new ExecutionTimer();
    }

    @Override
    public void afterFindBy(By by, WebElement webElement, WebDriver webDriver) {
        t_FindBy.end();
        Log.debug("Element found after " + t_FindBy.duration()  + " ms");
    }

    @Override
    public void beforeClickOn(WebElement webElement, WebDriver webDriver) {
        Log.debug("About to click on the " + webElement.toString());
    }

    @Override
    public void afterClickOn(WebElement webElement, WebDriver webDriver) {
        Log.debug("Element " + webElement.toString() + " clicked");
    }

    @Override
    public void beforeChangeValueOf(WebElement webElement, WebDriver webDriver, CharSequence[] charSequences) {
    }

    @Override
    public void afterChangeValueOf(WebElement webElement, WebDriver webDriver, CharSequence[] charSequences) {
    }

    @Override
    public void beforeScript(String s, WebDriver webDriver) {
    }

    @Override
    public void afterScript(String s, WebDriver webDriver) {
        Log.debug("Script " + s + " executed");
    }

    @Override
    public void beforeSwitchToWindow(String s, WebDriver webDriver) {
        Log.debug("Going to switch to window " + s);
    }

    @Override
    public void afterSwitchToWindow(String s, WebDriver webDriver) {
        Log.debug("Switched to window " + s);
    }

    @Override
    public void onException(Throwable throwable, WebDriver webDriver) {

    }

    @Override
    public <X> void beforeGetScreenshotAs(OutputType<X> outputType) {

    }

    @Override
    public <X> void afterGetScreenshotAs(OutputType<X> outputType, X x) {

    }

    @Override
    public void beforeGetText(WebElement webElement, WebDriver webDriver) {
        Log.debug("Going to extract text from element " + webElement);
    }

    @Override
    public void afterGetText(WebElement webElement, WebDriver webDriver, String s) {
        Log.debug("Text " + s + " extracted from element " + webElement);
    }


}