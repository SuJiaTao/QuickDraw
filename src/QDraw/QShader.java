// Bailey JT Brown
// 2024
// QShader.java

package QDraw;

import QDraw.QException.PointOfError;

public abstract class QShader {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final QColor NO_TEXTURE_COLOR    = new QColor(0xFF, 0x00, 0xFF);
    public static final QColor NO_SAMPLE_COLOR     = new QColor(0x00, 0x00, 0x00, 0x00); 
    public static final int RANDOM_GRANULATIRY     = 0xFFFF;
    public static final float NORMALIZATION_FACTOR = 1.0f / (float)RANDOM_GRANULATIRY;

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

    public static QColor sampleTexture(
        float u,
        float v,
        QSampleable texture,
        QViewer.SampleType sampleType
    ) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csm_fragment.h

        if (texture == null) {
            return NO_TEXTURE_COLOR;
        }
        
        switch (sampleType) {
            case Cutoff:
                if (u < 0.0f || u >= 1.0f || v < 0.0f || v >= 1.0f) {
                    return NO_SAMPLE_COLOR;
                }
                break;

            case Clamp:
                u = Math.min(1.0f, Math.max(u, 0.0f));
                v = Math.min(1.0f, Math.max(v, 0.0f));
                break;

            case Repeat:
                u = u - (float)Math.floor((float)u);
                v = v - (float)Math.floor((float)v);
                if (u < 0.0f) {
                    u = 1.0f + u;
                }
                if (v > 1.0f) {
                    v = 1.0f + v;
                }
                break;
        
            default:
                throw new QException(
                    PointOfError.BadState, 
                    "bad sample type: " + sampleType.toString()
                );
        }

        int texCoordX = (int)((float)texture.getWidth() * u);
        int texCoordY = (int)((float)texture.getHeight() * v);
        texCoordX = Math.max(0, Math.min(texCoordX, texture.getWidth() - 1));
        texCoordY = Math.max(0, Math.min(texCoordY, texture.getHeight() - 1));

        return new QColor(texture.getColor(texCoordX, texCoordY));
    }

    /////////////////////////////////////////////////////////////////
    // ABSTRACT METHODS
    public abstract QVector3 vertexShader(
        QViewer.VertexDrawInfo infoIn, Object userIn
    );

    public abstract QColor fragmentShader(
        QViewer.FragmentDrawInfo infoIn, Object userIn
    );
}
