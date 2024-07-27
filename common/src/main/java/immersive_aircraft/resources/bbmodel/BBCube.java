package immersive_aircraft.resources.bbmodel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import immersive_aircraft.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BBCube extends BBObject implements BBFaceContainer {
    private static final String[] SIDES = {"north", "east", "south", "west", "up", "down"};

    private static final Vector3f[] NORMALS = new Vector3f[]{
            new Vector3f(0, 0, -1),
            new Vector3f(1, 0, 0),
            new Vector3f(0, 0, 1),
            new Vector3f(-1, 0, 0),
            new Vector3f(0, 1, 0),
            new Vector3f(0, -1, 0),
    };

    private static final int[][] VERTEX_ORDER = new int[][]{
            {1, 4, 6, 3},
            {0, 1, 3, 2},
            {5, 0, 2, 7},
            {4, 5, 7, 6},
            {4, 1, 0, 5},
            {7, 2, 3, 6},
    };

    public final Vector3f from;
    public final Vector3f to;
    public final int inflate;
    public final List<BBFace> faces;
    private final boolean backfaceCulling;

    public BBCube(JsonObject element, BBModel model) {
        super(element);

        this.from = Utils.parseVector(element, "from");
        this.to = Utils.parseVector(element, "to");

        this.inflate = Utils.getIntElement(element, "inflate");

        this.faces = new LinkedList<>();
        for (int i = 0; i < 6; i++) {
            BBFace.BBVertex[] vertices = new BBFace.BBVertex[4];
            for (int j = 0; j < 4; j++) {
                vertices[j] = new BBFace.BBVertex();
            }
            this.faces.add(new BBFace(vertices));
        }

        Vector3f[] positions = getPositions();

        // Populate vertices
        for (int i = 0; i < 6; i++) {
            int[] order = VERTEX_ORDER[i];
            for (int j = 0; j < 4; j++) {
                BBFace f = faces.get(i);
                f.vertices[j].x = positions[order[3 - j]].x();
                f.vertices[j].y = positions[order[3 - j]].y();
                f.vertices[j].z = positions[order[3 - j]].z();
            }
        }

        // Populate normals
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                BBFace f = faces.get(i);
                f.vertices[j].nx = NORMALS[i].x();
                f.vertices[j].ny = NORMALS[i].y();
                f.vertices[j].nz = NORMALS[i].z();
            }
        }

        // Populate UV
        double[] u = new double[24];
        double[] v = new double[24];
        for (int i = 0; i < 6; i++) {
            JsonObject faceObject = element.getAsJsonObject("faces").getAsJsonObject(SIDES[i]);

            if (!Utils.isNull(faceObject, "texture")) {
                int id = Utils.getIntElement(faceObject, "texture");
                BBTexture texture = model.getTexture(id);

                BBFace f = faces.get(i);
                f.texture = texture;

                float[] uv = new float[4];
                Iterator<JsonElement> uvArray = faceObject.getAsJsonArray("uv").iterator();
                for (int j = 0; j < 4; j++) {
                    uv[j] = uvArray.next().getAsFloat();
                }

                int rot = Utils.getIntElement(faceObject, "rotation");
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

        // Only use culling on flat cubes with more than one face
        // In those cases, z-fighting would occur
        boolean flat = getVolume() < 0.01f;
        backfaceCulling = flat && faces.size() > 1;
    }

    private float getVolume() {
        float x2 = Float.MIN_VALUE, y2 = Float.MIN_VALUE, z2 = Float.MIN_VALUE, x1 = Float.MAX_VALUE, y1 = Float.MAX_VALUE, z1 = Float.MAX_VALUE;
        for (BBFace f : faces) {
            for (BBFace.BBVertex vert : f.vertices) {
                x1 = Math.min(x1, vert.x);
                y1 = Math.min(y1, vert.y);
                z1 = Math.min(z1, vert.z);
                x2 = Math.max(x2, vert.x);
                y2 = Math.max(y2, vert.y);
                z2 = Math.max(z2, vert.z);
            }
        }
        return (x2 - x1) * (y2 - y1) * (z2 - z1);
    }

    @NotNull
    private Vector3f[] getPositions() {
        Vector3f adjustedFrom = this.from;
        Vector3f adjustedTo = this.to;

        Vector3f inflate = new Vector3f(this.inflate, this.inflate, this.inflate);
        adjustedFrom.sub(inflate);
        adjustedTo.add(inflate);

        adjustedFrom.mul(1.0f / 16.0f);
        adjustedTo.mul(1.0f / 16.0f);

        adjustedFrom.sub(origin);
        adjustedTo.sub(origin);

        return new Vector3f[]{
                new Vector3f(adjustedTo.x(), adjustedTo.y(), adjustedTo.z()),
                new Vector3f(adjustedTo.x(), adjustedTo.y(), adjustedFrom.z()),
                new Vector3f(adjustedTo.x(), adjustedFrom.y(), adjustedTo.z()),
                new Vector3f(adjustedTo.x(), adjustedFrom.y(), adjustedFrom.z()),
                new Vector3f(adjustedFrom.x(), adjustedTo.y(), adjustedFrom.z()),
                new Vector3f(adjustedFrom.x(), adjustedTo.y(), adjustedTo.z()),
                new Vector3f(adjustedFrom.x(), adjustedFrom.y(), adjustedFrom.z()),
                new Vector3f(adjustedFrom.x(), adjustedFrom.y(), adjustedTo.z()),
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

    @Override
    public boolean enableCulling() {
        return backfaceCulling;
    }
}
