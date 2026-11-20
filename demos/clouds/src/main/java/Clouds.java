// Bailey JT Brown
// 2026
// Clouds.java

import ca.bael.QDraw.*;
import ca.bael.QDraw.QViewer.CullMode;
import ca.bael.QDraw.QViewer.RenderMode;
import ca.bael.QDraw.awt.QWindow;

public final class Clouds {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final int         WINDOW_WIDTH   = 1200;
    public static final int         WINDOW_HEIGHT  = 960;
    public static final int         FB_WIDTH       = WINDOW_WIDTH >> 1;
    public static final int         FB_HEIGHT      = WINDOW_HEIGHT >> 1;
    public static final float       FB_ASPECT      = (float)FB_WIDTH / (float)FB_HEIGHT;
    public static final float       LIGHT_STRENGTH = 8.0f;
    public static final QProjection PROJECTION     = QProjection.perspective(90.0f, FB_ASPECT, 0.05f);
    public static final QMesh       SKY_MESH       = new QMesh(Clouds.class.getResource("/UVSphere_Low.obj").getPath( ));
    public static final QMesh       CUBE_MESH      = new QMesh(Clouds.class.getResource("/Cube.obj").getPath( ));
    public static final QMaterial   CUBE_MATERIAL  = new QMaterial(new QColor(0x30, 0x30, 0x30), QColor.White( ), QColor.White( ), 32.0f);
    private static final QVector3   CUBE_POSITION  = new QVector3(0.0f, 0.0f, 10.0f);
    private static final QVector3   LIGHT_POSITION = new QVector3(5.0f, 5.0f, 5.0f);

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public static void main(String[] args) {
        QWindow       window      = new QWindow("Clouds", WINDOW_WIDTH, WINDOW_HEIGHT);
        QRenderBuffer frameBuffer = new QRenderBuffer(FB_WIDTH, FB_HEIGHT);
        QViewer       viewer      = new QViewer(frameBuffer);
        SkyShader     skyShader   = new SkyShader( );
        FlyCamera     camera      = new FlyCamera(QVector3.Zero( ), QQuaternion.fromAxisAngle(QVector3.Y( ), 180.0f));
        viewer.setProjection(PROJECTION);

        long prevTime = System.nanoTime( );
        while (true) {
            long now = System.nanoTime( );
            camera.update(window, (now - prevTime) * 1e-9f);
            prevTime = now;
            QMatrix4x4 view = camera.viewMatrix( );

            viewer.clearFrame( );
            viewer.setRenderMode(RenderMode.CustomShader);
            viewer.setCustomShader(skyShader);
            viewer.setCullMode(CullMode.Front);
            viewer.setDepthWrite(false);
            viewer.setMatrix(camera.viewRotationMatrix( ));
            viewer.drawMesh(SKY_MESH);

            viewer.setRenderMode(RenderMode.Material);
            viewer.setCullMode(CullMode.Back);
            viewer.setDepthWrite(true);
            viewer.setMatrix(QMatrix4x4.multiply(QMatrix4x4.translationMatrix(CUBE_POSITION), view));
            viewer.setLights(new QLights(new QLight[] { new QLight(view.transformPoint(LIGHT_POSITION.copy( )), LIGHT_STRENGTH) }));
            viewer.setUniformSlot(QViewer.DEFAULT_SHADER_MATERIAL_SLOT, CUBE_MATERIAL);
            viewer.drawMesh(CUBE_MESH);
            viewer.flush( );

            window.present(frameBuffer.getColorImage( ));
        }
    }
}
