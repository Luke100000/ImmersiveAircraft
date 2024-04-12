package immersive_aircraft.entity;

import immersive_aircraft.config.Config;
import immersive_aircraft.entity.misc.Trail;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_aircraft.util.Utils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;

/**
 * Abstract aircraft, which performs basic physics
 */
public abstract class AircraftEntity extends EngineVehicle {
    protected double lastY;

    public AircraftEntity(EntityType<? extends AircraftEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);
    }

    private static final List<Trail> TRAILS = Collections.emptyList();

    public List<Trail> getTrails() {
        return TRAILS;
    }

    @Override
    public void tick() {
        // rolling interpolation
        prevRoll = roll;
        if (onGround()) {
            setZRot(roll * 0.9f);
        } else {
            setZRot(-pressingInterpolatedX.getSmooth() * getProperties().get(VehicleStat.ROLL_FACTOR));
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
                .lerp(direction, getProperties().get(VehicleStat.LIFT))
                .scale(velocity.length() * (drag * getProperties().get(VehicleStat.FRICTION) + (1.0 - getProperties().get(VehicleStat.FRICTION)))));
    }

    @Override
    protected float getGroundDecay() {
        float gravity = Math.min(1.0f, Math.max(0.0f, getGravity() / (-0.04f)));
        float upgrade = Math.min(1.0f, getProperties().get(VehicleStat.ACCELERATION) * 0.5f);
        return (super.getGroundDecay() * gravity + (1.0f - gravity)) * (1.0f - upgrade) + upgrade;
    }

    @Override
    protected void updateController() {
        // left-right
        setYRot(getYRot() - getProperties().get(VehicleStat.YAW_SPEED) * pressingInterpolatedX.getSmooth());

        // forwards-backwards
        if (!onGround()) {
            setXRot(getXRot() + getProperties().get(VehicleStat.PITCH_SPEED) * pressingInterpolatedZ.getSmooth());
        }
        setXRot(getXRot() * (1.0f - getProperties().getAdditive(VehicleStat.STABILIZER)));
    }

    @Override
    protected void updateVelocity() {
        // get direction
        Vector3f direction = getForwardDirection();

        // glide
        float diff = (float) (lastY - getY());
        if (lastY != 0.0 && getProperties().get(VehicleStat.GLIDE_FACTOR) > 0 && diff != 0.0) {
            setDeltaMovement(getDeltaMovement().add(toVec3d(direction).scale(diff * getProperties().get(VehicleStat.GLIDE_FACTOR) * (1.0f - Math.abs(direction.y)))));
        }
        lastY = (float) getY();

        // convert power
        convertPower(toVec3d(direction));

        // friction
        applyFriction();

        if (onGround()) {
            // Landing
            setXRot((getXRot() + getProperties().get(VehicleStat.GROUND_PITCH)) * 0.9f - getProperties().get(VehicleStat.GROUND_PITCH));
        } else {
            // Wind
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
        float sensitivity = getProperties().get(VehicleStat.WIND);
        float thundering = level().getRainLevel(0.0f);
        float raining = level().getThunderLevel(0.0f);
        float weather = (float) ((Config.getInstance().windClearWeather + getDeltaMovement().length()) + thundering * Config.getInstance().windThunderWeather + raining * Config.getInstance().windRainWeather);
        return weather * sensitivity;
    }

    public Vector3f getWindEffect() {
        float wind = getWindStrength();
        float nx = (float) (Utils.cosNoise(tickCount / 20.0 / getProperties().get(VehicleStat.MASS)) * wind);
        float nz = (float) (Utils.cosNoise(tickCount / 21.0 / getProperties().get(VehicleStat.MASS)) * wind);
        return new Vector3f(nx, 0.0f, nz);
    }
}

