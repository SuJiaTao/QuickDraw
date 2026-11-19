// Bailey JT Brown
// 2023-2024
// QViwer.java

package ca.bael.QDraw;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import ca.bael.QDraw.QException.PointOfError;
import ca.bael.QDraw.QSampleable.SampleType;
import ca.bael.QDraw.QShader.ShaderRequirement;
import ca.bael.QDraw.QShader.ShaderRequirement.RequirementType;

public final class QViewer extends QEncoding {
    ////////////////////////////////////////////////////////////////
    // PUBLIC ENUMS
    public enum RenderMode {
        SolidFill,
        Textured,
        Normal,
        Material,
        CustomShader
    };

    ////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final float MIN_NEAR_CLIP = -0.005f;
    public static final float DEFAULT_NEAR_CLIP = MIN_NEAR_CLIP;

    public static final float DEFAULT_VIEWBOUND_LEFT = -1.0f;
    public static final float DEFAULT_VIEWBOUND_RIGHT = 1.0f;
    public static final float DEFAULT_VIEWBOUND_BOTTOM = -1.0f;
    public static final float DEFAULT_VIEWBOUND_TOP = 1.0f;

    private static final float BACKFACE_CULL_MIN_DOT = 0.0f;

    public static final int SHADER_UNIFORM_SLOTS = 16;
    public static final int SHADER_TEXTURE_SLOTS = 16;
    public static final int SHADER_VERTEX_ATTRIB_SLOTS = 8;

    public static final RenderMode DEFAULT_RENDER_MODE = RenderMode.Textured;
    public static final QColor DEFAULT_FILL_COLOR = QColor.White();
    private static final int RASTER_THREADS = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService RASTER_POOL = Executors.newFixedThreadPool(RASTER_THREADS, task -> {
        Thread thread = new Thread(task, "QViewer-raster");
        thread.setDaemon(true);
        return thread;
    });
    public static final SampleType DEFAULT_SAMPLE_TYPE = SampleType.Repeat;
    public static final int DEFAULT_SHADER_POSITION_SLOT = 0;
    public static final int DEFAULT_SHADER_UV_SLOT = 1;
    public static final int DEFAULT_SHADER_NORMAL_SLOT = 2;
    public static final int DEFAULT_SHADER_MATRIX_SLOT = 0;
    public static final int DEFAULT_SHADER_LIGHTS_SLOT = 1;
    public static final int DEFAULT_SHADER_TEXTURE_SLOT = 0;
    public static final int DEFAULT_SHADER_MATERIAL_SLOT = 2;

    ////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private final QShader SOLIDFILL_SHADER = BuiltinShaders.solidFill( );
    private final QShader TEXTURED_SHADER  = BuiltinShaders.textured( );
    private final QShader NORMAL_SHADER    = BuiltinShaders.normal( );
    private final QShader MATERIAL_SHADER  = BuiltinShaders.material( );

    private final ViewerState state = new ViewerState();
    private QRenderBuffer renderTarget = null;
    private QShader customShader = null;
    private RenderMode renderMode = DEFAULT_RENDER_MODE;

    private final QShader.VertexShaderContext vctx = new QShader.VertexShaderContext();
    private final float[] edge01 = new float[VCTR_NUM_CMPS];
    private final float[] edge02 = new float[VCTR_NUM_CMPS];
    private final float[] rayToCenter = new float[VCTR_NUM_CMPS];
    private final Triangle workTri = new Triangle(0);
    private final VertexCache vertexCache = new VertexCache();
    private final TriangleQueue triQueue = new TriangleQueue();
    private final ArrayList<DrawState> queuedDraws = new ArrayList<>();
    private final List<RasterWorker> workers = new ArrayList<>();
    private boolean clearPending;

    public final QTimer geometryTimer = new QTimer();
    public final QTimer rasterTimer = new QTimer();

    ////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void setNearClip(float val) {
        state.nearClip = Math.min(MIN_NEAR_CLIP, val);
    }

    public void setViewBounds(float left, float right, float bottom, float top) {
        state.viewLeft = left;
        state.viewRight = right;
        state.viewBottom = bottom;
        state.viewTop = top;
    }

    public void setRenderMode(RenderMode mode) {
        renderMode = mode;
    }

    public void setSampleType(SampleType _type) {
        state.sampleType = _type;
    }

    public void setFillColor(QColor _color) {
        state.fillColor.set(_color);
    }

    public void setCustomShader(QShader _shader) {
        customShader = _shader;
    }

    public void setUniformSlot(
            int slot,
            QUniform<?> uniform) {
        state.slotUniforms[slot] = uniform;
    }

    public void setMatrix(QMatrix4x4 matrix) {
        setUniformSlot(DEFAULT_SHADER_MATRIX_SLOT, matrix);
    }

    public void setLights(QLights lights) {
        setUniformSlot(DEFAULT_SHADER_LIGHTS_SLOT, lights);
    }

    public void clearUniformSlots() {
        for (int i = 0; i < state.slotUniforms.length; i++) {
            state.slotUniforms[i] = null;
        }
    }

    public void setTextureSlot(
            int slot,
            QSampleable texture) {
        state.slotTextures[slot] = texture;
    }

    public void setTexture(QSampleable texture) {
        setTextureSlot(DEFAULT_SHADER_TEXTURE_SLOT, texture);
    }

    public void clearTextureSlots() {
        for (int i = 0; i < state.slotTextures.length; i++) {
            state.slotTextures[i] = null;
        }
    }

    public void setVertexAttribSlot(
            int slot,
            QAttribIndexer indexer) {
        state.slotAttribs[slot] = indexer;
    }

    public void clearVertexAttribSlots() {
        for (int i = 0; i < state.slotAttribs.length; i++) {
            state.slotAttribs[i] = null;
        }
    }

    public void clearFrame() {
        flush();
        clearPending = true;
    }

    public void draw() {
        QShader shader = internalGetRelevantShader();
        ShaderRequirement[] requirements = shader.requirements();
        internalEnsureShaderRequirements(requirements);
        queuedDraws.add(new DrawState(state.snapshot(), shader, new QShader.OutputLayout(requirements)));
    }

    public void flush() {
        try {
            geometryTimer.start();
            triQueue.reset();
            for (DrawState queued : queuedDraws) {
                internalProcessGeometry(queued);
            }
            geometryTimer.stop();
            rasterTimer.start();
            if (!queuedDraws.isEmpty() || clearPending) {
                internalRasterizeParallel();
            }
            rasterTimer.stop();
        } finally {
            queuedDraws.clear();
            clearPending = false;
        }
    }

    public void drawMesh(
            QMesh mesh) {
        setVertexAttribSlot(DEFAULT_SHADER_POSITION_SLOT, mesh.getPosIndexer());
        setVertexAttribSlot(DEFAULT_SHADER_UV_SLOT, mesh.getUVIndexer());
        setVertexAttribSlot(DEFAULT_SHADER_NORMAL_SLOT, mesh.getNormalIndexer());

        draw();

        clearTextureSlots();
        clearUniformSlots();
        clearVertexAttribSlots();
    }

    ////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private void init(QRenderBuffer target) {
        renderTarget = target;
        for (int i = 0; i < RASTER_THREADS; i++) {
            workers.add(new RasterWorker(target, triQueue));
        }
    }

    private void internalEnsureShaderRequirements(ShaderRequirement[] requirements) {
        for (ShaderRequirement require : requirements) {
            switch (require.requireType) {
                case Attribute:
                    if (state.slotAttribs[require.slot] == null) {
                        throw new QException(
                                PointOfError.InvalidData,
                                "Shader requires attibute slot " + require.slot +
                                        ". Purpose: " + require.purpose);
                    }
                    break;

                case Uniform:
                    if (state.slotUniforms[require.slot] == null) {
                        String typeSpecification = "";
                        if (require.uniformClass != null) {
                            typeSpecification = " of type " + require.uniformClass;
                        }
                        throw new QException(
                                PointOfError.InvalidData,
                                "Shader requires uniform slot " +
                                        require.slot +
                                        typeSpecification +
                                        ". Purpose: " + require.purpose);
                    }
                    if (state.slotUniforms[require.slot].getClass() != require.uniformClass) {
                        throw new QException(
                                PointOfError.InvalidData,
                                "Shader requires uniform slot " + require.slot +
                                        " to be of type: " +
                                        require.uniformClass +
                                        ". Given was " +
                                        state.slotUniforms[require.slot].getClass());
                    }
                    break;

                case Texture:
                    if (state.slotTextures[require.slot] == null) {
                        throw new QException(
                                PointOfError.InvalidData,
                                "Shader requires texture slot " + require.slot +
                                        ". Purpose: " + require.purpose);
                    }
                    break;

                case Output:
                    break;

                default:
                    throw new QException(
                            PointOfError.InvalidData,
                            "Unknow requirement type: " + require.requireType.toString());
            }
        }
    }

    private QShader internalGetRelevantShader() {
        switch (renderMode) {
            case SolidFill:
                return SOLIDFILL_SHADER;

            case Textured:
                return TEXTURED_SHADER;

            case Normal:
                return NORMAL_SHADER;

            case Material:
                return MATERIAL_SHADER;

            case CustomShader:
                return customShader;

            default:
                throw new QException(
                        PointOfError.BadState,
                        "Unknown renderMode: " + renderMode.toString());
        }
    }

    private void internalProcessGeometry(DrawState draw) {
        if (renderTarget == null) {
            throw new QException(
                    PointOfError.BadState,
                    "Render target unassigned");
        }
        QAttribIndexer[] slotAttribs = draw.state.slotAttribs;

        // ENSURE ALL INDEXERS HAVE THE SAME TRI COUNT
        int triCount = -1;
        for (int slot = 0; slot < slotAttribs.length; slot++) {
            if (slotAttribs[slot] == null)
                continue;
            if (triCount == -1) {
                triCount = slotAttribs[slot].getTriCount();
                continue;
            }
            if (slotAttribs[slot].getTriCount() != triCount) {
                throw new QException(
                        PointOfError.BadState,
                        "Not all vertex attribute slots are the same length.");
            }
        }
        if (triCount == -1) {
            throw new QException(
                    PointOfError.BadState,
                    "No vertex attribute slots set!");
        }

        vctx.uniforms = draw.state.slotUniforms;
        vctx.textures = draw.state.slotTextures;
        vctx.layout = draw.layout;
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

        vertexCache.prepare(slotAttribs, draw.layout.total);

        draw.shader.beginDraw(vctx);

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

            // CULL IF BACKFACING
            if (internalCheckBackfacing(tri)) {
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

            Triangle[] clipTris = TriangleClipper.clip(tri, nearClip);
            for (Triangle clippedTri : clipTris) {
                internalEmitTri(clippedTri, false, draw);
            }
        }

        draw.triEnd = triQueue.triEnd;
    }

    private void internalEmitTri(Triangle tri, boolean shared, DrawState draw) {
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

    private void internalRasterizeParallel() {
        AtomicInteger nextStrip = new AtomicInteger();
        List<Callable<Void>> tasks = new ArrayList<>(workers.size());
        for (RasterWorker worker : workers) {
            tasks.add(() -> {
                worker.rasterize(queuedDraws, nextStrip, clearPending);
                return null;
            });
        }
        try {
            for (Future<Void> result : RASTER_POOL.invokeAll(tasks)) {
                result.get();
            }
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            if (e.getCause() instanceof Error cause) {
                throw cause;
            }
            throw new QException(PointOfError.BadState, e.getCause().toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new QException(PointOfError.BadState, "Interrupted while rasterizing");
        }
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
        QAttribIndexer[] slotAttribs = draw.state.slotAttribs;
        for (int slot = 0; slot < slotAttribs.length; slot++) {
            // skip if slot is unused
            QAttribIndexer indexer = slotAttribs[slot];
            if (indexer == null) {
                continue;
            }

            // populate attribute slot using indexer
            indexer.index(
                    tri.triNum,
                    triVertNum,
                    0,
                    vctx.attributes[slot]);
        }

        // CALL SHADER
        QVector3.copy(vert.posn, draw.shader.vertexShader(vctx).getComponents());
        vctx.ensureOutputsWritten();
        vertexCache.store(idx, vert);
    }

    private boolean internalCheckBackfacing(Triangle tri) {
        // NOTE:
        // if the dot product between the normal is > 0 for the ray
        // from the camera to the center of the tri, then it's backfacing
        QVector3.copy(rayToCenter, tri.getPosn(0));
        QVector3.add(rayToCenter, tri.getPosn(1));
        QVector3.add(rayToCenter, tri.getPosn(2));
        return (QVector3.dot(tri.normal, rayToCenter) > BACKFACE_CULL_MIN_DOT);
    }

    ////////////////////////////////////////////////////////////////
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
            float viewBottom, float viewTop) {
        init(renderTarget);
        setViewBounds(viewLeft, viewRight, viewBottom, viewTop);
    }

}
