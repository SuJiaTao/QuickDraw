// Bailey JT Brown
// 2023-2024
// QRenderBuffer.java

package QDraw;

import java.io.*;

import javax.imageio.ImageIO;

import java.awt.Graphics2D;
import java.awt.image.*;
import QDraw.QException.PointOfError;

public final class QRenderBuffer extends QSampleable {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final int   CLEAR_COLOR   = new QColor(0x00, 0x00, 0x00, 0x00).toInt();
    private static final float CLEAR_DEPTH   = 0.0f;

    public static QRenderBuffer CheckerBoard(int size) {
        return CheckerBoard(size, QColor.Alpha(), QColor.White());
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
    public QRenderBuffer(String imgPath) {
        File imgFile = null;
        try {
            imgFile = new File(imgPath);
        } catch (Exception e) {
            throw new QException(
                PointOfError.InvalidParameter, 
                "failed to open image file: " + imgPath
            );
        }

        BufferedImage tempImage = null;
        try {
            tempImage = ImageIO.read(imgFile);
        } catch (Exception e) {
            throw new QException(
                PointOfError.InvalidParameter, 
                "failed to read image file: " + imgPath
            );
        }

        // refer to
        // https://stackoverflow.com/questions/10391778/create-a-bufferedimage-from-file-and-make-it-type-int-argb
        // this is sadly the only way to coerce a read image to an ARGB int

        buffer = new BufferedImage(
            tempImage.getWidth(), 
            tempImage.getHeight(), 
            COLOR_PACKING
        );

        Graphics2D tempGraphics = buffer.createGraphics();
        tempGraphics.drawImage(tempImage, 0, 0, null);
        tempGraphics.dispose();

        depthBuffer = new float[buffer.getWidth() * buffer.getHeight()];
        colorBuffer = ((DataBufferInt)(buffer.getRaster().getDataBuffer())).getData();

        clearDepthBuffer();
    }

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
