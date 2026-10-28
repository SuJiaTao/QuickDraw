// Bailey JT Brown
// 2024
// MakeFuzzTexture.java

import ca.bael.QDraw.*;
import java.io.File;

public final class MakeFuzzTexture {
    public static void MakeFuzzyTexture( ) {
        final String inFile = MakeFuzzTexture.class.getResource("/Mascot256_AltEyes.png").getPath();
        QTexture tex = new QTexture(inFile);
        
        QSampleable.ColorMapSpacialFunction mapping = new QSampleable.ColorMapSpacialFunction() {
            public int mapFunc(int col, int x, int y) {
                QColor color = new QColor(col);
                
                int seed0 = (x >> 1) + (y >> 2) * tex.getWidth();
                int seed1 = (x >> 1) + (y >> 1) * tex.getWidth();
                int seed2 = (x >> 2) + (y >> 2) * tex.getWidth();

                // main blue
                if (color.equalsIgnoreAlpha(25, 76, 178)) {
                    float randVal = (QShader.seededRandom(seed0) * 2.0f) - 1.0f;
                    color = QShader.multiplyColor(
                        color, 
                        0.97f + randVal * 0.04f
                    );
                }

                // secondary blue
                if (color.equalsIgnoreAlpha(127, 153, 204)) {
                    float randVal = (QShader.seededRandom(seed1) * 2.0f) - 1.0f;
                    color = QShader.multiplyColor(
                        color, 
                        0.98f + randVal * 0.03f
                    );
                }

                // tertiary blue
                if (color.equalsIgnoreAlpha(204, 204, 229)) {
                    float randVal = (QShader.seededRandom(seed2) * 2.0f) - 1.0f;
                    color = QShader.multiplyColor(
                        color, 
                        0.96f + randVal * 0.04f
                    );
                }

                return color.toInt();
            }
        };

        tex.mapColorSpacial(mapping);
        final File outFile = new File(System.getProperty("user.dir") + "/out/Mascot256_AltEyes.png");
        outFile.getParentFile().mkdirs();
        tex.save(outFile.getAbsolutePath());
    }

    public static void main(String[] args) {
        MakeFuzzyTexture( );
    }
}
