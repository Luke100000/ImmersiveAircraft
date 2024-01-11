package immersive_aircraft.util.bbmodel;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.math.Vector3f;
import immersive_aircraft.util.Utils;

public class BBObject {
    public final String uuid;
    public final String name;

    public final Vector3f origin;
    public final Vector3f rotation;

    public final int color;

    public final boolean export;
    public final boolean visibility;

    public BBObject(JsonObject element) {
        this.uuid = element.getAsJsonPrimitive("uuid").getAsString();
        this.name = element.getAsJsonPrimitive("name").getAsString();

        this.origin = parseVector(element, "origin");
        this.origin.mul(1.0f / 16.0f);
        this.rotation = parseVector(element, "rotation");
        this.rotation.mul((float) (Math.PI / 180.0));

        this.color = Utils.getIntElement(element, "color");

        this.export = Utils.getBooleanElement(element, "export");
        this.visibility = Utils.getBooleanElement(element, "visibility");
    }

    protected Vector3f parseVector(JsonObject element, String member) {
        JsonArray array = element.getAsJsonArray(member);
        if (array == null) {
            return new Vector3f();
        }
        return new Vector3f(
                array.get(0).getAsFloat(),
                array.get(1).getAsFloat(),
                array.get(2).getAsFloat()
        );
    }
}
