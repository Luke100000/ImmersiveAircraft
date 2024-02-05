package immersive_aircraft.entity.misc;

import com.google.gson.JsonObject;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import immersive_aircraft.util.Utils;
import net.minecraft.network.FriendlyByteBuf;

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

    public static PositionDescriptor decode(FriendlyByteBuf byteBuf) {
        float x = byteBuf.readFloat();
        float y = byteBuf.readFloat();
        float z = byteBuf.readFloat();
        float yaw = byteBuf.readFloat();
        float pitch = byteBuf.readFloat();
        float roll = byteBuf.readFloat();
        return new PositionDescriptor(x, y, z, yaw, pitch, roll);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeFloat(x);
        buffer.writeFloat(y);
        buffer.writeFloat(z);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);
        buffer.writeFloat(roll);
    }

    public Matrix4f matrix() {
        Matrix4f matrix = Matrix4f.createTranslateMatrix(x, y, z);
        Quaternion quaternion = Quaternion.fromXYZ(yaw, pitch, roll);
        matrix.multiply(new Matrix4f(quaternion));
        return matrix;
    }
}
