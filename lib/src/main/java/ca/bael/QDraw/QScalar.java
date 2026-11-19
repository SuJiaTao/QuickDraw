// Bailey JT Brown
// 2026
// QScalar.java

package ca.bael.QDraw;

public final class QScalar implements QUniform<QScalar> {
    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private final double value;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public float floatValue( ) {
        return (float)value;
    }

    public int intValue( ) {
        return (int)value;
    }

    public double doubleValue( ) {
        return value;
    }

    public QScalar snapshot( ) {
        return this;
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    public QScalar(double _value) {
        value = _value;
    }
}
