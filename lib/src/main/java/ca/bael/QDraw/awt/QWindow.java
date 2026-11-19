// Bailey JT Brown
// 2023-2026
// QWindow.java

package ca.bael.QDraw.awt;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import ca.bael.QDraw.QException;
import ca.bael.QDraw.QException.PointOfError;
import ca.bael.QDraw.QImage;
import ca.bael.QDraw.QPresenter;
import ca.bael.QDraw.QTimer;
import ca.bael.QDraw.QVector3;

public final class QWindow implements QPresenter {
    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private final JFrame        window;
    private final QSwingCanvas  canvas;
    private final InputListener listener;

    /////////////////////////////////////////////////////////////////
    // PUBLIC MEMBERS
    public final QTimer drawTimer;
    public final QTimer showTimer;

    /////////////////////////////////////////////////////////////////
    // PRIVATE CLASSES
    private static class InputListener implements KeyListener, MouseListener {
        /////////////////////////////////////////////////////////////////
        // CONSTANTS
        private static final int KEY_BUFFER_INITIAL_SIZE = 20;
        public ArrayList<Integer>   keyCodesDown = new ArrayList<>(KEY_BUFFER_INITIAL_SIZE);
        public ArrayList<Character> charsDown    = new ArrayList<>(KEY_BUFFER_INITIAL_SIZE);
        public boolean mouseDown = false;
        public volatile int mouseButtonsDown = 0;

        /////////////////////////////////////////////////////////////////
        // CALLBACKS
        public void mouseClicked(MouseEvent arg0) {  }
        public void mouseEntered(MouseEvent arg0) {  }
        public void mouseExited(MouseEvent arg0) {  }

        public void mousePressed(MouseEvent arg0) {
            mouseDown = true;
            mouseButtonsDown |= 1 << arg0.getButton( );
        }

        public void mouseReleased(MouseEvent arg0) {
            mouseDown = false;
            mouseButtonsDown &= ~(1 << arg0.getButton( ));
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
                keyCodesDown.remove(Integer.valueOf(arg0.getKeyCode( )));
            }
            
            if (charsDown.contains(arg0.getKeyChar( ))) {
                charsDown.remove(Character.valueOf(arg0.getKeyChar( )));
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

        canvas = new QSwingCanvas( );
        canvas.setPreferredSize(new Dimension(width, height));
        drawTimer = canvas.drawTimer;
        showTimer = canvas.showTimer;

        window = new JFrame(title);
        window.add(canvas);
        window.pack( );
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);

        listener = new InputListener( );
        canvas.addKeyListener(listener);
        canvas.addMouseListener(listener);
        canvas.requestFocus( );
    }

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public Frame getFrame( ) {
        return window;
    }

    @Override
    public void present(QImage frame) {
        canvas.present(frame);
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

    public boolean isMouseButtonDown(int button) {
        return (listener.mouseButtonsDown & (1 << button)) != 0;
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
