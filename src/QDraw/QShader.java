// Bailey JT Brown
// 2024
// QShader.java

package QDraw;

import java.util.concurrent.atomic.AtomicLong;

import QDraw.QException.PointOfError;

public abstract class QShader {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final int RANDOM_GRANULATIRY        = 0xFFFF;
    public static final float NORMALIZATION_FACTOR    = 1.0f / (float)RANDOM_GRANULATIRY;
    public static final int VERTEX_SHADER_MAX_OUTPUTS = 8;

    /////////////////////////////////////////////////////////////////
    // PUBLIC CLASSES
    public static final class VertexShaderContext {
        /////////////////////////////////////////////////////////////////
        // PUBLIC MEMBERS
        public Object[]   uniforms;
        public QSampleable[] textures;
        public float[][]  attributes;
        public float[][]  outputsToFragShader = new float[VERTEX_SHADER_MAX_OUTPUTS][];
    }

    public static final class FragmentShaderContext {
        /////////////////////////////////////////////////////////////////
        // PUBLIC MEMBERS
        public Object[]   uniforms;
        public QSampleable[] textures;
        public QRenderBuffer target;
        public int        screenX;
        public int        screenY;
        public float      invDepth;
        public QVector3   normal;
        public float[][]  inputsFromVertexShader = new float[VERTEX_SHADER_MAX_OUTPUTS][];
    }

    public static final class ShaderRequirement {
        /////////////////////////////////////////////////////////////////
        // PUBLIC ENUMS
        public static enum RequirementType {
            Attribute,
            Uniform,
            Texture
        }

        /////////////////////////////////////////////////////////////////
        // PUBLIC MEMBERS
        public int             slot;
        public RequirementType requireType;
        public String          purpose;
        public Class<?>        uniformClass;

        /////////////////////////////////////////////////////////////////
        // CONSTRUCTOR
        public ShaderRequirement(int _slot, RequirementType _type, String _purpose) {
            slot        = _slot;
            requireType = _type;
            purpose     = _purpose;
        }

        public ShaderRequirement(
            int      _slot, 
            RequirementType _type, 
            String   _purpose, 
            Class<?> _uniformClass
        ) {
            if (_type != RequirementType.Uniform) {
                throw new QException(
                    PointOfError.InvalidParameter, 
                    "Can ONLY specify uniform class if requirement type is Uniform"
                );
            }
            slot         = _slot;
            requireType  = _type;
            purpose      = _purpose;
            uniformClass = _uniformClass;
        }
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

    public static void forwardAttributeToFragShader(
        VertexShaderContext vertCtx,
        int attribSlot
    ) {
        setOutputToFragShader(vertCtx, attribSlot, vertCtx.attributes[attribSlot]);
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
        return seededRandom((int)(_seedUniquifier.incrementAndGet( ) + System.nanoTime( )));
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

    public static float seededRandom(Object obj) {
        // generate seed based on object hash
        return seededRandom(obj.hashCode( ));
    }

    public static float seededRandom(FragmentShaderContext fctx) {
        // generate seed based on screen coordinate
        return seededRandom(fctx.screenX + (fctx.screenY * fctx.target.getWidth( )));
    }

    public static QVector3 randomVector( ) {
        return new QVector3(random( ), random( ), random( ));
    }

    public static QVector3 seededRandomVector(float flt) {
        return seededRandomVector(Float.floatToRawIntBits(flt));
    }

    public static QVector3 seededRandomVector(int seed) {
        return new QVector3(
            seededRandom((seed << 2) + 1),
            seededRandom((seed << 2) + 2),
            seededRandom((seed << 2) + 3)
        );
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

    public static QColor addColor(QColor color1, QColor color2) {
        return new QColor(
            (color1.getR() + color1.getR()),
            (color1.getG() + color2.getG()),
            (color1.getB() + color2.getB())
        );
    }

    public static QColor multiplyColor(QColor color1, QColor color2) {
        return new QColor(
            (color1.getR() * color1.getR()) >>> 8,
            (color1.getG() * color2.getG()) >>> 8,
            (color1.getB() * color2.getB()) >>> 8
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
    public abstract ShaderRequirement[] requirements( );

    public abstract QVector3 vertexShader(
        VertexShaderContext context
    );

    public abstract QColor fragmentShader(
        FragmentShaderContext context
    );
}
