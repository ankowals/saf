package libs.libCore.modules;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import java.util.*;
import java.util.stream.Stream;

import static org.apache.commons.io.FileUtils.readFileToString;

@SuppressWarnings("unchecked")
public class FileCore {

    /**
     * Returns path to the project directory
     *
     * @return path String, path to project directory
     */
    public String getProjectPath() {
        try {
            String tmp = Paths.get(ClassLoader.getSystemResource("").toURI()).getParent().toString();
            int idx = tmp.lastIndexOf(File.separator);
            String path = tmp.substring(0,idx);
            path = path + File.separator +"src";
            Log.debug("Project path is " + path);

            return path;
        } catch (URISyntaxException e) {
            Log.error( "Project path not found! " + e.getMessage() );
        }

        return "";
    }

    /**
     * Returns paths to files that meets criteria like name or extension
     * Search is done in subdirectories as well
     *
     * @param sDir String, directory to search
     * @param criteria String, search criteria, for example file name or file extension
     * @return result List, paths to found files
     */
    public ArrayList<String> searchForFile(String sDir, String criteria){
        ArrayList<String> result = new ArrayList<>();
        try {
            try (Stream<Path> stream = Files.find(Paths.get(sDir), 99,
                    (path, attr) -> String.valueOf(path).endsWith(criteria))) {
                stream.map(String::valueOf)
                        .forEach(x -> result.add(x));
            }
        } catch (IOException e) {
            Log.warn( "Can't access " + sDir );
        }

        Collections.sort(result, String.CASE_INSENSITIVE_ORDER);

        return result;
    }

    /**
     * Returns path to the currently executed feature file directory
     * Detects duplicated feature file names. In this case test suite execution will be stopped immediately.
     *
     * @return String, path to the feature file directory
     */
    public String getCurrentFeatureDirPath(){
        Log.debug("Looking for a path to the current feature file");

        Context scenarioCtx = GlobalCtxSingleton.getInstance().get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class).checkOut();
        String scenario = scenarioCtx.get("FeatureUri", String.class);
        String path = new File(scenario).getParentFile().getAbsolutePath();

        if ( path.equals("") ){
            Log.error("Feature file path not found! Aborting execution!");
        }

        return path;
    }


    /**
     * reads file to a string
     *
     * @param file File, file handle
     * @return String sFile, content of the file as string
     */
    public String readToString (File file) {
        try {
            return readFileToString(file, "UTF-8");
        } catch (IOException e) {
            Log.error(e.getMessage());
        }

        return null;
    }


    /**
     * writes string to file
     *
     * @param file File, file handle
     * @param content String, string that shall be written
     */
    public void writeToFile(File file, String content) {
        try {
            FileUtils.writeStringToFile(file, content, "UTF-8", false);
        } catch (IOException e) {
            Log.error(e.getMessage());
        }
    }


    /**
     * appends string to file
     *
     * @param file File, file handle
     * @param content String, string that shall be added to the file
     */
    public void appendToFile(File file, String content) {
        try {
            FileUtils.writeStringToFile(file, content, "UTF-8", true);
        } catch (IOException e) {
            Log.error(e.getMessage());
        }
    }


    /**
     * creates temporary file with a unique name
     *
     * @param name String, prefix used in the name of the file
     * @param extension String, extension that is going to be added to the file name
     *
     * @return File
     */
    public File createTempFile (String name, String extension) {
        try {
            getTempDir();
            return File.createTempFile(name, "." + extension);
        } catch (IOException e) {
            Log.error(e.getMessage());
        }

        return null;
    }


    /**
     * creates temporary directory with prefix SAF_dir_
     * Directory will be created in operating system temp folder
     *
     * @return File
     */
    public File createTempDir () {
         Path path = Paths.get(getTempDir().getAbsolutePath());

        try {
            return new File(Files.createTempDirectory(path, "SAF_dir_").toAbsolutePath().toString());
        } catch (IOException | UnsupportedOperationException | IllegalArgumentException | SecurityException e ) {
            Log.error(e.getMessage());
        }

        return null;
    }


    /**
     * returns system's temporary directory
     *
     * @return File
     */
    public File getTempDir () {
        Context scenarioCtx = GlobalCtxSingleton.getInstance().get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class).checkOut();
        Storage storage = scenarioCtx.get("Storage", Storage.class);
        boolean isSysTemp = storage.get("Environment.Active.UseSystemTempDir");
        if ( !isSysTemp ){
            System.setProperty("java.io.tmpdir", getProjectPath().replaceAll("src$", "target"));
        }

        Properties p = System.getProperties();
        Log.debug("java.io.tmpdir = " + p.get("java.io.tmpdir"));

        return new File(FileUtils.getTempDirectoryPath());
    }


    /**
     * read file line by line and returns list of lines
     *
     * @param file File, file handle
     * @return List<String>
     */
    public List<String> readLines (File file) {
        try {
            return FileUtils.readLines(file, "UTF-8");
        } catch (IOException e) {
            Log.error(e.getMessage());
        }

        return new ArrayList<>();
    }


    /**
     * Waits for a file for defined time duration. Return true if file exists else false when timeout is over
     *
     * @param file, File, the file to check, must not be null
     * @param timeout, Integer, the maximum time in seconds to wait
     * @return Boolean
     */
    public Boolean waitForFile(File file, int timeout) {
        try {
            return FileUtils.waitFor(file, timeout);
        } catch (NullPointerException e) {
            Log.error(e.getMessage());
        }

        return false;
    }


    /**
     * Removes file or directory and all of its content (subdirectories included)
     *
     * @param file, File, the file to check, must not be null
     */
    public void removeFile(File file) {
        try {
            FileUtils.forceDelete(file);
        } catch (IOException | NullPointerException e) {
            Log.error(e.getMessage());
        }
    }


}
