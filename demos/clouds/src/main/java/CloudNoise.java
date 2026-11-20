// Bailey JT Brown
// 2026
// CloudNoise.java

import java.util.Random;
import java.util.stream.IntStream;

public final class CloudNoise {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final int      TABLE_SIZE       = 256;
    public static final int      PERLIN_FREQUENCY = 4;
    public static final int      PERLIN_OCTAVES   = 4;
    public static final int      WORLEY_FREQUENCY = 4;
    private static final float[] WORLEY_WEIGHTS   = { 0.625f, 0.25f, 0.125f };

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private final int[]   perm     = new int[TABLE_SIZE * 2];
    private final float[] features = new float[TABLE_SIZE * 3];

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public static CloudVolume bakeShape(int size, long seed) {
        CloudNoise noise = new CloudNoise(seed);
        float[]    data  = new float[size * size * size];
        IntStream.range(0, size).parallel( ).forEach(z -> {
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    data[(z * size + y) * size + x] = noise.internalShape((float)x / size, (float)y / size, (float)z / size);
                }
            }
        });
        return new CloudVolume(size, data);
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private float internalShape(float x, float y, float z) {
        float perlin = Math.abs(internalPerlinFbm(x, y, z));
        float worley = 0.0f;
        for (int i = 0; i < WORLEY_WEIGHTS.length; i++) {
            worley += WORLEY_WEIGHTS[i] * internalWorley(x, y, z, WORLEY_FREQUENCY << i);
        }
        float perlinWorley = internalRemap(perlin, 0.0f, 1.0f, worley, 1.0f);
        return Math.max(0.0f, Math.min(1.0f, internalRemap(perlinWorley, worley - 1.0f, 1.0f, 0.0f, 1.0f)));
    }

    private float internalPerlinFbm(float x, float y, float z) {
        float sum   = 0.0f;
        float amp   = 1.0f;
        float total = 0.0f;
        for (int i = 0; i < PERLIN_OCTAVES; i++) {
            int freq = PERLIN_FREQUENCY << i;
            sum   += amp * internalPerlin(x * freq, y * freq, z * freq, freq);
            total += amp;
            amp   *= 0.5f;
        }
        return sum / total;
    }

    private float internalPerlin(float x, float y, float z, int period) {
        int   xi = (int)Math.floor(x);
        int   yi = (int)Math.floor(y);
        int   zi = (int)Math.floor(z);
        float xf = x - xi;
        float yf = y - yi;
        float zf = z - zi;
        int   x0 = Math.floorMod(xi, period), x1 = Math.floorMod(xi + 1, period);
        int   y0 = Math.floorMod(yi, period), y1 = Math.floorMod(yi + 1, period);
        int   z0 = Math.floorMod(zi, period), z1 = Math.floorMod(zi + 1, period);
        float u  = internalFade(xf);
        float v  = internalFade(yf);
        float w  = internalFade(zf);
        return internalLerp(
            internalLerp(
                internalLerp(internalGrad(internalHash(x0, y0, z0), xf, yf, zf),        internalGrad(internalHash(x1, y0, z0), xf - 1, yf, zf), u),
                internalLerp(internalGrad(internalHash(x0, y1, z0), xf, yf - 1, zf),    internalGrad(internalHash(x1, y1, z0), xf - 1, yf - 1, zf), u),
                v),
            internalLerp(
                internalLerp(internalGrad(internalHash(x0, y0, z1), xf, yf, zf - 1),     internalGrad(internalHash(x1, y0, z1), xf - 1, yf, zf - 1), u),
                internalLerp(internalGrad(internalHash(x0, y1, z1), xf, yf - 1, zf - 1), internalGrad(internalHash(x1, y1, z1), xf - 1, yf - 1, zf - 1), u),
                v),
            w);
    }

    private float internalWorley(float x, float y, float z, int cells) {
        float cx   = x * cells;
        float cy   = y * cells;
        float cz   = z * cells;
        int   xi   = (int)Math.floor(cx);
        int   yi   = (int)Math.floor(cy);
        int   zi   = (int)Math.floor(cz);
        float best = Float.MAX_VALUE;
        for (int dz = -1; dz <= 1; dz++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int   nx = xi + dx;
                    int   ny = yi + dy;
                    int   nz = zi + dz;
                    int   h  = internalHash(Math.floorMod(nx, cells), Math.floorMod(ny, cells), Math.floorMod(nz, cells)) * 3;
                    float ox = nx + features[h]     - cx;
                    float oy = ny + features[h + 1] - cy;
                    float oz = nz + features[h + 2] - cz;
                    best = Math.min(best, ox * ox + oy * oy + oz * oz);
                }
            }
        }
        return 1.0f - Math.min(1.0f, (float)Math.sqrt(best));
    }

    private int internalHash(int x, int y, int z) {
        return perm[perm[perm[x] + y] + z];
    }

    private static float internalGrad(int hash, float x, float y, float z) {
        int   h = hash & 15;
        float u = h < 8 ? x : y;
        float v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    private static float internalFade(float t) {
        return t * t * t * (t * (t * 6.0f - 15.0f) + 10.0f);
    }

    private static float internalLerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float internalRemap(float v, float lo, float hi, float newLo, float newHi) {
        return newLo + (v - lo) / (hi - lo) * (newHi - newLo);
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    private CloudNoise(long seed) {
        Random random = new Random(seed);
        for (int i = 0; i < TABLE_SIZE; i++) {
            perm[i] = i;
        }
        for (int i = TABLE_SIZE - 1; i > 0; i--) {
            int j   = random.nextInt(i + 1);
            int tmp = perm[i];
            perm[i] = perm[j];
            perm[j] = tmp;
        }
        System.arraycopy(perm, 0, perm, TABLE_SIZE, TABLE_SIZE);
        for (int i = 0; i < features.length; i++) {
            features[i] = random.nextFloat( );
        }
    }
}
