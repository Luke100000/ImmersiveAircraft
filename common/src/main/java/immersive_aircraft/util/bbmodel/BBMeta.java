package immersive_aircraft.util.bbmodel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class BBMeta {
    public final String formatVersion;
    public final String modelFormat;
    public final boolean boxUv;

    public BBMeta(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        this.formatVersion = object.get("format_version").getAsString();
        this.modelFormat = object.get("model_format").getAsString();
        this.boxUv = object.get("box_uv").getAsBoolean();
    }
}
