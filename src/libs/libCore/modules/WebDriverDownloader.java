package libs.libCore.modules;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class WebDriverDownloader {

    private final static Object MUTEX = new Object(); //guarantees synchronization on a MUTEX object thus avoids concurrent download
    private Storage Storage;
    private FileCore FileCore;

    public WebDriverDownloader(Storage storage, FileCore fileCore){
        this.Storage = storage;
        this.FileCore = fileCore;
    }

    public void manageDriver() {
        synchronized (MUTEX) {
            boolean isManaging = Storage.get("Environment.Active.WebDrivers.Manager.manage");
            if ( !isManaging ){
                return;
            }
            String path = Storage.get("Environment.Active.WebDrivers.Chrome.path");
            String browser = Storage.get("Environment.Active.Web.browser");
            String from = "";
            if ( path.startsWith("resources") ){
                path = FileCore.getProjectPath() + File.separator + path;
            }
            if ( browser.equalsIgnoreCase("Chrome") ){
                from = Storage.get("Environment.Active.WebDrivers.Manager.Chrome.url");
            }
            if ( browser.equalsIgnoreCase("Firefox") ){
                from = Storage.get("Environment.Active.WebDrivers.Manager.Firefox.url");
            }
            if ( browser.equalsIgnoreCase("Ie") ){
                from = Storage.get("Environment.Active.WebDrivers.Manager.InternetExplorer.url");
            }

            if ( !checkDriver(path) ){
                int timeout = Storage.get("Environment.Active.WebDrivers.Manager.timeout");
                downloadDriver(from, path, timeout * 1000, timeout * 1000);
            }

        }
    }

    private boolean checkDriver(String path){
        if ( new File(path).isFile() ){
            Log.debug("Driver available in " + path);
            return true;
        }
        boolean isForceUpdate = Storage.get("Environment.Active.WebDrivers.Manager.forceUpdate");
        if ( isForceUpdate ){
            Log.debug("Driver update forced!");
            return false;
        }
        return false;
    }

    private void downloadDriver(String from, String to, int conTimeout, int readTimeout){
        Log.debug("Driver " + to + " not found! Going to download it from " + from);
        String downloadedFileName = new File(from).getName();
        String pathToTmpDir = FileCore.getTempDir().getAbsolutePath();
        String targetFileName = new File(to).getName();
        try {
            FileUtils.copyURLToFile(new URL(from), new File(pathToTmpDir + File.separator + downloadedFileName), conTimeout, readTimeout);
            //check file extension and either unzip or just copy to thte target dir
            String ext = FilenameUtils.getExtension(downloadedFileName);
            if ( ext.equalsIgnoreCase("zip") ){
                extractZipFile(new File(pathToTmpDir + File.separator + downloadedFileName).toPath(), targetFileName, new File(to).toPath());
            }
            if ( ext.equalsIgnoreCase("exe") || ext.equalsIgnoreCase("") ){
                FileUtils.copyFile(new File(pathToTmpDir + File.separator + downloadedFileName), new File(to));
            }
            if ( ext.equalsIgnoreCase("tar.gz") ){
                unTarGz( new File (pathToTmpDir + File.separator + downloadedFileName).toPath(), new File(to).toPath() );
            }

        } catch ( IOException e ){
            Log.error(e.getMessage());
        }
    }

    private void extractZipFile(Path zipFile, String fileName, Path outputFile) throws IOException{
        //wrap the file system in a try-with-resources statement to auto-close it when finished and prevent a memory leak
        try (FileSystem fileSystem = FileSystems.newFileSystem(zipFile, null)){
            Path fileToExtract = fileSystem.getPath(fileName);
            Files.copy(fileToExtract, outputFile);
        }
    }

    private void unTarGz(Path pathInput, Path pathOutput) throws IOException {
        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(
                new GzipCompressorInputStream(
                    new BufferedInputStream( Files.newInputStream(pathInput))
        ));
        ArchiveEntry archiveEntry = null;
        while( (archiveEntry = tarArchiveInputStream.getNextEntry()) != null ){
            Path pathEntryOutput = pathOutput.resolve( archiveEntry.getName() );
            if ( archiveEntry.isDirectory() ){
                if ( !Files.exists( pathEntryOutput )){
                    Files.createDirectory( pathEntryOutput );
                }
            } else {
                Files.copy(tarArchiveInputStream, pathEntryOutput);
            }
        }

        tarArchiveInputStream.close();
    }

}