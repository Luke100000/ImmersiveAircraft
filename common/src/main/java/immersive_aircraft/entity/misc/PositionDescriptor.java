package immersive_aircraft.entity.misc;

import com.google.gson.JsonObject;
import immersive_aircraft.util.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.joml.Matrix4f;

public record PositionDescriptor(float x, float y, float z, float yaw, float pitch, float roll) {
    static PositionDescriptor fromJson(JsonObject json) {
        float x = Utils.getFloatElement(json, "x");
        float y = Utils.getFloatElement(json, "y");
        float z = Utils.getFloatElement(json, "z");
        float yaw = Utils.getFloatElement(json, "yaw") / 180 * (float) Math.PI;
        float pitch = Utils.getFloatElement(json, "pitch") / 180 * (float) Math.PI;
        float roll = Utils.getFloatElement(json, "roll") / 180 * (float) Math.PI;
        return new PositionDescriptor(x, y, z, yaw, pitch, roll);
    }

    public static PositionDescriptor decode(RegistryFriendlyByteBuf byteBuf) {
        float x = byteBuf.readFloat();
        float y = byteBuf.readFloat();
        float z = byteBuf.readFloat();
        float yaw = byteBuf.readFloat();
        float pitch = byteBuf.readFloat();
        float roll = byteBuf.readFloat();
        return new PositionDescriptor(x, y, z, yaw, pitch, roll);
    }

    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(x);
        buffer.writeFloat(y);
        buffer.writeFloat(z);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);
        buffer.writeFloat(roll);
    }

    public Matrix4f matrix() {
        Matrix4f matrix = new Matrix4f();
        matrix.translate(x, y, z);
        matrix.rotate(Utils.fromXYZ(pitch, yaw, roll));
        return matrix;
    }
}
