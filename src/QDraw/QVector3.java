// Bailey JT Brown
// 2023-2024
// QVector3.java

package QDraw;

import QDraw.QException.PointOfError;

public final class QVector3 extends QEncoding {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final float[] COMPONENTS_ZERO = { 0.0f, 0.0f, 0.0f };
    private static final float[] COMPONENTS_ONE  = { 1.0f, 1.0f, 1.0f };
    private static final float[] COMPONENTS_X    = { 0.0f, 0.0f, 0.0f };
    private static final float[] COMPONENTS_Y    = { 0.0f, 0.0f, 0.0f };
    private static final float[] COMPONENTS_Z    = { 0.0f, 0.0f, 0.0f };

    public static QVector3 Zero( ) {
        return new QVector3(COMPONENTS_ZERO);
    }

    public static QVector3 One( ) {
        return new QVector3(COMPONENTS_ONE);
    }

    public static QVector3 X( ) {
        return new QVector3(COMPONENTS_X);
    }

    public static QVector3 Y( ) {
        return new QVector3(COMPONENTS_Y);
    }

    public static QVector3 Z( ) {
        return new QVector3(COMPONENTS_Z);
    }
    
    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    // (refer to QEncoding.java for encoding details)
    private float[] components = new float[VCTR_NUM_CMPS];

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private static boolean compareFloatToEpsilon(float f1, float f2) {
        return Math.abs(f1 - f2) < VCTR_COMPARE_EPSILON;
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public QVector3 copy( ) {
        return new QVector3(this);
    }

    public float getX( ) {
        return components[VCTR_INDEX_X];
    }

    public QVector3 setX(float val) {
        components[VCTR_INDEX_X] = val;
        return this;
    }

    public float getY( ) {
        return components[VCTR_INDEX_Y];
    }

    public QVector3 setY(float val) {
        components[VCTR_INDEX_Y] = val;
        return this;
    }

    public float getZ( ) {
        return components[VCTR_INDEX_Z];
    }

    public QVector3 setZ(float val) {
        components[VCTR_INDEX_Z] = val;
        return this;
    }

    public float[] getComponents( ) {
        return components;
    }

    public float sqrMagnitude( ) {
        return QMath.sqrmag3(components);
    }

    public float magnitude( ) {
        return QMath.mag3(components);
    }

    public QVector3 normalize( ) {
        float mag3 = magnitude( );
        return multiply3(1.0f / mag3);
    }

    public static QVector3 add(QVector3 v1, QVector3 v2) {
        QVector3 rvec = v1.copy();
        rvec.add(v2);
        return rvec;
    }

    public QVector3 add(QVector3 other) {
        QMath.add3(components, other.components);
        return this;
    }

    public static QVector3 multiply3(QVector3 v, float factor) {
        QVector3 rvec = v.copy();
        rvec.multiply3(factor);
        return rvec;
    }

    public QVector3 multiply3(float factor) {
        QMath.mult3(components, factor);
        return this;
    }

    public void set(QVector3 other) {
        set(other.getComponents());
    }

    public void set(float[] comps) {
        QMath.copy3(0, components, 0, comps);
    }

    public void set(float x, float y, float z) {
        components[VCTR_INDEX_X] = x;
        components[VCTR_INDEX_Y] = y;
        components[VCTR_INDEX_Z] = z;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QVector3))
            return false;

        QVector3 v2 = (QVector3)o;
        
        return compareFloatToEpsilon(v2.getX(), components[VCTR_INDEX_X]) &&
               compareFloatToEpsilon(v2.getY(), components[VCTR_INDEX_Y]) &&
               compareFloatToEpsilon(v2.getZ(), components[VCTR_INDEX_Z]);
    }

    @Override
    public String toString( ) {
        return String.format(
                "(x:%f, y:%f, z:%f)",
                components[VCTR_INDEX_X],
                components[VCTR_INDEX_Y],
                components[VCTR_INDEX_Z]
            );
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public QVector3( ) {
        set(COMPONENTS_ZERO);
    }

    public QVector3(QVector3 v) {
        set(v.components);
    }

    public QVector3(float x, float y) {
        set(x, y, 0.0f);
    }

    public QVector3(float x, float y, float z) {
        set(x, y, z);
    }

    public QVector3(float[] vec) {
        if (vec == null) {
            throw new QException(
                PointOfError.NullParameter, 
                "cannot construct vector from null array"
            );
        }

        set(COMPONENTS_ZERO);

        switch (vec.length) {
            case 3:
                components[VCTR_INDEX_X] = vec[VCTR_INDEX_X];
                components[VCTR_INDEX_Y] = vec[VCTR_INDEX_Y];
                components[VCTR_INDEX_Z] = vec[VCTR_INDEX_Z];
                break;

            case 2:
                components[VCTR_INDEX_X] = vec[VCTR_INDEX_X];
                components[VCTR_INDEX_Y] = vec[VCTR_INDEX_Y];
                break;

            default:
                throw new QException(
                    PointOfError.InvalidData, 
                    "vector can only be constructed from size 2 or 3 array. " +
                    "given was size " + vec.length
                );
        }
    }
}
