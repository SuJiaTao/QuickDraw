// Bailey JT Brown
// 2024
// QMaterial.java

package QDraw;

public class QMaterial {
    /////////////////////////////////////////////////////////////////
    // PUBLIC MEMBERS
    public QColor ambient;
    public QColor diffuse;
    public QColor specular;
    public float  shininess;

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    public QMaterial(
        QColor _ambient, 
        QColor _diffuse, 
        QColor _specular, 
        float  _shininess) {
        ambient   = new QColor(_ambient.toInt( ));
        diffuse   = new QColor(_diffuse.toInt( ));
        specular  = new QColor(_specular.toInt( ));
        shininess = _shininess;
    }
}
