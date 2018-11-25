package libs.libCore.modules;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.exec.LogOutputStream;

public class ExecutorLogOutputStream extends LogOutputStream {

    private List<String> lines = new ArrayList<>();

    @Override protected void processLine(String line, int level) {
        Log.debug(line);
        lines.add(line);
    }

    public String getOutput() {
        return String.join(System.getProperty("line.separator"), lines);
    }
}
