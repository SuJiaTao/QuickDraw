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
    int[] indexBuffer;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public int[] getIndicies( ) {
        return indexBuffer;
    }

    public int getIndicieCount( ) {
        return indexBuffer.length;
    }

    public int getTriCount( ) {
        return getIndicieCount( ) / INDICIES_PER_TRI;
    }

    public int getTriBaseOffset(int triNum) {
        return triNum * INDICIES_PER_TRI;
    }

    public void getTriIndicies(
        int   offsetOut,
        int[] bufferOut,
        int   triNum
    ) {
        System.arraycopy(
            indexBuffer, 
            getTriBaseOffset(triNum), 
            bufferOut, offsetOut, 
            INDICIES_PER_TRI
        );
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

        indexBuffer = new int[numIndicies];
        System.arraycopy(
            inIndicies, 
            0, 
            indexBuffer, 
            0, 
            numIndicies
        );
    }
}
