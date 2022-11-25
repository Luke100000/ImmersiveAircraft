package immersive_aircraft.entity;

import immersive_aircraft.entity.properties.AircraftProperties;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class AirshipEntity extends Rotorcraft {
    private final AircraftProperties properties = new AircraftProperties(this)
            .setYawSpeed(0.5f)
            .setEngineSpeed(0.005f)
            .setVerticalSpeed(0.005f)
            .setGlideFactor(0.025f)
            .setDriftDrag(0.01f)
            .setLift(0.1f)
            .setWindSensitivity(0.0025f)
            .setRollFactor(1.0f)
            .setWheelFriction(0.5f);

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

        // speed
        if (movementY != 0) {
            setVelocity(getVelocity().add(0.0f, getEnginePower() * properties.getVerticalSpeed() * movementY, 0.0f));
        }

        // get pointing direction
        //code duplication, xy direction, common subclass called helicopter?
        Vec3d direction = getDirection();

        // accelerate
        float thrust = (float)(Math.pow(getEnginePower(), 5.0) * properties.getEngineSpeed()) * movementZ;
        setVelocity(getVelocity().add(direction.multiply(thrust)));
    }
}
