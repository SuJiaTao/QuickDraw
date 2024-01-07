// Bailey JT Brown
// 2024
// FuzzShader.java

import QDraw.*;

public class FuzzShader extends QShader {
    public static final int SHADER_POSITION_SLOT = QViewer.DEFAULT_SHADER_POSITION_SLOT;
    public static final int SHADER_UV_SLOT       = QViewer.DEFAULT_SHADER_UV_SLOT;
    public static final int SHADER_NORMAL_SLOT   = QViewer.DEFAULT_SHADER_NORMAL_SLOT;
    public static final int SHADER_MATRIX_SLOT   = QViewer.DEFAULT_SHADER_MATRIX_SLOT;
    public static final int SHADER_TEXTURE_SLOT  = QViewer.DEFAULT_SHADER_TEXTURE_SLOT;

    public QVector3 vertexShader(
        VertexShaderContext vctx
    ) {
        // TRANSFORM VERTEX AND NORMALS
        QVector3   vertexPos = new QVector3(vctx.attributes[SHADER_POSITION_SLOT]);
        QMatrix4x4 transform = (QMatrix4x4)vctx.uniforms[SHADER_MATRIX_SLOT];
        QMatrix4x4 rotMtr = transform.extractRotation( );
        QMath.mul3_4x4(
            vctx.attributes[SHADER_NORMAL_SLOT], 
            rotMtr.getComponents( )
        );
        
        forwardAttributeToFragShader(vctx, SHADER_POSITION_SLOT);
        forwardAttributeToFragShader(vctx, SHADER_UV_SLOT);
        forwardAttributeToFragShader(vctx, SHADER_NORMAL_SLOT);

        return QMatrix4x4.multiply(transform, vertexPos);
    }

    public QColor fragmentShader(
        FragmentShaderContext fragInfo
    ) {
        // RETRIEVE FRAG INFO
        float[] pos    = new float[3];
        float[] uv     = new float[2];
        float[] normal = new float[3];
        getOutputFromVertShader(fragInfo, SHADER_POSITION_SLOT, pos);
        getOutputFromVertShader(fragInfo, SHADER_UV_SLOT, uv);
        getOutputFromVertShader(fragInfo, SHADER_NORMAL_SLOT, normal);
        QSampleable tex = fragInfo.textures[SHADER_TEXTURE_SLOT];
        
        // WIGGLE UVS
        final int randSeedU = (((fragInfo.screenX >> 1) + (fragInfo.screenY >> 1) * fragInfo.target.getWidth( )) << 1);
        final int randSeedV = randSeedU + 1;
        final float wigglemag     = 0.004f;
        final float halfwigglemag = wigglemag * 0.5f;
        uv[0] += (seededRandom(randSeedU) * wigglemag) - halfwigglemag;
        uv[1] += (seededRandom(randSeedV) * wigglemag) - halfwigglemag;

        // SAMPLE TEXTURE
        QColor texCol = new QColor(tex.sample(uv[0], uv[1], QSampleable.SampleType.Repeat));

        // GENERATE RANDOM NORMAL OFFSET BASED ON COLOR
        QVector3 randOffset = seededRandomVector(texCol.toInt( )).multiply3(0.3f);
        
        // CALCULATE BRIGHTNESS FACTOR BASED ON POINT LIGHT
        QVector3 dFaceLightNormalized = QVector3.sub(
            new QVector3(3.0f, 2.4f, 3.0f),
            new QVector3(pos)
        ).fastNormalize( );

        float diffuse = Math.max(0.0f, QVector3.dot(
            new QVector3(normal).add(randOffset).fastNormalize( ), 
            dFaceLightNormalized
        ));
        
        // APPLY SIMPLE PHONG SHADING
        float ambient = 0.4f;
        float phong   = (float)Math.pow(diffuse, 10.0f);
        return multiplyColor(
            texCol, 
            Math.min(1.0f, ambient + diffuse + phong)
        );
    }
}
