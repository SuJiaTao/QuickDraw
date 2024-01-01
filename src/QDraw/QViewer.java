// Bailey JT Brown
// 2023
// QViwer.java

package QDraw;

import java.util.Arrays;

import QDraw.QException.PointOfError;

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
    private class Tri {
        public QVector pos0 = null, pos1 = null, pos2 = null;
        public QVector uv0  = null, uv1  = null, uv2  = null;

        public void set(
            QVector _pos0, QVector _pos1, QVector _pos2,
            QVector _uv0,  QVector _uv1,  QVector _uv2
        ) {

            pos0 = _pos0;
            pos1 = _pos1;
            pos2 = _pos2;
            uv0 = _uv0;
            uv1 = _uv1;
            uv2 = _uv2;

        }
    }

    private class ClipState {
        public int       numVertsBehind     = 0;
        public boolean[] vertBehindState    = new boolean[3];
        public int[]     vertBehindIndicies = new int[3];

        public String toString( ) {
            return String.format(
                        "<numVBehind: %d, vStates: %s vIndicies: %s>",
                        numVertsBehind,
                        Arrays.toString(vertBehindState),
                        Arrays.toString(vertBehindIndicies)
                    );
        }
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
        
        QMesh viewMesh = new QMesh(mesh);
        for (int posIndex = 0; posIndex < viewMesh.getPosCount(); posIndex++) {
            QVector vert = new QVector(viewMesh.getPos(posIndex));
            vert = QMatrix4x4.multiply3(meshTransform, vert);
            vert = QMatrix4x4.multiply3(viewTransform, vert);
            vert = QMatrix4x4.multiply3(projectionTransform, vert);
            viewMesh.setPos(posIndex, vert.getX(), vert.getY(), vert.getZ());
        }

        for (int tdiIndex = 0; tdiIndex < viewMesh.getTriCount(); tdiIndex++) {
            // TODO: cleanup this horrid mess
            Tri viewTri = new Tri();
            viewTri.set(
                new QVector(viewMesh.getTriPos(tdiIndex, 0)),
                new QVector(viewMesh.getTriPos(tdiIndex, 1)),
                new QVector(viewMesh.getTriPos(tdiIndex, 2)),
                new QVector(viewMesh.getTriUV(tdiIndex, 0)),
                new QVector(viewMesh.getTriUV(tdiIndex, 1)),
                new QVector(viewMesh.getTriUV(tdiIndex, 2))
            );

            Tri[] clipTris = internalClipTri(viewTri);

            for (Tri tri : clipTris) {
                internalViewTri(tri);
            }
        }

    }

    private Tri[] internalClipTri(Tri tri) {
        // TODO: complete
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c

        ClipState clipState = new ClipState();

        if (tri.pos0.getZ() > nearClip) {
            clipState.vertBehindIndicies[clipState.numVertsBehind++] = 0;
            clipState.vertBehindState[0] = true;
        }

        if (tri.pos1.getZ() > nearClip) {
            clipState.vertBehindIndicies[clipState.numVertsBehind++] = 1;
            clipState.vertBehindState[1] = true;
        }

        if (tri.pos2.getZ() > nearClip) {
            clipState.vertBehindIndicies[clipState.numVertsBehind++] = 2;
            clipState.vertBehindState[2] = true;
        }

        switch (clipState.numVertsBehind) {
            // ALL VERTS BEFORE
            // triangle is not clipped
            case 0:
                return new Tri[] { tri };
            
            // ALL VERTS BEHIND
            // triangle should be culled
            case 3:
                return new Tri[0];

            // 2 VERTS BEHIND
            // triangle is turned into smaller triangle
            case 2:
                return internalClipTriCase2(tri, clipState);

            // 1 VERTS BEHIND
            // triangle is clipped into 2 smaller triangles
            case 1:
                return internalClipTriCase1(tri, clipState);
        
            // BAD STATE
            default:
                throw new QException(
                    PointOfError.BadState, 
                    "reached bad clipping state: " + clipState.toString()
                );
        }
    }

    private QVector internalFindClipIntersect(QVector pI, QVector pF) {
        float invDZ   = 1.0f / (pF.getZ() - pI.getZ());
        float slopeXZ = (pF.getX() - pI.getX()) * invDZ;
        float slopeYZ = (pF.getY() - pI.getY()) * invDZ;
        float dClip   = (nearClip - pI.getZ());
        return new QVector(
            pI.getX() + slopeXZ * dClip,
            pI.getY() + slopeYZ * dClip,
            nearClip
        );
    }

    private Tri[] internalClipTriCase1(Tri tri, ClipState clipState) {
        
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c
        return null;
    }

    private Tri[] internalClipTriCase2(Tri tri, ClipState clipState) {

        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c
        switch (clipState.vertBehindIndicies[0] + clipState.vertBehindIndicies[1]) {
            case (1 + 2):
                
                tri.pos1 = internalFindClipIntersect(tri.pos0, tri.pos1);
                tri.pos2 = internalFindClipIntersect(tri.pos0, tri.pos2);
                return new Tri[] { tri };

            case (0 + 2):

                tri.pos0 = internalFindClipIntersect(tri.pos1, tri.pos0);
                tri.pos2 = internalFindClipIntersect(tri.pos1, tri.pos2);
                return new Tri[] { tri };

            case (0 + 1):

                tri.pos0 = internalFindClipIntersect(tri.pos2, tri.pos0);
                tri.pos1 = internalFindClipIntersect(tri.pos2, tri.pos1);
                return new Tri[] { tri };
        
            // BAD STATE
            default:
                throw new QException(
                    PointOfError.BadState, 
                    "bad clip state:" + clipState.toString()
                );
        }

    }

    private void internalViewTri(Tri tri) {
        // TODO: complete
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
