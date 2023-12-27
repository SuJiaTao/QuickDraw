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
    // PUBLIC DRAW SETTING METHODS
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



    /////////////////////////////////////////////////////////////////
    // PUBLIC MATRIX METHODS
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

    public static final QMatrix4x4 matrixPeek( ) {
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
}
