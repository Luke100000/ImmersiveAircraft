package immersive_aircraft.entity.misc;

import com.google.gson.JsonObject;
import immersive_aircraft.util.Utils;
import net.minecraft.network.FriendlyByteBuf;

public record TrailDescriptor(float x, float y, float z, float size, float rotate, float gray, int length) {
    static TrailDescriptor fromJson(JsonObject json) {
        float x = Utils.getFloatElement(json, "x");
        float y = Utils.getFloatElement(json, "y");
        float z = Utils.getFloatElement(json, "z");
        float size = Utils.getFloatElement(json, "size");
        float rotate = Utils.getFloatElement(json, "rotate");
        float gray = Utils.getFloatElement(json, "gray");
        int length = Utils.getIntElement(json, "length");
        return new TrailDescriptor(x, y, z, size, rotate, gray, length);
    }

    public static TrailDescriptor decode(FriendlyByteBuf buf) {
        float x = buf.readFloat();
        float y = buf.readFloat();
        float z = buf.readFloat();
        float size = buf.readFloat();
        float rotate = buf.readFloat();
        float gray = buf.readFloat();
        int length = buf.readInt();
        return new TrailDescriptor(x, y, z, size, rotate, gray, length);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
        buf.writeFloat(size);
        buf.writeFloat(rotate);
        buf.writeFloat(gray);
        buf.writeInt(length);
    }
}
