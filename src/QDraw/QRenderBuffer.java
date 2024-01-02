// Bailey JT Brown
// 2023-2024
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

    public static QRenderBuffer CheckerBoard(int size) {
        return CheckerBoard(size, QColor.Black(), QColor.White());
    }
    
    public static QRenderBuffer CheckerBoard(int size, QColor col1, QColor col2) {
        QRenderBuffer board = new QRenderBuffer(size, size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (((i + j) & 1) == 0) {
                    board.setColor(i, j, col1.toInt());
                } else {
                    board.setColor(i, j, col2.toInt());
                }
            }
        }
        return board;
    }

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
        return 
            x + 
            (buffer.getWidth() * (buffer.getHeight() - y - 1));
    }

    public int getWidth( ) {
        return buffer.getWidth( );
    }

    public int getHeight( ) {
        return buffer.getHeight( );
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
