// Bailey JT Brown
// 2023-2024
// QMesh.java

package QDraw;

import java.io.*;
import java.util.*;
import QDraw.QException.PointOfError;

public final class QMesh extends QEncoding {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final int OBJLOAD_POSN_BUFFER_INITIAL_SIZE = 
        MESH_POSN_NUM_CMPS * MESH_VERTS_PER_TRI * 20;
    private static final int OBJLOAD_UV_BUFFER_INITIAL_SIZE = 
        MESH_UV_NUM_CMPS * MESH_VERTS_PER_TRI * 20;
    private static final int OBJLOAD_FACEDAT_BUFFER_INITIAL_SIZE = 20;
    
    private static final float[] UNIT_PLANE_POSN_DATA = {
        -1.0f, -1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f,
         1.0f,  1.0f, 0.0f,
         1.0f, -1.0f, 0.0f
    };
    private static final float[] UNIT_PLANE_UV_DATA = {
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    };
    private static final int[][] UNIT_PLANE_FACE_DATA = {
        { 0, 0, 1, 1, 2, 2, 3, 3 }
    };

    public static QMesh UnitPlane() {
        return new QMesh(
            UNIT_PLANE_POSN_DATA, 
            UNIT_PLANE_UV_DATA, 
            UNIT_PLANE_FACE_DATA
        ); 
    }

    private static final float[] RIGHT_TRIANGLE_POSN_DATA = {
        -1.0f, -1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f,
         1.0f,  1.0f, 0.0f,
    };
    private static final float[] RIGHT_TRI_UV_DATA = {
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
    };
    private static final int[][] RIGHT_TRI_FACE_DATA = {
        { 0, 0, 1, 1, 2, 2 }
    };

    public static QMesh RightTriangle() {
        return new QMesh(
            RIGHT_TRIANGLE_POSN_DATA,
            RIGHT_TRI_UV_DATA,
            RIGHT_TRI_FACE_DATA
        );
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE CLASSES
    private class FloatReadBuffer {
        private float[] buffer;
        private int     head;

        private void grow( ) {
            float[] newBuffer = new float[buffer.length * 2];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
        }

        public FloatReadBuffer(int capacity) {
            buffer = new float[capacity];
            head   = 0;
        }

        public void add(float val) {
            buffer[head] = val;
            head++;
            if (head >= buffer.length) {
                grow( );
            }
        }

        public float[] toArray( ) {
            float[] retArray = new float[head];
            System.arraycopy(buffer, 0, retArray, 0, head);
            return retArray;
        }
    }

    private class IndicieReadBuffer {
        private int[][] buffer;
        private int     head;

        private void grow( ) {
            int[][] newBuffer = new int[buffer.length * 2][];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
        }

        public IndicieReadBuffer(int capacity) {
            buffer = new int[capacity][];
            head   = 0;
        }

        public void add(int[] val) {
            buffer[head] = val;
            head++;
            if (head >= buffer.length) {
                grow( );
            }
        }

        public int[][] toArray( ) {
            int[][] retArray = new int[head][];
            System.arraycopy(buffer, 0, retArray, 0, head);
            return retArray;
        }
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    // (refer to QEncoding.java for encoding details)
    private float[] posData;
    private int     posCount;
    private float[] uvData;
    private int     uvCount;
    private int[]   triDataIndicies;
    private int     triCount;

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private int[] generateTriData(int[][] inFaceIndicies) {
        
        for (int faceIndex = 0; faceIndex < inFaceIndicies.length; faceIndex++) {
            
            int faceDataLength = inFaceIndicies[faceIndex].length;

            if ((faceDataLength % MESH_ATRB_NUM_CMPS) != 0) {
                throw new QException(
                    PointOfError.MalformedData,
                    "inFaceIndicies elements length must be multiple of " +
                    MESH_ATRB_NUM_CMPS + 
                    ". face index " + faceIndex + " had " + faceDataLength
                );
            }

            int attributeCount = faceDataLength / MESH_ATRB_NUM_CMPS;
            if (attributeCount < MESH_FACE_MIN_ATRBS) {
                throw new QException(
                    PointOfError.MalformedData,
                    "inFaceIndicies elements must have at least " +
                    MESH_FACE_MIN_ATRBS + " attributes per face. " +
                    "face index " + faceIndex + " had " + attributeCount
                );
            }

            // check for bad index value
            for (int faceReadOffset = 0; faceReadOffset < faceDataLength; faceReadOffset += MESH_ATRB_NUM_CMPS) {

                int[] faceData = inFaceIndicies[faceIndex];
                int vertexIndex = faceData[faceReadOffset + MESH_ATRS_OFST_POS];
                int uvIndex     = faceData[faceReadOffset + MESH_ATRS_OFST_UV];

                if (vertexIndex < 0 || vertexIndex > posCount) {
                    throw new QException(
                        PointOfError.MalformedData,
                        "inFaceIndicies contained invalid vertex index. " +
                        "valid index range is 0 to " + posCount + "but found " +
                        "index value of " + vertexIndex
                    );
                }

                if (uvIndex < 0 || uvIndex > uvCount) {
                    throw new QException(
                        PointOfError.MalformedData,
                        "inFaceIndicies contained invalid uv index. " +
                        "valid index range is 0 to " + uvCount + "but found " +
                        "index value of " + uvIndex
                    );
                }

            }

            // 1 triangle per face + 1 extra tri for each new vert over 3
            triCount += 1 + (attributeCount - MESH_VERTS_PER_TRI); 

        }

        int[] triData = new int[triCount * MESH_TDI_NUM_CMPS];

        int triDataWriteOffset = 0;
        for (int faceIndex = 0; faceIndex < inFaceIndicies.length; faceIndex++) {
            
            int[] faceData     = inFaceIndicies[faceIndex];
            int extraVertCount = Math.max(0, (faceData.length / 2) - 3);

            // 1 element of triData can always be built from a face
            triData[triDataWriteOffset + MESH_TDI_OFST_POS0] =
                faceData[MESH_ATRB_NUM_CMPS * 0 + MESH_ATRS_OFST_POS];
            triData[triDataWriteOffset + MESH_TDI_OFST_UV0] =
                faceData[MESH_ATRB_NUM_CMPS * 0 + MESH_ATRS_OFST_UV];

            triData[triDataWriteOffset + MESH_TDI_OFST_POS1] =
                faceData[MESH_ATRB_NUM_CMPS * 1 + MESH_ATRS_OFST_POS];
            triData[triDataWriteOffset + MESH_TDI_OFST_UV1] =
                faceData[MESH_ATRB_NUM_CMPS * 1 + MESH_ATRS_OFST_UV];

            triData[triDataWriteOffset + MESH_TDI_OFST_POS2] =
                faceData[MESH_ATRB_NUM_CMPS * 2 + MESH_ATRS_OFST_POS];
            triData[triDataWriteOffset + MESH_TDI_OFST_UV2] =
                faceData[MESH_ATRB_NUM_CMPS * 2 + MESH_ATRS_OFST_UV];
            
            triDataWriteOffset += MESH_TDI_NUM_CMPS;

            // build extra triangles, one for each extra vertex
            // tesselation is in triangle fan configuration
            for (int nthExtra = 0; nthExtra < extraVertCount; nthExtra++) {

                // POSN/UV 0
                // same as first vertex in face
                triData[
                    triDataWriteOffset +
                    MESH_TDI_OFST_POS0
                ] =
                faceData[
                    MESH_ATRB_NUM_CMPS * 0 +
                    MESH_ATRS_OFST_POS
                ];
                triData[
                    triDataWriteOffset +
                    MESH_TDI_OFST_UV0
                ] =
                faceData[
                    MESH_ATRB_NUM_CMPS * 0 +
                    MESH_ATRS_OFST_UV
                ];

                // POSN/UV 1
                // (2 + n)th vertex (zero indexed)
                // where n is the current extra vertex index starting from 0
                triData[
                    triDataWriteOffset +
                    MESH_TDI_OFST_POS1
                ] =
                faceData[
                    MESH_ATRB_NUM_CMPS * 2 +
                    MESH_ATRB_NUM_CMPS * nthExtra +
                    MESH_ATRS_OFST_POS
                ];
                triData[
                    triDataWriteOffset +
                    MESH_TDI_OFST_UV1
                ] =
                faceData[
                    MESH_ATRB_NUM_CMPS * 2 +
                    MESH_ATRB_NUM_CMPS * nthExtra +
                    MESH_ATRS_OFST_UV
                ];

                // POSN/UV 2
                // (2 + n + 1)th vertex (zero indexed)
                // where n is the current extra vertex index starting from 0
                triData[
                    triDataWriteOffset +
                    MESH_TDI_OFST_POS2
                ] =
                faceData[
                    MESH_ATRB_NUM_CMPS * 3 +
                    MESH_ATRB_NUM_CMPS * nthExtra +
                    MESH_ATRS_OFST_POS
                ];
                triData[
                    triDataWriteOffset +
                    MESH_TDI_OFST_UV2
                ] =
                faceData[
                    MESH_ATRB_NUM_CMPS * 3 +
                    MESH_ATRB_NUM_CMPS * nthExtra +
                    MESH_ATRS_OFST_UV
                ];

                triDataWriteOffset += MESH_TDI_NUM_CMPS;

            } // END BUILD EXTRA TRIANGLES

        } // END LOOP FACES

        return triData;
    }

    private void initMesh(
        float[] inPosns,
        float[] inUVs,
        int[][] inFaceIndicies
        ) {

        if ((inPosns.length % MESH_POSN_NUM_CMPS) != 0) {
            throw new QException(
                PointOfError.MalformedData,
                "inPosns length must be multiple of " + MESH_POSN_NUM_CMPS + " . " +
                "given was length " + inPosns.length
            );
        }

        if ((inUVs.length % MESH_UV_NUM_CMPS) != 0) {
            throw new QException(
                PointOfError.MalformedData,
                "inUVs length must be multiple of " + MESH_UV_NUM_CMPS + " . " +
                "given was length " + inUVs.length
            );
        }

        posData  = new float[inPosns.length];
        System.arraycopy(inPosns, 0, posData, 0, posData.length);
        posCount = posData.length / MESH_POSN_NUM_CMPS;

        uvData  = new float[inUVs.length];
        System.arraycopy(inUVs, 0, uvData, 0, uvData.length);
        uvCount = uvData.length / MESH_UV_NUM_CMPS;

        triDataIndicies = generateTriData(inFaceIndicies);

    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public float[] getPosData( ) {
        return posData;
    }

    public int getPosCount( ) {
        return posCount;
    }
    
    public float[] getUVData( ) {
        return uvData;
    }

    public int getUVCount( ) {
        return uvCount;
    }

    public int[] getTriDataIndicies( ) {
        return triDataIndicies;
    }

    public int getTriCount( ) {
        return triCount;
    }

    public int getTriPosIndex(int triIndex, int triVertIndex) {
        return triDataIndicies[(triIndex * MESH_TDI_NUM_CMPS) + 
               (triVertIndex * MESH_ATRB_NUM_CMPS) +
               MESH_ATRS_OFST_POS];
    }

    public float[] getTriPos(int triIndex, int triVertIndex) {
        return getPos(getTriPosIndex(triIndex, triVertIndex));
    }

    public int getTriUVIndex(int triIndex, int triUVIndex) {
        return triDataIndicies[(triIndex * MESH_TDI_NUM_CMPS) + 
                (triUVIndex * MESH_ATRB_NUM_CMPS) +
                MESH_ATRS_OFST_UV];
    }

    public float[] getTriUV(int triIndex, int triUVIndex) {
        return getUV(getTriUVIndex(triIndex, triUVIndex));
    }

    public int getPosOffset(int index) {
        return index * MESH_POSN_NUM_CMPS;
    }

    public float[] getPos(int index) {
        return new float[] { 
            posData[getPosOffset(index) + MESH_POSN_OFST_X],
            posData[getPosOffset(index) + MESH_POSN_OFST_Y],
            posData[getPosOffset(index) + MESH_POSN_OFST_Z]
        };
    }

    public void setPos(int index, float x, float y, float z) {
        posData[getPosOffset(index) + MESH_POSN_OFST_X] = x;
        posData[getPosOffset(index) + MESH_POSN_OFST_Y] = y;
        posData[getPosOffset(index) + MESH_POSN_OFST_Z] = z;
    }

    public int getUVOffset(int index) {
        return index * MESH_UV_NUM_CMPS;
    }

    public float[] getUV(int index) {
        return new float[] { 
            uvData[getUVOffset(index) + MESH_UV_OFST_U],
            uvData[getUVOffset(index) + MESH_UV_OFST_V],
        };
    }

    public void setUV(int index, float u, float v) {
        uvData[getUVOffset(index) + MESH_UV_OFST_U] = u;
        uvData[getUVOffset(index) + MESH_UV_OFST_V] = v;
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // inPosn is:
    //   | <x y z> ... | : float[]
    //   array of contigugous 3 space vertex positions
    // inUVs is:
    //   | <u v> ... | : float[]
    //   array of contiguous 2 space UV coordinates
    // inFaceData is:
    //   | | <p0 uv0> <p1 uv1> <p2 uv2> ... | ... | : int[][]
    //   array of faceData attributes
    //   faceData attributes is an array of at least 3 attributes
    //   attributes are a pair indicies into inPosn and inUVs
    public QMesh(
        float[] inPosns,
        float[] inUVs,
        int[][] inFaceIndicies
        ) {
            initMesh(inPosns, inUVs, inFaceIndicies);
    }

    public QMesh(QMesh toCopy) {
        posCount = toCopy.posCount;
        uvCount  = toCopy.uvCount;
        triCount = toCopy.triCount;

        posData = new float[toCopy.posData.length];
        System.arraycopy(
            toCopy.posData, 
            0, 
            posData, 
            0, 
            posData.length
        );
        uvData  = new float[toCopy.uvData.length];
        System.arraycopy(
            toCopy.uvData, 
            0, 
            uvData, 
            0, 
            uvData.length
        );
        triDataIndicies = new int[toCopy.triDataIndicies.length];
        System.arraycopy(
            toCopy.triDataIndicies, 
            0, 
            triDataIndicies, 
            0, 
            triDataIndicies.length
        );
    }

    public QMesh(String objFilePath) {
        if (!objFilePath.endsWith(".obj")) {
            throw new QException(
                PointOfError.InvalidParameter, 
                "objFilePath must contain .obj file, file was instead " 
                + objFilePath
            );
        }

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(objFilePath));
        } catch (Exception e) {
            throw new QException(
                PointOfError.InvalidParameter, 
                "unable to read file " + objFilePath
            );
        }

        FloatReadBuffer posBuffer       = new FloatReadBuffer(OBJLOAD_POSN_BUFFER_INITIAL_SIZE);
        FloatReadBuffer uvBuffer        = new FloatReadBuffer(OBJLOAD_UV_BUFFER_INITIAL_SIZE);
        IndicieReadBuffer faceDatBuffer = new IndicieReadBuffer(OBJLOAD_FACEDAT_BUFFER_INITIAL_SIZE);
        while (scanner.hasNextLine( )) {
            String line = scanner.nextLine( );

            // PARSE POSITION
            if (line.substring(0, 2).equals("v ")) {
                String[] posVals = line.substring(2).split(" ");
                posBuffer.add(Float.parseFloat(posVals[0]));
                posBuffer.add(Float.parseFloat(posVals[1]));
                posBuffer.add(Float.parseFloat(posVals[2]));
                continue;
            }

            // PARSE UV
            if (line.substring(0, 3).equals("vt ")) {
                String[] uvVals = line.substring(3).split(" ");
                uvBuffer.add(Float.parseFloat(uvVals[0]));
                uvBuffer.add(Float.parseFloat(uvVals[1]));
                continue;
            }

            // PARSE FACEDATA
            // refer to
            // https://stackoverflow.com/questions/960431/how-can-i-convert-listinteger-to-int-in-java
            if (line.substring(0, 2).equals("f ")) {
                String[] faceVals = line.substring(2).split(" ");
                ArrayList<Integer> iBuffer = new ArrayList<>(MESH_TDI_NUM_CMPS);
                for (String val : faceVals) {
                    String[] indicies = val.split("/");
                    // NOTE: obj files are NOT 0 indexed
                    iBuffer.add(Integer.parseInt(indicies[0]) - 1);
                    iBuffer.add(Integer.parseInt(indicies[1]) - 1);
                }
                faceDatBuffer.add(iBuffer.stream().mapToInt(i -> i).toArray());
                continue;
            }
        }

        scanner.close();

        initMesh(
            posBuffer.toArray(), 
            uvBuffer.toArray(), 
            faceDatBuffer.toArray()
        );
    }

}
