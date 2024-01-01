// Bailey JT Brown
// 2023-2024
// QColor.java

package QDraw;

import QDraw.QException.PointOfError;

public final class QColor extends QEncoding {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final QColor Black = new QColor(0x00, 0x00, 0x00);
    public static final QColor White = new QColor(0xFF, 0xFF, 0xFF);
    public static final QColor Red   = new QColor(0xFF, 0x00, 0x00);
    public static final QColor Green = new QColor(0x00, 0xFF, 0x00);
    public static final QColor Blue  = new QColor(0x00, 0x00, 0xFF);

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private int colorData = 0;

    /////////////////////////////////////////////////////////////////
    // PUBLIC ENUMS
    public enum Channel {
        A, R, G, B
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public int toInt( ) {
        return colorData;
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

        return (colorData & channelMask) >> channelOffset;
    }

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

        val &= COL_CHNL_BMASK;
        colorData = (colorData & ~(channelMask)) | (val << channelOffset);

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
    
    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public QColor( ) {
        colorData = 0;
    }

    public QColor(int r, int g, int b) {
        colorData = 0;
        set(r, g, b);
    }

    public QColor(int r, int g, int b, int a) {
        colorData = 0;
        set(r, g, b, a);
    }
}
