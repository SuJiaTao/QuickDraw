// Bailey JT Brown
// 2023-2024
// ViewerState.java

package ca.bael.QDraw;

import ca.bael.QDraw.QSampleable.SampleType;

final class ViewerState {
    ////////////////////////////////////////////////////////////////
    // PUBLIC MEMBERS
    public float      nearClip   = QViewer.DEFAULT_NEAR_CLIP;
    public float      viewLeft   = QViewer.DEFAULT_VIEWBOUND_LEFT;
    public float      viewRight  = QViewer.DEFAULT_VIEWBOUND_RIGHT;
    public float      viewBottom = QViewer.DEFAULT_VIEWBOUND_BOTTOM;
    public float      viewTop    = QViewer.DEFAULT_VIEWBOUND_TOP;
    public QColor     fillColor  = QViewer.DEFAULT_FILL_COLOR.snapshot();
    public SampleType sampleType = QViewer.DEFAULT_SAMPLE_TYPE;
    public boolean    depthTest  = QViewer.DEFAULT_DEPTH_TEST;
    public boolean    depthWrite = QViewer.DEFAULT_DEPTH_WRITE;

    public final QUniform<?>[]    slotUniforms = new QUniform<?>[QViewer.SHADER_UNIFORM_SLOTS];
    public final QSampleable[]    slotTextures = new QSampleable[QViewer.SHADER_TEXTURE_SLOTS];
    public final QAttribBuffer[]  slotAttribs  = new QAttribBuffer[QViewer.SHADER_VERTEX_ATTRIB_SLOTS];
    public QIndexBuffer           indexBuffer;

    ////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public ViewerState snapshot() {
        ViewerState copy = new ViewerState();
        copy.nearClip   = nearClip;
        copy.viewLeft   = viewLeft;
        copy.viewRight  = viewRight;
        copy.viewBottom = viewBottom;
        copy.viewTop    = viewTop;
        copy.fillColor  = fillColor.snapshot();
        copy.sampleType = sampleType;
        copy.depthTest  = depthTest;
        copy.depthWrite = depthWrite;
        copy.indexBuffer = indexBuffer;
        System.arraycopy(slotTextures, 0, copy.slotTextures, 0, slotTextures.length);
        System.arraycopy(slotAttribs, 0, copy.slotAttribs, 0, slotAttribs.length);
        for (int i = 0; i < slotUniforms.length; i++) {
            copy.slotUniforms[i] = slotUniforms[i] == null ? null : slotUniforms[i].snapshot();
        }
        return copy;
    }
}
