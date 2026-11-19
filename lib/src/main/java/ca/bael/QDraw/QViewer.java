// Bailey JT Brown
// 2023-2024
// QViwer.java

package ca.bael.QDraw;

import java.util.ArrayList;
import java.util.Arrays;

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
    private static final float DEPTH_TEST_EPSILON = 0.002f;
    private static final int VERTS_PER_TRI = 3;

    public static final int SHADER_UNIFORM_SLOTS = 16;
    public static final int SHADER_TEXTURE_SLOTS = 16;
    public static final int SHADER_VERTEX_ATTRIB_SLOTS = 8;

    public static final RenderMode DEFAULT_RENDER_MODE = RenderMode.Textured;
    public static final QColor DEFAULT_FILL_COLOR = QColor.White();
    public static final SampleType DEFAULT_SAMPLE_TYPE = SampleType.Repeat;
    public static final int DEFAULT_SHADER_POSITION_SLOT = 0;
    public static final int DEFAULT_SHADER_UV_SLOT = 1;
    public static final int DEFAULT_SHADER_NORMAL_SLOT = 2;
    public static final int DEFAULT_SHADER_MATRIX_SLOT = 0;
    public static final int DEFAULT_SHADER_LIGHTS_SLOT = 1;
    public static final int DEFAULT_SHADER_TEXTURE_SLOT = 0;
    public static final int DEFAULT_SHADER_MATERIAL_SLOT = 2;

    private final QShader SOLIDFILL_SHADER = new QShader( ) {
        public ShaderRequirement[] requirements( ) {
            return new ShaderRequirement[] {
                new ShaderRequirement(
                    DEFAULT_SHADER_POSITION_SLOT, 
                    RequirementType.Attribute, 
                    "vertex position"
                ), 
                new ShaderRequirement(
                    DEFAULT_SHADER_MATRIX_SLOT, 
                    RequirementType.Uniform, 
                    "vertex transform matrix",
                    QMatrix4x4.class
                )
            };
        }

        public QVector3 vertexShader(
            VertexShaderContext context
        ) {
            QMatrix4x4 mtr = context.uniform(DEFAULT_SHADER_MATRIX_SLOT, QMatrix4x4.class);
            return mtr.transformPoint(context.attribVec3(DEFAULT_SHADER_POSITION_SLOT, context.position( )));
        }

        public QColor fragmentShader(
            FragmentShaderContext context
        ) {
            return context.fillColor;
        }
    };

    private final QShader TEXTURED_SHADER = new QShader( ) {
        public ShaderRequirement[] requirements( ) {
            return new ShaderRequirement[] {
                new ShaderRequirement(
                    DEFAULT_SHADER_POSITION_SLOT, 
                    RequirementType.Attribute, 
                    "vertex position"
                ), 
                new ShaderRequirement(
                    DEFAULT_SHADER_UV_SLOT, 
                    RequirementType.Attribute, 
                    "vertex uv"
                ), 
                new ShaderRequirement(
                    DEFAULT_SHADER_MATRIX_SLOT, 
                    RequirementType.Uniform, 
                    "vertex transform matrix",
                    QMatrix4x4.class
                ),
                new ShaderRequirement(
                    DEFAULT_SHADER_TEXTURE_SLOT, 
                    RequirementType.Texture, 
                    "mesh texture"
                ),
                new ShaderRequirement(
                    DEFAULT_SHADER_UV_SLOT,
                    RequirementType.Output,
                    "uv",
                    2
                )
            };
        }

        private OutputSlot uvSlot;

        public void beginDraw(VertexShaderContext context) {
            uvSlot = context.outputSlot(DEFAULT_SHADER_UV_SLOT);
        }

        public QVector3 vertexShader(
            VertexShaderContext context
        ) {
            QMatrix4x4 mtr = context.uniform(DEFAULT_SHADER_MATRIX_SLOT, QMatrix4x4.class);
            context.forward(uvSlot);

            return mtr.transformPoint(context.attribVec3(DEFAULT_SHADER_POSITION_SLOT, context.position( )));
        }

        public QColor fragmentShader(
            FragmentShaderContext context
        ) {
            return context.color( ).set(context.texture(DEFAULT_SHADER_TEXTURE_SLOT).sample(
                context.input(uvSlot, 0),
                context.input(uvSlot, 1),
                context.sampleType
            ));
        }
    };

    private final QShader NORMAL_SHADER = new QShader( ) {
        public ShaderRequirement[] requirements( ) {
            return new ShaderRequirement[] {
                new ShaderRequirement(
                    DEFAULT_SHADER_POSITION_SLOT, 
                    RequirementType.Attribute, 
                    "vertex position"
                ), 
                new ShaderRequirement(
                    DEFAULT_SHADER_NORMAL_SLOT, 
                    RequirementType.Attribute, 
                    "vertex normal"
                ),
                new ShaderRequirement(
                    DEFAULT_SHADER_MATRIX_SLOT, 
                    RequirementType.Uniform, 
                    "vertex transform matrix",
                    QMatrix4x4.class
                ),
                new ShaderRequirement(
                    DEFAULT_SHADER_NORMAL_SLOT,
                    RequirementType.Output,
                    "normal",
                    3
                )
            };
        }

        private QMatrix4x4 normalMtr;
        private OutputSlot normalSlot;
        private final QVector3 normal = new QVector3( );

        public void beginDraw(VertexShaderContext context) {
            normalMtr = context.uniform(DEFAULT_SHADER_MATRIX_SLOT, QMatrix4x4.class).normalMatrix( );
            normalSlot = context.outputSlot(DEFAULT_SHADER_NORMAL_SLOT);
        }

        public QVector3 vertexShader(
            VertexShaderContext context
        ) {
            QMatrix4x4 mtr = context.uniform(DEFAULT_SHADER_MATRIX_SLOT, QMatrix4x4.class);
            context.output(
                normalSlot,
                normalMtr.transformDir(context.attribVec3(DEFAULT_SHADER_NORMAL_SLOT, normal)).normalize( )
            );

            return mtr.transformPoint(context.attribVec3(DEFAULT_SHADER_POSITION_SLOT, context.position( )));
        }

        public QColor fragmentShader(
            FragmentShaderContext context
        ) {
            QVector3 normal = context.inputVec3(normalSlot);

            return context.color( ).set(
                (int)((1.0f + normal.getX( )) * 127.0f),
                (int)((1.0f + normal.getY( )) * 127.0f),
                (int)((1.0f + normal.getZ( )) * 127.0f)
            );
        }
    };

    private final QShader MATERIAL_SHADER = new QShader( ) {
        public ShaderRequirement[] requirements( ) {
            // NOTE:
            //  texture is optional for this shader as material uniform will 
            //  provide the base color
            return new ShaderRequirement[] {
                new ShaderRequirement(
                    DEFAULT_SHADER_POSITION_SLOT, 
                    RequirementType.Attribute, 
                    "vertex position"
                ), 
                new ShaderRequirement(
                    DEFAULT_SHADER_UV_SLOT, 
                    RequirementType.Attribute, 
                    "vertex uv"
                ), 
                new ShaderRequirement(
                    DEFAULT_SHADER_NORMAL_SLOT, 
                    RequirementType.Attribute, 
                    "vertex normal"
                ), 
                new ShaderRequirement(
                    DEFAULT_SHADER_MATRIX_SLOT, 
                    RequirementType.Uniform, 
                    "vertex transform matrix",
                    QMatrix4x4.class
                ),
                new ShaderRequirement(
                    DEFAULT_SHADER_LIGHTS_SLOT, 
                    RequirementType.Uniform, 
                    "view-space light position array",
                    QLights.class
                ),
                new ShaderRequirement(
                    DEFAULT_SHADER_MATERIAL_SLOT, 
                    RequirementType.Uniform, 
                    "mesh material",
                    QMaterial.class
                ),
                new ShaderRequirement(
                    DEFAULT_SHADER_POSITION_SLOT,
                    RequirementType.Output,
                    "view-space position",
                    3
                ),
                new ShaderRequirement(
                    DEFAULT_SHADER_UV_SLOT,
                    RequirementType.Output,
                    "uv",
                    2
                ),
                new ShaderRequirement(
                    DEFAULT_SHADER_NORMAL_SLOT,
                    RequirementType.Output,
                    "normal",
                    3
                )
            };
        }

        private QMatrix4x4 normalMtr;
        private OutputSlot posSlot;
        private OutputSlot uvSlot;
        private OutputSlot normalSlot;
        private final QVector3 normal = new QVector3( );

        public void beginDraw(VertexShaderContext context) {
            normalMtr = context.uniform(DEFAULT_SHADER_MATRIX_SLOT, QMatrix4x4.class).normalMatrix( );
            posSlot    = context.outputSlot(DEFAULT_SHADER_POSITION_SLOT);
            uvSlot     = context.outputSlot(DEFAULT_SHADER_UV_SLOT);
            normalSlot = context.outputSlot(DEFAULT_SHADER_NORMAL_SLOT);
        }

        public QVector3 vertexShader(
            VertexShaderContext context
        ) {
            QMatrix4x4 mtr    = context.uniform(DEFAULT_SHADER_MATRIX_SLOT, QMatrix4x4.class);
            QVector3   pos    = mtr.transformPoint(context.attribVec3(DEFAULT_SHADER_POSITION_SLOT, context.position( )));
            normalMtr.transformDir(context.attribVec3(DEFAULT_SHADER_NORMAL_SLOT, normal)).normalize( );

            context.output(posSlot, pos);
            context.forward(uvSlot);
            context.output(normalSlot, normal);

            return pos;
        }

        public QColor fragmentShader(
            FragmentShaderContext context
        ) {
            QSampleable tex = context.texture(DEFAULT_SHADER_TEXTURE_SLOT);
            QMaterial   mat = context.uniform(DEFAULT_SHADER_MATERIAL_SLOT, QMaterial.class);
            QLights  lights = context.uniform(DEFAULT_SHADER_LIGHTS_SLOT, QLights.class);

            float[] uv     = context.input(uvSlot, new float[2]);
            float[] normal = context.inputVec3(normalSlot).normalize( ).getComponents( );
            QColor texSampleColor = context.color( ).set(
                tex == null ? 0xFFFFFFFF : tex.sample(uv[0], uv[1], context.sampleType)
            );

            float diffuseIntensity = 0f;
            float specularIntensity = 0f;

            final QVector3 POS_VEC = context.inputVec3(posSlot);

            QVector3 viewDir = POS_VEC.copy().multiply3(-1).normalize();
            QVector3 toLight = new QVector3( );

            for (int i = 0; i < lights.count( ); i++) {
                QLight light = lights.get(i);
                float attenuation = light.illuminate(POS_VEC, toLight);
                float lambert = QVector3.dot(toLight.getComponents(), normal);
                if (lambert <= 0.0f) {
                    continue;
                }

                diffuseIntensity += lambert * attenuation;
                float specDot = QVector3.dot(toLight.add(viewDir).normalize().getComponents(), normal);
                specularIntensity += attenuation * (float)Math.pow(Math.max(0f, specDot), mat.shininess);
            }

            texSampleColor.set(
                (int)(texSampleColor.getR() * Math.min(1f, mat.ambient.getR() / 255f + diffuseIntensity * mat.diffuse.getR() / 255f) + mat.specular.getR() * specularIntensity),
                (int)(texSampleColor.getG() * Math.min(1f, mat.ambient.getG() / 255f + diffuseIntensity * mat.diffuse.getG() / 255f) + mat.specular.getG() * specularIntensity),
                (int)(texSampleColor.getB() * Math.min(1f, mat.ambient.getB() / 255f + diffuseIntensity * mat.diffuse.getB() / 255f) + mat.specular.getB() * specularIntensity)
            );

            return texSampleColor;
        }
    };

    ////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private float nearClip = DEFAULT_NEAR_CLIP;
    private float viewLeft = DEFAULT_VIEWBOUND_LEFT;
    private float viewRight = DEFAULT_VIEWBOUND_RIGHT;
    private float viewBottom = DEFAULT_VIEWBOUND_BOTTOM;
    private float viewTop = DEFAULT_VIEWBOUND_TOP;
    private QRenderBuffer renderTarget = null;
    private QShader customShader = null;
    private RenderMode renderMode = DEFAULT_RENDER_MODE;
    private QColor fillColor = DEFAULT_FILL_COLOR;
    private SampleType sampleType = DEFAULT_SAMPLE_TYPE;

    private QUniform<?>[] slotUniforms = new QUniform<?>[SHADER_UNIFORM_SLOTS];
    private QSampleable[] slotTextures = new QSampleable[SHADER_TEXTURE_SLOTS];
    private QAttribIndexer[] slotAttribs = new QAttribIndexer[SHADER_VERTEX_ATTRIB_SLOTS];
    private final QShader.VertexShaderContext vctx = new QShader.VertexShaderContext();
    private final QShader.FragmentShaderContext fctx = new QShader.FragmentShaderContext();
    private final RasterSetup raster = new RasterSetup();
    private final float[] edge01 = new float[VCTR_NUM_CMPS];
    private final float[] edge02 = new float[VCTR_NUM_CMPS];
    private final float[] rayToCenter = new float[VCTR_NUM_CMPS];
    private final Triangle workTri = new Triangle(0);
    private final VertexCache vertexCache = new VertexCache();
    private final TriangleQueue triQueue = new TriangleQueue();
    private QShader drawShader;
    private final ArrayList<DrawState> queuedDraws = new ArrayList<>();

    public final QTimer geometryTimer = new QTimer();
    public final QTimer rasterTimer = new QTimer();

    ////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void setNearClip(float val) {
        nearClip = Math.min(MIN_NEAR_CLIP, val);
    }

    public void setViewBounds(float left, float right, float bottom, float top) {
        viewLeft = left;
        viewRight = right;
        viewBottom = bottom;
        viewTop = top;
    }

    public void setRenderMode(RenderMode mode) {
        renderMode = mode;
    }

    public void setSampleType(SampleType _type) {
        sampleType = _type;
    }

    public void setFillColor(QColor _color) {
        fillColor.set(_color);
    }

    public void setCustomShader(QShader _shader) {
        customShader = _shader;
    }

    public void setUniformSlot(
            int slot,
            QUniform<?> uniform) {
        slotUniforms[slot] = uniform;
    }

    public void setMatrix(QMatrix4x4 matrix) {
        setUniformSlot(DEFAULT_SHADER_MATRIX_SLOT, matrix);
    }

    public void setLights(QLights lights) {
        setUniformSlot(DEFAULT_SHADER_LIGHTS_SLOT, lights);
    }

    public void clearUniformSlots() {
        for (int i = 0; i < slotUniforms.length; i++) {
            slotUniforms[i] = null;
        }
    }

    public void setTextureSlot(
            int slot,
            QSampleable texture) {
        slotTextures[slot] = texture;
    }

    public void setTexture(QSampleable texture) {
        setTextureSlot(DEFAULT_SHADER_TEXTURE_SLOT, texture);
    }

    public void clearTextureSlots() {
        for (int i = 0; i < slotTextures.length; i++) {
            slotTextures[i] = null;
        }
    }

    public void setVertexAttribSlot(
            int slot,
            QAttribIndexer indexer) {
        slotAttribs[slot] = indexer;
    }

    public void clearVertexAttribSlots() {
        for (int i = 0; i < slotAttribs.length; i++) {
            slotAttribs[i] = null;
        }
    }

    public void clearFrame() {
        flush();
        renderTarget.clearColorBuffer();
        renderTarget.clearDepthBuffer();
    }

    public void draw() {
        internalEnsureShaderRequirements(internalGetRelevantShader().requirements());
        queuedDraws.add(new DrawState(true));
    }

    public void flush() {
        DrawState live = new DrawState(false);
        try {
            geometryTimer.start();
            triQueue.reset();
            for (DrawState queued : queuedDraws) {
                queued.apply();
                internalProcessGeometry(queued);
            }
            geometryTimer.stop();
            rasterTimer.start();
            for (DrawState queued : queuedDraws) {
                internalRasterize(queued);
            }
            rasterTimer.stop();
        } finally {
            queuedDraws.clear();
            live.apply();
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
    // PRIVATE CLASSES
    private final class DrawState {
        ////////////////////////////////////////////////////////////////
        // PRIVATE MEMBERS
        private final float            nearClip;
        private final float            viewLeft;
        private final float            viewRight;
        private final float            viewBottom;
        private final float            viewTop;
        private final QShader          customShader;
        private final RenderMode       renderMode;
        private final QColor           fillColor;
        private final SampleType       sampleType;
        private final QUniform<?>[]    uniforms;
        private final QSampleable[]    textures;
        private final QAttribIndexer[] attribs;
        private QShader                shader;
        private QShader.OutputLayout   layout;
        private int                    triStart;
        private int                    triEnd;

        ////////////////////////////////////////////////////////////////
        // PUBLIC METHODS
        public void apply() {
            QViewer.this.nearClip     = nearClip;
            QViewer.this.viewLeft     = viewLeft;
            QViewer.this.viewRight    = viewRight;
            QViewer.this.viewBottom   = viewBottom;
            QViewer.this.viewTop      = viewTop;
            QViewer.this.customShader = customShader;
            QViewer.this.renderMode   = renderMode;
            QViewer.this.fillColor    = fillColor;
            QViewer.this.sampleType   = sampleType;
            System.arraycopy(uniforms, 0, slotUniforms, 0, uniforms.length);
            System.arraycopy(textures, 0, slotTextures, 0, textures.length);
            System.arraycopy(attribs, 0, slotAttribs, 0, attribs.length);
        }

        ////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        public DrawState(boolean snapshot) {
            nearClip     = QViewer.this.nearClip;
            viewLeft     = QViewer.this.viewLeft;
            viewRight    = QViewer.this.viewRight;
            viewBottom   = QViewer.this.viewBottom;
            viewTop      = QViewer.this.viewTop;
            customShader = QViewer.this.customShader;
            renderMode   = QViewer.this.renderMode;
            fillColor    = snapshot ? QViewer.this.fillColor.snapshot() : QViewer.this.fillColor;
            sampleType   = QViewer.this.sampleType;
            uniforms     = slotUniforms.clone();
            textures     = slotTextures.clone();
            attribs      = slotAttribs.clone();
            for (int i = 0; snapshot && i < uniforms.length; i++) {
                if (uniforms[i] != null) {
                    uniforms[i] = uniforms[i].snapshot();
                }
            }
        }
    }

    private static class Vertex {
        ////////////////////////////////////////////////////////////////
        // PUBLIC MEMBERS
        public float[] posn;
        public float[] shaderOutputs;

        ////////////////////////////////////////////////////////////////
        // PUBLIC METHODS
        public void findClippedShaderOutputs(
                Vertex vISrc,
                Vertex vFSrc,
                float factor) {
            // NOTE:
            // - vISrc and vFSrc should have identically formatted shader outputs,
            // that is, the slots are consistent in usage and component count,
            // so when initializing the new shader outputs, we arbitrarily use vISrc
            // as a reference
            // - vISrc or vFSrc might actually be THIS vertex, so we calculate everything
            // to an auxillary buffer before updating shaderOutputs
            float[] tempShaderOutputs = new float[vISrc.shaderOutputs.length];
            float facI = 1.0f - factor;
            float facF = factor;

            for (int i = 0; i < tempShaderOutputs.length; i++) {
                tempShaderOutputs[i] = vISrc.shaderOutputs[i] * facI +
                        vFSrc.shaderOutputs[i] * facF;
            }

            shaderOutputs = tempShaderOutputs;
        }

        ////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        public Vertex() {
        }

        public Vertex(Vertex toCopy) {
            posn = new float[VCTR_NUM_CMPS];
            QVector3.copy(
                    posn,
                    toCopy.posn);
            shaderOutputs = toCopy.shaderOutputs.clone( );
        }
    }

    private static class Triangle {
        ////////////////////////////////////////////////////////////////
        // CONSTANTS
        public static final int VERTS_PER_TRI = 3;

        ////////////////////////////////////////////////////////////////
        // PUBLIC MEMBERS
        public int triNum;
        public Vertex[] verts;
        public float[] normal;

        ////////////////////////////////////////////////////////////////
        // PUBLIC METHODS
        public void swapVerts(int v0, int v1) {
            Vertex temp = verts[v0];
            verts[v0] = verts[v1];
            verts[v1] = temp;
        }

        public float[] getPosn(int vertNum) {
            return verts[vertNum].posn;
        }

        public void setPosn(int vertNum, float[] inVals) {
            QVector3.copy(verts[vertNum].posn, inVals);
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

        public Vertex getVertex(int vertNum) {
            return verts[vertNum];
        }

        ////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        public Triangle(int _num) {
            triNum = _num;
            verts = new Vertex[] { new Vertex(), new Vertex(), new Vertex() };
            normal = new float[VCTR_NUM_CMPS];
        }

        public Triangle(Triangle toCopy) {
            triNum = toCopy.triNum;
            normal = new float[VCTR_NUM_CMPS];
            QVector3.copy(normal, toCopy.normal);

            verts = new Vertex[VERTS_PER_TRI];
            for (int i = 0; i < verts.length; i++) {
                verts[i] = new Vertex(toCopy.verts[i]);
            }
        }

    }

    private static class ClipState {
        ////////////////////////////////////////////////////////////////
        // PUBLIC MEMBERS
        public int numVertsBehind = 0;
        public boolean[] vertBehindState = new boolean[VERTS_PER_TRI];
        public int[] vertBehindIndicies = new int[VERTS_PER_TRI];

        ////////////////////////////////////////////////////////////////
        // PUBLIC METHODS
        public String toString() {
            return String.format(
                    "<numVBehind: %d, vStates: %s vIndicies: %s>",
                    numVertsBehind,
                    Arrays.toString(vertBehindState),
                    Arrays.toString(vertBehindIndicies));
        }
    }

    private static final class RasterSetup {
        ////////////////////////////////////////////////////////////////
        // PUBLIC MEMBERS
        public float[] verts;
        public int out0, out1, out2;
        public final float[] normal = new float[VCTR_NUM_CMPS];
        public float z0, z1, z2;
        public float p2x, p2y;
        public float db0dx, db0dy, db1dx, db1dy;

        ////////////////////////////////////////////////////////////////
        // PUBLIC METHODS
        public void set(float[] v, int o0, int o1, int o2, int n) {
            // refer to
            // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_rasterizetri.c
            float p0x = v[o0 + VCTR_INDEX_X];
            float p0y = v[o0 + VCTR_INDEX_Y];
            float p1x = v[o1 + VCTR_INDEX_X];
            float p1y = v[o1 + VCTR_INDEX_Y];
            p2x = v[o2 + VCTR_INDEX_X];
            p2y = v[o2 + VCTR_INDEX_Y];
            float invDenom = 1.0f / ((p1y - p2y) * (p0x - p2x) + (p2x - p1x) * (p0y - p2y));
            db0dx = (p1y - p2y) * invDenom;
            db0dy = (p2x - p1x) * invDenom;
            db1dx = (p2y - p0y) * invDenom;
            db1dy = (p0x - p2x) * invDenom;
            z0 = v[o0 + VCTR_INDEX_Z];
            z1 = v[o1 + VCTR_INDEX_Z];
            z2 = v[o2 + VCTR_INDEX_Z];
            verts = v;
            out0 = o0 + VCTR_NUM_CMPS;
            out1 = o1 + VCTR_NUM_CMPS;
            out2 = o2 + VCTR_NUM_CMPS;
            System.arraycopy(v, n, normal, 0, VCTR_NUM_CMPS);
        }

        public float b0At(float x, float y) {
            return db0dx * (x - p2x) + db0dy * (y - p2y);
        }

        public float b1At(float x, float y) {
            return db1dx * (x - p2x) + db1dy * (y - p2y);
        }
    }

    private static final class VertexCache {
        ////////////////////////////////////////////////////////////////
        // PRIVATE MEMBERS
        private int[]   indicies;
        private int[]   stamp   = new int[0];
        private int[]   emitted = new int[0];
        private float[] posn    = new float[0];
        private float[] outputs = new float[0];
        private int     drawId  = 0;
        private int     total;

        ////////////////////////////////////////////////////////////////
        // PUBLIC METHODS
        public void prepare(QAttribIndexer[] slots, int outputTotal) {
            drawId++;
            total = outputTotal;
            indicies = null;
            int size = 0;
            for (QAttribIndexer indexer : slots) {
                if (indexer == null) {
                    continue;
                }
                if (indicies == null) {
                    indicies = indexer.getIndicies();
                    size = indexer.getAttribCount();
                } else if (indexer.getIndicies() != indicies) {
                    indicies = null;
                    return;
                }
            }
            if (stamp.length < size) {
                stamp = new int[size];
                emitted = new int[size];
                posn = new float[size * VCTR_NUM_CMPS];
            }
            if (outputs.length < size * total) {
                outputs = new float[size * total];
            }
        }

        public int indexOf(int triNum, int triVertNum) {
            return indicies == null ? -1 : indicies[triNum * VERTS_PER_TRI + triVertNum];
        }

        public boolean load(int idx, Vertex vert) {
            if (idx < 0 || stamp[idx] != drawId) {
                return false;
            }
            System.arraycopy(posn, idx * VCTR_NUM_CMPS, vert.posn, 0, VCTR_NUM_CMPS);
            System.arraycopy(outputs, idx * total, vert.shaderOutputs, 0, total);
            return true;
        }

        public void store(int idx, Vertex vert) {
            if (idx < 0) {
                return;
            }
            stamp[idx] = drawId;
            emitted[idx] = -1;
            System.arraycopy(vert.posn, 0, posn, idx * VCTR_NUM_CMPS, VCTR_NUM_CMPS);
            System.arraycopy(vert.shaderOutputs, 0, outputs, idx * total, total);
        }

        public int emitted(int idx) {
            return idx < 0 || stamp[idx] != drawId ? -1 : emitted[idx];
        }

        public void setEmitted(int idx, int offset) {
            if (idx >= 0) {
                emitted[idx] = offset;
            }
        }
    }

    private static final class TriangleQueue {
        ////////////////////////////////////////////////////////////////
        // CONSTANTS
        public static final int RECORD_SIZE = 4;

        ////////////////////////////////////////////////////////////////
        // PUBLIC MEMBERS
        public float[] verts = new float[0];
        public int[]   tris  = new int[0];
        public int     vertEnd;
        public int     triEnd;

        ////////////////////////////////////////////////////////////////
        // PUBLIC METHODS
        public void reset() {
            vertEnd = 0;
            triEnd = 0;
        }

        public int allocVerts(int count) {
            if (vertEnd + count > verts.length) {
                verts = Arrays.copyOf(verts, Math.max(verts.length * 2, vertEnd + count));
            }
            int offset = vertEnd;
            vertEnd += count;
            return offset;
        }

        public void addTri(int v0, int v1, int v2, int normal) {
            if (triEnd + RECORD_SIZE > tris.length) {
                tris = Arrays.copyOf(tris, Math.max(tris.length * 2, RECORD_SIZE * 1024));
            }
            tris[triEnd++] = v0;
            tris[triEnd++] = v1;
            tris[triEnd++] = v2;
            tris[triEnd++] = normal;
        }
    }

    ////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private void init(QRenderBuffer target) {
        renderTarget = target;
    }

    private void internalEnsureShaderRequirements(ShaderRequirement[] requirements) {
        for (ShaderRequirement require : requirements) {
            switch (require.requireType) {
                case Attribute:
                    if (slotAttribs[require.slot] == null) {
                        throw new QException(
                                PointOfError.InvalidData,
                                "Shader requires attibute slot " + require.slot +
                                        ". Purpose: " + require.purpose);
                    }
                    break;

                case Uniform:
                    if (slotUniforms[require.slot] == null) {
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
                    if (slotUniforms[require.slot].getClass() != require.uniformClass) {
                        throw new QException(
                                PointOfError.InvalidData,
                                "Shader requires uniform slot " + require.slot +
                                        " to be of type: " +
                                        require.uniformClass +
                                        ". Given was " +
                                        slotUniforms[require.slot].getClass());
                    }
                    break;

                case Texture:
                    if (slotTextures[require.slot] == null) {
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

        drawShader = internalGetRelevantShader();
        ShaderRequirement[] requirements = drawShader.requirements();
        internalEnsureShaderRequirements(requirements);
        QShader.OutputLayout outputLayout = new QShader.OutputLayout(requirements);

        vctx.uniforms = slotUniforms;
        vctx.textures = slotTextures;
        vctx.layout = outputLayout;
        vctx.attributes = new float[slotAttribs.length][];
        for (int slot = 0; slot < slotAttribs.length; slot++) {
            if (slotAttribs[slot] != null) {
                vctx.attributes[slot] = new float[slotAttribs[slot].getComponentsPerAttrib()];
            }
        }

        for (Vertex vert : workTri.verts) {
            vert.posn = new float[VCTR_NUM_CMPS];
            vert.shaderOutputs = new float[outputLayout.total];
        }

        vertexCache.prepare(slotAttribs, outputLayout.total);

        drawShader.beginDraw(vctx);

        draw.shader = drawShader;
        draw.layout = outputLayout;
        draw.triStart = triQueue.triEnd;

        for (int triNum = 0; triNum < triCount; triNum++) {
            // GENERATE TRIANGLE
            Triangle tri = workTri;
            tri.triNum = triNum;
            internalProcessVertex(tri, 0);
            internalProcessVertex(tri, 1);
            internalProcessVertex(tri, 2);

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
            if (tri.getPosnZ(0) <= nearClip &&
                tri.getPosnZ(1) <= nearClip &&
                tri.getPosnZ(2) <= nearClip) {
                internalEmitTri(tri, true);
                continue;
            }

            Triangle[] clipTris = internalClipTri(tri);
            for (Triangle clippedTri : clipTris) {
                internalEmitTri(clippedTri, false);
            }
        }

        draw.triEnd = triQueue.triEnd;
    }

    private void internalEmitTri(Triangle tri, boolean shared) {
        int v0 = internalEmitVertex(tri, 0, shared);
        int v1 = internalEmitVertex(tri, 1, shared);
        int v2 = internalEmitVertex(tri, 2, shared);
        int n  = triQueue.allocVerts(VCTR_NUM_CMPS);
        System.arraycopy(tri.normal, 0, triQueue.verts, n, VCTR_NUM_CMPS);
        triQueue.addTri(v0, v1, v2, n);
    }

    private int internalEmitVertex(Triangle tri, int triVertNum, boolean shared) {
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
        out[offset + VCTR_INDEX_X] = (vert.posn[VCTR_INDEX_X] * -invZ - viewLeft) *
                (renderTarget.getWidth() / (viewRight - viewLeft));
        out[offset + VCTR_INDEX_Y] = (vert.posn[VCTR_INDEX_Y] * -invZ - viewBottom) *
                (renderTarget.getHeight() / (viewTop - viewBottom));
        out[offset + VCTR_INDEX_Z] = invZ;
        System.arraycopy(vert.shaderOutputs, 0, out, offset + VCTR_NUM_CMPS, total);

        vertexCache.setEmitted(idx, offset);
        return offset;
    }

    private void internalRasterize(DrawState draw) {
        drawShader      = draw.shader;
        fctx.uniforms   = draw.uniforms;
        fctx.textures   = draw.textures;
        fctx.sampleType = draw.sampleType;
        fctx.fillColor  = draw.fillColor;
        fctx.target     = renderTarget;
        fctx.layout     = draw.layout;
        fctx.inputs     = new float[draw.layout.total];

        int[] tris = triQueue.tris;
        for (int t = draw.triStart; t < draw.triEnd; t += TriangleQueue.RECORD_SIZE) {
            internalDrawTri(tris[t], tris[t + 1], tris[t + 2], tris[t + 3]);
        }
    }

    private void internalProcessVertex(Triangle tri, int triVertNum) {
        Vertex vert = tri.verts[triVertNum];
        int    idx  = vertexCache.indexOf(tri.triNum, triVertNum);
        if (vertexCache.load(idx, vert)) {
            return;
        }

        vctx.written = 0;
        vctx.outputs = vert.shaderOutputs;

        // POPULATE VERTEX SHADER INPUTS
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
        QVector3.copy(vert.posn, drawShader.vertexShader(vctx).getComponents());
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
                        "reached bad clipping state: " + clipState.toString());
        }
    }

    private void internalFindClipIntersect(
            Triangle srcTri,
            int pI,
            int pF,
            float[] out) {
        internalFindClipIntersect(
                srcTri.getPosn(pI),
                srcTri.getPosn(pF),
                out);
    }

    private void internalFindClipIntersect(
            float[] pI,
            float[] pF,
            float[] out) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c

        float invDZ = 1.0f / (pF[VCTR_INDEX_Z] - pI[VCTR_INDEX_Z]);
        float slopeXZ = (pF[VCTR_INDEX_X] - pI[VCTR_INDEX_X]) * invDZ;
        float slopeYZ = (pF[VCTR_INDEX_Y] - pI[VCTR_INDEX_Y]) * invDZ;
        float dClip = (nearClip - pI[VCTR_INDEX_Z]);

        out[VCTR_INDEX_X] = pI[VCTR_INDEX_X] + slopeXZ * dClip;
        out[VCTR_INDEX_Y] = pI[VCTR_INDEX_Y] + slopeYZ * dClip;
        out[VCTR_INDEX_Z] = nearClip;
    }

    private float internalFindClipInterpolationFactor(
            float[] pI,
            float[] pF,
            float[] pClip) {
        // NOTE:
        // takes a starting, ending, and middle position, generates
        // interpolation factor between pI to pF (0 for pI, 1 for pF)
        return (pClip[VCTR_INDEX_Z] - pI[VCTR_INDEX_Z]) /
               (pF[VCTR_INDEX_Z] - pI[VCTR_INDEX_Z]);
    }

    private Triangle[] internalClipTriCase1(Triangle tri, ClipState clipState) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c

        // NOTE:
        // - all faces must be constructed in CLOCKWISE winding order.
        // - when 1 vertex is clipped, generated face is a quad
        // - triangle will be re-shuffled so that it remains CLOCKWISE where pos2 is
        // clipped
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
                        "bad clip state: " + clipState.toString());
        }

        float[] pos02 = new float[VCTR_NUM_CMPS];
        float[] pos12 = new float[VCTR_NUM_CMPS];
        internalFindClipIntersect(shuffledTri, 0, 2, pos02);
        internalFindClipIntersect(shuffledTri, 1, 2, pos12);

        float fac02 = internalFindClipInterpolationFactor(
                shuffledTri.getPosn(0),
                shuffledTri.getPosn(2),
                pos02);
        float fac12 = internalFindClipInterpolationFactor(
                shuffledTri.getPosn(1),
                shuffledTri.getPosn(2),
                pos12);

        // NOTE:
        // - when 1 vertex is clipped, the resulting mesh is a quad
        // - our quad is 0/2, 0, 1, 1/2 (where a/b is clipped interpolation),
        // which will be tesselated as (0/2, 0, 1), (0/2, 1, 1/2)

        Triangle quadTri0 = new Triangle(shuffledTri);
        quadTri0.getVertex(2).findClippedShaderOutputs(
                shuffledTri.getVertex(0),
                shuffledTri.getVertex(2),
                fac02);
        quadTri0.setPosn(2, pos02); // (0 1 2) -> (0 1 0/2)
        quadTri0.swapVerts(0, 2); // (0 1 0/2) -> (0/2 1 0)
        quadTri0.swapVerts(1, 2); // (0/2 1 0) -> (0/2 0 1)

        Triangle quadTri1 = new Triangle(shuffledTri); // (0 1 2)
        quadTri1.getVertex(0).findClippedShaderOutputs(
                shuffledTri.getVertex(0),
                shuffledTri.getVertex(2),
                fac02);
        quadTri1.setPosn(0, pos02); // (0 1 2) -> (0/2 1 2)
        quadTri1.getVertex(2).findClippedShaderOutputs(
                shuffledTri.getVertex(1),
                shuffledTri.getVertex(2),
                fac12);
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
                        pos10);
                float fac20 = internalFindClipInterpolationFactor(
                        tri.getPosn(2),
                        tri.getPosn(0),
                        pos20);

                tri.getVertex(1).findClippedShaderOutputs(
                        tri.getVertex(1),
                        tri.getVertex(0),
                        fac10);
                tri.setPosn(1, pos10);

                tri.getVertex(2).findClippedShaderOutputs(
                        tri.getVertex(2),
                        tri.getVertex(0),
                        fac20);
                tri.setPosn(2, pos20);

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
                        pos01);
                float fac21 = internalFindClipInterpolationFactor(
                        tri.getPosn(2),
                        tri.getPosn(1),
                        pos21);

                tri.getVertex(0).findClippedShaderOutputs(
                        tri.getVertex(0),
                        tri.getVertex(1),
                        fac01);
                tri.setPosn(0, pos01);

                tri.getVertex(2).findClippedShaderOutputs(
                        tri.getVertex(2),
                        tri.getVertex(1),
                        fac21);
                tri.setPosn(2, pos21);

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
                        pos02);
                float fac12 = internalFindClipInterpolationFactor(
                        tri.getPosn(1),
                        tri.getPosn(2),
                        pos12);

                tri.getVertex(0).findClippedShaderOutputs(
                        tri.getVertex(0),
                        tri.getVertex(2),
                        fac02);
                tri.setPosn(0, pos02);

                tri.getVertex(1).findClippedShaderOutputs(
                        tri.getVertex(1),
                        tri.getVertex(2),
                        fac12);
                tri.setPosn(1, pos12);

                return new Triangle[] { tri };

            // BAD STATE
            default:
                throw new QException(
                        PointOfError.BadState,
                        "Bad clip state:" + clipState.toString());
        }

    }

    private void internalDrawTri(int o0, int o1, int o2, int normal) {
        float[] v = triQueue.verts;
        int swap;

        // NOTE:
        // - a triangle must be rasterized in two parts, a line will be cut
        // through it's middle highest verticie horizontally, and each partition
        // will be draw seperately
        // - the triangle will have to be sorted by height then cut
        // - after being cut, the top two verticies will be sorted from left to right

        if (v[o2 + VCTR_INDEX_Y] > v[o1 + VCTR_INDEX_Y]) { swap = o2; o2 = o1; o1 = swap; }
        if (v[o1 + VCTR_INDEX_Y] > v[o0 + VCTR_INDEX_Y]) { swap = o1; o1 = o0; o0 = swap; }
        if (v[o2 + VCTR_INDEX_Y] > v[o1 + VCTR_INDEX_Y]) { swap = o2; o2 = o1; o1 = swap; }
        raster.set(v, o0, o1, o2, normal);

        float invSlope20 = (v[o0 + VCTR_INDEX_X] - v[o2 + VCTR_INDEX_X]) /
                (v[o0 + VCTR_INDEX_Y] - v[o2 + VCTR_INDEX_Y]);
        float dY21 = v[o1 + VCTR_INDEX_Y] - v[o2 + VCTR_INDEX_Y];
        float midX = v[o2 + VCTR_INDEX_X] + (invSlope20 * dY21);
        float flatXL = Math.min(v[o1 + VCTR_INDEX_X], midX);
        float flatXR = Math.max(v[o1 + VCTR_INDEX_X], midX);

        internalDrawFlatTopTri(
                flatXL, flatXR, v[o1 + VCTR_INDEX_Y],
                v[o2 + VCTR_INDEX_X], v[o2 + VCTR_INDEX_Y]);
        internalDrawFlatBottomTri(
                flatXL, flatXR, v[o1 + VCTR_INDEX_Y],
                v[o0 + VCTR_INDEX_X], v[o0 + VCTR_INDEX_Y]);
    }

    private void internalDrawFlatTopTri(
            float flatXL, float flatXR, float flatY,
            float apexX, float apexY) {

        // NOTE:
        // - flattop triangle is drawn from the bottom apex up to the flat edge

        float invDY = 1.0f / (flatY - apexY);
        if (Float.isNaN(invDY)) {
            return;
        }

        float invSlopeLeft  = (flatXL - apexX) * invDY;
        float invSlopeRight = (flatXR - apexX) * invDY;

        int Y_START = Math.max((int) Math.ceil(apexY - 0.5f), 0);
        int Y_END = Math.min((int) Math.ceil(flatY - 0.5f), renderTarget.getHeight());

        for (int drawY = Y_START; drawY < Y_END; drawY++) {

            float distY = (drawY + 0.5f) - apexY;
            int X_START = Math.max(
                    (int) Math.ceil(apexX + (invSlopeLeft * distY) - 0.5f),
                    0);
            int X_END = Math.min(
                    (int) Math.ceil(apexX + (invSlopeRight * distY) - 0.5f),
                    renderTarget.getWidth());

            internalDrawSpan(X_START, X_END, drawY);

        }

    }

    private void internalDrawFlatBottomTri(
            float flatXL, float flatXR, float flatY,
            float apexX, float apexY) {

        // NOTE:
        // - flatbottom triangle is drawn from the flat edge up to the top apex

        float invDY = 1.0f / (apexY - flatY);
        if (Float.isNaN(invDY)) {
            return;
        }

        float invSlopeLeft  = (apexX - flatXL) * invDY;
        float invSlopeRight = (apexX - flatXR) * invDY;

        int Y_START = Math.max((int) Math.ceil(flatY - 0.5f), 0);
        int Y_END = Math.min((int) Math.ceil(apexY - 0.5f), renderTarget.getHeight());

        for (int drawY = Y_START; drawY < Y_END; drawY++) {

            float distY = (drawY + 0.5f) - flatY;
            int X_START = Math.max(
                    (int) Math.ceil(flatXL + (invSlopeLeft * distY) - 0.5f),
                    0);
            int X_END = Math.min(
                    (int) Math.ceil(flatXR + (invSlopeRight * distY) - 0.5f),
                    renderTarget.getWidth());

            internalDrawSpan(X_START, X_END, drawY);

        }

    }

    private void internalDrawSpan(int xStart, int xEnd, int drawY) {
        float centerX = xStart + 0.5f;
        float centerY = drawY + 0.5f;
        float b0 = raster.b0At(centerX, centerY);
        float b1 = raster.b1At(centerX, centerY);
        for (int drawX = xStart; drawX < xEnd; drawX++) {
            internalDrawFragment(drawX, drawY, b0, b1);
            b0 += raster.db0dx;
            b1 += raster.db1dx;
        }
    }

    private void internalInterpolateFragmentInputs(
            float a0,
            float a1,
            float a2,
            float[] buffer) {
        float[] verts = raster.verts;
        int o0 = raster.out0;
        int o1 = raster.out1;
        int o2 = raster.out2;
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = a0 * verts[o0 + i] + a1 * verts[o1 + i] + a2 * verts[o2 + i];
        }
    }

    private void internalDrawFragment(
            int drawX,
            int drawY,
            float b0,
            float b1) {

        float[] depthData = renderTarget.getDepthData();
        int[]   colorData = renderTarget.getColorData();
        int     pixel     = renderTarget.coordToDataIndex(drawX, drawY);

        float w0 = raster.z0 * b0;
        float w1 = raster.z1 * b1;
        float w2 = raster.z2 * (1.0f - b0 - b1);
        float invDepth = w0 + w1 + w2;

        // NOTE:
        // since all depths are negative and inverted, the further value
        // will be a smaller negative and hence greater. therefore the failing
        // depth test will be greater than the previous depth
        if (invDepth > depthData[pixel] - DEPTH_TEST_EPSILON) {
            return;
        }

        // CALL FRAGMENT SHADER
        fctx.screenX = drawX;
        fctx.screenY = drawY;
        fctx.invDepth = invDepth;
        fctx.normal = raster.normal;
        float invW = 1.0f / invDepth;
        internalInterpolateFragmentInputs(w0 * invW, w1 * invW, w2 * invW, fctx.inputs);

        QColor fragColor = drawShader.fragmentShader(fctx);

        // DISCARD IF FRAGMENT IS TRANSPARENT
        if ((fragColor.getA()) == 0) {
            return;
        }

        // BLEND FRAGMENT TO BELOW COLOR AND WRITE TO RENDERTARGET
        depthData[pixel] = invDepth;
        colorData[pixel] = QColor.blend(colorData[pixel], fragColor.toInt());
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
