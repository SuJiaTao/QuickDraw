// Bailey JT Brown
// 2024
// App.java

import QDraw.*;

public final class App {
    public static void MakeFuzzyTexture( ) {
        QTexture tex = new QTexture(System.getProperty("user.dir") + "\\resources\\Mascot256.png");
        
        QSampleable.ColorMapSpacialFunction mapping = new QSampleable.ColorMapSpacialFunction() {
            public int mapFunc(int col, int x, int y) {
                QColor color = new QColor(col);
                
                // main blue
                if (color.equalsIgnoreAlpha(25, 76, 178)) {
                    float randVal = (QShader.seededRandom(x + y * tex.getWidth()) * 2.0f) - 1.0f;
                    color = QShader.multiplyColor(
                        color, 
                        1.0f,
                        1.0f,
                        1.0f + randVal * 0.09f
                    );
                }

                // secondary blue
                if (color.equalsIgnoreAlpha(127, 153, 204)) {
                    float randVal = (QShader.seededRandom(x + y * tex.getWidth()) * 2.0f) - 1.0f;
                    color = QShader.multiplyColor(
                        color, 
                        1.0f,
                        1.0f,
                        1.0f + randVal * 0.09f
                    );
                }

                // tertiary blue
                if (color.equalsIgnoreAlpha(204, 204, 229)) {
                    float randVal = (QShader.seededRandom(x + y * tex.getWidth()) * 2.0f) - 1.0f;
                    color = QShader.multiplyColor(
                        color,
                        1.0f,
                        1.0f,
                        0.9f + randVal * 0.09f
                    );
                }

                return color.toInt();
            }
        };

        tex.mapColorSpacial(mapping);
        tex.save(System.getProperty("user.dir") + "\\resources\\MascotFuzzy256.png");
    }

    public static void main(String[] args) {
        MakeFuzzyTexture( );
    }
}
