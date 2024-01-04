// Bailey JT Brown
// 2024
// QSampleable.java

package QDraw;

import java.awt.image.*;

public abstract class QSampleable {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final int COLOR_PACKING = BufferedImage.TYPE_INT_ARGB;

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

    /////////////////////////////////////////////////////////////////
    // ABSTRACT METHODS
    public abstract int    getWidth( );
    public abstract int    getHeight( );
    public abstract int    getColor(int x, int y);
    public abstract void   setColor(int x, int y, int color);
}
