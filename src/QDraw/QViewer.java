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

    public static final float DEFAULT_VIEWBOUND_LEFT   = -1.0f;
    public static final float DEFAULT_VIEWBOUND_RIGHT  = 1.0f;
    public static final float DEFAULT_VIEWBOUND_BOTTOM = -1.0f;
    public static final float DEFAULT_VIEWBOUND_TOP    = 1.0f;

    public static final int SHADER_UNIFORM_SLOTS       = 16;
    public static final int SHADER_TEXTURE_SLOTS       = 16;
    public static final int SHADER_VERTEX_ATTRIB_SLOTS = 8;

    private static final float BACKFACE_CULL_MIN_DOT  = 0.5f;
    private static final float DEPTH_TEST_EPSILON     = 0.002f;

    private static final int VERTS_PER_TRI = 3;

    /////////////////////////////////////////////////////////////////
    // PUBLIC ENUMS
    public enum RenderMode {
        Fill,
        Textured,
        Normal,
        Depth,
        CustomShader
    };

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private float         nearClip     = DEFAULT_NEAR_CLIP;
    private float         viewLeft     = DEFAULT_VIEWBOUND_LEFT;
    private float         viewRight    = DEFAULT_VIEWBOUND_RIGHT;
    private float         viewBottom   = DEFAULT_VIEWBOUND_BOTTOM;
    private float         viewTop      = DEFAULT_VIEWBOUND_TOP;
    private QRenderBuffer renderTarget = null;
    private QShader       shader = null;

    private Object[]   slotUniforms      = new Object[SHADER_UNIFORM_SLOTS];
    private QTexture[] slotTextures      = new QTexture[SHADER_TEXTURE_SLOTS];
    private QAttribIndexer[] slotAttribs = new QAttribIndexer[SHADER_VERTEX_ATTRIB_SLOTS];  

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

    public void setShader(QShader _shader) {
        shader = _shader;
    }

    public void setUniformSlot(
        int    slot,
        Object uniform
    ) {
        slotUniforms[slot] = uniform;
    }

    public void clearUniformSlots( ) {
        for (int i = 0; i < slotUniforms.length; i++) {
            slotUniforms[i] = null;
        }
    }

    public void setTextureSlot(
        int      slot,
        QTexture texture
    ) {
        slotTextures[slot] = texture;
    }

    public void clearTextureSlots( ) {
        for (int i = 0; i < slotTextures.length; i++) {
            slotTextures[i] = null;
        }
    }

    public void setVertexAttribSlot(
        int            slot,
        QAttribIndexer indexer
    ) {
        slotAttribs[slot] = indexer;
    }

    public void clearVertexAttribSlots( ) {
        for (int i = 0; i < slotAttribs.length; i++) {
            slotAttribs[i] = null;
        }
    }

    public void clearFrame( ) {
        renderTarget.clearColorBuffer();
        renderTarget.clearDepthBuffer();
    }

    public void draw( ) {
        internalDraw( );
    }

    public void drawMesh(
        QMesh      mesh,
        QMatrix4x4 meshTransform
    ) {
        clearTextureSlots( );
        clearUniformSlots( );
        clearVertexAttribSlots( );

        setUniformSlot(0, meshTransform);
        // setTextureSlot(0, texture);
        setVertexAttribSlot(0, mesh.getPosIndexer( ));
        setVertexAttribSlot(1, mesh.getUVIndexer( ));
        setVertexAttribSlot(2, mesh.getNormalIndexer( ));

        // TODO: remove and complete
        QShader tempShader = new QShader() {
            public QVector3 vertexShader(
                VertexShaderContext context
            ) {
                QMatrix4x4 transform = (QMatrix4x4)context.uniforms[0];
                QVector3   pos = new QVector3(context.attributes[0]);
                return QMatrix4x4.multiply(transform, pos);
            }

            public QColor fragmentShader(
                FragmentShaderContext context
            ) { 
                return null;
            }
        };

        setShader(tempShader);
        draw( );
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE CLASSES
    private static class Vertex {
        public float[]   posn;
        public float[][] shaderOutputs;

        public void project( ) {
            posn[VCTR_INDEX_Z] = 1.0f / posn[VCTR_INDEX_Z];
            QMath.mult2(0, posn, -posn[VCTR_INDEX_Z]);
        }

        public void findClippedShaderOutputs(
            Vertex vISrc,
            Vertex vFSrc,
            float  factor
        ) {
            // NOTE:
            //   vISrc and vFSrc should have identically formatted shader outputs,
            //   that is, the slots are consistent in usage and component count,
            //   so when initializing the new shader outputs, we arbitrarily use vISrc
            //   as a reference
            shaderOutputs = new float[vISrc.shaderOutputs.length][];
            float facI = 1.0f - factor;
            float facF = factor;

            for (int i = 0; i < shaderOutputs.length; i++) {
                
                if (vISrc.shaderOutputs[i] == null) { continue; }
                shaderOutputs[i] = new float[vISrc.shaderOutputs[i].length];

                for (int comp = 0; comp < shaderOutputs[i].length; comp++) {
                    shaderOutputs[i][comp] = 
                        vISrc.shaderOutputs[i][comp] * facI + 
                        vFSrc.shaderOutputs[i][comp] * facF;
                }
            }
        }

        public Vertex( ) {
            posn = QMath.new3( );
        }

        public Vertex(Vertex toCopy) {
            posn = QMath.new3();
            QMath.copy3(
                posn, 
                toCopy.posn
            );

            shaderOutputs = new float[toCopy.shaderOutputs.length][];
            for (int i = 0; i < shaderOutputs.length; i++) {
                if (shaderOutputs[i] == null) { continue; }

                shaderOutputs[i] = new float[toCopy.shaderOutputs[i].length];
                System.arraycopy(
                    toCopy.shaderOutputs[i], 
                    0, 
                    shaderOutputs[i], 
                    0, 
                    toCopy.shaderOutputs.length
                );
            }
        }
    }

    private static class Triangle {
        public static final int VERTS_PER_TRI = 3;

        public int      triNum;
        public Vertex[] verts  = new Vertex[] {
            new Vertex( ), new Vertex( ), new Vertex( )
        };
        public float[]  normal = new float[VCTR_NUM_CMPS];

        public void swapVerts(int v0, int v1) {
            Vertex temp = verts[v0];
            verts[v0]   = verts[v1];
            verts[v1]   = temp;
        }

        public float[] getPosn(int vertNum) {
            return verts[vertNum].posn;
        }

        public void setPosn(int vertNum, float[] inVals) {
            QMath.copy3(verts[vertNum].posn, inVals);
        }

        public float getPosnX(int vertNum) {
            return getPosn(vertNum)[VCTR_INDEX_X];
        }

        public void setPosnX(int vertNum, float val) {
            getPosn(vertNum)[VCTR_INDEX_X] = val;
        }

        public float getPosnY(int vertNum) {
            return getPosn(vertNum)[VCTR_INDEX_Y];
        }

        public void setPosnY(int vertNum, float val) {
            getPosn(vertNum)[VCTR_INDEX_Y] = val;
        }

        public float getPosnZ(int vertNum) {
            return getPosn(vertNum)[VCTR_INDEX_Z];
        }

        public void setPosnZ(int vertNum, float val) {
            getPosn(vertNum)[VCTR_INDEX_Z] = val;
        }

        public Vertex getVertex(int vertNum) {
            return verts[vertNum];
        }

        public Triangle(int _num) {
            triNum = _num;
        }

        public Triangle(Triangle toCopy) {
            triNum = toCopy.triNum;
            normal = new float[VCTR_NUM_CMPS];
            QMath.copy3(normal, toCopy.normal);

            verts = new Vertex[VERTS_PER_TRI];
            for (int i = 0; i < toCopy.verts.length; i++) {
                verts[i] = new Vertex(toCopy.verts[i]);
            }
        }
    }

    private static class ClipState {
        public int       numVertsBehind     = 0;
        public boolean[] vertBehindState    = new boolean[VERTS_PER_TRI];
        public int[]     vertBehindIndicies = new int[VERTS_PER_TRI];

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
    }

    private void internalDraw( ) {
        if (renderTarget == null) {
            throw new QException(
                PointOfError.BadState, 
                "Render target unassigned"
            );
        }

        if (shader == null) {
            throw new QException(
                PointOfError.BadState, 
                "Shader not set"
            );
        }

        // ENSURE ALL INDEXERS HAVE THE SAME TRI COUNT
        int triCount = -1;
        for (int slot = 0; slot < slotAttribs.length; slot++) {
            if (slotAttribs[slot] == null) continue;
            if (triCount == -1) {
                triCount = slotAttribs[slot].getTriCount();
                continue;
            }
            if (slotAttribs[slot].getTriCount() != triCount) {
                throw new QException(
                    PointOfError.BadState, 
                    "Not all vertex attribute slots are the same length."
                );
            }
        }
        if (triCount == -1) {
            throw new QException(
                PointOfError.BadState, 
                "No vertex attribute slots set!"
            );
        }

        // TODO: clean up
        for (int triNum = 0; triNum < triCount; triNum++) {
            // generate tri
            Triangle tri = new Triangle(triNum);
            internalProcessVertex(tri, 0);
            internalProcessVertex(tri, 1);
            internalProcessVertex(tri, 2);

            float[] d01 = new float[VCTR_NUM_CMPS];
            QMath.copy3(d01, tri.getPosn(1));
            QMath.sub3(d01, tri.getPosn(0));

            float[] d02 = new float[VCTR_NUM_CMPS];
            QMath.copy3(d02, tri.getPosn(2));
            QMath.sub3(d02, tri.getPosn(0));

            float[] tempNormal = QMath.cross3(d01, d02);
            QMath.mult3(tempNormal, 1.0f / QMath.fastmag3(tempNormal));
            QMath.copy3(tri.normal, tempNormal);

            // TODO: clean this up!!!

            // test for clipping
            if (internalCheckBackfacing(tri)) { continue; }

            Triangle[] clipTris = internalClipTri(tri);
            for (Triangle clippedTri : clipTris) {
                internalViewTri(clippedTri);
            }

        }

    }

    private void internalProcessVertex(Triangle tri, int triVertNum) {
        QShader.VertexShaderContext vctx = new QShader.VertexShaderContext();
        vctx.triNum    = tri.triNum; 
        vctx.vertexNum = tri.triNum * VERTS_PER_TRI + triVertNum;
        vctx.uniforms  = slotUniforms;
        vctx.textures  = slotTextures;
        vctx.attributes = new float[slotAttribs.length][];

        for (int slot = 0; slot < slotAttribs.length; slot++) {
            QAttribIndexer indexer = slotAttribs[slot];
            if (indexer == null) { continue; }

            vctx.attributes[slot] = new float[indexer.getComponentsPerAttrib( )];
            indexer.index(
                tri.triNum, 
                triVertNum, 
                0, 
                vctx.attributes[slot]
            );
        }

        QVector3 vertShaderOut              = shader.vertexShader(vctx);
        tri.verts[triVertNum].posn          = vertShaderOut.getComponents();
        tri.verts[triVertNum].shaderOutputs = vctx.outputsToFragShader; 
    }

    private boolean internalCheckBackfacing(Triangle tri) {
        float[] awayAxis = { 0.0f, 0.0f, -1.0f };
        return (QMath.dot3(tri.normal, awayAxis) > BACKFACE_CULL_MIN_DOT);
    }

    private Triangle[] internalClipTri(Triangle tri) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c

        ClipState clipState = new ClipState();

        if (tri.getPosnZ(0) > nearClip) {
            clipState.vertBehindIndicies[clipState.numVertsBehind++] = 0;
            clipState.vertBehindState[0] = true;
        }

        if (tri.getPosnZ(1) > nearClip) {
            clipState.vertBehindIndicies[clipState.numVertsBehind++] = 1;
            clipState.vertBehindState[1] = true;
        }

        if (tri.getPosnZ(2) > nearClip) {
            clipState.vertBehindIndicies[clipState.numVertsBehind++] = 2;
            clipState.vertBehindState[2] = true;
        }

        switch (clipState.numVertsBehind) {
            // ALL VERTS BEFORE
            // triangle is not clipped
            case 0:
                return new Triangle[] { tri };
            
            // ALL VERTS BEHIND
            // triangle should be culled
            case 3:
                return new Triangle[0];

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
        Triangle srcTri,
        int pI,
        int pF,
        float[] out
    ) {
        internalFindClipIntersect(
            srcTri.getPosn(pI),
            srcTri.getPosn(pF),
            out
        );
    }

    private void internalFindClipIntersect(
        float[] pI,
        float[] pF,
        float[] out
    ) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c

        float invDZ   = 1.0f / (pF[VCTR_INDEX_Z] - pI[VCTR_INDEX_Z]);
        float slopeXZ = (pF[VCTR_INDEX_X] - pI[VCTR_INDEX_X]) * invDZ;
        float slopeYZ = (pF[VCTR_INDEX_Y] - pI[VCTR_INDEX_Y]) * invDZ;
        float dClip   = (nearClip - pI[VCTR_INDEX_Z]);

        out[VCTR_INDEX_X] = pI[VCTR_INDEX_X] + slopeXZ * dClip;
        out[VCTR_INDEX_Y] = pI[VCTR_INDEX_Y] + slopeYZ * dClip;
        out[VCTR_INDEX_Z] = nearClip;
    }

    private float internalFindClipInterpolationFactor(
        float[] pI,
        float[] pF,
        float[] pClip
    ) {
        float[] temp = QMath.new3( );
        QMath.copy3(temp, pF);
        QMath.sub3(temp, pI);
        float magTotal = QMath.fastmag3(temp);

        QMath.copy3(temp, pClip);
        QMath.sub3(temp, pI);
        float magIntersect = QMath.fastmag3(temp);

        return magIntersect / magTotal;
    }

    private Triangle[] internalClipTriCase1(Triangle tri, ClipState clipState) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c

        // NOTE:
        // - all faces must be constructed in CLOCKWISE winding order.
        // - when 1 vertex is clipped, generated face is a quad
        // - triangle will be re-shuffled so that it remains CLOCKWISE where pos2 is clipped
        // - (this is slightly different from Casesium where pos0 is clipped)

        Triangle shuffledTri = new Triangle(tri);

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

        float[] pos02 = new float[VCTR_NUM_CMPS];
        float[] pos12 = new float[VCTR_NUM_CMPS];
        internalFindClipIntersect(shuffledTri, 0, 2, pos02);
        internalFindClipIntersect(shuffledTri, 1, 2, pos12);

        // NOTE:
        // - when 1 vertex is clipped, the resulting mesh is a quad
        // - our quad is 0/2, 0, 1, 1/2 (where a/b is clipped interpolation),
        //   which will be tesselated as (0/2, 0, 1), (0/2, 1, 1/2)

        Triangle quadTri0 = new Triangle(shuffledTri);
        quadTri0.setPosn(2, pos02); // (0 1 2) -> (0 1 0/2)
        quadTri0.swapVerts(0, 2); // (0 1 0/2) -> (0/2 1 0)
        quadTri0.swapVerts(1, 2); // (0/2 1 0) -> (0/2 0 1)
        
        Triangle quadTri1 = new Triangle(shuffledTri); // (0 1 2)
        quadTri1.setPosn(0, pos02); // (0 1 2)   -> (0/2 1 2)
        quadTri1.setPosn(2, pos12); // (0/2 1 2) -> (0/2 1 1/2)
        
        return new Triangle[] { quadTri0, quadTri1 };

    }

    private Triangle[] internalClipTriCase2(Triangle tri, ClipState clipState) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c

        // TESSELATE TRIANGLE BASED ON 2 CLIPPED VERTICIES
        switch (clipState.vertBehindIndicies[0] + clipState.vertBehindIndicies[1]) {
            // VERTS 1 & 2 ARE CLIPPED
            case (1 + 2):

                float[] pos10 = new float[VCTR_NUM_CMPS];
                float[] pos20 = new float[VCTR_NUM_CMPS];
                internalFindClipIntersect(tri, 1, 0, pos10);
                internalFindClipIntersect(tri, 2, 0, pos20);
                float fac10 = internalFindClipInterpolationFactor(
                    tri.getPosn(1), 
                    tri.getPosn(0), 
                    pos10
                );
                float fac20 = internalFindClipInterpolationFactor(
                    tri.getPosn(2), 
                    tri.getPosn(0), 
                    pos10
                );
                
                tri.setPosn(1, pos10);
                tri.getVertex(1).findClippedShaderOutputs(
                    tri.getVertex(1), 
                    tri.getVertex(0), 
                    fac10
                );
                tri.setPosn(2, pos20);
                tri.getVertex(2).findClippedShaderOutputs(
                    tri.getVertex(2), 
                    tri.getVertex(0), 
                    fac20
                );

                return new Triangle[] { tri };

            // VERTS 0 & 2 ARE CLIPPED
            case (0 + 2):

                float[] pos01 = new float[VCTR_NUM_CMPS];
                float[] pos21 = new float[VCTR_NUM_CMPS];
                internalFindClipIntersect(tri, 0, 1, pos01);
                internalFindClipIntersect(tri, 2, 1, pos21);
                float fac01 = internalFindClipInterpolationFactor(
                    tri.getPosn(0), 
                    tri.getPosn(1), 
                    pos01
                );
                float fac21 = internalFindClipInterpolationFactor(
                    tri.getPosn(2), 
                    tri.getPosn(1), 
                    pos21
                );
                
                tri.setPosn(0, pos01);
                tri.getVertex(0).findClippedShaderOutputs(
                    tri.getVertex(0), 
                    tri.getVertex(1), 
                    fac01
                );
                tri.setPosn(2, pos21);
                tri.getVertex(2).findClippedShaderOutputs(
                    tri.getVertex(2), 
                    tri.getVertex(1), 
                    fac21
                );

                return new Triangle[] { tri };

            // VERTS 0 & 1 ARE CLIPPED
            case (0 + 1):

                float[] pos02 = new float[VCTR_NUM_CMPS];
                float[] pos12 = new float[VCTR_NUM_CMPS];
                internalFindClipIntersect(tri, 0, 2, pos02);
                internalFindClipIntersect(tri, 1, 2, pos12);
                float fac02 = internalFindClipInterpolationFactor(
                    tri.getPosn(0), 
                    tri.getPosn(2), 
                    pos02
                );
                float fac12 = internalFindClipInterpolationFactor(
                    tri.getPosn(1), 
                    tri.getPosn(2), 
                    pos12
                );
                
                tri.setPosn(0, pos02);
                tri.getVertex(0).findClippedShaderOutputs(
                    tri.getVertex(0), 
                    tri.getVertex(2), 
                    fac02
                );
                tri.setPosn(1, pos12);
                tri.getVertex(1).findClippedShaderOutputs(
                    tri.getVertex(1), 
                    tri.getVertex(2), 
                    fac12
                );

                return new Triangle[] { tri };
        
            // BAD STATE
            default:
                throw new QException(
                    PointOfError.BadState, 
                    "Bad clip state:" + clipState.toString()
                );
        }

    }

    private void internalMapVertToScreenSpace(Triangle srcTri, int vertNum) {
        // NOTE:
        //  - this transformation will map (left, right) -> (0, targetWidth) and
        //    (bottom, top) -> (0, targetheight). this is essentially a worldspace
        //    to screenspace transformation
        //  - in order to do this, the space is translated such that the bottom left
        //    corner is (0, 0), and then it is scaled to fit the screenspace

        srcTri.setPosnX(vertNum, srcTri.getPosnX(vertNum) - viewLeft);
        srcTri.setPosnY(vertNum, srcTri.getPosnY(vertNum) - viewBottom);

        srcTri.setPosnX(vertNum, 
            srcTri.getPosnX(vertNum) * 
            (renderTarget.getWidth()  / (viewRight - viewLeft)));
        srcTri.setPosnY(vertNum, 
            srcTri.getPosnY(vertNum) * 
            (renderTarget.getHeight() / (viewTop - viewBottom)));

    }

    private void internalViewTri(Triangle tri) {
        
        // NOTE:
        // - from this point forward, all z values will be inverted
        tri.getVertex(0).project();
        tri.getVertex(1).project();
        tri.getVertex(2).project();

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

        Triangle sortedTri = internalSortTriVertsByHeight(tri);

        float invSlope20 =
            (sortedTri.getPosnX(0) - sortedTri.getPosnX(2)) / 
            (sortedTri.getPosnY(0) - sortedTri.getPosnY(2));
        float dY21 = sortedTri.getPosnY(1) - sortedTri.getPosnY(2);

        float[] midPoint = new float[] {
            sortedTri.getPosnX(2) + (invSlope20 * dY21),
            sortedTri.getPosnY(1),
            0.0f // Z value can be ignored as interpolation uses unsplit tri
        };

        // flatTopTri is (1, mid, 2)
        Triangle flatTopTri = new Triangle(sortedTri);
        flatTopTri.swapVerts(0, 1); // (0 1 2) -> (1 0 2)
        flatTopTri.setPosn(1, midPoint); // (1 0 2) -> (1 mid 2)

        // flatBottomTri is (1, mid, 0)
        Triangle flatBottomTri = new Triangle(sortedTri);
        flatBottomTri.swapVerts(0, 1); // (0 1 2) -> (1 0 2)
        flatBottomTri.swapVerts(1, 2); // (1 0 2) -> (1 2 0)
        flatBottomTri.setPosn(1, midPoint); // (1 2 0) -> (1 mid 0)

        internalDrawFlatTopTri(flatTopTri, sortedTri);
        internalDrawFlatBottomTri(flatBottomTri, sortedTri);

    }

    private Triangle internalSortTriVertsByHeight(Triangle tri) { 

        if (tri.getPosnY(2) > tri.getPosnY(1)) {
            tri.swapVerts(2, 1);
        }
        if (tri.getPosnY(1) > tri.getPosnY(0)) {
            tri.swapVerts(1, 0);
        }
        if (tri.getPosnY(2) > tri.getPosnY(1)) {
            tri.swapVerts(2, 1);
        }

        return tri;

    }

    private void internalDrawFlatTopTri(Triangle flatTri, Triangle sortedTri) {
        
        // NOTE: 
        // - vertex arrangement is as follows:
        //   0 -> LEFT, 1 -> RIGHT, 2 -> BOTTOM POINT
        // - flattop triangle is drawn from bottom to top

        // sort top 2 verticies from left to right
        if (flatTri.getPosnX(0) > flatTri.getPosnX(1)) {
            flatTri.swapVerts(1, 0);
        }

        float invDY = 1.0f / (flatTri.getPosnY(0) - flatTri.getPosnY(2));
        if (Float.isNaN(invDY)) { return; }

        float invSlope20 = (flatTri.getPosnX(0) - flatTri.getPosnX(2)) * invDY;
        float invSlope21 = (flatTri.getPosnX(1) - flatTri.getPosnX(2)) * invDY;

        int Y_START = Math.max((int)flatTri.getPosnY(2), 0);
        int Y_END   = Math.min((int)flatTri.getPosnY(0), renderTarget.getHeight() - 1);

        for (int drawY = Y_START; drawY <= Y_END; drawY++) {

            float distY = Math.max(0.0f, drawY - flatTri.getPosnY(2));
            int X_START = Math.max(
                (int)(flatTri.getPosnX(2) + (invSlope20 * distY)),
                0
            );
            int X_END   = Math.min(
                (int)(flatTri.getPosnX(2) + (invSlope21 * distY)), 
                renderTarget.getWidth() - 1
            );

            for (int drawX = X_START; drawX <= X_END; drawX++) {
                internalDrawFragment(drawX, drawY, sortedTri);
            }

        }

    }

    private void internalDrawFlatBottomTri(Triangle flatTri, Triangle sortedTri) {
        
        // NOTE: 
        // - vertex arrangement is as follows:
        //   0 -> LEFT, 1 -> RIGHT, 2 -> TOP POINT
        // - flatbottom triangle is drawn from bottom to top

        // sort bottom 2 verticies from left to right
        if (flatTri.getPosnX(0) > flatTri.getPosnX(1)) {
            flatTri.swapVerts(1, 0);
        }

        float invDY = 1.0f / (flatTri.getPosnY(2) - flatTri.getPosnY(0));
        if (Float.isNaN(invDY)) { return; }

        float invSlope02 = (flatTri.getPosnX(2) - flatTri.getPosnX(0)) * invDY;
        float invSlope12 = (flatTri.getPosnX(2) - flatTri.getPosnX(1)) * invDY;

        int Y_START = Math.max((int)flatTri.getPosnY(0), 0);
        int Y_END   = Math.min((int)flatTri.getPosnY(2), renderTarget.getHeight() - 1);

        for (int drawY = Y_START; drawY <= Y_END; drawY++) {

            float distY = Math.max(0.0f, drawY - flatTri.getPosnY(0));
            int X_START = Math.max(
                (int)(flatTri.getPosnX(0) + (invSlope02 * distY)),
                0
            );
            int X_END   = Math.min(
                (int)(flatTri.getPosnX(1) + (invSlope12 * distY)), 
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
        Triangle     tri,
        float[] out
    ) {
        // refer to 
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_rasterizetri.c

        float p0x = tri.getPosnX(0);
        float p0y = tri.getPosnY(0);
        float p1x = tri.getPosnX(1);
        float p1y = tri.getPosnY(1);
        float p2x = tri.getPosnX(2);
        float p2y = tri.getPosnY(2);
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
        out[0] = Math.min(1.0f, Math.max(0.0f, out[0])); // clamp

        out[1] = ((p2y - p0y) * (d3x) + 
                 (p0x - p2x) * (d3y)) * 
                 invDenom;
        out[1] = Math.min(1.0f, Math.max(0.0f, out[1])); // clamp

        out[2] = 1.0f - out[1] - out[0];
    }

    private void internalDrawFragment(
        int drawX, 
        int drawY,
        Triangle triangle
    ) {

        float[] weights = new float[VERTS_PER_TRI];
        internalFindBaryWeights(drawX, drawY, triangle, weights);

        float invDepth = 
            triangle.getPosnZ(0) * weights[0] + 
            triangle.getPosnZ(1) * weights[1] +
            triangle.getPosnZ(2) * weights[2];

        int fragColor = 0xFFFFFFFF; // TODO: finish

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
        renderTarget.setColor(drawX, drawY, QColor.White().toInt());
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
