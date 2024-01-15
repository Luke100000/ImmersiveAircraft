package immersive_aircraft.entity.misc;

import com.mojang.math.Matrix4f;

public class WeaponMount {
    public static final WeaponMount EMPTY = new WeaponMount(Matrix4f.createScaleMatrix(0.0f, 0.0f, 0.0f));

    private final Matrix4f transform;

    public WeaponMount(Matrix4f transform) {
        this.transform = transform;
    }

    public Matrix4f getTransform() {
        return transform;
    }

    public enum Type {
        ROTATING,
        FRONT,
        DROP
    }
}
