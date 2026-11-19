// Bailey JT Brown
// 2024-2026
// WobbleShader.java

import ca.bael.QDraw.*;
import ca.bael.QDraw.QSampleable.SampleType;
import ca.bael.QDraw.QShader.ShaderRequirement.RequirementType;

public final class WobbleShader extends QShader {
    public static final int SHADER_TIME_SLOT = 1;

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
                    "vertex transform",
                    QMatrix4x4.class
            ),
            new ShaderRequirement(
                    SHADER_TIME_SLOT, 
                    RequirementType.Uniform, 
                    "time",
                    QScalar.class
            ),
            new ShaderRequirement(
                    QViewer.DEFAULT_SHADER_TEXTURE_SLOT, 
                    RequirementType.Texture, 
                    "face texture"
            ),
            new ShaderRequirement(
                    QViewer.DEFAULT_SHADER_UV_SLOT,
                    RequirementType.Output,
                    "uv",
                    2
            )
        };
    }

    public QVector3 vertexShader(
        VertexShaderContext vctx
    ) {
        // GET SHADER INPUTS
        QVector3   vertexPos = vctx.attribVec3(QViewer.DEFAULT_SHADER_POSITION_SLOT);
        QMatrix4x4 transform =
            new QMatrix4x4(vctx.uniform(QViewer.DEFAULT_SHADER_MATRIX_SLOT, QMatrix4x4.class));
        float      dt        = vctx.uniform(SHADER_TIME_SLOT, QScalar.class).floatValue( ) * 0.35f;

        // FORWARD UV DATA
        vctx.forward(QViewer.DEFAULT_SHADER_UV_SLOT);

        // CALCULATE WOBBLE FACTOR
        float offsetX = 0.03f * QMatrix4x4.cosDeg( dt + (vertexPos.getY() * 35.0f) );
        float offsetY = 0.12f * QMatrix4x4.sinDeg( 35.0f + (dt + vertexPos.getX() * 70.0f) * 0.5f );
        float offsetZ = 0.15f * QMatrix4x4.sinDeg( 90.0f + dt + (vertexPos.getY() * 35.0f) ); 

        // WOBBLE
        transform.multiply(QMatrix4x4.translationMatrix(offsetX, offsetY, offsetZ));
        return QMatrix4x4.multiply(transform, vertexPos);
    }

    public QColor fragmentShader(
        FragmentShaderContext fragInfo
    ) {
        // GET INPUTS
        QSampleable tex = fragInfo.texture(QViewer.DEFAULT_SHADER_TEXTURE_SLOT);
        float[]     uv  = fragInfo.input(QViewer.DEFAULT_SHADER_UV_SLOT, new float[2]);

        // SAMPLE TEXTURE
        return new QColor(tex.sample(uv[0], uv[1], SampleType.Repeat)).setA(200);
    }
}
