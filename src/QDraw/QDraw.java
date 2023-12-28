// Bailey JT Brown
// 2023
// QDraw.java

package QDraw;

import QDraw.*;
import QDraw.QException.PointOfError;

public final class QDraw {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final int   MATRIX_STACK_SIZE   = 0x40;
    public static final float MIN_CLIP_NEAR       = -0.01f;
    public static final float DEFAULT_CLIP_NEAR   = MIN_CLIP_NEAR;

    public static final int TEXTURE_SAMPLE_TYPE_CLAMP         = 0;
    public static final int TEXTURE_SAMPLE_TYPE_CLAMP_TO_EDGE = 1;
    public static final int TEXTURE_SAMPLE_TYPE_REPEAT        = 2;
    public static final int DEFAULT_TEXTURE_SAMPLE_TYPE       = TEXTURE_SAMPLE_TYPE_REPEAT;

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private static QColor        drawClearColor  = new QColor();
    private static QColor        drawLineColor   = new QColor(0xFF, 0xFF, 0xFF);
    private static QRenderBuffer drawTexture     = null; // TODO: create seperate texture class?
    private static QRenderBuffer drawTarget      = null;
    private static float         drawClipNear    = DEFAULT_CLIP_NEAR;
    private static int           drawSampleType  = DEFAULT_TEXTURE_SAMPLE_TYPE;

    private static QMatrix4x4[]  matrixStack        = new QMatrix4x4[MATRIX_STACK_SIZE];
    private static int           matrixStackPointer = 0;

    static {
        for (int i = 0; i < matrixStack.length; i++) {
            matrixStack[i] = new QMatrix4x4(QMatrix4x4.identity);
        }
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private static void internalProcessMeshVerts(float[] inOutVerts) {

        for (int readOffset = 0; readOffset < inOutVerts.length; readOffset += QMesh.COMPONENTS_PER_VERTEX) {

            // TODO: optimize to reduce allocations
            QVector4 vert = new QVector4(
                inOutVerts[readOffset + 0], 
                inOutVerts[readOffset + 1], 
                inOutVerts[readOffset + 2]
            );

            vert = QMatrix4x4.multiply4(matrixPeek( ), vert);

            inOutVerts[readOffset + 0] = vert.getX();
            inOutVerts[readOffset + 1] = vert.getY();
            inOutVerts[readOffset + 2] = vert.getZ();

        }

    }

    private static float[] internalBuildTriDataComplete(
        float[] inVertData,
        float[] inUVData,
        int[]   inTriData,
        int     triIndex
    ) {

        int triDataReadOffset = triIndex * QMesh.COMPONENTS_PER_TRI_DATA;

        float[] triDataComplete = new float[QMesh.COMPONENTS_PER_TRI_DATA];

        triDataComplete[QMesh.TRI_DATA_VERTEX_0_OFFSET] =
            inVertData[inTriData[triDataReadOffset + QMesh.TRI_DATA_VERTEX_0_OFFSET]];
        triDataComplete[QMesh.TRI_DATA_UV_0_OFFSET] =
            inUVData[inTriData[triDataReadOffset + QMesh.TRI_DATA_UV_0_OFFSET]];

        triDataComplete[QMesh.TRI_DATA_VERTEX_1_OFFSET] =
            inVertData[inTriData[triDataReadOffset + QMesh.TRI_DATA_VERTEX_1_OFFSET]];
        triDataComplete[QMesh.TRI_DATA_UV_1_OFFSET] =
            inUVData[inTriData[triDataReadOffset + QMesh.TRI_DATA_UV_1_OFFSET]];

        triDataComplete[QMesh.TRI_DATA_VERTEX_2_OFFSET] =
            inVertData[inTriData[triDataReadOffset + QMesh.TRI_DATA_VERTEX_2_OFFSET]];
        triDataComplete[QMesh.TRI_DATA_UV_2_OFFSET] =
            inUVData[inTriData[triDataReadOffset + QMesh.TRI_DATA_UV_2_OFFSET]];

        return triDataComplete;

    }

    private static float[][] internalClipTriVertsCase1(
        boolean[] inClipStates,
        int[]     inClipIndicies,
        float[]   inTriDataComplete
    ) {

        // TODO: complete
        return null;

    }

    private static float[][] internalClipTriVertsCase2(
        boolean[] inClipStates,
        int[]     inClipIndicies,
        float[]   inTriDataComplete
    ) {

        // TODO: complete
        return null;

    }

    private static float[][] internalClipTriVerts(float[] inTriDataComplete) {

        int       vertClipCount    = 0;
        boolean[] vertClipStates   = new boolean[QMesh.VERTICIES_PER_TRI];
        int[]     vertClipIndicies = new int[QMesh.VERTICIES_PER_TRI];
        
        if (inTriDataComplete[QMesh.TRI_DATA_VERTEX_0_OFFSET + 2] > drawClipNear) {
            vertClipStates[0] = true;
            vertClipIndicies[vertClipCount] = 0;
            vertClipCount += 1;
        }
        if (inTriDataComplete[QMesh.TRI_DATA_VERTEX_1_OFFSET + 2] > drawClipNear) {
            vertClipStates[1] = true;
            vertClipIndicies[vertClipCount] = 1;
            vertClipCount += 1;
        }
        if (inTriDataComplete[QMesh.TRI_DATA_VERTEX_1_OFFSET + 2] > drawClipNear) {
            vertClipStates[2] = true;
            vertClipIndicies[vertClipCount] = 2;
            vertClipCount += 1;
        }

        // CASE -> 0 VERTS BEHIND
        // triangle is completely before near plane, same as input
        if (vertClipCount == 0) {
            return new float[][] { inTriDataComplete };
        }

        // CASE -> 3 VERTS BEHIND
        // triangle is completely behind near plane, cull whole triangle
        if (vertClipCount == 3) {
            return null;
        }

        // CASE -> 1 VERT BEHIND
        // triangle is clipped into quad, must generate 2 triangles
        if (vertClipCount == 1) {
            return internalClipTriVertsCase1(vertClipStates, vertClipIndicies, inTriDataComplete);
        }

        // CASE -> 2 VERTS BEHIND
        // triangle is clipped into different triangle
        if (vertClipCount == 2) {
            return internalClipTriVertsCase2(vertClipStates, vertClipIndicies, inTriDataComplete);
        }

        throw new QException(
            PointOfError.BadState,
            "reached bad clipping state. max verts in triangle to be clipped is " +
            QMesh.VERTICIES_PER_TRI + ". tried to clip " + vertClipCount 
        );

    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public static void setClearColor(QColor color) {
        drawClearColor.set(color);
    }

    public static void setLineColor(QColor color) {
        drawLineColor.set(color);
    }

    public static void setTexture(QRenderBuffer texture) {
        drawTexture = texture;
    }

    public static void setTarget(QRenderBuffer target) {
        drawTarget = target;
    }

    public static void matrixIdentity( ) {
        matrixStack[matrixStackPointer].set(QMatrix4x4.identity);
    }

    public static void matrixRotate(float x, float y, float z) {
        matrixStack[matrixStackPointer].rotate(x, y, z);
    }

    public static void matrixRotate(QVector4 vec) {
        matrixStack[matrixStackPointer].rotate(vec);
    }

    public static void matrixTranslate(float x, float y, float z) {
        matrixStack[matrixStackPointer].translate(x, y, z);
    }

    public static void matrixTranslate(QVector4 vec) {
        matrixStack[matrixStackPointer].translate(vec);
    }

    public static void matrixScale(float s) {
        matrixStack[matrixStackPointer].scale(s);
    }

    public static void matrixScale(float x, float y, float z) {
        matrixStack[matrixStackPointer].scale(x, y, z);
    }

    public static void matrixScale(QVector4 vec) {
        matrixStack[matrixStackPointer].scale(vec);
    }

    public static void matrixSet(float[] vec) {
        matrixStack[matrixStackPointer].set(vec);
    }

    public static void matrixSet(QMatrix4x4 matrix) {
        matrixStack[matrixStackPointer].set(matrix);
    }

    public static QMatrix4x4 matrixPeek( ) {
        return matrixStack[matrixStackPointer];
    }
    
    public static void matrixPush( ) {
        matrixStackPointer += 1;
        if (matrixStackPointer >= MATRIX_STACK_SIZE) {
            throw new QException(
                PointOfError.BadState,
                "matrix stack overflow"
            );
        }
        matrixStack[matrixStackPointer].set(matrixStack[matrixStackPointer - 1]);
    }

    public static void matrixPop( ) {
        matrixStackPointer -= 1;
        if (matrixStackPointer < 0) {
            throw new QException(
                PointOfError.BadState,
                "matrix stack underflow"
            );
        }
    }

    // TODO: complete
    public static void draw(QMesh mesh) {

        float[] vertexData = mesh.getVertexData();
        int[]   triData    = mesh.getTriData();
        float[] uvData     = mesh.getUVData();

        float[] vertexDataCopy = new float[vertexData.length];
        System.arraycopy(
            vertexData, 
            0, 
            vertexDataCopy, 
            0, 
            vertexData.length
        );

        internalProcessMeshVerts(vertexDataCopy);

        final int triCount = triData.length / QMesh.COMPONENTS_PER_TRI_DATA;
        for (int triIndex = 0; triIndex < triCount; triIndex++) {

            float[] triDataComplete = internalBuildTriDataComplete(
                vertexData, 
                uvData, 
                triData, 
                triIndex
            );

            float[][] clipTriDataComplete = internalClipTriVerts(triDataComplete);

            // TODO: finish
            switch (clipTriDataComplete.length) {
                case 0:
                    break;
            
                default:
                    break;
            }

        }

    }

}
