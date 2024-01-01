// Bailey JT Brown
// 2024
// QMath.java

package QDraw;

import java.util.Arrays;

public final class QMath extends QEncoding {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final float MATH_TO_RADIANS = (float)Math.PI / 180.0f;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public static float[] new3( ) {
        return new float[] { 0.0f, 0.0f, 0.0f };
    }

    public static float[] new4x4( ) {
        return new float[MTR_NUM_CMPS];
    }

    public static float[] clone3(float[] v_3) {
        return clone3(0, v_3);
    }

    public static float[] clone3(int offset, float[] v_3) {
        float[] rvec = new float[3];
        System.arraycopy(v_3, offset, rvec, 0, 3);
        return rvec;    
    }

    public static void copy3(float[] trg, float[] src) {
        copy3(0, trg, 0, src);
    }

    public static void copy3(
        int     trgoffset,
        float[] trg,
        int     srcoffset,
        float[] src
    ) {
        System.arraycopy(src, srcoffset, trg, trgoffset, VCTR_NUM_CMPS);
    }

    public static void copy4x4(float[] trg_4x4, float[] src_4x4) {
        copy4x4(0, trg_4x4, 0, src_4x4);
    }

    public static void copy4x4(
        int     trgoffset,
        float[] trg_4x4,
        int     srcoffset,
        float[] src_4x4
    ) {
        System.arraycopy(src_4x4, srcoffset, trg_4x4, trgoffset, MTR_NUM_CMPS);
    }

    public static void add3(float[] v1_3, float[] v2_3) {
        QMath.add3(0, v1_3, 0, v2_3);
    }

    public static void add3(
        int     v1offset,
        float[] v1_3,
        int     v2offset,
        float[] v2_3
    ) {
        v1_3[v1offset + VCTR_INDEX_X] += v2_3[v2offset + VCTR_INDEX_X];
        v1_3[v1offset + VCTR_INDEX_Y] += v2_3[v2offset + VCTR_INDEX_Y];
        v1_3[v1offset + VCTR_INDEX_Z] += v2_3[v2offset + VCTR_INDEX_Z];
    }

    public static void mult3(float[] v_3, float fac) {
        mult3(0, v_3, fac);
    }

    public static void mult3(
        int voffset,
        float[] v1_3,
        float fac
    ) {
        v1_3[voffset + VCTR_INDEX_X] *= fac;
        v1_3[voffset + VCTR_INDEX_Y] *= fac;
        v1_3[voffset + VCTR_INDEX_Z] *= fac;
    }

    public static float sqrmag3(float[] v_3) {
        return sqrmag3(0, v_3);
    }

    public static float sqrmag3(
        int     offset,
        float[] v_3
    ) {
        return v_3[offset + VCTR_INDEX_X] * v_3[offset + VCTR_INDEX_X] + 
               v_3[offset + VCTR_INDEX_Y] * v_3[offset + VCTR_INDEX_Y] +
               v_3[offset + VCTR_INDEX_Z] * v_3[offset + VCTR_INDEX_Z];
    }

    public static float mag3(float[] v_3) {
        return mag3(0, v_3);
    }

    public static float mag3(
        int offset,
        float[] v_3
    ) {
        return (float)Math.sqrt(sqrmag3(offset, v_3));
    }

    public static int index4x4(int col, int row) {
        return col + (MTR_NUM_ROWS * row);
    }

    public static float get4x4(float[] m4x4, int col, int row) {
        return get4x4(0, m4x4, col, row);
    }

    public static float get4x4(
        int     moffset,
        float[] m4x4,
        int     col,
        int     row
    ) {
        return m4x4[moffset + index4x4(col, row)];
    }

    public static void set4x4(
        int     moffset,
        float[] m4x4,
        int     col,
        int     row,
        float   val
    ) {
        m4x4[moffset + index4x4(col, row)] = val;
    }

    public static void mul3_4x4(float[] v_3, float[] m4x4) {
        mul3_4x4(0, v_3, 0, m4x4);
    }

    public static void mul3_4x4(
        int voffset,
        float[] v_3,
        int moffset,
        float[] m4x4
    ) {
        float[] v_4 = new float[] {
            v_3[VCTR_INDEX_X],
            v_3[VCTR_INDEX_Y],
            v_3[VCTR_INDEX_Z],
            1.0f
        };

        float[] ret_4 = new float[4];

        for (int vCompIndex = 0; vCompIndex < v_4.length; vCompIndex++) {
            for (int columnIndex = 0; columnIndex < MTR_NUM_COLUMNS; columnIndex++) {
                ret_4[vCompIndex] +=
                    v_4[voffset + columnIndex] * 
                    get4x4(moffset, m4x4, columnIndex, vCompIndex);
            }
        }

        copy3(v_3, ret_4);
    }

    public static float[] mul4x4(float[] m1_4x4, float[] m2_4x4) {
        return mul4x4(0, m1_4x4, 0, m2_4x4);
    }

    public static float[] mul4x4(
        int m1offset,
        float[] m1_4x4,
        int m2offset,
        float[] m2_4x4
    ) {
        // refer to
        // https://github.com/SuJiaTao/Caesium/blob/master/csm_matrix.c
        float[] r4x4 = new4x4( );
        for (int i = 0; i < MTR_NUM_ROWS; i++) {
            for (int j = 0; j < MTR_NUM_COLUMNS; j++) {
                for (int k = 0; k < MTR_NUM_ROWS; k++) {
                    float val = 
                        get4x4(m1offset, m1_4x4, i, k) + 
                        get4x4(m2offset, m2_4x4, k, j);
                    set4x4(0, r4x4, i, j, val);
                }
            }
        }
        return r4x4;
    }

    public static float cosf(float degrees) {
        return (float)Math.cos(degrees * MATH_TO_RADIANS);
    }

    public static float sinf(float degrees) {
        return (float)Math.sin(degrees * MATH_TO_RADIANS);
    }
}
