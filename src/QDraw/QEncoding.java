// Bailey JT Brown
// 2023
// QEncoding.java

package QDraw;

public class QEncoding {
    /////////////////////////////////////////////////////////////////
    // COMMON ABBREVIATIONS:
    // ATRB -> attribute
    // CMPS -> components
    // POSN -> position
    // MTR  -> matrix
    // NUM  -> number (of)
    // VCTR -> vector
    // VERT -> vertex

    /////////////////////////////////////////////////////////////////
    // QVECTOR ENCODINGS
    protected static final int   VCTR_INDEX_X         = 0;
    protected static final int   VCTR_INDEX_Y         = 1;
    protected static final int   VCTR_INDEX_Z         = 2;
    protected static final int   VCTR_INDEX_W         = 3;
    protected static final int   VCTR_NUM_CMPS        = 4;
    protected static final float VCTR_COMPARE_EPSILON = 0.0005f;

    /////////////////////////////////////////////////////////////////
    // QMATRIX4x4 ENCODINGS
    protected static final int   MTR_NUM_ROWS    = VCTR_NUM_CMPS;
    protected static final int   MTR_NUM_COLUMNS = VCTR_NUM_CMPS;
    protected static final int   MTR_NUM_CMPS    = MTR_NUM_ROWS * MTR_NUM_COLUMNS;
    protected static final float MTR_TO_RADIANS  = (float)Math.PI / 180.0f;

    /////////////////////////////////////////////////////////////////
    // QMESH ENCODINGS
    public static final int COMPONENTS_PER_VERTEX = 3;
    public static final int VERTEX_X_OFFSET       = 0;
    public static final int VERTEX_Y_OFFSET       = 1;
    public static final int VERTEX_Z_OFFSET       = 2;

    public static final int COMPONENTS_PER_UV = 2;
    public static final int UV_U_OFFSET       = 0;
    public static final int UV_V_OFFSET       = 1;

    public static final int COMPONENTS_PER_ATTRIBUTE     = 2;
    public static final int MIN_ATTRIBUTES_PER_FACE_DATA = 3;
    public static final int ATTRIBUTE_VERTEX_OFFSET      = 0;
    public static final int ATTRIBUTE_UV_OFFSET          = 1;

    public static final int VERTICIES_PER_TRI        = 3;    
    public static final int COMPONENTS_PER_TRI_DATA  = VERTICIES_PER_TRI * COMPONENTS_PER_ATTRIBUTE;
    public static final int TRI_DATA_VERTEX_0_OFFSET = 0;
    public static final int TRI_DATA_VERTEX_1_OFFSET = 2;
    public static final int TRI_DATA_VERTEX_2_OFFSET = 4;
    public static final int TRI_DATA_UV_0_OFFSET     = 1;
    public static final int TRI_DATA_UV_1_OFFSET     = 3;
    public static final int TRI_DATA_UV_2_OFFSET     = 5;

}
