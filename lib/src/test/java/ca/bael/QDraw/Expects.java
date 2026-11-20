package ca.bael.QDraw;
// Bailey JT Brown

// 2023-2026
// Expects.java

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

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
        assertEquals(5 * 5, new QRenderBuffer(5, 5).getColorImage().getPixels().length);
        assertEquals(5 * 5, new QRenderBuffer(5, 5).getColorData().length);
        assertEquals(10 * 5, new QRenderBuffer(10, 5).getColorData().length);
        assertEquals(5 * 7, new QRenderBuffer(5, 7).getColorData().length);
        assertEquals(5 * 5, new QRenderBuffer(5, 5).getDepthData().length);
        assertEquals(10 * 5, new QRenderBuffer(10, 5).getDepthData().length);
        assertEquals(5 * 7, new QRenderBuffer(5, 7).getDepthData().length);
    }

    @Test
    void ImageTest() {
        QImage img = QImage.wrap(new int[] { 0x11223344, 0x55667788 }, 1, 2);
        IntBuffer ints = IntBuffer.allocate(2);
        img.writeTo(ints, QImage.PixelLayout.RGBA, true);
        assertArrayEquals(new int[] { 0x66778855, 0x22334411 }, ints.array());
        ByteBuffer bytes = ByteBuffer.allocate(8);
        img.writeTo(bytes, QImage.PixelLayout.BGRA, false);
        assertArrayEquals(new byte[] { 0x44, 0x33, 0x22, 0x11, (byte)0x88, 0x77, 0x66, 0x55 }, bytes.array());
        QImage copy = new QImage(1, 2);
        img.copyTo(copy);
        assertArrayEquals(img.getPixels(), copy.getPixels());
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
        QMatrix4x4 trs = QMatrix4x4.TRS(
                new QVector3(1.0f, -2.0f, 3.0f),
                new QVector3(10.0f, 20.0f, 30.0f),
                new QVector3(1.0f, 2.0f, 0.5f));
        assertEquals(QMatrix4x4.Identity(), QMatrix4x4.multiply(trs, trs.inverse()));
    }

    @Test
    void QuaternionTest() {
        QVector3 axisX = new QVector3(1.0f, 0.0f, 0.0f);
        QVector3 axisZ = new QVector3(0.0f, 0.0f, 1.0f);
        assertEquals(QMatrix4x4.rotationMatrix(30.0f, 0.0f, 0.0f), QQuaternion.fromAxisAngle(axisX, 30.0f).toMatrix());
        assertEquals(QMatrix4x4.rotationMatrix(0.0f, 0.0f, -90.0f), QQuaternion.fromAxisAngle(axisZ, -90.0f).toMatrix());
        assertEquals(new QVector3(0.0f, 1.0f, 0.0f), QQuaternion.fromAxisAngle(axisZ, -90.0f).rotate(new QVector3(1.0f, 0.0f, 0.0f)));
        QQuaternion q90 = QQuaternion.fromAxisAngle(axisZ, 90.0f);
        QQuaternion neg = new QQuaternion(-q90.getW(), -q90.getX(), -q90.getY(), -q90.getZ());
        assertEquals(QQuaternion.fromAxisAngle(axisZ, 45.0f).toMatrix(), QQuaternion.slerp(QQuaternion.Identity(), q90, 0.5f).toMatrix());
        assertEquals(QQuaternion.fromAxisAngle(axisZ, 45.0f).toMatrix(), QQuaternion.nlerp(QQuaternion.Identity(), q90, 0.5f).toMatrix());
        assertEquals(q90.toMatrix(), QQuaternion.slerp(q90, neg, 0.5f).toMatrix());
        QVector3 axisY = new QVector3(0.0f, 1.0f, 0.0f);
        assertEquals(QMatrix4x4.rotationMatrix(10.0f, 20.0f, 30.0f), QQuaternion.fromEuler(10.0f, 20.0f, 30.0f).toMatrix());
        for (QQuaternion q : new QQuaternion[] { QQuaternion.fromEuler(10.0f, 20.0f, 30.0f), QQuaternion.fromEuler(30.0f, 90.0f, 0.0f) }) {
            QVector3 e = q.toEuler();
            assertEquals(q.toMatrix(), QQuaternion.fromEuler(e.getX(), e.getY(), e.getZ()).toMatrix());
        }
        QQuaternion big = QQuaternion.fromAxisAngle(new QVector3(1.0f, 2.0f, 3.0f), 170.0f);
        assertEquals(big.toMatrix(), QQuaternion.fromMatrix(big.toMatrix()).toMatrix());
        assertEquals(QMatrix4x4.rotationMatrix(10.0f, 20.0f, 30.0f), QQuaternion.fromMatrix(QMatrix4x4.rotationMatrix(10.0f, 20.0f, 30.0f)).toMatrix());
        QQuaternion look = QQuaternion.lookRotation(axisX, axisY);
        assertEquals(axisX, look.forward());
        assertEquals(axisY, look.up());
        assertEquals(axisY, QQuaternion.fromToRotation(axisX, axisY).rotate(new QVector3(1.0f, 0.0f, 0.0f)));
        assertEquals(new QVector3(-1.0f, 0.0f, 0.0f), QQuaternion.fromToRotation(axisX, new QVector3(-1.0f, 0.0f, 0.0f)).rotate(new QVector3(1.0f, 0.0f, 0.0f)));
        assertEquals(90.0f, QQuaternion.angle(QQuaternion.Identity(), q90), 1e-3f);
        assertEquals(30.0f, QQuaternion.angle(QQuaternion.Identity(), QQuaternion.rotateTowards(QQuaternion.Identity(), q90, 30.0f)), 1e-2f);
        QQuaternion q60 = QQuaternion.fromAxisAngle(axisZ, 60.0f);
        QVector3 axis = new QVector3();
        float deg = q60.toAxisAngle(axis);
        assertEquals(q60.toMatrix(), QQuaternion.fromAxisAngle(axis, deg).toMatrix());
    }

    @Test
    void MipTest() {
        QTexture tex = new QTexture(2, 1);
        tex.setColor(0, 0, 0xFF000000);
        tex.setColor(1, 0, 0xFFFFFFFF);
        assertEquals(0xFF7F7F7F, tex.sample(0.5f, 0.5f, QSampleable.SampleType.Clamp, QSampleable.FilterType.Bilinear));
        assertEquals(0xFFFFFFFF, tex.sample(0.75f, 0.5f));
        QMipTexture mips = new QMipTexture(tex);
        assertEquals(2, mips.getLevelCount());
        assertEquals(0xFF808080, mips.getLevel(1).getColor(0, 0));
        assertEquals(0xFF808080, mips.sampleLod(0.5f, 0.5f, 1.0f, QSampleable.SampleType.Clamp, QSampleable.FilterType.Bilinear));
        assertEquals(0xFF808080, mips.sampleLod(0.5f, 0.5f, 0.6f, QSampleable.SampleType.Clamp, QSampleable.FilterType.Nearest));
    }

    @Test
    void ProjectionTest() {
        QProjection proj = QProjection.perspective(90.0f, 2.0f, 0.1f);
        assertEquals(2.0f, proj.right(), 1e-5f);
        assertEquals(-1.0f, proj.bottom(), 1e-5f);
        float[] out = new float[3];
        proj.toScreen(new float[] { 0.0f, 0.0f, -1.0f }, 200, 100, out, 0);
        assertArrayEquals(new float[] { 100.0f, 50.0f, -1.0f }, out, 1e-4f);
        proj.toScreen(new float[] { 0.5f, -0.25f, -2.0f }, 200, 100, out, 0);
        assertEquals(new QVector3(0.5f, -0.25f, -2.0f), proj.toView(out[0], out[1], out[2], 200, 100, new QVector3()));
        assertEquals(new QVector3(0.0f, 0.0f, -1.0f), proj.viewDirection(100.0f, 50.0f, 200, 100, new QVector3()));
        assertThrows(QException.class, () -> QProjection.perspective(90.0f, 1.0f, -0.1f));
    }

    @Test
    void FloatsTest() {
        float[] src = { 1.0f, 2.0f, 3.0f };
        QFloats floats = new QFloats(src);
        src[0] = 9.0f;
        assertEquals(3, floats.count());
        assertEquals(1.0f, floats.get(0));
        assertSame(floats, floats.snapshot());
    }

    @Test
    void CopyAndReadTest() {
        assertThrows(QException.class, () -> new QTexture(2, 2).copyTo(new QImage(3, 2)));

        QMesh mesh = QMesh.screenQuad();
        QMesh copy = new QMesh(mesh);
        copy.getPosBuffer().set(0, 9.0f, 9.0f, 9.0f);
        assertNotEquals(9.0f, mesh.getPosBuffer().getBuffer()[0]);

        QRenderBuffer rb = new QRenderBuffer(4, 2);
        rb.setDepth(3, 1, -0.5f);
        QShader.FragmentShaderContext fctx = new QShader.FragmentShaderContext();
        fctx.textures = new QSampleable[] { rb };
        assertEquals(-0.5f, fctx.gbufferDepth(0, 3, 1));
        assertEquals(-0.5f, fctx.gbufferDepth(0, 10, 10));
    }

    @Test 
    public void MathTest() {
        QMatrix4x4 mat0 = QMatrix4x4.translationMatrix(1.0f, 2.0f, 3.0f);

        float[] temp0 = { 1.0f, 2.0f, 3.0f };
        mat0.transformPoint(temp0);
        assertArrayEquals(new float[] { 2.0f, 4.0f, 6.0f }, temp0);

        float[] temp1 = { 1.0f, 2.0f, 3.0f };
        mat0.transformDir(temp1);
        assertArrayEquals(new float[] { 1.0f, 2.0f, 3.0f }, temp1);
    }

    @Test
    void RandomTest() {
        QShader.VertexShaderContext   vctx = new QShader.VertexShaderContext();
        QShader.FragmentShaderContext fctx = new QShader.FragmentShaderContext();
        for (int i = 0; i < 10000; i++) {
            for (float r : new float[] { QShader.random(), vctx.random(), fctx.random() }) {
                assertTrue(r >= 0.0f && r < 1.0f, "random out of [0, 1): " + r);
            }
        }
        assertNotEquals(vctx.random(), vctx.random());
    }
}
