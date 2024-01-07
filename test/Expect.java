// Bailey JT Brown
// 2023-2024
// Expect.java

import QDraw.*;
import java.util.Arrays;
import QDraw.QColor.Channel;
import QDraw.QException.PointOfError;

public final class Expect {

    private static void Expect(int[] val, int[] expected) {
        if (!(Arrays.equals(val, expected))) {
                throw new QException(
                    PointOfError.BadState,
                    String.format("value \n%s\n differs from expected \n%s",
                    Arrays.toString(val),
                    Arrays.toString(expected)
                ));
            }
    }
    
    private static void Expect(float[] val, float[] expected) {
        if (!(Arrays.equals(val, expected))) {
                throw new QException(
                    PointOfError.BadState,
                    String.format("value \n%s\n differs from expected \n%s",
                    Arrays.toString(val),
                    Arrays.toString(expected)
                ));
            }
    }

    private static void Expect(Object[] val, Object[] expected) {
        if (!(Arrays.equals(val, expected))) {
                throw new QException(
                    PointOfError.BadState,
                    String.format("value \n%s\n differs from expected \n%s",
                    Arrays.toString(val),
                    Arrays.toString(expected)
                ));
            }
    }

    private static void Expect(Object val, Object expected) {
        if (!(expected.equals(val) && val.equals(expected))) {
            throw new QException(
                PointOfError.BadState,
                String.format("value \n%s\n differs from expected \n%s",
                val.toString(),
                expected.toString()
            ));
        } 
    }

    private static void ColorTest( ) {
        Expect(
            new Integer(new QColor().toInt()), 
            new Integer(0)
        );
        Expect(
            new Integer(new QColor(0x12, 0x34, 0x56).toInt()), 
            new Integer(0xFF123456)
        );
        Expect(
            new Integer(new QColor(0x12, 0x34, 0x56, 0x78).toInt()), 
            new Integer(0x78123456)
        );
        Expect(
            new Integer(
                new QColor(0x12, 0x34, 0x56, 0x78)
                    .setChannel(0x00, Channel.A)
                    .toInt()), 
            new Integer(0x00123456)
        );
        Expect(
            new QColor(0x12, 0x34, 0x56, 0x78).getChannel(Channel.R),
            0x12
        );
        Expect(
            new QColor(0x12, 0x34, 0x56, 0x78).getChannel(Channel.G),
            0x34
        );
        Expect(
            new QColor(0x12, 0x34, 0x56, 0x78).getChannel(Channel.B),
            0x56
        );
        Expect(
            new QColor(0x12, 0x34, 0x56, 0x78).getChannel(Channel.A),
            0x78
        );
        Expect(
            new QColor(0x12, 0x34, 0x56, 0x78).getR(),
            0x12
        );
        Expect(
            new QColor(0x12, 0x34, 0x56, 0x78).getG(),
            0x34
        );
        Expect(
            new QColor(0x12, 0x34, 0x56, 0x78).getB(),
            0x56
        );
        Expect(
            new QColor(0x12, 0x34, 0x56, 0x78).getA(),
            0x78
        );
    }

    private static void RenderBufferTest( ) {
        Expect(
            new QRenderBuffer(5, 5).getBufferedImage() != null, 
            true
        );
        Expect(
            new QRenderBuffer(5, 5).getColorData().length,
            5 * 5
        );
        Expect(
            new QRenderBuffer(10, 5).getColorData().length,
            10 * 5
        );
        Expect(
            new QRenderBuffer(5, 7).getColorData().length,
            5 * 7
        );
        Expect(
            new QRenderBuffer(5, 5).getDepthData().length,
            5 * 5
        );
        Expect(
            new QRenderBuffer(10, 5).getDepthData().length,
            10 * 5
        );
        Expect(
            new QRenderBuffer(5, 7).getDepthData().length,
            5 * 7
        );
    }

    private static void VectorTest( ) {
        Expect(
            new QVector3(new float[]{1.0f, 2.0f}),
            new QVector3(1.0f, 2.0f)
        );
        Expect(
            new QVector3(new float[]{1.0f, 2.0f}),
            new QVector3(1.0f, 2.0f, 0.0f)
        );
        Expect(
            new QVector3(new float[]{1.0f, 2.0f}),
            new QVector3(1.0f, 2.0f, 0.0f)
        );
        Expect(
            new QVector3(new float[]{1.0f, 2.0f, 3.0f}),
            new QVector3(1.0f, 2.0f, 3.0f)
        );
        Expect(
            new QVector3(1.0f, 2.0f, 3.0f).getX(),
            1.0f
        );
        Expect(
            new QVector3(1.0f, 2.0f, 3.0f).getY(),
            2.0f
        );
        Expect(
            new QVector3(1.0f, 2.0f, 3.0f).getZ(),
            3.0f
        );
        Expect(
            new QVector3(1.0f, 2.0f, 3.0f)
                .add(new QVector3(5.0f, 6.0f, 7.0f)),
            QVector3.add(
                new QVector3(1.0f, 2.0f, 3.0f),
                new QVector3(5.0f, 6.0f, 7.0f)
            )
        );
        Expect(
            new QVector3(1.0f, 2.0f, 3.0f)
                .add(new QVector3(5.0f, 6.0f, 7.0f)),
            QVector3.add(
                new QVector3(1.0f, 2.0f, 3.0f),
                new QVector3(5.0f, 6.0f, 7.0f)
            )
        );
        Expect(
            new QVector3(1.0f, 2.0f, 3.0f)
                .multiply3(3.0f),
            QVector3.multiply3(
                new QVector3(1.0f, 2.0f, 3.0f),
                3.0f
            )
        );
        Expect(
            new QVector3(1.0f, 2.0f, 3.0f)
                .setX(0.0f),
            new QVector3(0.0f, 2.0f, 3.0f)
        );
        Expect(
            new QVector3(1.0f, 2.0f, 3.0f)
                .setY(0.0f),
            new QVector3(1.0f, 0.0f, 3.0f)
        );
        Expect(
            new QVector3(1.0f, 2.0f, 3.0f)
                .setZ(0.0f),
            new QVector3(1.0f, 2.0f, 0.0f)
        );
    }

    public static void MatrixTest( ) {
        Expect(
            QMatrix4x4.multiply(
                QMatrix4x4.Identity(), 
                new QVector3(1.0f, 2.0f, 3.0f)
            ),
            new QVector3(1.0f, 2.0f, 3.0f)
        );
        Expect(
            QMatrix4x4.multiply(
                QMatrix4x4.Identity(), 
                new QVector3(1.0f, 2.0f, 3.0f)
            ),
            QMatrix4x4.multiply(
                QMatrix4x4.Identity(), 
                new QVector3(1.0f, 2.0f, 3.0f)
            )
        );
        Expect(
            QMatrix4x4.multiply(
                QMatrix4x4.translationMatrix(1.0f, 2.0f, 3.0f), 
                new QVector3(1.0f, 2.0f, 3.0f)
            ),
            new QVector3(1.0f + 1.0f, 2.0f + 2.0f, 3.0f + 3.0f)
        );
        Expect(
            QMatrix4x4.multiply(
                QMatrix4x4.rotationMatrix(90.0f, 0.0f, 0.0f), 
                new QVector3(1.0f, 0.0f, 0.0f)
            ),
            new QVector3(
                1.0f, 0.0f, 0.0f
            )
        );
        Expect(
            QMatrix4x4.multiply(
                QMatrix4x4.rotationMatrix(0.0f, 90.0f, 0.0f), 
                new QVector3(0.0f, 1.0f, 0.0f)
            ),
            new QVector3(
                0.0f, 1.0f, 0.0f
            )
        );
        Expect(
            QMatrix4x4.multiply(
                QMatrix4x4.rotationMatrix(0.0f, 0.0f, 90.0f), 
                new QVector3(0.0f, 0.0f, 1.0f)
            ),
            new QVector3(
                0.0f, 0.0f, 1.0f
            )
        );
        Expect(
            QMatrix4x4.multiply(
                QMatrix4x4.rotationMatrix(0.0f, 90.0f, 0.0f), 
                new QVector3(1.0f, 0.0f, 0.0f)
            ),
            new QVector3(
                0.0f, 0.0f, 1.0f
            )
        );
        Expect(
            QMatrix4x4.multiply(
                QMatrix4x4.rotationMatrix(0.0f, -90.0f, 0.0f), 
                new QVector3(1.0f, 0.0f, 0.0f)
            ),
            new QVector3(
                0.0f, 0.0f, -1.0f
            )
        );
        Expect(
            QMatrix4x4.multiply(
                QMatrix4x4.rotationMatrix(0.0f, 0.0f, 90.0f), 
                new QVector3(1.0f, 0.0f, 0.0f)
            ),
            new QVector3(
                0.0f, -1.0f, 0.0f
            )
        );
        Expect(
            QMatrix4x4.multiply(
                QMatrix4x4.rotationMatrix(0.0f, 0.0f, -90.0f), 
                new QVector3(1.0f, 0.0f, 0.0f)
            ),
            new QVector3(
                0.0f, 1.0f, 0.0f
            )
        );
        Expect(
            QMatrix4x4.rotate(new QMatrix4x4(), new QVector3(1.0f, 2.0f, 3.0f)),
            new QMatrix4x4().rotate(new QVector3(1.0f, 2.0f, 3.0f))
        );
        Expect(
            QMatrix4x4.translate(new QMatrix4x4(), new QVector3(1.0f, 2.0f, 3.0f)),
            new QMatrix4x4().translate(new QVector3(1.0f, 2.0f, 3.0f))
        );
        Expect(
            QMatrix4x4.scale(new QMatrix4x4(), new QVector3(1.0f, 2.0f, 3.0f)),
            new QMatrix4x4().scale(new QVector3(1.0f, 2.0f, 3.0f))
        );
    }
    
    public static void MathTest( ) {
        QMatrix4x4 mat0 = QMatrix4x4.translationMatrix(1.0f, 2.0f, 3.0f);

        float[] temp0 = { 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f };
        QMath.mul3_4x4(temp0, mat0.getComponents());
        Expect(
            temp0, 
            new float[] { 2.0f, 4.0f, 6.0f, 4.0f, 5.0f, 6.0f }
        );

        float[] temp1 = { 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f };
        QMath.mul3_4x4(3, temp1, 0, mat0.getComponents());
        Expect(
            temp1, 
            new float[] { 1.0f, 2.0f, 3.0f, 5.0f, 7.0f, 9.0f }
        );
    }

    public static void main(String[] args) {
        System.out.println("Beginning tests...");
        ColorTest( );
        RenderBufferTest( );
        VectorTest( );
        MatrixTest( );
        MathTest( );
        System.out.println("All tests passed!");
    }
}
