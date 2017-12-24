package libs.libCore.modules;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Used to add log to file support.
 * We want to log all exceptions throw during cucumber execution via junit runner
 */
public class JunitListenerWithLogger extends RunListener {

    private static String stacktrace = "";

    @Override
    public void testFailure(Failure failure) throws Exception {
        //Log exception caught by junit runner
        Throwable e = failure.getException();
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        stacktrace = sw.toString();
        //it will be printed here multiple times so we print it in the scenario after hook
        //Log.warn(e.getMessage());
    }

    public static String getStacktrace(){
        return stacktrace;
    }
}
