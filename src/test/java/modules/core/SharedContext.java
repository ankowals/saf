package modules.core;

import org.openqa.selenium.support.events.EventFiringWebDriver;

public class SharedContext {
    public EventFiringWebDriver driver;
    public Context obj;
    public PropertyReader env;
    public Macro macro;
    public StepUtil step;
    public ConfigReader config;
}
