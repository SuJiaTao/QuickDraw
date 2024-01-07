// Bailey JT Brown
// 2024
// FuzzShader.java

import QDraw.*;
import QDraw.QShader.ShaderRequirement.RequirementType;

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
                    QVector3[].class
            ),
            new ShaderRequirement(
                    SHADER_TEXTURE_SLOT, 
                    RequirementType.Texture, 
                    "face texture"
            )
        };
    }

    public QVector3 vertexShader(
        VertexShaderContext vctx
    ) {
        // TRANSFORM VERTEX AND NORMALS
        QVector3   vertexPos = new QVector3(vctx.attributes[SHADER_POSITION_SLOT]);
        QMatrix4x4 transform = (QMatrix4x4)vctx.uniforms[SHADER_MATRIX_SLOT];
        QMatrix4x4 rotMtr = transform.extractRotation( );
        QMath.mul3_4x4(
            vctx.attributes[SHADER_NORMAL_SLOT], 
            rotMtr.getComponents( )
        );
        
        // forwardAttributeToFragShader(vctx, SHADER_POSITION_SLOT);
        vertexPos = QMatrix4x4.multiply(transform, vertexPos);
        setOutputToFragShader(vctx, SHADER_POSITION_SLOT, vertexPos.getComponents( ));
        forwardAttributeToFragShader(vctx, SHADER_UV_SLOT);
        forwardAttributeToFragShader(vctx, SHADER_NORMAL_SLOT);

        return vertexPos;
    }

    public QColor fragmentShader(
        FragmentShaderContext fctx
    ) {
        // RETRIEVE FRAG INFO
        float[] pos    = new float[3];
        float[] uv     = new float[2];
        float[] normal = new float[3];
        getOutputFromVertShader(fctx, SHADER_POSITION_SLOT, pos);
        getOutputFromVertShader(fctx, SHADER_UV_SLOT, uv);
        getOutputFromVertShader(fctx, SHADER_NORMAL_SLOT, normal);
        QSampleable tex = fctx.textures[SHADER_TEXTURE_SLOT];
        
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
        QVector3[] lights  = (QVector3[])fctx.uniforms[SHADER_LIGHTS_SLOT];
        for (QVector3 lightPos : lights) {
            QVector3 dFaceLightNormalized = QVector3.sub(
                lightPos,
                new QVector3(pos)
            ).fastNormalize( );

            float diffuse = Math.max(0.0f, QVector3.dot(
                new QVector3(normal).add(randOffset).fastNormalize( ), 
                dFaceLightNormalized
            ));
            
            float phong = (float)Math.pow(diffuse, 10.0f);

            diffuseAccum += (diffuse + phong);
        }
        
        return multiplyColor(
            texCol, 
            Math.min(1.0f, ambient + diffuseAccum)
        );
    }
}
