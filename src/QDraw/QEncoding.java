// Bailey JT Brown
// 2023-2024
// QEncoding.java

package QDraw;

public class QEncoding {
    /////////////////////////////////////////////////////////////////
    // COMMON ABBREVIATIONS:
    // ATRB -> attribute
    // BMAP -> bitmask
    // CMPS -> components
    // CHNL -> channel
    // CTX  -> context
    // COL  -> color
    // OFST -> offset
    // POSN -> position
    // MTR  -> matrix
    // NUM  -> number (of)
    // TDI  -> triDataIndicies
    // VCTR -> vector
    // VERT -> vertex
    // VTRI -> QViewer's internal Tri class

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
    // QCOLOR ENCODINGS
    // QColor is int
    // 4-byte integer encoded with each byte as ARGB
    protected static final int COL_LSHIFT_OFST_A = 24;
    protected static final int COL_LSHIFT_OFST_R = 16;
    protected static final int COL_LSHIFT_OFST_G = 8;
    protected static final int COL_LSHIFT_OFST_B = 0;
    protected static final int COL_CHNL_BMASK    = 0x000000FF;
    protected static final int COL_BMASK_A       = COL_CHNL_BMASK << COL_LSHIFT_OFST_A;
    protected static final int COL_BMASK_R       = COL_CHNL_BMASK << COL_LSHIFT_OFST_R;
    protected static final int COL_BMASK_G       = COL_CHNL_BMASK << COL_LSHIFT_OFST_G;
    protected static final int COL_BMASK_B       = COL_CHNL_BMASK << COL_LSHIFT_OFST_B;

    /////////////////////////////////////////////////////////////////
    // QVECTOR ENCODINGS
    // QVector is
    // | x y z | : float[]
    // size 3 array of floats representing a 3-component vector
    protected static final int   VCTR_INDEX_X         = 0;
    protected static final int   VCTR_INDEX_Y         = 1;
    protected static final int   VCTR_INDEX_Z         = 2;
    protected static final int   VCTR_NUM_CMPS        = 3;
    protected static final float VCTR_COMPARE_EPSILON = 0.0005f;

    /////////////////////////////////////////////////////////////////
    // QMATRIX4x4 ENCODINGS
    // QMatrix4x4 is
    // | <x1 y1 z1 w1>
    //   <x2 y2 z2 w2>
    //   <x3 y3 z3 w3>
    //   <x4 y4 z4 w4> | : float[]
    // size 16 float array representing a 4x4 matrix
    protected static final int   MTR_NUM_ROWS    = 4;
    protected static final int   MTR_NUM_COLUMNS = 4;
    protected static final int   MTR_NUM_CMPS    = MTR_NUM_ROWS * MTR_NUM_COLUMNS;

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
    protected static final int MESH_POSN_NUM_CMPS = 3;
    protected static final int MESH_POSN_OFST_X   = 0;
    protected static final int MESH_POSN_OFST_Y   = 1;
    protected static final int MESH_POSN_OFST_Z   = 2;

    protected static final int MESH_UV_NUM_CMPS = 2;
    protected static final int MESH_UV_OFST_U   = 0;
    protected static final int MESH_UV_OFST_V   = 1;

    protected static final int MESH_ATRB_NUM_CMPS  = 2;
    protected static final int MESH_FACE_MIN_ATRBS = 3;
    protected static final int MESH_ATRS_OFST_POS  = 0;
    protected static final int MESH_ATRS_OFST_UV   = 1;

    protected static final int MESH_VERTS_PER_TRI = 3;    
    protected static final int MESH_TDI_NUM_CMPS  = MESH_VERTS_PER_TRI * MESH_ATRB_NUM_CMPS;
    protected static final int MESH_TDI_OFST_POS0 = 0;
    protected static final int MESH_TDI_OFST_POS1 = 2;
    protected static final int MESH_TDI_OFST_POS2 = 4;
    protected static final int MESH_TDI_OFST_UV0  = 1;
    protected static final int MESH_TDI_OFST_UV1  = 3;
    protected static final int MESH_TDI_OFST_UV2  = 5;

    /////////////////////////////////////////////////////////////////
    // QVIWERER.TRI ENCODINGS
    protected static final int VTRI_POSDAT_NUM_CMPS = MESH_POSN_NUM_CMPS * MESH_VERTS_PER_TRI;
    protected static final int VTRI_UVDAT_NUM_UVS   = MESH_UV_NUM_CMPS * MESH_VERTS_PER_TRI;

}
