// Bailey JT Brown
// 2024
// QTexture.java

package QDraw;

// TODO: make internal packing FASTER than RB
public class QTexture extends QSampleable {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    // note: CHUNK_WIDTH/HEIGHT must be power of 4
    private static final int CHUNK_WIDTH  = 32;
    private static final int CHUNK_HEIGHT = 32;
    private static final int CHUNK_WIDTH_REMAINDER_MASK  = CHUNK_WIDTH  - 1;
    private static final int CHUNK_HEIGHT_REMAINDER_MASK = CHUNK_HEIGHT - 1;
    private static final int CHUNK_WIDTH_DIV_SHIFT  = 31 - Integer.numberOfLeadingZeros(CHUNK_WIDTH);
    private static final int CHUNK_HEIGHT_DIV_SHIFT = 31 - Integer.numberOfLeadingZeros(CHUNK_HEIGHT);
    private static final int CHUNK_AREA = CHUNK_WIDTH * CHUNK_HEIGHT;
    private static final int CHUNK_AREA_MUL_SHIFT    = 31 - Integer.numberOfLeadingZeros(CHUNK_AREA);

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

        xChunks        = targetWidth >> CHUNK_WIDTH_DIV_SHIFT;
        int xRemainder = targetWidth & CHUNK_WIDTH_REMAINDER_MASK;
        if (xRemainder > 0) { xChunks++; }

        yChunks        = targetHeight >> CHUNK_HEIGHT_DIV_SHIFT;
        int yRemainder = targetWidth & CHUNK_HEIGHT_REMAINDER_MASK;
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

        int chunkX = x >> CHUNK_WIDTH_DIV_SHIFT;
        int chunkY = y >> CHUNK_HEIGHT_DIV_SHIFT;

        int chunkSubX = x & CHUNK_WIDTH_REMAINDER_MASK;
        int chunkSubY = y & CHUNK_HEIGHT_REMAINDER_MASK;

        int offsetMajor = (chunkX + (xChunks * chunkY)) << CHUNK_AREA_MUL_SHIFT;
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
