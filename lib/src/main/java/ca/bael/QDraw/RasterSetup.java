// Bailey JT Brown
// 2023-2024
// RasterSetup.java

package ca.bael.QDraw;

import static ca.bael.QDraw.QVector3.VCTR_INDEX_X;
import static ca.bael.QDraw.QVector3.VCTR_INDEX_Y;
import static ca.bael.QDraw.QVector3.VCTR_INDEX_Z;
import static ca.bael.QDraw.QVector3.VCTR_NUM_CMPS;

final class RasterSetup {
    ////////////////////////////////////////////////////////////////
    // PUBLIC MEMBERS
    public float[] verts;
    public int out0, out1, out2;
    public final float[] normal = new float[VCTR_NUM_CMPS];
    public float z0, z1, z2;
    public float p2x, p2y;
    public float db0dx, db0dy, db1dx, db1dy;

    ////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void set(float[] v, int o0, int o1, int o2, int n) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csmint_pl_rasterizetri.c
        float p0x = v[o0 + VCTR_INDEX_X];
        float p0y = v[o0 + VCTR_INDEX_Y];
        float p1x = v[o1 + VCTR_INDEX_X];
        float p1y = v[o1 + VCTR_INDEX_Y];
        p2x = v[o2 + VCTR_INDEX_X];
        p2y = v[o2 + VCTR_INDEX_Y];
        float invDenom = 1.0f / ((p1y - p2y) * (p0x - p2x) + (p2x - p1x) * (p0y - p2y));
        db0dx = (p1y - p2y) * invDenom;
        db0dy = (p2x - p1x) * invDenom;
        db1dx = (p2y - p0y) * invDenom;
        db1dy = (p0x - p2x) * invDenom;
        z0 = v[o0 + VCTR_INDEX_Z];
        z1 = v[o1 + VCTR_INDEX_Z];
        z2 = v[o2 + VCTR_INDEX_Z];
        verts = v;
        out0 = o0 + VCTR_NUM_CMPS;
        out1 = o1 + VCTR_NUM_CMPS;
        out2 = o2 + VCTR_NUM_CMPS;
        System.arraycopy(v, n, normal, 0, VCTR_NUM_CMPS);
    }

    public float b0At(float x, float y) {
        return db0dx * (x - p2x) + db0dy * (y - p2y);
    }

    public float b1At(float x, float y) {
        return db1dx * (x - p2x) + db1dy * (y - p2y);
    }
}
