// Bailey JT Brown
// 2026
// QTimer.java

package ca.bael.QDraw;

public final class QTimer {
    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private long startNanos;
    private long totalNanos;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void start( ) {
        startNanos = System.nanoTime( );
    }

    public void stop( ) {
        totalNanos += System.nanoTime( ) - startNanos;
    }

    public double millisPer(int count) {
        return totalNanos / 1e6 / count;
    }

    public void reset( ) {
        totalNanos = 0;
    }
}
