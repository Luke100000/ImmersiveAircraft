package immersive_aircraft.resources.bbmodel;

import com.google.gson.JsonObject;

import java.util.LinkedList;
import java.util.List;

public class BBBone extends BBObject {
    public final List<BBObject> children = new LinkedList<>();

    public BBBone(JsonObject element, BBModel model) {
        super(element);

        element.getAsJsonObject().get("children").getAsJsonArray().forEach(child -> {
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
