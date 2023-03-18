package immersive_aircraft.entity;

import immersive_aircraft.entity.misc.AircraftProperties;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import org.joml.Vector3f;

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
            .setWindSensitivity(0.05f)
            .setMass(1.0f);

    public AirplaneEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    boolean useAirplaneControls() {
        return true;
    }

    @Override
    public AircraftProperties getProperties() {
        return properties;
    }

    @Override
    float getGroundVelocityDecay() {
        return falloffGroundVelocityDecay(0.9f);
    }

    @Override
    protected float getGravity() {
        Vector3f direction = getDirection();
        float speed = (float)getVelocity().length() * (1.0f - Math.abs(direction.y));
        return Math.max(0.0f, 1.0f - speed * 1.5f) * super.getGravity();
    }

    float getBrakeFactor() {
        return 0.95f;
    }

    @Override
    void updateController() {
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
        Vector3f direction = getDirection();

        // speed
        float thrust = (float)(Math.pow(getEnginePower(), 2.0) * properties.getEngineSpeed());

        // accelerate
        setVelocity(getVelocity().add(toVec3d(direction.mul(thrust))));
    }
}
