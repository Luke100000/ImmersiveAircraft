package immersive_aircraft.entity;

import immersive_aircraft.entity.misc.AircraftProperties;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Implements airplane like physics properties and accelerated towards
 */
public abstract class AirplaneEntity extends EngineAircraft {
    private final AircraftProperties properties = new AircraftProperties(this)
            .setYawSpeed(5.0f)
            .setPitchSpeed(4.0f)
            .setEngineSpeed(0.0225f)
            .setGlideFactor(0.05f)
            .setDriftDrag(0.01f)
            .setLift(0.15f)
            .setRollFactor(45.0f)
            .setGroundPitch(4.0f)
            .setWindSensitivity(0.025f)
            .setMass(1.0f);

    public AirplaneEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected boolean useAirplaneControls() {
        return true;
    }

    @Override
    public AircraftProperties getProperties() {
        return properties;
    }

    @Override
    protected float getGroundVelocityDecay() {
        return falloffGroundVelocityDecay(0.9f);
    }

    @Override
    protected float getGravity() {
        Vec3d direction = getDirection();
        float speed = (float)((float)getVelocity().length() * (1.0f - Math.abs(direction.getY())));
        return Math.max(0.0f, 1.0f - speed * 1.5f) * super.getGravity();
    }

    protected float getBrakeFactor() {
        return 0.95f;
    }

    @Override
    protected void updateController() {
        if (!hasPassengers()) {
            return;
        }

        super.updateController();

        // engine control
        if (movementY != 0) {
            setEngineTarget(Math.max(0.0f, Math.min(1.0f, getEngineTarget() + 0.1f * movementY)));
            if (movementY < 0) {
                setVelocity(getVelocity().multiply(getBrakeFactor()));
            }
        }

        // get direction
        Vec3d direction = getDirection();

        // speed
        float thrust = (float)(Math.pow(getEnginePower(), 2.0) * properties.getEngineSpeed());

        // accelerate
        setVelocity(getVelocity().add(direction.multiply(thrust)));
    }
}
