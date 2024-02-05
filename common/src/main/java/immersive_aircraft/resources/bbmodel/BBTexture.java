package immersive_aircraft.resources.bbmodel;

import com.google.gson.JsonObject;
import immersive_aircraft.util.Utils;

public class BBTexture {
    public final String uuid;
    public final String id;
    public final String name;
    public final int width;
    public final int height;
    public final int uvWidth;
    public final int uvHeight;

    public BBTexture(JsonObject element) {
        this.uuid = element.getAsJsonPrimitive("uuid").getAsString();
        this.id = element.getAsJsonPrimitive("id").getAsString();
        this.name = element.getAsJsonPrimitive("name").getAsString();
        this.width = Utils.getIntElement(element, "width", 16);
        this.height = Utils.getIntElement(element, "height",16);
        this.uvWidth = Utils.getIntElement(element, "uv_width",16);
        this.uvHeight = Utils.getIntElement(element, "uv_height",16);
    }
}
