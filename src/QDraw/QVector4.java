// Bailey JT Brown
// 2023
// QVector4.java

package QDraw;

import QDraw.QException.PointOfError;

public final class QVector4 {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final int INDEX_X = 0;
    private static final int INDEX_Y = 1;
    private static final int INDEX_Z = 2;
    private static final int INDEX_W = 3;
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
    public  static final float   COMPARE_EPSILON = 0.0005f;

    public static final int COMPONENT_COUNT = 4;
    public static final QVector4 zero4 = new QVector4(COMPONENTS_0000);
    public static final QVector4 zero3 = new QVector4(COMPONENTS_0001);
    public static final QVector4 one   = new QVector4(COMPONENTS_1111);
    public static final QVector4 x3    = new QVector4(COMPONENTS_XW1);
    public static final QVector4 x4    = new QVector4(COMPONENTS_XW0);
    public static final QVector4 y3    = new QVector4(COMPONENTS_YW1);
    public static final QVector4 y4    = new QVector4(COMPONENTS_YW0);
    public static final QVector4 z3    = new QVector4(COMPONENTS_ZW1);
    public static final QVector4 z4    = new QVector4(COMPONENTS_ZW0);
    public static final QVector4 w     = new QVector4(COMPONENTS_W);
    
    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private float[] components = new float[COMPONENT_COUNT];

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private static boolean compareFloatToEpsilon(float f1, float f2) {
        return Math.abs(f1 - f2) < COMPARE_EPSILON;
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public float getX( ) {
        return components[INDEX_X];
    }

    public QVector4 setX(float val) {
        components[INDEX_X] = val;
        return this;
    }

    public float getY( ) {
        return components[INDEX_Y];
    }

    public QVector4 setY(float val) {
        components[INDEX_Y] = val;
        return this;
    }

    public float getZ( ) {
        return components[INDEX_Z];
    }

    public QVector4 setZ(float val) {
        components[INDEX_Z] = val;
        return this;
    }

    public float getW( ) {
        return components[INDEX_W];
    }

    public QVector4 setW(float val) {
        components[INDEX_W] = val;
        return this;
    }

    public float[] getComponents( ) {
        return components;
    }

    public float sqrMagnitude3( ) {
        return components[INDEX_X] * components[INDEX_X] +
               components[INDEX_Y] * components[INDEX_Y] + 
               components[INDEX_Z] * components[INDEX_Z];
    }

    public float magnitude3( ) {
        return (float)Math.sqrt((float)sqrMagnitude3( ));
    }

    public float sqrMagnitude4( ) {
        return components[INDEX_X] * components[INDEX_X] +
               components[INDEX_Y] * components[INDEX_Y] + 
               components[INDEX_Z] * components[INDEX_Z] +
               components[INDEX_W] * components[INDEX_W];
    }

    public float magnitude4( ) {
        return (float)Math.sqrt((float)sqrMagnitude4( ));
    }

    public QVector4 normalize3( ) {
        float mag3 = magnitude3( );
        return multiply3(1.0f / mag3);
    }

    public QVector4 normalize4( ) {
        float mag4 = magnitude4( );
        return multiply4(1.0f / mag4);
    }

    public static QVector4 add3(QVector4 v1, QVector4 v2) {
        // TODO: possibly optimize (ensure to test first)
        return new QVector4(
            v1.getX() + v2.getX(),
            v1.getY() + v2.getY(),
            v1.getZ() + v2.getZ()
        );
    }

    public QVector4 add3(QVector4 other) {
        components[INDEX_X] += other.getX();
        components[INDEX_Y] += other.getY();
        components[INDEX_Z] += other.getZ();
        components[INDEX_W]  = 1.0f;
        return this;
    }

    public static QVector4 add4(QVector4 v1, QVector4 v2) {
        // TODO: possibly optimize (ensure to test first)
        return new QVector4(
            v1.getX() + v2.getX(),
            v1.getY() + v2.getY(),
            v1.getZ() + v2.getZ(),
            v1.getW() + v2.getW()
        );
    }

    public QVector4 add4(QVector4 other) {
        components[INDEX_X] += other.getX();
        components[INDEX_Y] += other.getY();
        components[INDEX_Z] += other.getZ();
        components[INDEX_W] += other.getW();
        return this;
    }

    public static QVector4 multiply3(QVector4 v, float factor) {
        return new QVector4(
            v.getX() * factor,
            v.getY() * factor,
            v.getZ() * factor
        );
    }

    public QVector4 multiply3(float factor) {
        components[INDEX_X] *= factor;
        components[INDEX_Y] *= factor;
        components[INDEX_Z] *= factor;
        components[INDEX_W]  = 1.0f;
        return this;
    }

    public static QVector4 multiply4(QVector4 v, float factor) {
        return new QVector4(
            v.getX() * factor,
            v.getY() * factor,
            v.getZ() * factor,
            v.getW() * factor
        );
    }

    public QVector4 multiply4(float factor) {
        components[INDEX_X] *= factor;
        components[INDEX_Y] *= factor;
        components[INDEX_Z] *= factor;
        components[INDEX_W] *= factor;
        return this;
    }

    public static QVector4 convertTo3(QVector4 v) {
        float[] components = v.components;
        components[INDEX_W] = 1.0f;
        return new QVector4(components);
    }

    public QVector4 convertTo3( ) {
        components[INDEX_W] = 1.0f;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QVector4))
            return false;

        QVector4 v2 = (QVector4)o;
        
        return compareFloatToEpsilon(v2.getX(), components[INDEX_X]) &&
               compareFloatToEpsilon(v2.getY(), components[INDEX_Y]) &&
               compareFloatToEpsilon(v2.getZ(), components[INDEX_Z]) &&
               compareFloatToEpsilon(v2.getW(), components[INDEX_W]); 
    }

    public boolean equals3(Object o) {
        if (!(o instanceof QVector4))
            return false;

        QVector4 v2 = (QVector4)o;
        
        return compareFloatToEpsilon(v2.getX(), components[INDEX_X]) &&
               compareFloatToEpsilon(v2.getY(), components[INDEX_Y]) &&
               compareFloatToEpsilon(v2.getZ(), components[INDEX_Z]);
    }

    @Override
    public String toString( ) {
        return String.format(
                "(%f, %f, %f, %f)",
                components[INDEX_X],
                components[INDEX_Y],
                components[INDEX_Z],
                components[INDEX_W]
            );
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public QVector4( ) {
        System.arraycopy(
            COMPONENTS_0001, 
            0, 
            components, 
            0, 
            COMPONENT_COUNT
        );
    }

    public QVector4(QVector4 v) {
        System.arraycopy(
            v.components, 
            0, 
            components, 
            0, 
            COMPONENT_COUNT
        );
    }

    public QVector4(float x, float y, float z) {
        components[INDEX_X] = x;
        components[INDEX_Y] = y;
        components[INDEX_Z] = z;
        components[INDEX_W] = 1.0f;
    }

    public QVector4(float x, float y, float z, float w) {
        components[INDEX_X] = x;
        components[INDEX_Y] = y;
        components[INDEX_Z] = z;
        components[INDEX_W] = w;
    }

    public QVector4(float[] vec) {
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
            COMPONENT_COUNT
        );

        // TODO: possibly optimize with ArrayCopy (test first!!!!)
        switch (vec.length) {
            case 4:
                components[INDEX_W] = vec[INDEX_W];

            case 3:
                components[INDEX_X] = vec[INDEX_X];
                components[INDEX_Y] = vec[INDEX_Y];
                components[INDEX_Z] = vec[INDEX_Z];
                break;

            default:
                throw new QException(
                    PointOfError.InvalidData, 
                    "vector can only be constructed from size 3 or 4 array. " +
                    "given was size " + vec.length
                );
        }
    }
}
