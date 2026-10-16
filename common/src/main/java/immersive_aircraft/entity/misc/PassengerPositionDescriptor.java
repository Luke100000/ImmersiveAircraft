package immersive_aircraft.entity.misc;

import com.google.gson.JsonObject;
import immersive_aircraft.entity.PassengerPosture;
import immersive_aircraft.util.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.Locale;

public record PassengerPositionDescriptor(
        float x, float y, float z,
        float yaw, float pitch, float roll,
        PassengerPosture posture
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

        return new PassengerPositionDescriptor(x, y, z, yaw, pitch, roll, posture);
    }

    public static PassengerPositionDescriptor decode(RegistryFriendlyByteBuf buffer) {
        float x = buffer.readFloat();
        float y = buffer.readFloat();
        float z = buffer.readFloat();

        float yaw = buffer.readFloat();
        float pitch = buffer.readFloat();
        float roll = buffer.readFloat();

        PassengerPosture posture = buffer.readEnum(PassengerPosture.class);

        return new PassengerPositionDescriptor(x, y, z, yaw, pitch, roll, posture);
    }

    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(x);
        buffer.writeFloat(y);
        buffer.writeFloat(z);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);
        buffer.writeFloat(roll);
        buffer.writeEnum(posture);
    }
}
