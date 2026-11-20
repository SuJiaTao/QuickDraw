// Bailey JT Brown
// 2026
// QImage.java

package ca.bael.QDraw;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import ca.bael.QDraw.QException.PointOfError;

public final class QImage extends QSampleable {
    /////////////////////////////////////////////////////////////////
    // PUBLIC ENUMS
    public enum PixelLayout {
        ARGB,
        BGRA,
        RGBA,
        ABGR
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private final int[] pixels;
    private final int   width;
    private final int   height;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public static QImage wrap(int[] pixels, int width, int height) {
        return new QImage(pixels, width, height);
    }

    public int[] getPixels( ) {
        return pixels;
    }

    public int getWidth( ) {
        return width;
    }

    public int getHeight( ) {
        return height;
    }

    public int coordToDataIndex(int x, int y) {
        return x + (width * (height - y - 1));
    }

    public int getColor(int x, int y) {
        return pixels[coordToDataIndex(x, y)];
    }

    public void setColor(int x, int y, int c) {
        pixels[coordToDataIndex(x, y)] = c;
    }

    @Override
    public void copyTo(QSampleable target) {
        if (target instanceof QImage dst && dst.width == width && dst.height == height) {
            System.arraycopy(pixels, 0, dst.pixels, 0, pixels.length);
            return;
        }
        super.copyTo(target);
    }

    public void writeTo(IntBuffer dst, PixelLayout layout, boolean flipY) {
        for (int row = 0; row < height; row++) {
            int base = (flipY ? height - 1 - row : row) * width;
            if (layout == PixelLayout.ARGB) {
                dst.put(pixels, base, width);
                continue;
            }
            for (int i = base; i < base + width; i++) {
                dst.put(swizzle(pixels[i], layout));
            }
        }
    }

    public void writeTo(ByteBuffer dst, PixelLayout layout, boolean flipY) {
        for (int row = 0; row < height; row++) {
            int base = (flipY ? height - 1 - row : row) * width;
            for (int i = base; i < base + width; i++) {
                int c = swizzle(pixels[i], layout);
                dst.put((byte)(c >>> 24));
                dst.put((byte)(c >>> 16));
                dst.put((byte)(c >>> 8));
                dst.put((byte)c);
            }
        }
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private static int swizzle(int argb, PixelLayout layout) {
        return switch (layout) {
            case ARGB -> argb;
            case BGRA -> Integer.reverseBytes(argb);
            case RGBA -> Integer.rotateLeft(argb, 8);
            case ABGR -> (argb & 0xFF00FF00) | ((argb & 0x00FF0000) >>> 16) | ((argb & 0x000000FF) << 16);
        };
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    private QImage(int[] pixels, int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new QException(
                PointOfError.InvalidParameter,
                "width or height was <= 0"
            );
        }
        if (pixels.length != width * height) {
            throw new QException(
                PointOfError.InvalidParameter,
                "pixels length must be width * height (" + (width * height) +
                "). Given was " + pixels.length
            );
        }
        this.pixels = pixels;
        this.width  = width;
        this.height = height;
    }

    public QImage(int width, int height) {
        this(new int[Math.max(0, width) * Math.max(0, height)], width, height);
    }
}
