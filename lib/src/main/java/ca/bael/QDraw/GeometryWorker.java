// Bailey JT Brown
// 2023-2026
// GeometryWorker.java

package ca.bael.QDraw;

import static ca.bael.QDraw.QVector3.VCTR_INDEX_X;
import static ca.bael.QDraw.QVector3.VCTR_INDEX_Y;
import static ca.bael.QDraw.QVector3.VCTR_INDEX_Z;
import static ca.bael.QDraw.QVector3.VCTR_NUM_CMPS;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ca.bael.QDraw.QException.PointOfError;

final class GeometryWorker {
    ////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final float BACKFACE_CULL_MIN_DOT = 0.0f;

    ////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private QRenderBuffer renderTarget;
    private final TriangleQueue triQueue = new TriangleQueue();
    private final QShader.VertexShaderContext vctx = new QShader.VertexShaderContext();
    private final float[] edge01 = new float[VCTR_NUM_CMPS];
    private final float[] edge02 = new float[VCTR_NUM_CMPS];
    private final float[] rayToCenter = new float[VCTR_NUM_CMPS];
    private final Triangle workTri = new Triangle(0);
    private final VertexCache vertexCache = new VertexCache();

    ////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void process(List<DrawState> queuedDraws, AtomicInteger nextDraw, QRenderBuffer target) {
        renderTarget = target;
        triQueue.reset();
        for (int i = nextDraw.getAndIncrement(); i < queuedDraws.size(); i = nextDraw.getAndIncrement()) {
            internalProcessDraw(queuedDraws.get(i));
        }
    }

    ////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private void internalProcessDraw(DrawState draw) {
        if (renderTarget == null) {
            throw new QException(
                    PointOfError.BadState,
                    "Render target unassigned");
        }
        QAttribBuffer[] slotAttribs = draw.state.slotAttribs;
        int[] indicies = draw.state.indexBuffer.getIndicies();
        int triCount = draw.state.indexBuffer.getTriCount();

        int vertCount = 0;
        for (int index : indicies) {
            if (index < 0) {
                throw new QException(
                        PointOfError.InvalidData,
                        "Negative vertex index: " + index);
            }
            vertCount = Math.max(vertCount, index + 1);
        }
        for (QAttribBuffer attribs : slotAttribs) {
            if (attribs != null && attribs.getAttribCount() < vertCount) {
                throw new QException(
                        PointOfError.InvalidData,
                        "Vertex index " + (vertCount - 1) +
                        " exceeds attribute buffer of size " + attribs.getAttribCount());
            }
        }

        vctx.uniforms = draw.state.slotUniforms;
        vctx.textures = draw.state.slotTextures;
        vctx.layout = draw.layout;
        vctx.drawData = null;
        vctx.attributes = new float[slotAttribs.length][];
        for (int slot = 0; slot < slotAttribs.length; slot++) {
            if (slotAttribs[slot] != null) {
                vctx.attributes[slot] = new float[slotAttribs[slot].getComponentsPerAttrib()];
            }
        }

        for (Vertex vert : workTri.verts) {
            vert.posn = new float[VCTR_NUM_CMPS];
            vert.shaderOutputs = new float[draw.layout.total];
        }

        vertexCache.prepare(indicies, vertCount, draw.layout.total);

        draw.data = draw.shader.beginDraw(vctx);
        vctx.drawData = draw.data;

        draw.out = triQueue;
        draw.triStart = triQueue.triEnd;

        for (int triNum = 0; triNum < triCount; triNum++) {
            // GENERATE TRIANGLE
            Triangle tri = workTri;
            tri.triNum = triNum;
            internalProcessVertex(tri, 0, draw);
            internalProcessVertex(tri, 1, draw);
            internalProcessVertex(tri, 2, draw);

            // GENERATE TRIANGLE NORMAL
            QVector3.copy(edge01, tri.getPosn(1));
            QVector3.sub(edge01, tri.getPosn(0));
            QVector3.copy(edge02, tri.getPosn(2));
            QVector3.sub(edge02, tri.getPosn(0));
            QVector3.cross(tri.normal, edge01, edge02);

            // CULL BY FACING
            if (internalCheckCulled(tri, draw.state.cullMode)) {
                continue;
            }

            QVector3.scale(tri.normal, 1.0f / QVector3.magnitude(tri.normal));

            // CLIP AND RENDER
            float nearClip = draw.state.nearClip;
            if (tri.getPosnZ(0) <= nearClip &&
                tri.getPosnZ(1) <= nearClip &&
                tri.getPosnZ(2) <= nearClip) {
                internalEmitTri(tri, true, draw);
                continue;
            }

            if (tri.getPosnZ(0) > nearClip &&
                tri.getPosnZ(1) > nearClip &&
                tri.getPosnZ(2) > nearClip) {
                continue;
            }

            Triangle[] clipTris = TriangleClipper.clip(tri, nearClip);
            for (Triangle clippedTri : clipTris) {
                internalEmitTri(clippedTri, false, draw);
            }
        }

        draw.triEnd = triQueue.triEnd;
    }

    private void internalEmitTri(Triangle tri, boolean shared, DrawState draw) {
        ViewerState view = draw.state;
        if (internalCheckOutside(tri, VCTR_INDEX_X, view.viewLeft, view.viewRight) ||
            internalCheckOutside(tri, VCTR_INDEX_Y, view.viewBottom, view.viewTop)) {
            return;
        }

        int v0 = internalEmitVertex(tri, 0, shared, draw.state);
        int v1 = internalEmitVertex(tri, 1, shared, draw.state);
        int v2 = internalEmitVertex(tri, 2, shared, draw.state);
        int n  = triQueue.allocVerts(VCTR_NUM_CMPS);
        System.arraycopy(tri.normal, 0, triQueue.verts, n, VCTR_NUM_CMPS);
        triQueue.addTri(v0, v1, v2, n);
        float[] v = triQueue.verts;
        draw.minY = Math.min(draw.minY, Math.min(v[v0 + VCTR_INDEX_Y], Math.min(v[v1 + VCTR_INDEX_Y], v[v2 + VCTR_INDEX_Y])));
        draw.maxY = Math.max(draw.maxY, Math.max(v[v0 + VCTR_INDEX_Y], Math.max(v[v1 + VCTR_INDEX_Y], v[v2 + VCTR_INDEX_Y])));
    }

    private int internalEmitVertex(Triangle tri, int triVertNum, boolean shared, ViewerState view) {
        int idx    = shared ? vertexCache.indexOf(tri.triNum, triVertNum) : -1;
        int offset = vertexCache.emitted(idx);
        if (offset >= 0) {
            return offset;
        }

        Vertex vert  = tri.verts[triVertNum];
        int    total = vert.shaderOutputs.length;
        offset = triQueue.allocVerts(VCTR_NUM_CMPS + total);

        float[] out  = triQueue.verts;
        float   invZ = 1.0f / vert.posn[VCTR_INDEX_Z];
        out[offset + VCTR_INDEX_X] = (vert.posn[VCTR_INDEX_X] * -invZ - view.viewLeft) *
                (renderTarget.getWidth() / (view.viewRight - view.viewLeft));
        out[offset + VCTR_INDEX_Y] = (vert.posn[VCTR_INDEX_Y] * -invZ - view.viewBottom) *
                (renderTarget.getHeight() / (view.viewTop - view.viewBottom));
        out[offset + VCTR_INDEX_Z] = invZ;
        System.arraycopy(vert.shaderOutputs, 0, out, offset + VCTR_NUM_CMPS, total);

        vertexCache.setEmitted(idx, offset);
        return offset;
    }

    private void internalProcessVertex(Triangle tri, int triVertNum, DrawState draw) {
        Vertex vert = tri.verts[triVertNum];
        int    idx  = vertexCache.indexOf(tri.triNum, triVertNum);
        if (vertexCache.load(idx, vert)) {
            return;
        }

        vctx.written = 0;
        vctx.outputs = vert.shaderOutputs;

        // POPULATE VERTEX SHADER INPUTS
        QAttribBuffer[] slotAttribs = draw.state.slotAttribs;
        for (int slot = 0; slot < slotAttribs.length; slot++) {
            // skip if slot is unused
            QAttribBuffer attribs = slotAttribs[slot];
            if (attribs == null) {
                continue;
            }

            // populate attribute slot using vertex index
            attribs.getAttrib(0, vctx.attributes[slot], idx);
        }

        // CALL SHADER
        QVector3.copy(vert.posn, draw.shader.vertexShader(vctx).getComponents());
        vctx.ensureOutputsWritten();
        vertexCache.store(idx, vert);
    }

    private boolean internalCheckCulled(Triangle tri, QViewer.CullMode cullMode) {
        if (cullMode == QViewer.CullMode.None) {
            return false;
        }
        // NOTE:
        // if the dot product between the normal is > 0 for the ray
        // from the camera to the center of the tri, then it's backfacing
        QVector3.copy(rayToCenter, tri.getPosn(0));
        QVector3.add(rayToCenter, tri.getPosn(1));
        QVector3.add(rayToCenter, tri.getPosn(2));
        float facing = QVector3.dot(tri.normal, rayToCenter);
        return cullMode == QViewer.CullMode.Back ? facing > BACKFACE_CULL_MIN_DOT : facing < -BACKFACE_CULL_MIN_DOT;
    }

    private static boolean internalCheckOutside(Triangle tri, int axis, float lo, float hi) {
        float a = tri.getPosn(0)[axis] * -(1.0f / tri.getPosnZ(0));
        float b = tri.getPosn(1)[axis] * -(1.0f / tri.getPosnZ(1));
        float c = tri.getPosn(2)[axis] * -(1.0f / tri.getPosnZ(2));
        return (a < lo && b < lo && c < lo) || (a > hi && b > hi && c > hi);
    }
}
