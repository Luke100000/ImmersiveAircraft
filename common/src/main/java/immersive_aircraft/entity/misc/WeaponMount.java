package immersive_aircraft.entity.misc;

import net.minecraft.network.RegistryFriendlyByteBuf;
import org.joml.Matrix4f;

public record WeaponMount(Matrix4f transform, boolean blocking) {
    public static final WeaponMount EMPTY = new WeaponMount(new Matrix4f(), false);

    public void encode(RegistryFriendlyByteBuf buffer) {
        float[] floatValues = new float[16];
        transform.get(floatValues);
        for (int i = 0; i < 16; i++) {
            buffer.writeFloat(floatValues[i]);
        }
        buffer.writeBoolean(blocking);
    }

    public static WeaponMount decode(RegistryFriendlyByteBuf buffer) {
        float[] floatValues = new float[16];
        for (int i = 0; i < 16; i++) {
            floatValues[i] = buffer.readFloat();
        }
        Matrix4f matrix = new Matrix4f();
        matrix.set(floatValues);
        return new WeaponMount(matrix, buffer.readBoolean());
    }

    public enum Type {
        ROTATING,
        FRONT,
        DROP
    }
}
