// Bailey JT Brown
// 2023-2024
// QRenderBuffer.java

package ca.bael.QDraw;

import java.awt.image.*;
import ca.bael.QDraw.QException.PointOfError;

public final class QRenderBuffer extends QSampleable {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final int   CLEAR_COLOR   = new QColor(0x00, 0x00, 0x00, 0x00).toInt();
    private static final float CLEAR_DEPTH   = 0.0f;

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private final BufferedImage buffer;
    private final float[]       depthBuffer;
    private final int[]         colorBuffer;
    private final int           width;
    private final int           height;

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    public QRenderBuffer(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new QException(
                PointOfError.InvalidParameter, 
                "width or height was <= 0"
            );
        }

        this.width    = width;
        this.height   = height;
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

    public int getColor(int x, int y) {
        return colorBuffer[coordToDataIndex(x, y)];
    }

    public void setColor(int x, int y, int c) {
        colorBuffer[coordToDataIndex(x, y)] = c;
    }

    public float[] getDepthData( ) {
        return depthBuffer;
    }

    public float getDepth(int x, int y) {
        return depthBuffer[coordToDataIndex(x, y)];
    }

    public void setDepth(int x, int y, float d) {
        depthBuffer[coordToDataIndex(x, y)] = d;
    }

    public int coordToDataIndex(int x, int y) {
        return x + (width * (height - y - 1));
    }

    public int getWidth( ) {
        return width;
    }

    public int getHeight( ) {
        return height;
    }

    public void clearColorBuffer( ) {
        for (int i = 0; i < colorBuffer.length; i++) {
            colorBuffer[i] = CLEAR_COLOR;
        }
    }

    public void clearDepthBuffer( ) {
        for (int i = 0; i < depthBuffer.length; i++) {
            depthBuffer[i] = CLEAR_DEPTH;
        }
    }
}
