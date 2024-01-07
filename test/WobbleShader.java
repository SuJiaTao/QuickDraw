// Bailey JT Brown
// 2024
// WobbleShader.java

import QDraw.*;
import QDraw.QSampleable.SampleType;
import QDraw.QShader.ShaderRequirement.RequirementType;

public final class WobbleShader extends QShader {
    public ShaderRequirement[] requirements( ) {
        return new ShaderRequirement[] {
            new ShaderRequirement(
                    QViewer.DEFAULT_SHADER_POSITION_SLOT, 
                    RequirementType.Attribute, 
                    "vertex position"
            ),
            new ShaderRequirement(
                    QViewer.DEFAULT_SHADER_UV_SLOT, 
                    RequirementType.Attribute, 
                    "vertex uv"
            ), 

            new ShaderRequirement(
                    QViewer.DEFAULT_SHADER_MATRIX_SLOT, 
                    RequirementType.Uniform, 
                    "vertex transform"
            ),
            new ShaderRequirement(
                    QViewer.DEFAULT_SHADER_TEXTURE_SLOT, 
                    RequirementType.Texture, 
                    "face texture"
            )
        };
    }

    public QVector3 vertexShader(
        VertexShaderContext vctx
    ) {
        // GET SHADER INPUTS
        QVector3   vertexPos = new QVector3(vctx.attributes[QViewer.DEFAULT_SHADER_POSITION_SLOT]);
        QMatrix4x4 transform = 
            new QMatrix4x4((QMatrix4x4)vctx.uniforms[QViewer.DEFAULT_SHADER_MATRIX_SLOT]);
        Float      dt        = ((Float)vctx.uniforms[1]) * 0.35f;

        // FORWARD UV DATA
        forwardAttributeToFragShader(vctx, QViewer.DEFAULT_SHADER_UV_SLOT);

        // CALCULATE WOBBLE FACTOR
        float offsetX = 0.03f * QMath.cosf( dt + (vertexPos.getY() * 35.0f) );
        float offsetY = 0.12f * QMath.sinf( 35.0f + (dt + vertexPos.getX() * 70.0f) * 0.5f );
        float offsetZ = 0.15f * QMath.sinf( 90.0f + dt + (vertexPos.getY() * 35.0f) ); 

        // WOBBLE
        transform.multiply(QMatrix4x4.translationMatrix(offsetX, offsetY, offsetZ));
        return QMatrix4x4.multiply(transform, vertexPos);
    }

    public QColor fragmentShader(
        FragmentShaderContext fragInfo
    ) {
        // GET INPUTS
        QSampleable tex = fragInfo.textures[QViewer.DEFAULT_SHADER_TEXTURE_SLOT];
        float[]     uv  = new float[2];
        getOutputFromVertShader(fragInfo, QViewer.DEFAULT_SHADER_UV_SLOT, uv);

        // SAMPLE TEXTURE
        return new QColor(tex.sample(uv[0], uv[1], SampleType.Repeat)).setA(200);
    }
}
