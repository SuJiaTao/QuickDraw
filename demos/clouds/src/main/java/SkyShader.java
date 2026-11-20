// Bailey JT Brown
// 2026
// SkyShader.java

import ca.bael.QDraw.QColor;
import ca.bael.QDraw.QMatrix4x4;
import ca.bael.QDraw.QShader;
import ca.bael.QDraw.QShader.ShaderRequirement.RequirementType;
import ca.bael.QDraw.QVector3;
import static ca.bael.QDraw.QViewer.DEFAULT_SHADER_MATRIX_SLOT;
import static ca.bael.QDraw.QViewer.DEFAULT_SHADER_POSITION_SLOT;

public final class SkyShader extends QShader {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final int DIRECTION_OUTPUT = 0;
    private static final float    GROUND_FADE = 8.0f;
    private static final QVector3 ZENITH      = new QVector3(0.18f, 0.36f, 0.75f);
    private static final QVector3 HORIZON     = QColor.unpack(0xDFD9E2, new QVector3( )); //new QVector3(0.70f, 0.82f, 0.95f);
    private static final QVector3 GROUND      = QColor.unpack(0x313E50, new QVector3( ));

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public ShaderRequirement[] requirements( ) {
        return new ShaderRequirement[] {
            new ShaderRequirement(DEFAULT_SHADER_POSITION_SLOT, RequirementType.Attribute, "sphere position"),
            new ShaderRequirement(DEFAULT_SHADER_MATRIX_SLOT, RequirementType.Uniform, "view rotation", QMatrix4x4.class),
            new ShaderRequirement(DIRECTION_OUTPUT, RequirementType.Output, "world direction", 3)
        };
    }

    public QVector3 vertexShader(VertexShaderContext context) {
        context.output(DIRECTION_OUTPUT, context.attribVec3(DEFAULT_SHADER_POSITION_SLOT, context.scratchVec3(0)));
        QMatrix4x4 mtr = context.uniform(DEFAULT_SHADER_MATRIX_SLOT, QMatrix4x4.class);
        return mtr.transformPoint(context.attribVec3(DEFAULT_SHADER_POSITION_SLOT, context.position( )));
    }

    public QColor fragmentShader(FragmentShaderContext context) {
        float    up  = context.inputVec3(DIRECTION_OUTPUT, context.scratchVec3(0)).normalize( ).getY( );
        QVector3 sky = context.scratchVec3(1);
        sky.set(HORIZON);
        if (up < 0.0f) {
            sky.lerp(GROUND, Math.min(1.0f, -up * GROUND_FADE));
        } else {
            sky.lerp(ZENITH, (float)Math.sqrt(up));
        }
        return context.color( ).set(sky);
    }
}
