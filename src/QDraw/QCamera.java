// Bailey JT Brown
// 2023
// QCamera.java

package QDraw;

public final class QCamera {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final float  MIN_NEAR_CLIP       = 0.005f;
    public static final QColor DEFAULT_CLEAR_COLOR = QColor.Black;
    public static final float  DEFAULT_NEAR_CLIP   = MIN_NEAR_CLIP;

    public static final float DEFAULT_VIEWDIMS_LEFT   = -1.0f;
    public static final float DEFAULT_VIEWDIMS_RIGHT  = 1.0f;
    public static final float DEFAULT_VIEWDIMS_BOTTOM = -1.0f;
    public static final float DEFAULT_VIEWDIMS_TOP    = 1.0f;

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private float      nearClip;
    private QColor     clearColor;
    private QMatrix4x4 viewTransform;
    private QMatrix4x4 cameraTransform;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void setNearClip(float val) {
        nearClip = Math.max(MIN_NEAR_CLIP, val);
    }

    public void setClearColor(QColor color) {
        clearColor.set(color);
    }

    public void setViewDims(float left, float right, float bottom, float top) {
        // refer to
        // https://registry.khronos.org/OpenGL-Refpages/gl2.1/xhtml/glOrtho.xml
        viewTransform.set(QMatrix4x4.Identity);
        viewTransform.scale(
            2.0f / (right - left),
            2.0f / (top - bottom),
            1.0f
        );
        viewTransform.translate(
            - (right + left) / (right - left),
            - (top + bottom) / (top - bottom),
            0.0f
        );
    }

    public void setCamera(
        QVector4 position,
        QVector4 rotation,
        QVector4 scale
    ) {
        
    }

}
