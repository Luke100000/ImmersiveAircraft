package immersive_aircraft.util.obj;

// This code was written by myself, Sean R. Owens, sean at guild dot net,
// and is released to the public domain. Share and enjoy. Since some
// people argue that it is impossible to release software to the public
// domain, you are also free to use this code under any version of the
// GPL, LPGL, Apache, or BSD licenses, or contact me for use of another
// license.  (I generally don't care so I'll almost certainly say yes.)
// In addition this code may also be used under the "unlicense" described
// at http://unlicense.org/ .  See the file UNLICENSE in the repo.

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

public class Builder {
    private final Logger log = Logger.getLogger(Builder.class.getName());

    public static final int EMPTY_VERTEX_VALUE = Integer.MIN_VALUE;

    // these accumulate each type of vertex as they are parsed, so they can then be referenced via index.
    public final ArrayList<VertexPosition> verticesG = new ArrayList<>();
    public final ArrayList<VertexColor> verticesC = new ArrayList<>();
    public final ArrayList<VertexTexture> verticesT = new ArrayList<>();
    public final ArrayList<VertexNormal> verticesN = new ArrayList<>();
    final HashMap<String, FaceVertex> faceVertexMap = new HashMap<>();
    public final ArrayList<FaceVertex> faceVertexList = new ArrayList<>();

    public final Map<String, Mesh> objects = new HashMap<>();
    public String objectName = null;
    public int faceTriCount = 0;
    public int faceQuadCount = 0;
    public int facePolyCount = 0;
    public int faceErrorCount = 0;

    private final static String OBJ_VERTEX_TEXTURE = "vt";
    private final static String OBJ_VERTEX_NORMAL = "vn";
    private final static String OBJ_VERTEX = "v";
    private final static String OBJ_FACE = "f";
    private final static String OBJ_OBJECT_NAME = "o";

    public Builder(BufferedReader stream) throws IOException {
        String line;

        while (true) {
            line = stream.readLine();
            if (null == line) {
                break;
            }

            line = line.trim();

            if (line.length() == 0) {
                continue;
            }

            if (line.startsWith(OBJ_VERTEX_TEXTURE)) {
                processVertexTexture(line);
            } else if (line.startsWith(OBJ_VERTEX_NORMAL)) {
                processVertexNormal(line);
            } else if (line.startsWith(OBJ_VERTEX)) {
                processVertex(line);
            } else if (line.startsWith(OBJ_FACE)) {
                processFace(line);
            } else if (line.startsWith(OBJ_OBJECT_NAME)) {
                processObjectName(line);
            }
        }
    }

    private void processVertex(String line) {
        float[] values = StringUtils.parseFloatList(7, line, OBJ_VERTEX.length());
        addVertexGeometric(values[0], values[1], values[2]);
        addVertexColor(values[3], values[4], values[5], values[6]);
    }

    private void processVertexTexture(String line) {
        float[] values = StringUtils.parseFloatList(2, line, OBJ_VERTEX_TEXTURE.length());
        addVertexTexture(values[0], 1.0f - values[1]);
    }

    private void processVertexNormal(String line) {
        float[] values = StringUtils.parseFloatList(3, line, OBJ_VERTEX_NORMAL.length());
        addVertexNormal(values[0], values[1], values[2]);
    }

    private void processFace(String line) {
        line = line.substring(OBJ_FACE.length()).trim();
        addFace(StringUtils.parseListVerticeNTuples(line, 3));
    }

    private void processObjectName(String line) {
        addObjectName(line.substring(OBJ_OBJECT_NAME.length()).trim());
    }

    public void addVertexGeometric(float x, float y, float z) {
        verticesG.add(new VertexPosition(x, y, z));
    }

    public void addVertexColor(float r, float g, float b, float a) {
        verticesC.add(new VertexColor(r, g, b, a));
    }

    public void addVertexTexture(float u, float v) {
        verticesT.add(new VertexTexture(u, v));
    }

    public void addVertexNormal(float x, float y, float z) {
        verticesN.add(new VertexNormal(x, y, z));
    }


    public void addFace(int[] vertexIndices) {
        Face face = new Face();

        int i = 0;
        // @TODO: add better error checking - make sure values is not empty and that it is a multiple of 3
        while (i < vertexIndices.length) {
            // >     v is the vertex reference number for a point element. Each point
            // >     element requires one vertex. Positive values indicate absolute
            // >     vertex numbers. Negative values indicate relative vertex numbers.

            FaceVertex fv = new FaceVertex();
            //            log.log(INFO,"Adding vertex g=" + vertexIndices[i] + " t=" + vertexIndices[i + 1] + " n=" + vertexIndices[i + 2]);
            int vertexIndex;
            vertexIndex = vertexIndices[i++];
            // Note that we can use negative references to denote vertices in manner relative to the current point in the file, i.e.
            // rather than "the 5th vertex in the file" we can say "the 5th vertex before now"
            if (vertexIndex < 0) {
                vertexIndex = vertexIndex + verticesG.size();
            }
            if (((vertexIndex - 1) >= 0) && ((vertexIndex - 1) < verticesG.size())) {
                // Note: vertex indices are 1-indexed, i.e. they start at
                // one, so we offset by -1 for the 0-indexed array lists.
                fv.v = verticesG.get(vertexIndex - 1);
                fv.c = verticesC.get(vertexIndex - 1);
            } else {
                log.log(SEVERE, "Index for geometric vertex=" + vertexIndex + " is out of the current range of geometric vertex values 1 to " + verticesG.size() + ", ignoring");
            }

            vertexIndex = vertexIndices[i++];
            if (vertexIndex != EMPTY_VERTEX_VALUE) {
                if (vertexIndex < 0) {
                    // Note that we can use negative references to denote vertices in manner relative to the current point in the file, i.e.
                    // rather than "the 5th vertex in the file" we can say "the 5th vertex before now"
                    vertexIndex = vertexIndex + verticesT.size();
                }
                if (((vertexIndex - 1) >= 0) && ((vertexIndex - 1) < verticesT.size())) {
                    // Note: vertex indices are 1-indexed, i.e. they start at
                    // one, so we offset by -1 for the 0-indexed array lists.
                    fv.t = verticesT.get(vertexIndex - 1);
                } else {
                    log.log(SEVERE, "Index for texture vertex=" + vertexIndex + " is out of the current range of texture vertex values 1 to " + verticesT.size() + ", ignoring");
                }
            }

            vertexIndex = vertexIndices[i++];
            if (vertexIndex != EMPTY_VERTEX_VALUE) {
                if (vertexIndex < 0) {
                    // Note that we can use negative references to denote vertices in manner relative to the current point in the file, i.e.
                    // rather than "the 5th vertex in the file" we can say "the 5th vertex before now"
                    vertexIndex = vertexIndex + verticesN.size();
                }
                if (((vertexIndex - 1) >= 0) && ((vertexIndex - 1) < verticesN.size())) {
                    // Note: vertex indices are 1-indexed, i.e. they start at
                    // one, so we offset by -1 for the 0-indexed array lists.
                    fv.n = verticesN.get(vertexIndex - 1);
                } else {
                    log.log(SEVERE, "Index for vertex normal=" + vertexIndex + " is out of the current range of vertex normal values 1 to " + verticesN.size() + ", ignoring");
                }
            }

            if (fv.v == null) {
                log.log(SEVERE, "Can't add vertex to face with missing vertex!  Throwing away face.");
                faceErrorCount++;
                return;
            }

            // Make sure we don't end up with redundant vertices
            // combinations - i.e. any specific combination of g,v and
            // t is only stored once and is reused instead.
            String key = fv.toString();
            FaceVertex fv2 = faceVertexMap.get(key);
            if (null == fv2) {
                faceVertexMap.put(key, fv);
                fv.index = faceVertexList.size();
                faceVertexList.add(fv);
            } else {
                fv = fv2;
            }

            face.add(fv);
        }

        if (objectName == null) {
            objectName = "unnamed";
            objects.put(objectName, new Mesh());
        }

        objects.get(objectName).add(face);

        // collect some stats for laughs
        if (face.vertices.size() == 3) {
            faceTriCount++;
        } else if (face.vertices.size() == 4) {
            faceQuadCount++;
        } else {
            facePolyCount++;
        }
    }

    public void addObjectName(String name) {
        this.objectName = name;
        objects.put(objectName, new Mesh());
    }
}
