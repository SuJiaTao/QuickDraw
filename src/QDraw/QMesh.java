// Bailey JT Brown
// 2023
// QMesh.java

package QDraw;

import QDraw.QException.PointOfError;

public final class QMesh {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public  static final int COMPONENTS_PER_VERTEX = 3;
    public  static final int COMPONENTS_PER_UV     = 2;

    private static final int COMPONENTS_PER_ATTRIBUTE     = 2;
    private static final int MIN_ATTRIBUTES_PER_FACE_DATA = 3;
    private static final int ATTRIBUTE_VERTEX_OFFSET      = 0;
    private static final int ATTRIBUTE_UV_OFFSET          = 1;

    private static final int VERTICIES_PER_TRI       = 3;    
    private static final int COMPONENTS_PER_TRI_DATA = VERTICIES_PER_TRI * COMPONENTS_PER_ATTRIBUTE;
    public static final int TRI_DATA_VERTEX_0_OFFSET = 0;
    public static final int TRI_DATA_VERTEX_1_OFFSET = 2;
    public static final int TRI_DATA_VERTEX_2_OFFSET = 4;
    public static final int TRI_DATA_UV_0_OFFSET     = 1;
    public static final int TRI_DATA_UV_1_OFFSET     = 3;
    public static final int TRI_DATA_UV_2_OFFSET     = 5;

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
    private float[] vertexData;  // PACKING | x y z | ...
    private float[] uvData;      // PACKING | u v | ...
    private int[]   triData;     // PACKING | vi1 uvi1 vi2 uvi2 vi3 uvi3 | ...

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private int[] generateTriData(int[][] inFaceData) {
        
        int triCount = 0;
        for (int faceIndex = 0; faceIndex < inFaceData.length; faceIndex++) {
            
            int faceDataLength = inFaceData[faceIndex].length;

            if ((faceDataLength % COMPONENTS_PER_ATTRIBUTE) != 0) {
                throw new QException(
                    PointOfError.MalformedData,
                    "inFaceData elements length must be multiple of " +
                    COMPONENTS_PER_ATTRIBUTE + 
                    ". face index " + faceIndex + " had " + faceDataLength
                );
            }

            int attributeCount = faceDataLength / COMPONENTS_PER_ATTRIBUTE;
            if (attributeCount < MIN_ATTRIBUTES_PER_FACE_DATA) {
                throw new QException(
                    PointOfError.MalformedData,
                    "inFaceData elements must have at least " +
                    MIN_ATTRIBUTES_PER_FACE_DATA + " attributes per face. " +
                    "face index " + faceIndex + " had " + attributeCount
                );
            }

            // check for bad index value
            for (int faceReadOffset = 0; faceReadOffset < faceDataLength; faceReadOffset += COMPONENTS_PER_ATTRIBUTE) {

                int[] faceData = inFaceData[faceIndex];
                int vertexIndex = faceData[faceReadOffset + ATTRIBUTE_VERTEX_OFFSET];
                int uvIndex     = faceData[faceReadOffset + ATTRIBUTE_UV_OFFSET];

                if (vertexIndex < 0 || vertexIndex > vertexData.length) {
                    throw new QException(
                        PointOfError.MalformedData,
                        "faceData contained invalid vertex index. " +
                        "valid index range is 0 to " + vertexData.length + "but found " +
                        "index value of " + vertexIndex
                    );
                }

                if (uvIndex < 0 || uvIndex > uvData.length) {
                    throw new QException(
                        PointOfError.MalformedData,
                        "faceData contained invalid uv index. " +
                        "valid index range is 0 to " + uvData.length + "but found " +
                        "index value of " + uvIndex
                    );
                }

            }

            // 1 triangle per face + 1 extra tri for each new vert over 3
            triCount += 1 + (attributeCount - VERTICIES_PER_TRI); 

        }

        int[] triData = new int[triCount * COMPONENTS_PER_TRI_DATA];

        int triDataWriteOffset = 0;
        for (int faceIndex = 0; faceIndex < inFaceData.length; faceIndex++) {
            
            int[] faceData     = inFaceData[faceIndex];
            int extraVertCount = Math.max(0, (faceData.length / 2) - 3);

            // 1 element of triData can always be built from a face
            triData[triDataWriteOffset + TRI_DATA_VERTEX_0_OFFSET] =
                faceData[COMPONENTS_PER_ATTRIBUTE * 0 + ATTRIBUTE_VERTEX_OFFSET];
            triData[triDataWriteOffset + TRI_DATA_UV_0_OFFSET] =
                faceData[COMPONENTS_PER_ATTRIBUTE * 0 + ATTRIBUTE_UV_OFFSET];

            triData[triDataWriteOffset + TRI_DATA_VERTEX_1_OFFSET] =
                faceData[COMPONENTS_PER_ATTRIBUTE * 1 + ATTRIBUTE_VERTEX_OFFSET];
            triData[triDataWriteOffset + TRI_DATA_UV_1_OFFSET] =
                faceData[COMPONENTS_PER_ATTRIBUTE * 1 + ATTRIBUTE_UV_OFFSET];

            triData[triDataWriteOffset + TRI_DATA_VERTEX_2_OFFSET] =
                faceData[COMPONENTS_PER_ATTRIBUTE * 2 + ATTRIBUTE_VERTEX_OFFSET];
            triData[triDataWriteOffset + TRI_DATA_UV_2_OFFSET] =
                faceData[COMPONENTS_PER_ATTRIBUTE * 2 + ATTRIBUTE_UV_OFFSET];
            
            triDataWriteOffset += COMPONENTS_PER_TRI_DATA;

            // build extra triangles, one for each extra vertex
            // tesselation is in triangle fan configuration
            for (int nthExtra = 0; nthExtra < extraVertCount; nthExtra++) {

                // VERTEX/UV 0
                // same as first vertex in face
                triData[
                    triDataWriteOffset +
                    TRI_DATA_VERTEX_0_OFFSET
                ] =
                faceData[
                    COMPONENTS_PER_ATTRIBUTE * 0 +
                    ATTRIBUTE_VERTEX_OFFSET
                ];
                triData[
                    triDataWriteOffset +
                    TRI_DATA_UV_0_OFFSET
                ] =
                faceData[
                    COMPONENTS_PER_ATTRIBUTE * 0 +
                    ATTRIBUTE_UV_OFFSET
                ];

                // VERTEX/UV 1
                // (2 + n)th vertex (zero indexed)
                // where n is the current extra vertex index starting from 0
                triData[
                    triDataWriteOffset +
                    TRI_DATA_VERTEX_1_OFFSET
                ] =
                faceData[
                    COMPONENTS_PER_ATTRIBUTE * 2 +
                    COMPONENTS_PER_ATTRIBUTE * nthExtra +
                    ATTRIBUTE_VERTEX_OFFSET
                ];
                triData[
                    triDataWriteOffset +
                    TRI_DATA_UV_1_OFFSET
                ] =
                faceData[
                    COMPONENTS_PER_ATTRIBUTE * 2 +
                    COMPONENTS_PER_ATTRIBUTE * nthExtra +
                    ATTRIBUTE_UV_OFFSET
                ];

                // VERTEX/UV 2
                // (2 + n + 1)th vertex (zero indexed)
                // where n is the current extra vertex index starting from 0
                triData[
                    triDataWriteOffset +
                    TRI_DATA_VERTEX_2_OFFSET
                ] =
                faceData[
                    COMPONENTS_PER_ATTRIBUTE * 3 +
                    COMPONENTS_PER_ATTRIBUTE * nthExtra +
                    ATTRIBUTE_VERTEX_OFFSET
                ];
                triData[
                    triDataWriteOffset +
                    TRI_DATA_UV_2_OFFSET
                ] =
                faceData[
                    COMPONENTS_PER_ATTRIBUTE * 3 +
                    COMPONENTS_PER_ATTRIBUTE * nthExtra +
                    ATTRIBUTE_UV_OFFSET
                ];

                triDataWriteOffset += COMPONENTS_PER_TRI_DATA;

            } // END BUILD EXTRA TRIANGLES

        } // END LOOP FACES

        return triData;
    }

    private void initMesh(
        float[] inVerts,   // | x y z |
        float[] inUVs,     // | u v |
        int[][] inFaceData // | ( v1 uv1 v2 uv2 ...) |
        ) {

            if ((inVerts.length % COMPONENTS_PER_VERTEX) != 0) {
                throw new QException(
                    PointOfError.MalformedData,
                    "inVerts length must be multiple of " + COMPONENTS_PER_VERTEX + " . " +
                    "given was length " + inVerts.length
                );
            }

            if ((inUVs.length % COMPONENTS_PER_UV) != 0) {
                throw new QException(
                    PointOfError.MalformedData,
                    "inUVs length must be multiple of " + COMPONENTS_PER_UV + " . " +
                    "given was length " + inUVs.length
                );
            }

            vertexData = new float[inVerts.length];
            System.arraycopy(inVerts, 0, vertexData, 0, vertexData.length);

            uvData = new float[inUVs.length];
            System.arraycopy(inUVs, 0, uvData, 0, uvData.length);

            triData = generateTriData(inFaceData);
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public float[] getVertexData( ) {
        return vertexData;
    }
    
    public float[] getUVData( ) {
        return uvData;
    }

    public int[] getTriData( ) {
        return triData;
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
