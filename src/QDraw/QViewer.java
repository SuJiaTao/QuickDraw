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
    private QMatrix4x4    viewTransform       = new QMatrix4x4(QMatrix4x4.Identity);
    private QRenderBuffer renderTarget        = null;
    private float         viewLeft, viewRight, viewBottom, viewTop;

    /////////////////////////////////////////////////////////////////
    // PRIVATE CLASSES
    private class Tri {
        public QVector pos0 = new QVector(), 
                       pos1 = new QVector(), 
                       pos2 = new QVector();
        public QVector uv0  = new QVector(), 
                       uv1  = new QVector(), 
                       uv2  = new QVector();

        public void set(
            QVector _pos0, QVector _uv0, 
            QVector _pos1, QVector _uv1,  
            QVector _pos2, QVector _uv2
        ) {

            pos0.set(_pos0);
            pos1.set(_pos1);
            pos2.set(_pos2);
            
            // todo: finish UV interpolation
            // uv0.set(_uv0);
            // uv1.set(_uv1);
            // uv2.set(_uv2);

        }

        public String toString( ) {
            return String.format(
                "<tri %s %s %s>",
                pos0.toString(),
                pos1.toString(),
                pos2.toString()
            );
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
        
        if (renderTarget == null) {
            throw new QException(
                PointOfError.BadState, 
                "render target unassigned"
            );
        }

        QMesh viewMesh = new QMesh(mesh);
        for (int posIndex = 0; posIndex < viewMesh.getPosCount(); posIndex++) {
            QVector vert = new QVector(viewMesh.getPos(posIndex));
            
            vert = QMatrix4x4.multiply3(meshTransform, vert);
            vert = QMatrix4x4.multiply3(viewTransform, vert);
            
            viewMesh.setPos(posIndex, vert.getX(), vert.getY(), vert.getZ());
        }

        for (int tdiIndex = 0; tdiIndex < viewMesh.getTriCount(); tdiIndex++) {
            // TODO: cleanup this horrid mess
            Tri viewTri = new Tri();
            viewTri.set(
                new QVector(viewMesh.getTriPos(tdiIndex, 0)),
                new QVector(viewMesh.getTriUV(tdiIndex, 0)),
                new QVector(viewMesh.getTriPos(tdiIndex, 1)),
                new QVector(viewMesh.getTriUV(tdiIndex, 1)),
                new QVector(viewMesh.getTriPos(tdiIndex, 2)),
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
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c

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

        // NOTE:
        // - all faces must be constructed in CLOCKWISE winding order.
        // - when 1 vertex is clipped, generated face is a quad
        // - triangle will be re-shuffled so that it remains CLOCKWISE where pos2 is clipped
        // - quad generated will be 0/2, 0, 1, 1/2 (where a/b is clipped interpolation)
        // - (this is slightly different from Casesium where pos0 is clipped)

        Tri shuffledTri = new Tri();

        // SHUFFLE TRIANGLE
        switch (clipState.vertBehindIndicies[0]) {
            // SHUFFLE (0, 1, 2) -> (1, 2, 0)
            case 0:

                shuffledTri.set(
                    tri.pos1, 
                    tri.uv1, 
                    tri.pos2, 
                    tri.uv2, 
                    tri.pos0, 
                    tri.uv0
                );

                break;

            // SHUFFLE (0, 1, 2) -> (2, 0, 1)
            case 1:

                shuffledTri.set(
                    tri.pos2, 
                    tri.uv2, 
                    tri.pos0, 
                    tri.uv0, 
                    tri.pos1, 
                    tri.uv1
                );

                break;

            // SHUFFLE (0, 1, 2) -> (0, 1, 2)
            case 2:

                shuffledTri.set(
                    tri.pos0, 
                    tri.uv0, 
                    tri.pos1, 
                    tri.uv1, 
                    tri.pos2, 
                    tri.uv2
                );

                break;
        
            default:
                throw new QException(
                    PointOfError.BadState,
                    "bad clip state: " + clipState.toString()
                );
        }

        QVector pos02 = internalFindClipIntersect(shuffledTri.pos0, shuffledTri.pos2);
        QVector pos12 = internalFindClipIntersect(shuffledTri.pos1, shuffledTri.pos2);

        // TODO: complete UV interpolation logic
        Tri quadTri0 = new Tri();
        quadTri0.set(pos02, null, shuffledTri.pos0, null, shuffledTri.pos1, null);

        Tri quadTri1 = new Tri();
        quadTri1.set(pos02, null, shuffledTri.pos1, null, pos12, null);

        return new Tri[] { quadTri0, quadTri1 };

    }

    private Tri[] internalClipTriCase2(Tri tri, ClipState clipState) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c

        // TESSELATE TRIANGLE BASED ON 2 CLIPPED VERTICIES
        switch (clipState.vertBehindIndicies[0] + clipState.vertBehindIndicies[1]) {
            // VERTS 1 & 2 ARE CLIPPED
            case (1 + 2):
                
                tri.pos1 = internalFindClipIntersect(tri.pos0, tri.pos1);
                tri.pos2 = internalFindClipIntersect(tri.pos0, tri.pos2);
                return new Tri[] { tri };

            // VERTS 0 & 2 ARE CLIPPED
            case (0 + 2):

                tri.pos0 = internalFindClipIntersect(tri.pos1, tri.pos0);
                tri.pos2 = internalFindClipIntersect(tri.pos1, tri.pos2);
                return new Tri[] { tri };

            // VERTS 0 & 1 ARE CLIPPED
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

    private void internalMapVertToScreenSpace(QVector vert) {
        // NOTE:
        //  - this transformation will map (left, right) -> (0, targetWidth) and
        //    (bottom, top) -> (0, targetheight). this is essentially a worldspace
        //    to screenspace transformation
        //  - in order to do this, the space is translated such that the bottom left
        //    corner is (0, 0), and then it is scaled to fit the screenspace

        vert.setX(vert.getX() - viewLeft);
        vert.setY(vert.getY() - viewBottom);
        vert.setX(vert.getX() * (renderTarget.getWidth()  / (viewRight - viewLeft)));
        vert.setY(vert.getY() * (renderTarget.getHeight() / (viewTop - viewBottom)));
    }

    private void internalViewTri(Tri tri) {
        
        // NOTE:
        // - from this point forward, all z values will be inverted
        tri.pos0.setZ(1.0f / tri.pos0.getZ());
        tri.pos1.setZ(1.0f / tri.pos1.getZ());
        tri.pos2.setZ(1.0f / tri.pos2.getZ());

        // PROJECT TRI
        tri.pos0.setX(tri.pos0.getX() * tri.pos0.getZ());
        tri.pos0.setY(tri.pos0.getY() * tri.pos0.getZ());
        tri.pos1.setX(tri.pos1.getX() * tri.pos1.getZ());
        tri.pos1.setY(tri.pos1.getY() * tri.pos1.getZ());
        tri.pos2.setX(tri.pos2.getX() * tri.pos2.getZ());
        tri.pos2.setY(tri.pos2.getY() * tri.pos2.getZ());

        // MAP TO SCREEN SPACE
        internalMapVertToScreenSpace(tri.pos0);
        internalMapVertToScreenSpace(tri.pos1);
        internalMapVertToScreenSpace(tri.pos2);

        // NOTE:
        // - a triangle must be rasterized in two parts, a line will be cut
        //   through it's middle highest verticie horizontally, and each partition
        //   will be draw seperately
        // - the triangle will have to be sorted by height then cut
        // - after being cut, the top two verticies will be sorted from left to right

        Tri sortedTri = internalSortTriVertsByHeight(tri);

        float invSlope20 =
            (sortedTri.pos0.getX() - sortedTri.pos2.getX()) / 
            (sortedTri.pos0.getY() - sortedTri.pos2.getY());
        float dY21 = sortedTri.pos1.getY() - sortedTri.pos2.getY();

        QVector midPoint = new QVector(
            sortedTri.pos2.getX() + (invSlope20 * dY21),
            sortedTri.pos1.getY()
        );

        Tri flatTopTri = new Tri();
        flatTopTri.set(
            sortedTri.pos1, null, midPoint, null, sortedTri.pos2, null
        );

        Tri flatBottomTri = new Tri();
        flatBottomTri.set(
            sortedTri.pos1, null, midPoint, null, sortedTri.pos0, null
        );

        internalDrawFlatTopTri(flatTopTri, sortedTri);
        internalDrawFlatBottomTri(flatBottomTri, sortedTri);

    }

    private Tri internalSortTriVertsByHeight(Tri tri) { 

        QVector temp;
        if (tri.pos0.getY() < tri.pos1.getY()) {
            temp = tri.pos1;
            tri.pos1 = tri.pos0;
            tri.pos0 = temp;
        }
        if (tri.pos1.getY() < tri.pos2.getY()) {
            temp = tri.pos2;
            tri.pos2 = tri.pos1;
            tri.pos1 = temp;
        }
        if (tri.pos0.getY() < tri.pos1.getY()) {
            temp = tri.pos1;
            tri.pos1 = tri.pos0;
            tri.pos0 = temp;
        }

        return tri;

    }

    private void internalDrawFlatTopTri(Tri flatTri, Tri sortedTri) {
        
        // NOTE: 
        // - vertex arrangement is as follows:
        //   0 -> LEFT, 1 -> RIGHT, 2 -> BOTTOM POINT
        // - flattop triangle is drawn from bottom to top

        // sort top 2 verticies from left to right
        if (flatTri.pos0.getX() > flatTri.pos1.getX()) {
            QVector temp = flatTri.pos1;
            flatTri.pos1 = flatTri.pos0;
            flatTri.pos0 = temp;
        }

        float invDY = 1.0f / (flatTri.pos0.getY() - flatTri.pos2.getY());
        if (Float.isNaN(invDY)) { return; }

        float invSlope20 = (flatTri.pos0.getX() - flatTri.pos2.getX()) * invDY;
        float invSlope21 = (flatTri.pos1.getX() - flatTri.pos2.getX()) * invDY;

        int Y_START = Math.max((int)flatTri.pos2.getY(), 0);
        int Y_END   = Math.min((int)flatTri.pos0.getY(), renderTarget.getHeight() - 1);

        for (int drawY = Y_START; drawY <= Y_END; drawY++) {

            float distY = Math.max(0.0f, drawY - flatTri.pos2.getY());
            int X_START = Math.max(
                (int)(flatTri.pos2.getX() + (invSlope20 * distY)),
                0
            );
            int X_END   = Math.min(
                (int)(flatTri.pos2.getX() + (invSlope21 * distY)), 
                renderTarget.getWidth() - 1
            );

            for (int drawX = X_START; drawX <= X_END; drawX++) {
                renderTarget.getColorData()[renderTarget.coordToDataIndex(drawX, drawY)] = 
                    QColor.White.toInt();
            }

        }

    }

    private void internalDrawFlatBottomTri(Tri flatTri, Tri sortedTri) {
        
        // NOTE: 
        // - vertex arrangement is as follows:
        //   0 -> LEFT, 1 -> RIGHT, 2 -> TOP POINT
        // - flatbottom triangle is drawn from bottom to top

        // sort bottom 2 verticies from left to right
        if (flatTri.pos0.getX() > flatTri.pos1.getX()) {
            QVector temp = flatTri.pos1;
            flatTri.pos1 = flatTri.pos0;
            flatTri.pos0 = temp;
        }

        float invDY = 1.0f / (flatTri.pos2.getY() - flatTri.pos0.getY());
        if (Float.isNaN(invDY)) { return; }

        float invSlope02 = (flatTri.pos2.getX() - flatTri.pos0.getX()) * invDY;
        float invSlope12 = (flatTri.pos2.getX() - flatTri.pos1.getX()) * invDY;

        int Y_START = Math.max((int)flatTri.pos0.getY(), 0);
        int Y_END   = Math.min((int)flatTri.pos2.getY(), renderTarget.getHeight() - 1);

        for (int drawY = Y_START; drawY <= Y_END; drawY++) {

            float distY = Math.max(0.0f, drawY - flatTri.pos0.getY());
            int X_START = Math.max(
                (int)(flatTri.pos0.getX() + (invSlope02 * distY)),
                0
            );
            int X_END   = Math.min(
                (int)(flatTri.pos1.getX() + (invSlope12 * distY)), 
                renderTarget.getWidth() - 1
            );

            for (int drawX = X_START; drawX <= X_END; drawX++) {
                renderTarget.setColor(drawX, drawY, QColor.White.toInt());
            }

        }

    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public QViewer(QRenderBuffer renderTarget) {
        init(renderTarget);
    }

    public QViewer(QRenderBuffer renderTarget, float aspect) {
        init(renderTarget);
        setViewBounds(-aspect, aspect, -1.0f, 1.0f);
    }

    public QViewer(
        QRenderBuffer renderTarget, 
        float viewLeft, float viewRight,
        float viewBottom, float viewTop
    ) {
        init(renderTarget);
        setViewBounds(viewLeft, viewRight, viewBottom, viewTop);
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void setNearClip(float val) {
        nearClip = Math.min(MIN_NEAR_CLIP, val);
    }

    public void setClearColor(QColor color) {
        clearColor.set(color);
    }

    public void setViewBounds(float left, float right, float bottom, float top) {
        viewLeft   = left;
        viewRight  = right;
        viewBottom = bottom;
        viewTop    = top;
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

    public void blink( ) {
        renderTarget.clearColorBuffer();
        renderTarget.clearDepthBuffer();
    }

    public void viewMesh(
        QMesh mesh,
        QMatrix4x4 meshTransform
    ) {
        internalViewMesh(mesh, meshTransform);
    }

}
