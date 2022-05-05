package immersive_airships.entity;

import immersive_airships.entity.properties.AircraftProperties;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GyrodyneEntity extends EngineAircraft {
    private final AircraftProperties properties = new AircraftProperties(this)
            .setYawSpeed(0.5f)
            .setPitchSpeed(0.5f)
            .setPushSpeed(0.025f)
            .setEngineSpeed(0.1f)
            .setVerticalSpeed(0.025f)
            .setGlideFactor(0.2f)
            .setMaxPitch(10)
            .setDriftDrag(0.01f)
            .setLift(0.1f)
            .setWindSensitivity(0.001f)
            .setRollFactor(8.0f)
            .setWheelFriction(0.5f);

    public GyrodyneEntity(EntityType<? extends AirshipEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    AircraftProperties getProperties() {
        return properties;
    }

    @Override
    int getPassengerSpace() {
        return 2;
    }

    @Override
    protected float getGravity() {
        return getEnginePower() == 1.0f ? 0.0f : super.getGravity();
    }

    @Override
    void updateController() {
        if (!hasPassengers()) {
            return;
        }

        super.updateController();

        //engine
        if (pressingUp) {
            setEngineTarget(1.0f);
        } else if (pressingDown && location != Location.IN_AIR) {
            setEngineTarget(0.0f);
        }

        // speed
        if (pressingUp) {
            setVelocity(getVelocity().add(0.0f, getEnginePower() * properties.getVerticalSpeed(), 0.0f));
        } else if (pressingDown) {
            setVelocity(getVelocity().add(0.0f, -getEnginePower() * properties.getVerticalSpeed(), 0.0f));
        }

        // get pointing direction
        Vec3d direction = new Vec3d(
                MathHelper.sin(-getYaw() * ((float)Math.PI / 180)),
                0.0,
                MathHelper.cos(getYaw() * ((float)Math.PI / 180))
        ).normalize();

        // speed
        float sin = MathHelper.sin(getPitch() * ((float)Math.PI / 180));
        float thrust = (float)(Math.pow(getEnginePower(), 5.0) * properties.getEngineSpeed()) * sin;
        if (location == Location.ON_LAND) {
            if (pressingForward) {
                thrust = properties.getPushSpeed();
            } else if (pressingBack) {
                thrust = -properties.getPushSpeed() * 0.5f;
            }
        }

        // accelerate
        setVelocity(getVelocity().add(direction.multiply(thrust)));
    }
}
