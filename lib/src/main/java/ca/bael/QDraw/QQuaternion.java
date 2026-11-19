// Bailey JT Brown
// 2026
// QQuaternion.java

package ca.bael.QDraw;

public final class QQuaternion {
    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private float w;
    private float x;
    private float y;
    private float z;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public static QQuaternion Identity( ) {
        return new QQuaternion(1.0f, 0.0f, 0.0f, 0.0f);
    }

    public static QQuaternion fromAxisAngle(QVector3 axis, float degrees) {
        float halfAngle = -0.5f * degrees;
        float s = QMatrix4x4.sinDeg(halfAngle) / axis.magnitude( );
        return new QQuaternion(
            QMatrix4x4.cosDeg(halfAngle),
            axis.getX( ) * s,
            axis.getY( ) * s,
            axis.getZ( ) * s
        );
    }

    public static QQuaternion multiply(QQuaternion a, QQuaternion b) {
        return new QQuaternion(
            a.w * b.w - a.x * b.x - a.y * b.y - a.z * b.z,
            a.w * b.x + a.x * b.w + a.y * b.z - a.z * b.y,
            a.w * b.y - a.x * b.z + a.y * b.w + a.z * b.x,
            a.w * b.z + a.x * b.y - a.y * b.x + a.z * b.w
        );
    }

    public float getW( ) {
        return w;
    }

    public float getX( ) {
        return x;
    }

    public float getY( ) {
        return y;
    }

    public float getZ( ) {
        return z;
    }

    public QQuaternion set(QQuaternion q) {
        w = q.w;
        x = q.x;
        y = q.y;
        z = q.z;
        return this;
    }

    public QQuaternion normalize( ) {
        float invMag = 1.0f / (float)Math.sqrt(w * w + x * x + y * y + z * z);
        w *= invMag;
        x *= invMag;
        y *= invMag;
        z *= invMag;
        return this;
    }

    public QQuaternion conjugate( ) {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    public QQuaternion rotateLocal(QVector3 axis, float degrees) {
        return set(multiply(this, fromAxisAngle(axis, degrees))).normalize( );
    }

    public QQuaternion rotateGlobal(QVector3 axis, float degrees) {
        return set(multiply(fromAxisAngle(axis, degrees), this)).normalize( );
    }

    public QVector3 rotate(QVector3 v) {
        float tx = 2.0f * (y * v.getZ( ) - z * v.getY( ));
        float ty = 2.0f * (z * v.getX( ) - x * v.getZ( ));
        float tz = 2.0f * (x * v.getY( ) - y * v.getX( ));
        v.set(
            v.getX( ) + w * tx + y * tz - z * ty,
            v.getY( ) + w * ty + z * tx - x * tz,
            v.getZ( ) + w * tz + x * ty - y * tx
        );
        return v;
    }

    public QMatrix4x4 toMatrix( ) {
        float xx = x * x, yy = y * y, zz = z * z;
        float xy = x * y, xz = x * z, yz = y * z;
        float wx = w * x, wy = w * y, wz = w * z;
        return new QMatrix4x4(new float[] {
            1.0f - 2.0f * (yy + zz), 2.0f * (xy - wz),        2.0f * (xz + wy),        0.0f,
            2.0f * (xy + wz),        1.0f - 2.0f * (xx + zz), 2.0f * (yz - wx),        0.0f,
            2.0f * (xz - wy),        2.0f * (yz + wx),        1.0f - 2.0f * (xx + yy), 0.0f,
            0.0f,                    0.0f,                    0.0f,                    1.0f
        });
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public QQuaternion(float w, float x, float y, float z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public QQuaternion(QQuaternion q) {
        set(q);
    }
}
