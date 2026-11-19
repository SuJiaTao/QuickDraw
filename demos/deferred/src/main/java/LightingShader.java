// Bailey JT Brown
// 2026
// LightingShader.java

import ca.bael.QDraw.*;
import ca.bael.QDraw.QSampleable.SampleType;
import ca.bael.QDraw.QShader.ShaderRequirement.RequirementType;
import static ca.bael.QDraw.QViewer.*;

public final class LightingShader extends QShader {
    public static final int      GBUFFER_TEXTURE_SLOT   = 0;
    public static final int      ALBEDO_TEXTURE_SLOT    = 1;
    public static final int      VIEW_MODE_UNIFORM_SLOT = 3;
    public static final int      INV_VIEW_UNIFORM_SLOT  = 4;
    public static final int      VIEW_POSITION          = 0;
    public static final int      VIEW_NORMAL            = 1;
    public static final int      VIEW_UV                = 2;
    public static final int      VIEW_UNLIT             = 3;
    public static final int      VIEW_LIT               = 4;
    public static final String[] VIEW_NAMES             = { "position", "normal", "uv", "texture (unlit)", "texture (lit)" };
    public static final float    POSITION_BAND          = 4.0f;
    public static final QColor   BACKGROUND             = new QColor(0x10, 0x10, 0x18);

    private final int   width;
    private final int   height;
    private final float viewLeft;
    private final float viewRight;
    private final float viewBottom;
    private final float viewTop;
    private final QVector3 worldMin;
    private final QVector3 worldMax;

    public LightingShader(int width, int height, float viewLeft, float viewRight, float viewBottom, float viewTop,
                          QVector3 worldMin, QVector3 worldMax) {
        this.width      = width;
        this.height     = height;
        this.viewLeft   = viewLeft;
        this.viewRight  = viewRight;
        this.viewBottom = viewBottom;
        this.viewTop    = viewTop;
        this.worldMin   = worldMin;
        this.worldMax   = worldMax;
    }

    public ShaderRequirement[] requirements( ) {
        return new ShaderRequirement[] {
            new ShaderRequirement(DEFAULT_SHADER_POSITION_SLOT, RequirementType.Attribute, "screen quad position"),
            new ShaderRequirement(GBUFFER_TEXTURE_SLOT, RequirementType.Texture, "g-buffer source"),
            new ShaderRequirement(ALBEDO_TEXTURE_SLOT, RequirementType.Texture, "mascot albedo"),
            new ShaderRequirement(DEFAULT_SHADER_LIGHTS_SLOT, RequirementType.Uniform, "view-space lights", QLights.class),
            new ShaderRequirement(DEFAULT_SHADER_MATERIAL_SLOT, RequirementType.Uniform, "lit material", QMaterial.class),
            new ShaderRequirement(VIEW_MODE_UNIFORM_SLOT, RequirementType.Uniform, "view mode", QScalar.class),
            new ShaderRequirement(INV_VIEW_UNIFORM_SLOT, RequirementType.Uniform, "view-to-world matrix", QMatrix4x4.class)
        };
    }

    public QColor fragmentShader(FragmentShaderContext context) {
        float invDepth = context.gbufferDepth(GBUFFER_TEXTURE_SLOT);
        if (invDepth == 0.0f) {
            return context.color( ).set(BACKGROUND);
        }
        if (context.gbufferLong(GBUFFER_TEXTURE_SLOT, GBufferShader.ID_GBUFFER) == GBufferShader.ID_LIGHT_MARKER) {
            return context.color( ).set(0xFF, 0xFF, 0xFF);
        }

        switch (context.uniform(VIEW_MODE_UNIFORM_SLOT, QScalar.class).intValue( )) {
            case VIEW_POSITION: {
                QVector3 p = reconstructPosition(context, invDepth, context.scratchVec3(0));
                context.uniform(INV_VIEW_UNIFORM_SLOT, QMatrix4x4.class).transformPoint(p);
                return context.color( ).set(
                    normalized(p.getX( ), worldMin.getX( ), worldMax.getX( )),
                    normalized(p.getY( ), worldMin.getY( ), worldMax.getY( )),
                    normalized(p.getZ( ), worldMin.getZ( ), worldMax.getZ( ))
                );
            }
            case VIEW_NORMAL: {
                QVector3 n = context.gbufferVec3(GBUFFER_TEXTURE_SLOT, GBufferShader.NORMAL_GBUFFER, context.scratchVec3(0));
                return context.color( ).set(
                    (int)((1.0f + n.getX( )) * 127.0f),
                    (int)((1.0f + n.getY( )) * 127.0f),
                    (int)((1.0f + n.getZ( )) * 127.0f)
                );
            }
            case VIEW_UV: {
                QVector3 uv = context.gbufferVec3(GBUFFER_TEXTURE_SLOT, GBufferShader.UV_GBUFFER, context.scratchVec3(0));
                return context.color( ).set(band(uv.getX( ) * POSITION_BAND), band(uv.getY( ) * POSITION_BAND), 0);
            }
            case VIEW_UNLIT: {
                QVector3 uv = context.gbufferVec3(GBUFFER_TEXTURE_SLOT, GBufferShader.UV_GBUFFER, context.scratchVec3(0));
                return context.color( ).set(context.texture(ALBEDO_TEXTURE_SLOT).sample(uv.getX( ), uv.getY( ), SampleType.Repeat));
            }
            default:
                return shadeLit(context, invDepth);
        }
    }

    private QColor shadeLit(FragmentShaderContext context, float invDepth) {
        QMaterial mat    = context.uniform(DEFAULT_SHADER_MATERIAL_SLOT, QMaterial.class);
        QLights   lights = context.uniform(DEFAULT_SHADER_LIGHTS_SLOT, QLights.class);
        QVector3  pos    = reconstructPosition(context, invDepth, context.scratchVec3(0));
        QVector3  normal = context.gbufferVec3(GBUFFER_TEXTURE_SLOT, GBufferShader.NORMAL_GBUFFER, context.scratchVec3(1));
        QVector3  uv     = context.gbufferVec3(GBUFFER_TEXTURE_SLOT, GBufferShader.UV_GBUFFER, context.scratchVec3(2));
        QColor    albedo = context.color( ).set(context.texture(ALBEDO_TEXTURE_SLOT).sample(uv.getX( ), uv.getY( ), SampleType.Repeat));

        QVector3 viewDir = context.scratchVec3(2);
        viewDir.set(pos);
        viewDir.multiply3(-1.0f).normalize( );
        QVector3 toLight = context.scratchVec3(3);

        float diffuse  = 0.0f;
        float specular = 0.0f;
        for (int i = 0; i < lights.count( ); i++) {
            float attenuation = lights.get(i).illuminate(pos, toLight);
            float lambert     = QVector3.dot(toLight, normal);
            if (lambert <= 0.0f) {
                continue;
            }
            diffuse += lambert * attenuation;
            float specDot = QVector3.dot(toLight.add(viewDir).normalize( ), normal);
            specular += attenuation * (float)Math.pow(Math.max(0.0f, specDot), mat.shininess);
        }

        return albedo.set(
            (int)(albedo.getR( ) * Math.min(1.0f, mat.ambient.getR( ) / 255.0f + diffuse * mat.diffuse.getR( ) / 255.0f) + mat.specular.getR( ) * specular),
            (int)(albedo.getG( ) * Math.min(1.0f, mat.ambient.getG( ) / 255.0f + diffuse * mat.diffuse.getG( ) / 255.0f) + mat.specular.getG( ) * specular),
            (int)(albedo.getB( ) * Math.min(1.0f, mat.ambient.getB( ) / 255.0f + diffuse * mat.diffuse.getB( ) / 255.0f) + mat.specular.getB( ) * specular)
        );
    }

    private QVector3 reconstructPosition(FragmentShaderContext context, float invDepth, QVector3 out) {
        float ndcX = viewLeft + (context.screenX( ) + 0.5f) / width * (viewRight - viewLeft);
        float ndcY = viewBottom + (context.screenY( ) + 0.5f) / height * (viewTop - viewBottom);
        out.set(-ndcX / invDepth, -ndcY / invDepth, 1.0f / invDepth);
        return out;
    }

    private static int band(float v) {
        float scaled = v / POSITION_BAND;
        return (int)((scaled - (float)Math.floor(scaled)) * 255.0f);
    }

    private static int normalized(float v, float min, float max) {
        return (int)(Math.min(1.0f, Math.max(0.0f, (v - min) / (max - min))) * 255.0f);
    }
}
