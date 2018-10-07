package libs.libCore.modules;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import io.qameta.allure.Attachment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.*;

import static java.lang.Math.toIntExact;

@SuppressWarnings("unchecked")
public class StepCore {

    private Context scenarioCtx;
    private FileCore FileCore;
    private Storage Storage;

    // PicoContainer injects class SharedContext
    public StepCore() {
        this.scenarioCtx = ThreadContext.getContext("Scenario");
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
            Thread.sleep(seconds*1000);
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
        if(value.getClass().getName().contains("Long")){
            return Long.class;
        }
        if(value.getClass().getName().contains("Integer")){
            return Integer.class;
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
        Storage Storage = scenarioCtx.get("Storage", Storage.class);
        T tmp = Storage.get(input);

        //check if String contains boolean
        if ( BooleanUtils.toBooleanObject(input) != null ) {
            Boolean b = BooleanUtils.toBoolean(input);
            result = (T) b;
            Log.debug("Converted String " + input + " to Boolean");
        }

        //check if String contains number
        if(NumberUtils.isNumber(input)){
            Number num = null;
            try {
                num = NumberFormat.getInstance(Locale.getDefault()).parse(input);
                if ( num instanceof Long ) {
                    Long tVal = (Long) num;
                    try {
                        int iVal = toIntExact(tVal);
                        num = iVal;
                    } catch (ArithmeticException e) {
                        //do nothing just return Long
                    }
                }
            } catch (Exception e) {
                Log.debug("Checking if String contains a numeric value " + input);
                Log.error("Not able to parse String to Number for " + input, e);
            }
            Class<T> typeKey = (Class<T>) getType(num);
            result = typeKey.cast(num);
            Log.debug("Converted String " + input + " to number of class " + result.getClass().getName());
        }

        if ( tmp != null ){
            result = tmp;
            Log.debug("Converted element from storage: " + input + " to " + result + " of class " + result.getClass().getName());
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
        String templateAfteEval = replaceInTemplate(sFile);

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
        String templateAfteEval = replaceInTemplate(sFile);

        //attach template after evaluation to the report
        File temp = FileCore.createTempFile(templateName,"template");
        FileCore.appendToFile(temp, templateAfteEval);
        String tempPath = temp.getAbsolutePath();
        attachFileToReport(templateName + ".template","text/plain",tempPath);

        return temp;

    }


    /**
     * Checks if ${ctx.storageName.storageKey} kind of variables exist in the template
     * If so executes variables substitution
     * Template file after evaluation is attached to the report
     *
     * @param templateName String, name of the template without .template extension
     * @param templateDirPath String, path to the directory where template is present
     *
     * @return File
     */
    public File evaluateTemplate(String templateName, String templateDirPath){
        File template = new File(templateDirPath + File.separator + templateName + ".template");
        String sFile = FileCore.readToString(template);

        //evaluate the template
        String templateAfterEval = replaceInTemplate(sFile);

        //attach template after evaluation to the report
        File temp = FileCore.createTempFile(templateName,"template");
        FileCore.appendToFile(temp, templateAfterEval);
        String tempPath = temp.getAbsolutePath();
        attachFileToReport(templateName + ".template","text/plain",tempPath);

        return temp;
    }



    /**
     * helper function used in evaluateTemplate method
     * replaces variables with values from storage
     *
     * @param input String, template content
     *
     * @return String
     */
    private String replaceInTemplate (String input) {
        Integer beignIdx = input.indexOf("${");
        Integer endIdx = input.indexOf("}", beignIdx);

        if (beignIdx != -1) {
            if ( endIdx == -1 ){
                Log.error("Typo in template! Missing closing bracket }. Can't do variable substitution!");
            }

            String toReplace = input.substring(beignIdx+2, endIdx);
            String toCheck = toReplace;
            if ( toReplace.startsWith("ctx.") ){
                toCheck = toReplace.substring(4);
            }
            String result = checkIfInputIsVariable(toCheck).toString();

            if (  ! toReplace.equals("ctx." + result) ) {
                return replaceInTemplate(input.replace("${" + toReplace + "}", result));
            }
        }

        return input;
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
        ArrayList<String> templates = FileCore.searchForFile(localTemplateDir,templateName + ".template");

        //if local template not found search for it in global dir
        if ( templates.size() < 1 ) {
            Log.debug("Looking for template " + templateName + " in " + globalTemplateDir);
            templates = FileCore.searchForFile(globalTemplateDir,templateName + ".template");
        }

        //if global template not found search for it in default dir
        if ( templates.size() < 1 ) {
            Log.debug("Looking for template " + templateName + " in " + defaultTemplateDir);
            templates = FileCore.searchForFile(defaultTemplateDir,templateName + ".template");
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
     * @param input File, file handle
     * @param filters List<String>, list of positive filters to apply
     *
     * @return String, file content after filtering
     */
    public String applyPositiveFilter (File input, List<String> filters) {

        if ( filters == null ) {
            Log.error("List of positive filters null!");
        }
        if ( filters.size() < 1 ) {
            Log.error("List of positive filters is empty!");
        }

        String output = "";
        List<String> lines = FileCore.readLines(input);

        String sFilter = "";
        for (String filter : filters) {
            sFilter = sFilter + ", " + filter;
        }

        String n = System.lineSeparator();

        Log.debug("Going to apply positive filter [" + sFilter.substring(1) + "]");
        for ( String line : lines ) {
            for ( String filter : filters) {
                if ( line.contains(filter) ) {
                    output = output + line + n;
                }
            }
        }

        output = output.trim();

        return output;
    }


    /**
     * filters file with negative filter. This means that only lines that do not contain specified
     * keywords will pass the filter
     * Filter shall be defined as a List of Strings
     *
     * @param input File, file handle
     * @param filters List<String>, list of negative filters to apply
     *
     * @return String, file content after filtering
     */
    public String applyNegativeFilter (File input, List<String> filters) {

        if ( filters == null ) {
            Log.error("List of negative filters null!");
        }
        if ( filters.size() < 1 ) {
            Log.error("List of negative filters is empty!");
        }

        String output = "";
        List<String> lines = FileCore.readLines(input);

        String sFilter = "";
        for (String filter : filters) {
            sFilter = sFilter + ", " + filter;
        }

        String n = System.lineSeparator();

        Log.debug("Going to apply negative filter [" + sFilter.substring(1) + "]");
        for ( String line : lines ) {
            Boolean isMatch = false;
            for ( String filter : filters) {
                if ( line.contains(filter) ) {
                    isMatch = true;
                }
            }
            if ( ! isMatch ) {
                output = output + line + n;
            }
        }

        output = output.trim();

        return output;
    }


    /**
     * filters file with block filter. This means that only lines that are between specified keywords
     * (keywords included) will pass the filter
     * Filter shall be defined as a List of Maps where each map contains 'begin' and 'end' keys
     * In this way multiple block filters can be defined
     *
     * @param input File, file handle
     * @param filters List<Map<String, String>>, list of block filters to apply
     *
     * @return String, file content after filtering
     */
    public String applyBlockFilter (File input, List<Map<String, String>> filters) {

        if ( filters == null ) {
            Log.error("List of block filters null!");
        }
        if ( filters.size() < 1 ) {
            Log.error("List of block filters is empty!");
        }

        String output = "";
        String n = System.lineSeparator();
        List<String> lines = FileCore.readLines(input);

        for ( Map<String, String> filter : filters) {
            Boolean isMatch = false;
            String begin = filter.get("begin");
            String end = filter.get("end");

            if (begin == null || begin == "") {
                Log.error("begin keyword of block filter " + filter + " null or empty!");
            }

            if (end == null || end == "") {
                Log.error("end keyword of block filter " + filter + " null or empty!");
            }

            for ( String line : lines ) {
                if ( line.contains( end ) ) {
                    output = output + line + n;
                    isMatch = false;
                }
                if ( line.contains( begin ) ) {
                    isMatch = true;
                    }
                if ( isMatch ) {
                    output = output + line + n;
                }
            }

        }

        output = output.trim();

        return output;
    }


    /**
     * Creates random string of desired length
     *
     * @param length
     * @return String
     */
    public String makeRandomString(int length) {
        Random rand = new Random();
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append( (char) ('a' + rand.nextInt(26)));
        }
        return result.toString();
    }


    /**
     * Attaches file to the report
     *
     * @param name name of the file to be displayed in the report
     * @param type type of file like text/plain or application/pdf etc.
     * @param path path to the file
     */
    @Attachment(value="{0}", type="{1}")
    public byte[] attachFileToReport(String name, String type, String path) {
        byte[] bytes = null;

        File file = new File(path);
        try {
            bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        } catch (IOException e) {
            Log.error( "File " + file.getAbsolutePath() + " not found!", e );
        }

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
        return message;
    }


    /**
     * Replaces variable place holders in String with values from Storage
     *
     * @param input, String
     *
     */
    public String replaceInString(String input) {
        Log.debug("Input is " + input);
        Integer beignIdx = input.indexOf("${");
        Integer endIdx = input.indexOf("}", beignIdx);

        if (beignIdx != -1) {
            if ( endIdx == -1 ){
                Log.error("Typo in config value " + input + "! Missing closing bracket }. Can't do variable substitution!");
            }

            String toReplace = input.substring(beignIdx+2, endIdx);
            String toCheck = toReplace;
            if ( toReplace.startsWith("ctx.") ){
                toCheck = toReplace.substring(4);
            }
            String result = checkIfInputIsVariable(toCheck).toString();

            if (  ! toReplace.equals("ctx." + result) ) {
                return replaceInString(input.replace("${" + toReplace + "}", result));
            }
        }

        return input;
    }


    /**
     * Creates a file in OS temporary directory which can be used to trigger browser lock<br>
     * Please use this method together with lockBrowser()<br>
     *
     */
    public void activateBrowserLock(){

        File pathToTempDir = FileCore.getTempDir();
        Log.debug("Path to temp directory is " + pathToTempDir);
        File file = new File(pathToTempDir + "\\" + "activateBrowserLock.lck");

        //get current timestamp
        Instant instant = Instant.now();
        Long now = instant.getEpochSecond();

        //prepare line for file
        String line = now.toString();

        FileCore.writeToFile(file, line);

    }

    private Boolean checkIfBrowserLockIsActive(){
        File pathToTempDir = FileCore.getTempDir();
        Log.debug("Path to temp directory is " + pathToTempDir);
        File file = new File(pathToTempDir + "\\" + "activateBrowserLock.lck");

        if (file.exists() && !file.isDirectory()) {
            String fileContent = FileCore.readToString(file);

            //get current timestamp
            Instant instant = Instant.now();
            Long now = instant.getEpochSecond();

            //check file content
            Long result = 0L;
            Long timestampFromFile = Long.valueOf(fileContent.trim());
            result = now - timestampFromFile;

            if ( result.intValue() <= 900 ){
                return true;
            }
        }

        return false;
    }

    /**
     * Can be used in case tests run in parallel on the same host.<br>
     * Will pause test execution if other webUI test is in progress.<br>
     * Use in case Sikuli is needed to handle flash based elements in the browser to avoid covering of browser window with other windows.
     *
     */
    public void lockBrowser(){
        /*

        THIS IS JUST IN CASE WE WANT TO RUN BROWSER TESTS IN PARALLEL ON SAME HOST
        AND USE SIKULI FOR FLASH BASED ELEMENTS for example via Jenkins
        It shall not affect single tests runs

         */

        if ( checkIfBrowserLockIsActive() ) {
            Log.warn("Browser lock activated! Going to check if new window can be open");
        } else {
            return;
        }

        //lock browser for 5 minutes
        String url = Storage.get("Environment.Active.Web.url");
        File pathToTempDir = FileCore.getTempDir();
        Log.debug("Path to temp directory is " + pathToTempDir);
        File file = new File(pathToTempDir + "\\" + "browserLock.lck");

        Boolean isBusy = true;
        Integer i = 0;

        while (isBusy) {

            //get current timestamp
            Instant instant = Instant.now();
            Long now = instant.getEpochSecond();

            //prepare line for file
            String line = url + "," + now.toString();
            Long result = 0L;
            String fileContent = "";

            //overall timeout is set to 25 minutes
            if(i==25){
                Log.debug("Timeout!");
                FileCore.writeToFile(file, line);
                //timeout we need to close this loop
                break;
            }

            if (file.exists() && !file.isDirectory()) {
                Log.debug(pathToTempDir + "\\" + "browserLock.lck" + " file found");
                fileContent = FileCore.readToString(file);
                Log.debug("File content is " + fileContent);

                if (fileContent.trim().equals("") || (! fileContent.contains(","))){
                    Log.debug("lck file empty, writing new content");
                    FileCore.writeToFile(file, line);
                    //file is empty browser can be open
                    break;
                }

                String[] tmp = fileContent.trim().split(",");
                if (url.equals(tmp[0].trim())){
                    //same job runs the browser, continue and do not wait
                    Log.debug("same job runs the browser, no need to wait");
                    FileCore.writeToFile(file, line);
                    break;
                }

                Long timestampFromFile = Long.valueOf(tmp[1].trim());
                result = now - timestampFromFile;

            } else {
                Log.warn(pathToTempDir + "\\" + "browserLock.lck" + " file not found! Going to create one");
                //create file and open browser
                FileCore.writeToFile(file, line);
                break;
            }

            if ( result.intValue() > 300 ){
                Log.debug("lck file found and 5 minutes since last usage has passed");
                //5 minutes between previous test has passed we need to break this loop
                //before we do that execute one last check to make sure that no other job is running
                fileContent = FileCore.readToString(file);
                Log.debug("File content is " + fileContent);

                String[] tmp = fileContent.trim().split(",");
                Long timestampFromFile = Long.valueOf(tmp[1].trim());
                result = now - timestampFromFile;

                if ( url.equals(tmp[0].trim()) || result.intValue() > 300 ){
                    //same job runs the browser, continue and do not wait
                    Log.debug("same job runs the browser or 5 minutes has passed, no need to wait");
                    FileCore.writeToFile(file, line);
                    break;
                } else{
                    lockBrowser();
                }
            }

            Log.debug("lck file found and in use. Going to wait");
            sleep(60);
            i++;
        }
    }

}