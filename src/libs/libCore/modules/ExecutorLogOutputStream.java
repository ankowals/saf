package libs.libCore.modules;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.exec.LogOutputStream;

public class ExecutorLogOutputStream extends LogOutputStream {

    private List<String> lines = new ArrayList<>();

    ExecutorLogOutputStream(int logLevel) {
        super(logLevel);
    }

    @Override
    protected void processLine(String line, int level) {
        if ( level == 0 ) {
            Log.debug(line);
        } else {
            Log.warn(line);
        }
        lines.add(line);
    }

    String getOutput() {
        return String.join(System.getProperty("line.separator"), lines);
    }
}
