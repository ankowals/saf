package libs.libCore.modules;

public class SSHResult {
    private String stdout;
    private String stderr;
    private Integer exitCode;


    /**
     * Constructor
     *
     * Creates new set of SSHResults
     *
     */
    public SSHResult(String stdout, String stderr, Integer exitCode) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
    }


    /**
     *
     * Returns content of standard output
     *
     */
    public String getStdout() {
        return stdout;
    }


    /**
     *
     * Returns content of standard error
     *
     */
    public String getStderr() { return stderr; }


    /**
     *
     * Returns value of exit status code
     *
     */
    public Integer getExitCode() {
        return exitCode;
    }

}
