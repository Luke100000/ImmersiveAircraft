package immersive_aircraft.resources.bbmodel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BBCube extends BBObject implements BBFaceContainer {
    private static final String[] SIDES = {"north", "east", "south", "west", "up", "down"};

    private static final float[][] NORMALS = new float[][]{
            {0, 0, -1},
            {1, 0, 0},
            {0, 0, 1},
            {-1, 0, 0},
            {0, 1, 0},
            {0, -1, 0},
    };

    private static final int[][] VERTEX_ORDER = new int[][]{
            {1, 4, 6, 3},
            {0, 1, 3, 2},
            {5, 0, 2, 7},
            {4, 5, 7, 6},
            {4, 1, 0, 5},
            {7, 2, 3, 6},
    };

    public final float[] from;
    public final float[] to;
    public final int inflate;
    public final List<BBFace> faces;

    public BBCube(JsonObject element, BBModel model) {
        super(element);

        this.from = BBUtils.parseVector(element, "from");
        this.to = BBUtils.parseVector(element, "to");

        this.inflate = BBUtils.getIntElement(element, "inflate");

        this.faces = new LinkedList<>();
        for (int i = 0; i < 6; i++) {
            BBFace.BBVertex[] vertices = new BBFace.BBVertex[4];
            for (int j = 0; j < 4; j++) {
                vertices[j] = new BBFace.BBVertex();
            }
            this.faces.add(new BBFace(vertices));
        }

        float[][] positions = getPositions();

        // Populate vertices
        for (int i = 0; i < 6; i++) {
            int[] order = VERTEX_ORDER[i];
            for (int j = 0; j < 4; j++) {
                BBFace f = faces.get(i);
                f.vertices[j].x = positions[order[3 - j]][0];
                f.vertices[j].y = positions[order[3 - j]][1];
                f.vertices[j].z = positions[order[3 - j]][2];
            }
        }

        // Populate normals
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                BBFace f = faces.get(i);
                f.vertices[j].nx = NORMALS[i][0];
                f.vertices[j].ny = NORMALS[i][1];
                f.vertices[j].nz = NORMALS[i][2];
            }
        }

        // Populate UV
        double[] u = new double[24];
        double[] v = new double[24];
        for (int i = 0; i < 6; i++) {
            JsonObject faceObject = element.getAsJsonObject("faces").getAsJsonObject(SIDES[i]);

            if (!BBUtils.isNull(faceObject, "texture")) {
                int id = BBUtils.getIntElement(faceObject, "texture");
                BBTexture texture = model.getTexture(id);

                BBFace f = faces.get(i);
                f.texture = texture;

                float[] uv = new float[4];
                Iterator<JsonElement> uvArray = faceObject.getAsJsonArray("uv").iterator();
                for (int j = 0; j < 4; j++) {
                    uv[j] = uvArray.next().getAsFloat();
                }

                int rot = BBUtils.getIntElement(faceObject, "rotation");
                while (rot > 0) {
                    roll(u, i);
                    roll(v, i);
                    rot -= 90;
                }

                float textureWidth = model.getTextureWidth(texture);
                float textureHeight = model.getTextureHeight(texture);

                f.vertices[0].u = uv[0] / textureWidth;
                f.vertices[0].v = uv[3] / textureHeight;
                f.vertices[1].u = uv[2] / textureWidth;
                f.vertices[1].v = uv[3] / textureHeight;
                f.vertices[2].u = uv[2] / textureWidth;
                f.vertices[2].v = uv[1] / textureHeight;
                f.vertices[3].u = uv[0] / textureWidth;
                f.vertices[3].v = uv[1] / textureHeight;
            }
        }

        // Remove degenerate faces
        for (int i = faces.size() - 1; i >= 0; i--) {
            BBFace f = faces.get(i);
            float v0x = f.vertices[1].x - f.vertices[0].x;
            float v0y = f.vertices[1].y - f.vertices[0].y;
            float v0z = f.vertices[1].z - f.vertices[0].z;
            float v1x = f.vertices[2].x - f.vertices[0].x;
            float v1y = f.vertices[2].y - f.vertices[0].y;
            float v1z = f.vertices[2].z - f.vertices[0].z;
            if (f.texture == null || v0x * v1y - v0y * v1x == 0 && v0x * v1z - v0z * v1x == 0 && v0y * v1z - v0z * v1y == 0) {
                faces.remove(i);
            }
        }
    }

    /**
     * Corner positions, relative to the cube origin (pivot), in blocks.
     */
    private float[][] getPositions() {
        float x1 = (from[0] - inflate) / 16.0f - origin[0];
        float y1 = (from[1] - inflate) / 16.0f - origin[1];
        float z1 = (from[2] - inflate) / 16.0f - origin[2];
        float x2 = (to[0] + inflate) / 16.0f - origin[0];
        float y2 = (to[1] + inflate) / 16.0f - origin[1];
        float z2 = (to[2] + inflate) / 16.0f - origin[2];

        return new float[][]{
                {x2, y2, z2},
                {x2, y2, z1},
                {x2, y1, z2},
                {x2, y1, z1},
                {x1, y2, z1},
                {x1, y2, z2},
                {x1, y1, z1},
                {x1, y1, z2},
        };
    }

    private void roll(double[] u, int i) {
        double lastU = u[i * 4 + 3];
        for (int j = 3; j > 0; j--) {
            u[i * 4 + j] = u[i * 4 + j - 1];
        }
        u[i * 4] = lastU;
    }

    @Override
    public Iterable<BBFace> getFaces() {
        return faces;
    }
}
