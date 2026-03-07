package immersive_aircraft.resources.bbmodel;

import com.google.gson.JsonObject;
import immersive_aircraft.util.Utils;

import java.util.LinkedList;
import java.util.List;

public class BBBone extends BBObject {
    public final List<BBObject> children = new LinkedList<>();

    public final boolean globalRotation;

    public BBBone(JsonObject element, BBModel model) {
        this(model.uuidToGroup.getOrDefault(element.getAsJsonPrimitive("uuid").getAsString(), element), model, element);
    }

    public BBBone(JsonObject element, BBModel model, JsonObject outlineElement) {
        super(element);

        this.globalRotation = Utils.getBooleanElement(element, "rotation_global", false);

        outlineElement.getAsJsonObject().get("children").getAsJsonArray().forEach(child -> {
            if (child.isJsonObject()) {
                this.children.add(new BBBone(child.getAsJsonObject(), model));
            } else {
                BBObject object = model.objects.get(child.getAsString());
                if (object != null) {
                    this.children.add(object);
                }
            }
        });
    }
}
