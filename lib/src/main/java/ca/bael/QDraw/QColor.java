// Bailey JT Brown
// 2023-2026
// QColor.java

package ca.bael.QDraw;

import ca.bael.QDraw.QException.PointOfError;

public final class QColor implements QUniform<QColor> {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    // QColor is int
    // 4-byte integer encoded with each byte as ARGB
    private static final int COL_LSHIFT_OFST_A = 24;
    private static final int COL_LSHIFT_OFST_R = 16;
    private static final int COL_LSHIFT_OFST_G = 8;
    private static final int COL_LSHIFT_OFST_B = 0;
    private static final int COL_CHNL_BMASK    = 0x000000FF;
    private static final int COL_BMASK_A       = COL_CHNL_BMASK << COL_LSHIFT_OFST_A;
    private static final int COL_BMASK_R       = COL_CHNL_BMASK << COL_LSHIFT_OFST_R;
    private static final int COL_BMASK_G       = COL_CHNL_BMASK << COL_LSHIFT_OFST_G;
    private static final int COL_BMASK_B       = COL_CHNL_BMASK << COL_LSHIFT_OFST_B;

    public static QColor Black() { return new QColor(0x00, 0x00, 0x00); }
    public static QColor White() { return new QColor(0xFF, 0xFF, 0xFF); }
    public static QColor Red()   { return new QColor(0xFF, 0x00, 0x00); }
    public static QColor Green() { return new QColor(0x00, 0xFF, 0x00); }
    public static QColor Blue()  { return new QColor(0x00, 0x00, 0xFF); }
    public static QColor Alpha() { return new QColor(0x00, 0x00, 0x00, 0x00); }

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private int colorData = 0;

    /////////////////////////////////////////////////////////////////
    // PUBLIC ENUMS
    public enum Channel {
        A, R, G, B
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private static int clampChannel(int val) {
        return Math.max(0x00, Math.min(COL_CHNL_BMASK, val));
    }

    private static int channel(int argb, int offset) {
        return (argb >>> offset) & COL_CHNL_BMASK;
    }

    private static int div255(int x) {
        return (x + 1 + (x >>> 8)) >>> 8;
    }

    private static int opaque(int r, int g, int b) {
        return COL_BMASK_A |
               (clampChannel(r) << COL_LSHIFT_OFST_R) |
               (clampChannel(g) << COL_LSHIFT_OFST_G) |
               (clampChannel(b) << COL_LSHIFT_OFST_B);
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC STATIC METHODS (packed ARGB)
    public static int blend(int bottom, int top) {
        int tFac = channel(top, COL_LSHIFT_OFST_A);
        if (tFac == 0xFF) return top;
        if (tFac == 0x00) return bottom;
        int bAlpha = channel(bottom, COL_LSHIFT_OFST_A);
        if (bAlpha == 0x00) return top;

        int bFac = 0xFF - tFac;
        if (bAlpha == 0xFF) {
            return opaque(
                div255(channel(top, COL_LSHIFT_OFST_R) * tFac + channel(bottom, COL_LSHIFT_OFST_R) * bFac),
                div255(channel(top, COL_LSHIFT_OFST_G) * tFac + channel(bottom, COL_LSHIFT_OFST_G) * bFac),
                div255(channel(top, COL_LSHIFT_OFST_B) * tFac + channel(bottom, COL_LSHIFT_OFST_B) * bFac)
            );
        }
        int wt  = tFac * 0xFF;
        int wb  = bAlpha * bFac;
        int sum = wt + wb;
        return (div255(sum) << COL_LSHIFT_OFST_A) |
               (((channel(top, COL_LSHIFT_OFST_R) * wt + channel(bottom, COL_LSHIFT_OFST_R) * wb) / sum) << COL_LSHIFT_OFST_R) |
               (((channel(top, COL_LSHIFT_OFST_G) * wt + channel(bottom, COL_LSHIFT_OFST_G) * wb) / sum) << COL_LSHIFT_OFST_G) |
               (((channel(top, COL_LSHIFT_OFST_B) * wt + channel(bottom, COL_LSHIFT_OFST_B) * wb) / sum) << COL_LSHIFT_OFST_B);
    }

    public static int add(int c1, int c2) {
        return opaque(
            channel(c1, COL_LSHIFT_OFST_R) + channel(c2, COL_LSHIFT_OFST_R),
            channel(c1, COL_LSHIFT_OFST_G) + channel(c2, COL_LSHIFT_OFST_G),
            channel(c1, COL_LSHIFT_OFST_B) + channel(c2, COL_LSHIFT_OFST_B)
        );
    }

    public static int multiply(int c1, int c2) {
        return opaque(
            div255(channel(c1, COL_LSHIFT_OFST_R) * channel(c2, COL_LSHIFT_OFST_R)),
            div255(channel(c1, COL_LSHIFT_OFST_G) * channel(c2, COL_LSHIFT_OFST_G)),
            div255(channel(c1, COL_LSHIFT_OFST_B) * channel(c2, COL_LSHIFT_OFST_B))
        );
    }

    public static int multiply(int c, float fac) {
        return multiply(c, fac, fac, fac);
    }

    public static int multiply(int c, float facR, float facG, float facB) {
        return opaque(
            (int)(channel(c, COL_LSHIFT_OFST_R) * facR),
            (int)(channel(c, COL_LSHIFT_OFST_G) * facG),
            (int)(channel(c, COL_LSHIFT_OFST_B) * facB)
        );
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC STATIC METHODS (float3)
    public static int pack(QVector3 rgb) {
        return pack(rgb, 1.0f);
    }

    public static int pack(QVector3 rgb, float alpha) {
        return (clampChannel(Math.round(alpha * 255.0f)) << COL_LSHIFT_OFST_A) |
               (clampChannel(Math.round(rgb.getX( ) * 255.0f)) << COL_LSHIFT_OFST_R) |
               (clampChannel(Math.round(rgb.getY( ) * 255.0f)) << COL_LSHIFT_OFST_G) |
               (clampChannel(Math.round(rgb.getZ( ) * 255.0f)) << COL_LSHIFT_OFST_B);
    }

    public static QVector3 unpack(int argb, QVector3 out) {
        out.set(
            channel(argb, COL_LSHIFT_OFST_R) / 255.0f,
            channel(argb, COL_LSHIFT_OFST_G) / 255.0f,
            channel(argb, COL_LSHIFT_OFST_B) / 255.0f
        );
        return out;
    }

    public static QVector3 rgbToHsv(QVector3 rgb, QVector3 out) {
        float r   = rgb.getX( );
        float g   = rgb.getY( );
        float b   = rgb.getZ( );
        float max = Math.max(r, Math.max(g, b));
        float d   = max - Math.min(r, Math.min(g, b));
        float h;
        if (d == 0.0f) {
            h = 0.0f;
        } else if (max == r) {
            h = (g - b) / d + (g < b ? 6.0f : 0.0f);
        } else if (max == g) {
            h = (b - r) / d + 2.0f;
        } else {
            h = (r - g) / d + 4.0f;
        }
        out.set(h * 60.0f, max == 0.0f ? 0.0f : d / max, max);
        return out;
    }

    public static QVector3 hsvToRgb(QVector3 hsv, QVector3 out) {
        float h = (hsv.getX( ) % 360.0f + 360.0f) % 360.0f / 60.0f;
        float s = hsv.getY( );
        float v = hsv.getZ( );
        int   i = (int)h;
        float f = h - i;
        float p = v * (1.0f - s);
        float q = v * (1.0f - s * f);
        float t = v * (1.0f - s * (1.0f - f));
        switch (i) {
            case 0:  out.set(v, t, p); break;
            case 1:  out.set(q, v, p); break;
            case 2:  out.set(p, v, t); break;
            case 3:  out.set(p, q, v); break;
            case 4:  out.set(t, p, v); break;
            default: out.set(v, p, q); break;
        }
        return out;
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public int toInt( ) {
        return colorData;
    }

    public QColor snapshot( ) {
        return new QColor(colorData);
    }

    public int getChannel(Channel c) {
        int channelOffset;
        int channelMask;
        switch (c) {
            case A:
                channelOffset = COL_LSHIFT_OFST_A;
                channelMask   = COL_BMASK_A;
                break;
            case R:
                channelOffset = COL_LSHIFT_OFST_R;
                channelMask   = COL_BMASK_R;
                break;
            case G:
                channelOffset = COL_LSHIFT_OFST_G;
                channelMask   = COL_BMASK_G;
                break;
            case B:
                channelOffset = COL_LSHIFT_OFST_B;
                channelMask   = COL_BMASK_B;
                break;
            default:
                throw new QException(PointOfError.BadState, "bad switch state");
        }

        return (colorData & channelMask) >>> channelOffset;
    }

    public int getR( ) { return channel(colorData, COL_LSHIFT_OFST_R); };
    public int getG( ) { return channel(colorData, COL_LSHIFT_OFST_G); };
    public int getB( ) { return channel(colorData, COL_LSHIFT_OFST_B); };
    public int getA( ) { return channel(colorData, COL_LSHIFT_OFST_A); };

    public QColor setChannel(int val, Channel c) {
        int channelOffset;
        int channelMask;
        switch (c) {
            case A:
                channelOffset = COL_LSHIFT_OFST_A;
                channelMask   = COL_BMASK_A;
                break;
            case R:
                channelOffset = COL_LSHIFT_OFST_R;
                channelMask   = COL_BMASK_R;
                break;
            case G:
                channelOffset = COL_LSHIFT_OFST_G;
                channelMask   = COL_BMASK_G;
                break;
            case B:
                channelOffset = COL_LSHIFT_OFST_B;
                channelMask   = COL_BMASK_B;
                break;
            default:
                throw new QException(PointOfError.BadState, "bad switch state");
        }

        val = clampChannel(val);
        colorData = (colorData & ~(channelMask)) | (val << channelOffset);

        return this;
    }

    public QColor setR(int r) { return setChannel(r, Channel.R); };
    public QColor setG(int g) { return setChannel(g, Channel.G); };
    public QColor setB(int b) { return setChannel(b, Channel.B); };
    public QColor setA(int a) { return setChannel(a, Channel.A); };

    public QColor set(int argb) {
        colorData = argb;
        return this;
    }

    public QColor set(int r, int g, int b) {
        set(r, g, b, 0xFF);
        return this;
    }

    public QColor set(int r, int g, int b, int a) {
        setChannel(r, Channel.R);
        setChannel(g, Channel.G);
        setChannel(b, Channel.B);
        setChannel(a, Channel.A);
        return this;
    }

    public QColor set(QColor toCopy) {
        this.colorData = toCopy.colorData;
        return this;
    }

    public QColor set(QVector3 rgb) {
        colorData = pack(rgb);
        return this;
    }

    public QColor set(QVector3 rgb, float alpha) {
        colorData = pack(rgb, alpha);
        return this;
    }

    public QColor blend(QColor top) {
        colorData = blend(colorData, top.colorData);
        return this;
    }

    public QColor add(QColor other) {
        colorData = add(colorData, other.colorData);
        return this;
    }

    public QColor multiply(QColor other) {
        colorData = multiply(colorData, other.colorData);
        return this;
    }

    public QColor multiply(float fac) {
        colorData = multiply(colorData, fac);
        return this;
    }

    public QColor multiply(float facR, float facG, float facB) {
        colorData = multiply(colorData, facR, facG, facB);
        return this;
    }

    public boolean equalsIgnoreAlpha(QColor col) {
        return equalsIgnoreAlpha(col.getR(), col.getG(), col.getB());
    }

    public boolean equalsIgnoreAlpha(int r, int g, int b) {
        return getR() == r && getG() == g && getB() == b;
    }

    public boolean equals(int r, int g, int b, int a) {
        return equals(new QColor(r, g, b, a));
    }

    public boolean equals(QColor col) {
        return colorData == col.toInt();
    }
    
    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public QColor( ) {
        colorData = 0;
    }

    public QColor(int argb) {
        colorData = argb;
    };

    public QColor(int r, int g, int b) {
        colorData = 0;
        set(r, g, b);
    }

    public QColor(int r, int g, int b, int a) {
        colorData = 0;
        set(r, g, b, a);
    }
}
