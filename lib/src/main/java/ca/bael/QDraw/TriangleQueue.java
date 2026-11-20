// Bailey JT Brown
// 2023-2026
// TriangleQueue.java

package ca.bael.QDraw;

import java.util.Arrays;

final class TriangleQueue {
    ////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final int RECORD_SIZE = 4;

    ////////////////////////////////////////////////////////////////
    // PUBLIC MEMBERS
    public float[] verts = new float[0];
    public int[]   tris  = new int[0];
    public int     vertEnd;
    public int     triEnd;

    ////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void reset() {
        vertEnd = 0;
        triEnd = 0;
    }

    public int allocVerts(int count) {
        if (vertEnd + count > verts.length) {
            verts = Arrays.copyOf(verts, Math.max(verts.length * 2, vertEnd + count));
        }
        int offset = vertEnd;
        vertEnd += count;
        return offset;
    }

    public void addTri(int v0, int v1, int v2, int normal) {
        if (triEnd + RECORD_SIZE > tris.length) {
            tris = Arrays.copyOf(tris, Math.max(tris.length * 2, RECORD_SIZE * 1024));
        }
        tris[triEnd++] = v0;
        tris[triEnd++] = v1;
        tris[triEnd++] = v2;
        tris[triEnd++] = normal;
    }
}
