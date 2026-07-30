package immersive_aircraft.entity.misc;

import net.minecraft.network.RegistryFriendlyByteBuf;
import org.joml.Matrix4f;

public record WeaponMount(Matrix4f transform, boolean blocking,
                          boolean enableRotation, float minYaw, float maxYaw, float minPitch, float maxPitch) {
    public static final WeaponMount EMPTY = new WeaponMount(new Matrix4f(), false, false, 0, 0, 0, 0);

    public WeaponMount(Matrix4f transform, boolean blocking) {
        this(transform, blocking, false, 0, 0, 0, 0);
    }

    public void encode(RegistryFriendlyByteBuf buffer) {
        float[] floatValues = new float[16];
        transform.get(floatValues);
        for (int i = 0; i < 16; i++) {
            buffer.writeFloat(floatValues[i]);
        }
        buffer.writeBoolean(blocking);
        buffer.writeBoolean(enableRotation);
        buffer.writeFloat(minYaw);
        buffer.writeFloat(maxYaw);
        buffer.writeFloat(minPitch);
        buffer.writeFloat(maxPitch);
    }

    public static WeaponMount decode(RegistryFriendlyByteBuf buffer) {
        float[] floatValues = new float[16];
        for (int i = 0; i < 16; i++) {
            floatValues[i] = buffer.readFloat();
        }
        Matrix4f matrix = new Matrix4f();
        matrix.set(floatValues);
        boolean blocking = buffer.readBoolean();
        boolean enableRotation = buffer.readBoolean();
        float minYaw = buffer.readFloat();
        float maxYaw = buffer.readFloat();
        float minPitch = buffer.readFloat();
        float maxPitch = buffer.readFloat();
        return new WeaponMount(matrix, blocking, enableRotation, minYaw, maxYaw, minPitch, maxPitch);
    }

    public enum Type {
        ROTATING,
        FRONT,
        DROP
    }
}