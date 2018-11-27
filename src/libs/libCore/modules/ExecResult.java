package libs.libCore.modules;

public class ExecResult {

    private String stdout;
    private String stderr;
    private Integer exitCode;

    /**
     * Constructor
     *
     * Creates new set of ExecResult
     *
     */
    public ExecResult(String stdout, String stderr, Integer exitCode) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
    }


    /**
     *
     * Returns content of standard output
     *
     */
    public String getStdOut() {
        return stdout;
    }


    /**
     *
     * Returns content of standard error
     *
     */
    public String getStdErr() { return stderr; }


    /**
     *
     * Returns value of exit status code
     *
     */
    public Integer getExitCode() {
        return exitCode;
    }

}