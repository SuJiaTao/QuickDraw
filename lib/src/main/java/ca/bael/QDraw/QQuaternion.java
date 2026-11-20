// Bailey JT Brown
// 2026
// QQuaternion.java

package ca.bael.QDraw;

public final class QQuaternion {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final float SLERP_LINEAR_THRESHOLD = 0.9995f;
    private static final float PARALLEL_EPSILON       = 1e-6f;
    private static final float ANTIPARALLEL_DOT       = -0.999999f;
    private static final float GIMBAL_THRESHOLD       = 0.99999f;

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

    public static QQuaternion nlerp(QQuaternion a, QQuaternion b, float t) {
        float s = internalDot(a, b) < 0.0f ? -1.0f : 1.0f;
        return new QQuaternion(
            a.w + (s * b.w - a.w) * t,
            a.x + (s * b.x - a.x) * t,
            a.y + (s * b.y - a.y) * t,
            a.z + (s * b.z - a.z) * t
        ).normalize( );
    }

    public static QQuaternion slerp(QQuaternion a, QQuaternion b, float t) {
        float d = internalDot(a, b);
        float s = d < 0.0f ? -1.0f : 1.0f;
        d *= s;
        if (d > SLERP_LINEAR_THRESHOLD) {
            return nlerp(a, b, t);
        }
        float theta  = (float)Math.acos(d);
        float invSin = 1.0f / (float)Math.sin(theta);
        float wa     = (float)Math.sin((1.0f - t) * theta) * invSin;
        float wb     = s * (float)Math.sin(t * theta) * invSin;
        return new QQuaternion(
            wa * a.w + wb * b.w,
            wa * a.x + wb * b.x,
            wa * a.y + wb * b.y,
            wa * a.z + wb * b.z
        );
    }

    public static QQuaternion fromEuler(float x, float y, float z) {
        return multiply(
            multiply(fromAxisAngle(new QVector3(1.0f, 0.0f, 0.0f), x), fromAxisAngle(new QVector3(0.0f, 1.0f, 0.0f), y)),
            fromAxisAngle(new QVector3(0.0f, 0.0f, 1.0f), z)
        );
    }

    public static QQuaternion fromMatrix(QMatrix4x4 m) {
        float[] c = m.getComponents( );
        float m00 = c[0], m01 = c[1], m02 = c[2];
        float m10 = c[4], m11 = c[5], m12 = c[6];
        float m20 = c[8], m21 = c[9], m22 = c[10];
        float trace = m00 + m11 + m22;
        if (trace > 0.0f) {
            float s = 0.5f / (float)Math.sqrt(trace + 1.0f);
            return new QQuaternion(0.25f / s, (m21 - m12) * s, (m02 - m20) * s, (m10 - m01) * s).normalize( );
        }
        if (m00 > m11 && m00 > m22) {
            float s = 2.0f * (float)Math.sqrt(1.0f + m00 - m11 - m22);
            return new QQuaternion((m21 - m12) / s, 0.25f * s, (m01 + m10) / s, (m02 + m20) / s).normalize( );
        }
        if (m11 > m22) {
            float s = 2.0f * (float)Math.sqrt(1.0f + m11 - m00 - m22);
            return new QQuaternion((m02 - m20) / s, (m01 + m10) / s, 0.25f * s, (m12 + m21) / s).normalize( );
        }
        float s = 2.0f * (float)Math.sqrt(1.0f + m22 - m00 - m11);
        return new QQuaternion((m10 - m01) / s, (m02 + m20) / s, (m12 + m21) / s, 0.25f * s).normalize( );
    }

    public static QQuaternion lookRotation(QVector3 forward, QVector3 up) {
        QVector3 back  = forward.copy( ).multiply3(-1.0f).normalize( );
        QVector3 right = QVector3.cross(up, back);
        if (right.magnitude( ) < PARALLEL_EPSILON) {
            right = QVector3.cross(Math.abs(back.getX( )) < 0.9f ? new QVector3(1.0f, 0.0f, 0.0f) : new QVector3(0.0f, 1.0f, 0.0f), back);
        }
        right.normalize( );
        QVector3 trueUp = QVector3.cross(back, right);
        return fromMatrix(new QMatrix4x4(new float[] {
            right.getX( ), trueUp.getX( ), back.getX( ), 0.0f,
            right.getY( ), trueUp.getY( ), back.getY( ), 0.0f,
            right.getZ( ), trueUp.getZ( ), back.getZ( ), 0.0f,
            0.0f,          0.0f,           0.0f,         1.0f
        }));
    }

    public static QQuaternion fromToRotation(QVector3 from, QVector3 to) {
        QVector3 a = from.copy( ).normalize( );
        QVector3 b = to.copy( ).normalize( );
        float    d = QVector3.dot(a, b);
        if (d < ANTIPARALLEL_DOT) {
            QVector3 axis = QVector3.cross(Math.abs(a.getX( )) < 0.9f ? new QVector3(1.0f, 0.0f, 0.0f) : new QVector3(0.0f, 1.0f, 0.0f), a).normalize( );
            return new QQuaternion(0.0f, axis.getX( ), axis.getY( ), axis.getZ( ));
        }
        QVector3 c = QVector3.cross(a, b);
        return new QQuaternion(1.0f + d, c.getX( ), c.getY( ), c.getZ( )).normalize( );
    }

    public static float angle(QQuaternion a, QQuaternion b) {
        return (float)Math.toDegrees(2.0 * Math.acos(Math.min(1.0f, Math.abs(internalDot(a, b)))));
    }

    public static QQuaternion rotateTowards(QQuaternion from, QQuaternion to, float maxDegrees) {
        float total = angle(from, to);
        if (total == 0.0f) {
            return new QQuaternion(to);
        }
        return slerp(from, to, Math.min(1.0f, maxDegrees / total));
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

    public QVector3 forward( ) {
        return rotate(new QVector3(0.0f, 0.0f, -1.0f));
    }

    public QVector3 up( ) {
        return rotate(new QVector3(0.0f, 1.0f, 0.0f));
    }

    public QVector3 right( ) {
        return rotate(new QVector3(1.0f, 0.0f, 0.0f));
    }

    public QVector3 toEuler( ) {
        float[] m = toMatrix( ).getComponents( );
        float   y = (float)Math.toDegrees(Math.asin(Math.max(-1.0f, Math.min(1.0f, -m[2]))));
        if (Math.abs(m[2]) > GIMBAL_THRESHOLD) {
            return new QVector3((float)Math.toDegrees(Math.atan2(-m[9], m[5])), y, 0.0f);
        }
        return new QVector3(
            (float)Math.toDegrees(Math.atan2(m[6], m[10])),
            y,
            (float)Math.toDegrees(Math.atan2(m[1], m[0]))
        );
    }

    public float toAxisAngle(QVector3 outAxis) {
        float cw = Math.max(-1.0f, Math.min(1.0f, w));
        float s  = (float)Math.sqrt(1.0f - cw * cw);
        if (s < PARALLEL_EPSILON) {
            outAxis.set(1.0f, 0.0f, 0.0f);
            return 0.0f;
        }
        outAxis.set(-x / s, -y / s, -z / s);
        return (float)Math.toDegrees(2.0 * Math.acos(cw));
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private static float internalDot(QQuaternion a, QQuaternion b) {
        return a.w * b.w + a.x * b.x + a.y * b.y + a.z * b.z;
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
