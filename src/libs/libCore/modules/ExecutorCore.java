package libs.libCore.modules;

import org.apache.commons.exec.*;
import java.io.*;
import java.nio.charset.Charset;

public class ExecutorCore {

    private int exitValue;

    public Integer getExitCode(){
        return exitValue;
    }

    /**
     * Execute a Command as a background or blocking process.
     *
     * @param cmd     String, Command to execute
     * @param workingDir  File, Working directory
     * @param timeout     Integer, Kill process after this time (in sec) (0: no timeout)
     * @param blocking    Boolean,  Synchronous/blocking (true) or asynchronous/background startup (false).
     * @return  String, contains content of StdOut/StdErr output
     */
    public String execute(String cmd, File workingDir, int timeout, boolean blocking)
    {
        Executor executor = new DefaultExecutor();
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        CommandLine cmdLine = null;

        try {
            cmdLine = CommandLine.parse(cmd);
        } catch (IllegalArgumentException e) {
            Log.error(e.getMessage());
        }

        if (timeout > 0)
        {
            ExecuteWatchdog watchdog = new ExecuteWatchdog(1000 * timeout);
            executor.setWatchdog(watchdog);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);

        //live-streaming
        InputStream pis = null;
        if ( blocking ) {
            PipedOutputStream pos = new PipedOutputStream();
            try {
                pis = new PipedInputStream(pos);
            } catch (IOException e) {
                Log.error(e.getMessage());
            }
            executor.setStreamHandler(new PumpStreamHandler(pos));
        } else {
            executor.setStreamHandler(new PumpStreamHandler(bos));
        }

        //This is used to end the process when the JVM exits
        ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();
        executor.setProcessDestroyer(processDestroyer);

        executor.setWorkingDirectory(workingDir);

        Log.debug("Command to execute is " + cmd);
        Log.debug("Working dir is " + workingDir.getAbsolutePath());

        try {
            executor.execute(cmdLine, resultHandler);
        } catch (IOException e) {
            Log.error(e.getMessage());
        }

        if ( blocking ) {

            Reader reader = new InputStreamReader(pis);
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
                        bos.write(bytes);
                    }

                    resultHandler.waitFor();

                } catch (InterruptedException | IOException e) {
                    //do nothing
                }
            }

            try {
                r.close();
                reader.close();
            } catch (IOException e) {
                Log.error(e.getMessage());
            }


        }

        if ( blocking ) {
            exitValue = resultHandler.getExitValue();
            Log.debug("Command execution exitValue is " + exitValue);
            if (executor.isFailure(exitValue)) {
                Log.debug("Command execution failed");
            } else {
                Log.debug("Command execution successful");
            }
        }

        return new String(bos.toByteArray(), Charset.defaultCharset());
    }

}