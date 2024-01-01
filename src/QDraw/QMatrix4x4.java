// Bailey JT Brown
// 2023-2024
// QMatrix4x4.java

package QDraw;

public final class QMatrix4x4 extends QEncoding {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
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
    // PUBLIC METHODS
    public float getValue(int col, int row) {
        return QMath.get4x4(components, col, row);
    }

    public void setValue(int col, int row, float val) {
        QMath.set4x4(0, components, col, row, val);
    }

    public void set(float[] vec) {
        QMath.copy4x4(components, vec);
    }

    public void set(QMatrix4x4 m2) {
        QMath.copy4x4(0, components, 0, m2.components);
    }

    public float[] getComponents( ) {
        return components;
    }

    public static QVector3 multiply(QMatrix4x4 m, QVector3 v) {
        QVector3 temp = v.copy();
        QMath.mul3_4x4(temp.getComponents(), m.components);
        return temp;
    }

    public static QMatrix4x4 multiply(QMatrix4x4 m1, QMatrix4x4 m2) {
        return new QMatrix4x4(QMath.mul4x4(m1.components, m2.components));
    }

    public void multiply(QMatrix4x4 m2) {
        set(QMath.mul4x4(this.components, m2.components));
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
        // TODO: optimize
        // references:
        // https://github.com/SuJiaTao/Caesium/blob/master/csm_matrix.c
        // https://math.stackexchange.com/questions/1882276/combining-all-three-rotation-matrices
        float cosX = QMath.cosf(x);
        float cosY = QMath.cosf(y);
        float cosZ = QMath.cosf(z);
        float sinX = QMath.sinf(x);
        float sinY = QMath.sinf(y);
        float sinZ = QMath.sinf(z);
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
