package immersive_aircraft.entity.misc;

import com.google.gson.JsonObject;
import immersive_aircraft.util.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record BoundingBoxDescriptor(float width, float height, float x, float y, float z) {
    public static BoundingBoxDescriptor fromJson(JsonObject json) {
        float width = Utils.getFloatElement(json, "width");
        float height = Utils.getFloatElement(json, "height");
        float x = Utils.getFloatElement(json, "x");
        float y = Utils.getFloatElement(json, "y");
        float z = Utils.getFloatElement(json, "z");
        return new BoundingBoxDescriptor(width, height, x, y, z);
    }

    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(width);
        buffer.writeFloat(height);
        buffer.writeFloat(x);
        buffer.writeFloat(y);
        buffer.writeFloat(z);
    }

    public static BoundingBoxDescriptor decode(RegistryFriendlyByteBuf buffer) {
        float width = buffer.readFloat();
        float height = buffer.readFloat();
        float x = buffer.readFloat();
        float y = buffer.readFloat();
        float z = buffer.readFloat();
        return new BoundingBoxDescriptor(width, height, x, y, z);
    }
}
