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
    public static final float DEFAULT_CLIP_NEAR   = 0.01f;

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

    /////////////////////////////////////////////////////////////////
    // DRAW METHOD AND INTERNALS
    private static final int OFFSET_X = 0;
    private static final int OFFSET_Y = 1;
    private static final int OFFSET_Z = 2;
    private static final int CLIP_CASE_CULL = 0;
    private static final int CLIP_CASE_1TRI = 1;
    private static final int CLIP_CASE_2TRI = 2;
    private static final int CLIP_CASE_NONE = 3;
    private static final int VERTEX_0_OFFSET = 
        (QMesh.COMPONENTS_PER_VERTEX + QMesh.COMPONENTS_PER_UV) * 0;
    private static final int VERTEX_1_OFFSET = 
        (QMesh.COMPONENTS_PER_VERTEX + QMesh.COMPONENTS_PER_UV) * 1;
    private static final int VERTEX_2_OFFSET = 
        (QMesh.COMPONENTS_PER_VERTEX + QMesh.COMPONENTS_PER_UV) * 2;

    private static float[] __ipv_vert4_1 = { 0.0f, 0.0f, 0.0f, 1.0f };
    private static float[] __ipv_vert4_2 = { 0.0f, 0.0f, 0.0f, 1.0f };
    private static float[] __ipv_vert4_3 = { 0.0f, 0.0f, 0.0f, 1.0f };
    private static void internalProcessVerticies(float[] triangle) {
        
        __ipv_vert4_1[3] = 0.0f;
        __ipv_vert4_2[3] = 0.0f;
        __ipv_vert4_3[3] = 0.0f;

        System.arraycopy(
            triangle, 
            VERTEX_0_OFFSET, 
            __ipv_vert4_1, 
            0,
            3
        );
        System.arraycopy(
            triangle, 
            VERTEX_1_OFFSET, 
            __ipv_vert4_2, 
            0,
            3
        );
        System.arraycopy(
            triangle, 
            VERTEX_2_OFFSET, 
            __ipv_vert4_3, 
            0,
            3
        );

        QMatrix4x4 transformMatrix = matrixStack[matrixStackPointer];
        QMatrix4x4.multiply4DestructiveNoAlloc(
            transformMatrix,
            __ipv_vert4_1
        );
        QMatrix4x4.multiply4DestructiveNoAlloc(
            transformMatrix,
            __ipv_vert4_2
        );
        QMatrix4x4.multiply4DestructiveNoAlloc(
            transformMatrix,
            __ipv_vert4_3
        );

        System.arraycopy(
            __ipv_vert4_1, 
            0, 
            triangle, 
            VERTEX_0_OFFSET,
            3
        );
        System.arraycopy(
            __ipv_vert4_2, 
            0, 
            triangle, 
            VERTEX_1_OFFSET,
            3
        );
        System.arraycopy(
            __ipv_vert4_3, 
            0, 
            triangle, 
            VERTEX_2_OFFSET,
            3
        );

    }

    private static void findPlaneIntersectPointDestructiveNoAlloc(
        float x1, float y1, float z1,
        float x2, float y2, float z2,
        float[] outBuff
    ) {

        float rcpDeltaZ = 1.0f / (z2 - z1);
        float xzSlope = (x2 - x1) * rcpDeltaZ;
        float yzSlope = (y2 - y1) * rcpDeltaZ;

        outBuff[0] = x1 + xzSlope * (drawClipNear - z1);
        outBuff[1] = y1 + yzSlope * (drawClipNear - z1);
        outBuff[2] = drawClipNear;

    }

    private static int       __ict_clipCase       = CLIP_CASE_NONE;
    private static boolean[] __ict_clipStates     = { false, false, false };
    private static int[]     __ict_behindVertNums = { 0, 0, 0 };
    private static float[] __ict_cliptri_1 = new float[QMesh.COMPONENTS_PER_BAKED_DATA];
    private static float[] __ict_cliptri_2 = new float[QMesh.COMPONENTS_PER_BAKED_DATA];
    private static void internalClipTriangle(float[] triangle) {

        // refer to:
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c

        int vertBehindCount = 0;
        if (triangle[VERTEX_0_OFFSET + OFFSET_Z] < drawClipNear) {
            __ict_behindVertNums[vertBehindCount] = 0;
            vertBehindCount += 1;
            __ict_clipStates[0] = true;
        }
        if (triangle[VERTEX_1_OFFSET + OFFSET_Z] < drawClipNear) {
            __ict_behindVertNums[vertBehindCount] = 1;
            vertBehindCount += 1;
            __ict_clipStates[1] = true;
        }
        if (triangle[VERTEX_2_OFFSET + OFFSET_Z] < drawClipNear) {
            __ict_behindVertNums[vertBehindCount] = 2;
            vertBehindCount += 1;
            __ict_clipStates[2] = true;
        }

        // triangle is behind, cull it!
        if (vertBehindCount == 3) {
            __ict_clipCase = CLIP_CASE_CULL;
            return;
        }

        

    }

    public static void draw(QMesh mesh) {
        if (drawTarget == null)
            return;

        float[] bakedData = mesh.getBakedData();
        float[] triangle = new float[QMesh.COMPONENTS_PER_BAKED_DATA];
        for (int bakedDataOffset = 0; 
             bakedDataOffset > bakedData.length; 
             bakedDataOffset += QMesh.COMPONENTS_PER_BAKED_DATA
        ) {
            
            System.arraycopy(
                bakedData, 
                bakedDataOffset, 
                triangle, 
                0, 
                QMesh.COMPONENTS_PER_BAKED_DATA
            );

            internalProcessVerticies(triangle);
            internalClipTriangle(triangle);

            if (__ict_clipCase == CLIP_CASE_CULL)
                continue;

            switch (__ict_clipCase) {
                case CLIP_CASE_NONE:
                    
                    break;

                case CLIP_CASE_1TRI:

                    break;

                case CLIP_CASE_2TRI:

                    break;
            
                default:
                    break;
            }

        }
        
    }

}
