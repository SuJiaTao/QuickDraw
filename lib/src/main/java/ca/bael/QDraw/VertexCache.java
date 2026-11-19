// Bailey JT Brown
// 2023-2024
// VertexCache.java

package ca.bael.QDraw;

import static ca.bael.QDraw.QVector3.VCTR_NUM_CMPS;

final class VertexCache {
    ////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private int[]   indicies;
    private int[]   stamp   = new int[0];
    private int[]   emitted = new int[0];
    private float[] posn    = new float[0];
    private float[] outputs = new float[0];
    private int     drawId  = 0;
    private int     total;

    ////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void prepare(int[] indicies, int vertCount, int outputTotal) {
        drawId++;
        total = outputTotal;
        this.indicies = indicies;
        if (stamp.length < vertCount) {
            stamp = new int[vertCount];
            emitted = new int[vertCount];
            posn = new float[vertCount * VCTR_NUM_CMPS];
        }
        if (outputs.length < vertCount * total) {
            outputs = new float[vertCount * total];
        }
    }

    public int indexOf(int triNum, int triVertNum) {
        return indicies[triNum * Triangle.VERTS_PER_TRI + triVertNum];
    }

    public boolean load(int idx, Vertex vert) {
        if (idx < 0 || stamp[idx] != drawId) {
            return false;
        }
        System.arraycopy(posn, idx * VCTR_NUM_CMPS, vert.posn, 0, VCTR_NUM_CMPS);
        System.arraycopy(outputs, idx * total, vert.shaderOutputs, 0, total);
        return true;
    }

    public void store(int idx, Vertex vert) {
        if (idx < 0) {
            return;
        }
        stamp[idx] = drawId;
        emitted[idx] = -1;
        System.arraycopy(vert.posn, 0, posn, idx * VCTR_NUM_CMPS, VCTR_NUM_CMPS);
        System.arraycopy(vert.shaderOutputs, 0, outputs, idx * total, total);
    }

    public int emitted(int idx) {
        return idx < 0 || stamp[idx] != drawId ? -1 : emitted[idx];
    }

    public void setEmitted(int idx, int offset) {
        if (idx >= 0) {
            emitted[idx] = offset;
        }
    }
}
