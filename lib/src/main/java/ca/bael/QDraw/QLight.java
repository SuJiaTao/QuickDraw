// Bailey JT Brown
// 2024-2026
// QLight.java

package ca.bael.QDraw;

public class QLight implements QUniform<QLight> {
    /////////////////////////////////////////////////////////////////
    // PUBLIC MEMBERS
    public QVector3 position;
    public float    strength;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public QLight snapshot( ) {
        return new QLight(this);
    }

    public float illuminate(QVector3 point, QVector3 outDir) {
        outDir.set(position);
        float dist = outDir.sub(point).magnitude( );
        outDir.multiply3(1.0f / dist);
        return strength / dist;
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    public QLight(QVector3 _position, float _strength) {
        position = new QVector3(_position);
        strength = _strength;
    }

    public QLight(
        QLight toCopy
    ) {
        this(toCopy.position, toCopy.strength);
    }
}
