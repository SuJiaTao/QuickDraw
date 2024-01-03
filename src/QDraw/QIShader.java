// Bailey JT Brown
// 2024
// QIShader.java

package QDraw;

public interface QIShader {
    public QVector3 vertexShader(
        int        vertexNum,
        QVector3   inVertex,
        QMatrix4x4 transform,
        Object     userIn
    );
    public QColor fragmentShader(
        int    screenX,
        int    screenY,
        QColor belowColor,
        QColor textureSample,
        Object userIn
    );
}
