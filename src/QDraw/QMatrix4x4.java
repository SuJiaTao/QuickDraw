// Bailey JT Brown
// 2023
// QMatrix4x4.java

package QDraw;

import QDraw.QException.PointOfError;

public final class QMatrix4x4 {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final int ROW_COUNT       = QVector4.COMPONENT_COUNT;
    private static final int COLUMN_COUNT    = QVector4.COMPONENT_COUNT;
    private static final int COMPONENT_COUNT = ROW_COUNT * COLUMN_COUNT;
    private static final float TO_RADIANS    = (float)Math.PI / 180.0f;

    private static final float[] COMPONENTS_IDENTITY = {
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
    };

    public static final QMatrix4x4 identity = new QMatrix4x4(COMPONENTS_IDENTITY);

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private float[] components = new float[COMPONENT_COUNT];

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private static float sinf(float degrees) {
        // TODO: optimize!!
        return (float)Math.sin(degrees * TO_RADIANS);
    }

    private static float cosf(float degrees) {
        // TODO: optimize!!
        return (float)Math.cos(degrees * TO_RADIANS);
    }

    // quick reference: col -> x, row -> y
    private static int getComponentIndex(int col, int row) {
        return col + (ROW_COUNT * row);
    }

    private float getValue(int col, int row) {
        return components[getComponentIndex(col, row)];
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void set(float[] vec) {
        if (!(vec.length == COMPONENT_COUNT)) {
            throw new QException(
                PointOfError.InvalidData, 
                "can only construct matrix4x4 with size 16 vector." +
                " given was size " + vec.length
            );
        }
        System.arraycopy(
            vec, 
            0, 
            components, 
            0, 
            COMPONENT_COUNT
        );
    }

    public void set(QMatrix4x4 m2) {
        System.arraycopy(
            m2.getComponents(),
            0, 
            components,
            0,
            COMPONENT_COUNT
        );
    }

    public float[] getComponents( ) {
        return components;
    }

    public static QVector4 multiply4(QMatrix4x4 mat4x4, QVector4 vec) {
        float[] returnComponents = new float[QVector4.COMPONENT_COUNT];
        for (int vCompIndex = 0; vCompIndex < QVector4.COMPONENT_COUNT; vCompIndex++) {
            for (int columnIndex = 0; columnIndex < COLUMN_COUNT; columnIndex++) {
                returnComponents[vCompIndex] +=
                    vec.getComponents()[columnIndex] * 
                    mat4x4.getValue(columnIndex, vCompIndex);
            }
        }
        return new QVector4(returnComponents);
    }

    public QVector4 multiply4(QVector4 vec) {
        return multiply4(this, vec);
    }

    public static QVector4 multiply3(QMatrix4x4 mat4x4, QVector4 vec3) {
        return multiply4(mat4x4, vec3).setW(1.0f);
    }

    public QVector4 multiply3(QVector4 vec3) {
        return multiply3(this, vec3);
    }

    public static QMatrix4x4 multiply4x4(QMatrix4x4 m1, QMatrix4x4 m2) {
        // stolen from https://github.com/SuJiaTao/Caesium/blob/master/csm_matrix.c
        float[] returnComponents = new float[COMPONENT_COUNT];
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                for (int k = 0; k < ROW_COUNT; k++) {
                    returnComponents[getComponentIndex(i, j)] +=
                        m1.getValue(i, k) * m2.getValue(k, j);
                }
            }
        }
        return new QMatrix4x4(returnComponents);
    }

    public QMatrix4x4 multiply4x4(QMatrix4x4 m2) {
        // TODO: possibly optimize?
        System.arraycopy(
            multiply4x4(this, m2), 
            0, 
            components, 
            0, 
            COMPONENT_COUNT
        );
        return this;
    }

    public static QMatrix4x4 scaleMatrix(float s) {
        return scaleMatrix(s, s, s);
    }

    public static QMatrix4x4 scaleMatrix(QVector4 vec3) {
        return scaleMatrix(vec3.getX(), vec3.getY(), vec3.getZ());
    }

    public static QMatrix4x4 scaleMatrix(float x, float y, float z) {
        float[] returnComponents = {
               x, 0.0f, 0.0f, 0.0f,
            0.0f,    y, 0.0f, 0.0f,
            0.0f, 0.0f,    z, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
        };
        return new QMatrix4x4(returnComponents);
    }

    public static QMatrix4x4 translationMatrix(QVector4 vec3) {
        return translationMatrix(vec3.getX(), vec3.getY(), vec3.getZ());
    }

    public static QMatrix4x4 translationMatrix(float x, float y, float z) {
        float[] returnComponents = {
            1.0f, 0.0f, 0.0f,    x,
            0.0f, 1.0f, 0.0f,    y,
            0.0f, 0.0f, 1.0f,    z,
            0.0f, 0.0f, 0.0f, 1.0f
        };
        return new QMatrix4x4(returnComponents);
    }

    public static QMatrix4x4 rotationMatrix(QVector4 vec3) {
        return rotationMatrix(vec3.getX(), vec3.getY(), vec3.getZ());
    }

    public static QMatrix4x4 rotationMatrix(float x, float y, float z) {
        // TODO: optimize
        // references:
        // https://github.com/SuJiaTao/Caesium/blob/master/csm_matrix.c
        // https://math.stackexchange.com/questions/1882276/combining-all-three-rotation-matrices
        float cosX = cosf(x);
        float cosY = cosf(y);
        float cosZ = cosf(z);
        float sinX = sinf(x);
        float sinY = sinf(y);
        float sinZ = sinf(z);
        float sinXsinY = sinX * sinY;
        float cosXsinY = cosX * sinY;

        float[] returnComponents = {
            cosY * cosZ,
            cosY * sinZ, 
            -sinY, 
            0.0f,

            sinXsinY * cosZ - cosX * sinZ, 
            sinXsinY * sinZ + cosX * cosZ, 
            sinX * cosY, 
            0.0f,

            cosXsinY * cosZ + sinX * sinZ,
            cosXsinY * sinZ - sinX * cosZ,
            cosX * cosY,
            0.0f,

            0.0f,
            0.0f,
            0.0f,
            1.0f
        };

        return new QMatrix4x4(returnComponents);
    }

    public QMatrix4x4 scale(float s) {
        multiply4x4(QMatrix4x4.scaleMatrix(s));
        return this;
    }

    public QMatrix4x4 scale(float x, float y, float z) {
        multiply4x4(QMatrix4x4.scaleMatrix(x, y, z));
        return this;
    }

    public QMatrix4x4 scale(QVector4 vec3) {
        multiply4x4(QMatrix4x4.scaleMatrix(vec3));
        return this;
    }

    public QMatrix4x4 translate(float x, float y, float z) {
        multiply4x4(QMatrix4x4.translationMatrix(x, y, z));
        return this;
    }

    public QMatrix4x4 translate(QVector4 vec3) {
        multiply4x4(QMatrix4x4.translationMatrix(vec3));
        return this;
    }

    public QMatrix4x4 rotate(float x, float y, float z) {
        multiply4x4(QMatrix4x4.rotationMatrix(x, y, z));
        return this;
    }

    public QMatrix4x4 rotate(QVector4 vec3) {
        multiply4x4(QMatrix4x4.rotationMatrix(vec3));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QMatrix4x4)) {
            return false;
        }
        
        QMatrix4x4 m2 = (QMatrix4x4)o;
        for (int i = 0; i < COMPONENT_COUNT; i++) {
            if (!(Math.abs(components[i] - m2.components[i]) < QVector4.COMPARE_EPSILON))
                return false;
        }
        return true;
    }

    @Override
    public String toString( ) {
        return String.format(
            "|\t%f\t %f\t %f\t %f\t|\n" +
            "|\t%f\t %f\t %f\t %f\t|\n" +
            "|\t%f\t %f\t %f\t %f\t|\n" +
            "|\t%f\t %f\t %f\t %f\t|\n",
            components[0],  components[1],  components[2],  components[3],
            components[4],  components[5],  components[6],  components[7],
            components[8],  components[9],  components[10], components[11],
            components[12], components[13], components[14], components[15]
        );
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public QMatrix4x4( ) {
        System.arraycopy(
            COMPONENTS_IDENTITY, 
            0, 
            components, 
            0, 
            COMPONENT_COUNT
        );
    }

    public QMatrix4x4(QMatrix4x4 mat) {
        set(mat);
    }

    public QMatrix4x4(float[] vec) {
        set(vec);
    }
}