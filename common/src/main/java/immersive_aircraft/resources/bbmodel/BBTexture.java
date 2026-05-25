package immersive_aircraft.resources.bbmodel;

import com.google.gson.JsonObject;
import immersive_aircraft.util.Utils;
import net.minecraft.resources.Identifier;

public class BBTexture {
    public static final BBTexture MISSING = new BBTexture();

    public final String uuid;
    public final String id;
    public final String name;
    public final int width;
    public final int height;
    public final int uvWidth;
    public final int uvHeight;

    public final Identifier location;

    public BBTexture() {
        this.uuid = "";
        this.id = "";
        this.name = "";
        this.width = 16;
        this.height = 16;
        this.uvWidth = 16;
        this.uvHeight = 16;

        this.location = Identifier.parse("missing");
    }

    public BBTexture(JsonObject element, Identifier identifier) {
        this.uuid = element.getAsJsonPrimitive("uuid").getAsString();
        this.id = element.getAsJsonPrimitive("id").getAsString();
        this.name = element.getAsJsonPrimitive("name").getAsString();
        this.width = Utils.getIntElement(element, "width", 16);
        this.height = Utils.getIntElement(element, "height", 16);
        this.uvWidth = Utils.getIntElement(element, "uv_width", 16);
        this.uvHeight = Utils.getIntElement(element, "uv_height", 16);

        if (this.name.contains(":")) {
            this.location = Identifier.parse(this.name);
        } else {
            this.location = Identifier.fromNamespaceAndPath(identifier.getNamespace(), "textures/entity/" + this.name);
        }
    }
}
