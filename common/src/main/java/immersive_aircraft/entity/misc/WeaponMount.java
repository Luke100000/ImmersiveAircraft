package immersive_aircraft.entity.misc;

import com.mojang.math.Matrix4f;
import net.minecraft.network.FriendlyByteBuf;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public record WeaponMount(Matrix4f transform, boolean blocking) {
    public static final WeaponMount EMPTY = new WeaponMount(Matrix4f.createScaleMatrix(1.0f, 1.0f, 1.0f), false);

    public void encode(FriendlyByteBuf buffer) {
        FloatBuffer floatValues = MemoryUtil.memAllocFloat(16);
        transform.store(floatValues);
        for (int i = 0; i < 16; i++) {
            buffer.writeFloat(floatValues.get(i));
        }
        buffer.writeBoolean(blocking);
    }

    public static WeaponMount decode(FriendlyByteBuf buffer) {
        FloatBuffer floatValues = MemoryUtil.memAllocFloat(16);
        for (int i = 0; i < 16; i++) {
            floatValues.put(buffer.readFloat());
        }
        Matrix4f matrix = new Matrix4f();
        matrix.load(floatValues);
        return new WeaponMount(matrix, buffer.readBoolean());
    }

    public enum Type {
        ROTATING,
        FRONT,
        DROP
    }
}
