// Bailey JT Brown
// 2024
// QShader.java

package ca.bael.QDraw;

import java.util.concurrent.atomic.AtomicLong;

import ca.bael.QDraw.QException.PointOfError;
import ca.bael.QDraw.QShader.ShaderRequirement.RequirementType;

public abstract class QShader {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    public static final int RANDOM_GRANULATIRY        = 0xFFFF;
    public static final float NORMALIZATION_FACTOR    = 1.0f / (float)RANDOM_GRANULATIRY;
    public static final int VERTEX_SHADER_MAX_OUTPUTS = 8;

    /////////////////////////////////////////////////////////////////
    // PUBLIC CLASSES
    public static final class OutputSlot {
        final int slot;
        final int offset;
        final int size;

        OutputSlot(int _slot, int _offset, int _size) {
            slot   = _slot;
            offset = _offset;
            size   = _size;
        }
    }

    public static final class VertexShaderContext {
        /////////////////////////////////////////////////////////////////
        // PACKAGE MEMBERS
        QUniform<?>[] uniforms;
        QSampleable[] textures;
        float[][]     attributes;
        OutputLayout  layout;
        float[]       outputs;
        int           written;
        QDrawData     drawData;
        final QVector3 position = new QVector3( );

        /////////////////////////////////////////////////////////////////
        // PUBLIC METHODS
        public QVector3 attribVec3(int slot) {
            return new QVector3(attributes[slot]);
        }

        public QVector3 attribVec3(int slot, QVector3 out) {
            float[] attrib = attributes[slot];
            out.set(attrib[0], attrib[1], attrib.length > 2 ? attrib[2] : 0.0f);
            return out;
        }

        public QVector3 position( ) {
            return position;
        }

        public float[] attrib(int slot, float[] out) {
            System.arraycopy(attributes[slot], 0, out, 0, attributes[slot].length);
            return out;
        }

        public <T extends QUniform<T>> T uniform(int slot, Class<T> type) {
            return type.cast(uniforms[slot]);
        }

        public QSampleable texture(int slot) {
            return textures[slot];
        }

        public <T extends QDrawData> T drawData(Class<T> type) {
            return type.cast(drawData);
        }

        public void output(int slot, QVector3 v) {
            output(slot, v.getComponents( ));
        }

        public OutputSlot outputSlot(int slot) {
            return new OutputSlot(slot, layout.checkedOffset(slot), layout.sizes[slot]);
        }

        public void output(int slot, float... v) {
            write(slot, layout.checkedOffset(slot), layout.sizes[slot], v);
        }

        public void output(OutputSlot slot, QVector3 v) {
            output(slot, v.getComponents( ));
        }

        public void output(OutputSlot slot, float... v) {
            write(slot.slot, slot.offset, slot.size, v);
        }

        private void write(int slot, int offset, int size, float[] v) {
            if (v.length != size) {
                throw new QException(
                    PointOfError.InvalidParameter,
                    "Output slot " + slot + " declared " + size +
                    " components. Given was " + v.length
                );
            }
            System.arraycopy(v, 0, outputs, offset, size);
            written |= 1 << slot;
        }

        void ensureOutputsWritten( ) {
            int missing = layout.declared & ~written;
            if (missing != 0) {
                throw new QException(
                    PointOfError.InvalidData,
                    "Vertex shader did not write declared output slot " +
                    Integer.numberOfTrailingZeros(missing)
                );
            }
        }

        public void forward(int attribSlot) {
            output(attribSlot, attributes[attribSlot]);
        }

        public void forward(OutputSlot slot) {
            output(slot, attributes[slot.slot]);
        }
    }

    public static final class FragmentShaderContext {
        /////////////////////////////////////////////////////////////////
        // PACKAGE MEMBERS
        QUniform<?>[] uniforms;
        QSampleable[] textures;
        QRenderBuffer target;
        int           screenX;
        int           screenY;
        float         invDepth;
        float[]       normal;
        OutputLayout  layout;
        float[]       inputs;
        QSampleable.SampleType sampleType;
        QColor        fillColor;
        QDrawData     drawData;
        final QColor  color = new QColor( );

        /////////////////////////////////////////////////////////////////
        // PUBLIC METHODS
        public QColor color( ) {
            return color;
        }

        public QVector3 inputVec3(int slot) {
            return inputVec3(slot, new QVector3( ));
        }

        public QVector3 inputVec3(int slot, QVector3 out) {
            int offset = layout.checkedOffset(slot);
            out.set(
                inputs[offset],
                inputs[offset + 1],
                layout.sizes[slot] > 2 ? inputs[offset + 2] : 0.0f
            );
            return out;
        }

        public float input(int slot, int comp) {
            return inputs[layout.checkedOffset(slot) + comp];
        }

        public float[] input(int slot, float[] out) {
            System.arraycopy(inputs, layout.checkedOffset(slot), out, 0, layout.sizes[slot]);
            return out;
        }

        public QVector3 inputVec3(OutputSlot slot) {
            return inputVec3(slot, new QVector3( ));
        }

        public QVector3 inputVec3(OutputSlot slot, QVector3 out) {
            out.set(
                inputs[slot.offset],
                inputs[slot.offset + 1],
                slot.size > 2 ? inputs[slot.offset + 2] : 0.0f
            );
            return out;
        }

        public float input(OutputSlot slot, int comp) {
            return inputs[slot.offset + comp];
        }

        public float[] input(OutputSlot slot, float[] out) {
            System.arraycopy(inputs, slot.offset, out, 0, slot.size);
            return out;
        }

        public <T extends QUniform<T>> T uniform(int slot, Class<T> type) {
            return type.cast(uniforms[slot]);
        }

        public QSampleable texture(int slot) {
            return textures[slot];
        }

        public <T extends QDrawData> T drawData(Class<T> type) {
            return type.cast(drawData);
        }

        public int screenX( ) {
            return screenX;
        }

        public int screenY( ) {
            return screenY;
        }

        public float invDepth( ) {
            return invDepth;
        }

        public QVector3 faceNormal( ) {
            return new QVector3(normal);
        }
    }

    static final class OutputLayout {
        /////////////////////////////////////////////////////////////////
        // PACKAGE MEMBERS
        final int[] offsets = new int[VERTEX_SHADER_MAX_OUTPUTS];
        final int[] sizes   = new int[VERTEX_SHADER_MAX_OUTPUTS];
        final int   total;
        final int   declared;

        /////////////////////////////////////////////////////////////////
        // PACKAGE METHODS
        int checkedOffset(int slot) {
            if (sizes[slot] == 0) {
                throw new QException(
                    PointOfError.InvalidParameter,
                    "Output slot " + slot + " was not declared in requirements()"
                );
            }
            return offsets[slot];
        }

        /////////////////////////////////////////////////////////////////
        // CONSTRUCTOR
        OutputLayout(ShaderRequirement[] requirements) {
            int offset = 0;
            int mask   = 0;
            for (ShaderRequirement require : requirements) {
                if (require.requireType != RequirementType.Output) {
                    continue;
                }
                offsets[require.slot] = offset;
                sizes[require.slot]   = require.components;
                offset += require.components;
                mask |= 1 << require.slot;
            }
            total = offset;
            declared = mask;
        }
    }

    public static final class ShaderRequirement {
        /////////////////////////////////////////////////////////////////
        // PUBLIC ENUMS
        public static enum RequirementType {
            Attribute,
            Uniform,
            Texture,
            Output
        }

        /////////////////////////////////////////////////////////////////
        // PUBLIC MEMBERS
        public int             slot;
        public RequirementType requireType;
        public String          purpose;
        public Class<? extends QUniform<?>> uniformClass;
        public int             components;

        /////////////////////////////////////////////////////////////////
        // CONSTRUCTOR
        public ShaderRequirement(int _slot, RequirementType _type, String _purpose) {
            slot        = _slot;
            requireType = _type;
            purpose     = _purpose;
        }

        public ShaderRequirement(
            int      _slot, 
            RequirementType _type, 
            String   _purpose, 
            Class<? extends QUniform<?>> _uniformClass
        ) {
            if (_type != RequirementType.Uniform) {
                throw new QException(
                    PointOfError.InvalidParameter, 
                    "Can ONLY specify uniform class if requirement type is Uniform"
                );
            }
            slot         = _slot;
            requireType  = _type;
            purpose      = _purpose;
            uniformClass = _uniformClass;
        }

        public ShaderRequirement(
            int      _slot,
            RequirementType _type,
            String   _purpose,
            int      _components
        ) {
            if (_type != RequirementType.Output) {
                throw new QException(
                    PointOfError.InvalidParameter,
                    "Can ONLY specify component count if requirement type is Output"
                );
            }
            slot        = _slot;
            requireType = _type;
            purpose     = _purpose;
            components  = _components;
        }
    }

    /////////////////////////////////////////////////////////////////
    // BUILT IN SHADER FUNCTIONS
    private static AtomicLong _seedUniquifier = new AtomicLong(0x5EED);
    public static float random( ) {
        return seededRandom((int)(_seedUniquifier.incrementAndGet( ) + System.nanoTime( )));
    }

    public static float seededRandom(float seed) {
        return seededRandom(Float.floatToIntBits(seed));
    }

    public static float seededRandom(int iSeed) {
        // simple XORSHIFT
        iSeed *= iSeed;
        iSeed ^= (iSeed <<  13);
        iSeed ^= (iSeed >>> 17);
        iSeed ^= (iSeed <<  5);

        // bound and normalize
        iSeed = (iSeed & RANDOM_GRANULATIRY);
        return (float)iSeed * NORMALIZATION_FACTOR;
    }

    public static float seededRandom(Object obj) {
        // generate seed based on object hash
        return seededRandom(obj.hashCode( ));
    }

    public static float seededRandom(FragmentShaderContext fctx) {
        // generate seed based on screen coordinate
        return seededRandom(fctx.screenX + (fctx.screenY * fctx.target.getWidth( )));
    }

    public static QVector3 randomVector( ) {
        return new QVector3(random( ), random( ), random( ));
    }

    public static QVector3 seededRandomVector(float flt) {
        return seededRandomVector(Float.floatToRawIntBits(flt));
    }

    public static QVector3 seededRandomVector(int seed) {
        return new QVector3(
            seededRandom((seed << 2) + 1),
            seededRandom((seed << 2) + 2),
            seededRandom((seed << 2) + 3)
        );
    }

    /////////////////////////////////////////////////////////////////
    // OVERRIDABLE METHODS
    public QDrawData beginDraw(VertexShaderContext context) {
        return null;
    }

    public QVector3 vertexShader(
        VertexShaderContext context
    ) {
        return context.attribVec3(QViewer.DEFAULT_SHADER_POSITION_SLOT, context.position( ));
    }

    public QColor fragmentShader(
        FragmentShaderContext context
    ) {
        return context.color( ).set(0xFF, 0x00, 0xFF, 0xFF);
    }

    /////////////////////////////////////////////////////////////////
    // ABSTRACT METHODS
    public abstract ShaderRequirement[] requirements( );
}
