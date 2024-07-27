package immersive_aircraft.entity.weapon;

import com.mojang.math.Axis;
import immersive_aircraft.Main;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix3f;
import org.joml.Vector3f;

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
        Matrix3f camera = new Matrix3f();

        if (vehicle.adaptPlayerRotation && Main.firstPersonGetter.isFirstPerson()) {
            camera.rotate(Axis.ZP.rotationDegrees(vehicle.getRoll()));
            camera.rotate(Axis.XP.rotationDegrees(vehicle.getXRot()));
        }

        camera.rotate(Axis.XP.rotationDegrees(pilot.getXRot()));
        camera.rotate(Axis.YP.rotationDegrees(pilot.getYRot() + 180.0f));

        return camera;
    }

    public void pointTo(VehicleEntity vehicle) {
        pointTo(vehicle, new Vector3f(0.0f, 0.0f, -1.0f));
    }

    public void pointTo(VehicleEntity vehicle, Vector3f normal) {
        screenToGlobal(vehicle, normal);

        // Convert into vehicle space
        Matrix3f vehicleTransform = new Matrix3f(vehicle.getVehicleNormalTransform());
        vehicleTransform.invert();
        normal.mul(vehicleTransform);

        // Convert into weapon space
        Matrix3f weaponTransform = new Matrix3f(weapon.getMount().transform());
        weaponTransform.invert();
        normal.mul(weaponTransform);

        yaw = (float) -Math.atan2(normal.x(), normal.z());
        pitch = (float) -Math.atan2(normal.y(), Math.sqrt(normal.x() * normal.x() + normal.z() * normal.z()));
    }

    public Vector3f screenToGlobal(VehicleEntity vehicle) {
        return screenToGlobal(vehicle, new Vector3f(0.0f, 0.0f, -1.0f));
    }

    public Vector3f screenToGlobal(VehicleEntity vehicle, Vector3f normal) {
        Entity pilot = vehicle.getGunner(weapon.getGunnerOffset());

        if (pilot != null) {
            Matrix3f camera = getCamera(vehicle, pilot);
            camera.invert();
            normal.mul(camera);
        }

        return normal;
    }
}
