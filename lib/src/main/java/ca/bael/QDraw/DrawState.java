// Bailey JT Brown
// 2023-2024
// DrawState.java

package ca.bael.QDraw;

final class DrawState {
    ////////////////////////////////////////////////////////////////
    // PUBLIC MEMBERS
    public final ViewerState          state;
    public final QShader              shader;
    public final QShader.OutputLayout layout;
    public int                        triStart;
    public int                        triEnd;
    public float                      minY = Float.POSITIVE_INFINITY;
    public float                      maxY = Float.NEGATIVE_INFINITY;

    ////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    public DrawState(ViewerState state, QShader shader, QShader.OutputLayout layout) {
        this.state  = state;
        this.shader = shader;
        this.layout = layout;
    }
}
