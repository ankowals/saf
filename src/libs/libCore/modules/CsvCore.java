package libs.libCore.modules;

import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

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

}
