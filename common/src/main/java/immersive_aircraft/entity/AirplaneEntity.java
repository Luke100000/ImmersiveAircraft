package immersive_aircraft.entity;

import immersive_aircraft.client.AircraftInput;
import immersive_aircraft.client.KeyBindings;
import immersive_aircraft.item.upgrade.VehicleStat;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Implements airplane like physics properties and accelerated towards
 */
public abstract class AirplaneEntity extends AircraftEntity {
    private static final float ENGINE_THRUST_SCALE = 1.25f;
    private static final float ENGINE_SPEED_TO_MAX_SPEED = 500.0f;

    private float previousThrottle;

    public AirplaneEntity(EntityType<? extends AircraftEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);
    }

    @Override
    protected boolean useAirplaneControls() {
        return true;
    }

    @Override
    protected double getGlideAcceleration(Vec3 direction, double heightDifference) {
        // The descent-speed bonus must not grow beyond the acceleration supplied by gravity.
        double limit = Math.max(0.0, getGravity()) * Math.abs(direction.y);
        return Mth.clamp(super.getGlideAcceleration(direction, heightDifference), -limit, limit);
    }

    @Override
    protected double getDefaultGravity() {
        Vec3 direction = toVec3d(getForwardDirection());
        double lift = getLiftEfficiency(direction, 1.0 - Math.abs(direction.y));
        return (1.0 - lift) * super.getDefaultGravity();
    }

    @Override
    protected float getLiftFactor(Vec3 direction) {
        return (float) (super.getLiftFactor(direction) * getLiftEfficiency(direction, 1.0));
    }

    private double getLiftEfficiency(Vec3 direction, double speedFactor) {
        Vec3 velocity = getDeltaMovement();
        double forwardSpeed = Math.max(0.0, direction.dot(velocity));
        double alignment = Math.clamp(direction.dot(velocity.normalize()), 0.0, 1.0);
        return Math.min(1.0, forwardSpeed * getProperties().get(VehicleStat.LIFT) * 10.0 * speedFactor) * alignment;
    }

    protected float getBrakeFactor() {
        return getProperties().get(VehicleStat.BRAKE_FACTOR);
    }

    @Override
    protected void updateController() {
        if (!isVehicle()) {
            return;
        }

        super.updateController();

        // engine control
        float throttle = level().isClientSide ? AircraftInput.throttle() : -1;
        float brake = Math.max(0, -movementY);
        // An idle controller must not reset a throttle set with the keyboard.
        if (throttle > 0 || throttle == 0 && previousThrottle > 0) {
            brake = AircraftInput.strength(KeyBindings.down);
            setEngineTarget(Math.max(0, throttle - brake));
        } else if (movementY != 0) {
            previousThrottle = 0;
            setEngineTarget(Math.clamp(getEngineTarget() + 0.1f * movementY, 0.0f, 1.0f));
        }
        if (throttle >= 0) {
            previousThrottle = throttle;
        }
        if (brake > 0) {
            setDeltaMovement(getDeltaMovement().scale(Mth.lerp(brake, 1.0f, getBrakeFactor())));
        }

        // get the direction
        Vector3f direction = getForwardDirection();

        // speed
        float engineSpeed = getProperties().get(VehicleStat.ENGINE_SPEED);
        float thrust = (float) (Math.pow(getEnginePower(), 2.0) * engineSpeed * ENGINE_THRUST_SCALE);
        float maxSpeed = engineSpeed * ENGINE_SPEED_TO_MAX_SPEED;
        if (maxSpeed > 0.0f) {
            double forwardSpeed = Math.max(0.0, getDeltaMovement().dot(toVec3d(direction))) * 20.0;
            thrust *= (float) Mth.clamp((1.0 - forwardSpeed / maxSpeed) / 0.25, 0.0, 1.0);
        }
        if (onGround()) {
            thrust *= 0.75f;
        }
        if (onGround() && getEngineTarget() < 1.0) {
            thrust = getProperties().get(VehicleStat.PUSH_SPEED) / (1.0f + (float) getDeltaMovement().length() * 5.0f) * pressingInterpolatedZ.getSmooth() * (1.0f - getEnginePower());
        }

        // accelerate
        setDeltaMovement(getDeltaMovement().add(toVec3d(direction.mul(thrust))));
    }
}
