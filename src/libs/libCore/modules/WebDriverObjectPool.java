package libs.libCore.modules;

import cucumber.api.Result;
import cucumber.api.event.TestStepFinished;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WebDriverObjectPool extends AbstractObjectPool<EventFiringWebDriver>{

    WebDriverObjectPool(){
        this.available = new HashMap<>();
        this.inUse = new HashMap<>();
    }

    @Override
    protected EventFiringWebDriver create(String browser){
        return new WebDriverFactory().create(browser);
    }

    @Override
    protected void close(EventFiringWebDriver instance) {
        if ( validate(instance) ) {
            printLogs(instance, "browser");
            printLogs(instance, "driver");
            instance.close();
            instance.quit();
        }
    }

    private void printLogs(EventFiringWebDriver instance, String type){
        if ( instance.manage().logs().get(type).getAll().size() > 0 ) {
            Log.debug(type + " console logs are available below");
            for (LogEntry logEntry : instance.manage().logs().get(type).getAll()) {
                Log.debug("" + logEntry);
            }
        }
    }

    public synchronized void checkInAllPerThread(TestStepFinished event, String testCaseName, StepCore stepCore){
        for (Map.Entry<String, Set<EventFiringWebDriver>> entry : inUse.entrySet()) {
            String key = entry.getKey();
            if ( key.contains(Thread.currentThread().getName()) ) {
                Set<EventFiringWebDriver> instances = entry.getValue();
                for (EventFiringWebDriver instance : instances) {
                    key = key.replace("_" + Thread.currentThread().getName(), "");
                    takeScreenshotOnFailure(instance, event, testCaseName, stepCore);
                    checkIn(key, instance);
                }
            }
        }
    }

    public synchronized void closeAllPerThread(TestStepFinished event, String testCaseName, StepCore stepCore){
        for (Map.Entry<String, Set<EventFiringWebDriver>> entry : inUse.entrySet()) {
            String key = entry.getKey();
            if ( key.contains(Thread.currentThread().getName()) ) {
                Set<EventFiringWebDriver> instances = entry.getValue();
                for (EventFiringWebDriver instance : instances) {
                    takeScreenshotOnFailure(instance, event, testCaseName, stepCore);
                    close(instance);
                }
                inUse.remove(key);
            }
        }
        for (Map.Entry<String, Set<EventFiringWebDriver>> entry : available.entrySet()) {
            String key = entry.getKey();
            if ( key.contains(Thread.currentThread().getName()) ) {
                Set<EventFiringWebDriver> instances = entry.getValue();
                for (EventFiringWebDriver instance : instances) {
                    takeScreenshotOnFailure(instance, event, testCaseName, stepCore);
                    close(instance);

                }
                available.remove(key);
            }
        }
    }

    private void takeScreenshotOnFailure(EventFiringWebDriver instance, TestStepFinished event, String testCaseName, StepCore stepCore){
        if ( (event.result.is(Result.Type.FAILED) || event.result.getErrorMessage() != null)
                && validate(instance) ) {
            Log.debug("Try to take a screenshot");
            try {
                byte[] screenshot = ((TakesScreenshot) instance).getScreenshotAs(OutputType.BYTES);
                String name = StringUtils.remove(testCaseName, "-");
                if (name.length() > 256) {
                    name = name.substring(0, 255);
                }
                stepCore.attachScreenshotToReport(name, screenshot);
            } catch (WebDriverException e) {
                Log.warn("Driver not usable. Can't take screenshot");
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                Log.warn(sw.toString());
            }
        }
    }


}