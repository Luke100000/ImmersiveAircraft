package immersive_aircraft.entity;

import immersive_aircraft.entity.properties.AircraftProperties;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class GyrodyneEntity extends Rotorcraft {
    private final static float PUSH_SPEED = 0.1f;

    private final AircraftProperties properties = new AircraftProperties(this)
            .setYawSpeed(5.0f)
            .setPitchSpeed(0.3f)
            .setEngineSpeed(0.1f)
            .setVerticalSpeed(0.025f)
            .setGlideFactor(0.025f)
            .setDriftDrag(0.01f)
            .setLift(0.1f)
            .setRollFactor(8.0f)
            .setStabilizer(0.4f)
            .setWheelFriction(0.5f)
            .setWindSensitivity(0.005f)
            .setMass(5.0f);

    public GyrodyneEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public AircraftProperties getProperties() {
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
        // todo engine needs to be started by pushing, once reaching 1 the engine stays on
        super.updateController();

        // up and down
        setVelocity(getVelocity().add(0.0f, getEnginePower() * properties.getVerticalSpeed() * pressingInterpolatedY.getSmooth(), 0.0f));

        // get direction
        Vec3d direction = getDirection();

        // speed
        float sin = MathHelper.sin(getPitch() * ((float)Math.PI / 180));
        float thrust = (float)(Math.pow(getEnginePower(), 2.0) * properties.getEngineSpeed()) * sin;
        if (location == Location.ON_LAND) {
            thrust = PUSH_SPEED * pressingInterpolatedZ.getSmooth() * (pressingInterpolatedZ.getSmooth() > 0.0 ? 1.0f : 0.5f);
        }

        // accelerate
        setVelocity(getVelocity().add(direction.multiply(thrust)));
    }
}
