// Bailey JT Brown
// 2023-2024
// QViwer.java

package ca.bael.QDraw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import ca.bael.QDraw.QException.PointOfError;
import ca.bael.QDraw.QSampleable.SampleType;
import ca.bael.QDraw.QShader.ShaderRequirement;

public final class QViewer {
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

    public static final int SHADER_UNIFORM_SLOTS = 16;
    public static final int SHADER_TEXTURE_SLOTS = 16;
    public static final int SHADER_VERTEX_ATTRIB_SLOTS = 8;

    public static final RenderMode DEFAULT_RENDER_MODE = RenderMode.Textured;
    public static final QColor DEFAULT_FILL_COLOR = QColor.White();
    private static final int WORKER_THREADS = Runtime.getRuntime().availableProcessors();
    private static final String[] WORKER_NAMES = {
        "Kitty",  "Sharky", "Ouppy",  "Froggy", "Solo",   "Bango",  "Tango",
        "Kiki",   "Bouba",  "Lala",   "Tiki",   "Yana",   "Yote",   "Boyfriend",
        "Zeta",   "Yanis",  "Pipi",   "Taha",   "Poke",   "Mini",   "Zala",
        "Potke",  "Lambda", "Saura",  "Boppe",  "Omua",   "Fauna",  "Talle"
    };
    private static final String[] WORKER_THREAD_NAMES = internalPickWorkerNames();
    private static final AtomicInteger WORKER_COUNT = new AtomicInteger();
    private static final ExecutorService WORKER_POOL = Executors.newFixedThreadPool(WORKER_THREADS, task -> {
        String name = WORKER_THREAD_NAMES[WORKER_COUNT.getAndIncrement() % WORKER_THREAD_NAMES.length];
        Thread thread = new Thread(task, "QuickDrawWorker_" + name);
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
    private QShader customShader = null;
    private RenderMode renderMode = DEFAULT_RENDER_MODE;

    private final ArrayList<DrawState> queuedDraws = new ArrayList<>();
    private final List<GeometryWorker> geometryWorkers = new ArrayList<>();
    private final List<RasterWorker> rasterWorkers = new ArrayList<>();
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
            if (!queuedDraws.isEmpty()) {
                internalProcessGeometryParallel();
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
    private static String[] internalPickWorkerNames() {
        List<String> shuffled = new ArrayList<>(List.of(WORKER_NAMES));
        Collections.shuffle(shuffled);
        Map<String, Integer> uses = new HashMap<>();
        String[] names = new String[WORKER_THREADS];
        for (int i = 0; i < WORKER_THREADS; i++) {
            String name = i < shuffled.size() ? shuffled.get(i)
                    : WORKER_NAMES[ThreadLocalRandom.current().nextInt(WORKER_NAMES.length)];
            int use = uses.merge(name, 1, Integer::sum);
            names[i] = use == 1 ? name : name + use;
        }
        return names;
    }

    private void init(QRenderBuffer target) {
        for (int i = 0; i < WORKER_THREADS; i++) {
            geometryWorkers.add(new GeometryWorker(target));
            rasterWorkers.add(new RasterWorker(target));
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

    private void internalProcessGeometryParallel() {
        AtomicInteger nextDraw = new AtomicInteger();
        List<Callable<Void>> tasks = new ArrayList<>(geometryWorkers.size());
        for (GeometryWorker worker : geometryWorkers) {
            tasks.add(() -> {
                worker.process(queuedDraws, nextDraw);
                return null;
            });
        }
        internalRunParallel(tasks);
    }

    private void internalRasterizeParallel() {
        AtomicInteger nextStrip = new AtomicInteger();
        List<Callable<Void>> tasks = new ArrayList<>(rasterWorkers.size());
        for (RasterWorker worker : rasterWorkers) {
            tasks.add(() -> {
                worker.rasterize(queuedDraws, nextStrip, clearPending);
                return null;
            });
        }
        internalRunParallel(tasks);
    }

    private static void internalRunParallel(List<Callable<Void>> tasks) {
        try {
            for (Future<Void> result : WORKER_POOL.invokeAll(tasks)) {
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
            throw new QException(PointOfError.BadState, "Interrupted while flushing");
        }
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
