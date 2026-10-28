package ca.bael.QDraw;
// Bailey JT Brown

// 2023-2024
// Expects.java

import org.junit.jupiter.api.Test;

import ca.bael.QDraw.QColor.Channel;
import static org.junit.jupiter.api.Assertions.*;

public final class Expects {
    @Test 
    void ColorTest() {
        assertEquals(0, new QColor().toInt());
        assertEquals(0xFF123456, new QColor(0x12, 0x34, 0x56).toInt());
        assertEquals(0x78123456, new QColor(0x12, 0x34, 0x56, 0x78).toInt());
        assertEquals(0x00123456,
                new QColor(0x12, 0x34, 0x56, 0x78)
                        .setChannel(0x00, Channel.A)
                        .toInt());
        assertEquals(0x12, new QColor(0x12, 0x34, 0x56, 0x78).getChannel(Channel.R));
        assertEquals(0x34, new QColor(0x12, 0x34, 0x56, 0x78).getChannel(Channel.G));
        assertEquals(0x56, new QColor(0x12, 0x34, 0x56, 0x78).getChannel(Channel.B));
        assertEquals(0x78, new QColor(0x12, 0x34, 0x56, 0x78).getChannel(Channel.A));
        assertEquals(0x12, new QColor(0x12, 0x34, 0x56, 0x78).getR());
        assertEquals(0x34, new QColor(0x12, 0x34, 0x56, 0x78).getG());
        assertEquals(0x56, new QColor(0x12, 0x34, 0x56, 0x78).getB());
        assertEquals(0x78, new QColor(0x12, 0x34, 0x56, 0x78).getA());
    }

    @Test 
    void RenderBufferTest() {
        assertEquals(Boolean.valueOf(true), Boolean.valueOf(new QRenderBuffer(5, 5).getBufferedImage() != null));
        assertEquals(5 * 5, new QRenderBuffer(5, 5).getColorData().length);
        assertEquals(10 * 5, new QRenderBuffer(10, 5).getColorData().length);
        assertEquals(5 * 7, new QRenderBuffer(5, 7).getColorData().length);
        assertEquals(5 * 5, new QRenderBuffer(5, 5).getDepthData().length);
        assertEquals(10 * 5, new QRenderBuffer(10, 5).getDepthData().length);
        assertEquals(5 * 7, new QRenderBuffer(5, 7).getDepthData().length);
    }

    @Test 
    void VectorTest() {
        assertEquals(new QVector3(1.0f, 2.0f), new QVector3(new float[] { 1.0f, 2.0f }));
        assertEquals(new QVector3(1.0f, 2.0f, 0.0f), new QVector3(new float[] { 1.0f, 2.0f }));
        assertEquals(new QVector3(1.0f, 2.0f, 0.0f), new QVector3(new float[] { 1.0f, 2.0f }));
        assertEquals(new QVector3(1.0f, 2.0f, 3.0f), new QVector3(new float[] { 1.0f, 2.0f, 3.0f }));
        assertEquals(Float.valueOf(1.0f), Float.valueOf(new QVector3(1.0f, 2.0f, 3.0f).getX()));
        assertEquals(Float.valueOf(2.0f), Float.valueOf(new QVector3(1.0f, 2.0f, 3.0f).getY()));
        assertEquals(Float.valueOf(3.0f), Float.valueOf(new QVector3(1.0f, 2.0f, 3.0f).getZ()));
        assertEquals(
                QVector3.add(
                        new QVector3(1.0f, 2.0f, 3.0f),
                        new QVector3(5.0f, 6.0f, 7.0f)),
                new QVector3(1.0f, 2.0f, 3.0f)
                        .add(new QVector3(5.0f, 6.0f, 7.0f)));
        assertEquals(
                QVector3.add(
                        new QVector3(1.0f, 2.0f, 3.0f),
                        new QVector3(5.0f, 6.0f, 7.0f)),
                new QVector3(1.0f, 2.0f, 3.0f)
                        .add(new QVector3(5.0f, 6.0f, 7.0f)));
        assertEquals(
                QVector3.multiply3(
                        new QVector3(1.0f, 2.0f, 3.0f),
                        3.0f),
                new QVector3(1.0f, 2.0f, 3.0f)
                        .multiply3(3.0f));
        assertEquals(
                new QVector3(0.0f, 2.0f, 3.0f),
                new QVector3(1.0f, 2.0f, 3.0f)
                        .setX(0.0f));
        assertEquals(
                new QVector3(1.0f, 0.0f, 3.0f),
                new QVector3(1.0f, 2.0f, 3.0f)
                        .setY(0.0f));
        assertEquals(
                new QVector3(1.0f, 2.0f, 0.0f),
                new QVector3(1.0f, 2.0f, 3.0f)
                        .setZ(0.0f));
    }

    @Test 
    void MatrixTest() {
        assertEquals(
                new QVector3(1.0f, 2.0f, 3.0f),
                QMatrix4x4.multiply(
                        QMatrix4x4.Identity(),
                        new QVector3(1.0f, 2.0f, 3.0f)));
        assertEquals(
                QMatrix4x4.multiply(
                        QMatrix4x4.Identity(),
                        new QVector3(1.0f, 2.0f, 3.0f)),
                QMatrix4x4.multiply(
                        QMatrix4x4.Identity(),
                        new QVector3(1.0f, 2.0f, 3.0f)));
        assertEquals(
                new QVector3(1.0f + 1.0f, 2.0f + 2.0f, 3.0f + 3.0f),
                QMatrix4x4.multiply(
                        QMatrix4x4.translationMatrix(1.0f, 2.0f, 3.0f),
                        new QVector3(1.0f, 2.0f, 3.0f)));
        assertEquals(
                new QVector3(
                        1.0f, 0.0f, 0.0f),
                QMatrix4x4.multiply(
                        QMatrix4x4.rotationMatrix(90.0f, 0.0f, 0.0f),
                        new QVector3(1.0f, 0.0f, 0.0f)));
        assertEquals(
                new QVector3(
                        0.0f, 1.0f, 0.0f),
                QMatrix4x4.multiply(
                        QMatrix4x4.rotationMatrix(0.0f, 90.0f, 0.0f),
                        new QVector3(0.0f, 1.0f, 0.0f)));
        assertEquals(
                new QVector3(
                        0.0f, 0.0f, 1.0f),
                QMatrix4x4.multiply(
                        QMatrix4x4.rotationMatrix(0.0f, 0.0f, 90.0f),
                        new QVector3(0.0f, 0.0f, 1.0f)));
        assertEquals(
                new QVector3(
                        0.0f, 0.0f, 1.0f),
                QMatrix4x4.multiply(
                        QMatrix4x4.rotationMatrix(0.0f, 90.0f, 0.0f),
                        new QVector3(1.0f, 0.0f, 0.0f)));
        assertEquals(
                new QVector3(
                        0.0f, 0.0f, -1.0f),
                QMatrix4x4.multiply(
                        QMatrix4x4.rotationMatrix(0.0f, -90.0f, 0.0f),
                        new QVector3(1.0f, 0.0f, 0.0f)));
        assertEquals(
                new QVector3(
                        0.0f, -1.0f, 0.0f),
                QMatrix4x4.multiply(
                        QMatrix4x4.rotationMatrix(0.0f, 0.0f, 90.0f),
                        new QVector3(1.0f, 0.0f, 0.0f)));
        assertEquals(
                new QVector3(
                        0.0f, 1.0f, 0.0f),
                QMatrix4x4.multiply(
                        QMatrix4x4.rotationMatrix(0.0f, 0.0f, -90.0f),
                        new QVector3(1.0f, 0.0f, 0.0f)));
        assertEquals(
                new QMatrix4x4().rotate(new QVector3(1.0f, 2.0f, 3.0f)),
                QMatrix4x4.rotate(new QMatrix4x4(), new QVector3(1.0f, 2.0f, 3.0f)));
        assertEquals(
                new QMatrix4x4().translate(new QVector3(1.0f, 2.0f, 3.0f)),
                QMatrix4x4.translate(new QMatrix4x4(), new QVector3(1.0f, 2.0f, 3.0f)));
        assertEquals(
                new QMatrix4x4().scale(new QVector3(1.0f, 2.0f, 3.0f)),
                QMatrix4x4.scale(new QMatrix4x4(), new QVector3(1.0f, 2.0f, 3.0f)));
    }

    public static void MathTest() {
        QMatrix4x4 mat0 = QMatrix4x4.translationMatrix(1.0f, 2.0f, 3.0f);

        float[] temp0 = { 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f };
        QMath.mul3_4x4(temp0, mat0.getComponents());
        assertArrayEquals(new float[] { 2.0f, 4.0f, 6.0f, 4.0f, 5.0f, 6.0f }, temp0);

        float[] temp1 = { 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f };
        QMath.mul3_4x4(3, temp1, 0, mat0.getComponents());
        assertArrayEquals(new float[] { 1.0f, 2.0f, 3.0f, 5.0f, 7.0f, 9.0f }, temp1);
    }
}
