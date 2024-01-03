// Bailey JT Brown
// 2024
// Visual.java

import QDraw.*;
import QDraw.QViewer.RenderType;

public final class Visual {
    public static final int VISTEST_WINDOW_WIDTH  = 1200;
    public static final int VISTEST_WINDOW_HEIGHT = 960; 
    public static final int VISTEST_FB_WIDTH      = VISTEST_WINDOW_WIDTH  >> 1;
    public static final int VISTEST_FB_HEIGHT     = VISTEST_WINDOW_HEIGHT >> 1;
    public static final float VISTEST_FB_ASPECT   = (float)VISTEST_FB_WIDTH / (float)VISTEST_FB_HEIGHT;
    public static final int VISTEST_RUNTIME_SEC   = 20;
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
            eyes.clearFrame( );


            window.updateFrame( );
        }
    }
    */

    public static void Uncle( ) {
        long t0 = System.currentTimeMillis();

        eyes.setNearClip(-0.0f);
        eyes.setRenderType(RenderType.Textured);

        QMesh uncle = new QMesh(System.getProperty("user.dir") + "\\Resources\\Uncle.obj");
        QRenderBuffer texture = 
            new QRenderBuffer(System.getProperty("user.dir") + "\\Resources\\Uncle_Texture.jpg");

        eyes.setTexture(texture);

        float time = 0.0f;
        while ((System.currentTimeMillis() - t0) < VISTEST_RUNTIME_MS) {
            eyes.clearFrame( );

            time += 0.25f;

            QMatrix4x4 m0 = QMatrix4x4.TRS(
                new QVector3(0.0f, 0.0f, -2.0f), 
                new QVector3(0, time, 0), 
                QVector3.One().multiply3(0.125f)
            );

            eyes.drawMesh(uncle, m0);

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

        eyes.setTexture(texture);

        float time = 0.0f;
        float osc0 = 0.0f;
        float osc1 = 0.0f;
        while ((System.currentTimeMillis() - t0) < VISTEST_RUNTIME_MS) {
            eyes.clearFrame( );

            time = time + 1.5f;
            osc0 = QMath.sinf(time);
            osc1 = QMath.cosf(time);

            long rt0 = System.currentTimeMillis();

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

                    eyes.drawMesh(uncle, m0);

                }
            }

            long rt1 = System.currentTimeMillis();

            System.out.println("dt: " + (rt1 - rt0));

            window.updateFrame( );
        }
    }

    public static void CustomWobblyUncle( ) {
        long t0 = System.currentTimeMillis();

        eyes.setRenderType(RenderType.CustomShader);
        eyes.setCustomShader(
            new QShader() {
                public QVector3 vertexShader(
                    int        vertexNum,
                    QVector3   inOutVertex,
                    QMatrix4x4 transform,
                    Object     userIn
                ) {
                    float randX = random() * 0.02f;
                    float randY = random() * 0.02f;
                    float randZ = random() * 0.02f;
                    transform.multiply(QMatrix4x4.translationMatrix(randX, randY, randZ));
                    return QMatrix4x4.multiply(transform, inOutVertex);
                }

                public QColor fragmentShader(
                    int    screenX,
                    int    screenY,
                    float  fragU,
                    float  fragV,
                    QRenderBuffer texture,
                    QColor belowColor,
                    Object userIn
                ) {
                    fragU += random() * 0.01f;
                    fragV += random() * 0.01f;
                    int randAlpha = (int)(128.0f + random() * 128.0f);
                    QColor texCol = sampleTexture(fragU, fragV, texture, QViewer.SampleType.Repeat).setA(randAlpha);
                    return blendColor(belowColor, texCol);
                }
            }
        );

        QMesh uncle = new QMesh(System.getProperty("user.dir") + "\\Resources\\Uncle.obj");
        QRenderBuffer texture = 
            new QRenderBuffer(System.getProperty("user.dir") + "\\Resources\\Uncle_Texture.jpg");
        eyes.setTexture(texture);

        float time = 0.0f;
        while ((System.currentTimeMillis() - t0) < VISTEST_RUNTIME_MS) {
            eyes.clearFrame( );

            time += 0.25f;

            QMatrix4x4 m0 = QMatrix4x4.TRS(
                new QVector3(0.0f, 0.0f, -2.0f), 
                new QVector3(0, time, 0), 
                QVector3.One().multiply3(0.125f)
            );

            eyes.drawMesh(uncle, m0);

            window.updateFrame( );
        }
    }

    public static void main(String[] args) {
        
        window      = new QWindow("Visual Test", VISTEST_WINDOW_WIDTH, VISTEST_WINDOW_HEIGHT);
        frameBuffer = new QRenderBuffer(VISTEST_FB_WIDTH, VISTEST_FB_HEIGHT);
        window.setRenderBuffer(frameBuffer);
        eyes        = new QViewer(frameBuffer);
        eyes.setViewBounds(-VISTEST_FB_ASPECT, VISTEST_FB_ASPECT, -1.0f, 1.0f);

        Uncle( );
    }
}
