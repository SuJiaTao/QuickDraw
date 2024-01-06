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
    private QAttribBuffer attribBuffer;
    private int[]         attribIndexBuffer;

    /////////////////////////////////////////////////////////////////
    // PUBLIC CLASSES
    public class TriAttribs {
        /////////////////////////////////////////////////////////////////
        // PRIVATE MEMBERS
        private QAttribBuffer sourceBuffer;
        private float[] v0_dat;
        private float[] v1_dat;
        private float[] v2_dat;

        /////////////////////////////////////////////////////////////////
        // PUBLIC METHODS
        public QAttribBuffer getSourceBuffer( ) { 
            return sourceBuffer; 
        }

        public void interpolateAttribute(
            float   weight0,
            float   weight1,
            float   weight2,
            int     offsetOut,
            float[] bufferOut
        ) {
            for (int comp = 0; comp < sourceBuffer.getComponentsPerAttrib(); comp++) {
                bufferOut[offsetOut + comp] = 
                    weight0 * v0_dat[comp] + 
                    weight1 * v1_dat[comp] + 
                    weight2 * v2_dat[comp];
            }
        }

        public void getAttribute(
            int     vertNum,
            int     offsetOut,
            float[] bufferOut
        ) {
            float[] src = null;
            switch (vertNum) {
                case 0:
                    src = v0_dat;
                    break;

                case 1:
                    src = v1_dat;
                    break;
                
                case 2:
                    src = v2_dat;
            
                default:
                    throw new QException(
                        PointOfError.BadState, 
                        "Cannot get attribute for vertex " + vertNum + "." + 
                        "Valid vertNums: 0, 1, 2");
            }
            System.arraycopy(
                src, 
                0, 
                bufferOut, 
                offsetOut, 
                src.length
            );
        }

        /////////////////////////////////////////////////////////////////
        // CONSTRUCTOR
        public TriAttribs(QAttribIndexer indexer, int triNum) {
            sourceBuffer = indexer.attribBuffer;
            v0_dat = new float[sourceBuffer.getComponentsPerAttrib()];
            v1_dat = new float[sourceBuffer.getComponentsPerAttrib()];
            v2_dat = new float[sourceBuffer.getComponentsPerAttrib()];
            indexer.index(triNum, 0, 0, v0_dat);
            indexer.index(triNum, 1, 0, v1_dat);
            indexer.index(triNum, 2, 0, v2_dat);
        }
    }

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

    public void setAttribBuffer(QAttribBuffer aBuffer) {
        attribBuffer = aBuffer;
    }

    public void index(
        int           triNum,
        int           vertNum,
        int           offsetOut,
        float[]       bufferOut
    ) {
        int[] triIndicies = new int[INDICIES_PER_TRI];
        getTriIndicies(0, triIndicies, triNum);
        attribBuffer.getAttrib(offsetOut, bufferOut, triIndicies[vertNum]);
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
