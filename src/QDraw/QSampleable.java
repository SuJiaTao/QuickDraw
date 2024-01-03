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
    // ABSTRACT METHODS
    public abstract int    getWidth( );
    public abstract int    getHeight( );
    public abstract int    getColor(int x, int y);
    public abstract void   setColor(int x, int y, int color);
}
