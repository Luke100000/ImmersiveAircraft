package immersive_aircraft.resources.bbmodel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.math.Vector3f;
import immersive_aircraft.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class BBCube extends BBObject {
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
    public final BBFace[] faces;

    public BBCube(JsonObject element, BBModel model) {
        super(element);

        this.from = Utils.parseVector(element, "from");
        this.to = Utils.parseVector(element, "to");

        this.inflate = Utils.getIntElement(element, "inflate");

        this.faces = new BBFace[6];
        for (int i = 0; i < 6; i++) {
            BBFace.BBVertex[] vertices = new BBFace.BBVertex[4];
            for (int j = 0; j < 4; j++) {
                vertices[j] = new BBFace.BBVertex();
            }
            this.faces[i] = new BBFace(vertices);
        }

        Vector3f[] positions = getPositions();

        // Populate vertices
        for (int i = 0; i < 6; i++) {
            int[] order = VERTEX_ORDER[i];
            for (int j = 0; j < 4; j++) {
                faces[i].vertices[j].x = positions[order[3 - j]].x();
                faces[i].vertices[j].y = positions[order[3 - j]].y();
                faces[i].vertices[j].z = positions[order[3 - j]].z();
            }
        }

        // Populate normals
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                faces[i].vertices[j].nx = NORMALS[i].x();
                faces[i].vertices[j].ny = NORMALS[i].y();
                faces[i].vertices[j].nz = NORMALS[i].z();
            }
        }

        // Populate UV
        double[] u = new double[24];
        double[] v = new double[24];
        for (int i = 0; i < 6; i++) {
            JsonObject faceObject = element.getAsJsonObject("faces").getAsJsonObject(SIDES[i]);
            int id = Utils.getIntElement(element, "texture");
            BBTexture texture = model.textures.get(id);

            faces[i].texture = texture;

            float[] uv = new float[4];
            Iterator<JsonElement> uvArray = faceObject.getAsJsonArray("uv").iterator();
            for (int j = 0; j < 4; j++) {
                uv[j] = uvArray.next().getAsFloat();
            }

            int rot = Utils.getIntElement(faceObject, "rotation");
            while (rot > 0) {
                roll(u, i * 4);
                roll(v, i * 4);
                rot -= 90;
            }

            faces[i].vertices[0].u = uv[0] / texture.uvWidth;
            faces[i].vertices[0].v = uv[3] / texture.uvHeight;
            faces[i].vertices[1].u = uv[2] / texture.uvWidth;
            faces[i].vertices[1].v = uv[3] / texture.uvHeight;
            faces[i].vertices[2].u = uv[2] / texture.uvWidth;
            faces[i].vertices[2].v = uv[1] / texture.uvHeight;
            faces[i].vertices[3].u = uv[0] / texture.uvWidth;
            faces[i].vertices[3].v = uv[1] / texture.uvHeight;
        }
    }

    @NotNull
    private Vector3f[] getPositions() {
        Vector3f adjustedFrom = this.from;
        Vector3f adjustedTo = this.to;

        Vector3f inflate = new Vector3f(this.inflate, this.inflate, this.inflate);
        adjustedFrom.sub(inflate);
        adjustedTo.sub(inflate);

        adjustedFrom.mul(1.0f / 16.0f);
        adjustedTo.mul(1.0f / 16.0f);

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
}
