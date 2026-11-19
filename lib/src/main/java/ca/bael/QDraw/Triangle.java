// Bailey JT Brown
// 2023-2024
// Triangle.java

package ca.bael.QDraw;

import static ca.bael.QDraw.QVector3.VCTR_INDEX_X;
import static ca.bael.QDraw.QVector3.VCTR_INDEX_Y;
import static ca.bael.QDraw.QVector3.VCTR_INDEX_Z;
import static ca.bael.QDraw.QVector3.VCTR_NUM_CMPS;

final class Triangle {
    ////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final int VERTS_PER_TRI = 3;

    ////////////////////////////////////////////////////////////////
    // PUBLIC MEMBERS
    public int triNum;
    public Vertex[] verts;
    public float[] normal;

    ////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void swapVerts(int v0, int v1) {
        Vertex temp = verts[v0];
        verts[v0] = verts[v1];
        verts[v1] = temp;
    }

    public float[] getPosn(int vertNum) {
        return verts[vertNum].posn;
    }

    public void setPosn(int vertNum, float[] inVals) {
        QVector3.copy(verts[vertNum].posn, inVals);
    }

    public float getPosnX(int vertNum) {
        return getPosn(vertNum)[VCTR_INDEX_X];
    }

    public void setPosnX(int vertNum, float val) {
        getPosn(vertNum)[VCTR_INDEX_X] = val;
    }

    public float getPosnY(int vertNum) {
        return getPosn(vertNum)[VCTR_INDEX_Y];
    }

    public void setPosnY(int vertNum, float val) {
        getPosn(vertNum)[VCTR_INDEX_Y] = val;
    }

    public float getPosnZ(int vertNum) {
        return getPosn(vertNum)[VCTR_INDEX_Z];
    }

    public Vertex getVertex(int vertNum) {
        return verts[vertNum];
    }

    ////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public Triangle(int _num) {
        triNum = _num;
        verts = new Vertex[] { new Vertex(), new Vertex(), new Vertex() };
        normal = new float[VCTR_NUM_CMPS];
    }

    public Triangle(Triangle toCopy) {
        triNum = toCopy.triNum;
        normal = new float[VCTR_NUM_CMPS];
        QVector3.copy(normal, toCopy.normal);

        verts = new Vertex[VERTS_PER_TRI];
        for (int i = 0; i < verts.length; i++) {
            verts[i] = new Vertex(toCopy.verts[i]);
        }
    }

}
