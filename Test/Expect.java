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

    public static void MeshTest( ) {
        Expect(
            QMesh.UnitPlane().getTriDataIndicies( ),
            new int[] { 0, 0, 1, 1, 2, 2, 0, 0, 2, 2, 3, 3 } 
        );

        Expect(
            QMesh.UnitPlane().getPos(0), 
            new float[] { -1.0f, -1.0f, 0.0f }
        );

        Expect(
            QMesh.UnitPlane().getPos(1), 
            new float[] { -1.0f, 1.0f, 0.0f }
        );

        Expect(
            QMesh.UnitPlane().getUV(0), 
            new float[] { 0.0f, 0.0f }
        );

        Expect(
            QMesh.UnitPlane().getUV(1), 
            new float[] { 0.0f, 1.0f }
        );

        Expect(
            QMesh.UnitPlane().getPosCount(), 
            4
        );

        Expect(
            QMesh.UnitPlane().getUVCount(), 
            4
        );

        Expect(
            QMesh.UnitPlane().getTriCount(), 
            2
        );

        Expect(
            QMesh.UnitPlane().getTriDataIndicies( ),
            new int[] { 0, 0, 1, 1, 2, 2, 0, 0, 2, 2, 3, 3 } 
        );

        Expect(
            QMesh.UnitPlane().getPos(0), 
            new float[] { -1.0f, -1.0f, 0.0f }
        );

        Expect(
            QMesh.UnitPlane().getPos(1), 
            new float[] { -1.0f, 1.0f, 0.0f }
        );

        Expect(
            QMesh.UnitPlane().getUV(0), 
            new float[] { 0.0f, 0.0f }
        );

        Expect(
            QMesh.UnitPlane().getUV(1), 
            new float[] { 0.0f, 1.0f }
        );

        Expect(
            QMesh.UnitPlane().getPosCount(), 
            4
        );

        Expect(
            QMesh.UnitPlane().getUVCount(), 
            4
        );

        Expect(
            QMesh.UnitPlane().getTriCount(), 
            2
        );

        Expect(
            QMesh.UnitPlane().getTriPos(0, 0), 
            new float[] { -1.0f, -1.0f, 0.0f }
        );

        Expect(
            QMesh.UnitPlane().getTriUV(0, 0), 
            new float[] { 0.0f, 0.0f}
        );

        Expect(
            QMesh.UnitPlane().getTriPos(0, 1), 
            new float[] { -1.0f, 1.0f, 0.0f }
        );

        Expect(
            QMesh.UnitPlane().getTriUV(0, 1), 
            new float[] { 0.0f, 1.0f}
        );

        Expect(
            QMesh.UnitPlane().getTriPos(1, 0), 
            new float[] { -1.0f, -1.0f, 0.0f }
        );

        Expect(
            QMesh.UnitPlane().getTriUV(1, 0), 
            new float[] { 0.0f, 0.0f}
        );

        Expect(
            QMesh.UnitPlane().getTriPos(1, 1), 
            new float[] { 1.0f, 1.0f, 0.0f }
        );

        Expect(
            QMesh.UnitPlane().getTriUV(1, 1), 
            new float[] { 1.0f, 1.0f}
        );

        Expect(
            new QMesh(
                new float[] {
                    1.0f, 2.0f, 3.0f,
                    4.0f, 5.0f, 6.0f,
                    7.0f, 8.0f, 9.0f,
                    1.0f, 2.0f, 3.0f,
                    4.0f, 5.0f, 6.0f,
                    7.0f, 8.0f, 9.0f,
                },
                new float[] {
                    1.0f, 2.0f,
                    4.0f, 5.0f,
                    7.0f, 8.0f,
                    1.0f, 2.0f,
                    4.0f, 5.0f,
                    7.0f, 8.0f,
                },
                new int[][] {
                    { 0, 0, 1, 1, 2, 2, 3, 3 },
                    { 0, 1, 1, 0, 2, 3 },
                    { 3, 3, 4, 4, 2, 2, 5, 5, 0, 0 }
                }
            ).getTriDataIndicies(), 
            new int[] {
                0, 0, 1, 1, 2, 2,
                0, 0, 2, 2, 3, 3,
                0, 1, 1, 0, 2, 3,
                3, 3, 4, 4, 2, 2,
                3, 3, 2, 2, 5, 5,
                3, 3, 5, 5, 0, 0
            }
        );

        QMesh cube = new QMesh(System.getProperty("user.dir") + "\\Resources\\Cube.obj");
        Expect(cube.getPosCount(), 8);
        Expect(cube.getUVCount(), 14);
        Expect(
            cube.getPos(0), 
            new float[] { 1.000000f, 1.000000f, -1.000000f }
        );
        Expect(
            cube.getPos(5), 
            new float[] { -1.000000f, -1.000000f, -1.000000f }
        );
        Expect(
            cube.getPos(5), 
            new float[] { -1.000000f, -1.000000f, -1.000000f }
        );

        /*
         *  f 1/1 5/5 7/9 3/3
            f 4/4 3/3 7/10 8/12
            f 8/13 7/11 5/6 6/8
            f 6/7 2/2 4/4 8/14
            f 2/2 1/1 3/3 4/4
            f 6/8 5/6 1/1 2/2
         */

        System.out.println(Arrays.toString(cube.getTriDataIndicies()));
        
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
        ColorTest( );
        RenderBufferTest( );
        VectorTest( );
        MatrixTest( );
        MeshTest( );
        MathTest( );
    }
}
