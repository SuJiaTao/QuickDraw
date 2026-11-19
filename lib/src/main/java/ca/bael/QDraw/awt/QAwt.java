// Bailey JT Brown
// 2026
// QAwt.java

package ca.bael.QDraw.awt;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import ca.bael.QDraw.QImage;

public final class QAwt {
    /////////////////////////////////////////////////////////////////
    // PUBLIC ENUMS
    public enum Scaling {
        Stretch,
        Fit,
        IntegerScale
    }

    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final int[] ARGB_MASKS = { 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000 };

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public static BufferedImage asBufferedImage(QImage image, boolean useAlpha) {
        int[] masks = useAlpha ? ARGB_MASKS : Arrays.copyOf(ARGB_MASKS, 3);
        DirectColorModel model = useAlpha
            ? new DirectColorModel(32, masks[0], masks[1], masks[2], masks[3])
            : new DirectColorModel(24, masks[0], masks[1], masks[2]);
        WritableRaster raster = Raster.createPackedRaster(
            new DataBufferInt(image.getPixels( ), image.getPixels( ).length),
            image.getWidth( ),
            image.getHeight( ),
            image.getWidth( ),
            masks,
            null
        );
        return new BufferedImage(model, raster, false, null);
    }

    public static void drawScaled(Graphics2D g, BufferedImage image, int width, int height, Scaling scaling) {
        int w = width;
        int h = height;
        if (scaling != Scaling.Stretch) {
            float fac = Math.min((float)width / image.getWidth( ), (float)height / image.getHeight( ));
            if (scaling == Scaling.IntegerScale) {
                fac = Math.max(1.0f, (float)Math.floor(fac));
            }
            w = (int)(image.getWidth( ) * fac);
            h = (int)(image.getHeight( ) * fac);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
        }
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(image, (width - w) / 2, (height - h) / 2, w, h, null);
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    private QAwt( ) { }
}
