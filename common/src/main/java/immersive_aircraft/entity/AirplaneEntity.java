package immersive_aircraft.entity;

import immersive_aircraft.entity.misc.AircraftProperties;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Implements airplane like physics properties and accelerated towards
 */
public abstract class AirplaneEntity extends EngineAircraft {
    // todo property
    private static final float PUSH_SPEED = 0.025f;

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

    public AirplaneEntity(EntityType<? extends AircraftEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);
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
        Vec3 direction = getForwardDirection();
        float speed = (float) ((float) getDeltaMovement().length() * (1.0f - Math.abs(direction.y())));
        return Math.max(0.0f, 1.0f - speed * 1.5f) * super.getGravity();
    }

    protected float getBrakeFactor() {
        return 0.95f;
    }

    @Override
    protected void updateController() {
        if (!isVehicle()) {
            return;
        }

        super.updateController();

        // engine control
        if (movementY != 0) {
            setEngineTarget(Math.max(0.0f, Math.min(1.0f, getEngineTarget() + 0.1f * movementY)));
            if (movementY < 0) {
                setDeltaMovement(getDeltaMovement().scale(getBrakeFactor()));
            }
        }

        // get direction
        Vec3 direction = getForwardDirection();

        // speed
        float thrust = (float) (Math.pow(getEnginePower(), 2.0) * properties.getEngineSpeed());
        if (onGround && getEngineTarget() < 1.0) {
            thrust = PUSH_SPEED / (1.0f + (float) getDeltaMovement().length() * 5.0f) * pressingInterpolatedZ.getSmooth() * (1.0f - getEnginePower());
        }

        // accelerate
        setDeltaMovement(getDeltaMovement().add(direction.scale(thrust)));
    }
}
