// Bailey JT Brown
// 2023-2026
// QWindow.java

package ca.bael.QDraw.awt;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    private final Set<Integer>  keysDown = ConcurrentHashMap.newKeySet( );
    private volatile int        mouseButtonsDown;

    /////////////////////////////////////////////////////////////////
    // PUBLIC MEMBERS
    public final QTimer drawTimer;
    public final QTimer showTimer;

    /////////////////////////////////////////////////////////////////
    // PRIVATE CLASSES
    private final class InputListener implements KeyListener, MouseListener, FocusListener {
        public void keyPressed(KeyEvent e)      { keysDown.add(e.getKeyCode( )); }
        public void keyReleased(KeyEvent e)     { keysDown.remove(e.getKeyCode( )); }
        public void keyTyped(KeyEvent e)        { }
        public void mousePressed(MouseEvent e)  { mouseButtonsDown |= 1 << e.getButton( ); }
        public void mouseReleased(MouseEvent e) { mouseButtonsDown &= ~(1 << e.getButton( )); }
        public void mouseClicked(MouseEvent e)  { }
        public void mouseEntered(MouseEvent e)  { }
        public void mouseExited(MouseEvent e)   { }
        public void focusGained(FocusEvent e)   { }
        public void focusLost(FocusEvent e) {
            keysDown.clear( );
            mouseButtonsDown = 0;
        }
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

        InputListener listener = new InputListener( );
        canvas.addKeyListener(listener);
        canvas.addMouseListener(listener);
        canvas.addFocusListener(listener);
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
        return keysDown.contains(keyCode);
    }

    public boolean isMouseButtonDown(int button) {
        return (mouseButtonsDown & (1 << button)) != 0;
    }

    public QVector3 getInputWASDVector( ) {
        return internalAxis(KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D);
    }

    public QVector3 getInputArrowKeysVector( ) {
        return internalAxis(KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT);
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private QVector3 internalAxis(int up, int down, int left, int right) {
        return new QVector3(
            (isKeyDown(right) ? 1.0f : 0.0f) - (isKeyDown(left) ? 1.0f : 0.0f),
            (isKeyDown(up) ? 1.0f : 0.0f) - (isKeyDown(down) ? 1.0f : 0.0f)
        ).normalize( );
    }
}
