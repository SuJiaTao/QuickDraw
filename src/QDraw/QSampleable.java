// Bailey JT Brown
// 2024
// QSampleable.java

package QDraw;

import java.awt.image.*;
import QDraw.QException.PointOfError;

public abstract class QSampleable {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    protected static final int COLOR_PACKING   = BufferedImage.TYPE_INT_ARGB;
    protected static final int NO_SAMPLE_COLOR = new QColor(0x00, 0x00, 0x00, 0x00).toInt(); 

    /////////////////////////////////////////////////////////////////
    // PUBLIC ENUMS
    public enum SampleType {
        Cutoff,
        Clamp,
        Repeat
    };

    /////////////////////////////////////////////////////////////////
    // PUBLIC INTERFACES
    public static interface ColorMapFunction {
        public int mapFunc(int colorIn);
    }

    public static interface ColorMapSpacialFunction {
        public int mapFunc(int colorIn, int x, int y);
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void mapColor(ColorMapFunction cpf) {
        for (int mapX = 0; mapX < getWidth(); mapX++) {
            for (int mapY = 0; mapY < getHeight(); mapY++) {
                setColor(mapX, mapY, cpf.mapFunc(getColor(mapX, mapY)));
            }
        }
    }

    public void mapColorSpacial(ColorMapSpacialFunction cpsf) {
        for (int mapX = 0; mapX < getWidth(); mapX++) {
            for (int mapY = 0; mapY < getHeight(); mapY++) {
                setColor(mapX, mapY, cpsf.mapFunc(getColor(mapX, mapY), mapX, mapY));
            }
        }
    }

    public void copyTo(QSampleable target) {
        for (int copyX = 0; copyX < getWidth(); copyX++) {
            for (int copyY = 0; copyY < getHeight(); copyY++) {
                target.setColor(copyX, copyY, getColor(copyX, copyY));
            }
        }
    }

    public int sample(
        float u,
        float v,
        SampleType sampleType
    ) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csm_fragment.h
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
                    "Invalid sample type: " + sampleType.toString()
                );
        }

        int texCoordX = (int)((float)getWidth() * u);
        int texCoordY = (int)((float)getHeight() * v);
        texCoordX = Math.max(0, Math.min(texCoordX, getWidth() - 1));
        texCoordY = Math.max(0, Math.min(texCoordY, getHeight() - 1));

        return getColor(texCoordX, texCoordY);
    }

    /////////////////////////////////////////////////////////////////
    // ABSTRACT METHODS
    public abstract int    getWidth( );
    public abstract int    getHeight( );
    public abstract int    getColor(int x, int y);
    public abstract void   setColor(int x, int y, int color);
}
