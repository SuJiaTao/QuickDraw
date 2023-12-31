// Bailey JT Brown
// 2023
// QViwer.java

package QDraw;

public final class QViewer {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final float  MIN_NEAR_CLIP       = -0.005f;
    public static final QColor DEFAULT_CLEAR_COLOR = QColor.Black;
    public static final float  DEFAULT_NEAR_CLIP   = MIN_NEAR_CLIP;

    public static final float DEFAULT_VIEWBOUND_LEFT   = -1.0f;
    public static final float DEFAULT_VIEWBOUND_RIGHT  = 1.0f;
    public static final float DEFAULT_VIEWBOUND_BOTTOM = -1.0f;
    public static final float DEFAULT_VIEWBOUND_TOP    = 1.0f;

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private float         nearClip   = DEFAULT_NEAR_CLIP;
    private QColor        clearColor = DEFAULT_CLEAR_COLOR;
    private QMatrix4x4    projectionTransform = QMatrix4x4.Identity;
    private QMatrix4x4    viewTransform       = QMatrix4x4.Identity;
    private QRenderBuffer renderTarget        = null;

    /////////////////////////////////////////////////////////////////
    // PRIVATE CLASSES
    private class QTri {
        QVector pos0, pos1, pos2;
        QVector uv0, uv1, uv2;
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private void init(QRenderBuffer target) {
        renderTarget = target;
        setViewBounds(
            DEFAULT_VIEWBOUND_LEFT, 
            DEFAULT_VIEWBOUND_RIGHT, 
            DEFAULT_VIEWBOUND_BOTTOM, 
            DEFAULT_VIEWBOUND_TOP
        );
    }

    private void internalViewMesh(
        QMesh mesh,
        QMatrix4x4 meshTransform
    ) {
        
        

    }

    private void internalViewTri(

    ) {

    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public QViewer( ) {
        init(null);
    }

    public QViewer(QRenderBuffer renderTarget) {
        init(renderTarget);
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void setRenderTarget(QRenderBuffer target) {
        renderTarget = target;
    }

    public void setNearClip(float val) {
        nearClip = Math.min(MIN_NEAR_CLIP, val);
    }

    public void setClearColor(QColor color) {
        clearColor.set(color);
    }

    public void setViewBounds(float left, float right, float bottom, float top) {
        // TODO: this may also be completely wrong
        // refer to
        // https://registry.khronos.org/OpenGL-Refpages/gl2.1/xhtml/glOrtho.xml
        projectionTransform.set(QMatrix4x4.Identity);
        projectionTransform.scale(
            2.0f / (right - left),
            2.0f / (top - bottom),
            1.0f
        );
        projectionTransform.translate(
            - (right + left) / (right - left),
            - (top + bottom) / (top - bottom),
            0.0f
        );
    }

    public void setCamera(
        QVector position,
        QVector rotation,
        QVector scale
    ) {
        // TODO: this may be totally wrong
        viewTransform = QMatrix4x4.TRS(
            position.multiply3(-1.0f), 
            rotation.multiply3(-1.0f), 
            new QVector(
                1.0f / scale.getX(),
                1.0f / scale.getY(),
                1.0f / scale.getZ()
            )
        );
    }

}
