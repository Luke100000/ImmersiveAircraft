package immersive_aircraft.resources;

import immersive_aircraft.compat.Matrix3f;
import immersive_aircraft.compat.Matrix4f;
import immersive_aircraft.compat.Vec3f;
import immersive_aircraft.compat.Vector4f;
import immersive_aircraft.resources.bbmodel.BBBone;
import immersive_aircraft.resources.bbmodel.BBFace;
import immersive_aircraft.resources.bbmodel.BBFaceContainer;
import immersive_aircraft.resources.bbmodel.BBModel;
import immersive_aircraft.resources.bbmodel.BBObject;
import immersive_aircraft.util.obj.Face;
import immersive_aircraft.util.obj.FaceVertex;
import immersive_aircraft.util.obj.Mesh;
import immersive_aircraft.util.obj.VertexColor;
import immersive_aircraft.util.obj.VertexNormal;
import immersive_aircraft.util.obj.VertexPosition;
import immersive_aircraft.util.obj.VertexTexture;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bridges Blockbench .bbmodel files into the 1.16.5-style {@link Mesh} rendering used by
 * the ported renderers.
 *
 * For every bone (and for explicitly extracted elements, see {@link #SPECIAL_ELEMENTS})
 * a Mesh is produced with the static pose fully baked into the vertices: the whole
 * ancestor chain of bone pivots/rotations plus the element's own pivot/rotation is
 * applied at load time, so renderers draw meshes in model space and apply procedural
 * animations around the bone pivots exposed by {@link Baked#pivots} (1.20.1 instead
 * walked the bone tree every frame; baking keeps our GlStateManager renderers simple).
 *
 * Mesh keys: "boneName" for texture 0, "boneName#&lt;index&gt;" for other textures,
 * or the element name for special elements (extracted from their bone's mesh).
 */
public class BBModelBridge {
    public static final String ROOT_BONE = "root";

    /**
     * Elements that need individual treatment (sail animation) and are therefore
     * extracted from their bone mesh into element-named meshes.
     */
    private static final Map<String, Set<String>> SPECIAL_ELEMENTS = new HashMap<>();

    static {
        SPECIAL_ELEMENTS.put("objects/warship.bbmodel", new HashSet<>(Arrays.asList(
                "net", "tail_fin_flag", "nose_fin_top_flag", "nose_fin_bottom_flag"
        )));
    }

    public static class Baked {
        public final Map<String, Mesh> meshes = new HashMap<>();
        public final Map<String, Integer> meshTexture = new HashMap<>();
        public final Map<String, float[]> pivots = new HashMap<>();
        public final Map<String, List<String>> subtreeMeshKeys = new HashMap<>();
        public final List<ResourceLocation> textures = new ArrayList<>();
    }

    private static final Map<ResourceLocation, Baked> CACHE = new HashMap<>();

    public static Baked get(ResourceLocation id) {
        Baked baked = CACHE.get(id);
        if (baked == null) {
            baked = bake(id);
            CACHE.put(id, baked);
        }
        return baked;
    }

    public static void clearCache() {
        CACHE.clear();
    }

    public static Mesh getMesh(ResourceLocation id, String key) {
        return get(id).meshes.get(key);
    }

    public static float[] getPivot(ResourceLocation id, String bone) {
        return get(id).pivots.get(bone);
    }

    public static List<String> getSubtreeMeshKeys(ResourceLocation id, String bone) {
        List<String> keys = get(id).subtreeMeshKeys.get(bone);
        return keys == null ? new ArrayList<>() : keys;
    }

    public static ResourceLocation getTexture(ResourceLocation id, int index) {
        List<ResourceLocation> textures = get(id).textures;
        return index < textures.size() ? textures.get(index) : textures.get(0);
    }

    private static Baked bake(ResourceLocation id) {
        Baked baked = new Baked();
        BBModel model = BBModelLoader.getModel(id);
        if (model == null) {
            return baked;
        }
        for (int i = 0; i < model.textures.size(); i++) {
            baked.textures.add(model.textures.get(i).location);
        }

        Set<String> special = SPECIAL_ELEMENTS.getOrDefault(id.getPath(), new HashSet<>());

        Matrix4f identity = Matrix4f.scale(1.0f, 1.0f, 1.0f);
        Matrix3f identityNormal = Matrix3f.scale(1.0f, 1.0f, 1.0f);
        List<String> rootKeys = new ArrayList<>();
        for (BBObject object : model.root) {
            visit(object, identity, identityNormal, ROOT_BONE, baked, special, rootKeys);
        }
        baked.subtreeMeshKeys.put(ROOT_BONE, rootKeys);

        return baked;
    }

    /**
     * @param parentXf full transform of the frame the object lives in (all ancestors applied)
     * @param parentNf rotation part of parentXf
     * @param bone     name of the bone the object belongs to
     * @param keys     collects all mesh keys of the visited subtree
     */
    private static void visit(BBObject object, Matrix4f parentXf, Matrix3f parentNf, String bone, Baked baked, Set<String> special, List<String> keys) {
        Matrix4f xf = parentXf.copy();
        Matrix3f nf = parentNf.copy();

        // translate to origin, apply initial rotation (fromXYZ: x first, then y, then z,
        // which with post-multiplied quaternions means multiplying z, y, x in this order)
        xf.multiplyByTranslation(object.origin[0], object.origin[1], object.origin[2]);
        multiplyRotation(xf, nf, object.rotation);

        if (object instanceof BBBone) {
            BBBone boneObject = (BBBone) object;

            // model-space pivot of this bone: xf includes this bone's rotation around the
            // origin, and a rotation around the origin maps the origin to itself, so
            // transforming the origin by xf yields the correct pivot.
            Vector4f pivot = new Vector4f(object.origin[0], object.origin[1], object.origin[2], 1.0f);
            pivot.transform(xf);
            baked.pivots.put(object.name, new float[]{pivot.getX(), pivot.getY(), pivot.getZ()});

            xf.multiplyByTranslation(-object.origin[0], -object.origin[1], -object.origin[2]);

            List<String> subtreeKeys = new ArrayList<>();
            for (BBObject child : boneObject.children) {
                visit(child, xf, nf, object.name, baked, special, subtreeKeys);
            }
            baked.subtreeMeshKeys.put(object.name, subtreeKeys);
            keys.addAll(subtreeKeys);
        } else if (object instanceof BBFaceContainer) {
            String name = special.contains(object.name) ? object.name : bone;
            BBFaceContainer container = (BBFaceContainer) object;
            for (BBFace face : container.getFaces()) {
                int texture = face.texture == null ? 0 : face.texture.index;
                String key = texture == 0 ? name : name + "#" + texture;
                Mesh mesh = baked.meshes.computeIfAbsent(key, k -> new Mesh());
                baked.meshTexture.put(key, texture);
                if (!keys.contains(key)) {
                    keys.add(key);
                }

                Face objFace = new Face();
                for (BBFace.BBVertex v : face.vertices) {
                    Vector4f position = new Vector4f(v.x, v.y, v.z, 1.0f);
                    position.transform(xf);
                    Vec3f normal = new Vec3f(v.nx, v.ny, v.nz);
                    normal.transform(nf);

                    FaceVertex vertex = new FaceVertex();
                    vertex.v = new VertexPosition(position.getX(), position.getY(), position.getZ());
                    vertex.t = new VertexTexture(v.u, v.v);
                    vertex.n = new VertexNormal(normal.getX(), normal.getY(), normal.getZ());
                    vertex.c = new VertexColor(1.0f, 1.0f, 1.0f, 1.0f);
                    objFace.add(vertex);
                }
                mesh.faces.add(objFace);
            }
        }
    }

    private static void multiplyRotation(Matrix4f xf, Matrix3f nf, float[] rotation) {
        float deg = (float) (180.0 / Math.PI);
        xf.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(rotation[2] * deg));
        xf.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotation[1] * deg));
        xf.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(rotation[0] * deg));
        nf.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(rotation[2] * deg));
        nf.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotation[1] * deg));
        nf.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(rotation[0] * deg));
    }
}
