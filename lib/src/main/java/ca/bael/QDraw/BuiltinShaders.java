// Bailey JT Brown
// 2023-2024
// BuiltinShaders.java

package ca.bael.QDraw;

import static ca.bael.QDraw.QViewer.DEFAULT_SHADER_LIGHTS_SLOT;
import static ca.bael.QDraw.QViewer.DEFAULT_SHADER_MATERIAL_SLOT;
import static ca.bael.QDraw.QViewer.DEFAULT_SHADER_MATRIX_SLOT;
import static ca.bael.QDraw.QViewer.DEFAULT_SHADER_NORMAL_SLOT;
import static ca.bael.QDraw.QViewer.DEFAULT_SHADER_POSITION_SLOT;
import static ca.bael.QDraw.QViewer.DEFAULT_SHADER_TEXTURE_SLOT;
import static ca.bael.QDraw.QViewer.DEFAULT_SHADER_UV_SLOT;

import ca.bael.QDraw.QShader.ShaderRequirement;
import ca.bael.QDraw.QShader.ShaderRequirement.RequirementType;

final class BuiltinShaders {
    ////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public static QShader solidFill( ) {
        return new QShader( ) {
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
    }

    public static QShader textured( ) {
        return new QShader( ) {
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
    }

    public static QShader normal( ) {
        return new QShader( ) {
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
    }

    public static QShader material( ) {
        return new QShader( ) {
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
    }
}
