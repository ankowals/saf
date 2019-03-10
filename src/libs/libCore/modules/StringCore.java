package libs.libCore.modules;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;

public class StringCore {

    private Context scenarioCtx;

    public StringCore() {
        this.scenarioCtx = GlobalCtxSingleton.getInstance().get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class).checkOut();
    }

    public String deleteWhitespaces(String input) {
        return StringUtils.deleteWhitespace(input);
    }

    public String capitalize(String input) {
        return StringUtils.capitalize(input);
    }

    public boolean isNumber(String input){
        return NumberUtils.isNumber(input);
    }

    public boolean isBoolean(String input){
        if ( BooleanUtils.toBooleanObject(input) != null ) {
            return true;
        }

        return false;
    }

    public boolean containsIgnoreCase(String input, String searchStr){
        return StringUtils.containsIgnoreCase(input, searchStr);
    }

    public boolean eaqualsIgoreCase(String input, String expectedStr){
        return StringUtils.equalsIgnoreCase(input, expectedStr);
    }

}
