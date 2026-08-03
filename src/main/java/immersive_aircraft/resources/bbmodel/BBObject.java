package immersive_aircraft.resources.bbmodel;

import com.google.gson.JsonObject;

public class BBObject {
    public final String uuid;
    public final String name;

    /**
     * Blockbench pivot, in blocks (1/16 units).
     */
    public final float[] origin;
    /**
     * Initial rotation, in radians (x, y, z).
     */
    public final float[] rotation;

    public final int color;

    public final boolean export;
    public final boolean visibility;

    public BBObject(JsonObject element) {
        this.uuid = element.getAsJsonPrimitive("uuid").getAsString();
        this.name = element.getAsJsonPrimitive("name").getAsString();

        this.origin = BBUtils.parseVector(element, "origin");
        this.origin[0] /= 16.0f;
        this.origin[1] /= 16.0f;
        this.origin[2] /= 16.0f;
        this.rotation = BBUtils.parseVector(element, "rotation");
        this.rotation[0] *= (float) (Math.PI / 180.0);
        this.rotation[1] *= (float) (Math.PI / 180.0);
        this.rotation[2] *= (float) (Math.PI / 180.0);

        this.color = BBUtils.getIntElement(element, "color");

        this.export = BBUtils.getBooleanElement(element, "export");
        this.visibility = BBUtils.getBooleanElement(element, "visibility");
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }
}
