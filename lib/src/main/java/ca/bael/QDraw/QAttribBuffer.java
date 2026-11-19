// Bailey JT Brown
// 2024-2026
// QAttribBuffer.java

package ca.bael.QDraw;

import ca.bael.QDraw.QException.PointOfError;

public final class QAttribBuffer {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final int MIN_COMPS_PER_ATTRIB = 1;

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private final float[] buffer;
    private final int     compsPerAttrib;
    private final int     attribCount;

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

    public void set(int attribIndex, float... values) {
        System.arraycopy(values, 0, buffer, getAttribBaseOffset(attribIndex), compsPerAttrib);
    }

    public static QAttribBuffer wrap(float[] bufferIn, int numCompsPerAttrib) {
        return new QAttribBuffer(bufferIn, numCompsPerAttrib);
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    private QAttribBuffer(float[] bufferIn, int numCompsPerAttrib) {
        if (numCompsPerAttrib < MIN_COMPS_PER_ATTRIB) {
            throw new QException(
                PointOfError.InvalidParameter,
                "numCompsPerAttrib must be at least " +
                MIN_COMPS_PER_ATTRIB +
                ". Given was: " + numCompsPerAttrib
            );
        }
        if ((bufferIn.length % numCompsPerAttrib) != 0) {
            throw new QException(
                PointOfError.InvalidParameter,
                "buffer length must be multiple of " + numCompsPerAttrib +
                ". Given was: " + bufferIn.length
            );
        }

        buffer         = bufferIn;
        compsPerAttrib = numCompsPerAttrib;
        attribCount    = bufferIn.length / numCompsPerAttrib;
    }

    public QAttribBuffer(int numCompsPerAttrib, int numAttribs) {
        this(new float[numCompsPerAttrib * numAttribs], numCompsPerAttrib);
    }

    public QAttribBuffer(QAttribBuffer toCopy) {
        this(toCopy.buffer.clone( ), toCopy.compsPerAttrib);
    }

    public QAttribBuffer(float[] bufferIn, int numCompsPerAttrib, int numAttribs) {
        this(numCompsPerAttrib, numAttribs);
        System.arraycopy(bufferIn, 0, buffer, 0, buffer.length);
    }
}
