// Bailey JT Brown
// 2023-2024
// RasterWorker.java

package ca.bael.QDraw;

import static ca.bael.QDraw.QVector3.VCTR_INDEX_X;
import static ca.bael.QDraw.QVector3.VCTR_INDEX_Y;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

final class RasterWorker {
    ////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final float DEPTH_TEST_EPSILON = 0.002f;
    private static final int   STRIP_HEIGHT       = 8;

    ////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private final QRenderBuffer renderTarget;
    private final RasterSetup raster = new RasterSetup();
    private final QShader.FragmentShaderContext fctx = new QShader.FragmentShaderContext();
    private QShader shader;
    private TriangleQueue triQueue;
    private int stripStart;
    private int stripEnd;

    ////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void rasterize(List<DrawState> queuedDraws, AtomicInteger nextStrip, boolean clearPending) {
        int height     = renderTarget.getHeight();
        int stripCount = (height + STRIP_HEIGHT - 1) / STRIP_HEIGHT;
        for (int strip = nextStrip.getAndIncrement(); strip < stripCount; strip = nextStrip.getAndIncrement()) {
            stripStart = strip * STRIP_HEIGHT;
            stripEnd   = Math.min(stripStart + STRIP_HEIGHT, height);
            if (clearPending) {
                renderTarget.clearRows(stripStart, stripEnd);
            }
            for (DrawState draw : queuedDraws) {
                internalRasterize(draw);
            }
        }
    }

    ////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private void internalRasterize(DrawState draw) {
        if (Math.ceil(draw.maxY - 0.5f) <= stripStart || Math.ceil(draw.minY - 0.5f) >= stripEnd) {
            return;
        }
        shader          = draw.shader;
        triQueue        = draw.out;
        fctx.uniforms   = draw.state.slotUniforms;
        fctx.textures   = draw.state.slotTextures;
        fctx.sampleType = draw.state.sampleType;
        fctx.fillColor  = draw.state.fillColor;
        fctx.target     = renderTarget;
        fctx.layout     = draw.layout;
        fctx.drawData   = draw.data;
        if (fctx.inputs == null || fctx.inputs.length != draw.layout.total) {
            fctx.inputs = new float[draw.layout.total];
        }

        int[] tris = triQueue.tris;
        for (int t = draw.triStart; t < draw.triEnd; t += TriangleQueue.RECORD_SIZE) {
            internalDrawTri(tris[t], tris[t + 1], tris[t + 2], tris[t + 3]);
        }
    }

    private void internalDrawTri(int o0, int o1, int o2, int normal) {
        float[] v = triQueue.verts;
        int swap;

        // NOTE:
        // - a triangle must be rasterized in two parts, a line will be cut
        // through it's middle highest verticie horizontally, and each partition
        // will be draw seperately
        // - the triangle will have to be sorted by height then cut
        // - after being cut, the top two verticies will be sorted from left to right

        if (v[o2 + VCTR_INDEX_Y] > v[o1 + VCTR_INDEX_Y]) { swap = o2; o2 = o1; o1 = swap; }
        if (v[o1 + VCTR_INDEX_Y] > v[o0 + VCTR_INDEX_Y]) { swap = o1; o1 = o0; o0 = swap; }
        if (v[o2 + VCTR_INDEX_Y] > v[o1 + VCTR_INDEX_Y]) { swap = o2; o2 = o1; o1 = swap; }
        if (Math.ceil(v[o0 + VCTR_INDEX_Y] - 0.5f) <= stripStart ||
            Math.ceil(v[o2 + VCTR_INDEX_Y] - 0.5f) >= stripEnd) {
            return;
        }
        raster.set(v, o0, o1, o2, normal);

        float invSlope20 = (v[o0 + VCTR_INDEX_X] - v[o2 + VCTR_INDEX_X]) /
                (v[o0 + VCTR_INDEX_Y] - v[o2 + VCTR_INDEX_Y]);
        float dY21 = v[o1 + VCTR_INDEX_Y] - v[o2 + VCTR_INDEX_Y];
        float midX = v[o2 + VCTR_INDEX_X] + (invSlope20 * dY21);
        float flatXL = Math.min(v[o1 + VCTR_INDEX_X], midX);
        float flatXR = Math.max(v[o1 + VCTR_INDEX_X], midX);

        internalDrawFlatTopTri(
                flatXL, flatXR, v[o1 + VCTR_INDEX_Y],
                v[o2 + VCTR_INDEX_X], v[o2 + VCTR_INDEX_Y]);
        internalDrawFlatBottomTri(
                flatXL, flatXR, v[o1 + VCTR_INDEX_Y],
                v[o0 + VCTR_INDEX_X], v[o0 + VCTR_INDEX_Y]);
    }

    private void internalDrawFlatTopTri(
            float flatXL, float flatXR, float flatY,
            float apexX, float apexY) {

        // NOTE:
        // - flattop triangle is drawn from the bottom apex up to the flat edge

        float invDY = 1.0f / (flatY - apexY);
        if (Float.isNaN(invDY)) {
            return;
        }

        float invSlopeLeft  = (flatXL - apexX) * invDY;
        float invSlopeRight = (flatXR - apexX) * invDY;

        int Y_START = Math.max((int) Math.ceil(apexY - 0.5f), stripStart);
        int Y_END = Math.min((int) Math.ceil(flatY - 0.5f), stripEnd);

        for (int drawY = Y_START; drawY < Y_END; drawY++) {

            float distY = (drawY + 0.5f) - apexY;
            int X_START = Math.max(
                    (int) Math.ceil(apexX + (invSlopeLeft * distY) - 0.5f),
                    0);
            int X_END = Math.min(
                    (int) Math.ceil(apexX + (invSlopeRight * distY) - 0.5f),
                    renderTarget.getWidth());

            internalDrawSpan(X_START, X_END, drawY);

        }

    }

    private void internalDrawFlatBottomTri(
            float flatXL, float flatXR, float flatY,
            float apexX, float apexY) {

        // NOTE:
        // - flatbottom triangle is drawn from the flat edge up to the top apex

        float invDY = 1.0f / (apexY - flatY);
        if (Float.isNaN(invDY)) {
            return;
        }

        float invSlopeLeft  = (apexX - flatXL) * invDY;
        float invSlopeRight = (apexX - flatXR) * invDY;

        int Y_START = Math.max((int) Math.ceil(flatY - 0.5f), stripStart);
        int Y_END = Math.min((int) Math.ceil(apexY - 0.5f), stripEnd);

        for (int drawY = Y_START; drawY < Y_END; drawY++) {

            float distY = (drawY + 0.5f) - flatY;
            int X_START = Math.max(
                    (int) Math.ceil(flatXL + (invSlopeLeft * distY) - 0.5f),
                    0);
            int X_END = Math.min(
                    (int) Math.ceil(flatXR + (invSlopeRight * distY) - 0.5f),
                    renderTarget.getWidth());

            internalDrawSpan(X_START, X_END, drawY);

        }

    }

    private void internalDrawSpan(int xStart, int xEnd, int drawY) {
        float centerX = xStart + 0.5f;
        float centerY = drawY + 0.5f;
        float b0 = raster.b0At(centerX, centerY);
        float b1 = raster.b1At(centerX, centerY);
        for (int drawX = xStart; drawX < xEnd; drawX++) {
            internalDrawFragment(drawX, drawY, b0, b1);
            b0 += raster.db0dx;
            b1 += raster.db1dx;
        }
    }

    private void internalInterpolateFragmentInputs(
            float a0,
            float a1,
            float a2,
            float[] buffer) {
        float[] verts = raster.verts;
        int o0 = raster.out0;
        int o1 = raster.out1;
        int o2 = raster.out2;
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = a0 * verts[o0 + i] + a1 * verts[o1 + i] + a2 * verts[o2 + i];
        }
    }

    private void internalDrawFragment(
            int drawX,
            int drawY,
            float b0,
            float b1) {

        float[] depthData = renderTarget.getDepthData();
        int[]   colorData = renderTarget.getColorData();
        int     pixel     = renderTarget.coordToDataIndex(drawX, drawY);

        float w0 = raster.z0 * b0;
        float w1 = raster.z1 * b1;
        float w2 = raster.z2 * (1.0f - b0 - b1);
        float invDepth = w0 + w1 + w2;

        // NOTE:
        // since all depths are negative and inverted, the further value
        // will be a smaller negative and hence greater. therefore the failing
        // depth test will be greater than the previous depth
        if (invDepth > depthData[pixel] - DEPTH_TEST_EPSILON) {
            return;
        }

        // CALL FRAGMENT SHADER
        fctx.screenX = drawX;
        fctx.screenY = drawY;
        fctx.invDepth = invDepth;
        fctx.normal = raster.normal;
        fctx.gbufferWritten = 0;
        float invW = 1.0f / invDepth;
        internalInterpolateFragmentInputs(w0 * invW, w1 * invW, w2 * invW, fctx.inputs);

        QColor fragColor = shader.fragmentShader(fctx);

        // DISCARD IF FRAGMENT IS TRANSPARENT
        if ((fragColor.getA()) == 0) {
            return;
        }

        fctx.ensureGBufferWritten();
        internalWriteGBuffer(pixel);

        // BLEND FRAGMENT TO BELOW COLOR AND WRITE TO RENDERTARGET
        depthData[pixel] = invDepth;
        colorData[pixel] = QColor.blend(colorData[pixel], fragColor.toInt());
    }

    private void internalWriteGBuffer(int pixel) {
        for (int mask = fctx.layout.gbufferDeclared; mask != 0; mask &= mask - 1) {
            int slot = Integer.numberOfTrailingZeros(mask);
            if (fctx.layout.gbufferFormats[slot] == QRenderBuffer.GBufferFormat.Float3) {
                System.arraycopy(fctx.gbufferFloats, slot * 3, renderTarget.getGBufferFloatData(slot), pixel * 3, 3);
            } else {
                renderTarget.getGBufferLongData(slot)[pixel] = fctx.gbufferLongs[slot];
            }
        }
    }

    ////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public RasterWorker(QRenderBuffer renderTarget) {
        this.renderTarget = renderTarget;
    }
}
