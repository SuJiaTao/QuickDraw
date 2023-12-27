// Bailey JT Brown
// 2023
// QRenderBuffer.java

package QDraw;

import java.awt.image.*;
import QDraw.QException.PointOfError;

public final class QRenderBuffer {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final int   COLOR_PACKING = BufferedImage.TYPE_INT_ARGB;
    private static final int   CLEAR_COLOR   = new QColor(0x00, 0x00, 0x00, 0x00).toInt();
    private static final float CLEAR_DEPTH   = Float.POSITIVE_INFINITY;

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private BufferedImage buffer;
    private float[]       depthBuffer;
    private int[]         colorBuffer;

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    public QRenderBuffer(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new QException(
                PointOfError.InvalidParameter, 
                "width or height was <= 0"
            );
        }

        buffer        = new BufferedImage(width, height, COLOR_PACKING);
        depthBuffer   = new float[width * height];
        colorBuffer   = ((DataBufferInt)(buffer.getRaster().getDataBuffer())).getData();
    
        clearColorBuffer();
        clearDepthBuffer();
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public BufferedImage getBufferedImage( ) {
        return buffer;
    }

    public int[] getColorData( ) {
        return colorBuffer;
    }

    public float[] getDepthData( ) {
        return depthBuffer;
    }

    public int coordToDataIndex(int x, int y) {
        return 
            x + 
            (buffer.getWidth() * (buffer.getHeight() - y - 1));
    }

    public void clearColorBuffer( ) {
        // TODO: possibly optimize
        // (ensure to profile against regular clear)
        for (int i = 0; i < colorBuffer.length; i++) {
            colorBuffer[i] = CLEAR_COLOR;
        }
    }

    public void clearDepthBuffer( ) {
        // TODO: possibly optimize
        // TODO: possibly change clear value
        for (int i = 0; i < depthBuffer.length; i++) {
            depthBuffer[i] = CLEAR_DEPTH;
        }
    }
}
