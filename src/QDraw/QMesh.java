// Bailey JT Brown
// 2023
// QMesh.java

package QDraw;

import QDraw.QException.PointOfError;

public final class QMesh {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final int COMPONENTS_PER_VERTEX     = 3;
    private static final int COMPONENTS_PER_UV         = 2;
    private static final int VERTICIES_PER_TRI         = 3;
    private static final int COMPONENTS_PER_ATTRIBUTE  = 2;
    private static final int COMPONENTS_PER_TRI_DATA   = 
        VERTICIES_PER_TRI * COMPONENTS_PER_ATTRIBUTE;
    private static final int TRI_DATA_VERTEX_OFFSET    = 0;
    private static final int TRI_DATA_UV_OFFSET        = 1;
    private static final int FACE_DATA_MIN_ATTRIBUTES  = 3;
    private static final int COMPONENTS_PER_BAKED_DATA =
        (COMPONENTS_PER_VERTEX + COMPONENTS_PER_UV) * VERTICIES_PER_TRI;

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
        { 0, 0, 1, 1, 3, 3},
        { 1, 1, 2, 2, 3, 3}
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
    private float[] bakedData;   // PACKING | xyzuv1 xyzuv2 xyzuv3 | ...

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

            int attributeCount = faceDataLength / 2;
            if (attributeCount < FACE_DATA_MIN_ATTRIBUTES) {
                throw new QException(
                    PointOfError.MalformedData,
                    "inFaceData elements must have at least " +
                    FACE_DATA_MIN_ATTRIBUTES + " attributes per face. " +
                    "face index " + faceIndex + " had " + attributeCount
                );
            }

            // 1 triangle per face + extra for each new vert
            triCount += 1 + (3 - attributeCount); 

        }

        int[] triData = new int[triCount * COMPONENTS_PER_TRI_DATA];

        int triIndex = 0;
        for (int faceIndex = 0; faceIndex < inFaceData.length; faceIndex++) {
            
            int[] faceData          = inFaceData[faceIndex];
            final int extraTriCount = Math.max(0, (faceData.length / 2) - 3);

            // 1 element of triData can always be built from a face
            System.arraycopy(
                faceData, 
                0, 
                triData, 
                triIndex, 
                COMPONENTS_PER_TRI_DATA
            );
            
            triIndex += COMPONENTS_PER_TRI_DATA;

            // the next elements are tesselated as a triangle fan
            for (int extraTriIndex = 0; extraTriIndex < extraTriCount; extraTriIndex++) {

                // vertex0 -> first vertex
                triData[triIndex + 0] = 
                    faceData[COMPONENTS_PER_TRI_DATA 
                    + TRI_DATA_VERTEX_OFFSET];
                triData[triIndex + 1] = 
                    faceData[COMPONENTS_PER_TRI_DATA 
                    + TRI_DATA_UV_OFFSET];

                // vertex1 -> last vertex in original triangle
                triData[triIndex + 2] = 
                    faceData[COMPONENTS_PER_TRI_DATA 
                    + COMPONENTS_PER_ATTRIBUTE * 1
                    + extraTriIndex 
                    + TRI_DATA_VERTEX_OFFSET];
                triData[triIndex + 3] = 
                    faceData[COMPONENTS_PER_TRI_DATA 
                    + COMPONENTS_PER_ATTRIBUTE * 1
                    + extraTriIndex 
                    + TRI_DATA_UV_OFFSET];

                // vertex2 -> 1 + last original vertex + current triangle offset
                triData[triIndex + 4] = 
                    faceData[COMPONENTS_PER_TRI_DATA 
                    + COMPONENTS_PER_ATTRIBUTE * 2
                    + extraTriIndex 
                    + TRI_DATA_VERTEX_OFFSET];
                triData[triIndex + 5] = 
                    faceData[COMPONENTS_PER_TRI_DATA 
                    + COMPONENTS_PER_ATTRIBUTE * 2
                    + extraTriIndex 
                    + TRI_DATA_UV_OFFSET];

                triIndex += COMPONENTS_PER_TRI_DATA;
            }

        }

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

    private void bakeMesh( ) {

        if ((triData.length % COMPONENTS_PER_TRI_DATA) != 0) {
            throw new QException(
                PointOfError.BadState,
                "could not bake mesh, triData must be generated with " +
                "length multiple of " + COMPONENTS_PER_TRI_DATA + " . triData " +
                "was length " + triData.length
            );
        }

        final int triangleCount = triData.length / COMPONENTS_PER_TRI_DATA;
        bakedData = new float[triangleCount * COMPONENTS_PER_BAKED_DATA];

        int bakedDataOffset = 0;
        for (int triDatOffset = 0; 
             triDatOffset < triData.length; 
             triDatOffset += COMPONENTS_PER_TRI_DATA
            ) {
            
            int vertIndex1 = triData[triDatOffset + 0];
            int uvIndex1   = triData[triDatOffset + 1];
            int vertIndex2 = triData[triDatOffset + 2];
            int uvIndex2   = triData[triDatOffset + 3];
            int vertIndex3 = triData[triDatOffset + 4]; 
            int uvIndex3   = triData[triDatOffset + 5]; 

            // xyzuv1
            bakedData[bakedDataOffset + 0] = vertexData[(vertIndex1 * COMPONENTS_PER_VERTEX) + 0];
            bakedData[bakedDataOffset + 1] = vertexData[(vertIndex1 * COMPONENTS_PER_VERTEX) + 1];
            bakedData[bakedDataOffset + 2] = vertexData[(vertIndex1 * COMPONENTS_PER_VERTEX) + 2];
            bakedData[bakedDataOffset + 3] = uvData[(uvIndex1 * COMPONENTS_PER_UV) + 0];
            bakedData[bakedDataOffset + 4] = uvData[(uvIndex1 * COMPONENTS_PER_UV) + 1];

            // xyzuv 2
            bakedData[bakedDataOffset + 5] = vertexData[(vertIndex2 * COMPONENTS_PER_VERTEX) + 0];
            bakedData[bakedDataOffset + 6] = vertexData[(vertIndex2 * COMPONENTS_PER_VERTEX) + 1];
            bakedData[bakedDataOffset + 7] = vertexData[(vertIndex2 * COMPONENTS_PER_VERTEX) + 2];
            bakedData[bakedDataOffset + 8] = uvData[(uvIndex2 * COMPONENTS_PER_UV) + 0];
            bakedData[bakedDataOffset + 9] = uvData[(uvIndex2 * COMPONENTS_PER_UV) + 1];

            // xyzuv 3
            bakedData[bakedDataOffset + 10] = vertexData[(vertIndex3 * COMPONENTS_PER_VERTEX) + 0];
            bakedData[bakedDataOffset + 11] = vertexData[(vertIndex3 * COMPONENTS_PER_VERTEX) + 1];
            bakedData[bakedDataOffset + 12] = vertexData[(vertIndex3 * COMPONENTS_PER_VERTEX) + 2];
            bakedData[bakedDataOffset + 13] = uvData[(uvIndex3 * COMPONENTS_PER_UV) + 0];
            bakedData[bakedDataOffset + 14] = uvData[(uvIndex3 * COMPONENTS_PER_UV) + 1];
            
            bakedDataOffset += COMPONENTS_PER_BAKED_DATA;
        }

    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public final float[] getVertexData( ) {
        return vertexData;
    }
    
    public final float[] getUVData( ) {
        return uvData;
    }

    public final int[] getTriData( ) {
        return triData;
    }

    public final float[] getBakedData( ) {
        return bakedData;
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public QMesh(
        float[] inVerts,   // | x y z |
        float[] inUVs,     // | u v |
        int[][] inFaceData // | ( v1 uv1 v2 uv2 ...) |
        ) {
            initMesh(inVerts, inUVs, inFaceData);
            bakeMesh( );
    }

}
