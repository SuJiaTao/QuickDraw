// Bailey JT Brown
// 2024-2026
// FuzzShader.java

import ca.bael.QDraw.QColor;
import ca.bael.QDraw.QDrawData;
import ca.bael.QDraw.QLight;
import ca.bael.QDraw.QLights;
import ca.bael.QDraw.QMatrix4x4;
import ca.bael.QDraw.QSampleable;
import ca.bael.QDraw.QShader;
import ca.bael.QDraw.QShader.ShaderRequirement.RequirementType;
import ca.bael.QDraw.QVector3;
import ca.bael.QDraw.QViewer;

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
                    QLights.class
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

    private record FuzzData(QMatrix4x4 normalMtr) implements QDrawData { }

    public QDrawData beginDraw(VertexShaderContext vctx) {
        return new FuzzData(vctx.uniform(SHADER_MATRIX_SLOT, QMatrix4x4.class).normalMatrix( ));
    }

    public QVector3 vertexShader(
        VertexShaderContext vctx
    ) {
        // TRANSFORM VERTEX AND NORMALS
        QMatrix4x4 normalMtr = vctx.drawData(FuzzData.class).normalMtr( );
        QMatrix4x4 transform = vctx.uniform(SHADER_MATRIX_SLOT, QMatrix4x4.class);
        QVector3   vertexPos = transform.transformPoint(vctx.attribVec3(SHADER_POSITION_SLOT, vctx.position( )));
        QVector3   normal    = normalMtr.transformDir(vctx.attribVec3(SHADER_NORMAL_SLOT, vctx.scratchVec3(0))).normalize( );

        vctx.output(SHADER_POSITION_SLOT, vertexPos);
        vctx.forward(SHADER_UV_SLOT);
        vctx.output(SHADER_NORMAL_SLOT, normal);

        return vertexPos;
    }

    public QColor fragmentShader(
        FragmentShaderContext fctx
    ) {
        // RETRIEVE FRAG INFO
        QVector3    pos    = fctx.inputVec3(SHADER_POSITION_SLOT, fctx.scratchVec3(0));
        QVector3    normal = fctx.inputVec3(SHADER_NORMAL_SLOT, fctx.scratchVec3(1));
        QSampleable tex    = fctx.texture(SHADER_TEXTURE_SLOT);
        
        // WIGGLE UVS
        final float randDir       = seededRandom(fctx);
        final float wigglemag     = 0.004f;
        final float halfwigglemag = wigglemag * 0.5f;
        float u = fctx.input(SHADER_UV_SLOT, 0) + (randDir * wigglemag) - halfwigglemag;
        float v = fctx.input(SHADER_UV_SLOT, 1) + (randDir * wigglemag) - halfwigglemag;

        // SAMPLE TEXTURE
        QColor texCol = fctx.color( ).set(tex.sample(u, v, QSampleable.SampleType.Repeat));

        // GENERATE RANDOM NORMAL OFFSET BASED ON COLOR
        int seed = texCol.toInt( );
        normal.set(
            normal.getX( ) + 0.3f * seededRandom((seed << 2) + 1),
            normal.getY( ) + 0.3f * seededRandom((seed << 2) + 2),
            normal.getZ( ) + 0.3f * seededRandom((seed << 2) + 3)
        );
        
        // APPLY SIMPLE PHONG SHADING
        float ambient      = 0.4f;
        float diffuseAccum = 0.0f;
        QLights  lights  = fctx.uniform(SHADER_LIGHTS_SLOT, QLights.class);
        QVector3 toLight = fctx.scratchVec3(2);
        for (int i = 0; i < lights.count( ); i++) {
            QLight light = lights.get(i);
            float attenuation = light.illuminate(pos, toLight);

            float diffuse = Math.max(0.0f, QVector3.dot(normal, toLight));

            float phong = (float)Math.pow(diffuse, 10.0f);

            diffuseAccum += (diffuse + phong) * attenuation;
        }
        
        return texCol.multiply(Math.min(1.0f, ambient + diffuseAccum));
    }
}
