// Bailey JT Brown
// 2024
// Uncle.java

import QDraw.*;
import QDraw.QViewer.RenderType;

public final class Uncle {
    public static final int WINDOW_WIDTH    = 1200;
    public static final int WINDOW_HEIGHT   = 960; 
    public static final int FB_WIDTH        = WINDOW_WIDTH  >> 1;
    public static final int FB_HEIGHT       = WINDOW_HEIGHT >> 1;
    public static final float FB_ASPECT     = (float)FB_WIDTH / (float)FB_HEIGHT;
    public static final int CUT_RUNTIME_SEC = 10;
    public static final int CUT_RUNTIME_MS  = 1000 * CUT_RUNTIME_SEC;
    public static QWindow       window;
    public static QRenderBuffer frameBuffer;
    public static QViewer       eyes;

    public static void RegularUncle( ) {
        long t0 = System.currentTimeMillis();

        eyes.setNearClip(-0.0f);
        eyes.setRenderType(RenderType.Textured);

        QMesh uncle = new QMesh(System.getProperty("user.dir") + "\\resources\\Uncle.obj");
        QTexture texture = 
            new QTexture(System.getProperty("user.dir") + "\\resources\\Uncle_Texture.jpg");

        eyes.setTexture(texture);

        float time = 0.0f;
        while ((System.currentTimeMillis() - t0) < CUT_RUNTIME_MS) {
            eyes.clearFrame( );

            time += 0.4f;

            QMatrix4x4 m0 = QMatrix4x4.TRS(
                new QVector3(0.0f, 0.0f, -2.0f), 
                new QVector3(0, time, 0), 
                QVector3.One().multiply3(0.125f)
            );

            eyes.drawMesh(uncle, m0);

            window.updateFrame( );
        }
    }

    public static void UncleAndHisBuddy( ) {
        long t0 = System.currentTimeMillis();

        eyes.setNearClip(-0.0f);

        QMesh uncle = new QMesh(System.getProperty("user.dir") + "\\resources\\Uncle.obj");
        QTexture texture = 
            new QTexture(System.getProperty("user.dir") + "\\resources\\Uncle_Texture.jpg");

        eyes.setTexture(texture);

        float time = 0.0f;
        while ((System.currentTimeMillis() - t0) < CUT_RUNTIME_MS) {
            eyes.clearFrame( );

            time += 0.4f;

            QMatrix4x4 m0 = QMatrix4x4.TRS(
                new QVector3(-1.3f, 0.0f, -2.3f), 
                new QVector3(0, time, 0), 
                QVector3.One().multiply3(0.125f)
            );

            eyes.setRenderType(RenderType.Textured);
            eyes.drawMesh(uncle, m0);

            QMatrix4x4 m1 = QMatrix4x4.TRS(
                new QVector3(1.3f, 0.0f, -2.3f), 
                new QVector3(0, time, 0), 
                QVector3.One().multiply3(0.125f)
            );

            eyes.setRenderType(RenderType.Normal);
            eyes.drawMesh(uncle, m1);

            window.updateFrame( );
        }
    }

    public static void TechUncle( ) {
        long t0 = System.currentTimeMillis();

        eyes.setNearClip(-0.0f);
        eyes.setRenderType(RenderType.Textured);

        QMesh uncle = 
            new QMesh(System.getProperty("user.dir") + "\\resources\\Uncle.obj");
        QTexture texture = 
            new QTexture(System.getProperty("user.dir") + "\\resources\\Matrix.jpg");

        eyes.setTexture(texture);

        float time = 0.0f;
        float osc0 = 0.0f;
        float osc1 = 0.0f;
        while ((System.currentTimeMillis() - t0) < CUT_RUNTIME_MS) {
            eyes.clearFrame( );

            time = time + 2.5f;
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

                    eyes.drawMesh(uncle, m0);

                }
            }

            window.updateFrame( );
        }
    }

    public static void CustomWobblyUncle( ) {
        long t0 = System.currentTimeMillis();

        eyes.setRenderType(RenderType.CustomShader);
        eyes.setCustomShader(
            new QShader() {
                public QVector3 vertexShader(
                    VertexDrawInfo vertInfo,
                    Object         userIn
                ) {
                    float dt = (float)(System.currentTimeMillis() - t0) * 0.6f;
                    float offsetX = 0.1f * QMath.cosf( dt + (vertInfo.vertexPos.getY() * 35.0f) );
                    float offsetY = 0.1f * QMath.sinf( (dt + vertInfo.vertexPos.getX() * 70.0f) * 0.5f );
                    float offsetZ = 0.1f * QMath.sinf( 90.0f + dt + (vertInfo.vertexPos.getY() * 35.0f) );                    
                    vertInfo.transform.multiply(QMatrix4x4.translationMatrix(offsetX, offsetY, offsetZ));
                    return QMatrix4x4.multiply(vertInfo.transform, vertInfo.vertexPos);
                }

                public QColor fragmentShader(
                    FragmentDrawInfo fragInfo,
                    Object           userIn
                ) {
                    QColor texCol = sampleTexture(
                        fragInfo.fragU, 
                        fragInfo.fragV, 
                        fragInfo.texture, 
                        QViewer.SampleType.Repeat
                    ).setA(200);
                    return blendColor(fragInfo.belowColor, texCol);
                }
            }
        );

        QMesh uncle = new QMesh(System.getProperty("user.dir") + "\\resources\\Uncle.obj");
        QTexture texture = 
            new QTexture(System.getProperty("user.dir") + "\\resources\\Uncle_Texture.jpg");
        eyes.setTexture(texture);

        float time = 0.0f;
        while ((System.currentTimeMillis() - t0) < CUT_RUNTIME_MS) {
            eyes.clearFrame( );

            time += 1.0f;

            QMatrix4x4 m0 = QMatrix4x4.TRS(
                new QVector3(0.0f, 0.0f, -2.0f), 
                new QVector3(0.0f, time, 0.0f), 
                QVector3.One().multiply3(0.125f)
            );

            eyes.drawMesh(uncle, m0);

            window.updateFrame( );
        }
    }

    public static void ShadedUncle( ) {
        long t0 = System.currentTimeMillis();

        eyes.setNearClip(-0.0f);
        eyes.setRenderType(RenderType.CustomShader);
        eyes.setCustomShader(
            new QShader() {
                public QVector3 vertexShader(
                    VertexDrawInfo vertInfo,
                    Object         userIn
                ) {
                    return QMatrix4x4.multiply(vertInfo.transform, vertInfo.vertexPos);
                }

                public QColor fragmentShader(
                    FragmentDrawInfo fragInfo,
                    Object           userIn
                ) {
                    fragInfo.fragU += random() * 0.005f;
                    fragInfo.fragV += random() * 0.005f;

                    QColor texCol = sampleTexture(fragInfo.fragU, fragInfo.fragV, fragInfo.texture, QViewer.SampleType.Repeat);
                    QVector3 dFaceLightNormalized = QVector3.sub(
                        new QVector3(-4.0f, 1.4f, 10.0f),
                        fragInfo.faceCenterWorldSpace
                    ).normalize();
                    float brightnessFactor = QVector3.dot(
                        fragInfo.faceNormal, 
                        dFaceLightNormalized
                    );
                    texCol = multiplyColor(texCol, Math.max(0.0f, brightnessFactor));
                    return blendColor(fragInfo.belowColor, texCol);
                }
            }
        );

        QMesh uncle = new QMesh(System.getProperty("user.dir") + "\\resources\\Uncle.obj");
        QTexture texture = 
            new QTexture(System.getProperty("user.dir") + "\\resources\\Uncle_Texture.jpg");

        eyes.setTexture(texture);

        float time = 0.0f;
        while ((System.currentTimeMillis() - t0) < CUT_RUNTIME_MS) {
            eyes.clearFrame( );

            time += 1.4f;

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
        
        window      = new QWindow("Visual Test", WINDOW_WIDTH, WINDOW_HEIGHT);
        frameBuffer = new QRenderBuffer(FB_WIDTH, FB_HEIGHT);
        window.setRenderBuffer(frameBuffer);
        eyes        = new QViewer(frameBuffer);
        eyes.setViewBounds(-FB_ASPECT, FB_ASPECT, -1.0f, 1.0f);

        RegularUncle( );
        UncleAndHisBuddy( );
        TechUncle( );
        CustomWobblyUncle( );
        ShadedUncle( );

        System.exit(0);

    }
}
