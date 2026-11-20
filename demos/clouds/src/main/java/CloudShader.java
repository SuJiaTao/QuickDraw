// Bailey JT Brown
// 2026
// CloudShader.java

import ca.bael.QDraw.QColor;
import ca.bael.QDraw.QDrawData;
import ca.bael.QDraw.QMatrix4x4;
import ca.bael.QDraw.QScalar;
import ca.bael.QDraw.QShader;
import ca.bael.QDraw.QShader.ShaderRequirement.RequirementType;
import ca.bael.QDraw.QVector3;
import static ca.bael.QDraw.QViewer.DEFAULT_SHADER_POSITION_SLOT;

public final class CloudShader extends QShader {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final int   CAMERA_TO_WORLD_SLOT = 0;
    public static final int   TIME_SLOT            = 1;
    public static final int   DEPTH_TEXTURE_SLOT   = 0;
    public static final float CLOUD_BASE           = 40.0f;
    public static final float CLOUD_TOP            = 70.0f;
    public static final float MAX_DISTANCE         = 400.0f;
    public static final int   STEPS                = 32;
    public static final int   LIGHT_STEPS          = 4;
    public static final float LIGHT_STEP           = 4.0f;
    public static final float SHAPE_SCALE          = 0.005f;
    public static final float COVERAGE             = 0.5f;
    public static final float DENSITY              = 1.0f;
    public static final float EXTINCTION           = 0.08f;
    public static final float PHASE_G              = 0.3f;
    public static final float MIN_TRANSMITTANCE    = 0.01f;
    public static final float MIN_DIRECTION_Y      = 1e-4f;
    public static final float WIND_X               = 4.0f;
    public static final float WIND_Z               = 1.5f;
    private static final QVector3 SUN_DIRECTION    = new QVector3(0.5f, 0.6f, -0.4f).normalize( );
    private static final QVector3 SUN_COLOR        = new QVector3(1.0f, 0.95f, 0.85f);
    private static final QVector3 AMBIENT_COLOR    = new QVector3(0.55f, 0.65f, 0.8f);

    /////////////////////////////////////////////////////////////////
    // PRIVATE CLASSES
    private record CloudData(QMatrix4x4 camToWorld, float camX, float camY, float camZ, float windX, float windZ) implements QDrawData { }

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private final CloudVolume volume;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public ShaderRequirement[] requirements( ) {
        return new ShaderRequirement[] {
            new ShaderRequirement(DEFAULT_SHADER_POSITION_SLOT, RequirementType.Attribute, "screen quad position"),
            new ShaderRequirement(CAMERA_TO_WORLD_SLOT, RequirementType.Uniform, "camera-to-world matrix", QMatrix4x4.class),
            new ShaderRequirement(TIME_SLOT, RequirementType.Uniform, "time in seconds", QScalar.class),
            new ShaderRequirement(DEPTH_TEXTURE_SLOT, RequirementType.Texture, "scene depth copy")
        };
    }

    public QDrawData beginDraw(VertexShaderContext context) {
        QMatrix4x4 camToWorld = context.uniform(CAMERA_TO_WORLD_SLOT, QMatrix4x4.class);
        QVector3   camPos     = camToWorld.transformPoint(new QVector3( ));
        float      time       = context.uniform(TIME_SLOT, QScalar.class).floatValue( );
        return new CloudData(camToWorld, camPos.getX( ), camPos.getY( ), camPos.getZ( ), WIND_X * time, WIND_Z * time);
    }

    public QColor fragmentShader(FragmentShaderContext context) {
        CloudData data = context.drawData(CloudData.class);
        QVector3  dir  = data.camToWorld( ).transformDir(context.viewDirection(context.scratchVec3(0)));
        float     dx   = dir.getX( );
        float     dy   = dir.getY( );
        float     dz   = dir.getZ( );

        float invDy  = 1.0f / (Math.abs(dy) < MIN_DIRECTION_Y ? Math.copySign(MIN_DIRECTION_Y, dy) : dy);
        float t0     = (CLOUD_BASE - data.camY( )) * invDy;
        float t1     = (CLOUD_TOP - data.camY( )) * invDy;
        float tEnter = Math.max(0.0f, Math.min(t0, t1));
        float tExit  = Math.min(MAX_DISTANCE, Math.max(t0, t1));
        float invDepth = context.gbufferDepth(DEPTH_TEXTURE_SLOT);
        if (invDepth != 0.0f) {
            tExit = Math.min(tExit, context.viewPosition(invDepth, context.scratchVec3(1)).magnitude( ));
        }
        if (tExit <= tEnter) {
            return context.color( ).set(0x00000000);
        }

        float step  = (tExit - tEnter) / STEPS;
        float t     = tEnter + step * context.random( );
        float phase = internalPhase(dx * SUN_DIRECTION.getX( ) + dy * SUN_DIRECTION.getY( ) + dz * SUN_DIRECTION.getZ( ));
        float trans = 1.0f;
        float r     = 0.0f;
        float g     = 0.0f;
        float b     = 0.0f;
        for (int i = 0; i < STEPS && trans > MIN_TRANSMITTANCE; i++, t += step) {
            float px = data.camX( ) + dx * t;
            float py = data.camY( ) + dy * t;
            float pz = data.camZ( ) + dz * t;
            float d  = internalDensity(data, px, py, pz);
            if (d <= 0.0f) {
                continue;
            }
            float sun     = internalLightMarch(data, px, py, pz) * phase;
            float ambient = 0.5f + 0.5f * (py - CLOUD_BASE) / (CLOUD_TOP - CLOUD_BASE);
            float dTrans  = (float)Math.exp(-d * step * EXTINCTION);
            float w       = trans * (1.0f - dTrans);
            r     += w * (SUN_COLOR.getX( ) * sun + AMBIENT_COLOR.getX( ) * ambient);
            g     += w * (SUN_COLOR.getY( ) * sun + AMBIENT_COLOR.getY( ) * ambient);
            b     += w * (SUN_COLOR.getZ( ) * sun + AMBIENT_COLOR.getZ( ) * ambient);
            trans *= dTrans;
        }

        float alpha = 1.0f - trans;
        if (alpha <= 0.0f) {
            return context.color( ).set(0x00000000);
        }
        QVector3 rgb = context.scratchVec3(1);
        rgb.set(r / alpha, g / alpha, b / alpha);
        return context.color( ).set(rgb, alpha);
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private float internalDensity(CloudData data, float px, float py, float pz) {
        float h = (py - CLOUD_BASE) / (CLOUD_TOP - CLOUD_BASE);
        if (h <= 0.0f || h >= 1.0f) {
            return 0.0f;
        }
        float profile = Math.min(1.0f, h * 4.0f) * Math.min(1.0f, (1.0f - h) * 2.5f);
        float shape   = volume.sample((px + data.windX( )) * SHAPE_SCALE, py * SHAPE_SCALE, (pz + data.windZ( )) * SHAPE_SCALE);
        float covered = (shape * profile - (1.0f - COVERAGE)) / COVERAGE;
        return Math.max(0.0f, Math.min(1.0f, covered)) * DENSITY;
    }

    private float internalLightMarch(CloudData data, float px, float py, float pz) {
        float sum = 0.0f;
        for (int i = 1; i <= LIGHT_STEPS; i++) {
            float s = LIGHT_STEP * i;
            sum += internalDensity(data, px + SUN_DIRECTION.getX( ) * s, py + SUN_DIRECTION.getY( ) * s, pz + SUN_DIRECTION.getZ( ) * s);
        }
        return (float)Math.exp(-sum * LIGHT_STEP * EXTINCTION);
    }

    private static float internalPhase(float cosTheta) {
        float g2 = PHASE_G * PHASE_G;
        return (1.0f - g2) / (float)Math.pow(1.0f + g2 - 2.0f * PHASE_G * cosTheta, 1.5);
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public CloudShader(CloudVolume volume) {
        this.volume = volume;
    }
}
