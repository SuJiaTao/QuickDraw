// Bailey JT Brown
// 2024
// QShader.java

package QDraw;

public abstract class QShader {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final int RANDOM_GRANULATIRY     = 0xFFFF;
    public static final float NORMALIZATION_FACTOR = 1.0f / (float)RANDOM_GRANULATIRY;

    /////////////////////////////////////////////////////////////////
    // PUBLIC CLASSES
    public static final class VertexDrawInfo {
        public int        vertexNum;
        public QMesh      mesh;
        public QVector3   vertexPos;
        public QMatrix4x4 transform;
    }

    public static final class FragmentDrawInfo {
        public int         screenX, screenY;
        public float       fragU, fragV;
        public QSampleable texture;
        public QColor      belowColor;
        public QVector3    faceNormal;
        public QVector3    faceCenterWorldSpace;
    }

    /////////////////////////////////////////////////////////////////
    // BUILT IN SHADER METHODS
    private static int _seedUniquifier = 0;
    public static float random( ) {
        return seededRandom((_seedUniquifier++) + (int)System.nanoTime());
    }

    public static float seededRandom(float seed) {
        return seededRandom(Float.floatToIntBits(seed));
    }

    public static float seededRandom(int iSeed) {
        // simple XORSHIFT
        iSeed *= iSeed;
        iSeed ^= (iSeed <<  13);
        iSeed ^= (iSeed >>> 17);
        iSeed ^= (iSeed <<  5);

        // bound and normalize
        iSeed = (iSeed & RANDOM_GRANULATIRY);
        return (float)iSeed * NORMALIZATION_FACTOR;
    }

    public static QColor blendColor(QColor bottom, QColor top) {
        int tFac = top.getA();
        if (tFac == 0xFF) return top;

        int bFac = 0xFF - tFac;
        if (bFac == 0xFF) return bottom;

        // NOTE: unsigned lshift 8 is equivalent to division by 255
        return new QColor(
            (top.getR() * tFac + bottom.getR() * bFac) >>> 8,
            (top.getG() * tFac + bottom.getG() * bFac) >>> 8,
            (top.getB() * tFac + bottom.getB() * bFac) >>> 8
        );
    }

    public static QColor multiplyColor(QColor color, float factor) {
        int iFac = (int)(factor * 255.0f);
        return new QColor(
            (color.getR() * iFac) >>> 8,
            (color.getG() * iFac) >>> 8,
            (color.getB() * iFac) >>> 8
        );
    }

    public static QColor multiplyColor(QColor color, float facR, float facG, float facB) {
        int iFacR = (int)(facR * 255.0f);
        int iFacG = (int)(facG * 255.0f);
        int iFacB = (int)(facB * 255.0f);
        return new QColor(
            (color.getR() * iFacR) >>> 8,
            (color.getG() * iFacG) >>> 8,
            (color.getB() * iFacB) >>> 8
        );
    }

    /////////////////////////////////////////////////////////////////
    // ABSTRACT METHODS
    public abstract QVector3 vertexShader(
        VertexDrawInfo infoIn, Object userIn
    );

    public abstract QColor fragmentShader(
        FragmentDrawInfo infoIn, Object userIn
    );
}
