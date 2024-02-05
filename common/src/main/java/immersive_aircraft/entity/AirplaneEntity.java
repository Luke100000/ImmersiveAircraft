package immersive_aircraft.entity;

import immersive_aircraft.item.upgrade.AircraftStat;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Implements airplane like physics properties and accelerated towards
 */
public abstract class AirplaneEntity extends EngineAircraft {
    public AirplaneEntity(EntityType<? extends AircraftEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);
    }

    @Override
    protected boolean useAirplaneControls() {
        return true;
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
        float thrust = (float) (Math.pow(getEnginePower(), 2.0) * getProperties().get(AircraftStat.ENGINE_SPEED));
        if (onGround && getEngineTarget() < 1.0) {
            thrust = getProperties().get(AircraftStat.PUSH_SPEED) / (1.0f + (float) getDeltaMovement().length() * 5.0f) * pressingInterpolatedZ.getSmooth() * (1.0f - getEnginePower());
        }

        // accelerate
        setDeltaMovement(getDeltaMovement().add(direction.scale(thrust)));
    }
}
