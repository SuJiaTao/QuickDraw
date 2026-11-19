// Bailey JT Brown
// 2023-2026
// QRenderBuffer.java

package ca.bael.QDraw;

import java.awt.image.*;
import java.util.Arrays;
import ca.bael.QDraw.QException.PointOfError;

public final class QRenderBuffer extends QSampleable {
    /////////////////////////////////////////////////////////////////
    // PUBLIC ENUMS
    public enum GBufferFormat {
        Float3,
        Long
    }

    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final int GBUFFER_SLOTS = 4;
    private static final int   CLEAR_COLOR   = new QColor(0x00, 0x00, 0x00, 0x00).toInt();
    private static final float CLEAR_DEPTH   = 0.0f;

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private final BufferedImage buffer;
    private final float[]       depthBuffer;
    private final int[]         colorBuffer;
    private final int           width;
    private final int           height;
    private final float[][]     gbufferFloats = new float[GBUFFER_SLOTS][];
    private final long[][]      gbufferLongs  = new long[GBUFFER_SLOTS][];

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
        buffer        = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
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

    public void attachGBuffer(int slot, GBufferFormat format) {
        gbufferFloats[slot] = format == GBufferFormat.Float3 ? new float[width * height * 3] : null;
        gbufferLongs[slot]  = format == GBufferFormat.Long ? new long[width * height] : null;
    }

    public GBufferFormat getGBufferFormat(int slot) {
        if (gbufferFloats[slot] != null) {
            return GBufferFormat.Float3;
        }
        return gbufferLongs[slot] != null ? GBufferFormat.Long : null;
    }

    float[] getGBufferFloatData(int slot) {
        return gbufferFloats[slot];
    }

    long[] getGBufferLongData(int slot) {
        return gbufferLongs[slot];
    }

    public QVector3 getGBufferVec3(int slot, int x, int y, QVector3 out) {
        float[] data = gbufferFloats[slot];
        if (data == null) {
            throw new QException(
                PointOfError.BadState,
                "G-buffer slot " + slot + " is not attached as " + GBufferFormat.Float3
            );
        }
        int index = coordToDataIndex(x, y) * 3;
        out.set(data[index], data[index + 1], data[index + 2]);
        return out;
    }

    public long getGBufferLong(int slot, int x, int y) {
        long[] data = gbufferLongs[slot];
        if (data == null) {
            throw new QException(
                PointOfError.BadState,
                "G-buffer slot " + slot + " is not attached as " + GBufferFormat.Long
            );
        }
        return data[coordToDataIndex(x, y)];
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

    void clearRows(int y0, int y1) {
        int from = width * (height - y1);
        int to   = width * (height - y0);
        Arrays.fill(colorBuffer, from, to, CLEAR_COLOR);
        Arrays.fill(depthBuffer, from, to, CLEAR_DEPTH);
        for (int slot = 0; slot < GBUFFER_SLOTS; slot++) {
            if (gbufferFloats[slot] != null) {
                Arrays.fill(gbufferFloats[slot], from * 3, to * 3, 0.0f);
            }
            if (gbufferLongs[slot] != null) {
                Arrays.fill(gbufferLongs[slot], from, to, 0L);
            }
        }
    }
}
