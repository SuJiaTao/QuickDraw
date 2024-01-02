// Bailey JT Brown
// 2024
// Visual.java

import QDraw.*;
import QDraw.QViewer.RenderType;

public final class Visual {
    public static final int VISTEST_FB_WIDTH      = 600;
    public static final int VISTEST_FB_HEIGHT     = 480;
    public static final float VISTEST_FB_ASPECT   = (float)VISTEST_FB_WIDTH / (float)VISTEST_FB_HEIGHT;
    public static final int VISTEST_WINDOW_WIDTH  = 1200;
    public static final int VISTEST_WINDOW_HEIGHT = 960; 
    public static final int VISTEST_RUNTIME_SEC   = 5;
    public static final int VISTEST_RUNTIME_MS    = 1000 * VISTEST_RUNTIME_SEC;
    public static QWindow       window;
    public static QRenderBuffer frameBuffer;
    public static QViewer       eyes;

    /*
    public static void TestTemplate( ) {
        long t0 = System.currentTimeMillis();

        eyes.setNearClip( val );
        eyes.setRenderType( val );
        eyes.setFillColor( val );

        while ((System.currentTimeMillis() - t0) < VISTEST_RUNTIME_MS) {
            eyes.blink( );


            window.updateFrame( );
        }
    }
    */

    public static void PlaneDance( ) {
        long t0 = System.currentTimeMillis();

        eyes.setNearClip(-1.0f);
        eyes.setRenderType(RenderType.FlatColor);

        float time = 0.0f;
        float osc0 = 0.0f;
        float osc1 = 0.0f;
        while ((System.currentTimeMillis() - t0) < VISTEST_RUNTIME_MS) {
            time = time + 0.5f;
            osc0 = QMath.cosf(time);
            osc1 = QMath.sinf(time);

            QMatrix4x4 m0 = QMatrix4x4.TRS(
                new QVector3(osc0, osc1, -2.0f), 
                new QVector3(time, time * 0.5f, time), 
                QVector3.One().multiply3(osc0 * 2.0f)
            );

            QMatrix4x4 m1 = QMatrix4x4.TRS(
                new QVector3(osc1, osc0, -5.0f),
                new QVector3(0, 0, time),
                QVector3.One().multiply3(osc1 * 0.25f + 1.0f)
            );

            eyes.blink();

            eyes.setFillColor(new QColor(0xFF, 0x80, 0x20));
            eyes.viewMesh(QMesh.UnitPlane(), m0);

            eyes.setFillColor(new QColor(0x20, 0xFF, 0x80));
            eyes.viewMesh(QMesh.UnitPlane(), m1);

            window.updateFrame();
            
        }
    }

    public static void Uncle( ) {
        long t0 = System.currentTimeMillis();

        eyes.setNearClip(-0.0f);
        eyes.setRenderType(RenderType.Textured);

        QMesh uncle = new QMesh(System.getProperty("user.dir") + "\\Resources\\Uncle.obj");
        QRenderBuffer texture = 
            new QRenderBuffer(System.getProperty("user.dir") + "\\Resources\\Uncle_Texture.jpg");

        eyes.setRenderTexture(texture);

        float time = 0.0f;
        while ((System.currentTimeMillis() - t0) < VISTEST_RUNTIME_MS) {
            eyes.blink( );

            time += 0.25f;

            QMatrix4x4 m0 = QMatrix4x4.TRS(
                new QVector3(0.0f, 0.0f, -2.0f), 
                new QVector3(0, time, 0), 
                QVector3.One().multiply3(0.125f)
            );

            eyes.viewMesh(uncle, m0);

            window.updateFrame( );
        }
    }

    public static void TechUncle( ) {
        long t0 = System.currentTimeMillis();

        eyes.setNearClip(-0.0f);
        eyes.setRenderType(RenderType.Textured);

        QMesh uncle = 
            new QMesh(System.getProperty("user.dir") + "\\Resources\\Uncle.obj");
        QRenderBuffer texture = 
            new QRenderBuffer(System.getProperty("user.dir") + "\\Resources\\Matrix.jpg");

        eyes.setRenderTexture(texture);

        float time = 0.0f;
        float osc0 = 0.0f;
        float osc1 = 0.0f;
        while (true) {
            eyes.blink( );

            time = time + 1.5f;
            osc0 = QMath.sinf(time);
            osc1 = QMath.cosf(time);

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
                        QVector3.One().multiply3(0.125f)
                    );

                    eyes.viewMesh(uncle, m0);

                }
            }

            window.updateFrame( );
        }
    }

    public static void main(String[] args) {
        
        window      = new QWindow("Visual Test", VISTEST_WINDOW_WIDTH, VISTEST_WINDOW_HEIGHT);
        frameBuffer = new QRenderBuffer(VISTEST_FB_WIDTH, VISTEST_FB_HEIGHT);
        window.setRenderBuffer(frameBuffer);
        eyes        = new QViewer(frameBuffer);
        eyes.setViewBounds(-VISTEST_FB_ASPECT, VISTEST_FB_ASPECT, -1.0f, 1.0f);

        TechUncle( );

    }
}
