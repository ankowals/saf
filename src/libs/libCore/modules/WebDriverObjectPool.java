package libs.libCore.modules;

import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.support.events.EventFiringWebDriver;

public class WebDriverObjectPool extends AbstractObjectPool<EventFiringWebDriver>{

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

}