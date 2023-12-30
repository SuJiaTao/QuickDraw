// Bailey JT Brown
// 2023
// QMesh.java

package QDraw;

import QDraw.QException.PointOfError;

public final class QMesh extends QEncoding {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final float[] UNIT_PLANE_VERTEX_DATA = {
        -1.0f, -1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f,
         1.0f,  1.0f, 0.0f,
         1.0f, -1.0f, 0.0f
    };
    private static final float[] UNIT_PLANE_UV_DATA = {
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 1.0f
    };
    private static final int[][] UNIT_PLANE_FACE_DATA = {
        { 0, 0, 1, 1, 2, 2, 3, 3 }
    };

    public static final QMesh unitPlane = new QMesh(
        UNIT_PLANE_VERTEX_DATA, 
        UNIT_PLANE_UV_DATA, 
        UNIT_PLANE_FACE_DATA
    );

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private float[] posData;
    private int     posCount;
    private float[] uvData;
    private int     uvCount;
    private int[]   triDataIndicies;
    private int     triCount;

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private int[] generateTriData(int[][] inFaceData) {
        
        int triCount = 0;
        for (int faceIndex = 0; faceIndex < inFaceData.length; faceIndex++) {
            
            int faceDataLength = inFaceData[faceIndex].length;

            if ((faceDataLength % MESH_ATRB_NUM_CMPS) != 0) {
                throw new QException(
                    PointOfError.MalformedData,
                    "inFaceData elements length must be multiple of " +
                    MESH_ATRB_NUM_CMPS + 
                    ". face index " + faceIndex + " had " + faceDataLength
                );
            }

            int attributeCount = faceDataLength / MESH_ATRB_NUM_CMPS;
            if (attributeCount < MESH_FACE_MIN_ATRBS) {
                throw new QException(
                    PointOfError.MalformedData,
                    "inFaceData elements must have at least " +
                    MESH_FACE_MIN_ATRBS + " attributes per face. " +
                    "face index " + faceIndex + " had " + attributeCount
                );
            }

            // check for bad index value
            for (int faceReadOffset = 0; faceReadOffset < faceDataLength; faceReadOffset += MESH_ATRB_NUM_CMPS) {

                int[] faceData = inFaceData[faceIndex];
                int vertexIndex = faceData[faceReadOffset + MESH_ATRS_OFST_POS];
                int uvIndex     = faceData[faceReadOffset + MESH_ATRS_OFST_UV];
                
                final int vertexDataMaxIndex = posData.length / MESH_POSN_NUM_CMPS;
                final int uvDataMaxIndex     = uvData.length / MESH_UV_NUM_CMPS;

                if (vertexIndex < 0 || vertexIndex > vertexDataMaxIndex) {
                    throw new QException(
                        PointOfError.MalformedData,
                        "faceData contained invalid vertex index. " +
                        "valid index range is 0 to " + vertexDataMaxIndex + "but found " +
                        "index value of " + vertexIndex
                    );
                }

                if (uvIndex < 0 || uvIndex > uvDataMaxIndex) {
                    throw new QException(
                        PointOfError.MalformedData,
                        "faceData contained invalid uv index. " +
                        "valid index range is 0 to " + uvDataMaxIndex + "but found " +
                        "index value of " + uvIndex
                    );
                }

            }

            // 1 triangle per face + 1 extra tri for each new vert over 3
            triCount += 1 + (attributeCount - MESH_VERTS_PER_TRI); 

        }

        int[] triData = new int[triCount * MESH_TDI_NUM_CMPS];

        int triDataWriteOffset = 0;
        for (int faceIndex = 0; faceIndex < inFaceData.length; faceIndex++) {
            
            int[] faceData     = inFaceData[faceIndex];
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

                // VERTEX/UV 0
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

                // VERTEX/UV 1
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

                // VERTEX/UV 2
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
        float[] inVerts,   // | x y z |
        float[] inUVs,     // | u v |
        int[][] inFaceData // | ( v1 uv1 v2 uv2 ...) |
        ) {

            if ((inVerts.length % MESH_POSN_NUM_CMPS) != 0) {
                throw new QException(
                    PointOfError.MalformedData,
                    "inVerts length must be multiple of " + MESH_POSN_NUM_CMPS + " . " +
                    "given was length " + inVerts.length
                );
            }

            if ((inUVs.length % MESH_UV_NUM_CMPS) != 0) {
                throw new QException(
                    PointOfError.MalformedData,
                    "inUVs length must be multiple of " + MESH_UV_NUM_CMPS + " . " +
                    "given was length " + inUVs.length
                );
            }

            posData = new float[inVerts.length];
            System.arraycopy(inVerts, 0, posData, 0, posData.length);

            uvData = new float[inUVs.length];
            System.arraycopy(inUVs, 0, uvData, 0, uvData.length);

            triDataIndicies = generateTriData(inFaceData);
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public float[] getPosData( ) {
        return posData;
    }
    
    public float[] getUVData( ) {
        return uvData;
    }

    public int[] getTriDataIndicies( ) {
        return triDataIndicies;
    }

    public float[] getVertex(int index) {
        return new float[] { 
            posData[index * MESH_POSN_NUM_CMPS + MESH_POSN_OFST_X],
            posData[index * MESH_POSN_NUM_CMPS + MESH_POSN_OFST_Y],
            posData[index * MESH_POSN_NUM_CMPS + MESH_POSN_OFST_Z]
        };
    }

    public float[] getUV(int index) {
        return new float[] { 
            uvData[index * MESH_UV_NUM_CMPS + MESH_UV_OFST_U],
            uvData[index * MESH_UV_NUM_CMPS + MESH_UV_OFST_V],
        };
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public QMesh(
        float[] inVerts,   // | x y z |
        float[] inUVs,     // | u v |
        int[][] inFaceData // | ( v1 uv1 v2 uv2 ...) |
        ) {
            initMesh(inVerts, inUVs, inFaceData);
    }

}
