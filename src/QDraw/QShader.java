// Bailey JT Brown
// 2024
// QShader.java

package QDraw;

import java.util.concurrent.atomic.AtomicLong;

public abstract class QShader {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final int RANDOM_GRANULATIRY        = 0xFFFF;
    public static final float NORMALIZATION_FACTOR    = 1.0f / (float)RANDOM_GRANULATIRY;
    public static final int VERTEX_SHADER_MAX_OUTPUTS = 4;

    /////////////////////////////////////////////////////////////////
    // SHADER CONTEXT CLASSES
    public static final class VertexShaderContext {
        public Object[]   uniforms;
        public QSampleable[] textures;
        public float[][]  attributes;
        public float[][]  outputsToFragShader = new float[VERTEX_SHADER_MAX_OUTPUTS][];
    }

    public static final class FragmentShaderContext {
        public Object[]   uniforms;
        public QSampleable[] textures;
        public int        screenX;
        public int        screenY;
        public float      invDepth;
        public float[][]  inputsFromVertexShader = new float[VERTEX_SHADER_MAX_OUTPUTS][];
    }

    /////////////////////////////////////////////////////////////////
    // BUILT IN SHADER FUNCTIONS
    public static void setOutputToFragShader(
        VertexShaderContext vertCtx,
        int outputSlot, 
        float[] inBuffer
    ) {
        vertCtx.outputsToFragShader[outputSlot] = new float[inBuffer.length];
        System.arraycopy(
            inBuffer, 
            0, 
            vertCtx.outputsToFragShader[outputSlot], 
            0, 
            inBuffer.length
        );
    }

    public static void getOutputFromVertShader(
        FragmentShaderContext fragCtx,
        int inputSlot, 
        float[] outBuffer
    ) {
        System.arraycopy(
            fragCtx.inputsFromVertexShader[inputSlot], 
            0, 
            outBuffer, 
            0, 
            fragCtx.inputsFromVertexShader[inputSlot].length
        );
    }

    private static AtomicLong _seedUniquifier = new AtomicLong(0x5EED);
    public static float random( ) {
        return seededRandom((int)(_seedUniquifier.incrementAndGet() + System.nanoTime()));
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
        VertexShaderContext context
    );

    public abstract QColor fragmentShader(
        FragmentShaderContext context
    );
}
