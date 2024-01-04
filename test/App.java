// Bailey JT Brown
// 2024
// App.java

import QDraw.*;

public final class App {
    public static void MakeFuzzyTexture( ) {
        QTexture tex = new QTexture(System.getProperty("user.dir") + "\\resources\\Mascot256.png");
        
        QSampleable.ColorProcessFunction mapping = new QSampleable.ColorProcessFunction() {
            public int mapFunc(int col) {
                QColor color = new QColor(col);
                
                // main blue
                if (color.equalsIgnoreAlpha(25, 76, 178)) {
                    float randVal = (QShader.random() * 2.0f) - 1.0f;
                    color = QShader.multiplyColor(color, 1.0f + randVal * 0.09f);
                }

                // secondary blue
                if (color.equalsIgnoreAlpha(127, 153, 204)) {
                    float randVal = (QShader.random() * 2.0f) - 1.0f;
                    color = QShader.multiplyColor(color, 1.0f + randVal * 0.05f);
                }

                // tertiary blue
                if (color.equalsIgnoreAlpha(204, 204, 229)) {
                    float randVal = (QShader.random() * 2.0f) - 1.0f;
                    color = QShader.multiplyColor(color, 1.0f + randVal * 0.02f);
                }

                return color.toInt();
            }
        };

        tex.mapColor(mapping);
        tex.save(System.getProperty("user.dir") + "\\resources\\MascotFuzzy256.png");
    }

    public static void main(String[] args) {
        
    }
}
