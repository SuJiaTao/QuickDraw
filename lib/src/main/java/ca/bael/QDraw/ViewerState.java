// Bailey JT Brown
// 2023-2026
// ViewerState.java

package ca.bael.QDraw;

import ca.bael.QDraw.QSampleable.SampleType;
import ca.bael.QDraw.QViewer.BlendMode;
import ca.bael.QDraw.QViewer.CullMode;

final class ViewerState {
    ////////////////////////////////////////////////////////////////
    // PUBLIC MEMBERS
    public QProjection projection = QViewer.DEFAULT_PROJECTION;
    public QColor     fillColor  = QViewer.DEFAULT_FILL_COLOR.snapshot();
    public SampleType sampleType = QViewer.DEFAULT_SAMPLE_TYPE;
    public boolean    depthTest  = QViewer.DEFAULT_DEPTH_TEST;
    public boolean    depthWrite = QViewer.DEFAULT_DEPTH_WRITE;
    public boolean    colorWrite = QViewer.DEFAULT_COLOR_WRITE;
    public CullMode   cullMode   = QViewer.DEFAULT_CULL_MODE;
    public BlendMode  blendMode  = QViewer.DEFAULT_BLEND_MODE;

    public final QUniform<?>[]    slotUniforms = new QUniform<?>[QViewer.SHADER_UNIFORM_SLOTS];
    public final QSampleable[]    slotTextures = new QSampleable[QViewer.SHADER_TEXTURE_SLOTS];
    public final QAttribBuffer[]  slotAttribs  = new QAttribBuffer[QViewer.SHADER_VERTEX_ATTRIB_SLOTS];
    public QIndexBuffer           indexBuffer;

    ////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public ViewerState snapshot() {
        ViewerState copy = new ViewerState();
        copy.projection = projection;
        copy.fillColor  = fillColor.snapshot();
        copy.sampleType = sampleType;
        copy.depthTest  = depthTest;
        copy.depthWrite = depthWrite;
        copy.colorWrite = colorWrite;
        copy.cullMode   = cullMode;
        copy.blendMode  = blendMode;
        copy.indexBuffer = indexBuffer;
        System.arraycopy(slotTextures, 0, copy.slotTextures, 0, slotTextures.length);
        System.arraycopy(slotAttribs, 0, copy.slotAttribs, 0, slotAttribs.length);
        for (int i = 0; i < slotUniforms.length; i++) {
            copy.slotUniforms[i] = slotUniforms[i] == null ? null : slotUniforms[i].snapshot();
        }
        return copy;
    }
}
