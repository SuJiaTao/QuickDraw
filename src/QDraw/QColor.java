// Bailey JT Brown
// 2023
// QColor.java

package QDraw;

import QDraw.QException.PointOfError;

public final class QColor {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final int LEFTSHIFT_OFFSET_A = 24;
    private static final int LEFTSHIFT_OFFSET_R = 16;
    private static final int LEFTSHIFT_OFFSET_G = 8;
    private static final int LEFTSHIFT_OFFSET_B = 0;
    private static final int BITMASK_COLOR      = 0x000000FF;
    private static final int BITMASK_A          = BITMASK_COLOR << LEFTSHIFT_OFFSET_A;
    private static final int BITMASK_R          = BITMASK_COLOR << LEFTSHIFT_OFFSET_R;
    private static final int BITMASK_G          = BITMASK_COLOR << LEFTSHIFT_OFFSET_G;
    private static final int BITMASK_B          = BITMASK_COLOR << LEFTSHIFT_OFFSET_B;
    
    public static final QColor red   = new QColor(0xFF, 0x00, 0x00);
    public static final QColor green = new QColor(0x00, 0xFF, 0x00);
    public static final QColor blue  = new QColor(0x00, 0x00, 0xFF);

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
                channelOffset = LEFTSHIFT_OFFSET_A;
                channelMask   = BITMASK_A;
                break;
            case R:
                channelOffset = LEFTSHIFT_OFFSET_R;
                channelMask   = BITMASK_R;
                break;
            case G:
                channelOffset = LEFTSHIFT_OFFSET_G;
                channelMask   = BITMASK_G;
                break;
            case B:
                channelOffset = LEFTSHIFT_OFFSET_B;
                channelMask   = BITMASK_B;
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
                channelOffset = LEFTSHIFT_OFFSET_A;
                channelMask   = BITMASK_A;
                break;
            case R:
                channelOffset = LEFTSHIFT_OFFSET_R;
                channelMask   = BITMASK_R;
                break;
            case G:
                channelOffset = LEFTSHIFT_OFFSET_G;
                channelMask   = BITMASK_G;
                break;
            case B:
                channelOffset = LEFTSHIFT_OFFSET_B;
                channelMask   = BITMASK_B;
                break;
            default:
                throw new QException(PointOfError.BadState, "bad switch state");
        }

        val &= BITMASK_COLOR;
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

    public static QColor blend(QColor bottom, QColor top) {
        // TODO: decide whether to implement color blending
        return bottom;
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
