package libs.libCore.modules;

import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

@SuppressWarnings("unchecked")
public class AssertCore {

    private Context scenarioCtx;
    private Storage Storage;
    private CsvCore CsvCore;

    public AssertCore() {
        this.scenarioCtx = GlobalCtxSingleton.getInstance().get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class).checkOut();
        this.Storage = scenarioCtx.get("Storage", Storage.class);
        this.CsvCore = scenarioCtx.get("CsvCore", CsvCore.class);
    }


    /**
     * Validates value of a parameter available in ValidatableResponse
     * Supported actions are equalTo, containsString, containsInAnyOrder, greaterThan, lessThan
     *
     * @param vResp, ValidatableResponse, response that is going to be validated
     * @param key, String, name of the response property that which value is going to be validated
     * @param action, String, determines kind of assertion that is going to be performed
     * @param expectedValue, Object, expected value
     */
    public void validatableResponseBodyTableAssertion(ValidatableResponse vResp, String key, String action, Object expectedValue){

        String type = expectedValue.getClass().getName();
        String cType = null;

        //log null pointer exception in case message body is empty
        try {
            cType = vResp.extract().path(key).getClass().getName();
        } catch (NullPointerException e) {
            Log.error("Key " + key + " not found in the message body" + e.getMessage() );
        }

        Log.debug("Current is " + vResp.extract().path(key));
        Log.debug("Its type is " + cType);

        Log.debug("Action is " + action);

        Log.debug("Expected value is " + expectedValue);
        Log.debug("Its type is " + type);

        if (action.equalsIgnoreCase("equalTo")){
            vResp.body(key, equalTo(expectedValue));
        } else if (action.equalsIgnoreCase("containsString")){
            vResp.body(key, containsString(expectedValue.toString()));
        } else if (action.equalsIgnoreCase("containsInAnyOrder")){
            vResp.body(key, containsInAnyOrder(expectedValue));
        } else if (action.equalsIgnoreCase("greaterThan")){
            if (cType.contains("Int")) {
                vResp.body(key, greaterThan((int) expectedValue));
            } else if (cType.contains("Long")) {
                vResp.body(key, greaterThan((Long) expectedValue));
            } else if (cType.contains("Double")) {
                vResp.body(key, greaterThan((Double) expectedValue));
            } else if (cType.contains("Float")) {
                vResp.body(key, greaterThan((Float) expectedValue));
            } else {
                Log.error("Type not supported for greaterThen comparison. " +
                        "Please use one of Int, Long, Double, Float");
            }
        } else if (action.equalsIgnoreCase("lessThan")){
            if (cType.contains("Int")) {
                vResp.body(key, lessThan((int) expectedValue));
            } else if (cType.contains("Long")) {
                vResp.body(key, lessThan((Long) expectedValue));
            } else if (cType.contains("Double")) {
                vResp.body(key, lessThan((Double) expectedValue));
            } else if (cType.contains("Float")) {
                vResp.body(key, lessThan((Float) expectedValue));
            } else {
                Log.error("Type not supported for lessThan comparison. " +
                        "Please use one of Int, Long, Double, Float");
            }
        } else {
            Log.error("Action " + action + " not supported. Please use one of " +
                    "equalTo, containsInAnyOrder, containsString, greaterThan, lessThan");
        }
    }

    public void extractValueFromBodyAndStoreItInStorage(ValidatableResponse vResp, String key, String pathInStorage){

        String cType = null;

        //log null pointer exception in case message body is empty
        try {
            cType = vResp.extract().path(key).getClass().getName();
        } catch (NullPointerException e) {
            Log.error("Key " + key + " not found in the message body" + e.getMessage() );
        }

        Log.debug("Key " + key + " value is");
        Log.debug(vResp.extract().path(key));
        Log.debug("Its type is " + cType);

        Storage.set(pathInStorage, vResp.extract().path(key));
    }

    public void verifyValueInParticularRowAndColumn(String columnName, String action, String expectedValue, String input){
        //extract desired line number
        if ( !columnName.contains("[") || !columnName.contains("]") ){
            Log.error("Specified column name in the key field shall contain row number! " +
                    "Please specify one by appending [row number] to column name!");
        }
        String sRowNum = columnName.substring(columnName.indexOf("[") + 1);
        columnName = columnName.substring(0, columnName.indexOf("["));
        sRowNum = sRowNum.substring(0, sRowNum.indexOf("]"));

        if ( !StringUtils.isNumeric(sRowNum) ){
            Log.error("Provided column id is not a number!");
        }

        int rowNum = Integer.parseInt(sRowNum);

        //extract header
        String[] header = CsvCore.extractLine(input, 0);
        //extract line from csv
        String[] line = CsvCore.extractLine(input, rowNum);
        //extract value from particular column of particular line
        String extractedValue = CsvCore.extractValueFromColumnAtLine(header, line, columnName);


        if ( action.equals("equals") ){
            Assert.assertEquals(extractedValue, expectedValue);
        }
    }

}
