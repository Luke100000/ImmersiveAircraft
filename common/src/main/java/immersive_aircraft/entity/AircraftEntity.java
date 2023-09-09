package immersive_aircraft.entity;

import immersive_aircraft.config.Config;
import immersive_aircraft.entity.misc.AircraftProperties;
import immersive_aircraft.entity.misc.Trail;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.util.Utils;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Abstract aircraft, which performs basic physics
 */
public abstract class AircraftEntity extends InventoryVehicleEntity {
    private float lastY;

    public AircraftEntity(EntityType<? extends AircraftEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);
    }

    private static final List<Trail> TRAILS = Collections.emptyList();

    public List<Trail> getTrails() {
        return TRAILS;
    }

    public abstract AircraftProperties getProperties();

    final List<List<Vector3f>> PASSENGER_POSITIONS = List.of(List.of(new Vector3f(0.0f, 0.0f, 0.0f)));

    protected List<List<Vector3f>> getPassengerPositions() {
        return PASSENGER_POSITIONS;
    }

    @Override
    public void tick() {
        // rolling interpolation
        prevRoll = roll;
        if (onGround) {
            roll *= 0.9;
        } else {
            roll = -pressingInterpolatedX.getSmooth() * getProperties().getRollFactor();
        }

        if (Double.isNaN(getDeltaMovement().x) || Double.isNaN(getDeltaMovement().y) || Double.isNaN(getDeltaMovement().z)) {
            setDeltaMovement(0, 0, 0);
        }

        super.tick();
    }

    protected void convertPower(Vec3 direction) {
        Vec3 velocity = getDeltaMovement();
        double drag = Math.abs(direction.dot(velocity.normalize()));
        setDeltaMovement(velocity.normalize()
                .lerp(direction, getProperties().getLift())
                .scale(velocity.length() * (drag * getProperties().getDriftDrag() + (1.0 - getProperties().getDriftDrag()))));
    }

    protected float getHorizontalVelocityDelay() {
        return 0.98f;
    }

    protected float getVerticalVelocityDelay() {
        return 0.98f;
    }

    // Considers gravity and upgrades to modify decay
    protected float falloffGroundVelocityDecay(float original) {
        float gravity = Math.min(1.0f, Math.max(0.0f, getGravity() / (-0.04f)));
        float upgrade = Math.min(1.0f, getTotalUpgrade(AircraftStat.ACCELERATION) * 0.5f);
        return (original * gravity + (1.0f - gravity)) * (1.0f - upgrade) + upgrade;
    }

    protected float getGroundVelocityDecay() {
        return 0.95f;
    }

    protected float getRotationDecay() {
        return 0.98f;
    }

    @Override
    protected void updateVelocity() {
        float decay = 1.0f - 0.015f * getTotalUpgrade(AircraftStat.FRICTION);
        float gravity = getGravity();
        if (wasTouchingWater) {
            gravity *= 0.25f;
            decay = 0.9f;
        } else if (onGround) {
            if (isVehicle()) {
                decay = getGroundVelocityDecay();
            } else {
                decay = 0.75f;
            }
        }

        // get direction
        Vector3f direction = getDirection();

        // glide
        float diff = (float)(lastY - getY());
        if (lastY != 0.0 && getProperties().getGlideFactor() > 0) {
            setDeltaMovement(getDeltaMovement().add(toVec3d(direction).scale(diff * getProperties().getGlideFactor() * (1.0f - Math.abs(direction.y)))));
        }
        lastY = (float)getY();

        // convert power
        convertPower(toVec3d(direction));

        // friction
        Vec3 velocity = getDeltaMovement();
        setDeltaMovement(velocity.x * decay * getHorizontalVelocityDelay(), velocity.y * decay * getVerticalVelocityDelay() + gravity, velocity.z * decay * getHorizontalVelocityDelay());
        pressingInterpolatedX.decay(0.0f, 1.0f - decay * getRotationDecay());
        pressingInterpolatedZ.decay(0.0f, 1.0f - decay * getRotationDecay());

        // wind
        if (!onGround) {
            Vector3f effect = getWindEffect();
            setXRot(getXRot() + effect.x);
            setYRot(getYRot() + effect.z);

            float offsetStrength = 0.005f;
            setDeltaMovement(getDeltaMovement().add(effect.x * offsetStrength, 0.0f, effect.z * offsetStrength));
        }
    }

    public void chill() {
        lastY = 0.0f;
    }

    public float getWindStrength() {
        float sensitivity = getProperties().getWindSensitivity();
        float thundering = level.getRainLevel(0.0f);
        float raining = level.getThunderLevel(0.0f);
        float weather = (float)((Config.getInstance().windClearWeather + getDeltaMovement().length()) + thundering * Config.getInstance().windThunderWeather + raining * Config.getInstance().windRainWeather);
        return weather * sensitivity;
    }

    public Vector3f getWindEffect() {
        float wind = getWindStrength();
        float nx = (float)(Utils.cosNoise(tickCount / 20.0 / getProperties().getMass()) * wind);
        float nz = (float)(Utils.cosNoise(tickCount / 21.0 / getProperties().getMass()) * wind);
        return new Vector3f(nx, 0.0f, nz);
    }
}

