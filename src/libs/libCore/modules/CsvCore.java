package libs.libCore.modules;

import com.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

@SuppressWarnings("unchecked")
public class CsvCore {

    private int getHeaderLocation(String[] header, String columnName){
        return Arrays.asList(header).indexOf(columnName);
    }

    public String[] extractLine(String input, int lineNumber){
        CSVReader reader = new CSVReader(new StringReader(input));
        String[] nextLine = null;
        int index = 0;

        try{
            while ( (nextLine = reader.readNext()) != null ){
                if ( index == lineNumber ){
                    return nextLine;
                }
                index++;
            }
        } catch (IOException e ){
            Log.error(e.getMessage());
        }

        return nextLine;
    }

    public String extractValueFromColumnAtLine(String[] header, String[] line, String columnName){
        if ( header == null || header.length == 0 ){
            Log.error("Supplied header null or empty!");
        }
        if ( line == null || line.length == 0 ){
            Log.error("Supplied line null or empty!");
        }

        return line[getHeaderLocation(header, columnName)];
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
        String[] header = extractLine(input, 0);
        //extract line from csv
        String[] line = extractLine(input, rowNum);
        //extract value from particular column of particular line
        String extractedValue = extractValueFromColumnAtLine(header, line, columnName);


        if ( action.equals("equals") ){
            Assert.assertEquals(extractedValue, expectedValue);
        }
    }

}
