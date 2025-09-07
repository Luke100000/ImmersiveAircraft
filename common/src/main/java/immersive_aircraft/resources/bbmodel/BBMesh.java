package immersive_aircraft.resources.bbmodel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import immersive_aircraft.Main;
import immersive_aircraft.util.Utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BBMesh extends BBObject implements BBFaceContainer {
    public final List<BBFace> faces = new LinkedList<>();

    private static Map<String, float[]> getArrayMap(JsonElement element, int size) {
        Map<String, float[]> vertices = new HashMap<>();
        element.getAsJsonObject().entrySet().forEach(entry -> {
            float[] v = new float[size];
            JsonArray j = entry.getValue().getAsJsonArray();
            for (int i = 0; i < size; i++) {
                v[i] = j.get(i).getAsFloat();
            }
            vertices.put(entry.getKey(), v);
        });
        return vertices;
    }

    private static float[] getNormal(List<String> vertexIdentifiers, Map<String, float[]> positions) {
        float[] p1 = positions.get(vertexIdentifiers.get(0));
        float[] p2 = positions.get(vertexIdentifiers.get(1));
        float[] p3 = positions.get(vertexIdentifiers.get(2));

        float[] v1 = new float[]{p2[0] - p1[0], p2[1] - p1[1], p2[2] - p1[2]};
        float[] v2 = new float[]{p3[0] - p1[0], p3[1] - p1[1], p3[2] - p1[2]};

        float[] normal = new float[]{
                v1[1] * v2[2] - v1[2] * v2[1],
                v1[2] * v2[0] - v1[0] * v2[2],
                v1[0] * v2[1] - v1[1] * v2[0]
        };

        float length = (float) Math.sqrt(normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2]);
        normal[0] /= length;
        normal[1] /= length;
        normal[2] /= length;

        return normal;
    }

    public BBMesh(JsonObject element, BBModel model) {
        super(element);

        // Get the vertex positions
        Map<String, float[]> positions = getArrayMap(element.get("vertices"), 3);

        element.get("faces").getAsJsonObject().entrySet().forEach(face -> {
            JsonObject faceObject = face.getValue().getAsJsonObject();

            if (!Utils.isNull(faceObject, "texture")) {
                // Get the texture
                int id = Utils.getIntElement(faceObject, "texture");
                BBTexture texture = model.getTexture(id);

                // Get the uv
                Map<String, float[]> uvs = getArrayMap(faceObject.get("uv"), 2);

                // Get the vertex identifiers spanning the face
                List<String> vertexIdentifiers = new LinkedList<>();
                for (JsonElement jsonElement : faceObject.getAsJsonArray("vertices")) {
                    vertexIdentifiers.add(jsonElement.getAsString());
                }

                if (vertexIdentifiers.size() != 4) {
                    Main.LOGGER.warn("Non-quad face found in model: {}!", model.id);
                } else {
                    // Get normal vector
                    float[] n = getNormal(vertexIdentifiers, positions);
                    int index = 0;
                    BBFace.BBVertex[] vertices = new BBFace.BBVertex[4];
                    for (String identifier : vertexIdentifiers) {
                        float[] uv = uvs.get(identifier);
                        float[] pos = positions.get(identifier);

                        float textureWidth = model.getTextureWidth(texture);
                        float textureHeight = model.getTextureHeight(texture);

                        BBFace.BBVertex vd = new BBFace.BBVertex();
                        vd.x = pos[0] / 16.0f;
                        vd.y = pos[1] / 16.0f;
                        vd.z = pos[2] / 16.0f;
                        vd.nx = n[0];
                        vd.ny = n[1];
                        vd.nz = n[2];
                        vd.u = uv[0] / textureWidth;
                        vd.v = uv[1] / textureHeight;
                        vertices[index++] = vd;
                    }

                    BBFace f = new BBFace(vertices);
                    f.texture = texture;
                    this.faces.add(f);
                }
            }
        });
    }

    @Override
    public Iterable<BBFace> getFaces() {
        return faces;
    }
}
