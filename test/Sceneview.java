// Bailey JT Brown
// 2024
// Sceneview.java

import QDraw.*;
import QDraw.QViewer.RenderMode;

public final class Sceneview {
    public static final String RESOURCE_PATH = System.getProperty("user.dir") + "//resources//";
    public static final int WINDOW_WIDTH  = 1300;
    public static final int WINDOW_HEIGHT = 1000;
    public static final int RESOLUTION_X  = WINDOW_WIDTH / 2;
    public static final int RESOLUTION_Y  = WINDOW_HEIGHT / 2;
    public static final float ASPECT      = (float)RESOLUTION_X / (float)RESOLUTION_Y; 
    public static void main(String[] args) {
        QVector3 viewPos    = new QVector3( );
        QVector3 viewVel    = new QVector3( );
        QVector3 viewLook    = QVector3.Z( );
        QVector3 viewLookVel = new QVector3( );
        
        QRenderBuffer frameBuffer = new QRenderBuffer(RESOLUTION_X, RESOLUTION_Y);
        QWindow window = new QWindow("Scene View", WINDOW_WIDTH, WINDOW_HEIGHT);
        window.setRenderBuffer(frameBuffer);

        QViewer viewer = new QViewer(frameBuffer);
        viewer.setNearClip(-0.05f);
        viewer.setViewBounds(-ASPECT, ASPECT, -1.0f, 1.0f);

        QMesh    mesh0 = new QMesh(RESOURCE_PATH + "Mascot_Smooth.obj");
        QMesh    mesh1 = new QMesh(RESOURCE_PATH + "Cube.obj");
        QTexture tex0  = new QTexture(RESOURCE_PATH + "MascotFuzzy256.png");
        QTexture tex1  = new QTexture(RESOURCE_PATH + "Texture_Medium.jpg");

        float lastTime = (float)(System.nanoTime( ) >> 10);

        QVector3[] lights = new QVector3[] {
            new QVector3(5.0f, 1.0f, 3.0f),
            new QVector3(-2.0f, 0.0f, -20.0f)
        };

        while (true) {

            float currentTime = (float)(System.nanoTime( ) >> 10);
            float dt = (currentTime - lastTime) / 6000;

            float sleepMSecs = 5;
            if (dt < sleepMSecs) continue;

            lastTime = currentTime;

            // UPDATE VIEW
            QVector3 moveInput = (window.getInputWASDVector( ).multiply3(0.05f));
            viewVel.add(new QVector3(-moveInput.getX( ), 0.0f, moveInput.getY( )));
            final float maxViewVel = 1.0f;
            if (viewVel.magnitude( ) > maxViewVel) {
                viewVel.normalize( );
                viewVel.multiply3(maxViewVel);
            }

            QVector3 viewVelLookCorrected;
            QMatrix4x4 viewVelCorrectionMatrix = QMatrix4x4.Identity( );
            viewVelCorrectionMatrix.rotate(viewLook.getX( ), 0.0f, 0.0f);
            viewVelCorrectionMatrix.rotate(0.0f, viewLook.getY( ), 0.0f);
            viewVelLookCorrected = QMatrix4x4.multiply(viewVelCorrectionMatrix, viewVel);
            viewPos.add(viewVelLookCorrected);
            
            viewVel.multiply3(0.8f);

            QVector3 lookInput = window.getInputArrowKeysVector( ).multiply3(2.0f);
            viewLookVel.add(new QVector3(-lookInput.getY( ), lookInput.getX()));
            final float maxLookVel = 4.5f;
            if (viewLookVel.magnitude( ) > maxLookVel) {
                viewLookVel.normalize( );
                viewLookVel.multiply3(maxLookVel);
            }
            viewLook.add(viewLookVel);
            viewLookVel.multiply3(0.85f);

            // SETUP VIEWMATRIX
            QMatrix4x4 viewMatrix = QMatrix4x4.Identity( );
            viewMatrix.translate(viewPos);
            viewMatrix.rotate(0.0f, -viewLook.getY( ), 0.0f);
            viewMatrix.rotate(-viewLook.getX( ), 0.0f, 0.0f);

            // RENDER MODEL0
            viewer.clearFrame( );

            QMatrix4x4 modelMatrix0 = QMatrix4x4.TRS(
                new QVector3(0.0f, -.0f, -3.0f),
                QVector3.Zero( ),
                QVector3.One( ).multiply3(0.35f)
            );

            viewer.setRenderMode(RenderMode.CustomShader);
            viewer.setCustomShader(new FuzzShader( ));

            // generate new light pos based on view transform
            QVector3[] transformedLights = new QVector3[lights.length];
            for (int i = 0 ; i < transformedLights.length; i++) {
                transformedLights[i] = QMatrix4x4.multiply(viewMatrix, lights[i]);
            }
            viewer.setUniformSlot(
                QViewer.DEFAULT_SHADER_LIGHTS_SLOT, 
                transformedLights
            );

            viewer.setRenderMode(RenderMode.CustomShader);
            viewer.setCustomShader(new FuzzShader( ));
            viewer.setUniformSlot(
                QViewer.DEFAULT_SHADER_LIGHTS_SLOT, 
                transformedLights
            );

            viewer.setTexture(tex0);
            viewer.drawMesh(
                mesh0, 
                QMatrix4x4.multiply(modelMatrix0, viewMatrix)
            );

            // RENDER MODEL1
            QMatrix4x4 modelMatrix1 = QMatrix4x4.TRS(
                new QVector3(-3.0f, -1.3f, -3.0f),
                QVector3.Zero( ),
                QVector3.One( ).multiply3(0.35f)
            );

            viewer.setRenderMode(RenderMode.Lit);
            viewer.setUniformSlot(
                QViewer.DEFAULT_SHADER_LIGHTS_SLOT, 
                transformedLights
            );

            viewer.setTexture(tex1);
            viewer.drawMesh(
                mesh1, 
                QMatrix4x4.multiply(modelMatrix1, viewMatrix)
            );
            
            window.updateFrame( );
        }
    }
}
