package immersive_aircraft.entity;

import immersive_aircraft.entity.properties.AircraftProperties;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class AirshipEntity extends Rotorcraft {
    private final AircraftProperties properties = new AircraftProperties(this)
            .setYawSpeed(5.0f)
            .setEngineSpeed(0.025f)
            .setVerticalSpeed(0.025f)
            .setGlideFactor(0.0f)
            .setDriftDrag(0.01f)
            .setLift(0.1f)
            .setRollFactor(5.0f)
            .setWheelFriction(0.5f)
            .setWindSensitivity(0.0025f)
            .setMass(10.0f);

    public AirshipEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public AircraftProperties getProperties() {
        return properties;
    }

    List<List<Vec3d>> PASSENGER_POSITIONS = List.of(
            List.of(
                    new Vec3d(0.0f, -0.4f, 0.0f)
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
        super.updateController();

        // up and down
        setVelocity(getVelocity().add(0.0f, getEnginePower() * properties.getVerticalSpeed() * pressingInterpolatedY.getSmooth(), 0.0f));

        // get pointing direction
        Vec3d direction = getDirection();

        // accelerate
        float thrust = (float)(Math.pow(getEnginePower(), 5.0) * properties.getEngineSpeed()) * pressingInterpolatedZ.getSmooth();
        setVelocity(getVelocity().add(direction.multiply(thrust)));
    }
}
