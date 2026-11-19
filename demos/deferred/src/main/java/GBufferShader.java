// Bailey JT Brown
// 2024
// GBufferShader.java

import ca.bael.QDraw.*;
import ca.bael.QDraw.QRenderBuffer.GBufferFormat;
import ca.bael.QDraw.QShader.ShaderRequirement.RequirementType;
import static ca.bael.QDraw.QViewer.*;

public final class GBufferShader extends QShader {
    public static final int  NORMAL_GBUFFER  = 0;
    public static final int  UV_GBUFFER      = 1;
    public static final int  ID_GBUFFER      = 2;
    public static final int  ID_UNIFORM_SLOT = 1;
    public static final long ID_MASCOT       = 0;
    public static final long ID_LIGHT_MARKER = 1;

    private record GBufferData(QMatrix4x4 normalMtr, OutputSlot uvSlot, OutputSlot normalSlot, long id) implements QDrawData { }

    public ShaderRequirement[] requirements( ) {
        return new ShaderRequirement[] {
            new ShaderRequirement(DEFAULT_SHADER_POSITION_SLOT, RequirementType.Attribute, "vertex position"),
            new ShaderRequirement(DEFAULT_SHADER_UV_SLOT, RequirementType.Attribute, "vertex uv"),
            new ShaderRequirement(DEFAULT_SHADER_NORMAL_SLOT, RequirementType.Attribute, "vertex normal"),
            new ShaderRequirement(DEFAULT_SHADER_MATRIX_SLOT, RequirementType.Uniform, "model-view matrix", QMatrix4x4.class),
            new ShaderRequirement(ID_UNIFORM_SLOT, RequirementType.Uniform, "material id", QScalar.class),
            new ShaderRequirement(DEFAULT_SHADER_UV_SLOT, RequirementType.Output, "uv", 2),
            new ShaderRequirement(DEFAULT_SHADER_NORMAL_SLOT, RequirementType.Output, "view-space normal", 3),
            new ShaderRequirement(NORMAL_GBUFFER, RequirementType.GBuffer, "view-space normal", GBufferFormat.Float3),
            new ShaderRequirement(UV_GBUFFER, RequirementType.GBuffer, "uv", GBufferFormat.Float3),
            new ShaderRequirement(ID_GBUFFER, RequirementType.GBuffer, "material id", GBufferFormat.Long)
        };
    }

    public QDrawData beginDraw(VertexShaderContext context) {
        return new GBufferData(
            context.uniform(DEFAULT_SHADER_MATRIX_SLOT, QMatrix4x4.class).normalMatrix( ),
            context.outputSlot(DEFAULT_SHADER_UV_SLOT),
            context.outputSlot(DEFAULT_SHADER_NORMAL_SLOT),
            context.uniform(ID_UNIFORM_SLOT, QScalar.class).intValue( )
        );
    }

    public QVector3 vertexShader(VertexShaderContext context) {
        GBufferData data = context.drawData(GBufferData.class);
        QMatrix4x4  mtr  = context.uniform(DEFAULT_SHADER_MATRIX_SLOT, QMatrix4x4.class);
        context.forward(data.uvSlot( ));
        context.output(data.normalSlot( ), data.normalMtr( ).transformDir(context.attribVec3(DEFAULT_SHADER_NORMAL_SLOT)).normalize( ));
        return mtr.transformPoint(context.attribVec3(DEFAULT_SHADER_POSITION_SLOT, context.position( )));
    }

    public QColor fragmentShader(FragmentShaderContext context) {
        GBufferData data = context.drawData(GBufferData.class);
        context.outputGBuffer(NORMAL_GBUFFER, context.inputVec3(data.normalSlot( ), context.scratchVec3(0)).normalize( ));
        QVector3 uv = context.scratchVec3(1);
        uv.set(context.input(data.uvSlot( ), 0), context.input(data.uvSlot( ), 1), 0.0f);
        context.outputGBuffer(UV_GBUFFER, uv);
        context.outputGBuffer(ID_GBUFFER, data.id( ));
        return context.color( ).set(0xFF, 0xFF, 0xFF);
    }
}
