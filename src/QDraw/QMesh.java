// Bailey JT Brown
// 2023-2024
// QMesh.java

package QDraw;

import java.io.*;
import java.util.*;
import QDraw.QException.PointOfError;

// TODO: re-write to use AttriBuffer/Indexer to internally store data

public final class QMesh extends QEncoding {
    /////////////////////////////////////////////////////////////////
    // CONSTANTS
    private static final int CMPS_PER_POSN   = 3;
    private static final int CMPS_PER_UV     = 2;
    private static final int CMPS_PER_NORMAL = 3;
    private static final int VERTS_PER_TRI   = 3;

    private static final int OBJLOAD_POSN_BUFFER_INITIAL_SIZE = 
        CMPS_PER_POSN * VERTS_PER_TRI * 20;
    private static final int OBJLOAD_UV_BUFFER_INITIAL_SIZE = 
        CMPS_PER_UV * VERTS_PER_TRI * 20;
    private static final int OBJLOAD_NORMAL_BUFFER_INITIAL_SIZE = 
        CMPS_PER_NORMAL * VERTS_PER_TRI * 20;
    private static final int OBJLOAD_FACEDAT_BUFFER_INITIAL_SIZE = 20;

    private static final int ATTRIBS_PER_FACE_VERTEX   = 3; // 1 - posn, 2 - uv, 3 - normal
    private static final int MIN_ATTRIBS_PER_FACE      = VERTS_PER_TRI * ATTRIBS_PER_FACE_VERTEX; 
    private static final int FACE_ATTRIB_POSN_OFFSET   = 0;
    private static final int FACE_ATTRIB_UV_OFFSET     = 1;
    private static final int FACE_ATTRIB_NORMAL_OFFSET = 2;

    /////////////////////////////////////////////////////////////////
    // PRIVATE CLASSES
    private class FloatList {
        /////////////////////////////////////////////////////////////////
        // PRIVATE MEMBERS
        private float[] buffer;
        private int     head;

        /////////////////////////////////////////////////////////////////
        // PRIVATE METHODS
        private void grow( ) {
            float[] newBuffer = new float[buffer.length * 2];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
        }

        /////////////////////////////////////////////////////////////////
        // PUBLIC METHODS
        public void add(float val) {
            buffer[head] = val;
            head++;
            if (head >= buffer.length) {
                grow( );
            }
        }

        public float[] toArray( ) {
            float[] retArray = new float[head];
            System.arraycopy(buffer, 0, retArray, 0, head);
            return retArray;
        }

        /////////////////////////////////////////////////////////////////
        // CONSTRUCTOR
        public FloatList(int capacity) {
            buffer = new float[capacity];
            head   = 0;
        }
    }

    private class IntList {
        /////////////////////////////////////////////////////////////////
        // PRIVATE MEMBERS
        private int[] buffer;
        private int     head;

        /////////////////////////////////////////////////////////////////
        // PRIVATE METHODS
        private void grow( ) {
            int[] newBuffer = new int[buffer.length * 2];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
        }

        /////////////////////////////////////////////////////////////////
        // PUBLIC METHODS
        public void add(int val) {
            buffer[head] = val;
            head++;
            if (head >= buffer.length) {
                grow( );
            }
        }

        public int[] toArray( ) {
            int[] retArray = new int[head];
            System.arraycopy(buffer, 0, retArray, 0, head);
            return retArray;
        }

        /////////////////////////////////////////////////////////////////
        // CONSTRUCTOR
        public IntList(int capacity) {
            buffer = new int[capacity];
            head   = 0;
        }
    }

    private class IndicieList {
        /////////////////////////////////////////////////////////////////
        // PRIVATE MEMBERS
        private int[][] buffer;
        private int     head;

        /////////////////////////////////////////////////////////////////
        // PRIVATE METHODS
        private void grow( ) {
            int[][] newBuffer = new int[buffer.length * 2][];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
        }

        /////////////////////////////////////////////////////////////////
        // PUBLIC METHODS
        public void add(int[] val) {
            buffer[head] = val;
            head++;
            if (head >= buffer.length) {
                grow( );
            }
        }

        public int[][] toArray( ) {
            int[][] retArray = new int[head][];
            System.arraycopy(buffer, 0, retArray, 0, head);
            return retArray;
        }

        /////////////////////////////////////////////////////////////////
        // CONSTRUCTOR
        public IndicieList(int capacity) {
            buffer = new int[capacity][];
            head   = 0;
        }
    }

    /////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    private QAttribBuffer  posBuffer;
    private QAttribBuffer  uvBuffer;
    private QAttribBuffer  normalBuffer;
    private QAttribIndexer posIndexer;
    private QAttribIndexer uvIndexer;
    private QAttribIndexer normalIndexer;

    /////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    public QAttribIndexer getPosIndexer( ) { return posIndexer; }
    public QAttribIndexer getUVIndexer( ) { return uvIndexer; }
    public QAttribIndexer getNormalIndexer( ) { return normalIndexer; }

    /////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private void initMesh(
        float[] inPosns,
        float[] inUVs,
        float[] inNormals,
        int[][] inFaceIndicies
        ) {

        if ((inPosns.length % CMPS_PER_POSN) != 0) {
            throw new QException(
                PointOfError.MalformedData,
                "inPosns length must be multiple of " + CMPS_PER_POSN + " . " +
                "Given was " + inPosns.length
            );
        }

        if ((inUVs.length % CMPS_PER_UV) != 0) {
            throw new QException(
                PointOfError.MalformedData,
                "inUVs length must be multiple of " + CMPS_PER_UV + " . " +
                "Given was " + inUVs.length
            );
        }

        if ((inNormals.length % CMPS_PER_NORMAL) != 0) {
            throw new QException(
                PointOfError.MalformedData,
                "inNormals length must be multiple of " + CMPS_PER_NORMAL + " . " +
                "Given was " + inUVs.length
            );
        }

        posBuffer    = new QAttribBuffer(
            inPosns, 
            CMPS_PER_POSN, 
            inPosns.length / CMPS_PER_POSN
        );
        uvBuffer     = new QAttribBuffer(
            inUVs, 
            CMPS_PER_UV, 
            inUVs.length / CMPS_PER_UV
        );
        normalBuffer = new QAttribBuffer(
            inNormals, 
            CMPS_PER_NORMAL, 
            inNormals.length / CMPS_PER_NORMAL
        );

        initIndexers(inFaceIndicies);

    }

    private void initIndexers(int[][] inFaceIndicies) {

        int numTriangles = 0;
        for (int faceNum = 0; faceNum < inFaceIndicies.length; faceNum++) {
            int[] faceIndicies   = inFaceIndicies[faceNum];
            int faceIndicieCount = faceIndicies.length;
            int faceVertexCount  = faceIndicieCount / ATTRIBS_PER_FACE_VERTEX;

            // ENSURE WELL FORMED DATA
            if ((faceIndicieCount % ATTRIBS_PER_FACE_VERTEX) != 0) {
                throw new QException(
                    PointOfError.MalformedData, 
                    "inFaceIndicies face " + faceNum + 
                    " must have multiple of " + ATTRIBS_PER_FACE_VERTEX +
                    " indicies. Given was " + faceIndicieCount
                );
            }

            // ENSURE VALID VERTEX COUNT
            if (faceVertexCount < VERTS_PER_TRI) {
                throw new QException(
                    PointOfError.MalformedData, 
                    "inFaceIndicies face " + faceNum + 
                    " consists of only " + faceVertexCount +
                    " verticies. Minimum verts per face is " + VERTS_PER_TRI
                );
            }

            // COUNT TRIANGLES
            numTriangles += 1 + (faceVertexCount - VERTS_PER_TRI);
        }

        IntList tempPosIndicies    = new IntList(numTriangles);
        IntList tempUVIndicies     = new IntList(numTriangles);
        IntList tempNormalIndicies = new IntList(numTriangles);

        for (int faceNum = 0; faceNum < inFaceIndicies.length; faceNum++) {
            // NOTE:
            // one triangle can always be constructed from a face, then the rest will
            // be tesselated from the face
            int[] faceIndicies  = inFaceIndicies[faceNum];
            int   numExtraVerts = (faceIndicies.length / ATTRIBS_PER_FACE_VERTEX) - VERTS_PER_TRI;
            
            // construct first triangle
            tempPosIndicies.add(faceIndicies[ATTRIBS_PER_FACE_VERTEX * 0 + FACE_ATTRIB_POSN_OFFSET]);
            tempPosIndicies.add(faceIndicies[ATTRIBS_PER_FACE_VERTEX * 1 + FACE_ATTRIB_POSN_OFFSET]);
            tempPosIndicies.add(faceIndicies[ATTRIBS_PER_FACE_VERTEX * 2 + FACE_ATTRIB_POSN_OFFSET]);
            
            tempUVIndicies.add(faceIndicies[ATTRIBS_PER_FACE_VERTEX * 0 + FACE_ATTRIB_UV_OFFSET]);
            tempUVIndicies.add(faceIndicies[ATTRIBS_PER_FACE_VERTEX * 1 + FACE_ATTRIB_UV_OFFSET]);
            tempUVIndicies.add(faceIndicies[ATTRIBS_PER_FACE_VERTEX * 2 + FACE_ATTRIB_UV_OFFSET]);
            
            tempNormalIndicies.add(faceIndicies[ATTRIBS_PER_FACE_VERTEX * 0 + FACE_ATTRIB_NORMAL_OFFSET]);
            tempNormalIndicies.add(faceIndicies[ATTRIBS_PER_FACE_VERTEX * 1 + FACE_ATTRIB_NORMAL_OFFSET]);
            tempNormalIndicies.add(faceIndicies[ATTRIBS_PER_FACE_VERTEX * 2 + FACE_ATTRIB_NORMAL_OFFSET]);

            // tesselate extras if needed as triangle fan
            for (int nthExtra = 0; nthExtra < numExtraVerts; nthExtra++) {
                // VERTEX 0 -> first vertex in face
                int vertex0BaseOffset = ATTRIBS_PER_FACE_VERTEX * 0;
                tempPosIndicies.add(faceIndicies[vertex0BaseOffset + FACE_ATTRIB_POSN_OFFSET]);
                tempUVIndicies.add(faceIndicies[vertex0BaseOffset + FACE_ATTRIB_UV_OFFSET]);
                tempNormalIndicies.add(faceIndicies[vertex0BaseOffset + FACE_ATTRIB_NORMAL_OFFSET]);

                // VERTEX 1 -> (2 + n)th vertex
                // where n is the nth extra triangle being tesselated (starting from 0)
                int vertex1BaseOffset = ATTRIBS_PER_FACE_VERTEX * (2 + nthExtra);
                tempPosIndicies.add(faceIndicies[vertex1BaseOffset + FACE_ATTRIB_POSN_OFFSET]);
                tempUVIndicies.add(faceIndicies[vertex1BaseOffset + FACE_ATTRIB_UV_OFFSET]);
                tempNormalIndicies.add(faceIndicies[vertex1BaseOffset + FACE_ATTRIB_NORMAL_OFFSET]);

                // VERTEX 2 -> (3 + n)th vertex
                // where n is the nth extra triangle being tesselated (starting from 0)
                int vertex2BaseOffset = ATTRIBS_PER_FACE_VERTEX * (3 + nthExtra);
                tempPosIndicies.add(faceIndicies[vertex2BaseOffset + FACE_ATTRIB_POSN_OFFSET]);
                tempUVIndicies.add(faceIndicies[vertex2BaseOffset + FACE_ATTRIB_UV_OFFSET]);
                tempNormalIndicies.add(faceIndicies[vertex2BaseOffset + FACE_ATTRIB_NORMAL_OFFSET]);
            }

        }

        int totalVertCount = numTriangles * VERTS_PER_TRI;
        
        posIndexer = new QAttribIndexer(tempPosIndicies.toArray(), totalVertCount);
        posIndexer.setAttribBuffer(posBuffer);

        uvIndexer = new QAttribIndexer(tempUVIndicies.toArray(), totalVertCount);
        uvIndexer.setAttribBuffer(uvBuffer);

        normalIndexer = new QAttribIndexer(tempNormalIndicies.toArray(), totalVertCount);
        normalIndexer.setAttribBuffer(normalBuffer);

    }

    /////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // inPosn is:
    //   | <x y z> ... | : float[]
    //   array of contigugous 3 space vertex positions
    // inUVs is:
    //   | <u v> ... | : float[]
    //   array of contiguous 2 space UV coordinates
    // inNormals is:
    //   | <x y z> ... | : float[]
    //   array of contiguous 3 space surface normals
    // inFaceIndicies is:
    //   | | <p0 uv0 n0> <p1 uv1 n1> <p2 uv2 n2> ... | ... | : int[][]
    //   array of vertex attribute indicies per face (stored as an array)
    //   where each face has at least 3 verticies. vertex attribute indicies
    //   are ordered as position, uv, normal
    public QMesh(
        float[] inPosns,
        float[] inUVs,
        float[] inNormals,
        int[][] inFaceIndicies
        ) {
            initMesh(inPosns, inUVs, inNormals, inFaceIndicies);
    }

    public QMesh(QMesh toCopy) {
        posBuffer     = toCopy.posBuffer;
        uvBuffer      = toCopy.uvBuffer;
        normalBuffer  = toCopy.normalBuffer;
        posIndexer    = toCopy.posIndexer;
        uvIndexer     = toCopy.uvIndexer;
        normalIndexer = toCopy.normalIndexer;
    }

    public QMesh(String objFilePath) {
        if (!objFilePath.endsWith(".obj")) {
            throw new QException(
                PointOfError.InvalidParameter, 
                "objFilePath must contain .obj file, file was instead " 
                + objFilePath
            );
        }

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(objFilePath));
        } catch (Exception e) {
            throw new QException(
                PointOfError.InvalidParameter, 
                "Unable to read file " + objFilePath
            );
        }

        FloatList   posBuffer       = new FloatList(OBJLOAD_POSN_BUFFER_INITIAL_SIZE);
        FloatList   uvBuffer        = new FloatList(OBJLOAD_UV_BUFFER_INITIAL_SIZE);
        FloatList   normalBuffer    = new FloatList(OBJLOAD_NORMAL_BUFFER_INITIAL_SIZE);
        IndicieList faceIndexBuffer = new IndicieList(OBJLOAD_FACEDAT_BUFFER_INITIAL_SIZE);

        while (scanner.hasNextLine( )) {
            String line = scanner.nextLine( );

            // PARSE POSITION
            if (line.substring(0, 2).equals("v ")) {
                String[] posVals = line.substring(2).split(" ");
                posBuffer.add(Float.parseFloat(posVals[0]));
                posBuffer.add(Float.parseFloat(posVals[1]));
                posBuffer.add(Float.parseFloat(posVals[2]));
                continue;
            }

            // PARSE UV
            if (line.substring(0, 3).equals("vt ")) {
                String[] uvVals = line.substring(3).split(" ");
                uvBuffer.add(Float.parseFloat(uvVals[0]));
                uvBuffer.add(Float.parseFloat(uvVals[1]));
                continue;
            }

            // PARSE NORMAL
            if (line.substring(0, 3).equals("vn ")) {
                String[] normVals = line.substring(3).split(" ");
                normalBuffer.add(Float.parseFloat(normVals[0]));
                normalBuffer.add(Float.parseFloat(normVals[1]));
                normalBuffer.add(Float.parseFloat(normVals[2]));
                continue;
            }

            // PARSE FACEDATA
            if (line.substring(0, 2).equals("f ")) {
                // NOTE
                // - .obj file face indicies are structured each as p/uv/n and delimited
                //   by whitespaces
                String[] faceAttribIndicies = line.substring(2).split(" ");
                ArrayList<Integer> iBuffer = new ArrayList<>(MIN_ATTRIBS_PER_FACE);

                for (String faceAttribs : faceAttribIndicies) {
                    // NOTE: obj files are NOT 0 indexed
                    String[] indicies = faceAttribs.split("/");
                    if (indicies.length < 3) {
                        throw new QException(
                            PointOfError.MalformedData, 
                            ".obj file must contain face normal data! Normals not found."
                        );
                    }
                    iBuffer.add(Integer.parseInt(indicies[0]) - 1);
                    iBuffer.add(Integer.parseInt(indicies[1]) - 1);
                    iBuffer.add(Integer.parseInt(indicies[2]) - 1);
                }

                // refer to
                // https://stackoverflow.com/questions/960431/how-can-i-convert-listinteger-to-int-in-java
                faceIndexBuffer.add(iBuffer.stream( ).mapToInt(i -> i).toArray( ));
                continue;
            }
        }

        scanner.close( );

        initMesh(
            posBuffer.toArray( ), 
            uvBuffer.toArray( ), 
            normalBuffer.toArray( ),
            faceIndexBuffer.toArray( )
        );
    }

}
