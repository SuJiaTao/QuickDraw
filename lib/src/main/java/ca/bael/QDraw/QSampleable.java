// Bailey JT Brown
// 2024-2026
// QSampleable.java

package ca.bael.QDraw;

import java.awt.image.*;
import ca.bael.QDraw.QException.PointOfError;

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

    public enum FilterType {
        Nearest,
        Bilinear
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
        if (target.getWidth( ) != getWidth( ) || target.getHeight( ) != getHeight( )) {
            throw new QException(
                PointOfError.InvalidParameter,
                "copyTo sizes differ: " + getWidth( ) + "x" + getHeight( ) +
                " vs " + target.getWidth( ) + "x" + target.getHeight( )
            );
        }
        for (int copyY = 0; copyY < getHeight(); copyY++) {
            for (int copyX = 0; copyX < getWidth(); copyX++) {
                target.setColor(copyX, copyY, getColor(copyX, copyY));
            }
        }
    }

    public final int sample(float u, float v) {
        return sample(u, v, SampleType.Repeat, FilterType.Nearest);
    }

    public final int sample(float u, float v, SampleType sampleType) {
        return sample(u, v, sampleType, FilterType.Nearest);
    }

    public final int sample(float u, float v, SampleType sampleType, FilterType filterType) {
        if (sampleType == SampleType.Cutoff && (u < 0.0f || u >= 1.0f || v < 0.0f || v >= 1.0f)) {
            return NO_SAMPLE_COLOR;
        }
        return filterType == FilterType.Bilinear
            ? doSampleBilinear(u, v, sampleType)
            : doSampleNearest(u, v, sampleType);
    }

    /////////////////////////////////////////////////////////////////
    // PROTECTED METHODS
    protected int doSampleNearest(float u, float v, SampleType sampleType) {
        if (sampleType == SampleType.Repeat) {
            u = u - (float)Math.floor(u);
            v = v - (float)Math.floor(v);
        }
        int texCoordX = Math.max(0, Math.min((int)(getWidth( ) * u), getWidth( ) - 1));
        int texCoordY = Math.max(0, Math.min((int)(getHeight( ) * v), getHeight( ) - 1));
        return getColor(texCoordX, texCoordY);
    }

    protected int doSampleBilinear(float u, float v, SampleType sampleType) {
        float x  = u * getWidth( ) - 0.5f;
        float y  = v * getHeight( ) - 0.5f;
        int   x0 = (int)Math.floor(x);
        int   y0 = (int)Math.floor(y);
        int   tx = (int)((x - x0) * 256.0f);
        int   ty = (int)((y - y0) * 256.0f);
        int   x1 = wrapCoord(x0 + 1, getWidth( ), sampleType);
        int   y1 = wrapCoord(y0 + 1, getHeight( ), sampleType);
        x0 = wrapCoord(x0, getWidth( ), sampleType);
        y0 = wrapCoord(y0, getHeight( ), sampleType);
        return lerpARGB(
            lerpARGB(getColor(x0, y0), getColor(x1, y0), tx),
            lerpARGB(getColor(x0, y1), getColor(x1, y1), tx),
            ty
        );
    }

    /////////////////////////////////////////////////////////////////
    // PACKAGE METHODS
    static int lerpARGB(int a, int b, int t) {
        int rb = ((a & 0x00FF00FF) * (256 - t) + (b & 0x00FF00FF) * t) >>> 8 & 0x00FF00FF;
        int ag = (((a >>> 8) & 0x00FF00FF) * (256 - t) + ((b >>> 8) & 0x00FF00FF) * t) & 0xFF00FF00;
        return rb | ag;
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private static int wrapCoord(int c, int size, SampleType sampleType) {
        return sampleType == SampleType.Repeat ? Math.floorMod(c, size) : Math.max(0, Math.min(c, size - 1));
    }

    /////////////////////////////////////////////////////////////////
    // ABSTRACT METHODS
    public abstract int    getWidth( );
    public abstract int    getHeight( );
    public abstract int    getColor(int x, int y);
    public abstract void   setColor(int x, int y, int color);
}
