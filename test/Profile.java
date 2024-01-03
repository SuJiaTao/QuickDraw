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
import QDraw.QViewer.RenderType;

public final class Profile {
    public static final int WINDOW_WIDTH  = 1200;
    public static final int WINDOW_HEIGHT = 960; 
    public static final int FB_WIDTH      = WINDOW_WIDTH;
    public static final int FB_HEIGHT     = WINDOW_HEIGHT;
    public static final float FB_ASPECT   = (float)FB_WIDTH / (float)FB_HEIGHT;
    public static QWindow       window;
    public static QRenderBuffer frameBuffer;
    public static QViewer       viewer;

    // NOTE: all timings are in microseconds
    private static long   lastTime;
    private static long   minTime;
    private static long   maxTime;
    private static int    testCount;
    private static final int SAVE_TIME_BUFFER_SIZE = 10000;
    private static long[]    dtBuffer = new long[SAVE_TIME_BUFFER_SIZE];

    public static void reset( ) {
        testCount = 0;
        minTime   = 0;
        maxTime   = 0;
    }

    public static long getTimeMicroseconds( ) {
        return System.nanoTime( ) >> 10; // div 1024, rough conversion
    }

    public static void beginTime( ) {
        lastTime = getTimeMicroseconds( );
    }

    public static void endTime( ) {
        long dt = getTimeMicroseconds( ) - lastTime;
        if (testCount == 0) {
            minTime = dt;
            maxTime = dt;
        }
        minTime = Math.min(dt, minTime);
        maxTime = Math.max(dt, maxTime);
        dtBuffer[testCount % SAVE_TIME_BUFFER_SIZE] = dt;
        testCount++;
    }

    public static long avgTime( ) {
        float sum = 0;
        int sumCount = Math.min(SAVE_TIME_BUFFER_SIZE, testCount);
        for (int i = 0; i < sumCount; i++) {
            sum += (float)dtBuffer[i];
        } 
        sum /= (float)sumCount;
        return (long)sum;
    }

    public static void ProfileTextureVSRenderBuffer(int iterations) {
        viewer.setRenderType(RenderType.Textured);

        String texPath = System.getProperty("user.dir") + "\\resources\\Large_Texture.jpg";
        QRenderBuffer rbTex = new QRenderBuffer(texPath);
        QTexture      tTex  = new QTexture(texPath);

        String cubePath = System.getProperty("user.dir") + "\\resources\\Uncle.obj";
        QMesh  cubeMesh = new QMesh(cubePath);

        QMatrix4x4 tMatrix;

        long  SAMPLE_FRAME_COUNT = 1000;
        float time;

        for (int iter = 0; iter < iterations; iter++) {

            // PROFILE TEXTURE
            reset( );
            time = 0.0f;
            viewer.setTexture(tTex); // <- SET TO TEX
            for (int frame = 0; frame < SAMPLE_FRAME_COUNT; frame++) {
                viewer.clearFrame( );

                tMatrix = QMatrix4x4.TRS(
                    new QVector3(0, 0, -15.0f), 
                    new QVector3(time * 0.1f, time, 0.0f), 
                    QVector3.One()
                );

                beginTime( );
                viewer.drawMesh(cubeMesh, tMatrix);
                endTime( );

                time += 2.0f;

                window.updateFrame( );
            }

            System.out.println("TEX avg time: " + avgTime( ));
            
            System.gc();

            // PROFILE RENDERBUFFER
            reset( );
            time = 0.0f;
            viewer.setTexture(rbTex); // <- SET TO RB
            for (int frame = 0; frame < SAMPLE_FRAME_COUNT; frame++) {
                viewer.clearFrame( );

                tMatrix = QMatrix4x4.TRS(
                    new QVector3(0, 0, -15.0f), 
                    new QVector3(time * 0.1f, time, 0.0f), 
                    QVector3.One()
                );

                beginTime( );
                viewer.drawMesh(cubeMesh, tMatrix);
                endTime( );

                time += 2.0f;

                window.updateFrame( );
            }

            System.gc();

            System.out.println("RB avg time: " + avgTime( ));

        }
    }

    public static void main(String[] args) {
        window      = new QWindow("Profiling Tests", WINDOW_WIDTH, WINDOW_HEIGHT);
        frameBuffer = new QRenderBuffer(FB_WIDTH, FB_HEIGHT);
        window.setRenderBuffer(frameBuffer);
        viewer      = new QViewer(frameBuffer);
        viewer.setViewBounds(-FB_ASPECT, FB_ASPECT, -1.0f, 1.0f);

        ProfileTextureVSRenderBuffer(5);
    }
}
