package immersive_aircraft.entity.misc;

import com.google.gson.JsonObject;
import immersive_aircraft.util.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.joml.Matrix4f;

public record PositionDescriptor(float x, float y, float z, float yaw, float pitch, float roll,
                                 boolean enableRotation, float minYaw, float maxYaw, float minPitch, float maxPitch) {
    static PositionDescriptor fromJson(JsonObject json) {
        float x = Utils.getFloatElement(json, "x");
        float y = Utils.getFloatElement(json, "y");
        float z = Utils.getFloatElement(json, "z");
        float yaw = Utils.getFloatElement(json, "yaw") / 180 * (float) Math.PI;
        float pitch = Utils.getFloatElement(json, "pitch") / 180 * (float) Math.PI;
        float roll = Utils.getFloatElement(json, "roll") / 180 * (float) Math.PI;
        boolean enableRotation = json.has("enableRotation") && json.get("enableRotation").getAsBoolean();
        float minYaw = Utils.getFloatElement(json, "minYaw", 0);
        float maxYaw = Utils.getFloatElement(json, "maxYaw", 0);
        float minPitch = Utils.getFloatElement(json, "minPitch", 0);
        float maxPitch = Utils.getFloatElement(json, "maxPitch", 0);
        return new PositionDescriptor(x, y, z, yaw, pitch, roll, enableRotation, minYaw, maxYaw, minPitch, maxPitch);
    }

    public static PositionDescriptor decode(RegistryFriendlyByteBuf byteBuf) {
        float x = byteBuf.readFloat();
        float y = byteBuf.readFloat();
        float z = byteBuf.readFloat();
        float yaw = byteBuf.readFloat();
        float pitch = byteBuf.readFloat();
        float roll = byteBuf.readFloat();
        boolean enableRotation = byteBuf.readBoolean();
        float minYaw = byteBuf.readFloat();
        float maxYaw = byteBuf.readFloat();
        float minPitch = byteBuf.readFloat();
        float maxPitch = byteBuf.readFloat();
        return new PositionDescriptor(x, y, z, yaw, pitch, roll, enableRotation, minYaw, maxYaw, minPitch, maxPitch);
    }

    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(x);
        buffer.writeFloat(y);
        buffer.writeFloat(z);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);
        buffer.writeFloat(roll);
        buffer.writeBoolean(enableRotation);
        buffer.writeFloat(minYaw);
        buffer.writeFloat(maxYaw);
        buffer.writeFloat(minPitch);
        buffer.writeFloat(maxPitch);
    }

    public Matrix4f matrix() {
        Matrix4f matrix = new Matrix4f();
        matrix.translate(x, y, z);
        matrix.rotate(Utils.fromXYZ(pitch, yaw, roll));
        return matrix;
    }
}