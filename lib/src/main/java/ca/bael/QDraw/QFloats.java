// Bailey JT Brown
// 2026
// QFloats.java

package ca.bael.QDraw;

public final class QFloats implements QUniform<QFloats> {
    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private final float[] values;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public int count( ) {
        return values.length;
    }

    public float get(int index) {
        return values[index];
    }

    public QFloats snapshot( ) {
        return this;
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    public QFloats(float... _values) {
        values = _values.clone( );
    }
}
