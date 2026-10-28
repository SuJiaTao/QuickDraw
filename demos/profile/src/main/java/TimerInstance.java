// Bailey JT Brown
// 2024
// TimerInstance.java

public final class TimerInstance {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final int SAVE_TIME_BUFFER_SIZE = 10000;

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private long   lastTimeMiSec;
    private int    testCount;
    private long[] dtMiSecBuffer = new long[SAVE_TIME_BUFFER_SIZE];

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    public TimerInstance( ) {
        reset( );
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void reset( ) {
        testCount = 0;
    }

    public long getTimeMicroseconds( ) {
        return System.nanoTime( ) >> 10; // div 1024, rough conversion
    }

    public void beginTime( ) {
        lastTimeMiSec = getTimeMicroseconds( );
    }

    public void endTime( ) {
        long dt = getTimeMicroseconds( ) - lastTimeMiSec;
        dtMiSecBuffer[testCount % SAVE_TIME_BUFFER_SIZE] = dt;
        testCount++;
    }

    public long avgTime( ) {
        float sum = 0;
        int sumCount = Math.min(SAVE_TIME_BUFFER_SIZE, testCount);
        for (int i = 0; i < sumCount; i++) {
            sum += (float)dtMiSecBuffer[i];
        } 
        sum /= (float)sumCount;
        return (long)sum;
    }
}
