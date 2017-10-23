package libs.libCore.modules;

public class ExecutionTimer {

    private long start;
    private long end;

    public ExecutionTimer() {
        reset();
        start = System.currentTimeMillis();
    }

    /**
     * stop counting elapsed time
     */
    public void end() {
        end = System.currentTimeMillis();
    }

    /**
     * Calculate the difference between start and stop time
     *
     * @return elasped time in ms
     */
    public long duration(){
        return (end-start);
    }

    /**
     * reset start tiem and stop time
     */
    public void reset() {
        start = 0;
        end   = 0;
    }

}
