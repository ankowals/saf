package modules.core;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import org.apache.logging.log4j.Logger;

/**
 * A wrapper class which takes a logger as constructor argument and offers a PrintStream whose flush
 * method writes the written content to the supplied logger (debug level).
 * <p>
 * Usage:<br>
 * initializing in @BeforeClass of the unit test:
 * <pre>
 *          ToLoggerPrintStream loggerPrintStream = new ToLoggerPrintStream( myLog );
 *          RestAssured.config = RestAssured.config().logConfig(
 *                                 new LogConfig( loggerPrintStream.getPrintStream(), true ) );
 * </pre>
 * will redirect all log outputs of a ValidatableResponse to the supplied logger:
 * <pre>
 *             resp.then().log().all( true );
 * </pre>
 *
 * @version 1.0 (28.10.2015)
 * @author  Heri Bender
 */
public class ToLoggerPrintStream {
    /**
     * Logger for this class
     */
    private Logger myLog;
    private PrintStream myPrintStream;

    /**
     * @return printStream
     */
    public PrintStream getPrintStream() {
        if (myPrintStream == null) {
            OutputStream output = new OutputStream() {
                private StringBuilder myStringBuilder = new StringBuilder();

                @Override
                public void write(int b) throws IOException {
                    this.myStringBuilder.append((char) b);
                }

                /**
                 * @see java.io.OutputStream#flush()
                 */
                @Override
                public void flush() {
                    myLog.debug(this.myStringBuilder.toString());
                    myStringBuilder = new StringBuilder();
                }
            };

            myPrintStream = new PrintStream(output, true);  // true: autoflush must be set!
        }

        return myPrintStream;
    }

    /**
     * Constructor
     *
     * @param aLogger
     */
    public ToLoggerPrintStream(Logger aLogger) {
        super();
        myLog = aLogger;
    }
}
