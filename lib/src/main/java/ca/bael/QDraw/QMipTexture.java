// Bailey JT Brown
// 2026
// QMipTexture.java

package ca.bael.QDraw;

import java.util.ArrayList;
import ca.bael.QDraw.QException.PointOfError;

public final class QMipTexture extends QSampleable {
    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private final QTexture[] levels;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public int getLevelCount( ) {
        return levels.length;
    }

    public QTexture getLevel(int level) {
        return levels[level];
    }

    public int getWidth( ) {
        return levels[0].getWidth( );
    }

    public int getHeight( ) {
        return levels[0].getHeight( );
    }

    public int getColor(int x, int y) {
        return levels[0].getColor(x, y);
    }

    public void setColor(int x, int y, int color) {
        throw new QException(
            PointOfError.BadState,
            "QMipTexture is read-only"
        );
    }

    public int sampleLod(float u, float v, float lod, SampleType sampleType, FilterType filterType) {
        float clamped = Math.max(0.0f, Math.min(lod, levels.length - 1));
        if (filterType == FilterType.Nearest) {
            return levels[Math.round(clamped)].sample(u, v, sampleType, FilterType.Nearest);
        }
        int l0 = (int)clamped;
        int t  = (int)((clamped - l0) * 256.0f);
        int c0 = levels[l0].sample(u, v, sampleType, FilterType.Bilinear);
        if (t == 0) {
            return c0;
        }
        return lerpARGB(c0, levels[l0 + 1].sample(u, v, sampleType, FilterType.Bilinear), t);
    }

    /////////////////////////////////////////////////////////////////
    // PROTECTED METHODS
    @Override
    protected int doSampleNearest(float u, float v, SampleType sampleType) {
        return levels[0].doSampleNearest(u, v, sampleType);
    }

    @Override
    protected int doSampleBilinear(float u, float v, SampleType sampleType) {
        return levels[0].doSampleBilinear(u, v, sampleType);
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private static QTexture internalDownsample(QTexture src) {
        int w = src.getWidth( );
        int h = src.getHeight( );
        QTexture dst = new QTexture(Math.max(1, w / 2), Math.max(1, h / 2));
        for (int y = 0; y < dst.getHeight( ); y++) {
            for (int x = 0; x < dst.getWidth( ); x++) {
                int x0 = Math.min(2 * x, w - 1);
                int x1 = Math.min(2 * x + 1, w - 1);
                int y0 = Math.min(2 * y, h - 1);
                int y1 = Math.min(2 * y + 1, h - 1);
                dst.setColor(x, y, internalAverage4(
                    src.getColor(x0, y0), src.getColor(x1, y0),
                    src.getColor(x0, y1), src.getColor(x1, y1)
                ));
            }
        }
        return dst;
    }

    private static int internalAverage4(int a, int b, int c, int d) {
        int rb = ((a & 0x00FF00FF) + (b & 0x00FF00FF) + (c & 0x00FF00FF) + (d & 0x00FF00FF) + 0x00020002) >>> 2 & 0x00FF00FF;
        int ag = (((a >>> 8) & 0x00FF00FF) + ((b >>> 8) & 0x00FF00FF) + ((c >>> 8) & 0x00FF00FF) + ((d >>> 8) & 0x00FF00FF) + 0x00020002) << 6 & 0xFF00FF00;
        return rb | ag;
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public QMipTexture(QSampleable base) {
        ArrayList<QTexture> chain = new ArrayList<>( );
        QTexture level = new QTexture(base.getWidth( ), base.getHeight( ));
        base.copyTo(level);
        chain.add(level);
        while (level.getWidth( ) > 1 || level.getHeight( ) > 1) {
            level = internalDownsample(level);
            chain.add(level);
        }
        levels = chain.toArray(new QTexture[0]);
    }
}
