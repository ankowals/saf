package libs.libCore.modules;

import org.apache.commons.exec.*;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.winium.DesktopOptions;
import org.openqa.selenium.winium.WiniumDriver;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class ExecutorCore {

    private Context scenarioCtx;
    private Context globalCtx;
    private FileCore FileCore;
    private Storage Storage;

    public ExecutorCore() {
        this.scenarioCtx = ThreadContext.getContext("Scenario");
        this.globalCtx = ThreadContext.getContext("Global");
        this.FileCore = scenarioCtx.get("FileCore",FileCore.class);
        this.Storage = scenarioCtx.get("Storage",Storage.class);
    }
    /**
     * Execute a Command as a background or blocking process.
     *
     * @param cmd     String, Command to execute
     * @param workingDir  File, Working directory
     * @param timeout     Integer, Kill process after this time (in sec) (0: no timeout)
     * @param blocking    Boolean,  Synchronous/blocking (true) or asynchronous/background startup (false).
     * @return  An outputstream that contains the output of the process written into stdout/stderr
     */
    public ByteArrayOutputStream execute(String cmd, File workingDir, int timeout, boolean blocking)
    {
        Executor executor = new DefaultExecutor();
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        CommandLine cmdLine = null;

        try {
            cmdLine = CommandLine.parse(cmd);
        } catch (IllegalArgumentException e) {
            Log.error("", e);
        }

        if (timeout > 0)
        {
            ExecuteWatchdog watchdog = new ExecuteWatchdog(1000 * timeout);
            executor.setWatchdog(watchdog);
        }

        ByteArrayOutputStream os1 = new ByteArrayOutputStream(1024);

        //live-streaming
        InputStream is = null;
        if ( blocking ) {
            PipedOutputStream os = new PipedOutputStream();
            try {
                is = new PipedInputStream(os);
            } catch (IOException e) {
                Log.error("", e);
            }
            executor.setStreamHandler(new PumpStreamHandler(os));
        } else {
            executor.setStreamHandler(new PumpStreamHandler(os1));
        }

        //This is used to end the process when the JVM exits
        ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();
        executor.setProcessDestroyer(processDestroyer);

        executor.setWorkingDirectory(workingDir);

        Log.debug("Command to execute is " + cmd);
        Log.debug("Working dir is " + workingDir.getAbsolutePath());

        try {
            executor.execute(cmdLine, resultHandler);
        } catch (ExecuteException e) {
            Log.error("", e);
        } catch (IOException e) {
            Log.error("", e);
        }

        if ( blocking ) {

            Reader reader = new InputStreamReader(is);
            BufferedReader r = new BufferedReader(reader);
            String tmp;

            while ( ! resultHandler.hasResult() ) {
                try {
                    //add live streaming
                    while ((tmp = r.readLine()) != null) {
                        //Do something with tmp line
                        Log.debug(tmp);
                        String line = tmp + System.getProperty("line.separator");
                        byte[] bytes = line.getBytes();
                        os1.write(bytes);
                    }

                    resultHandler.waitFor();

                } catch (InterruptedException e) {
                    //do nothing
                } catch (IOException e) {
                    Log.error("", e);
                }
            }

            try {
                r.close();
                reader.close();
            } catch (IOException e) {
                Log.error("", e);
            }


        }

        if ( blocking ) {
            int exitValue = resultHandler.getExitValue();
            Log.debug("Command execution exitValue is " + exitValue);
            if (executor.isFailure(exitValue)) {
                Log.debug("Command execution failed");
            } else {
                Log.debug("Command execution successful");
            }
        }

        return os1;
    }


    public void startApp(String pathToApp, String args){
        File workingDir = FileCore.getTempDir();
        String port = Storage.get("Environment.Active.WebDrivers.WiniumDesktop.port");
        String url = "http://localhost:" + port;
        Log.debug("Url is " + url);
        URL uri = null;
        try {
            uri = new URL(url);
        } catch (MalformedURLException e) {
            Log.error("", e);
        }

        Log.debug("Trying to run an app from " + pathToApp);
        DesktopOptions options = new DesktopOptions();
        options.setApplicationPath(pathToApp);
        if ( args != null && (! args.equals("")) ) {
            Log.debug("Setting application arguments " + args);
            options.setArguments(args);
        } else {
            Log.debug("No application arguments provided " + args);
        }

        String path = Storage.get("Environment.Active.WebDrivers.WiniumDesktop.path");
        //Set winium driver path
        //System.setProperty("webdriver.winium.driver.desktop", FileCore.getProjectPath() + File.separator + path);
        //WiniumDriverService service = new WiniumDriverService.Builder()
        //        .usingPort(Integer.valueOf(port)).withVerbose(true).withSilent(false).build();
        //service.start(); //Build and Start a Winium Driver service

        execute(FileCore.getProjectPath() + File.separator + path + " --port " + port, workingDir, 60, false);

        WiniumDriver App = new WiniumDriver(uri, options);
        Boolean closeAppDriver = Storage.get("Environment.Active.WebDrivers.WiniumDesktop.CloseAppAfterScenario");
        if ( closeAppDriver ) {
            scenarioCtx.put("App", WiniumDriver.class, App);
            scenarioCtx.put("WiniumDriverStartedOnLocalHost", Boolean.class, true);
        } else {
            globalCtx.put("App", WiniumDriver.class, App);
            globalCtx.put("WiniumDriverStartedOnLocalHost", Boolean.class, true);
        }

        //initialize winiumCore with driver
        WiniumCore winiumCore = new WiniumCore();
        scenarioCtx.put("WiniumCore", WiniumCore.class, winiumCore);

        if ( args != null && (! args.equals("")) ){
            Log.debug("Started an app from " + pathToApp + " " + args);
        } else {
            Log.debug("Started an app from " + pathToApp);
        }


    }


    public void closeWiniumResources(){
        File workingDir = FileCore.getTempDir();

        String cmd = "Get-CimInstance Win32_Process | Where {$_.name -match '.*Winium.*'} | Select Caption, CommandLine, ProcessId | Format-list";
        ByteArrayOutputStream out = execute("Powershell.exe \"" + cmd + "\"", workingDir, 60, true);
        String result = new String(out.toByteArray(), Charset.defaultCharset());
        if ( result.contains("Winium.Desktop.Driver.exe") ) {
            Log.debug("Closing Winium.Desktop.Driver.exe");
            String[] tmp = StringUtils.deleteWhitespace(result.trim()).split("ProcessId:");
            String processId = tmp[tmp.length - 1].trim();
            cmd = "Stop-Process -Id " + processId + " -Force -passThru";
            execute("Powershell.exe \"" + cmd + "\"", workingDir, 60, true);
        }

    }


}