package immersive_aircraft.entity;

import immersive_aircraft.config.Config;
import immersive_aircraft.entity.misc.AircraftProperties;
import immersive_aircraft.entity.misc.Trail;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.util.Utils;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

/**
 * Abstract aircraft, which performs basic physics
 */
public abstract class AircraftEntity extends InventoryVehicleEntity {
    private double lastY;

    public AircraftEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    private static final List<Trail> TRAILS = Collections.emptyList();

    public List<Trail> getTrails() {
        return TRAILS;
    }

    public abstract AircraftProperties getProperties();

    final List<List<Vec3d>> PASSENGER_POSITIONS = List.of(List.of(new Vec3d(0.0f, 0.0f, 0.0f)));

    protected List<List<Vec3d>> getPassengerPositions() {
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

        if (Double.isNaN(getVelocity().x) || Double.isNaN(getVelocity().y) || Double.isNaN(getVelocity().z)) {
            setVelocity(0, 0, 0);
        }

        super.tick();
    }

    protected void convertPower(Vec3d direction) {
        Vec3d velocity = getVelocity();
        double drag = Math.abs(direction.dotProduct(velocity.normalize()));
        setVelocity(velocity.normalize()
                .lerp(direction, getProperties().getLift())
                .multiply(velocity.length() * (drag * getProperties().getDriftDrag() + (1.0 - getProperties().getDriftDrag()))));
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
        if (touchingWater) {
            gravity *= 0.25f;
            decay = 0.9f;
        } else if (onGround) {
            if (hasPassengers()) {
                decay = getGroundVelocityDecay();
            } else {
                decay = 0.75f;
            }
        }

        // get direction
        Vec3d direction = getDirection();

        // glide
        double diff = lastY - getY();
        if (lastY != 0.0 && getProperties().getGlideFactor() > 0) {
            setVelocity(getVelocity().add(direction.multiply(diff * getProperties().getGlideFactor() * (1.0f - Math.abs(direction.getY())))));
        }
        lastY = getY();

        // convert power
        convertPower(direction);

        // friction
        Vec3d velocity = getVelocity();
        setVelocity(velocity.x * decay * getHorizontalVelocityDelay(), velocity.y * decay * getVerticalVelocityDelay() + gravity, velocity.z * decay * getHorizontalVelocityDelay());
        pressingInterpolatedX.decay(0.0f, 1.0f - decay * getRotationDecay());
        pressingInterpolatedZ.decay(0.0f, 1.0f - decay * getRotationDecay());

        // wind
        if (!onGround) {
            Vec3f effect = getWindEffect();
            setPitch(getPitch() + effect.getX());
            setYaw(getYaw() + effect.getZ());

            float offsetStrength = 0.005f;
            setVelocity(getVelocity().add(effect.getX() * offsetStrength, 0.0f, effect.getZ() * offsetStrength));
        }
    }

    public void chill() {
        lastY = 0.0;
    }

    public float getWindStrength() {
        float sensitivity = getProperties().getWindSensitivity();
        float thundering = world.getRainGradient(0.0f);
        float raining = world.getThunderGradient(0.0f);
        float weather = (float)((Config.getInstance().windClearWeather + getVelocity().length()) + thundering * Config.getInstance().windThunderWeather + raining * Config.getInstance().windRainWeather);
        return weather * sensitivity;
    }

    public Vec3f getWindEffect() {
        float wind = getWindStrength();
        float nx = (float)(Utils.cosNoise(age / 20.0 / getProperties().getMass()) * wind);
        float nz = (float)(Utils.cosNoise(age / 21.0 / getProperties().getMass()) * wind);
        return new Vec3f(nx, 0.0f, nz);
    }
}

