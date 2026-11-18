// Bailey JT Brown
// 2024
// FuzzShader.java

import ca.bael.QDraw.*;
import ca.bael.QDraw.QShader.ShaderRequirement.RequirementType;

public class FuzzShader extends QShader {
    public static final int SHADER_POSITION_SLOT = QViewer.DEFAULT_SHADER_POSITION_SLOT;
    public static final int SHADER_UV_SLOT       = QViewer.DEFAULT_SHADER_UV_SLOT;
    public static final int SHADER_NORMAL_SLOT   = QViewer.DEFAULT_SHADER_NORMAL_SLOT;
    public static final int SHADER_MATRIX_SLOT   = QViewer.DEFAULT_SHADER_MATRIX_SLOT;
    public static final int SHADER_LIGHTS_SLOT   = QViewer.DEFAULT_SHADER_LIGHTS_SLOT;
    public static final int SHADER_TEXTURE_SLOT  = QViewer.DEFAULT_SHADER_TEXTURE_SLOT;

    public ShaderRequirement[] requirements( ) {
        return new ShaderRequirement[] {
            new ShaderRequirement(
                    SHADER_POSITION_SLOT, 
                    RequirementType.Attribute, 
                    "vertex position"
            ),
            new ShaderRequirement(
                    SHADER_UV_SLOT, 
                    RequirementType.Attribute, 
                    "vertex uv"
            ), 
            new ShaderRequirement(
                    SHADER_NORMAL_SLOT, 
                    RequirementType.Attribute, 
                    "vertex normal"
            ), 
            new ShaderRequirement(
                    SHADER_MATRIX_SLOT, 
                    RequirementType.Uniform, 
                    "vertex transform",
                    QMatrix4x4.class
            ),
            new ShaderRequirement(
                    SHADER_LIGHTS_SLOT, 
                    RequirementType.Uniform, 
                    "point light position array",
                    QLight[].class
            ),
            new ShaderRequirement(
                    SHADER_TEXTURE_SLOT, 
                    RequirementType.Texture, 
                    "face texture"
            ),
            new ShaderRequirement(
                    SHADER_POSITION_SLOT, 
                    RequirementType.Output, 
                    "view-space position",
                    3
            ),
            new ShaderRequirement(
                    SHADER_UV_SLOT, 
                    RequirementType.Output, 
                    "uv",
                    2
            ),
            new ShaderRequirement(
                    SHADER_NORMAL_SLOT, 
                    RequirementType.Output, 
                    "normal",
                    3
            )
        };
    }

    private QMatrix4x4 normalMtr;

    public void beginDraw(VertexShaderContext vctx) {
        normalMtr = vctx.uniform(SHADER_MATRIX_SLOT, QMatrix4x4.class).normalMatrix( );
    }

    public QVector3 vertexShader(
        VertexShaderContext vctx
    ) {
        // TRANSFORM VERTEX AND NORMALS
        QMatrix4x4 transform = vctx.uniform(SHADER_MATRIX_SLOT, QMatrix4x4.class);
        QVector3   vertexPos = transform.transformPoint(vctx.attribVec3(SHADER_POSITION_SLOT));
        QVector3   normal    = normalMtr.transformDir(vctx.attribVec3(SHADER_NORMAL_SLOT)).normalize( );

        vctx.output(SHADER_POSITION_SLOT, vertexPos);
        vctx.forward(SHADER_UV_SLOT);
        vctx.output(SHADER_NORMAL_SLOT, normal);

        return vertexPos;
    }

    public QColor fragmentShader(
        FragmentShaderContext fctx
    ) {
        // RETRIEVE FRAG INFO
        QVector3    pos    = fctx.inputVec3(SHADER_POSITION_SLOT);
        float[]     uv     = fctx.input(SHADER_UV_SLOT, new float[2]);
        QVector3    normal = fctx.inputVec3(SHADER_NORMAL_SLOT);
        QSampleable tex    = fctx.texture(SHADER_TEXTURE_SLOT);
        
        // WIGGLE UVS
        final float randDir       = seededRandom(fctx);
        final float wigglemag     = 0.004f;
        final float halfwigglemag = wigglemag * 0.5f;
        uv[0] += (randDir * wigglemag) - halfwigglemag;
        uv[1] += (randDir * wigglemag) - halfwigglemag;

        // SAMPLE TEXTURE
        QColor texCol = new QColor(tex.sample(uv[0], uv[1], QSampleable.SampleType.Repeat));

        // GENERATE RANDOM NORMAL OFFSET BASED ON COLOR
        QVector3 randOffset = seededRandomVector(texCol.toInt( )).multiply3(0.3f);
        
        // APPLY SIMPLE PHONG SHADING
        float ambient      = 0.4f;
        float diffuseAccum = 0.0f;
        QLight[] lights  = fctx.uniform(SHADER_LIGHTS_SLOT, QLight[].class);
        for (QLight light : lights) {
            QVector3 dFaceLight = QVector3.sub(
                light.position,
                pos
            );

            float dist = dFaceLight.fastMagnitude( );
            dFaceLight.multiply3(1.0f / dist);

            float diffuse = Math.max(0.0f, QVector3.dot(
                normal.copy( ).add(randOffset), 
                dFaceLight
            ));
            
            float phong = (float)Math.pow(diffuse, 10.0f);

            diffuseAccum += (diffuse + phong) * (light.strength / dist);
        }
        
        return texCol.multiply(Math.min(1.0f, ambient + diffuseAccum));
    }
}
