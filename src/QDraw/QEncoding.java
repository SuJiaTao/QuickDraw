// Bailey JT Brown
// 2023
// QEncoding.java

package QDraw;

public class QEncoding {
    /////////////////////////////////////////////////////////////////
    // COMMON ABBREVIATIONS:
    // ATRB -> attribute
    // CMPS -> components
    // OFST -> offset
    // POSN -> position
    // MTR  -> matrix
    // NUM  -> number (of)
    // TDI  -> triDataIndicies
    // VCTR -> vector
    // VERT -> vertex

    /////////////////////////////////////////////////////////////////
    // DATA COMMENT INTERPRETATIONS:
    // | x y z |            is array of exactly n elements
    // < x y z >            is contiguous logical grouping of elements in array
    // ( x y z )            is 3 important related class members
    // | x y z ... |        is array of arbitrary number of elements
    // | < x y z > ... |    is array of arbitrary number of groupings
    // | | x1 y1 z1 | ... | is arbitrary length array of arrays
    // : Type               is of Type
    // : Type[]             is array of Type

    /////////////////////////////////////////////////////////////////
    // QVECTOR ENCODINGS
    // QVector is
    // | x y z w | : float[]
    // size 4 array of floats representing a 4-component vector
    protected static final int   VCTR_INDEX_X         = 0;
    protected static final int   VCTR_INDEX_Y         = 1;
    protected static final int   VCTR_INDEX_Z         = 2;
    protected static final int   VCTR_INDEX_W         = 3;
    protected static final int   VCTR_NUM_CMPS        = 4;
    protected static final float VCTR_COMPARE_EPSILON = 0.0005f;

    /////////////////////////////////////////////////////////////////
    // QMATRIX4x4 ENCODINGS
    // QMatrix4x4 is
    // | <x1 y1 z1 w1>
    //   <x2 y2 z2 w2>
    //   <x3 y3 z3 w3>
    //   <x4 y4 z4 w4> | : float[]
    // size 16 float array representing a 4x4 matrix
    protected static final int   MTR_NUM_ROWS    = VCTR_NUM_CMPS;
    protected static final int   MTR_NUM_COLUMNS = VCTR_NUM_CMPS;
    protected static final int   MTR_NUM_CMPS    = MTR_NUM_ROWS * MTR_NUM_COLUMNS;
    protected static final float MTR_TO_RADIANS  = (float)Math.PI / 180.0f;

    /////////////////////////////////////////////////////////////////
    // QMESH ENCODINGS
    // QMesh is
    //   (posData, uvData, triDataIndicies)
    // posData is
    //   | <x1 y1 z1> <x2 y2 z2> ... | : float[]
    //   groups of 3-space positions for each vertex
    // uvData is
    //   | <u1 v1> <u2 v2> ... | : float[]
    //   groups of 2-space UV coordinates for each vertex
    // triDataIndicies is
    //   | <<p0 uv0> <p1 uv1> <p2 uv2>> ... | : int[]
    //   groups of indicies into posData and uvData for each triangle
    //   group <p uv> is an "attribute"
    //   each indicie will point to first element in each grouping
    public static final int MESH_POSN_NUM_CMPS = 3;
    public static final int MESH_POSN_OFST_X   = 0;
    public static final int MESH_POSN_OFST_Y   = 1;
    public static final int MESH_POSN_OFST_Z   = 2;

    public static final int MESH_UV_NUM_CMPS = 2;
    public static final int MESH_UV_OFST_U   = 0;
    public static final int MESH_UV_OFST_V   = 1;

    public static final int MESH_ATRB_NUM_CMPS  = 2;
    public static final int MESH_FACE_MIN_ATRBS = 3;
    public static final int MESH_ATRS_OFST_POS  = 0;
    public static final int MESH_ATRS_OFST_UV   = 1;

    public static final int MESH_VERTS_PER_TRI = 3;    
    public static final int MESH_TDI_NUM_CMPS  = MESH_VERTS_PER_TRI * MESH_ATRB_NUM_CMPS;
    public static final int MESH_TDI_OFST_POS0 = 0;
    public static final int MESH_TDI_OFST_POS1 = 2;
    public static final int MESH_TDI_OFST_POS2 = 4;
    public static final int MESH_TDI_OFST_UV0  = 1;
    public static final int MESH_TDI_OFST_UV1  = 3;
    public static final int MESH_TDI_OFST_UV2  = 5;

}
