// Bailey JT Brown
// 2024
// QLight.java

package QDraw;

public class QLight {
    /////////////////////////////////////////////////////////////////
    // PUBLIC MEMBERS
    public QVector3 position;
    public QVector3 direction;
    public float    spread;
    public float    strength;

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private void init(
        QVector3 _position,
        QVector3 _direction,
        float    _spread,
        float    _strength
    ) {
        position  = new QVector3(_position);
        direction = new QVector3(_direction);
        strength  = _strength;
        spread    = _spread;
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    public QLight(QVector3 _position, float _strength) {
        init(_position, new QVector3( ), 0.0f, _strength);
    }

    public QLight(
        QLight toCopy
    ) {
        init(
            toCopy.position,
            toCopy.direction,
            toCopy.spread,
            toCopy.strength
        );
    }

    public QLight(
        QVector3 _position,
        QVector3 _direction,
        float    _spread,
        float    _strength
    ) {
        init(_position, _direction, _spread, _strength);
    }
}
