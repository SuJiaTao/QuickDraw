// Bailey JT Brown
// 2023-2024
// QWindow.java

package QDraw;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
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
    private InputListener listener;

    /////////////////////////////////////////////////////////////////
    // PUBLIC MEMBERS
    public QColor clearColor;

    /////////////////////////////////////////////////////////////////
    // PRIVATE CLASSES
    private static class InputListener implements KeyListener, MouseListener {
        /////////////////////////////////////////////////////////////////
        // CONSTANTS
        private static final int KEY_BUFFER_INITIAL_SIZE = 20;
        public ArrayList<Integer>   keyCodesDown = new ArrayList<>(KEY_BUFFER_INITIAL_SIZE);
        public ArrayList<Character> charsDown    = new ArrayList<>(KEY_BUFFER_INITIAL_SIZE);
        public boolean mouseDown = false;

        /////////////////////////////////////////////////////////////////
        // CALLBACKS
        public void mouseClicked(MouseEvent arg0) {  }
        public void mouseEntered(MouseEvent arg0) {  }
        public void mouseExited(MouseEvent arg0) {  }

        public void mousePressed(MouseEvent arg0) {
            mouseDown = true;
        }

        public void mouseReleased(MouseEvent arg0) {
            mouseDown = false;
        }

        public void keyPressed(KeyEvent arg0) {
            if (!keyCodesDown.contains(arg0.getKeyCode( ))) {
                keyCodesDown.add(arg0.getKeyCode( ));
            }
            
            if (!charsDown.contains(arg0.getKeyChar( ))) {
                charsDown.add(arg0.getKeyChar( ));
            }
        }

        public void keyReleased(KeyEvent arg0) {
            if (keyCodesDown.contains(arg0.getKeyCode( ))) {
                keyCodesDown.remove(new Integer(arg0.getKeyCode( )));
            }
            
            if (charsDown.contains(arg0.getKeyChar( ))) {
                charsDown.remove(new Character(arg0.getKeyChar( )));
            }
        }

        public void keyTyped(KeyEvent arg0) { }
    }

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
        window.addKeyListener(null);

        listener = new InputListener( );
        window.addKeyListener(listener);
        window.addMouseListener(listener);

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

    public boolean isKeyDown(int keyCode) {
        return listener.keyCodesDown.contains(keyCode);
    }

    public boolean isCharDown(char charCode) {
        return listener.charsDown.contains(charCode);
    }

    public boolean isMouseClicked( ) {
        return listener.mouseDown;
    }

    public boolean isCharDownIgnoreCase(char charCode) {
        return 
            listener.charsDown.contains(Character.toUpperCase(charCode)) ||
            listener.charsDown.contains(Character.toLowerCase(charCode));
    }

    public QVector3 getInputWASDVector( ) {
        QVector3 accumVec = new QVector3( );
        if (isCharDownIgnoreCase('W')) {
            accumVec.add(new QVector3(0.0f, 1.0f));
        }
        if (isCharDownIgnoreCase('S')) {
            accumVec.add(new QVector3(0.0f, -1.0f));
        }
        if (isCharDownIgnoreCase('A')) {
            accumVec.add(new QVector3(-1.0f, 0.0f));
        }
        if (isCharDownIgnoreCase('D')) {
            accumVec.add(new QVector3(1.0f, 0.0f));
        }
        return accumVec.normalize( );
    }

    public QVector3 getInputArrowKeysVector( ) {
        QVector3 accumVec = new QVector3( );
        if (isKeyDown(KeyEvent.VK_UP)) {
            accumVec.add(new QVector3(0.0f, 1.0f));
        }
        if (isKeyDown(KeyEvent.VK_DOWN)) {
            accumVec.add(new QVector3(0.0f, -1.0f));
        }
        if (isKeyDown(KeyEvent.VK_LEFT)) {
            accumVec.add(new QVector3(-1.0f, 0.0f));
        }
        if (isKeyDown(KeyEvent.VK_RIGHT)) {
            accumVec.add(new QVector3(1.0f, 0.0f));
        }
        return accumVec.normalize( );
    }
}
