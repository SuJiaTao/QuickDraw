// Bailey JT Brown
// 2024
// QIShader.java

package QDraw;

public interface QIShader {
    public void vertexShader(
        int        vertexNum,
        QVector3   inOutVertex,
        QMatrix4x4 transform,
        Object     userIn
    );
    public QColor fragmentShader(
        int    screenX,
        int    screenY,
        int    belowColorInt,
        int    textureSampleInt,
        Object userIn
    );
}
