// Bailey JT Brown
// 2024
// QAttribBuffer.java

package QDraw;

import QDraw.QException.PointOfError;

public final class QAttribBuffer {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final int MIN_COMPS_PER_ATTRIB = 1;

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private float[] buffer;
    private int     compsPerAttrib;
    private int     attribCount;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public float[] getBuffer( ) {
        return buffer;
    }

    public int getComponentsPerAttrib( ) {
        return compsPerAttrib;
    }

    public int getAttribCount( ) {
        return attribCount;
    }

    public int getAttribBaseOffset(int attribIndex) {
        return compsPerAttrib * attribIndex;
    }

    public void getAttrib(
        int     offsetOut, 
        float[] bufferOut, 
        int     attribIndex
    ) {
        System.arraycopy(
            buffer, 
            getAttribBaseOffset(attribIndex), 
            bufferOut, 
            offsetOut, 
            compsPerAttrib
        );
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public QAttribBuffer(QAttribBuffer toCopy) {
        buffer = new float[toCopy.buffer.length];
        System.arraycopy(
            toCopy, 
            0, 
            buffer, 
            0, 
            buffer.length
        );
        compsPerAttrib = toCopy.compsPerAttrib;
        attribCount    = toCopy.attribCount;
    }

    public QAttribBuffer(float[] bufferIn, int numCompsPerAttrib, int numAttribs) {
        if (numCompsPerAttrib < MIN_COMPS_PER_ATTRIB) {
            throw new QException(
                PointOfError.InvalidParameter, 
                "numAttribs must be greater than " +
                MIN_COMPS_PER_ATTRIB +
                ". Given was: " + numAttribs
            );
        }

        compsPerAttrib = numCompsPerAttrib;
        attribCount    = numAttribs;
        buffer         = new float[numCompsPerAttrib * numAttribs];
        System.arraycopy(bufferIn, 0, buffer, 0, buffer.length);
    }
}
