// Bailey JT Brown
// 2023
// Test.java

import java.util.Arrays;

import QDraw.*;
import QDraw.QException.PointOfError;
import QDraw.QColor.Channel;

public final class Test {

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
            new QVector4(new float[]{1.0f, 2.0f, 3.0f}),
            new QVector4(1.0f, 2.0f, 3.0f)
        );
        Expect(
            new QVector4(new float[]{1.0f, 2.0f, 3.0f, 4.0f}),
            new QVector4(1.0f, 2.0f, 3.0f, 4.0f)
        );
        Expect(
            new QVector4(5.0f, 5.0f, 5.0f).getW(),
            1.0f
        );
        Expect(
            new QVector4(5.0f, 5.0f, 5.0f, 2.0f).getW(),
            2.0f
        );
        Expect(
            new QVector4(1.0f, 2.0f, 3.0f, 4.0f).getX(),
            1.0f
        );
        Expect(
            new QVector4(1.0f, 2.0f, 3.0f, 4.0f).getY(),
            2.0f
        );
        Expect(
            new QVector4(1.0f, 2.0f, 3.0f, 4.0f).getZ(),
            3.0f
        );
        Expect(
            new QVector4(1.0f, 2.0f, 3.0f, 4.0f).getW(),
            4.0f
        );
        Expect(
            new QVector4(1.0f, 2.0f, 3.0f, 4.0f)
                .add3(new QVector4(5.0f, 6.0f, 7.0f, 8.0f)),
            QVector4.add3(
                new QVector4(1.0f, 2.0f, 3.0f, 4.0f),
                new QVector4(5.0f, 6.0f, 7.0f, 8.0f)
            )
        );
        Expect(
            new QVector4(1.0f, 2.0f, 3.0f, 4.0f)
                .add4(new QVector4(5.0f, 6.0f, 7.0f, 8.0f)),
            QVector4.add4(
                new QVector4(1.0f, 2.0f, 3.0f, 4.0f),
                new QVector4(5.0f, 6.0f, 7.0f, 8.0f)
            )
        );
        Expect(
            new QVector4(1.0f, 2.0f, 3.0f, 4.0f)
                .multiply3(3.0f),
            QVector4.multiply3(
                new QVector4(1.0f, 2.0f, 3.0f, 4.0f),
                3.0f
            )
        );
        Expect(
            new QVector4(1.0f, 2.0f, 3.0f, 4.0f)
                .multiply4(3.0f),
            QVector4.multiply4(
                new QVector4(1.0f, 2.0f, 3.0f, 4.0f),
                3.0f
            )
        );
        Expect(
            new QVector4(1.0f, 2.0f, 3.0f, 4.0f)
                .setX(0.0f),
            new QVector4(0.0f, 2.0f, 3.0f, 4.0f)
        );
        Expect(
            new QVector4(1.0f, 2.0f, 3.0f, 4.0f)
                .setY(0.0f),
            new QVector4(1.0f, 0.0f, 3.0f, 4.0f)
        );
        Expect(
            new QVector4(1.0f, 2.0f, 3.0f, 4.0f)
                .setZ(0.0f),
            new QVector4(1.0f, 2.0f, 0.0f, 4.0f)
        );
        Expect(
            new QVector4(1.0f, 2.0f, 3.0f, 4.0f)
                .setW(0.0f),
            new QVector4(1.0f, 2.0f, 3.0f, 0.0f)
        );
    }

    public static void MatrixTest( ) {
        Expect(
            QMatrix4x4.multiply3(
                QMatrix4x4.identity, 
                new QVector4(1.0f, 2.0f, 3.0f, 4.0f)
            ),
            new QVector4(1.0f, 2.0f, 3.0f, 1.0f)
        );
        Expect(
            QMatrix4x4.multiply4(
                QMatrix4x4.identity, 
                new QVector4(1.0f, 2.0f, 3.0f, 4.0f)
            ),
            new QVector4(1.0f, 2.0f, 3.0f, 4.0f)
        );
        Expect(
            QMatrix4x4.multiply4(
                QMatrix4x4.identity, 
                new QVector4(1.0f, 2.0f, 3.0f, 1.0f)
            ),
            QMatrix4x4.multiply3(
                QMatrix4x4.identity, 
                new QVector4(1.0f, 2.0f, 3.0f, 1.0f)
            )
        );
        Expect(
            QMatrix4x4.multiply4(
                QMatrix4x4.translationMatrix(1.0f, 2.0f, 3.0f), 
                new QVector4(1.0f, 2.0f, 3.0f, 1.0f)
            ),
            new QVector4(1.0f + 1.0f, 2.0f + 2.0f, 3.0f + 3.0f, 1.0f)
        );
        Expect(
            QMatrix4x4.multiply4(
                QMatrix4x4.translationMatrix(1.0f, 2.0f, 3.0f), 
                new QVector4(1.0f, 2.0f, 3.0f, 2.0f)
            ),
            new QVector4(
                1.0f + 1.0f * 2.0f, 
                2.0f + 2.0f * 2.0f, 
                3.0f + 3.0f * 2.0f, 
                2.0f
            )
        );
        Expect(
            QMatrix4x4.multiply3(
                QMatrix4x4.translationMatrix(1.0f, 2.0f, 3.0f), 
                new QVector4(1.0f, 2.0f, 3.0f, 2.0f)
            ),
            new QVector4(
                1.0f + 1.0f * 2.0f, 
                2.0f + 2.0f * 2.0f, 
                3.0f + 3.0f * 2.0f, 
                1.0f
            )
        );
        Expect(
            QMatrix4x4.multiply4(
                QMatrix4x4.scaleMatrix(2.0f, 3.0f, 4.0f), 
                new QVector4(1.0f, 2.0f, 3.0f, 2.0f)
            ),
            new QVector4(
                1.0f * 2.0f,
                2.0f * 3.0f,
                3.0f * 4.0f,
                2.0f
            )
        );
        Expect(
            QMatrix4x4.multiply3(
                QMatrix4x4.scaleMatrix(2.0f, 3.0f, 4.0f), 
                new QVector4(1.0f, 2.0f, 3.0f, 2.0f)
            ),
            new QVector4(
                1.0f * 2.0f,
                2.0f * 3.0f,
                3.0f * 4.0f,
                1.0f
            )
        );
        Expect(
            QMatrix4x4.multiply4(
                QMatrix4x4.rotationMatrix(90.0f, 0.0f, 0.0f), 
                new QVector4(1.0f, 0.0f, 0.0f, 1.0f)
            ),
            new QVector4(
                1.0f, 0.0f, 0.0f, 1.0f
            )
        );
        Expect(
            QMatrix4x4.multiply4(
                QMatrix4x4.rotationMatrix(0.0f, 90.0f, 0.0f), 
                new QVector4(0.0f, 1.0f, 0.0f, 1.0f)
            ),
            new QVector4(
                0.0f, 1.0f, 0.0f, 1.0f
            )
        );
        Expect(
            QMatrix4x4.multiply4(
                QMatrix4x4.rotationMatrix(0.0f, 0.0f, 90.0f), 
                new QVector4(0.0f, 0.0f, 1.0f, 1.0f)
            ),
            new QVector4(
                0.0f, 0.0f, 1.0f, 1.0f
            )
        );
        Expect(
            QMatrix4x4.multiply4(
                QMatrix4x4.rotationMatrix(0.0f, 90.0f, 0.0f), 
                new QVector4(1.0f, 0.0f, 0.0f, 1.0f)
            ),
            new QVector4(
                0.0f, 0.0f, 1.0f, 1.0f
            )
        );
        Expect(
            QMatrix4x4.multiply4(
                QMatrix4x4.rotationMatrix(0.0f, -90.0f, 0.0f), 
                new QVector4(1.0f, 0.0f, 0.0f, 1.0f)
            ),
            new QVector4(
                0.0f, 0.0f, -1.0f, 1.0f
            )
        );
        Expect(
            QMatrix4x4.multiply4(
                QMatrix4x4.rotationMatrix(0.0f, 0.0f, 90.0f), 
                new QVector4(1.0f, 0.0f, 0.0f, 1.0f)
            ),
            new QVector4(
                0.0f, -1.0f, 0.0f, 1.0f
            )
        );
        Expect(
            QMatrix4x4.multiply4(
                QMatrix4x4.rotationMatrix(0.0f, 0.0f, -90.0f), 
                new QVector4(1.0f, 0.0f, 0.0f, 1.0f)
            ),
            new QVector4(
                0.0f, 1.0f, 0.0f, 1.0f
            )
        );
    }

    public static void MeshTest( ) {
        Expect(
            QMesh.unitPlane.getTriDataIndicies( ),
            new int[] { 0, 0, 1, 1, 2, 2, 0, 0, 2, 2, 3, 3 } 
        );

        Expect(
            QMesh.unitPlane.getPos(0), 
            new float[] { -1.0f, -1.0f, 0.0f }
        );

        Expect(
            QMesh.unitPlane.getPos(1), 
            new float[] { -1.0f, 1.0f, 0.0f }
        );

        Expect(
            QMesh.unitPlane.getUV(0), 
            new float[] { 0.0f, 0.0f }
        );

        Expect(
            QMesh.unitPlane.getUV(1), 
            new float[] { 0.0f, 1.0f }
        );

        Expect(
            QMesh.unitPlane.getPosCount(), 
            4
        );

        Expect(
            QMesh.unitPlane.getUVCount(), 
            4
        );

        Expect(
            QMesh.unitPlane.getTriCount(), 
            2
        );

        Expect(
            QMesh.unitPlane.getTriPos(0, 0), 
            new float[] { -1.0f, -1.0f, 0.0f }
        );

        Expect(
            QMesh.unitPlane.getTriUV(0, 0), 
            new float[] { 0.0f, 0.0f}
        );

        Expect(
            QMesh.unitPlane.getTriPos(0, 1), 
            new float[] { -1.0f, 1.0f, 0.0f }
        );

        Expect(
            QMesh.unitPlane.getTriUV(0, 1), 
            new float[] { 0.0f, 1.0f}
        );

        Expect(
            QMesh.unitPlane.getTriPos(1, 0), 
            new float[] { -1.0f, -1.0f, 0.0f }
        );

        Expect(
            QMesh.unitPlane.getTriUV(1, 0), 
            new float[] { 0.0f, 0.0f}
        );

        Expect(
            QMesh.unitPlane.getTriPos(1, 1), 
            new float[] { 1.0f, 1.0f, 0.0f }
        );

        Expect(
            QMesh.unitPlane.getTriUV(1, 1), 
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
            });
    }

    public static void main(String[] args) {
        ColorTest( );
        RenderBufferTest( );
        VectorTest( );
        MatrixTest( );
        MeshTest( );

        QRenderBuffer rb = new QRenderBuffer(50, 50);
        QWindow window   = new QWindow("testwin", 500, 500);
        rb.getColorData()[rb.coordToDataIndex(5, 5)] = QColor.red.toInt();

        window.setRenderBuffer(rb);

        while(true) {
            window.updateFrame();
        }
    }
}
