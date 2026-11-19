// Bailey JT Brown
// 2023-2024
// TriangleClipper.java

package ca.bael.QDraw;

import java.util.Arrays;

import ca.bael.QDraw.QException.PointOfError;

final class TriangleClipper extends QEncoding {
    ////////////////////////////////////////////////////////////////
    // PRIVATE CLASSES
    private static class ClipState {
        ////////////////////////////////////////////////////////////////
        // PUBLIC MEMBERS
        public int numVertsBehind = 0;
        public boolean[] vertBehindState = new boolean[Triangle.VERTS_PER_TRI];
        public int[] vertBehindIndicies = new int[Triangle.VERTS_PER_TRI];

        ////////////////////////////////////////////////////////////////
        // PUBLIC METHODS
        public String toString() {
            return String.format(
                    "<numVBehind: %d, vStates: %s vIndicies: %s>",
                    numVertsBehind,
                    Arrays.toString(vertBehindState),
                    Arrays.toString(vertBehindIndicies));
        }
    }

    ////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public static Triangle[] clip(Triangle tri, float nearClip) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c

        ClipState clipState = new ClipState();

        if (tri.getPosnZ(0) > nearClip) {
            clipState.vertBehindIndicies[clipState.numVertsBehind++] = 0;
            clipState.vertBehindState[0] = true;
        }

        if (tri.getPosnZ(1) > nearClip) {
            clipState.vertBehindIndicies[clipState.numVertsBehind++] = 1;
            clipState.vertBehindState[1] = true;
        }

        if (tri.getPosnZ(2) > nearClip) {
            clipState.vertBehindIndicies[clipState.numVertsBehind++] = 2;
            clipState.vertBehindState[2] = true;
        }

        switch (clipState.numVertsBehind) {
            // ALL VERTS BEFORE
            // triangle is not clipped
            case 0:
                return new Triangle[] { tri };

            // ALL VERTS BEHIND
            // triangle should be culled
            case 3:
                return new Triangle[0];

            // 2 VERTS BEHIND
            // triangle is turned into smaller triangle
            case 2:
                return internalClipTriCase2(tri, clipState, nearClip);

            // 1 VERTS BEHIND
            // triangle is clipped into 2 smaller triangles
            case 1:
                return internalClipTriCase1(tri, clipState, nearClip);

            // BAD STATE
            default:
                throw new QException(
                        PointOfError.BadState,
                        "reached bad clipping state: " + clipState.toString());
        }
    }

    ////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private static void internalFindClipIntersect(
            Triangle srcTri,
            int pI,
            int pF,
            float[] out,
            float nearClip) {
        internalFindClipIntersect(
                srcTri.getPosn(pI),
                srcTri.getPosn(pF),
                out,
                nearClip);
    }

    private static void internalFindClipIntersect(
            float[] pI,
            float[] pF,
            float[] out,
            float nearClip) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c

        float invDZ = 1.0f / (pF[VCTR_INDEX_Z] - pI[VCTR_INDEX_Z]);
        float slopeXZ = (pF[VCTR_INDEX_X] - pI[VCTR_INDEX_X]) * invDZ;
        float slopeYZ = (pF[VCTR_INDEX_Y] - pI[VCTR_INDEX_Y]) * invDZ;
        float dClip = (nearClip - pI[VCTR_INDEX_Z]);

        out[VCTR_INDEX_X] = pI[VCTR_INDEX_X] + slopeXZ * dClip;
        out[VCTR_INDEX_Y] = pI[VCTR_INDEX_Y] + slopeYZ * dClip;
        out[VCTR_INDEX_Z] = nearClip;
    }

    private static float internalFindClipInterpolationFactor(
            float[] pI,
            float[] pF,
            float[] pClip) {
        // NOTE:
        // takes a starting, ending, and middle position, generates
        // interpolation factor between pI to pF (0 for pI, 1 for pF)
        return (pClip[VCTR_INDEX_Z] - pI[VCTR_INDEX_Z]) /
               (pF[VCTR_INDEX_Z] - pI[VCTR_INDEX_Z]);
    }

    private static Triangle[] internalClipTriCase1(Triangle tri, ClipState clipState, float nearClip) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c

        // NOTE:
        // - all faces must be constructed in CLOCKWISE winding order.
        // - when 1 vertex is clipped, generated face is a quad
        // - triangle will be re-shuffled so that it remains CLOCKWISE where pos2 is
        // clipped
        // - (this is slightly different from Casesium where pos0 is clipped)

        Triangle shuffledTri = new Triangle(tri);

        // SHUFFLE TRIANGLE
        switch (clipState.vertBehindIndicies[0]) {
            // SHUFFLE (0, 1, 2) -> (1, 2, 0)
            case 0:

                shuffledTri.swapVerts(0, 2); // (0 1 2) -> (2 1 0)
                shuffledTri.swapVerts(0, 1); // (2 1 0) -> (1 2 0)
                break;

            // SHUFFLE (0, 1, 2) -> (2, 0, 1)
            case 1:

                shuffledTri.swapVerts(0, 2); // (0 1 2) -> (2 1 0)
                shuffledTri.swapVerts(1, 2); // (2 1 0) -> (2 0 1)
                break;

            // SHUFFLE (0, 1, 2) -> (0, 1, 2)
            // or rather, no shuffle
            case 2:

                break;

            default:
                throw new QException(
                        PointOfError.BadState,
                        "bad clip state: " + clipState.toString());
        }

        float[] pos02 = new float[VCTR_NUM_CMPS];
        float[] pos12 = new float[VCTR_NUM_CMPS];
        internalFindClipIntersect(shuffledTri, 0, 2, pos02, nearClip);
        internalFindClipIntersect(shuffledTri, 1, 2, pos12, nearClip);

        float fac02 = internalFindClipInterpolationFactor(
                shuffledTri.getPosn(0),
                shuffledTri.getPosn(2),
                pos02);
        float fac12 = internalFindClipInterpolationFactor(
                shuffledTri.getPosn(1),
                shuffledTri.getPosn(2),
                pos12);

        // NOTE:
        // - when 1 vertex is clipped, the resulting mesh is a quad
        // - our quad is 0/2, 0, 1, 1/2 (where a/b is clipped interpolation),
        // which will be tesselated as (0/2, 0, 1), (0/2, 1, 1/2)

        Triangle quadTri0 = new Triangle(shuffledTri);
        quadTri0.getVertex(2).findClippedShaderOutputs(
                shuffledTri.getVertex(0),
                shuffledTri.getVertex(2),
                fac02);
        quadTri0.setPosn(2, pos02); // (0 1 2) -> (0 1 0/2)
        quadTri0.swapVerts(0, 2); // (0 1 0/2) -> (0/2 1 0)
        quadTri0.swapVerts(1, 2); // (0/2 1 0) -> (0/2 0 1)

        Triangle quadTri1 = new Triangle(shuffledTri); // (0 1 2)
        quadTri1.getVertex(0).findClippedShaderOutputs(
                shuffledTri.getVertex(0),
                shuffledTri.getVertex(2),
                fac02);
        quadTri1.setPosn(0, pos02); // (0 1 2) -> (0/2 1 2)
        quadTri1.getVertex(2).findClippedShaderOutputs(
                shuffledTri.getVertex(1),
                shuffledTri.getVertex(2),
                fac12);
        quadTri1.setPosn(2, pos12); // (0/2 1 2) -> (0/2 1 1/2)

        return new Triangle[] { quadTri0, quadTri1 };

    }

    private static Triangle[] internalClipTriCase2(Triangle tri, ClipState clipState, float nearClip) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_cliptri.c

        // TESSELATE TRIANGLE BASED ON 2 CLIPPED VERTICIES
        switch (clipState.vertBehindIndicies[0] + clipState.vertBehindIndicies[1]) {
            // VERTS 1 & 2 ARE CLIPPED
            case (1 + 2):

                float[] pos10 = new float[VCTR_NUM_CMPS];
                float[] pos20 = new float[VCTR_NUM_CMPS];
                internalFindClipIntersect(tri, 1, 0, pos10, nearClip);
                internalFindClipIntersect(tri, 2, 0, pos20, nearClip);
                float fac10 = internalFindClipInterpolationFactor(
                        tri.getPosn(1),
                        tri.getPosn(0),
                        pos10);
                float fac20 = internalFindClipInterpolationFactor(
                        tri.getPosn(2),
                        tri.getPosn(0),
                        pos20);

                tri.getVertex(1).findClippedShaderOutputs(
                        tri.getVertex(1),
                        tri.getVertex(0),
                        fac10);
                tri.setPosn(1, pos10);

                tri.getVertex(2).findClippedShaderOutputs(
                        tri.getVertex(2),
                        tri.getVertex(0),
                        fac20);
                tri.setPosn(2, pos20);

                return new Triangle[] { tri };

            // VERTS 0 & 2 ARE CLIPPED
            case (0 + 2):

                float[] pos01 = new float[VCTR_NUM_CMPS];
                float[] pos21 = new float[VCTR_NUM_CMPS];
                internalFindClipIntersect(tri, 0, 1, pos01, nearClip);
                internalFindClipIntersect(tri, 2, 1, pos21, nearClip);
                float fac01 = internalFindClipInterpolationFactor(
                        tri.getPosn(0),
                        tri.getPosn(1),
                        pos01);
                float fac21 = internalFindClipInterpolationFactor(
                        tri.getPosn(2),
                        tri.getPosn(1),
                        pos21);

                tri.getVertex(0).findClippedShaderOutputs(
                        tri.getVertex(0),
                        tri.getVertex(1),
                        fac01);
                tri.setPosn(0, pos01);

                tri.getVertex(2).findClippedShaderOutputs(
                        tri.getVertex(2),
                        tri.getVertex(1),
                        fac21);
                tri.setPosn(2, pos21);

                return new Triangle[] { tri };

            // VERTS 0 & 1 ARE CLIPPED
            case (0 + 1):

                float[] pos02 = new float[VCTR_NUM_CMPS];
                float[] pos12 = new float[VCTR_NUM_CMPS];
                internalFindClipIntersect(tri, 0, 2, pos02, nearClip);
                internalFindClipIntersect(tri, 1, 2, pos12, nearClip);
                float fac02 = internalFindClipInterpolationFactor(
                        tri.getPosn(0),
                        tri.getPosn(2),
                        pos02);
                float fac12 = internalFindClipInterpolationFactor(
                        tri.getPosn(1),
                        tri.getPosn(2),
                        pos12);

                tri.getVertex(0).findClippedShaderOutputs(
                        tri.getVertex(0),
                        tri.getVertex(2),
                        fac02);
                tri.setPosn(0, pos02);

                tri.getVertex(1).findClippedShaderOutputs(
                        tri.getVertex(1),
                        tri.getVertex(2),
                        fac12);
                tri.setPosn(1, pos12);

                return new Triangle[] { tri };

            // BAD STATE
            default:
                throw new QException(
                        PointOfError.BadState,
                        "Bad clip state:" + clipState.toString());
        }

    }
}
