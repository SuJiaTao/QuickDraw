// Bailey JT Brown
// 2023-2024
// QMatrix4x4.java

package ca.bael.QDraw;

public final class QMatrix4x4 extends QEncoding {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final float DEG_TO_RAD = (float)Math.PI / 180.0f;

    private static final float[] COMPONENTS_IDENTITY = {
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
    };

    private static final float[] COMPONENTS_ZERO = {
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 0.0f
    };

    public static QMatrix4x4 Identity( ) {
        return new QMatrix4x4(COMPONENTS_IDENTITY);
    }

    public static final QMatrix4x4 Zero( ) {
        return new QMatrix4x4(COMPONENTS_ZERO);
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    // (refer to QEncoding.java for encoding details)
    private float[] components = new float[MTR_NUM_CMPS];

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private static float[] multiplyComponents(float[] m1, float[] m2) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csm_matrix.c
        float[] r = new float[MTR_NUM_CMPS];
        for (int row = 0; row < MTR_NUM_ROWS; row++) {
            int rowStart = MTR_NUM_COLUMNS * row;
            float b0 = m2[rowStart];
            float b1 = m2[rowStart + 1];
            float b2 = m2[rowStart + 2];
            float b3 = m2[rowStart + 3];
            for (int col = 0; col < MTR_NUM_COLUMNS; col++) {
                r[rowStart + col] =
                    b0 * m1[col] + b1 * m1[col + 4] + b2 * m1[col + 8] + b3 * m1[col + 12];
            }
        }
        return r;
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public static float cosDeg(float degrees) {
        return (float)Math.cos(degrees * DEG_TO_RAD);
    }

    public static float sinDeg(float degrees) {
        return (float)Math.sin(degrees * DEG_TO_RAD);
    }

    public float getValue(int col, int row) {
        return components[col + MTR_NUM_ROWS * row];
    }

    public void setValue(int col, int row, float val) {
        components[col + MTR_NUM_ROWS * row] = val;
    }

    public void set(float[] vec) {
        System.arraycopy(vec, 0, components, 0, MTR_NUM_CMPS);
    }

    public void set(QMatrix4x4 m2) {
        set(m2.components);
    }

    public float[] getComponents( ) {
        return components;
    }

    public void transformDir(float[] v) {
        float x = v[VCTR_INDEX_X];
        float y = v[VCTR_INDEX_Y];
        float z = v[VCTR_INDEX_Z];
        v[VCTR_INDEX_X] = components[0] * x + components[1] * y + components[2]  * z;
        v[VCTR_INDEX_Y] = components[4] * x + components[5] * y + components[6]  * z;
        v[VCTR_INDEX_Z] = components[8] * x + components[9] * y + components[10] * z;
    }

    public void transformPoint(float[] v) {
        transformDir(v);
        v[VCTR_INDEX_X] += components[3];
        v[VCTR_INDEX_Y] += components[7];
        v[VCTR_INDEX_Z] += components[11];
    }

    public QVector3 transformDir(QVector3 v) {
        transformDir(v.getComponents());
        return v;
    }

    public QVector3 transformPoint(QVector3 v) {
        transformPoint(v.getComponents());
        return v;
    }

    public static QVector3 multiply(QMatrix4x4 m, QVector3 v) {
        return m.transformPoint(v.copy());
    }

    public static QMatrix4x4 multiply(QMatrix4x4 m1, QMatrix4x4 m2) {
        return new QMatrix4x4(multiplyComponents(m1.components, m2.components));
    }

    public void multiply(QMatrix4x4 m2) {
        set(multiplyComponents(this.components, m2.components));
    }

    public static QMatrix4x4 scaleMatrix(float s) {
        return scaleMatrix(s, s, s);
    }

    public static QMatrix4x4 scaleMatrix(QVector3 vec3) {
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

    public static QMatrix4x4 translationMatrix(QVector3 vec3) {
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

    public static QMatrix4x4 rotationMatrix(QVector3 vec3) {
        return rotationMatrix(vec3.getX(), vec3.getY(), vec3.getZ());
    }

    public static QMatrix4x4 rotationMatrix(float x, float y, float z) {
        // references:
        // https://github.com/SuJiaTao/Caesium/blob/master/csm_matrix.c
        // https://math.stackexchange.com/questions/1882276/combining-all-three-rotation-matrices
        float cosX = cosDeg(x);
        float cosY = cosDeg(y);
        float cosZ = cosDeg(z);
        float sinX = sinDeg(x);
        float sinY = sinDeg(y);
        float sinZ = sinDeg(z);
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
        multiply(QMatrix4x4.scaleMatrix(s));
        return this;
    }

    public QMatrix4x4 scale(float x, float y, float z) {
        multiply(QMatrix4x4.scaleMatrix(x, y, z));
        return this;
    }

    public QMatrix4x4 scale(QVector3 vec3) {
        multiply(QMatrix4x4.scaleMatrix(vec3));
        return this;
    }

    public static QMatrix4x4 scale(QMatrix4x4 mat, float s) {
        return QMatrix4x4.multiply(mat, QMatrix4x4.scaleMatrix(s));
    }

    public static QMatrix4x4 scale(QMatrix4x4 mat, float x, float y, float z) {
        return QMatrix4x4.multiply(mat, QMatrix4x4.scaleMatrix(x, y, z));
    }

    public static QMatrix4x4 scale(QMatrix4x4 mat, QVector3 vec3) {
        return QMatrix4x4.multiply(mat, QMatrix4x4.scaleMatrix(vec3));
    }

    public QMatrix4x4 translate(float x, float y, float z) {
        multiply(QMatrix4x4.translationMatrix(x, y, z));
        return this;
    }

    public QMatrix4x4 translate(QVector3 vec3) {
        multiply(QMatrix4x4.translationMatrix(vec3));
        return this;
    }

    public static QMatrix4x4 translate(QMatrix4x4 mat, float x, float y, float z) {
        return QMatrix4x4.multiply(mat, QMatrix4x4.translationMatrix(x, y, z));
    }

    public static QMatrix4x4 translate(QMatrix4x4 mat, QVector3 vec3) {
        return QMatrix4x4.multiply(mat, QMatrix4x4.translationMatrix(vec3));
    }

    public QMatrix4x4 rotate(float x, float y, float z) {
        multiply(QMatrix4x4.rotationMatrix(x, y, z));
        return this;
    }

    public QMatrix4x4 rotate(QVector3 vec3) {
        multiply(QMatrix4x4.rotationMatrix(vec3));
        return this;
    }

    public static QMatrix4x4 rotate(QMatrix4x4 mat, float x, float y, float z) {
        return QMatrix4x4.multiply(mat, QMatrix4x4.rotationMatrix(x, y, z));
    }

    public static QMatrix4x4 rotate(QMatrix4x4 mat, QVector3 vec3) {
        return QMatrix4x4.multiply(mat, QMatrix4x4.rotationMatrix(vec3));
    }

    public static QMatrix4x4 TRS(
        QVector3 translation,
        QVector3 rotation,
        QVector3 scale
    ) {
        return translate(rotate(scale(Identity(), scale), rotation), translation);
    }

    // NOTE:
    // for the extract family of methods, refer to
    // https://math.stackexchange.com/questions/237369/given-this-transformation-matrix-how-do-i-decompose-it-into-translation-rotati
    
    public QVector3 extractTranslation( ) {
        return new QVector3(getValue(3, 0), getValue(3, 1), getValue(3, 2));
    }

    public QVector3 extractScale( ) {
        float sx = new QVector3(
            getValue(0, 0), getValue(0, 1), getValue(0, 2)
        ).magnitude( );
        float sy = new QVector3(
            getValue(1, 0), getValue(1, 1), getValue(1, 2)
        ).magnitude( );
        float sz = new QVector3(
            getValue(2, 0), getValue(2, 1), getValue(2, 2)
        ).magnitude( );
        return new QVector3(sx, sy, sz);
    }

    public QVector3 extractScaleFast( ) {
        float sx = new QVector3(
            getValue(0, 0), getValue(0, 1), getValue(0, 2)
        ).fastMagnitude( );
        float sy = new QVector3(
            getValue(1, 0), getValue(1, 1), getValue(1, 2)
        ).fastMagnitude( );
        float sz = new QVector3(
            getValue(2, 0), getValue(2, 1), getValue(2, 2)
        ).fastMagnitude( );
        return new QVector3(sx, sy, sz);
    }

    public QMatrix4x4 normalMatrix( ) {
        float[] r0 = { components[0], components[1], components[2]  };
        float[] r1 = { components[4], components[5], components[6]  };
        float[] r2 = { components[8], components[9], components[10] };
        float[] c0 = QVector3.cross(r1, r2);
        float[] c1 = QVector3.cross(r2, r0);
        float[] c2 = QVector3.cross(r0, r1);
        float invDet = 1.0f / QVector3.dot(r0, c0);
        return new QMatrix4x4(new float[] {
            c0[0] * invDet, c0[1] * invDet, c0[2] * invDet, 0.0f,
            c1[0] * invDet, c1[1] * invDet, c1[2] * invDet, 0.0f,
            c2[0] * invDet, c2[1] * invDet, c2[2] * invDet, 0.0f,
            0.0f,           0.0f,           0.0f,           1.0f
        });
    }

    public QMatrix4x4 extractRotation( ) {
        // clear translation
        QMatrix4x4 rotMat = new QMatrix4x4(this);
        QVector3 scale    = extractScale( );

        rotMat.setValue(3, 0, 0.0f);
        rotMat.setValue(3, 1, 0.0f);
        rotMat.setValue(3, 2, 0.0f);

        for (int y = 0; y < VCTR_NUM_CMPS; y++) {
            rotMat.setValue(0, y, rotMat.getValue(0, y) / scale.getX( ));
            rotMat.setValue(1, y, rotMat.getValue(1, y) / scale.getY( ));
            rotMat.setValue(2, y, rotMat.getValue(2, y) / scale.getZ( ));
        }

        return rotMat;
    }

    public QMatrix4x4 extractRotationFast( ) {
        // clear translation
        QMatrix4x4 rotMat = new QMatrix4x4(this);
        QVector3 scale    = extractScaleFast( );

        rotMat.setValue(3, 0, 0.0f);
        rotMat.setValue(3, 1, 0.0f);
        rotMat.setValue(3, 2, 0.0f);

        for (int y = 0; y < VCTR_NUM_CMPS; y++) {
            rotMat.setValue(0, y, rotMat.getValue(0, y) / scale.getX( ));
            rotMat.setValue(1, y, rotMat.getValue(1, y) / scale.getY( ));
            rotMat.setValue(2, y, rotMat.getValue(2, y) / scale.getZ( ));
        }

        return rotMat;
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
