// Bailey JT Brown
// 2023-2024
// QViwer.java

package QDraw;

import java.util.Arrays;

import QDraw.QException.PointOfError;
import QDraw.QSampleable.SampleType;
import QDraw.QShader.ShaderRequirement;
import QDraw.QShader.ShaderRequirement.RequirementType;

public final class QViewer extends QEncoding {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final float MIN_NEAR_CLIP     = -0.005f;
    public static final float DEFAULT_NEAR_CLIP = MIN_NEAR_CLIP;

    public static final float DEFAULT_VIEWBOUND_LEFT   = -1.0f;
    public static final float DEFAULT_VIEWBOUND_RIGHT  = 1.0f;
    public static final float DEFAULT_VIEWBOUND_BOTTOM = -1.0f;
    public static final float DEFAULT_VIEWBOUND_TOP    = 1.0f;

    private static final float BACKFACE_CULL_MIN_DOT  = 0.5f;
    private static final float DEPTH_TEST_EPSILON     = 0.002f;
    private static final int   VERTS_PER_TRI = 3;

    public static final int SHADER_UNIFORM_SLOTS       = 16;
    public static final int SHADER_TEXTURE_SLOTS       = 16;
    public static final int SHADER_VERTEX_ATTRIB_SLOTS = 8;

    public static final RenderMode DEFAULT_RENDER_MODE   = RenderMode.Textured;
    public static final QColor     DEFAULT_FILL_COLOR    = QColor.White();
    public static final SampleType DEFAULT_SAMPLE_TYPE   = SampleType.Repeat;
    public static final int DEFAULT_SHADER_POSITION_SLOT = 0;
    public static final int DEFAULT_SHADER_UV_SLOT       = 1;
    public static final int DEFAULT_SHADER_NORMAL_SLOT   = 2;
    public static final int DEFAULT_SHADER_MATRIX_SLOT   = 0;
    public static final int DEFAULT_SHADER_TEXTURE_SLOT  = 0;

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
                    "vertex transform matrix"
                )
            };
        }

        public QVector3 vertexShader(
            VertexShaderContext context
        ) {
            QMatrix4x4 mtr = (QMatrix4x4)context.uniforms[DEFAULT_SHADER_MATRIX_SLOT];
            return QMatrix4x4.multiply(
                mtr, 
                new QVector3(context.attributes[DEFAULT_SHADER_POSITION_SLOT])
            );
        }

        public QColor fragmentShader(
            FragmentShaderContext context
        ) {
            return fillColor;
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
                    "vertex transform matrix"
                ),
                new ShaderRequirement(
                    DEFAULT_SHADER_TEXTURE_SLOT, 
                    RequirementType.Texture, 
                    "mesh texture"
                )
            };
        }

        public QVector3 vertexShader(
            VertexShaderContext context
        ) {
            QMatrix4x4 mtr = (QMatrix4x4)context.uniforms[DEFAULT_SHADER_MATRIX_SLOT];
            forwardAttributeToFragShader(context, DEFAULT_SHADER_UV_SLOT);
            return QMatrix4x4.multiply(
                mtr, 
                new QVector3(context.attributes[DEFAULT_SHADER_POSITION_SLOT])
            );
        }

        public QColor fragmentShader(
            FragmentShaderContext context
        ) {
            QSampleable tex = context.textures[DEFAULT_SHADER_TEXTURE_SLOT];
            float[] uv = new float[2];
            getOutputFromVertShader(
                context, 
                DEFAULT_SHADER_UV_SLOT, 
                uv
            );
            return new QColor(tex.sample(uv[0], uv[1], sampleType));
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
                    "vertex transform matrix"
                )
            };
        }

        public QVector3 vertexShader(
            VertexShaderContext context
        ) {
            QMatrix4x4 mtr = (QMatrix4x4)context.uniforms[DEFAULT_SHADER_MATRIX_SLOT];
            QMatrix4x4 rotMtr = mtr.extractRotation( );
            QMath.mul3_4x4(
                context.attributes[DEFAULT_SHADER_NORMAL_SLOT], 
                rotMtr.getComponents( )
            );
            forwardAttributeToFragShader(context, DEFAULT_SHADER_NORMAL_SLOT);
            return QMatrix4x4.multiply(
                mtr, 
                new QVector3(context.attributes[DEFAULT_SHADER_POSITION_SLOT])
            );
        }

        public QColor fragmentShader(
            FragmentShaderContext context
        ) {
            float[] normal = new float[VCTR_NUM_CMPS];
            getOutputFromVertShader(context, DEFAULT_SHADER_NORMAL_SLOT, normal);
            return new QColor(
                (int)((1.0f + normal[VCTR_INDEX_X]) * 127.0f),
                (int)((1.0f + normal[VCTR_INDEX_Y]) * 127.0f),
                (int)((1.0f + normal[VCTR_INDEX_Z]) * 127.0f)
            );
        }
    };

    /////////////////////////////////////////////////////////////////
    // PUBLIC ENUMS
    public enum RenderMode {
        SolidFill,
        Textured,
        Normal,
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
    private QShader       customShader = null;
    private RenderMode    renderMode   = DEFAULT_RENDER_MODE;
    private QColor        fillColor    = DEFAULT_FILL_COLOR;
    private SampleType    sampleType   = DEFAULT_SAMPLE_TYPE;

    private Object[]   slotUniforms      = new Object[SHADER_UNIFORM_SLOTS];
    private QSampleable[] slotTextures   = new QSampleable[SHADER_TEXTURE_SLOTS];
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
        int    slot,
        Object uniform
    ) {
        slotUniforms[slot] = uniform;
    }

    public void setMatrix(QMatrix4x4 matrix) {
        setUniformSlot(DEFAULT_SHADER_MATRIX_SLOT, matrix);
    }

    public void clearUniformSlots( ) {
        for (int i = 0; i < slotUniforms.length; i++) {
            slotUniforms[i] = null;
        }
    }

    public void setTextureSlot(
        int         slot,
        QSampleable texture
    ) {
        slotTextures[slot] = texture;
    }

    public void setTexture(QSampleable texture) {
        setTextureSlot(DEFAULT_SHADER_TEXTURE_SLOT, texture);
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
        setMatrix(meshTransform);
        setVertexAttribSlot(DEFAULT_SHADER_POSITION_SLOT, mesh.getPosIndexer( ));
        setVertexAttribSlot(DEFAULT_SHADER_UV_SLOT, mesh.getUVIndexer( ));
        setVertexAttribSlot(DEFAULT_SHADER_NORMAL_SLOT, mesh.getNormalIndexer( ));

        draw( );

        clearTextureSlots( );
        clearUniformSlots( );
        clearVertexAttribSlots( );
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE CLASSES
    private static class Vertex {
        /////////////////////////////////////////////////////////////////
        // PUBLIC MEMBERS
        public float[]   posn;
        public float[][] shaderOutputs;

        /////////////////////////////////////////////////////////////////
        // PUBLIC METHODS
        public void project( ) {
            posn[VCTR_INDEX_Z] = 1.0f / posn[VCTR_INDEX_Z];
            QMath.mult2(posn, -posn[VCTR_INDEX_Z]);
        }

        public void findClippedShaderOutputs(
            Vertex vISrc,
            Vertex vFSrc,
            float  factor
        ) {
            // NOTE:
            // - vISrc and vFSrc should have identically formatted shader outputs,
            //   that is, the slots are consistent in usage and component count,
            //   so when initializing the new shader outputs, we arbitrarily use vISrc
            //   as a reference
            // - vISrc or vFSrc might actually be THIS vertex, so we calculate everything
            //   to an auxillary buffer before updating shaderOutputs
            float[][] tempShaderOutputs = new float[vISrc.shaderOutputs.length][];
            float facI = 1.0f - factor;
            float facF = factor;

            for (int i = 0; i < tempShaderOutputs.length; i++) {
                
                if (vISrc.shaderOutputs[i] == null) { continue; }
                tempShaderOutputs[i] = new float[vISrc.shaderOutputs[i].length];

                for (int comp = 0; comp < tempShaderOutputs[i].length; comp++) {
                    tempShaderOutputs[i][comp] = 
                        vISrc.shaderOutputs[i][comp] * facI + 
                        vFSrc.shaderOutputs[i][comp] * facF;
                }
            }
            
            shaderOutputs = tempShaderOutputs;
        }

        /////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        public Vertex( ) {
            posn = QMath.new3( );
        }

        public Vertex(Vertex toCopy) {
            posn = QMath.new3();
            QMath.copy3(
                posn, 
                toCopy.posn
            );

            // NOTE:
            // toCopy could be THIS vertex, so create a temp before assinging
            float[][] tempShaderOutputs = new float[toCopy.shaderOutputs.length][];
            for (int i = 0; i < tempShaderOutputs.length; i++) {
                if (toCopy.shaderOutputs[i] == null) { continue; }

                tempShaderOutputs[i] = new float[toCopy.shaderOutputs[i].length];
                System.arraycopy(
                    toCopy.shaderOutputs[i], 
                    0, 
                    tempShaderOutputs[i], 
                    0, 
                    toCopy.shaderOutputs[i].length
                );
            }

            shaderOutputs = tempShaderOutputs;
        }
    }

    private static class Triangle {
        /////////////////////////////////////////////////////////////////
        // CONSTANTS
        public static final int VERTS_PER_TRI = 3;

        /////////////////////////////////////////////////////////////////
        // PUBLIC MEMBERS
        public int      triNum;
        public Vertex[] verts  = new Vertex[] {
            new Vertex( ), new Vertex( ), new Vertex( )
        };
        public float[]  normal = new float[VCTR_NUM_CMPS];

        /////////////////////////////////////////////////////////////////
        // PUBLIC METHODS
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

        public Vertex getVertex(int vertNum) {
            return verts[vertNum];
        }

        /////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        public Triangle(int _num) {
            triNum = _num;
        }

        public Triangle(Triangle toCopy) {
            triNum = toCopy.triNum;
            normal = new float[VCTR_NUM_CMPS];
            QMath.copy3(normal, toCopy.normal);

            verts = new Vertex[VERTS_PER_TRI];
            for (int i = 0; i < verts.length; i++) {
                verts[i] = new Vertex(toCopy.verts[i]);
            }
        }

    }

    private static class ClipState {
        /////////////////////////////////////////////////////////////////
        // PUBLIC MEMBERS
        public int       numVertsBehind     = 0;
        public boolean[] vertBehindState    = new boolean[VERTS_PER_TRI];
        public int[]     vertBehindIndicies = new int[VERTS_PER_TRI];

        /////////////////////////////////////////////////////////////////
        // PUBLIC METHODS
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

    private void internalEnsureShaderRequirements(QShader shader) {
        QShader.ShaderRequirement[] requirements = shader.requirements( );
        for (ShaderRequirement require : requirements) {
            switch (require.requireType) {
                case Attribute:
                    if (slotAttribs[require.slot] == null) {
                        throw new QException(
                            PointOfError.InvalidData, 
                            "Shader requires attibute slot " + require.slot +
                            ". Purpose: " + require.purpose
                        );
                    }
                    break;

                case Uniform:
                    if (slotUniforms[require.slot] == null) {
                        throw new QException(
                            PointOfError.InvalidData, 
                            "Shader requires uniform slot " + require.slot +
                            ". Purpose: " + require.purpose
                        );
                    }
                    break;

                case Texture:
                    if (slotUniforms[require.slot] == null) {
                        throw new QException(
                            PointOfError.InvalidData, 
                            "Shader requires texture slot " + require.slot +
                            ". Purpose: " + require.purpose
                        );
                    }
                    break;
            
                default:
                    throw new QException(
                        PointOfError.InvalidData, 
                        "Unknow requirement type: " + require.requireType.toString( )
                    );
            }
        }
    }

    private QShader internalGetRelevantShader( ) {
        switch (renderMode) {
            case SolidFill:
                return SOLIDFILL_SHADER;

            case Textured:
                return TEXTURED_SHADER;

            case Normal:
                return NORMAL_SHADER;

            case CustomShader:
                return customShader;

            default:
                throw new QException(
                    PointOfError.BadState, 
                    "Unknown renderMode: " + renderMode.toString( )
                );
        }
    }

    private void internalDraw( ) {
        if (renderTarget == null) {
            throw new QException(
                PointOfError.BadState, 
                "Render target unassigned"
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

        for (int triNum = 0; triNum < triCount; triNum++) {
            // GENERATE TRIANGLE
            Triangle tri = new Triangle(triNum);
            internalProcessVertex(tri, 0);
            internalProcessVertex(tri, 1);
            internalProcessVertex(tri, 2);

            // GENERATE TRIANGLE NORMAL
            float[] d01 = new float[VCTR_NUM_CMPS];
            QMath.copy3(d01, tri.getPosn(1));
            QMath.sub3(d01, tri.getPosn(0));

            float[] d02 = new float[VCTR_NUM_CMPS];
            QMath.copy3(d02, tri.getPosn(2));
            QMath.sub3(d02, tri.getPosn(0));

            float[] tempNormal = QMath.cross3(d01, d02);
            QMath.mult3(tempNormal, 1.0f / QMath.mag3(tempNormal));
            QMath.copy3(tri.normal, tempNormal);

            // CULL IF BACKFACING
            if (internalCheckBackfacing(tri)) { continue; }

            // CLIP AND RENDER 
            Triangle[] clipTris = internalClipTri(tri);
            for (Triangle clippedTri : clipTris) {
                internalDrawTri(clippedTri);
            }
        }

    }

    private void internalProcessVertex(Triangle tri, int triVertNum) {
        QShader.VertexShaderContext vctx = new QShader.VertexShaderContext();
        vctx.uniforms   = slotUniforms;
        vctx.textures   = slotTextures;
        vctx.attributes = new float[slotAttribs.length][];

        // POPULATE VERTEX SHADER INPUTS
        for (int slot = 0; slot < slotAttribs.length; slot++) {
            // skip if slot is unused
            QAttribIndexer indexer = slotAttribs[slot];
            if (indexer == null) { continue; }

            // populate attribute slot using indexer
            vctx.attributes[slot] = new float[indexer.getComponentsPerAttrib( )];
            indexer.index(
                tri.triNum, 
                triVertNum, 
                0, 
                vctx.attributes[slot]
            );
        }

        // CALL SHADER
        QShader shader = internalGetRelevantShader( );
        internalEnsureShaderRequirements(shader);
        QVector3 vertShaderOut  = shader.vertexShader(vctx);
        tri.verts[triVertNum].posn          = vertShaderOut.getComponents( );
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
        // NOTE:
        // takes a starting, ending, and middle position, generates
        // interpolation factor between pI to pF (0 for pI, 1 for pF)
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

        float fac02 = internalFindClipInterpolationFactor(
            shuffledTri.getPosn(0), 
            shuffledTri.getPosn(2), 
            pos02
        );
        float fac12 = internalFindClipInterpolationFactor(
            shuffledTri.getPosn(1), 
            shuffledTri.getPosn(2), 
            pos12
        );

        // NOTE:
        // - when 1 vertex is clipped, the resulting mesh is a quad
        // - our quad is 0/2, 0, 1, 1/2 (where a/b is clipped interpolation),
        //   which will be tesselated as (0/2, 0, 1), (0/2, 1, 1/2)

        Triangle quadTri0 = new Triangle(shuffledTri);
        quadTri0.getVertex(2).findClippedShaderOutputs(
            shuffledTri.getVertex(0),
            shuffledTri.getVertex(2), 
            fac02
        );
        quadTri0.setPosn(2, pos02); // (0 1 2) -> (0 1 0/2)
        quadTri0.swapVerts(0, 2);     // (0 1 0/2) -> (0/2 1 0)
        quadTri0.swapVerts(1, 2);     // (0/2 1 0) -> (0/2 0 1)
        
        Triangle quadTri1 = new Triangle(shuffledTri); // (0 1 2)
        quadTri1.getVertex(0).findClippedShaderOutputs(
            shuffledTri.getVertex(0),
            shuffledTri.getVertex(2), 
            fac02
        );
        quadTri1.setPosn(0, pos02); // (0 1 2) -> (0/2 1 2)
        quadTri1.getVertex(2).findClippedShaderOutputs(
            shuffledTri.getVertex(1),
            shuffledTri.getVertex(2), 
            fac12
        );
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
                
                tri.getVertex(1).findClippedShaderOutputs(
                    tri.getVertex(1), 
                    tri.getVertex(0), 
                    fac10
                );
                tri.setPosn(1, pos10);
                
                tri.getVertex(2).findClippedShaderOutputs(
                    tri.getVertex(2), 
                    tri.getVertex(0), 
                    fac20
                );
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
                    pos01
                );
                float fac21 = internalFindClipInterpolationFactor(
                    tri.getPosn(2), 
                    tri.getPosn(1), 
                    pos21
                );
                
                tri.getVertex(0).findClippedShaderOutputs(
                    tri.getVertex(0), 
                    tri.getVertex(1), 
                    fac01
                );
                tri.setPosn(0, pos01);

                tri.getVertex(2).findClippedShaderOutputs(
                    tri.getVertex(2), 
                    tri.getVertex(1), 
                    fac21
                );
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
                    pos02
                );
                float fac12 = internalFindClipInterpolationFactor(
                    tri.getPosn(1), 
                    tri.getPosn(2), 
                    pos12
                );
                
                tri.getVertex(0).findClippedShaderOutputs(
                    tri.getVertex(0), 
                    tri.getVertex(2), 
                    fac02
                );
                tri.setPosn(0, pos02);
                
                tri.getVertex(1).findClippedShaderOutputs(
                    tri.getVertex(1), 
                    tri.getVertex(2), 
                    fac12
                );
                tri.setPosn(1, pos12);

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

    private void internalDrawTri(Triangle tri) {
        
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

    private float[][] internalInterpolateFragmentInputs(
        float[]   baryWeights,
        Triangle  tri
    ) {
        // NOTE:
        //  in principle, all vertex outputs/fragment inputs should be formatted
        //  the same, so we will use v0 as a reference
        Vertex v0 = tri.getVertex(0);
        float[][] buffer = new float[v0.shaderOutputs.length][];

        for (int slot = 0; slot < buffer.length; slot++) {

            float[] outputSlot = v0.shaderOutputs[slot];
            if (outputSlot == null) { continue; }
            buffer[slot] = new float[outputSlot.length];

            // NOTE:
            //  do a perspective-correct interpolation of each vertex output
            float[] output0 = tri.getVertex(0).shaderOutputs[slot];
            float[] output1 = tri.getVertex(1).shaderOutputs[slot];
            float[] output2 = tri.getVertex(2).shaderOutputs[slot];

            // refer to
            // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_rasterizetri.c
            int compCount = outputSlot.length;
            float w0      = tri.getPosnZ(0) * baryWeights[0];
            float w1      = tri.getPosnZ(1) * baryWeights[1];
            float w2      = tri.getPosnZ(2) * baryWeights[2];
            float invWSum = 1.0f / (w0 + w1 + w2);  
            for (int comp = 0; comp < compCount; comp++) {
                buffer[slot][comp] =
                    (w0 * output0[comp] +
                     w1 * output1[comp] + 
                     w2 * output2[comp]) * invWSum;
            }
        }

        return buffer;
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

        // NOTE:
        // since all depths are negative and inverted, the further value
        // will be a smaller negative and hence greater. therefore the failing
        // depth test will be greater than the previous depth
        if (invDepth > renderTarget.getDepth(drawX, drawY) - DEPTH_TEST_EPSILON) {
            return;
        }
        
        // CALL FRAGMENT SHADER
        QShader.FragmentShaderContext fctx = new QShader.FragmentShaderContext();
        fctx.uniforms = slotUniforms;
        fctx.textures = slotTextures;
        fctx.target   = renderTarget;
        fctx.screenX  = drawX;
        fctx.screenY  = drawY;
        fctx.invDepth = invDepth;
        fctx.normal   = new QVector3(triangle.normal);
        fctx.inputsFromVertexShader = internalInterpolateFragmentInputs(weights, triangle);

        QShader shader    = internalGetRelevantShader( );
        QColor  fragColor = shader.fragmentShader(fctx);

        // DISCARD IF FRAGMENT IS TRANSPARENT
        if ((fragColor.getA( )) == 0) {
            return;
        }

        // BLEND FRAGMENT TO BELOW COLOR AND WRITE TO RENDERTARGET
        QColor blendedColor = QShader.blendColor(
            new QColor(renderTarget.getColor(drawX, drawY)), 
            fragColor
        );

        renderTarget.setDepth(drawX, drawY, invDepth);
        renderTarget.setColor(drawX, drawY, blendedColor.toInt());
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
