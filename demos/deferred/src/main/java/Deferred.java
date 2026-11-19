// Bailey JT Brown
// 2024
// Deferred.java

import java.awt.event.KeyEvent;
import java.util.Random;

import ca.bael.QDraw.*;
import ca.bael.QDraw.QRenderBuffer.GBufferFormat;
import ca.bael.QDraw.QViewer.RenderMode;

public final class Deferred {
    public static final int   WINDOW_WIDTH     = 1200;
    public static final int   WINDOW_HEIGHT    = 960;
    public static final int   FB_WIDTH         = WINDOW_WIDTH >> 1;
    public static final int   FB_HEIGHT        = WINDOW_HEIGHT >> 1;
    public static final float FB_ASPECT        = (float) FB_WIDTH / (float) FB_HEIGHT;
    public static final int   LATTICE_X        = 16;
    public static final int   LATTICE_Y        = 1;
    public static final int   LATTICE_Z        = 16;
    public static final float LATTICE_SPACING  = 2.2f;
    public static final float MASCOT_SCALE     = 0.2f;
    public static final float MASCOT_HEIGHT    = 9.6f * MASCOT_SCALE;
    public static final float CAMERA_YAW       = 45.0f;
    public static final float CAMERA_PITCH     = -35.264f;
    public static final float CAMERA_DISTANCE  = 3.0f * MASCOT_HEIGHT;
    public static final float CAMERA_TURN_RATE = 60.0f;
    public static final int   LIGHT_COUNT      = 8;
    public static final float LIGHT_STRENGTH   = 3.0f;
    public static final float LIGHT_HEIGHT     = 3.0f;
    public static final float LIGHT_BOB        = 2.0f;
    public static final float MARKER_SCALE     = 0.3f;
    public static final long  LIGHT_SEED       = 0x5EED;

    public static final QMesh      MASCOT_MESH    = new QMesh(Deferred.class.getResource("/Mascot_Smooth.obj").getPath());
    public static final QMesh      SPHERE_MESH    = new QMesh(Deferred.class.getResource("/UVSphere_Low.obj").getPath());
    public static final QMesh      SCREEN_QUAD    = QMesh.screenQuad();
    public static final QTexture   MASCOT_TEXTURE = new QTexture(Deferred.class.getResource("/MascotFuzzy256.png").getPath());
    public static final QMaterial  LIT_MATERIAL   = new QMaterial(new QColor(0x28, 0x28, 0x28), QColor.White(), QColor.White(), 32.0f);
    public static final QScalar    MASCOT_ID      = new QScalar(GBufferShader.ID_MASCOT);
    public static final QScalar    MARKER_ID      = new QScalar(GBufferShader.ID_LIGHT_MARKER);

    private static final float[][] LIGHT_PARAMS = makeLightParams();

    private static float[][] makeLightParams() {
        Random random = new Random(LIGHT_SEED);
        float[][] params = new float[LIGHT_COUNT][12];
        for (float[] light : params) {
            for (int i = 0; i < light.length; i += 2) {
                light[i]     = 0.3f + 0.9f * random.nextFloat();
                light[i + 1] = (float) (2.0 * Math.PI * random.nextFloat());
            }
        }
        return params;
    }

    private static float wave(float[] p, int i, float t) {
        return (float) (Math.sin(p[i] * t + p[i + 1]) * Math.cos(p[i + 2] * t + p[i + 3]));
    }

    private static QVector3 lightWorldPosition(int light, float t) {
        float[] p     = LIGHT_PARAMS[light];
        float   halfX = 0.5f * LATTICE_X * LATTICE_SPACING;
        float   halfZ = 0.5f * LATTICE_Z * LATTICE_SPACING;
        return new QVector3(
            halfX * wave(p, 0, t),
            LIGHT_HEIGHT + LIGHT_BOB * wave(p, 4, t),
            halfZ * wave(p, 8, t)
        );
    }

    private static QMatrix4x4 viewMatrix(float yaw) {
        return QMatrix4x4.Identity()
                .rotate(0.0f, yaw, 0.0f)
                .rotate(CAMERA_PITCH, 0.0f, 0.0f)
                .translate(0.0f, 0.0f, -CAMERA_DISTANCE);
    }

    private static QMatrix4x4 modelView(QMatrix4x4 view, QVector3 position, float scale) {
        return QMatrix4x4.multiply(QMatrix4x4.TRS(position, QVector3.Zero(), QVector3.One().multiply3(scale)), view);
    }

    public static void main(String[] args) {
        QWindow       window      = new QWindow("Deferred", WINDOW_WIDTH, WINDOW_HEIGHT);
        QRenderBuffer frameBuffer = new QRenderBuffer(FB_WIDTH, FB_HEIGHT);
        QRenderBuffer gbuffer     = new QRenderBuffer(FB_WIDTH, FB_HEIGHT);
        gbuffer.attachGBuffer(GBufferShader.NORMAL_GBUFFER, GBufferFormat.Float3);
        gbuffer.attachGBuffer(GBufferShader.UV_GBUFFER, GBufferFormat.Float3);
        gbuffer.attachGBuffer(GBufferShader.ID_GBUFFER, GBufferFormat.Long);
        window.setRenderBuffer(frameBuffer);

        QViewer geomPass = new QViewer(gbuffer, FB_ASPECT);
        geomPass.setRenderMode(RenderMode.CustomShader);
        geomPass.setCustomShader(new GBufferShader());

        QViewer lightPass = new QViewer(frameBuffer);
        lightPass.setRenderMode(RenderMode.CustomShader);
        lightPass.setCustomShader(new LightingShader(FB_WIDTH, FB_HEIGHT, -FB_ASPECT, FB_ASPECT, -1.0f, 1.0f));

        int     view       = LightingShader.VIEW_LIT;
        boolean prevUp     = false;
        boolean prevDown   = false;
        float   yaw        = CAMERA_YAW;
        float   prevTime   = 0.0f;
        int     fpsFrames  = 0;
        long    fpsStart   = System.nanoTime();
        long    t0         = System.nanoTime();
        System.out.println("view: " + LightingShader.VIEW_NAMES[view]);

        while (true) {
            boolean up   = window.isKeyDown(KeyEvent.VK_UP);
            boolean down = window.isKeyDown(KeyEvent.VK_DOWN);
            if ((up && !prevUp) || (down && !prevDown)) {
                int count = LightingShader.VIEW_NAMES.length;
                view = (view + (up && !prevUp ? count - 1 : 1)) % count;
                System.out.println("view: " + LightingShader.VIEW_NAMES[view]);
            }
            prevUp   = up;
            prevDown = down;
            float time = (System.nanoTime() - t0) * 1e-9f;
            float turn = (window.isKeyDown(KeyEvent.VK_RIGHT) ? 1.0f : 0.0f) - (window.isKeyDown(KeyEvent.VK_LEFT) ? 1.0f : 0.0f);
            yaw += turn * CAMERA_TURN_RATE * (time - prevTime);
            prevTime = time;
            QMatrix4x4 viewMtr = viewMatrix(yaw);

            geomPass.clearFrame();
            for (int x = 0; x < LATTICE_X; x++) {
                for (int y = 0; y < LATTICE_Y; y++) {
                    for (int z = 0; z < LATTICE_Z; z++) {
                        geomPass.setMatrix(modelView(viewMtr, new QVector3(
                            (x - 0.5f * (LATTICE_X - 1)) * LATTICE_SPACING,
                            (y - 0.5f * (LATTICE_Y - 1)) * LATTICE_SPACING,
                            (z - 0.5f * (LATTICE_Z - 1)) * LATTICE_SPACING), MASCOT_SCALE));
                        geomPass.setUniformSlot(GBufferShader.ID_UNIFORM_SLOT, MASCOT_ID);
                        geomPass.drawMesh(MASCOT_MESH);
                    }
                }
            }

            QLight[] lights = new QLight[LIGHT_COUNT];
            for (int i = 0; i < LIGHT_COUNT; i++) {
                QVector3 world = lightWorldPosition(i, time);
                geomPass.setMatrix(modelView(viewMtr, world, MARKER_SCALE));
                geomPass.setUniformSlot(GBufferShader.ID_UNIFORM_SLOT, MARKER_ID);
                geomPass.drawMesh(SPHERE_MESH);
                lights[i] = new QLight(viewMtr.transformPoint(world), LIGHT_STRENGTH);
            }
            geomPass.flush();

            lightPass.clearFrame();
            lightPass.setTextureSlot(LightingShader.GBUFFER_TEXTURE_SLOT, gbuffer);
            lightPass.setTextureSlot(LightingShader.ALBEDO_TEXTURE_SLOT, MASCOT_TEXTURE);
            lightPass.setLights(new QLights(lights));
            lightPass.setUniformSlot(QViewer.DEFAULT_SHADER_MATERIAL_SLOT, LIT_MATERIAL);
            lightPass.setUniformSlot(LightingShader.VIEW_MODE_UNIFORM_SLOT, new QScalar(view));
            lightPass.drawMesh(SCREEN_QUAD);
            lightPass.flush();
            window.updateFrame();

            fpsFrames++;
            long now = System.nanoTime();
            if (now - fpsStart >= 1_000_000_000L) {
                System.out.printf("%d fps (%.2f ms/frame, g-buffer %.2f geom %.2f raster, lighting %.2f raster)%n",
                    fpsFrames, (now - fpsStart) / 1e6 / fpsFrames,
                    geomPass.geometryTimer.millisPer(fpsFrames), geomPass.rasterTimer.millisPer(fpsFrames),
                    lightPass.rasterTimer.millisPer(fpsFrames));
                geomPass.geometryTimer.reset();
                geomPass.rasterTimer.reset();
                lightPass.rasterTimer.reset();
                fpsFrames = 0;
                fpsStart  = now;
            }
        }
    }
}
