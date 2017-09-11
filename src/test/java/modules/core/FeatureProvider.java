package modules.core;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.stream.Stream;

public class FeatureProvider {

    private static ArrayList<String> result = new ArrayList();

    public static void printFeatures(String sDir) {
        try {
            Files.find(Paths.get(sDir), 999, (p, bfa) -> bfa.isRegularFile()
                    && p.getFileName().toString().matches(".*\\.feature"))
                    .forEach(x -> Log.debug("Feature found " + x));
        } catch (IOException e) {
            Log.error("directory " + sDir + " not found!");
            Log.error(e.getMessage());
        }
    }

   private static ArrayList<String> searchForFeature(ArrayList<String> result, String sDir, String keyword, String extension){
        try {
           try (Stream<Path> stream = Files.find(Paths.get(sDir), 99,
                   (path, attr) -> String.valueOf(path).endsWith(extension))) {
               stream.map(String::valueOf).filter(s-> StringUtils.containsIgnoreCase(s, keyword + extension))
                       .forEach(x -> result.add(x));
           }
       } catch (IOException e) {
           Log.error("directory " + sDir + " not found!");
           Log.error(e.getMessage());
       }

       return result;
   }

    public static ArrayList<String> searchForFile(String sDir, String extension){
       result.clear();
       try {
            try (Stream<Path> stream = Files.find(Paths.get(sDir), 99,
                    (path, attr) -> String.valueOf(path).endsWith(extension))) {
                stream.map(String::valueOf)
                        .forEach(x -> result.add(x));
            }
        } catch (IOException e) {
            Log.error("directory " + sDir + " not found!");
            Log.error(e.getMessage());
        }

        return result;
    }

   public static ArrayList<String> getSpecificFeature(String sDir, String keyword, String extension){
       String tmpKeyword = StringUtils.remove(keyword, "-");
       result.clear();
       result = searchForFeature(result, sDir, tmpKeyword, extension);

       if (result.size()==0){
           String tmpKeyword2 = StringUtils.replaceChars(keyword,"-","_");
           result = searchForFeature(result, sDir, tmpKeyword2, extension);
       }

       if (result.size()==0){
           result = searchForFeature(result, sDir, keyword, extension);
       }

       return result;
   }

   public static String getProjectPath() {
       String path = null;
       try {
           String tmp = Paths.get(ClassLoader.getSystemResource("").toURI()).getParent().toString();
           int idx = tmp.lastIndexOf(File.separator);
           path = tmp.substring(0,idx);
           Log.debug("Project path is " + path);
       } catch (URISyntaxException e) {
           Log.error("Project path not found!");
           Log.error(e.getMessage());
       }

       return path;
   }

   public static String getGlobalConfigPath() {
        return getProjectPath() + "//src//test//resources//config";
   }

   public static String getFeaturesPath() {
       return getProjectPath() + "//src//test//resources//features";
   }

}
