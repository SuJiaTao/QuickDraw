// Bailey JT Brown
// 2026
// QProjection.java

package ca.bael.QDraw;

import static ca.bael.QDraw.QVector3.VCTR_INDEX_X;
import static ca.bael.QDraw.QVector3.VCTR_INDEX_Y;
import static ca.bael.QDraw.QVector3.VCTR_INDEX_Z;

import ca.bael.QDraw.QException.PointOfError;

public final class QProjection implements QUniform<QProjection> {
    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private final float left;
    private final float right;
    private final float bottom;
    private final float top;
    private final float near;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public static QProjection perspective(float fovYDegrees, float aspect, float near) {
        float t = (float)Math.tan(Math.toRadians(0.5f * fovYDegrees));
        return new QProjection(-aspect * t, aspect * t, -t, t, near);
    }

    public static QProjection perspective(float fovYDegrees, QRenderBuffer target, float near) {
        return perspective(fovYDegrees, (float)target.getWidth( ) / target.getHeight( ), near);
    }

    public static QProjection frustum(float left, float right, float bottom, float top, float near) {
        return new QProjection(left, right, bottom, top, near);
    }

    public float left( ) {
        return left;
    }

    public float right( ) {
        return right;
    }

    public float bottom( ) {
        return bottom;
    }

    public float top( ) {
        return top;
    }

    public float near( ) {
        return near;
    }

    public QProjection snapshot( ) {
        return this;
    }

    /////////////////////////////////////////////////////////////////
    // PACKAGE METHODS
    void toScreen(float[] viewPos, int width, int height, float[] out, int outOffset) {
        float invZ = 1.0f / viewPos[VCTR_INDEX_Z];
        out[outOffset + VCTR_INDEX_X] = (viewPos[VCTR_INDEX_X] * -invZ - left) * (width / (right - left));
        out[outOffset + VCTR_INDEX_Y] = (viewPos[VCTR_INDEX_Y] * -invZ - bottom) * (height / (top - bottom));
        out[outOffset + VCTR_INDEX_Z] = invZ;
    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    private QProjection(float left, float right, float bottom, float top, float near) {
        if (!(near > 0.0f)) {
            throw new QException(
                PointOfError.InvalidParameter,
                "near must be > 0. Given was " + near
            );
        }
        if (!(right > left) || !(top > bottom)) {
            throw new QException(
                PointOfError.InvalidParameter,
                "frustum must have right > left and top > bottom"
            );
        }
        this.left   = left;
        this.right  = right;
        this.bottom = bottom;
        this.top    = top;
        this.near   = near;
    }
}
