// Bailey JT Brown
// 2026
// QIndexBuffer.java

package ca.bael.QDraw;

import ca.bael.QDraw.QException.PointOfError;

public final class QIndexBuffer {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final int INDICIES_PER_TRI = 3;

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private final int[] indicies;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public int[] getIndicies( ) {
        return indicies;
    }

    public int getTriCount( ) {
        return indicies.length / INDICIES_PER_TRI;
    }

    public void set(int triNum, int i0, int i1, int i2) {
        int base = triNum * INDICIES_PER_TRI;
        indicies[base]     = i0;
        indicies[base + 1] = i1;
        indicies[base + 2] = i2;
    }

    public static QIndexBuffer wrap(int[] inIndicies) {
        return new QIndexBuffer(inIndicies);
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    private QIndexBuffer(int[] inIndicies) {
        if ((inIndicies.length % INDICIES_PER_TRI) != 0) {
            throw new QException(
                PointOfError.InvalidData,
                "index count must be multiple of " + INDICIES_PER_TRI +
                ". Given was " + inIndicies.length
            );
        }
        indicies = inIndicies;
    }

    public QIndexBuffer(int triCount) {
        this(new int[triCount * INDICIES_PER_TRI]);
    }
}
