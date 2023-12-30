// Bailey JT Brown
// 2023
// QMatrix4x4.java

package QDraw;

import QDraw.QException.PointOfError;

public final class QMatrix4x4 extends QEncoding {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final float[] COMPONENTS_IDENTITY = {
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
    };

    public static final QMatrix4x4 identity = new QMatrix4x4(COMPONENTS_IDENTITY);

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private float[] components = new float[MTR_NUM_CMPS];

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private static float sinf(float degrees) {
        // TODO: optimize!!
        return (float)Math.sin(degrees * MTR_TO_RADIANS);
    }

    private static float cosf(float degrees) {
        // TODO: optimize!!
        return (float)Math.cos(degrees * MTR_TO_RADIANS);
    }

    // quick reference: col -> x, row -> y
    private static int getComponentIndex(int col, int row) {
        return col + (MTR_NUM_ROWS * row);
    }

    private float getValue(int col, int row) {
        return components[getComponentIndex(col, row)];
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void set(float[] vec) {
        if (!(vec.length == MTR_NUM_CMPS)) {
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
            MTR_NUM_CMPS
        );
    }

    public void set(QMatrix4x4 m2) {
        System.arraycopy(
            m2.getComponents(),
            0, 
            components,
            0,
            MTR_NUM_CMPS
        );
    }

    public float[] getComponents( ) {
        return components;
    }

    private static float[] __m4dna_tempComponents  = { 0.0f, 0.0f, 0.0f, 0.0f };
    public static void multiply4DestructiveNoAlloc(
        QMatrix4x4 mat4x4, 
        float[] vec
    ) {
        __m4dna_tempComponents[0] = 0.0f;
        __m4dna_tempComponents[1] = 0.0f;
        __m4dna_tempComponents[2] = 0.0f;
        __m4dna_tempComponents[3] = 0.0f;

        for (int vCompIndex = 0; vCompIndex < VCTR_NUM_CMPS; vCompIndex++) {
            for (int columnIndex = 0; columnIndex < MTR_NUM_COLUMNS; columnIndex++) {
                __m4dna_tempComponents[vCompIndex] +=
                    vec[columnIndex] * 
                    mat4x4.getValue(columnIndex, vCompIndex);
            }
        }

        System.arraycopy(
            __m4dna_tempComponents, 
            0, 
            vec, 
            0, 
            VCTR_NUM_CMPS
        );
    }

    public static float[] multiply4(QMatrix4x4 mat4x4, float[] vec) {
        float[] returnComponents = new float[VCTR_NUM_CMPS];
        System.arraycopy(
            vec, 
            0, 
            returnComponents, 
            0,
            VCTR_NUM_CMPS
        );
        multiply4DestructiveNoAlloc(mat4x4, returnComponents);
        return returnComponents;
    }

    public static QVector4 multiply4(QMatrix4x4 mat4x4, QVector4 vec) {
        return new QVector4(multiply4(mat4x4, vec.getComponents()));
    }

    public QVector4 multiply4(QVector4 vec) {
        return multiply4(this, vec);
    }

    private static float[] __m3dna_tempComponents = { 0.0f, 0.0f, 0.0f, 1.0f };
    public static void multiply3DestructiveNoAlloc(QMatrix4x4 mat4x4, float[] vec3) {
        __m3dna_tempComponents[0] = vec3[0];
        __m3dna_tempComponents[1] = vec3[1];
        __m3dna_tempComponents[2] = vec3[2];
        __m3dna_tempComponents[3] = 1.0f;
        multiply4DestructiveNoAlloc(mat4x4, __m3dna_tempComponents);
        System.arraycopy(
            __m3dna_tempComponents, 
            0, 
            vec3, 
            0,
            3
        );
    }

    public static float[] multiply3(QMatrix4x4 mat4x4, float[] vec3) {
        float[] returnComponents = {vec3[0], vec3[1], vec3[2], 1.0f};
        multiply3DestructiveNoAlloc(mat4x4, returnComponents);
        return returnComponents;
    }

    public static QVector4 multiply3(QMatrix4x4 mat4x4, QVector4 vec3) {
        return multiply4(mat4x4, vec3).setW(1.0f);
    }

    public QVector4 multiply3(QVector4 vec3) {
        return multiply3(this, vec3);
    }

    public static float[] multiply4x4(float[] m1, float[] m2) {
        // stolen from https://github.com/SuJiaTao/Caesium/blob/master/csm_matrix.c
        float[] returnComponents = new float[MTR_NUM_CMPS];
        for (int i = 0; i < MTR_NUM_ROWS; i++) {
            for (int j = 0; j < MTR_NUM_COLUMNS; j++) {
                for (int k = 0; k < MTR_NUM_ROWS; k++) {
                    returnComponents[getComponentIndex(i, j)] +=
                        m1[getComponentIndex(i, k)] * 
                        m2[getComponentIndex(k, j)];
                }
            }
        }
        return returnComponents;
    }

    public static QMatrix4x4 multiply4x4(QMatrix4x4 m1, QMatrix4x4 m2) {
        return new QMatrix4x4(multiply4x4(m1.getComponents(), m2.getComponents()));
    }

    public QMatrix4x4 multiply4x4(QMatrix4x4 m2) {
        // TODO: possibly optimize?
        System.arraycopy(
            multiply4x4(this, m2), 
            0, 
            components, 
            0, 
            MTR_NUM_CMPS
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
        for (int i = 0; i < MTR_NUM_CMPS; i++) {
            if (!(Math.abs(components[i] - m2.components[i]) < VCTR_COMPARE_EPSILON))
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
            MTR_NUM_CMPS
        );
    }

    public QMatrix4x4(QMatrix4x4 mat) {
        set(mat);
    }

    public QMatrix4x4(float[] vec) {
        set(vec);
    }
}
