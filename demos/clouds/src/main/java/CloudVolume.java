// Bailey JT Brown
// 2026
// CloudVolume.java

import ca.bael.QDraw.QException;
import ca.bael.QDraw.QException.PointOfError;

public final class CloudVolume {
    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private final int     size;
    private final float[] data;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public float sample(float x, float y, float z) {
        float fx = x * size;
        float fy = y * size;
        float fz = z * size;
        int   ix = (int)Math.floor(fx);
        int   iy = (int)Math.floor(fy);
        int   iz = (int)Math.floor(fz);
        float tx = fx - ix;
        float ty = fy - iy;
        float tz = fz - iz;
        int   m  = size - 1;
        int   x0 = ix & m, x1 = (ix + 1) & m;
        int   y0 = iy & m, y1 = (iy + 1) & m;
        int   z0 = iz & m, z1 = (iz + 1) & m;
        float c00 = internalLerp(data[internalIndex(x0, y0, z0)], data[internalIndex(x1, y0, z0)], tx);
        float c10 = internalLerp(data[internalIndex(x0, y1, z0)], data[internalIndex(x1, y1, z0)], tx);
        float c01 = internalLerp(data[internalIndex(x0, y0, z1)], data[internalIndex(x1, y0, z1)], tx);
        float c11 = internalLerp(data[internalIndex(x0, y1, z1)], data[internalIndex(x1, y1, z1)], tx);
        return internalLerp(internalLerp(c00, c10, ty), internalLerp(c01, c11, ty), tz);
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private int internalIndex(int x, int y, int z) {
        return (z * size + y) * size + x;
    }

    private static float internalLerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public CloudVolume(int size, float[] data) {
        if (size <= 0 || (size & (size - 1)) != 0) {
            throw new QException(PointOfError.InvalidParameter, "size must be a power of two. Given was " + size);
        }
        if (data.length != size * size * size) {
            throw new QException(PointOfError.InvalidParameter, "data must hold size^3 = " + (size * size * size) + " values. Given was " + data.length);
        }
        this.size = size;
        this.data = data;
    }
}
