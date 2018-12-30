package libs.libCore.modules;

import cucumber.api.Result;
import cucumber.api.event.TestStepFinished;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.winium.WiniumDriver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WiniumDriverObjectPool extends AbstractObjectPool<WiniumDriver> {

    WiniumDriverObjectPool(){
        this.available = new HashMap<>();
        this.inUse = new HashMap<>();
    }

    @Override
    protected WiniumDriver create(String node){
        return new WiniumDriverFactory().create(node);
    }

    @Override
    protected void close(WiniumDriver instance) {
        if ( validate(instance) ) {
            WiniumDriverFactory wdf = new WiniumDriverFactory();
            wdf.closeDriver(instance);
            wdf.closeWiniumResources();
        }
    }

    public synchronized void checkInAllPerThread(TestStepFinished event, String testCaseName, StepCore stepCore){
        for (Map.Entry<String, Set<WiniumDriver>> entry : inUse.entrySet()) {
            String key = entry.getKey();
            if ( key.contains(Thread.currentThread().getName()) ) {
                Set<WiniumDriver> instances = entry.getValue();
                for (WiniumDriver instance : instances) {
                    key = key.replace("_" + Thread.currentThread().getName(), "");
                    takeScreenshotOnFailure(instance, event, testCaseName, stepCore);
                    checkIn(key, instance);
                }
            }
        }
    }

    public synchronized void closeAllPerThread(TestStepFinished event, String testCaseName, StepCore stepCore){
        for (Map.Entry<String, Set<WiniumDriver>> entry : inUse.entrySet()) {
            String key = entry.getKey();
            if ( key.contains(Thread.currentThread().getName()) ) {
                Set<WiniumDriver> instances = entry.getValue();
                for (WiniumDriver instance : instances) {
                    takeScreenshotOnFailure(instance, event, testCaseName, stepCore);
                    close(instance);
                }
                inUse.remove(key);
            }
        }
        for (Map.Entry<String, Set<WiniumDriver>> entry : available.entrySet()) {
            String key = entry.getKey();
            if ( key.contains(Thread.currentThread().getName()) ) {
                Set<WiniumDriver> instances = entry.getValue();
                for (WiniumDriver instance : instances) {
                    takeScreenshotOnFailure(instance, event, testCaseName, stepCore);
                    close(instance);

                }
                available.remove(key);
            }
        }
    }

    private void takeScreenshotOnFailure(WiniumDriver instance, TestStepFinished event, String testCaseName, StepCore stepCore){
        if ( (event.result.is(Result.Type.FAILED) || (event.result.is(Result.Type.SKIPPED)
                || event.result.getErrorMessage() != null))
                && validate(instance) ) {
            Log.debug("Taking screenshot on scenario failure");
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