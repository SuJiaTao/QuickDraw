// Bailey JT Brown
// 2024
// Mascot.java

import QDraw.*;
import QDraw.QSampleable.SampleType;
import QDraw.QViewer.RenderMode;

public final class Mascot {
    public static final int WINDOW_WIDTH    = 1200;
    public static final int WINDOW_HEIGHT   = 960; 
    public static final int FB_WIDTH        = WINDOW_WIDTH  >> 1;
    public static final int FB_HEIGHT       = WINDOW_HEIGHT >> 1;
    public static final float FB_ASPECT     = (float)FB_WIDTH / (float)FB_HEIGHT;
    public static final int CUT_RUNTIME_SEC = 15;
    public static final int CUT_RUNTIME_MS  = 1000 * CUT_RUNTIME_SEC;

    public static final QMesh MASCOT_MESH = 
        new QMesh(System.getProperty("user.dir") + "\\resources\\Mascot_Smooth.obj");
    public static final QMesh CUBE_MESH = 
        new QMesh(System.getProperty("user.dir") + "\\resources\\Cube.obj");
    public static final QTexture MASCOT_TEXTURE = 
        new QTexture(System.getProperty("user.dir") + "\\resources\\MascotFuzzy256.png");
    public static final QTexture BERRIES_TEXTURE = 
        new QTexture(System.getProperty("user.dir") + "\\resources\\Texture_Medium.jpg");
    public static final QTexture TECH_TEXTURE = 
        new QTexture(System.getProperty("user.dir") + "\\resources\\Tech.jpg");
    public static final QTexture MASCOTS_BOYFRIEND_TEXTURE;
    static {
        MASCOTS_BOYFRIEND_TEXTURE = 
            new QTexture(System.getProperty("user.dir") + "\\resources\\MascotFuzzy256_AltEyes.png");
        for (int i = 0; i < MASCOTS_BOYFRIEND_TEXTURE.getWidth(); i++) {
            for (int j = 0; j < MASCOTS_BOYFRIEND_TEXTURE.getHeight(); j++) {
                QColor color = new QColor(MASCOTS_BOYFRIEND_TEXTURE.getColor(i, j));
                int r = color.getR();
                int b = color.getB();
                color.setR(b);
                color.setB(r);
                MASCOTS_BOYFRIEND_TEXTURE.setColor(i, j, color.toInt());
            }
        }
    }
        

    public static QWindow       window;
    public static QRenderBuffer frameBuffer;
    public static QViewer       eyes;

    public static void RegularMascot( ) {
        long  t0   = System.currentTimeMillis();
        while ((System.currentTimeMillis() - t0) < CUT_RUNTIME_MS) {
            eyes.clearFrame( );
            float time = (float)(System.currentTimeMillis() - t0) * 0.15f;

            QMatrix4x4 m0 = QMatrix4x4.TRS(
                new QVector3(0.0f, 0.0f, -2.0f), 
                new QVector3(0, time, 0), 
                QVector3.One().multiply3(0.25f)
            );

            eyes.setRenderMode(RenderMode.Textured);
            eyes.setTexture(MASCOT_TEXTURE);
            eyes.drawMesh(MASCOT_MESH, m0);

            window.updateFrame( );
        }
    }

    public static void MascotAndMetalMascot( ) {
        long t0 = System.currentTimeMillis();
        while ((System.currentTimeMillis() - t0) < CUT_RUNTIME_MS) {
            eyes.clearFrame( );
            float time = (float)(System.currentTimeMillis() - t0) * 0.15f;

            QMatrix4x4 m0 = QMatrix4x4.TRS(
                new QVector3(-1.3f, 0.0f, -2.3f), 
                new QVector3(0, time, 0), 
                QVector3.One().multiply3(0.25f)
            );

            eyes.setRenderMode(RenderMode.Textured);
            eyes.setTexture(MASCOT_TEXTURE);
            eyes.drawMesh(MASCOT_MESH, m0);

            QMatrix4x4 m1 = QMatrix4x4.TRS(
                new QVector3(1.3f, 0.0f, -2.3f), 
                new QVector3(0, time, 0), 
                QVector3.One().multiply3(0.25f)
            );

            eyes.setRenderMode(RenderMode.Normal);
            eyes.drawMesh(MASCOT_MESH, m1);

            window.updateFrame( );
        }
    }

    public static void MascotAndHisBoyFriend( ) {
        long t0 = System.currentTimeMillis();
        while ((System.currentTimeMillis() - t0) < CUT_RUNTIME_MS) {
            eyes.clearFrame( );
            float time = (float)(System.currentTimeMillis() - t0) * 0.15f;

            QMatrix4x4 m0 = QMatrix4x4.TRS(
                new QVector3(-0.8f, 0.0f, -2.0f), 
                new QVector3(0, time, 0), 
                QVector3.One().multiply3(0.25f)
            );

            eyes.setRenderMode(RenderMode.Textured);
            eyes.setTexture(MASCOT_TEXTURE);
            eyes.drawMesh(MASCOT_MESH, m0);

            QMatrix4x4 m1 = QMatrix4x4.TRS(
                new QVector3(0.8f, 0.0f, -2.0f), 
                new QVector3(-time + 90.0f, 0, 0), 
                QVector3.One().multiply3(0.25f)
            );

            eyes.setRenderMode(RenderMode.Textured);
            eyes.setTexture(MASCOTS_BOYFRIEND_TEXTURE);
            eyes.drawMesh(MASCOT_MESH, m1);

            window.updateFrame( );
        }
    }

    public static void TechMascot( ) {
        long t0 = System.currentTimeMillis();
        while ((System.currentTimeMillis() - t0) < CUT_RUNTIME_MS) {
            eyes.clearFrame( );
            float time = (float)(System.currentTimeMillis() - t0) * 0.035f;
            float osc0 = QMath.sinf(time);
            float osc1 = QMath.cosf(time);

            for (int i = -6; i < 6; i++) {
                for (int j = -6; j < 6; j++) {

                    float x = (1.0f + osc0) * 2.0f * i;
                    float y = (osc1 * i) + (osc0 * j);
                    float z = -5.0f + (osc1 * 2.0f * j);
                    QMatrix4x4 m0 = QMatrix4x4.TRS(
                        new QVector3(
                            x,
                            y, 
                            z
                        ), 
                        new QVector3(time * i, time, time * j), 
                        QVector3.One().multiply3(0.2f)
                    );

                    eyes.setRenderMode(RenderMode.Textured);
                    eyes.setTexture(TECH_TEXTURE);
                    eyes.drawMesh(MASCOT_MESH, m0);

                }
            }

            window.updateFrame( );
        }
    }

    public static void CustomWobblyMascot( ) {
        long t0 = System.currentTimeMillis();
        eyes.setRenderMode(RenderMode.CustomShader);
        eyes.setCustomShader(new WobbleShader( ));

        while ((System.currentTimeMillis() - t0) < CUT_RUNTIME_MS) {
            eyes.clearFrame( );
            float time = (float)(System.currentTimeMillis() - t0) * 0.15f;

            QMatrix4x4 m0 = QMatrix4x4.TRS(
                new QVector3(0.0f, 0.0f, -2.0f), 
                new QVector3(0.0f, time, 0.0f), 
                QVector3.One().multiply3(0.25f)
            );

            Float dt = (float)(System.currentTimeMillis() - t0);

            eyes.setUniformSlot(1, dt);
            eyes.setTexture(MASCOT_TEXTURE);
            eyes.drawMesh(MASCOT_MESH, m0);

            window.updateFrame( );
        }
    }

    public static void UltraGFXMascot( ) {
        eyes.setRenderMode(RenderMode.CustomShader);
        eyes.setCustomShader(
            new FuzzShader( )
        );

        long t0 = System.currentTimeMillis();
        while ((System.currentTimeMillis() - t0) < CUT_RUNTIME_MS) {
            eyes.clearFrame( );
            float time = (float)(System.currentTimeMillis() - t0) * 0.15f;

            QMatrix4x4 m0 = QMatrix4x4.TRS(
                new QVector3(0.0f, 0.0f, -2.0f), 
                new QVector3(0, time, 0), 
                QVector3.One().multiply3(0.25f)
            );
            eyes.setTexture(MASCOT_TEXTURE);
            eyes.drawMesh(MASCOT_MESH, m0);

            eyes.setTexture(BERRIES_TEXTURE);
            QMatrix4x4 m1 = QMatrix4x4.TRS(
                new QVector3(-3.25f, 0.0f, -7.0f), 
                new QVector3(time * 0.5f, time * 0.5f, time * 0.5f), 
                QVector3.One().multiply3(1.0f)
            );
            eyes.drawMesh(CUBE_MESH, m1);

            eyes.setTexture(TECH_TEXTURE);
            QMatrix4x4 m2 = QMatrix4x4.TRS(
                new QVector3(3.25f, 0.0f, -7.0f), 
                new QVector3(time * 0.5f, time * 0.5f, time * 0.5f), 
                QVector3.One().multiply3(1.0f)
            );
            eyes.drawMesh(CUBE_MESH, m2);

            window.updateFrame( );
        }
    }

    public static void main(String[] args) {
        
        window      = new QWindow("Visual Test", WINDOW_WIDTH, WINDOW_HEIGHT);
        frameBuffer = new QRenderBuffer(FB_WIDTH, FB_HEIGHT);
        window.setRenderBuffer(frameBuffer);
        eyes        = new QViewer(frameBuffer);
        eyes.setViewBounds(-FB_ASPECT, FB_ASPECT, -1.0f, 1.0f);
        eyes.setNearClip(-0.3f);

        while (true) {
            // RegularMascot( );
            // MascotAndMetalMascot( );
            // MascotAndHisBoyFriend( );
            // TechMascot( );
            CustomWobblyMascot( );
            // UltraGFXMascot( );
        }

    }
}
