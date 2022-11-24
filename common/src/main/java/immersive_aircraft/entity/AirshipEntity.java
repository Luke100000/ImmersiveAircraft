package immersive_aircraft.entity;

import immersive_aircraft.entity.properties.AircraftProperties;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class AirshipEntity extends EngineAircraft {
    private final AircraftProperties properties = new AircraftProperties(this)
            .setYawSpeed(0.5f)
            .setEngineSpeed(0.0025f)
            .setVerticalSpeed(0.025f)
            .setGlideFactor(0.025f)
            .setDriftDrag(0.01f)
            .setWindSensitivity(0.00025f)
            .setRollFactor(1.0f)
            .setWheelFriction(0.5f);

    public AirshipEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    AircraftProperties getProperties() {
        return properties;
    }

    List<List<Vec3d>> PASSENGER_POSITIONS = List.of(
            List.of(
                    new Vec3d(0.0f, -0.4f, 0.2f)
            ),
            List.of(
                    new Vec3d(0.0f, -0.4f, 0.2f),
                    new Vec3d(0.0f, -0.4f, -0.6f)
            )
    );

    protected List<List<Vec3d>> getPassengerPositions() {
        return PASSENGER_POSITIONS;
    }

    @Override
    protected float getGravity() {
        return getEnginePower() == 1.0f ? 0.0f : super.getGravity();
    }

    @Override
    void updateController() {
        if (!hasPassengers()) {
            setEngineTarget(0.0f);
            return;
        } else {
            setEngineTarget(1.0f);
        }

        super.updateController();

        // speed
        if (movementY != 0) {
            setVelocity(getVelocity().add(0.0f, getEnginePower() * properties.getVerticalSpeed() * movementY, 0.0f));
        }

        // get pointing direction
        Vec3d direction = new Vec3d(
                MathHelper.sin(-getYaw() * ((float)Math.PI / 180)),
                0.0,
                MathHelper.cos(getYaw() * ((float)Math.PI / 180))
        ).normalize();

        // accelerate
        float thrust = (float)(Math.pow(getEnginePower(), 5.0) * properties.getEngineSpeed()) * movementZ;
        setVelocity(getVelocity().add(direction.multiply(thrust)));
    }
}
