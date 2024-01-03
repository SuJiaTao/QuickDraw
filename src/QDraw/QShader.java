// Bailey JT Brown
// 2024
// QShader.java

package QDraw;

import QDraw.QException.PointOfError;

public abstract class QShader {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final QColor NO_TEXTURE_COLOR = new QColor(0xFF, 0x00, 0xFF);
    public static final QColor NO_SAMPLE_COLOR  = new QColor(0x00, 0x00, 0x00, 0x00); 

    /////////////////////////////////////////////////////////////////
    // BUILT IN SHADER METHODS
    public static QColor blendColor(QColor bottom, QColor top) {
        int tFac = top.getA();
        int bFac = 0xFF - tFac;
        return new QColor(
            ((top.getR() * tFac) + (bottom.getR() * bFac)) >> 9,
            ((top.getG() * tFac) + (bottom.getG() * bFac)) >> 9,
            ((top.getB() * tFac) + (bottom.getB() * bFac)) >> 9
        );
    }

    public static QColor sampleTexture(
        float u,
        float v,
        QRenderBuffer texture,
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
        int        vertexNum,
        QVector3   inVertex,
        QMatrix4x4 transform,
        Object     userIn
    );

    public abstract QColor fragmentShader(
        int    screenX,
        int    screenY,
        float  fragU,
        float  fragV,
        QRenderBuffer texture,
        QColor belowColor,
        Object userIn
    );
}
