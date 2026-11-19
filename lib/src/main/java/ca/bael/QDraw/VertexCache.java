// Bailey JT Brown
// 2023-2024
// VertexCache.java

package ca.bael.QDraw;

final class VertexCache extends QEncoding {
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
    public void prepare(QAttribIndexer[] slots, int outputTotal) {
        drawId++;
        total = outputTotal;
        indicies = null;
        int size = 0;
        for (QAttribIndexer indexer : slots) {
            if (indexer == null) {
                continue;
            }
            if (indicies == null) {
                indicies = indexer.getIndicies();
                size = indexer.getAttribCount();
            } else if (indexer.getIndicies() != indicies) {
                indicies = null;
                return;
            }
        }
        if (stamp.length < size) {
            stamp = new int[size];
            emitted = new int[size];
            posn = new float[size * VCTR_NUM_CMPS];
        }
        if (outputs.length < size * total) {
            outputs = new float[size * total];
        }
    }

    public int indexOf(int triNum, int triVertNum) {
        return indicies == null ? -1 : indicies[triNum * Triangle.VERTS_PER_TRI + triVertNum];
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
