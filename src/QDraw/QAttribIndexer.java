// Bailey JT Brown
// 2024
// QAttribIndexer.java

package QDraw;

import QDraw.QException.PointOfError;

public final class QAttribIndexer {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final int INDICIES_PER_TRI = 3;

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    int[] attribIndexBuffer;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public int[] getIndicies( ) {
        return attribIndexBuffer;
    }

    public int getIndicieCount( ) {
        return attribIndexBuffer.length;
    }

    public int getTriCount( ) {
        return getIndicieCount( ) / INDICIES_PER_TRI;
    }

    public int getTriBaseOffset(int triNum) {
        return triNum * INDICIES_PER_TRI;
    }

    public int getIndicie(int vertNum) {
        return attribIndexBuffer[vertNum];
    }

    public void getTriIndicies(
        int   offsetOut,
        int[] bufferOut,
        int   triNum
    ) {
        System.arraycopy(
            attribIndexBuffer, 
            getTriBaseOffset(triNum), 
            bufferOut, offsetOut, 
            INDICIES_PER_TRI
        );
    }

    public void indexAttribBuffer(
        QAttribBuffer buffer,
        int           triNum,
        int           vertNum,
        int           offsetOut,
        float[]       bufferOut
    ) {
        int[] triIndicies = new int[INDICIES_PER_TRI];
        getTriIndicies(0, triIndicies, triNum);
        buffer.getAttrib(offsetOut, bufferOut, triIndicies[vertNum]);
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public QAttribIndexer(int[] inIndicies, int numIndicies) {
        if ((numIndicies % INDICIES_PER_TRI) != 0) {
            throw new QException(
                PointOfError.InvalidData, 
                "numIndicies must be multiple of " + 
                INDICIES_PER_TRI + 
                " in order to properly render mesh. Given was " +
                numIndicies
            );
        }

        attribIndexBuffer = new int[numIndicies];
        System.arraycopy(
            inIndicies, 
            0, 
            attribIndexBuffer, 
            0, 
            numIndicies
        );
    }
}
