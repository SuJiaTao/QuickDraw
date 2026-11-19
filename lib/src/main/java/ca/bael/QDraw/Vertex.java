// Bailey JT Brown
// 2023-2024
// Vertex.java

package ca.bael.QDraw;

import static ca.bael.QDraw.QVector3.VCTR_NUM_CMPS;

final class Vertex {
    ////////////////////////////////////////////////////////////////
    // PUBLIC MEMBERS
    public float[] posn;
    public float[] shaderOutputs;

    ////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public void findClippedShaderOutputs(
            Vertex vISrc,
            Vertex vFSrc,
            float factor) {
        // NOTE:
        // - vISrc and vFSrc should have identically formatted shader outputs,
        // that is, the slots are consistent in usage and component count,
        // so when initializing the new shader outputs, we arbitrarily use vISrc
        // as a reference
        // - vISrc or vFSrc might actually be THIS vertex, so we calculate everything
        // to an auxillary buffer before updating shaderOutputs
        float[] tempShaderOutputs = new float[vISrc.shaderOutputs.length];
        float facI = 1.0f - factor;
        float facF = factor;

        for (int i = 0; i < tempShaderOutputs.length; i++) {
            tempShaderOutputs[i] = vISrc.shaderOutputs[i] * facI +
                    vFSrc.shaderOutputs[i] * facF;
        }

        shaderOutputs = tempShaderOutputs;
    }

    ////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public Vertex() {
    }

    public Vertex(Vertex toCopy) {
        posn = new float[VCTR_NUM_CMPS];
        QVector3.copy(
                posn,
                toCopy.posn);
        shaderOutputs = toCopy.shaderOutputs.clone( );
    }
}
