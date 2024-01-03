// Bailey JT Brown
// 2023-2024
// QViwer.java

package QDraw;

import java.util.Arrays;

import QDraw.QException.PointOfError;

public final class QViewer extends QEncoding {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final float MIN_NEAR_CLIP     = -0.005f;
    public static final float DEFAULT_NEAR_CLIP = MIN_NEAR_CLIP;
    public static final int DEFAULT_FILL_COLOR  = QColor.White().toInt();

    public static final float DEFAULT_VIEWBOUND_LEFT   = -1.0f;
    public static final float DEFAULT_VIEWBOUND_RIGHT  = 1.0f;
    public static final float DEFAULT_VIEWBOUND_BOTTOM = -1.0f;
    public static final float DEFAULT_VIEWBOUND_TOP    = 1.0f;

    public static final float RENDER_DEPTH_MAX_DEPTH = -20.0f;

    public static final RenderType DEFAULT_RENDERTYPE = RenderType.Textured;
    public static final SampleType DEFAULT_SAMPLETYPE = SampleType.Repeat;

    private static final float BACKFACE_CULL_MIN_DOT  = 0.5f;
    private static final float DEPTH_TEST_EPSILON     = 0.002f;

    /////////////////////////////////////////////////////////////////
    // PUBLIC ENUMS
    public enum RenderType {
        FlatColor,
        Textured,
        CustomShader,
        Depth
    };

    public enum SampleType {
        Cutoff,
        Clamp,
        Repeat
    };

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private float         nearClip   = DEFAULT_NEAR_CLIP;
    private float         viewLeft, viewRight, viewBottom, viewTop;
    private QRenderBuffer renderTarget   = null;
    private QSampleable   texture        = null;
    private int           fillColor      = DEFAULT_FILL_COLOR;
    private RenderType    renderType     = DEFAULT_RENDERTYPE;
    private SampleType    sampleType     = DEFAULT_SAMPLETYPE;
    private QShader       customShader   = null;
    private Object        shaderInput    = null;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void setNearClip(float val) {
        nearClip = Math.min(MIN_NEAR_CLIP, val);
    }

    public void setViewBounds(float left, float right, float bottom, float top) {
        viewLeft   = left;
        viewRight  = right;
        viewBottom = bottom;
        viewTop    = top;
    }

    public void setRenderType(RenderType rType) {
        renderType = rType;
    }

    public void setSampleType(SampleType sType) {
        sampleType = sType;
    }

    public void setFillColor(QColor color) {
        fillColor = color.toInt( );
    }

    public void setTexture(QSampleable _texture) {
        texture = _texture;
    }

    public void setCustomShader(QShader shader) {
        customShader = shader;
    }

    public void setCustomShaderInput(Object input) {
        shaderInput = input;
    }

    public void clearFrame( ) {
        renderTarget.clearColorBuffer();
        renderTarget.clearDepthBuffer();
    }

    public void drawMesh(
        QMesh mesh,
        QMatrix4x4 meshTransform
    ) {
        internalViewMesh(mesh, meshTransform);
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE CLASSES
    private class Tri extends QEncoding {
        public float[] posDat = new float[VTRI_POSDAT_NUM_CMPS];
        public float[] uvDat  = new float[VTRI_UVDAT_NUM_CMPS];

        public Tri(Tri toCopy) {
            System.arraycopy(toCopy.posDat, 0, posDat, 0, VTRI_POSDAT_NUM_CMPS);
            System.arraycopy(toCopy.uvDat, 0, uvDat, 0, VTRI_UVDAT_NUM_CMPS);
        }

        public Tri(QMesh srcMesh, int tdiIndex) { 
            setPosFromTri(srcMesh, tdiIndex, 0);
            setPosFromTri(srcMesh, tdiIndex, 1);
            setPosFromTri(srcMesh, tdiIndex,2);
            setUVFromTri(srcMesh, tdiIndex, 0);
            setUVFromTri(srcMesh, tdiIndex, 1);
            setUVFromTri(srcMesh, tdiIndex,2);
        }

        public void swapVerts(int v1Num, int v2Num) {
            float[] tempPos = new float[MESH_POSN_NUM_CMPS];
            float[] tempUV  = new float[MESH_UV_NUM_CMPS];

            // move v1 to temp
            QMath.copy3(0, tempPos, getPosOffset(v1Num), posDat);
            QMath.copy2(0, tempUV, getUVOffset(v1Num), uvDat);

            // move v2 to v1
            QMath.copy3(getPosOffset(v1Num), posDat, getPosOffset(v2Num), posDat);
            QMath.copy2(getUVOffset(v1Num), uvDat, getUVOffset(v2Num), uvDat);

            // move temp to v2
            QMath.copy3(getPosOffset(v2Num), posDat, 0, tempPos);
            QMath.copy2(getUVOffset(v2Num), uvDat, 0, tempUV);
        }

        public void setPosFromTri(
            QMesh meshSrc,
            int   tdiIndex,
            int   posIndex
        ) {
            QMath.copy3(
                getPosOffset(posIndex), 
                posDat,
                meshSrc.getPosOffset(meshSrc.getTriPosIndex(tdiIndex, posIndex)),
                meshSrc.getPosData()
            );
        }

        public void setUVFromTri(
            QMesh meshSrc,
            int   tdiIndex,
            int   uvIndex
        ) {
            QMath.copy2(
                getUVOffset(uvIndex), 
                uvDat, 
                meshSrc.getUVOffset(meshSrc.getTriUVIndex(tdiIndex, uvIndex)),
                meshSrc.getUVData()
            );;
        }

        public void setVert(
            int vertNum,
            int posOffsetIn,
            float[] posIn,
            int uvOffsetIn,
            float[] uvIn
        ) {
            setPos(vertNum, posOffsetIn, posIn);
            setUV(vertNum, uvOffsetIn, uvIn);
        }

        public void setPos(
            int     posNum,
            int     offsetIn,
            float[] in
        ) {
            QMath.copy3(getPosOffset(posNum), posDat, offsetIn, in);
        }

        public void setUV(
            int     uvNum,
            int     offsetIn,
            float[] in
        ) {
            QMath.copy2(getUVOffset(uvNum), uvDat, offsetIn, in);
        }

        private void projectPos(int vertNum) {
            float invZ = 1.0f / getPosZ(vertNum);
            setPosZ(vertNum, invZ);
            QMath.mult2(getPosOffset(vertNum), posDat, -invZ);
        }

        public int getPosOffset(int posNum) {
            return MESH_POSN_NUM_CMPS * posNum;
        }

        public float getPosX(int posNum) {
            return posDat[getPosOffset(posNum) + VCTR_INDEX_X];
        }

        public void setPosX(int posNum, float x) {
            posDat[getPosOffset(posNum) + VCTR_INDEX_X] = x;
        }

        public float getPosY(int posNum) {
            return posDat[getPosOffset(posNum) + VCTR_INDEX_Y];
        }

        public void setPosY(int posNum, float y) {
            posDat[getPosOffset(posNum) + VCTR_INDEX_Y] = y;
        }

        public float getPosZ(int posNum) {
            return posDat[getPosOffset(posNum) + VCTR_INDEX_Z];
        }

        public void setPosZ(int posNum, float z) {
            posDat[getPosOffset(posNum) + VCTR_INDEX_Z] = z;
        }

        public int getUVOffset(int uvNum) {
            return MESH_UV_NUM_CMPS * uvNum;
        }

        public float getUV_U(int uvNum) {
            return uvDat[getUVOffset(uvNum) + MESH_UV_OFST_U];
        }

        public float getUV_V(int uvNum) {
            return uvDat[getUVOffset(uvNum) + MESH_UV_OFST_V];
        }

        public String toString( ) {
            return String.format(
                "<(%f %f %f) (%f %f %f) (%f %f %f)>", 
                posDat[0], posDat[1], posDat[2],
                posDat[3], posDat[4], posDat[5],
                posDat[6], posDat[7], posDat[8]);
        }
    }

    private class ClipState {
        public int       numVertsBehind     = 0;
        public boolean[] vertBehindState    = new boolean[MESH_POSN_NUM_CMPS];
        public int[]     vertBehindIndicies = new int[MESH_POSN_NUM_CMPS];

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

        if (renderType == RenderType.CustomShader && customShader == null) {
            throw new QException(
                PointOfError.BadState, 
                "render type is CustomShader but no shader was assigned"
            );
        }

        // NOTE:
        // - a copy of the mesh is created, but each vertex is transformed by
        //   the provided transformation matrix or vertex shader
        QMesh viewMesh         = new QMesh(mesh);
        float[] viewMeshPosDat = viewMesh.getPosData( );
        for (int posIndex = 0; posIndex < viewMesh.getPosCount(); posIndex++) {
            if (renderType == RenderType.CustomShader) {
                QVector3 vec = new QVector3(viewMesh.getPos(posIndex));
                vec = customShader.vertexShader(
                    posIndex, 
                    vec, 
                    new QMatrix4x4(meshTransform),
                    shaderInput
                );
                QMath.copy3(
                    viewMesh.getPosOffset(posIndex), 
                    viewMeshPosDat, 
                    0, 
                    vec.getComponents()
                );
            } else {
                QMath.mul3_4x4(
                    viewMesh.getPosOffset(posIndex), 
                    viewMeshPosDat, 
                    0, 
                    meshTransform.getComponents()
                );
            }
        }

        // NOTE:
        // - a triangle is constructed from viewMesh's TDI, if it is facing away from the camera
        //   it is culled, otherwise it is then clipped and the clipping output is rendered
        for (int tdiIndex = 0; tdiIndex < viewMesh.getTriCount(); tdiIndex++) {

            Tri viewTri    = new Tri(viewMesh, tdiIndex);
            if (internalCheckBackfacing(viewTri)) {
                continue;
            }

            Tri[] clipTris = internalClipTri(viewTri);

            for (Tri tri : clipTris) {
                internalViewTri(tri);
            }

        }

    }

    private boolean internalCheckBackfacing(Tri tri) {
        float[] d01 = new float[MESH_POSN_NUM_CMPS];
        QMath.copy3(0, d01, tri.getPosOffset(1), tri.posDat);
        QMath.sub3(0, d01, tri.getPosOffset(0), tri.posDat);

        float[] d02 = new float[MESH_POSN_NUM_CMPS];
        QMath.copy3(0, d02, tri.getPosOffset(2), tri.posDat);
        QMath.sub3(0, d02, tri.getPosOffset(0), tri.posDat);

        float[] normal = QMath.cross3(d01, d02);
        QMath.mult3(normal, 1.0f / QMath.mag3(normal));

        float[] awayAxis = { 0.0f, 0.0f, -1.0f };
        return (QMath.dot3(normal, awayAxis) > BACKFACE_CULL_MIN_DOT);
    }

    private Tri[] internalClipTri(Tri tri) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c

        ClipState clipState = new ClipState();

        if (tri.getPosZ(0) > nearClip) {
            clipState.vertBehindIndicies[clipState.numVertsBehind++] = 0;
            clipState.vertBehindState[0] = true;
        }

        if (tri.getPosZ(1) > nearClip) {
            clipState.vertBehindIndicies[clipState.numVertsBehind++] = 1;
            clipState.vertBehindState[1] = true;
        }

        if (tri.getPosZ(2) > nearClip) {
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

    private void internalFindClipIntersect(
        Tri srcTri,
        int pI,
        int pF,
        int offsetOut,
        float[] out
    ) {
        internalFindClipIntersect(
            srcTri.getPosOffset(pI),
            srcTri.posDat,
            srcTri.getPosOffset(pF),
            srcTri.posDat,
            offsetOut,
            out
        );
    }

    private void internalFindClipIntersect(
        int offsetPI,
        float[] pI,
        int offsetPF,
        float[] pF,
        int offsetOut,
        float[] out
    ) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c

        float invDZ   = 1.0f / (pF[offsetPF + VCTR_INDEX_Z] - pI[offsetPI + VCTR_INDEX_Z]);
        float slopeXZ = (pF[offsetPF + VCTR_INDEX_X] - pI[offsetPI + VCTR_INDEX_X]) * invDZ;
        float slopeYZ = (pF[offsetPF + VCTR_INDEX_Y] - pI[offsetPI + VCTR_INDEX_Y]) * invDZ;
        float dClip   = (nearClip - pI[offsetPI + VCTR_INDEX_Z]);

        out[offsetOut + VCTR_INDEX_X] = pI[offsetPI + VCTR_INDEX_X] + slopeXZ * dClip;
        out[offsetOut + VCTR_INDEX_Y] = pI[offsetPI + VCTR_INDEX_Y] + slopeYZ * dClip;
        out[offsetOut + VCTR_INDEX_Z] = nearClip;
    }

    private void internalFindIntersectUV(
        Tri     tri,
        int     vnumI,
        int     vnumF,
        float[] intersect,
        int     offsetUVOut,
        float[] uvOut
    ) {

        // NOTE:
        // - the intersection UV is naiively calculated by taking the
        //   magnitude of the displacement between midpoint and pI over
        //   the magnitude of the displacement between pF and Pi

        float[] temp = new float[MESH_POSN_NUM_CMPS];

        QMath.copy3(temp, intersect);
        QMath.sub3(0, temp, tri.getPosOffset(vnumI), tri.posDat);
        float magIntersect = QMath.mag3(temp);

        QMath.copy3(0, temp, tri.getPosOffset(vnumF), tri.posDat);
        QMath.sub3(0, temp, tri.getPosOffset(vnumI), tri.posDat);
        float magTotal = QMath.mag3(temp);

        float factorF = magIntersect / magTotal;
        float factorI = 1.0f - factorF;

        uvOut[MESH_UV_OFST_U] = tri.getUV_U(vnumI) * factorI + tri.getUV_U(vnumF) * factorF;
        uvOut[MESH_UV_OFST_V] = tri.getUV_V(vnumI) * factorI + tri.getUV_V(vnumF) * factorF;
    }

    private Tri[] internalClipTriCase1(Tri tri, ClipState clipState) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c

        // NOTE:
        // - all faces must be constructed in CLOCKWISE winding order.
        // - when 1 vertex is clipped, generated face is a quad
        // - triangle will be re-shuffled so that it remains CLOCKWISE where pos2 is clipped
        // - (this is slightly different from Casesium where pos0 is clipped)

        Tri shuffledTri = new Tri(tri);

        // SHUFFLE TRIANGLE
        switch (clipState.vertBehindIndicies[0]) {
            // SHUFFLE (0, 1, 2) -> (1, 2, 0)
            case 0:

                shuffledTri.swapVerts(0, 2); // (0 1 2) -> (2 1 0)
                shuffledTri.swapVerts(0, 1); // (2 1 0) -> (1 2 0)
                break;

            // SHUFFLE (0, 1, 2) -> (2, 0, 1)
            case 1:

                shuffledTri.swapVerts(0, 2); // (0 1 2) -> (2 1 0)
                shuffledTri.swapVerts(1, 2); // (2 1 0) -> (2 0 1)
                break;

            // SHUFFLE (0, 1, 2) -> (0, 1, 2)
            // or rather, no shuffle
            case 2:

                break;
        
            default:
                throw new QException(
                    PointOfError.BadState,
                    "bad clip state: " + clipState.toString()
                );
        }

        float[] pos02 = new float[MESH_POSN_NUM_CMPS];
        float[] pos12 = new float[MESH_POSN_NUM_CMPS];
        internalFindClipIntersect(shuffledTri, 0, 2, 0, pos02);
        internalFindClipIntersect(shuffledTri, 1, 2, 0, pos12);

        float[] uv02  = new float[MESH_UV_NUM_CMPS];
        float[] uv12  = new float[MESH_UV_NUM_CMPS];
        internalFindIntersectUV(shuffledTri, 0, 2, pos02, 0, uv02);
        internalFindIntersectUV(shuffledTri, 1, 2, pos12, 0, uv12);


        // NOTE:
        // - when 1 vertex is clipped, the resulting mesh is a quad
        // - our quad is 0/2, 0, 1, 1/2 (where a/b is clipped interpolation),
        //   which will be tesselated as (0/2, 0, 1), (0/2, 1, 1/2)

        Tri quadTri0 = new Tri(shuffledTri);
        quadTri0.setPos(2, 0, pos02); // (0 1 2) -> (0 1 0/2)
        quadTri0.setUV(2, 0, uv02);
        quadTri0.swapVerts(0, 2); // (0 1 0/2) -> (0/2 1 0)
        quadTri0.swapVerts(1, 2); // (0/2 1 0) -> (0/2 0 1)
        
        Tri quadTri1 = new Tri(shuffledTri); // (0 1 2)
        quadTri1.setPos(0, 0, pos02); // (0 1 2)   -> (0/2 1 2)
        quadTri1.setUV(0, 0, uv02);
        quadTri1.setPos(2, 0, pos12); // (0/2 1 2) -> (0/2 1 1/2)
        quadTri1.setUV(2, 0, uv12);
        
        return new Tri[] { quadTri0, quadTri1 };

    }

    private Tri[] internalClipTriCase2(Tri tri, ClipState clipState) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c

        // TESSELATE TRIANGLE BASED ON 2 CLIPPED VERTICIES
        switch (clipState.vertBehindIndicies[0] + clipState.vertBehindIndicies[1]) {
            // VERTS 1 & 2 ARE CLIPPED
            case (1 + 2):

                float[] pos10 = new float[MESH_POSN_NUM_CMPS];
                float[] pos20 = new float[MESH_POSN_NUM_CMPS];
                internalFindClipIntersect(tri, 1, 0, 0, pos10);
                internalFindClipIntersect(tri, 2, 0, 0, pos20);

                float[] uv10 = new float[MESH_UV_NUM_CMPS];
                float[] uv20 = new float[MESH_UV_NUM_CMPS];
                internalFindIntersectUV(tri, 1, 0, pos10, 0, uv10);
                internalFindIntersectUV(tri, 2, 0, pos20, 0, uv20);
                
                tri.setVert(1, 0, pos10, 0, uv10);
                tri.setVert(2, 0, pos20, 0, uv20);

                return new Tri[] { tri };

            // VERTS 0 & 2 ARE CLIPPED
            case (0 + 2):

                float[] pos01 = new float[MESH_POSN_NUM_CMPS];
                float[] pos21 = new float[MESH_POSN_NUM_CMPS];
                internalFindClipIntersect(tri, 0, 1, 0, pos01);
                internalFindClipIntersect(tri, 2, 1, 0, pos21);

                float[] uv01 = new float[MESH_UV_NUM_CMPS];
                float[] uv21 = new float[MESH_UV_NUM_CMPS];
                internalFindIntersectUV(tri, 0, 1, pos01, 0, uv01);
                internalFindIntersectUV(tri, 2, 1, pos21, 0, uv21);

                tri.setVert(0, 0, pos01, 0, uv01);
                tri.setVert(2, 0, pos21, 0, uv21);

                return new Tri[] { tri };

            // VERTS 0 & 1 ARE CLIPPED
            case (0 + 1):

                float[] pos02 = new float[MESH_POSN_NUM_CMPS];
                float[] pos12 = new float[MESH_POSN_NUM_CMPS];
                internalFindClipIntersect(tri, 2, 0, 0, pos02);
                internalFindClipIntersect(tri, 2, 1, 0, pos12);

                float[] uv02 = new float[MESH_UV_NUM_CMPS];
                float[] uv12 = new float[MESH_UV_NUM_CMPS];
                internalFindIntersectUV(tri, 0, 2, pos02, 0, uv02);
                internalFindIntersectUV(tri, 1, 2, pos12, 0, uv12);

                tri.setVert(0, 0, pos02, 0, uv02);
                tri.setVert(1, 0, pos12, 0, uv12);

                return new Tri[] { tri };
        
            // BAD STATE
            default:
                throw new QException(
                    PointOfError.BadState, 
                    "bad clip state:" + clipState.toString()
                );
        }

    }

    private void internalMapVertToScreenSpace(Tri srcTri, int vertNum) {
        // NOTE:
        //  - this transformation will map (left, right) -> (0, targetWidth) and
        //    (bottom, top) -> (0, targetheight). this is essentially a worldspace
        //    to screenspace transformation
        //  - in order to do this, the space is translated such that the bottom left
        //    corner is (0, 0), and then it is scaled to fit the screenspace

        srcTri.setPosX(vertNum, srcTri.getPosX(vertNum) - viewLeft);
        srcTri.setPosY(vertNum, srcTri.getPosY(vertNum) - viewBottom);

        srcTri.setPosX(vertNum, 
            srcTri.getPosX(vertNum) * 
            (renderTarget.getWidth()  / (viewRight - viewLeft)));
        srcTri.setPosY(vertNum, 
            srcTri.getPosY(vertNum) * 
            (renderTarget.getHeight() / (viewTop - viewBottom)));

    }

    private void internalViewTri(Tri tri) {
        
        // NOTE:
        // - from this point forward, all z values will be inverted
        tri.projectPos(0);
        tri.projectPos(1);
        tri.projectPos(2);

        // MAP TO SCREEN SPACE
        internalMapVertToScreenSpace(tri, 0);
        internalMapVertToScreenSpace(tri, 1);
        internalMapVertToScreenSpace(tri, 2);

        // NOTE:
        // - a triangle must be rasterized in two parts, a line will be cut
        //   through it's middle highest verticie horizontally, and each partition
        //   will be draw seperately
        // - the triangle will have to be sorted by height then cut
        // - after being cut, the top two verticies will be sorted from left to right

        Tri sortedTri = internalSortTriVertsByHeight(tri);

        float invSlope20 =
            (sortedTri.getPosX(0) - sortedTri.getPosX(2)) / 
            (sortedTri.getPosY(0) - sortedTri.getPosY(2));
        float dY21 = sortedTri.getPosY(1) - sortedTri.getPosY(2);

        float[] midPoint = new float[] {
            sortedTri.getPosX(2) + (invSlope20 * dY21),
            sortedTri.getPosY(1),
            0.0f // Z value can be ignored as interpolation uses unsplit tri
        };

        // flatTopTri is (1, mid, 2)
        Tri flatTopTri = new Tri(sortedTri);
        flatTopTri.swapVerts(0, 1); // (0 1 2) -> (1 0 2)
        flatTopTri.setPos(1, 0, midPoint); // (1 0 2) -> (1 mid 2)

        // flatBottomTri is (1, mid, 0)
        Tri flatBottomTri = new Tri(sortedTri);
        flatBottomTri.swapVerts(0, 1); // (0 1 2) -> (1 0 2)
        flatBottomTri.swapVerts(1, 2); // (1 0 2) -> (1 2 0)
        flatBottomTri.setPos(1, 0, midPoint); // (1 2 0) -> (1 mid 0)

        internalDrawFlatTopTri(flatTopTri, sortedTri);
        internalDrawFlatBottomTri(flatBottomTri, sortedTri);

    }

    private Tri internalSortTriVertsByHeight(Tri tri) { 

        if (tri.getPosY(2) > tri.getPosY(1)) {
            tri.swapVerts(2, 1);
        }
        if (tri.getPosY(1) > tri.getPosY(0)) {
            tri.swapVerts(1, 0);
        }
        if (tri.getPosY(2) > tri.getPosY(1)) {
            tri.swapVerts(2, 1);
        }

        return tri;

    }

    private void internalDrawFlatTopTri(Tri flatTri, Tri sortedTri) {
        
        // NOTE: 
        // - vertex arrangement is as follows:
        //   0 -> LEFT, 1 -> RIGHT, 2 -> BOTTOM POINT
        // - flattop triangle is drawn from bottom to top

        // sort top 2 verticies from left to right
        if (flatTri.getPosX(0) > flatTri.getPosX(1)) {
            flatTri.swapVerts(1, 0);
        }

        float invDY = 1.0f / (flatTri.getPosY(0) - flatTri.getPosY(2));
        if (Float.isNaN(invDY)) { return; }

        float invSlope20 = (flatTri.getPosX(0) - flatTri.getPosX(2)) * invDY;
        float invSlope21 = (flatTri.getPosX(1) - flatTri.getPosX(2)) * invDY;

        int Y_START = Math.max((int)flatTri.getPosY(2), 0);
        int Y_END   = Math.min((int)flatTri.getPosY(0), renderTarget.getHeight() - 1);

        for (int drawY = Y_START; drawY <= Y_END; drawY++) {

            float distY = Math.max(0.0f, drawY - flatTri.getPosY(2));
            int X_START = Math.max(
                (int)(flatTri.getPosX(2) + (invSlope20 * distY)),
                0
            );
            int X_END   = Math.min(
                (int)(flatTri.getPosX(2) + (invSlope21 * distY)), 
                renderTarget.getWidth() - 1
            );

            for (int drawX = X_START; drawX <= X_END; drawX++) {
                internalDrawFragment(drawX, drawY, sortedTri);
            }

        }

    }

    private void internalDrawFlatBottomTri(Tri flatTri, Tri sortedTri) {
        
        // NOTE: 
        // - vertex arrangement is as follows:
        //   0 -> LEFT, 1 -> RIGHT, 2 -> TOP POINT
        // - flatbottom triangle is drawn from bottom to top

        // sort bottom 2 verticies from left to right
        if (flatTri.getPosX(0) > flatTri.getPosX(1)) {
            flatTri.swapVerts(1, 0);
        }

        float invDY = 1.0f / (flatTri.getPosY(2) - flatTri.getPosY(0));
        if (Float.isNaN(invDY)) { return; }

        float invSlope02 = (flatTri.getPosX(2) - flatTri.getPosX(0)) * invDY;
        float invSlope12 = (flatTri.getPosX(2) - flatTri.getPosX(1)) * invDY;

        int Y_START = Math.max((int)flatTri.getPosY(0), 0);
        int Y_END   = Math.min((int)flatTri.getPosY(2), renderTarget.getHeight() - 1);

        for (int drawY = Y_START; drawY <= Y_END; drawY++) {

            float distY = Math.max(0.0f, drawY - flatTri.getPosY(0));
            int X_START = Math.max(
                (int)(flatTri.getPosX(0) + (invSlope02 * distY)),
                0
            );
            int X_END   = Math.min(
                (int)(flatTri.getPosX(1) + (invSlope12 * distY)), 
                renderTarget.getWidth() - 1
            );

            for (int drawX = X_START; drawX <= X_END; drawX++) {
                internalDrawFragment(drawX, drawY, sortedTri);
            }

        }

    }

    private void internalFindBaryWeights(
        int     drawX,
        int     drawY,
        Tri     tri,
        float[] out
    ) {
        // refer to 
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_rasterizetri.c

        float p0x = tri.getPosX(0);
        float p0y = tri.getPosY(0);
        float p1x = tri.getPosX(1);
        float p1y = tri.getPosY(1);
        float p2x = tri.getPosX(2);
        float p2y = tri.getPosY(2);
        float invDenom = 
            1.0f / 
            (((p1y - p2y) *
             (p0x - p2x)) +
            ((p2x - p1x) * 
             (p0y - p2y)));
        float d3x = (float)drawX - p2x;
        float d3y = (float)drawY - p2y;
        
        out[0] = ((p1y - p2y) * (d3x) + 
                 (p2x - p1x) * (d3y)) * 
                 invDenom;
        out[1] = ((p2y - p0y) * (d3x) + 
                 (p0x - p2x) * (d3y)) * 
                 invDenom;
        out[2] = 1.0f - out[1] - out[0];
    }

    private void internalFindScreenUV(
        float[] weights,
        Tri tri,
        float[] outUV
    ) {

        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_rasterizetri.c

        float w0      = tri.getPosZ(0) * weights[0];
        float w1      = tri.getPosZ(1) * weights[1];
        float w2      = tri.getPosZ(2) * weights[2];
        float invSumW = 1.0f / (w0 + w1 + w2);

        float interpU = 
            ((w0 * tri.getUV_U(0)) +
             (w1 * tri.getUV_U(1)) +
             (w2 * tri.getUV_U(2))) * invSumW;

        float interpV = 
            ((w0 * tri.getUV_V(0)) +
             (w1 * tri.getUV_V(1)) +
             (w2 * tri.getUV_V(2))) * invSumW;

        outUV[MESH_UV_OFST_U] = interpU;
        outUV[MESH_UV_OFST_V] = interpV;

    }

    private void internalDrawFragment(
        int drawX, 
        int drawY,
        Tri triangle
    ) {

        float[] weights = new float[MESH_VERTS_PER_TRI];
        float[] uvs     = new float[MESH_UV_NUM_CMPS];
        internalFindBaryWeights(drawX, drawY, triangle, weights);
        internalFindScreenUV(weights, triangle, uvs);

        float invDepth = 
            triangle.getPosZ(0) * weights[0] + 
            triangle.getPosZ(1) * weights[1] +
            triangle.getPosZ(2) * weights[2];

        int fragColor;
        switch (renderType) {
            case FlatColor:
                fragColor = fillColor;
                break;

            case Textured:
                fragColor = QShader.sampleTexture(
                    uvs[MESH_UV_OFST_U], 
                    uvs[MESH_UV_OFST_V],
                    texture,
                    sampleType
                ).toInt();
                break;

            case CustomShader:
                fragColor = customShader.fragmentShader(
                    drawX, 
                    drawY,
                    uvs[MESH_UV_OFST_U], 
                    uvs[MESH_UV_OFST_V],
                    texture,
                    new QColor(renderTarget.getColor(drawX, drawY)),
                    shaderInput
                ).toInt();
                break;

            case Depth:
                float depth  = 1.0f / invDepth;
                float factor = depth / RENDER_DEPTH_MAX_DEPTH;
                factor       = Math.max(0.0f, Math.min(1.0f, factor));
                factor       = factor * 255.0f;
                fragColor = new QColor(
                    (int)factor,
                    (int)factor,
                    (int)factor
                ).toInt();
                break;

            default:
                throw new QException(
                    PointOfError.BadState, 
                    "bad renderType: " + renderType.toString()
                );
        }

        // don't draw on transparent
        if ((fragColor >> COL_LSHIFT_OFST_A) == 0) {
            return;
        }

        // NOTE:
        // since all depths are negative and inverted, the further value
        // will be a smaller negative and hence greater. therefore the failing
        // depth test will be greater than the previous depth
        if (invDepth > renderTarget.getDepth(drawX, drawY) - DEPTH_TEST_EPSILON) {
            return;
        }

        renderTarget.setDepth(drawX, drawY, invDepth);
        renderTarget.setColor(drawX, drawY, fragColor);

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

}
