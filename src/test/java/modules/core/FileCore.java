package modules.core;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import java.util.*;
import java.util.stream.Stream;

import static org.apache.commons.io.FileUtils.readFileToString;

public class FileCore {

    private SharedContext ctx;

    public FileCore (SharedContext ctx) {
        this.ctx = ctx;

    }

    /**
     * Returns path to the project directory
     *
     * @return path String, path to project directory
     */
    public String getProjectPath() {
        String path = null;
        try {
            String tmp = Paths.get(ClassLoader.getSystemResource("").toURI()).getParent().toString();
            int idx = tmp.lastIndexOf(File.separator);
            path = tmp.substring(0,idx);
            Log.debug("Project path is " + path);
        } catch (URISyntaxException e) {
            Log.error( "Project path not found!", e );
        }

        return path;
    }

    /**
     * Returns path the global configuration directory
     *
     * @return path String, path to global configuration directory
     */
    public String getGlobalConfigPath() {
        return getProjectPath() + "//src//test//resources//config";
    }


    /**
     * Returns path the directory with feature files
     *
     * @return path String, path to features directory
     */
    public String getFeaturesPath() {
        return getProjectPath() + "//src//test//resources//features";
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
            Log.error( "Can't access " + sDir, e);
        }

        Collections.sort(result, String.CASE_INSENSITIVE_ORDER);

        return result;
    }


    /**
     * Returns path to the feature file directory that is currently executed
     *
     * @return result String, path to the feature file directory
     */
    public String getCurrentFeatureDirPath(){

        ArrayList<String> featurePaths = new ArrayList();
        String sDir = getFeaturesPath();
        String extension = ".feature";
        String featureDir = null;
        String keyword = ctx.Object.get("FeatureId", String.class);

        //feature id returned by cucumber-jvm may contain "-" but file
        //name on the disk does not have to
        //file name can be without whitespaces, with underscores or with minuses
        //or with whitespaces??!!
        //other combinations are not checked
        //in most common case just 1 go through this loop will be done
        HashMap<String, String> keywords = new HashMap();
        keywords.put(keyword, "");
        String tmpKeyword = StringUtils.remove(keyword, "-");
        keywords.put(tmpKeyword, "");
        tmpKeyword = StringUtils.replaceChars(keyword,"-","_");
        keywords.put(tmpKeyword, "");
        tmpKeyword = StringUtils.replaceChars(keyword,"-"," ");
        keywords.put(tmpKeyword, "");

        //would be nice to find more efficient way to search for files that contains
        //multiple search keywords
        //maybe we can create feature file index before before hook is fired?
        for (HashMap.Entry<String, String> entry : keywords.entrySet()) {
            ArrayList<String> tmpPaths = new ArrayList();
            tmpPaths = searchForFeature(sDir, entry.getKey(), extension);
            featurePaths.addAll(tmpPaths);
        }

        // if feature file was not found rise an error
        if(featurePaths.size()==0){
            Log.error("Currently used feature file path not found. " +
                    "Please make sure that Feature file and Feature name are same");
        }

        //if more than 1 feature file was found rise an error
        if (featurePaths.size()>1){
            Log.warn("Found more than 1 feature that meats criteria: name. "
                    + "Please fix feature files or stick to one features naming convention");
            for (String filePath : featurePaths) {
                Log.warn(filePath);
            }
            Log.error("Found more than 1 feature that meats criteria: name!");
        }

        if(featurePaths.size()==1) {
            Log.debug("Feature file found. Path is " + featurePaths.get(0));
            featureDir = FilenameUtils.getFullPathNoEndSeparator(featurePaths.get(0));
        }

        return featureDir;
   }


    /**
     * searches for feature files
     * helper function used by getCurrentFeatureDirPath
     *
     * @param sDir String, path to features directory
     * @param keyword String, feature file id
     * @param extension String, extension of feature file
     * @return result ArrayList, list of paths to found feature files
     */
    private ArrayList<String> searchForFeature(String sDir, String keyword, String extension){

        ArrayList<String> result = new ArrayList<>();
        try {
            try (Stream<Path> stream = Files.find(Paths.get(sDir), 99,
                    (path, attr) -> String.valueOf(path).endsWith(extension))) {
                        stream.map(String::valueOf).filter(s-> StringUtils.containsIgnoreCase(s, keyword + extension))
                        .forEach(x -> result.add(x));
            }
        } catch (IOException e) {
            Log.error( "Can't access " + sDir, e );
        }

        return result;
    }


    /**
     * reads file to a string
     *
     * @param file File, file handle
     * @return String sFile, content of the file as string
     */
    public String readToString (File file) {

        /*
        FileInputStream inputStream = null;
        String sFile = null;
        try {
            inputStream = new FileInputStream(path);
            try {
                sFile = IOUtils.toString(inputStream);
            } catch (IOException e) {
                Log.error("", e);
            }
        } catch (FileNotFoundException e) {
            Log.error("", e);
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.error("", e);
            }
        }

        return sFile;
        */

        String result = null;
        String encoding = "UTF-8";

        try {
            result = readFileToString(file, encoding);
        } catch (IOException e) {
            Log.error( "", e );
        }

        return result;
    }


    public void writeToFile(File file, String content) {
        String encoding = "UTF-8";
        try {
            FileUtils.writeStringToFile(file, content, encoding, false);
        } catch (IOException e) {
            Log.error( "", e );
        }
    }


    public void appendToFile(File file, String content) {
        String encoding = "UTF-8";
        try {
            FileUtils.writeStringToFile(file, content, encoding, true);
        } catch (IOException e) {
            Log.error( "", e );
        }
    }

    public File createTempFile (String name, String extension) {
        File result = null;

        try {
            result = File.createTempFile(name, "." + extension);
        } catch (IOException e) {
            Log.error( "", e );
        }

        return result;
    }

    public File createTempDir () {
        Path pathToTempDir = null;
        String systemTmpDirPath = FileUtils.getTempDirectoryPath();
        Path path = Paths.get(systemTmpDirPath);

        try {
            pathToTempDir = Files.createTempDirectory(path, "SAF_dir_");
        } catch (IOException | UnsupportedOperationException | IllegalArgumentException | SecurityException e ) {
            Log.error( "", e );
        }

        String sPathToTempDir = pathToTempDir.toAbsolutePath().toString();
        File result = new File(sPathToTempDir);

        return result;
    }

    public List<String> readLines (File file) {

        List<String> result = null;

        try {
            result = FileUtils.readLines(file);
        } catch (IOException e) {
            Log.error( "", e );
        }

        return result;
    }

}
