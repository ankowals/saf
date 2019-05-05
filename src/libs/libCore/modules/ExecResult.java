package libs.libCore.modules;

public class ExecResult {

    private String stdout;
    private String stderr;
    private Integer exitCode;

    public ExecResult(String stdout, String stderr, Integer exitCode) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
    }

    public String getStdOut() {
        return stdout;
    }

    public String getStdErr() { return stderr; }

    public Integer getExitCode() {
        return exitCode;
    }

}