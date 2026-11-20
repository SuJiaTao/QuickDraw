// Bailey JT Brown
// 2026
// FlyCamera.java

import java.awt.event.KeyEvent;

import ca.bael.QDraw.QMatrix4x4;
import ca.bael.QDraw.QQuaternion;
import ca.bael.QDraw.QVector3;
import ca.bael.QDraw.awt.QWindow;

public final class FlyCamera {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final float MOVE_SPEED = 20.0f;
    public static final float TURN_RATE  = 90.0f;

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private final QVector3    position;
    private final QQuaternion rotation;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void update(QWindow window, float dt) {
        QVector3 look = window.getInputArrowKeysVector( );
        rotation.rotateGlobal(QVector3.Y( ), look.getX( ) * TURN_RATE * dt);
        rotation.rotateLocal(QVector3.X( ), -look.getY( ) * TURN_RATE * dt);

        QVector3 move = window.getInputWASDVector( );
        float    rise = (window.isKeyDown(KeyEvent.VK_SPACE) ? 1.0f : 0.0f) - (window.isKeyDown(KeyEvent.VK_SHIFT) ? 1.0f : 0.0f);
        float    step = MOVE_SPEED * dt;
        position.fma(rotation.forward( ), move.getY( ) * step)
                .fma(rotation.right( ), move.getX( ) * step)
                .fma(QVector3.Y( ), rise * step);
    }

    public QMatrix4x4 viewRotationMatrix( ) {
        return new QQuaternion(rotation).conjugate( ).toMatrix( );
    }

    public QMatrix4x4 viewMatrix( ) {
        return QMatrix4x4.multiply(QMatrix4x4.translationMatrix(QVector3.multiply3(position, -1.0f)), viewRotationMatrix( ));
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public FlyCamera(QVector3 position, QQuaternion rotation) {
        this.position = position.copy( );
        this.rotation = new QQuaternion(rotation);
    }
}
