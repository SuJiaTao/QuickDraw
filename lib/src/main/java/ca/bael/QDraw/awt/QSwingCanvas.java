// Bailey JT Brown
// 2026
// QSwingCanvas.java

package ca.bael.QDraw.awt;

import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import ca.bael.QDraw.QImage;
import ca.bael.QDraw.QPresenter;
import ca.bael.QDraw.QTimer;

public final class QSwingCanvas extends Canvas implements QPresenter {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final int NUM_BUFFERS = 2;

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private QAwt.Scaling  scaling = QAwt.Scaling.Stretch;
    private QImage        source;
    private BufferedImage view;

    /////////////////////////////////////////////////////////////////
    // PUBLIC MEMBERS
    public final QTimer drawTimer = new QTimer( );
    public final QTimer showTimer = new QTimer( );

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void setScaling(QAwt.Scaling scaling) {
        this.scaling = scaling;
    }

    @Override
    public void present(QImage frame) {
        if (!isDisplayable( )) {
            return;
        }
        if (getBufferStrategy( ) == null) {
            createBufferStrategy(NUM_BUFFERS);
        }
        if (frame != source) {
            source = frame;
            view   = QAwt.asBufferedImage(frame, false);
        }
        BufferStrategy strategy = getBufferStrategy( );
        do {
            do {
                drawTimer.start( );
                Graphics2D g = (Graphics2D)strategy.getDrawGraphics( );
                QAwt.drawScaled(g, view, getWidth( ), getHeight( ), scaling);
                g.dispose( );
                drawTimer.stop( );
            } while (strategy.contentsRestored( ));
            showTimer.start( );
            strategy.show( );
            showTimer.stop( );
        } while (strategy.contentsLost( ));
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public QSwingCanvas( ) {
        setIgnoreRepaint(true);
    }
}
