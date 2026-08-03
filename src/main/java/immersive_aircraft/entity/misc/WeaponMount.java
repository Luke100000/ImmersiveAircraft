package immersive_aircraft.entity.misc;

import immersive_aircraft.compat.Matrix4f;
import immersive_aircraft.compat.Vec3f;

/**
 * 1.20.1 WeaponMount port: a mutable transform (mount position/rotation relative to the
 * vehicle) plus the blocking flag (hidden in first person when the player is a passenger).
 */
public class WeaponMount {
    public static final WeaponMount EMPTY = new WeaponMount(Matrix4f.scale(1.0f, 1.0f, 1.0f), false);

    private final Matrix4f transform;
    private final boolean blocking;

    public WeaponMount(Matrix4f transform, boolean blocking) {
        this.transform = transform;
        this.blocking = blocking;
    }

    public Matrix4f transform() {
        return transform;
    }

    public boolean blocking() {
        return blocking;
    }

    /**
     * Mount from a position/rotation descriptor (the 1.20.1 aircraft json values):
     * translate(x, y, z) then rotate fromXYZ(pitch, yaw, roll) - angles in degrees.
     */
    public static WeaponMount of(float x, float y, float z, float yaw, float pitch, float roll, boolean blocking) {
        Matrix4f matrix = Matrix4f.translate(x, y, z);
        matrix.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(roll));
        matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(yaw));
        matrix.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(pitch));
        return new WeaponMount(matrix, blocking);
    }

    public static WeaponMount of(float x, float y, float z, float yaw, float pitch, float roll) {
        return of(x, y, z, yaw, pitch, roll, false);
    }

    public enum Type {
        ROTATING,
        FRONT,
        DROP
    }
}
