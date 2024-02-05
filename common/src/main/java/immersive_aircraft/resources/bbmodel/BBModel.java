package immersive_aircraft.resources.bbmodel;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class BBModel {
    public final BBMeta meta;
    public final List<BBTexture> textures = new LinkedList<>();
    public final LinkedList<BBObject> root;
    public final HashMap<String, BBObject> objects;
    public final List<BBAnimation> animations = new LinkedList<>();

    public BBModel(JsonObject model) {
        this.meta = new BBMeta(model.get("meta"));
        this.root = new LinkedList<>();
        this.objects = new HashMap<>();

        model.get("textures").getAsJsonArray().forEach(element -> {
            BBTexture texture = new BBTexture(element.getAsJsonObject());
            this.textures.add(texture);
        });

        model.get("elements").getAsJsonArray().forEach(element -> {
            String type = element.getAsJsonObject().get("type").getAsString();
            if (type.equals("cube")) {
                BBObject object = new BBCube(element.getAsJsonObject(), this);
                this.objects.put(object.uuid, object);
            }
        });

        model.get("outliner").getAsJsonArray().forEach(element -> {
            if (element.isJsonPrimitive()) {
                this.root.add(this.objects.get(element.getAsString()));
            } else {
                BBObject bone = new BBBone(element.getAsJsonObject(), this);
                this.root.add(bone);
                this.objects.put(bone.uuid, bone);
            }
        });

        if (model.has("animations")) {
            model.get("animations").getAsJsonArray().forEach(element -> {
                BBAnimation animation = new BBAnimation(element.getAsJsonObject());
                this.animations.add(animation);
            });
        }
    }
}
