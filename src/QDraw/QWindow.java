// Bailey JT Brown
// 2023
// QWindow.java

package QDraw;

import java.awt.*;
import javax.swing.*;
import QDraw.QException.PointOfError;

public final class QWindow {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final int   NUM_BUFFERS = 2;
    private static final Color CLEAR_COLOR = Color.black;

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private JFrame        window;
    private Graphics2D    windowGraphics; 
    private QRenderBuffer renderBuffer;
    private int           windowWidth;
    private int           windowHeight;

    /////////////////////////////////////////////////////////////////
    // PUBLIC MEMBERS
    public QColor clearColor;

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    public QWindow(String title, int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new QException(
                PointOfError.InvalidParameter, 
                "width or height is <= 0"
            );
        }

        windowWidth  = width;
        windowHeight = height;

        clearColor = new QColor();

        window = new JFrame(title);
        window.setSize(width, height);
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);

        window.createBufferStrategy(NUM_BUFFERS);
        windowGraphics = (Graphics2D)window.getBufferStrategy().getDrawGraphics();
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public Frame getFrame( ) {
        return window;
    }

    public void setRenderBuffer(QRenderBuffer buffer) {
        renderBuffer = buffer;
    }

    public void updateFrame( ) {
        if (renderBuffer == null) {
            return;
        }
        
        windowGraphics.setColor(CLEAR_COLOR);
        windowGraphics.fillRect(
            0, 
            0, 
            windowWidth, 
            windowHeight
        );
        windowGraphics.drawImage(
            renderBuffer.getBufferedImage(),
            0,
            0,
            windowWidth,
            windowHeight,
            null
        );

        window.getBufferStrategy().show();
    }
}
