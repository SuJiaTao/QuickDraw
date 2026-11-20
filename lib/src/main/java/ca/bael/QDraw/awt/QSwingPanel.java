// Bailey JT Brown
// 2026
// QSwingPanel.java

package ca.bael.QDraw.awt;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.swing.JComponent;
import ca.bael.QDraw.QImage;
import ca.bael.QDraw.QPresenter;

public final class QSwingPanel extends JComponent implements QPresenter {
    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private final Object           lock    = new Object( );
    private volatile QAwt.Scaling  scaling = QAwt.Scaling.Stretch;
    private BufferedImage          frame;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void setScaling(QAwt.Scaling scaling) {
        this.scaling = scaling;
        repaint( );
    }

    @Override
    public void present(QImage image) {
        synchronized (lock) {
            if (frame == null || frame.getWidth( ) != image.getWidth( ) || frame.getHeight( ) != image.getHeight( )) {
                frame = new BufferedImage(image.getWidth( ), image.getHeight( ), BufferedImage.TYPE_INT_RGB);
            }
            int[] dst = ((DataBufferInt)frame.getRaster( ).getDataBuffer( )).getData( );
            System.arraycopy(image.getPixels( ), 0, dst, 0, dst.length);
        }
        repaint( );
    }

    @Override
    protected void paintComponent(Graphics g) {
        synchronized (lock) {
            if (frame == null) {
                return;
            }
            Graphics2D g2 = (Graphics2D)g.create( );
            QAwt.drawScaled(g2, frame, getWidth( ), getHeight( ), scaling);
            g2.dispose( );
        }
    }
}
