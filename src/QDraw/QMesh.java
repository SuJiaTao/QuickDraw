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

    public float[] getTriPos(int triIndex, int triVertIndex) {
        return getPos(
            triDataIndicies[
                (triIndex * MESH_TDI_NUM_CMPS) + 
                (triVertIndex * MESH_ATRB_NUM_CMPS) +
                MESH_ATRS_OFST_POS
            ]
        );
    }

    public float[] getTriUV(int triIndex, int triUVIndex) {
        return getUV(
            triDataIndicies[
                (triIndex * MESH_TDI_NUM_CMPS) + 
                (triUVIndex * MESH_ATRB_NUM_CMPS) +
                MESH_ATRS_OFST_UV
            ]
        );
    }

    public float[] getPos(int index) {
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

}
