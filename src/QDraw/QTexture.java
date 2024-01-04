// Bailey JT Brown
// 2024
// QTexture.java

package QDraw;

import java.io.*;
import java.awt.image.*;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import QDraw.QException.PointOfError;

public class QTexture extends QSampleable {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    // note: CHUNK_WIDTH/HEIGHT must be power of 4
    private static final int CHUNK_WIDTH  = 16;
    private static final int CHUNK_HEIGHT = 16;
    private static final int CHUNK_WIDTH_REMAINDER_MASK  = CHUNK_WIDTH  - 1;
    private static final int CHUNK_HEIGHT_REMAINDER_MASK = CHUNK_HEIGHT - 1;
    private static final int CHUNK_WIDTH_FAC_SHIFT  = 31 - Integer.numberOfLeadingZeros(CHUNK_WIDTH);
    private static final int CHUNK_HEIGHT_FAC_SHIFT = 31 - Integer.numberOfLeadingZeros(CHUNK_HEIGHT);
    private static final int CHUNK_AREA             = CHUNK_WIDTH * CHUNK_HEIGHT;
    private static final int CHUNK_AREA_FAC_SHIFT   = 31 - Integer.numberOfLeadingZeros(CHUNK_AREA);

    public static QTexture CheckerBoard(int size) {
        return CheckerBoard(size, QColor.White(), QColor.Black());
    }

    public static QTexture CheckerBoard(int size, QColor col1, QColor col2) {
        QTexture board = new QTexture(size, size);
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
    private int   width, height;
    private int   xChunks, yChunks;
    private int[] colorBuffer;

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private void initColorBuffer(int targetWidth, int targetHeight) {
        // NOTE:
        // in this encoding, width/height dimensions must be multiples of
        // CHUNK_WIDTH and CHUNK_HEIGHT, so we will allocate in excess
        width  = targetWidth;
        height = targetHeight;

        xChunks        = targetWidth >> CHUNK_WIDTH_FAC_SHIFT;
        int xRemainder = targetWidth & CHUNK_WIDTH_REMAINDER_MASK;
        if (xRemainder > 0) { xChunks++; }

        yChunks        = targetHeight >> CHUNK_HEIGHT_FAC_SHIFT;
        int yRemainder = targetHeight & CHUNK_HEIGHT_REMAINDER_MASK;
        if (yRemainder > 0) { yChunks++; }

        colorBuffer = new int[CHUNK_AREA * xChunks * yChunks];
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public int translateIndex(int x, int y) {
        // NOTE:
        // - the texture encoding scheme is designed with cache locality in mind.
        //   typically, the next sampled pixel will be near to the first one, however,
        //   if the next pixel is one below, we will have to access an entire scanline
        //   across memory
        // - the principle behind this encoding scheme is to subdivide the image into
        //   local chunks, so that when a nearby pixel is accessed, we wont have to move
        //   so many scanlines over
        // - chunks are in dimensions of 4^n x 4^n so that we can do this quick bitwise/shift
        //   tricks to speed up our translation calculations
        // - (x, y) maps to chunk (x / 4^n, y / 4^n) and the sub-chunk index is just
        //   (x mod 4^n, y mod 4^n)
        // - this requires that we occasionally allocate more memory to a texture than needed
        //   at times but the overhead is minimal.

        int chunkX = x >> CHUNK_WIDTH_FAC_SHIFT;
        int chunkY = y >> CHUNK_HEIGHT_FAC_SHIFT;

        int chunkSubX = x & CHUNK_WIDTH_REMAINDER_MASK;
        int chunkSubY = y & CHUNK_HEIGHT_REMAINDER_MASK;

        int offsetMajor = (chunkX + (xChunks * chunkY)) << CHUNK_AREA_FAC_SHIFT;
        int offsetMinor = chunkSubX + (chunkSubY << CHUNK_WIDTH_FAC_SHIFT);

        return offsetMajor + offsetMinor;
    }

    public int getWidth( ) {
        return width;
    }

    public int getXChunks( ) {
        return xChunks;
    }

    public int getHeight( ) {
        return height;
    }

    public int getYChunks( ) {
        return yChunks;
    }

    public int[] getColorBuffer( ) {
        return colorBuffer;
    }

    public int getColor(int x, int y) {
        return colorBuffer[translateIndex(x, y)];
    }

    public void setColor(int x, int y, int color) {
        colorBuffer[translateIndex(x, y)] = color;
    }

    public QTexture fromRenderBuffer(QRenderBuffer rb) {
        QTexture tex = new QTexture(rb.getWidth(), rb.getHeight());
        rb.copyTo(tex);
        return tex;
    }

    public QRenderBuffer toRenderBuffer( ) {
        QRenderBuffer rb = new QRenderBuffer(width, height);
        copyTo(rb);
        return rb;
    }

    public void save(String fileSaveOut) {
        if (!fileSaveOut.endsWith(".png")) {
            throw new QException(
                PointOfError.InvalidParameter, 
                "must save file as .png, file specified was " + fileSaveOut
            );
        }

        File outFile = null;
        try {
            outFile = new File(fileSaveOut);
        } catch (Exception e) {
            throw new QException(
                PointOfError.InvalidParameter, 
                "failed to open file " + fileSaveOut
            );
        }

        BufferedImage tempImage = toRenderBuffer().getBufferedImage();

        try {
            boolean result = ImageIO.write(tempImage, "png", outFile);
            if (!result) { throw new RuntimeException(); } // this is a bad hack
        } catch (Exception e) {
            throw new QException(
                PointOfError.BadState, 
                "failed to write to file " + fileSaveOut
            );
        }
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public QTexture(int width, int height) {
        initColorBuffer(width, height);
    }

    public QTexture(String imgPath) {

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
        BufferedImage bufferARGB = new BufferedImage(
            tempImage.getWidth(), 
            tempImage.getHeight(), 
            COLOR_PACKING
        );

        Graphics2D tempGraphics = bufferARGB.createGraphics();
        tempGraphics.drawImage(tempImage, 0, 0, null);
        tempGraphics.dispose();

        // grab ARGB colors, init texture and copy
        int[] colorsARGB = ((DataBufferInt)(bufferARGB.getRaster().getDataBuffer())).getData();
        initColorBuffer(bufferARGB.getWidth(), bufferARGB.getHeight());
        for (int copyX = 0; copyX < bufferARGB.getWidth(); copyX++) {
            for (int copyY = 0; copyY < bufferARGB.getHeight(); copyY++) {
                int invertY = (bufferARGB.getHeight() - copyY - 1);
                setColor(copyX, copyY, colorsARGB[copyX + (invertY * bufferARGB.getWidth())]);
            }
        }
    }
}
