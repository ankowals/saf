package libs.libCore.modules;

public class SSHResult {
    private String stdout;
    private String stderr;
    private Integer exitCode;

    public SSHResult(String stdout, String stderr, Integer exitCode) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() { return stderr; }

    public Integer getExitCode() {
        return exitCode;
    }

}
