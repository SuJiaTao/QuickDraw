// Bailey JT Brown
// 2024
// Profile.java

import QDraw.QMatrix4x4;
import QDraw.QMesh;
import QDraw.QRenderBuffer;
import QDraw.QTexture;
import QDraw.QVector3;
import QDraw.QViewer;
import QDraw.QWindow;
import QDraw.QViewer.RenderMode;

public final class Profile {
    public static final int WINDOW_WIDTH  = 1600;
    public static final int WINDOW_HEIGHT = 1000; 
    public static final int FB_WIDTH      = WINDOW_WIDTH;
    public static final int FB_HEIGHT     = WINDOW_HEIGHT;
    public static final float FB_ASPECT   = (float)FB_WIDTH / (float)FB_HEIGHT;
    public static QWindow       window;
    public static QRenderBuffer frameBuffer;
    public static QViewer       viewer;

    public static void ProfileTextureVSRenderBuffer(
            int iterations, 
            String texPath,
            String meshPath
        ) {

        QRenderBuffer rbTex = new QTexture(texPath).toRenderBuffer( );
        QTexture      tTex  = new QTexture(texPath);
        QMesh         mesh  = new QMesh(meshPath);

        TimerInstance rbTimer  = new TimerInstance();
        TimerInstance texTimer = new TimerInstance();

        QMatrix4x4 tMatrix;


        for (int iter = 0; iter < iterations; iter++) {

            System.gc();

            rbTimer.reset( );
            texTimer.reset( );
            
            long  SAMPLE_FRAME_COUNT = 500;
            float time = 0.0f;

            for (int frame = 0; frame < SAMPLE_FRAME_COUNT; frame++) {
                
                tMatrix = QMatrix4x4.TRS(
                    new QVector3(0, 0, -7.0f), 
                    new QVector3(time * 0.1f, time, 0.0f), 
                    QVector3.One()
                );

                // alternate between using tex vs rb on each frame
                if ((frame & 1) == 0) {

                    // PROFILE TEX
                    viewer.clearFrame( );
                    viewer.setTextureSlot(0, tTex);

                    texTimer.beginTime();
                    viewer.setMatrix(tMatrix);
                    viewer.drawMesh(mesh);
                    texTimer.endTime();

                    window.updateFrame( );

                    // PROFILE RB
                    viewer.clearFrame( );
                    viewer.setTextureSlot(0, rbTex);

                    rbTimer.beginTime();
                    viewer.setMatrix(tMatrix);
                    viewer.drawMesh(mesh);
                    rbTimer.endTime();

                    window.updateFrame( );

                } else {

                    // PROFILE RB
                    viewer.clearFrame( );
                    viewer.setTextureSlot(0, rbTex);

                    rbTimer.beginTime();
                    viewer.setMatrix(tMatrix);
                    viewer.drawMesh(mesh);
                    rbTimer.endTime();

                    window.updateFrame( );

                    // PROFILE TEX
                    viewer.clearFrame( );
                    viewer.setTextureSlot(0, tTex);

                    texTimer.beginTime();
                    viewer.setMatrix(tMatrix);
                    viewer.drawMesh(mesh);
                    texTimer.endTime();

                    window.updateFrame( );

                }

                time += 6.0f;
            }

            System.out.println("\tTEXAVG: " + texTimer.avgTime());
            System.out.println("\tRBAVG:  " + rbTimer.avgTime());
            System.out.println("\tRATIO:  " + 
                (float)texTimer.avgTime() / (float)rbTimer.avgTime());
        }
    }

    public static void main(String[] args) {
        window      = new QWindow("Profiling Tests", WINDOW_WIDTH, WINDOW_HEIGHT);
        frameBuffer = new QRenderBuffer(FB_WIDTH, FB_HEIGHT);
        window.setRenderBuffer(frameBuffer);
        viewer      = new QViewer(frameBuffer);
        viewer.setViewBounds(-FB_ASPECT, FB_ASPECT, -1.0f, 1.0f);
        viewer.setRenderMode(RenderMode.Textured);

        String basePath = System.getProperty("user.dir") + "\\resources\\";
        String meshPath = basePath + "Mascot.obj";
        String hugeTexPath = basePath + "Texture_Huge.jpg";
        String largeTexPath = basePath + "Texture_Large.jpg";
        String medTexPath = basePath + "Texture_Medium.jpg";
        String smallTexPath = basePath + "Texture_Small.jpg";
        System.out.println("PROFILE HUGE");
        ProfileTextureVSRenderBuffer(2, hugeTexPath, meshPath);
        System.out.println("PROFILE LARGE");
        ProfileTextureVSRenderBuffer(2, largeTexPath, meshPath);
        System.out.println("PROFILE MEDIUM");
        ProfileTextureVSRenderBuffer(2, medTexPath, meshPath);
        System.out.println("PROFILE SMALL");
        ProfileTextureVSRenderBuffer(2, smallTexPath, meshPath);
    }
}
