// Bailey JT Brown
// 2026
// QLights.java

package ca.bael.QDraw;

public final class QLights implements QUniform<QLights> {
    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private final QLight[] lights;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public int count( ) {
        return lights.length;
    }

    public QLight get(int index) {
        return lights[index];
    }

    public QLights snapshot( ) {
        QLight[] copies = new QLight[lights.length];
        for (int i = 0; i < copies.length; i++) {
            copies[i] = lights[i].snapshot( );
        }
        return new QLights(copies);
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    public QLights(QLight... _lights) {
        lights = _lights.clone( );
    }
}
