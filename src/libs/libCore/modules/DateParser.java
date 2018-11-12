package libs.libCore.modules;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateParser {

    // List of all date formats that we want to parse.
    // Add your own format here.
    private static final List<String> dateFormats = new ArrayList<String>() {{
        add("dd.MM.yyyy");
        add("dd.MMM.yyyy");
        add("M/dd/yyyy");
        add("dd.M.yyyy");
        add("M/dd/yyyy hh:mm:ss");
        add("dd.M.yyyy hh:mm:ss");
        add("dd-MMM-yyyy");
    }};

    private static final List<String> regexList = new ArrayList<String>() {{
        add("\\d\\d[.]\\d\\d[.]\\d\\d\\d\\d");
        add("\\d\\d[.][a-zA-Z]{3}[.]\\d\\d\\d\\d");
        add("[0-9]{1,2}[/]\\d\\d[/]\\d\\d\\d\\d");
        add("\\d\\d[.][0-9]{1,2}[.]\\d\\d\\d\\d");
        add("[0-9]{1,2}[/]\\d\\d[/]\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d");
        add("\\d\\d[.][0-9]{1,2}[.]\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d");
        add("\\d\\d-[a-zA-Z]{3}-\\d\\d\\d\\d");
    }};

    /**
     * Convert String with various formats into java.util.Date
     *
     * @param input
     *            Date as a string
     * @return java.util.Date object if input string is parsed
     * 			successfully else returns null
     */
    public static Date convertToDate(String input) {
        Date date = null;
        if( null == input ) {
            Log.error("Input for date util null or empty!");
        }
        if( "" == input ) {
            Log.error("Input for date util null or empty!");
        }

        if (regexList.size() != dateFormats.size()) {
            Log.error("Wrong configuration of DateUtil date formats. dateFormats list size is " +
                    dateFormats.size() + " but regexList size is " +
                    regexList.size() );
        }

        for (int i = 0; i < regexList.size(); i++) {
            String regex = regexList.get(i);
            Pattern p = Pattern.compile(regex); // thread-safe
            Matcher m = p.matcher(input);

            try {
                if (m.matches()) {
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormats.get(i));
                    sdf.setLenient(false);

                    date = sdf.parse(input);

                    return date;
                }
            } catch (ParseException e) {
                Log.error("", e);
            }
        }

        return date;
    }
}