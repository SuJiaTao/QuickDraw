// Bailey JT Brown
// 2024
// QTexture.java

package QDraw;

public class QTexture extends QSampleable {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final int CHUNK_WIDTH  = 4;
    private static final int CHUNK_HEIGHT = 4;
    private static final int CHUNK_AREA   = CHUNK_WIDTH * CHUNK_HEIGHT;

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
        xChunks        = targetWidth  >> 2;
        int xRemainder = targetWidth & 0b11;
        if (xRemainder > 0) { xChunks++; }

        yChunks        = targetHeight >> 2;
        int yRemainder = targetWidth & 0b11;
        if (yRemainder > 0) { yChunks++; }

        colorBuffer = new int[CHUNK_AREA * xChunks * yChunks];
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public int translateIndex(int x, int y) {
        int chunkX = x >> 2; // >> 2 is the same is division by 4
        int chunkY = y >> 2;

        int chunkSubX = x & 0b11;
        int chunkSubY = y & 0b11;

        int offsetMajor = CHUNK_AREA * (chunkX + (xChunks * chunkY));
        int offsetMinor = chunkSubX + (CHUNK_WIDTH * chunkSubY);

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

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public QTexture(int width, int height) {
        initColorBuffer(width, height);
    }

    public QTexture(String imgPath) {
        // renderbuffer convinently converts the image to ARGB format
        QRenderBuffer tempTexture = new QRenderBuffer(imgPath);

        // copy over data (this can be slow)
        initColorBuffer(tempTexture.getWidth(), tempTexture.getHeight());
        for (int copyX = 0; copyX < tempTexture.getWidth(); copyX++) {
            for (int copyY = 0; copyY < tempTexture.getHeight(); copyY++) {
                setColor(copyX, copyY, tempTexture.getColor(copyX, copyY));
            }
        }
    }
}
