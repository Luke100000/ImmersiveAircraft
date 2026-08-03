package immersive_aircraft.entity.weapon;

import immersive_aircraft.compat.Matrix3f;
import immersive_aircraft.compat.Vec3f;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class RotationalManager {
    private final Weapon weapon;

    float yaw = 0.0f;
    float pitch = 0.0f;
    float roll = 0.0f;

    float lastYaw = 0.0f;
    float lastPitch = 0.0f;
    float lastRoll = 0.0f;

    public RotationalManager(Weapon weapon) {
        this.weapon = weapon;
    }

    private float turn(float diff) {
        if (diff > Math.PI) {
            diff -= (float) (Math.PI * 2.0f);
        } else if (diff < -Math.PI) {
            diff += (float) (Math.PI * 2.0f);
        }
        return diff;
    }

    public float getPitch(float tickDelta) {
        float diff = turn(pitch - lastPitch);
        return lastPitch + diff * tickDelta;
    }

    public float getYaw(float tickDelta) {
        float diff = turn(yaw - lastYaw);
        return lastYaw + diff * tickDelta;
    }

    public float getRoll(float tickDelta) {
        float diff = turn(roll - lastRoll);
        return lastRoll + diff * tickDelta;
    }

    public void tick() {
        lastYaw = yaw;
        lastPitch = pitch;
        lastRoll = roll;
    }

    public Matrix3f getCamera(VehicleEntity vehicle, Entity pilot) {
        Matrix3f camera = Matrix3f.scale(1.0f, 1.0f, 1.0f);

        if (vehicle.world.isRemote && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
            camera.multiply(immersive_aircraft.compat.Vec3f.POSITIVE_Z.getDegreesQuaternion(vehicle.getRoll()));
            camera.multiply(immersive_aircraft.compat.Vec3f.POSITIVE_X.getDegreesQuaternion(vehicle.getPitch()));
        }

        camera.multiply(immersive_aircraft.compat.Vec3f.POSITIVE_X.getDegreesQuaternion(pilot.rotationPitch));
        camera.multiply(immersive_aircraft.compat.Vec3f.POSITIVE_Y.getDegreesQuaternion(pilot.rotationYaw + 180.0f));

        return camera;
    }

    public void pointTo(VehicleEntity vehicle) {
        pointTo(vehicle, new Vec3f(0.0f, 0.0f, -1.0f));
    }

    public void pointTo(VehicleEntity vehicle, Vec3f normal) {
        screenToGlobal(vehicle, normal);

        // Convert into vehicle space
        Matrix3f vehicleTransform = new Matrix3f(vehicle.getVehicleNormalTransform());
        vehicleTransform.invert();
        normal.transform(vehicleTransform);

        // Convert into weapon space
        Matrix3f weaponTransform = new Matrix3f(weapon.getMount().transform());
        weaponTransform.invert();
        normal.transform(weaponTransform);

        yaw = (float) -Math.atan2(normal.getX(), normal.getZ());
        pitch = (float) -Math.atan2(normal.getY(), Math.sqrt(normal.getX() * normal.getX() + normal.getZ() * normal.getZ()));
    }

    public Vec3f screenToGlobal(VehicleEntity vehicle) {
        return screenToGlobal(vehicle, new Vec3f(0.0f, 0.0f, -1.0f));
    }

    public Vec3f screenToGlobal(VehicleEntity vehicle, Vec3f normal) {
        Entity pilot = vehicle.getGunner(weapon.getGunnerOffset());

        if (pilot != null) {
            Matrix3f camera = getCamera(vehicle, pilot);
            camera.invert();
            normal.transform(camera);
        }

        return normal;
    }
}
