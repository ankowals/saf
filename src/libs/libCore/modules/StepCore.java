package libs.libCore.modules;

import io.qameta.allure.Allure;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import io.qameta.allure.Attachment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.*;

import static java.lang.Math.toIntExact;

@SuppressWarnings("unchecked")
public class StepCore {

    private Context scenarioCtx;
    private FileCore FileCore;
    private Storage Storage;

    // PicoContainer injects class SharedContext
    public StepCore() {
        this.scenarioCtx = GlobalCtxSingleton.getInstance().get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class).checkOut();
        this.FileCore = scenarioCtx.get("FileCore",FileCore.class);
        this.Storage = scenarioCtx.get("Storage", Storage.class);
    }

    /**
     * Waits for defined amount of time in seconds
     *
     * @param seconds number of seconds to wait
     */
    public void sleep (Integer seconds) {
        try {
            Log.debug("Waiting for " + seconds + " seconds");
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            // don't do anything
        }
    }

    /**
     * returns type of value
     * helper function
     *
     * @param value object value
     *
     * @return type of value
     */
    private <T> Class<?> getType(T value){
        if(value.getClass().getName().contains("String")){
            return String.class;
        }
        if(value.getClass().getName().contains("Double")){
            return Double.class;
        }
        if(value.getClass().getName().contains("Float")){
            return Float.class;
        }
        if(value.getClass().getName().contains("Integer")){
            return Integer.class;
        }
        if(value.getClass().getName().contains("Long")){
            return Long.class;
        }
        if(value.getClass().getName().contains("ArrayList")){
            return ArrayList.class;
        }
        if(value.getClass().getName().contains("HashMap")){
            return HashMap.class;
        }
        if(value.getClass().getName().contains("Boolean")){
            return Boolean.class;
        }
        Log.error("Type of object " + value.getClass().getName() + " not supported!");

        return null;
    }

    /**
     * Checks if string provided as an input to the step def is actually a key in the storage
     * Returns input value or value extracted from storage.
     * Please note that in this case type of input is String but returned value can be one of
     * String, Double, Long, Int, Boolean
     *
     * @param input key in the storage or value
     *
     * @return value from storage or input
     */
    public <T> T checkIfInputIsVariable(String input) {
        T result = (T) input;
        T tmp = Storage.get(input);

        //check if String contains boolean
        if ( BooleanUtils.toBooleanObject(input) != null ) {
            Boolean b = BooleanUtils.toBoolean(input);
            result = (T) b;
            Log.debug("Converting String " + input + " to boolean of class " + result.getClass().getName());
        }

        //check if String contains number
        if( NumberUtils.isNumber(input) ){
            Number num = null;
            try {
                num = NumberFormat.getInstance(Locale.getDefault()).parse(input);
                if ( num instanceof Long ) {
                    long tVal = (long) num;
                    try {
                        num = toIntExact(tVal);
                    } catch (ArithmeticException e) {
                        //do nothing just return Long
                    }
                }
                if ( num instanceof Double ){
                    double tVal = (double) num;
                    if ((double)(float)tVal == tVal) {
                        num = num.floatValue();
                    }
                }
            } catch (Exception e) {
                Log.debug("Checking if String " + input + " contains numeric value");
                Log.error("Not able to parse String " + input + " to Number! " + e.getMessage());
            }
            Class<T> typeKey = (Class<T>) getType(num);
            result = typeKey.cast(num);
            Log.debug("Converting String " + input + " to number of class " + result.getClass().getName());
        }

        //enforce string if requested
        if ( input.startsWith("\"") && input.endsWith("\"") ) {
            input = input.replaceFirst("\"", "");
            input = StringUtils.removeEnd(input, "\"");
            Class<T> typeKey = (Class<T>) getType(input);
            result = typeKey.cast(input);
            Log.debug("Converting String " + input + " to string of class " + result.getClass().getName());
        } else if (input.startsWith("'") && input.endsWith("'")) {
            input = input.replaceFirst("'", "");
            input = StringUtils.removeEnd(input, "'");
            Class<T> typeKey = (Class<T>) getType(input);
            result = typeKey.cast(input);
            Log.debug("Converting String " + input + " to string of class " + result.getClass().getName());
        }

        if ( tmp != null ){
            result = tmp;
            Log.debug("Converting element from storage: " + input + " to " + result + " of class " + result.getClass().getName());
        }

        return result;
    }


    /**
     * Executes white space tolerant template comparison
     *
     * @param templateName String, name of the template without .template extension
     * @param pathToResults String, path on the file system to the file which shall be compared with template
     */
    public void compareWithTemplate(String templateName, String pathToResults) {
        Log.debug("Template comparison started");

        String templatePath = searchForTemplate(templateName);

        File template = new File(templatePath);
        File results = new File(pathToResults);

        String sFile = FileCore.readToString(template);
        String sResults = FileCore.readToString(results);

        //evaluate the template
        String templateAfteEval = replaceInString(sFile);

        //attach template after evaluation to the report
        File temp = FileCore.createTempFile(templateName,"template");
        FileCore.appendToFile(temp, templateAfteEval);
        String tempPath = temp.getAbsolutePath();
        attachFileToReport(templateName + ".template","text/plain",tempPath);

        //trim template content and string to compare
        String templateContent = templateAfteEval.trim().replaceAll("\\s+","");
        String resultToCompare = sResults.trim().replaceAll("\\s+","");

        FileCore.removeFile(temp);

        //compare ignoring white spaces
        if ( ! resultToCompare.matches(templateContent) ) {
            Log.error("Template comparison failed!");
        }
    }


    /**
     * Checks if ${ctx.storageName.storageKey} kind of variables exist in the template
     * If so executes variables substitution
     * Template file after evaluation is attached to the report
     *
     * @param templateName String, name of the template without .template extension
     *
     * @return File
     */
    public File evaluateTemplate(String templateName) {
        String templatePath = searchForTemplate(templateName);

        File template = new File(templatePath);
        String sFile = FileCore.readToString(template);

        //evaluate the template
        String templateAfteEval = replaceInString(sFile);

        //attach template after evaluation to the report
        File temp = FileCore.createTempFile(templateName,"template");
        FileCore.appendToFile(temp, templateAfteEval);
        String tempPath = temp.getAbsolutePath();
        attachFileToReport(templateName + ".template","text/plain",tempPath);

        return temp;

    }


    /**
     * Returns paths to the template file with particular name
     * Search is done in local and global templates directories
     *
     * @param templateName String, name of the template file without extension
     * @return templatePath String, path to the template file
     */
    public String searchForTemplate(String templateName) {
        //find global template dir
        String projectDir = FileCore.getProjectPath();
        String globalTemplateDir = projectDir + File.separator + "templates";

        //find local template dir
        String localDir = scenarioCtx.get("FeatureFileDir", String.class);
        String localTemplateDir = localDir + File.separator + "templates";

        //find default template dir
        String defaultTemplateDir = projectDir + File.separator + "libs";

        //search for template first in local dir
        Log.debug("Looking for template " + templateName + " in " + localTemplateDir);
        List<String> templates = FileCore.searchForFile(localTemplateDir,".*" + templateName + "\\.template$");

        //if local template not found search for it in global dir
        if ( templates.size() < 1 ) {
            Log.debug("Looking for template " + templateName + " in " + globalTemplateDir);
            templates = FileCore.searchForFile(globalTemplateDir,".*" + templateName + "\\.template$");
        }

        //if global template not found search for it in default dir
        if ( templates.size() < 1 ) {
            Log.debug("Looking for template " + templateName + " in " + defaultTemplateDir);
            templates = FileCore.searchForFile(defaultTemplateDir,".*" + templateName + "\\.template$");
        }

        if ( templates.size() < 1 ) {
            Log.error("Template " + templateName + ".template was not found!");
        }

        //return the template if multiple files found return just the first one!
        String templatePath = templates.get(0);
        Log.debug("Template found in " + templatePath);

        return templatePath;
    }


    /**
     * filters file with positive filter. This means that only lines that contain specified keywords
     * will pass the filter
     * Filter shall be defined as a List of Strings
     *
     * @param input String, input content
     * @param filters List<String>, list of positive filters to apply
     *
     * @return String, file content after filtering
     */
    public String applyPositiveFilter (String input, List<String> filters) {

        if ( filters == null ) {
            Log.error("List of positive filters null!");
        }
        if ( filters.size() < 1 ) {
            Log.error("List of positive filters is empty!");
        }

        //List<String> lines = FileCore.readLines(input);
        List<String> lines = Arrays.asList(input.split("\\r?\\n"));

        StringBuilder sb = new StringBuilder();
        for (String filter : filters) {
            sb.append(filter);
            sb.append(", ");
        }
        String sFilter = sb.toString();

        Log.debug("Going to apply positive filter [" + sFilter.trim().substring(0,sFilter.length()-1) + "]");
        sb = new StringBuilder();
        for ( String line : lines ) {
            for ( String filter : filters) {
                if ( line.contains(filter) ) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                }
            }
        }
        return sb.toString().trim();

    }


    /**
     * filters file with negative filter. This means that only lines that do not contain specified
     * keywords will pass the filter
     * Filter shall be defined as a List of Strings
     *
     * @param input String, input content
     * @param filters List<String>, list of negative filters to apply
     *
     * @return String, file content after filtering
     */
    public String applyNegativeFilter (String input, List<String> filters) {

        if ( filters == null ) {
            Log.error("List of negative filters null!");
        }
        if ( filters.size() < 1 ) {
            Log.error("List of negative filters is empty!");
        }

        //List<String> lines = FileCore.readLines(input);
        List<String> lines = Arrays.asList(input.split("\\r?\\n"));

        StringBuilder sb = new StringBuilder();
        for (String filter : filters) {
            sb.append(filter);
            sb.append(", ");
        }
        String sFilter = sb.toString();

        Log.debug("Going to apply negative filter [" + sFilter.trim().substring(0,sFilter.length()-1) + "]");
        sb = new StringBuilder();
        for ( String line : lines ) {
            boolean isMatch = false;
            for ( String filter : filters) {
                if ( line.contains(filter) ) {
                    isMatch = true;
                }
            }
            if ( ! isMatch ) {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
        }

        return sb.toString().trim();
    }


    /**
     * filters file with block filter. This means that only lines that are between specified keywords
     * (keywords included) will pass the filter
     * Filter shall be defined as a List of Maps where each map contains 'begin' and 'end' keys
     * In this way multiple block filters can be defined
     *
     * @param input String, input content
     * @param filters List<Map<String, String>>, list of block filters to apply
     *
     * @return String, file content after filtering
     */
    public String applyBlockFilter (String input, List<Map<String, String>> filters) {

        if ( filters == null ) {
            Log.error("List of block filters null!");
        }
        if ( filters.size() < 1 ) {
            Log.error("List of block filters is empty!");
        }

        //List<String> lines = FileCore.readLines(input);
        List<String> lines = Arrays.asList(input.split("\\r?\\n"));

        StringBuilder sb = new StringBuilder();

        for ( Map<String, String> filter : filters) {
            boolean isMatch = false;
            String begin = filter.get("begin");
            String end = filter.get("end");

            if (begin == null || begin.equals("")) {
                Log.error("begin keyword of block filter " + filter + " null or empty!");
            }

            if (end == null || end.equals("")) {
                Log.error("end keyword of block filter " + filter + " null or empty!");
            }

            for ( String line : lines ) {
                if ( line.contains( end ) ) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    isMatch = false;
                }
                if ( line.contains( begin ) ) {
                    isMatch = true;
                    }
                if ( isMatch ) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                }
            }

        }

        return sb.toString().trim();
    }


    /**
     * Attaches file to the report<br>
     * List of MIME types is available under https://www.iana.org/assignments/media-types/media-types.xhtml
     *
     * @param name name of the file to be displayed in the report
     * @param type type of file like text/plain or application/pdf etc.
     * @param path path to the file
     */
    @Attachment(value="{0}", type="{1}")
    public byte[] attachFileToReport(String name, String type, String path) {
        byte[] bytes = null;

        File file = new File(path);
        String extension = FilenameUtils.getExtension(file.getAbsolutePath());

        try {
            bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        } catch (IOException e) {
            Log.error( "File " + file.getAbsolutePath() + " not found! " + e.getMessage());
        }

        String attachmentSource = Allure.getLifecycle().prepareAttachment(name, type, extension);
        Allure.getLifecycle().writeAttachment(attachmentSource, new ByteArrayInputStream(bytes));

        Log.debug("File " + path + " with name " + name + " attached to report");

        return bytes;
    }

    /**
     * Attaches screenshot to the report
     *
     * @param name name of the screenshot
     */
    @Attachment(value="{0}", type="image/png")
    public byte[] attachScreenshotToReport(String name, byte[] screenshot){
        String tName = StringUtils.deleteWhitespace(name);
        Log.debug("Screenshot with name " + tName + " attached to report");

        String attachmentSource = Allure.getLifecycle().prepareAttachment(name, "image/png", "png");
        Allure.getLifecycle().writeAttachment(attachmentSource, new ByteArrayInputStream(screenshot));

        return screenshot;
    }

    /**
     * Attaches text to the report
     *
     * @param name of the text to be displayed in the report
     * @param message content of the text to be displayed in the report
     */
    @Attachment(value="{0}", type="text/plain")
    public String attachMessageToReport(String name, String message){
        Log.debug("Message with name " + name + " attached to report");

        String attachmentSource = Allure.getLifecycle().prepareAttachment(name, "text/plain", "txt");
        Allure.getLifecycle().writeAttachment(attachmentSource,new ByteArrayInputStream(message.getBytes(Charset.forName("UTF-8"))));

        return message;
    }


    /**
     * Replaces variable place holders in String with values from Storage
     *
     * @param input, String
     *
     */
    public String replaceInString(String input) {
        //Log.debug("Input is " + input);
        int beignIdx = input.indexOf("${");
        int endIdx = input.indexOf("}", beignIdx);

        if (beignIdx != -1) {
            if ( endIdx == -1 ){
                Log.error("Typo in config value " + input + "! Missing closing bracket }. Can't do variable substitution!");
            }

            String toReplace = input.substring(beignIdx+2, endIdx);
            String toCheck = toReplace;
            String result = null;
            if ( toReplace.startsWith("ctx.") ){
                toCheck = toReplace.substring(4);
                result = checkIfInputIsVariable(toCheck).toString();
            }

            if ( toReplace.startsWith("mcr.") ){
                toCheck = toReplace.substring(4);
                HashMap<String, String> macros = scenarioCtx.get("CalculatedMacros", HashMap.class);
                result = macros.get(toCheck);
            }

            if ( result == null ){
                Log.warn("Can't replace variable " + toReplace + " because its value does not exists or null!");
            }

            if (  ! toReplace.equals("ctx." + result) && ! toReplace.equals("mcr." + result) ) {
                return replaceInString(input.replace("${" + toReplace + "}", result));
            }
        }

        return input;
    }


    public String encodeString(String input) {
        Log.debug("Encoding input is " + input);

        return StringEncoder.encrypt(input);
    }


    public String decodeString(String input) {
        Log.debug("Decoding input string " + input);

        return StringEncoder.decrypt(input);
    }

}