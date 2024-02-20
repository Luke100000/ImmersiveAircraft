package immersive_aircraft.resources.bbmodel;

import com.google.gson.JsonObject;
import immersive_aircraft.Main;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static immersive_aircraft.resources.bbmodel.BBTexture.MISSING;

public class BBModel {
    public final BBMeta meta;
    public final List<BBTexture> textures = new LinkedList<>();
    public final LinkedList<BBObject> root;
    public final HashMap<String, BBObject> objects;
    public final HashMap<String, BBObject> objectsByName;
    public final List<BBAnimation> animations = new LinkedList<>();

    public BBModel(JsonObject model) {
        this.meta = new BBMeta(model.get("meta"));
        this.root = new LinkedList<>();
        this.objects = new HashMap<>();
        this.objectsByName = new HashMap<>();

        model.get("textures").getAsJsonArray().forEach(element -> {
            BBTexture texture = new BBTexture(element.getAsJsonObject());
            this.textures.add(texture);
        });

        model.get("elements").getAsJsonArray().forEach(element -> {
            String type = element.getAsJsonObject().get("type").getAsString();
            if (type.equals("cube")) {
                BBObject object = new BBCube(element.getAsJsonObject(), this);
                this.objects.put(object.uuid, object);
                this.objectsByName.put(object.name, object);
            } else if (type.equals("mesh")) {
                BBObject object = new BBMesh(element.getAsJsonObject(), this);
                this.objects.put(object.uuid, object);
                this.objectsByName.put(object.name, object);
            } else {
                Main.LOGGER.warn("Unknown object type {}", type);
            }
        });

        model.get("outliner").getAsJsonArray().forEach(element -> {
            if (element.isJsonPrimitive()) {
                BBObject object = this.objects.get(element.getAsString());
                if (object != null) {
                    this.root.add(object);
                } else {
                    Main.LOGGER.warn("Object with uuid {} not found", element.getAsString());
                }
            } else {
                BBObject bone = new BBBone(element.getAsJsonObject(), this);
                this.root.add(bone);
                this.objects.put(bone.uuid, bone);
                this.objectsByName.put(bone.name, bone);
            }
        });

        if (model.has("animations")) {
            model.get("animations").getAsJsonArray().forEach(element -> {
                BBAnimation animation = new BBAnimation(element.getAsJsonObject());
                this.animations.add(animation);
            });
        }
    }

    public BBTexture getTexture(int id) {
        return id < textures.size() ? textures.get(id) : MISSING;
    }
}
