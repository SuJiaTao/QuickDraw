// Bailey JT Brown
// 2023
// QVector.java

package QDraw;

import QDraw.QException.PointOfError;

public final class QVector extends QEncoding {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final float[] COMPONENTS_0000 = {0.0f, 0.0f, 0.0f, 0.0f};
    private static final float[] COMPONENTS_0001 = {0.0f, 0.0f, 0.0f, 1.0f};
    private static final float[] COMPONENTS_1111 = {1.0f, 1.0f, 1.0f, 1.0f};
    private static final float[] COMPONENTS_XW1  = {1.0f, 0.0f, 0.0f, 1.0f};
    private static final float[] COMPONENTS_XW0  = {1.0f, 0.0f, 0.0f, 0.0f};
    private static final float[] COMPONENTS_YW1  = {0.0f, 1.0f, 0.0f, 1.0f};
    private static final float[] COMPONENTS_YW0  = {0.0f, 1.0f, 0.0f, 0.0f};
    private static final float[] COMPONENTS_ZW1  = {0.0f, 0.0f, 1.0f, 1.0f};
    private static final float[] COMPONENTS_ZW0  = {0.0f, 0.0f, 1.0f, 0.0f};
    private static final float[] COMPONENTS_W    = {0.0f, 0.0f, 0.0f, 1.0f};
    
    public static final QVector Zero4 = new QVector(COMPONENTS_0000);
    public static final QVector Zero3 = new QVector(COMPONENTS_0001);
    public static final QVector One   = new QVector(COMPONENTS_1111);
    public static final QVector X3    = new QVector(COMPONENTS_XW1);
    public static final QVector X4    = new QVector(COMPONENTS_XW0);
    public static final QVector Y3    = new QVector(COMPONENTS_YW1);
    public static final QVector Y4    = new QVector(COMPONENTS_YW0);
    public static final QVector Z3    = new QVector(COMPONENTS_ZW1);
    public static final QVector Z4    = new QVector(COMPONENTS_ZW0);
    public static final QVector W     = new QVector(COMPONENTS_W);
    
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
    public float getX( ) {
        return components[VCTR_INDEX_X];
    }

    public QVector setX(float val) {
        components[VCTR_INDEX_X] = val;
        return this;
    }

    public float getY( ) {
        return components[VCTR_INDEX_Y];
    }

    public QVector setY(float val) {
        components[VCTR_INDEX_Y] = val;
        return this;
    }

    public float getZ( ) {
        return components[VCTR_INDEX_Z];
    }

    public QVector setZ(float val) {
        components[VCTR_INDEX_Z] = val;
        return this;
    }

    public float getW( ) {
        return components[VCTR_INDEX_W];
    }

    public QVector setW(float val) {
        components[VCTR_INDEX_W] = val;
        return this;
    }

    public float[] getComponents( ) {
        return components;
    }

    public float sqrMagnitude3( ) {
        return components[VCTR_INDEX_X] * components[VCTR_INDEX_X] +
               components[VCTR_INDEX_Y] * components[VCTR_INDEX_Y] + 
               components[VCTR_INDEX_Z] * components[VCTR_INDEX_Z];
    }

    public float magnitude3( ) {
        return (float)Math.sqrt((float)sqrMagnitude3( ));
    }

    public float sqrMagnitude4( ) {
        return components[VCTR_INDEX_X] * components[VCTR_INDEX_X] +
               components[VCTR_INDEX_Y] * components[VCTR_INDEX_Y] + 
               components[VCTR_INDEX_Z] * components[VCTR_INDEX_Z] +
               components[VCTR_INDEX_W] * components[VCTR_INDEX_W];
    }

    public float magnitude4( ) {
        return (float)Math.sqrt((float)sqrMagnitude4( ));
    }

    public QVector normalize3( ) {
        float mag3 = magnitude3( );
        return multiply3(1.0f / mag3);
    }

    public QVector normalize4( ) {
        float mag4 = magnitude4( );
        return multiply4(1.0f / mag4);
    }

    public static QVector add3(QVector v1, QVector v2) {
        // TODO: possibly optimize (ensure to test first)
        return new QVector(
            v1.getX() + v2.getX(),
            v1.getY() + v2.getY(),
            v1.getZ() + v2.getZ()
        );
    }

    public QVector add3(QVector other) {
        components[VCTR_INDEX_X] += other.getX();
        components[VCTR_INDEX_Y] += other.getY();
        components[VCTR_INDEX_Z] += other.getZ();
        components[VCTR_INDEX_W]  = 1.0f;
        return this;
    }

    public static QVector add4(QVector v1, QVector v2) {
        // TODO: possibly optimize (ensure to test first)
        return new QVector(
            v1.getX() + v2.getX(),
            v1.getY() + v2.getY(),
            v1.getZ() + v2.getZ(),
            v1.getW() + v2.getW()
        );
    }

    public QVector add4(QVector other) {
        components[VCTR_INDEX_X] += other.getX();
        components[VCTR_INDEX_Y] += other.getY();
        components[VCTR_INDEX_Z] += other.getZ();
        components[VCTR_INDEX_W] += other.getW();
        return this;
    }

    public static QVector multiply3(QVector v, float factor) {
        return new QVector(
            v.getX() * factor,
            v.getY() * factor,
            v.getZ() * factor
        );
    }

    public QVector multiply3(float factor) {
        components[VCTR_INDEX_X] *= factor;
        components[VCTR_INDEX_Y] *= factor;
        components[VCTR_INDEX_Z] *= factor;
        components[VCTR_INDEX_W]  = 1.0f;
        return this;
    }

    public static QVector multiply4(QVector v, float factor) {
        return new QVector(
            v.getX() * factor,
            v.getY() * factor,
            v.getZ() * factor,
            v.getW() * factor
        );
    }

    public QVector multiply4(float factor) {
        components[VCTR_INDEX_X] *= factor;
        components[VCTR_INDEX_Y] *= factor;
        components[VCTR_INDEX_Z] *= factor;
        components[VCTR_INDEX_W] *= factor;
        return this;
    }

    public static QVector convertTo3(QVector v) {
        float[] components = v.components;
        components[VCTR_INDEX_W] = 1.0f;
        return new QVector(components);
    }

    public QVector convertTo3( ) {
        components[VCTR_INDEX_W] = 1.0f;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QVector))
            return false;

        QVector v2 = (QVector)o;
        
        return compareFloatToEpsilon(v2.getX(), components[VCTR_INDEX_X]) &&
               compareFloatToEpsilon(v2.getY(), components[VCTR_INDEX_Y]) &&
               compareFloatToEpsilon(v2.getZ(), components[VCTR_INDEX_Z]) &&
               compareFloatToEpsilon(v2.getW(), components[VCTR_INDEX_W]); 
    }

    public boolean equals3(Object o) {
        if (!(o instanceof QVector))
            return false;

        QVector v2 = (QVector)o;
        
        return compareFloatToEpsilon(v2.getX(), components[VCTR_INDEX_X]) &&
               compareFloatToEpsilon(v2.getY(), components[VCTR_INDEX_Y]) &&
               compareFloatToEpsilon(v2.getZ(), components[VCTR_INDEX_Z]);
    }

    @Override
    public String toString( ) {
        return String.format(
                "(%f, %f, %f, %f)",
                components[VCTR_INDEX_X],
                components[VCTR_INDEX_Y],
                components[VCTR_INDEX_Z],
                components[VCTR_INDEX_W]
            );
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public QVector( ) {
        System.arraycopy(
            COMPONENTS_0001, 
            0, 
            components, 
            0, 
            VCTR_NUM_CMPS
        );
    }

    public QVector(QVector v) {
        System.arraycopy(
            v.components, 
            0, 
            components, 
            0, 
            VCTR_NUM_CMPS
        );
    }

    public QVector(float x, float y) {
        components[VCTR_INDEX_X] = x;
        components[VCTR_INDEX_Y] = y;
        components[VCTR_INDEX_Z] = 0.0f;
        components[VCTR_INDEX_W] = 1.0f;
    }

    public QVector(float x, float y, float z) {
        components[VCTR_INDEX_X] = x;
        components[VCTR_INDEX_Y] = y;
        components[VCTR_INDEX_Z] = z;
        components[VCTR_INDEX_W] = 1.0f;
    }

    public QVector(float x, float y, float z, float w) {
        components[VCTR_INDEX_X] = x;
        components[VCTR_INDEX_Y] = y;
        components[VCTR_INDEX_Z] = z;
        components[VCTR_INDEX_W] = w;
    }

    public QVector(float[] vec) {
        if (vec == null) {
            throw new QException(
                PointOfError.NullParameter, 
                "cannot construct vector from null array"
            );
        }

        System.arraycopy(
            COMPONENTS_0001, 
            0, 
            components, 
            0, 
            VCTR_NUM_CMPS
        );

        // TODO: possibly optimize with ArrayCopy (test first!!!!)
        switch (vec.length) {
            case 4:
                components[VCTR_INDEX_W] = vec[VCTR_INDEX_W];

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
                    "vector can only be constructed from size 2, 3 or 4 array. " +
                    "given was size " + vec.length
                );
        }
    }
}
