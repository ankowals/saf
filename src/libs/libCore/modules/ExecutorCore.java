package libs.libCore.modules;

import org.apache.commons.exec.*;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.io.*;
import java.util.Vector;

public class ExecutorCore {

    /**
     * Execute a Command as a background or blocking process.
     *
     * @param cmd     String, Command to execute
     * @param workingDir  File, Working directory
     * @param timeout     Integer, Kill process after this time (in sec) (0: no timeout)
     * @return  String, contains content of StdOut/StdErr output
     */
    public ExecResult execute(String cmd, File workingDir, int timeout)
    {

        if (timeout <= 0) {
            Log.error("Please timeout in seconds!");
        }

        Executor executor = new DefaultExecutor();
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        ExecuteWatchdog watchdog = new ExecuteWatchdog(1000 * timeout);
        ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer(); //This is used to end the process when the JVM exits

        ExecutorLogOutputStream os = new ExecutorLogOutputStream();
        ExecutorLogOutputStream es = new ExecutorLogOutputStream();

        PumpStreamHandler psh = new PumpStreamHandler(os, es);

        executor.setStreamHandler(psh);
        executor.setWatchdog(watchdog);
        executor.setProcessDestroyer(processDestroyer);
        executor.setWorkingDirectory(workingDir);

        try {
            Log.debug("Command to execute is " + cmd);
            Log.debug("Working dir is " + workingDir.getAbsolutePath());
            executor.execute(fixCommandLine(CommandLine.parse(cmd)), resultHandler);
            resultHandler.waitFor();
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            Log.error(e.getMessage());
        }

        return new ExecResult(os.getOutput(), es.getOutput(), resultHandler.getExitValue());
    }

    private CommandLine fixCommandLine(CommandLine badCommandLine) {
        try {
            CommandLine fixedCommandLine = new CommandLine(badCommandLine.getExecutable());
            fixedCommandLine.setSubstitutionMap(badCommandLine.getSubstitutionMap());
            Vector<?> arguments = (Vector<?>) FieldUtils.readField(badCommandLine, "arguments", true);
            arguments.stream()
                    .map(badArgument -> {
                        try {
                            return (String) FieldUtils.readField(badArgument, "value", true);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .forEach(goodArgument -> fixedCommandLine.addArgument(goodArgument, false));
            return fixedCommandLine;
        } catch (Exception e) {
            Log.warn("Cannot fix command line " + e.getMessage());
            return badCommandLine;
        }
    }

}