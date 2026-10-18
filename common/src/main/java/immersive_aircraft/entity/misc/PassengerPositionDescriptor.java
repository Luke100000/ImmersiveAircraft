package immersive_aircraft.entity.misc;

import com.google.gson.JsonObject;
import immersive_aircraft.entity.PassengerPosture;
import immersive_aircraft.util.Utils;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Locale;

public record PassengerPositionDescriptor(
        float x, float y, float z,
        float yaw, float pitch, float roll,
        PassengerPosture posture,
        float cameraOffsetX, float cameraOffsetY, float cameraOffsetZ
) {
    public static PassengerPositionDescriptor fromJson(JsonObject json) {
        float x = Utils.getFloatElement(json, "x");
        float y = Utils.getFloatElement(json, "y");
        float z = Utils.getFloatElement(json, "z");

        float yaw = Utils.getFloatElement(json, "yaw") / 180 * (float) Math.PI;
        float pitch = Utils.getFloatElement(json, "pitch") / 180 * (float) Math.PI;
        float roll = Utils.getFloatElement(json, "roll") / 180 * (float) Math.PI;

        PassengerPosture posture = json.has("posture")
                ? PassengerPosture.valueOf(json.get("posture").getAsString().toUpperCase(Locale.ROOT))
                : PassengerPosture.SEATED;

        float cameraOffsetX = Utils.getFloatElement(json, "cameraOffsetX");
        float cameraOffsetY = Utils.getFloatElement(json, "cameraOffsetY");
        float cameraOffsetZ = Utils.getFloatElement(json, "cameraOffsetZ");

        return new PassengerPositionDescriptor(x, y, z, yaw, pitch, roll, posture,
                cameraOffsetX, cameraOffsetY, cameraOffsetZ);
    }

    public static PassengerPositionDescriptor decode(FriendlyByteBuf buffer) {
        float x = buffer.readFloat();
        float y = buffer.readFloat();
        float z = buffer.readFloat();

        float yaw = buffer.readFloat();
        float pitch = buffer.readFloat();
        float roll = buffer.readFloat();

        PassengerPosture posture = buffer.readEnum(PassengerPosture.class);
        float cameraOffsetX = buffer.readFloat();
        float cameraOffsetY = buffer.readFloat();
        float cameraOffsetZ = buffer.readFloat();

        return new PassengerPositionDescriptor(x, y, z, yaw, pitch, roll, posture, cameraOffsetX, cameraOffsetY, cameraOffsetZ);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeFloat(x);
        buffer.writeFloat(y);
        buffer.writeFloat(z);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);
        buffer.writeFloat(roll);
        buffer.writeEnum(posture);
        buffer.writeFloat(cameraOffsetX);
        buffer.writeFloat(cameraOffsetY);
        buffer.writeFloat(cameraOffsetZ);
    }
}
