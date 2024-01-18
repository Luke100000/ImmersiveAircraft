package immersive_aircraft.entity;

import com.mojang.math.Vector3f;
import immersive_aircraft.config.Config;
import immersive_aircraft.data.AircraftDataLoader;
import immersive_aircraft.entity.misc.AircraftProperties;
import immersive_aircraft.entity.misc.PositionDescriptor;
import immersive_aircraft.entity.misc.Trail;
import immersive_aircraft.entity.weapons.Telescope;
import immersive_aircraft.entity.weapons.Weapon;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.util.Utils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Abstract aircraft, which performs basic physics
 */
public abstract class AircraftEntity extends InventoryVehicleEntity {
    private final AircraftProperties properties;
    private double lastY;

    public AircraftEntity(EntityType<? extends AircraftEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);

        this.properties = new AircraftProperties(AircraftDataLoader.get(identifier).getProperties(), this);
    }

    private static final List<Trail> TRAILS = Collections.emptyList();

    public List<Trail> getTrails() {
        return TRAILS;
    }

    public AircraftProperties getProperties() {
        return properties;
    }

    protected List<List<PositionDescriptor>> getPassengerPositions() {
        return AircraftDataLoader.get(identifier).getPassengerPositions();
    }

    @Override
    public void tick() {
        // rolling interpolation
        prevRoll = roll;
        if (onGround) {
            setZRot(roll * 0.9f);
        } else {
            setZRot(-pressingInterpolatedX.getSmooth() * getProperties().get(AircraftStat.ROLL_FACTOR));
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
                .lerp(direction, getProperties().get(AircraftStat.LIFT))
                .scale(velocity.length() * (drag * getProperties().get(AircraftStat.FRICTION) + (1.0 - getProperties().get(AircraftStat.FRICTION)))));
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

    // todo property
    protected float getGroundVelocityDecay() {
        return 0.95f;
    }

    // todo property
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
        Vec3 direction = getForwardDirection();

        // glide
        double diff = lastY - getY();
        if (lastY != 0.0 && getProperties().get(AircraftStat.GLIDE_FACTOR) > 0 && diff != 0.0) {
            setDeltaMovement(getDeltaMovement().add(direction.scale(diff * getProperties().get(AircraftStat.GLIDE_FACTOR) * (1.0f - Math.abs(direction.y())))));
        }
        lastY = getY();

        // convert power
        convertPower(direction);

        // friction
        Vec3 velocity = getDeltaMovement();
        setDeltaMovement(velocity.x * decay * getHorizontalVelocityDelay(), velocity.y * decay * getVerticalVelocityDelay() + gravity, velocity.z * decay * getHorizontalVelocityDelay());
        pressingInterpolatedX.decay(0.0f, 1.0f - decay * getRotationDecay());
        pressingInterpolatedZ.decay(0.0f, 1.0f - decay * getRotationDecay());

        // wind
        if (!onGround) {
            Vector3f effect = getWindEffect();
            setXRot(getXRot() + effect.x());
            setYRot(getYRot() + effect.z());

            float offsetStrength = 0.005f;
            setDeltaMovement(getDeltaMovement().add(effect.x() * offsetStrength, 0.0f, effect.z() * offsetStrength));
        }
    }

    public void chill() {
        lastY = 0.0;
    }

    @Override
    public float getDurability() {
        return getProperties().get(AircraftStat.DURABILITY);
    }

    public float getWindStrength() {
        float sensitivity = getProperties().get(AircraftStat.WIND);
        float thundering = level.getRainLevel(0.0f);
        float raining = level.getThunderLevel(0.0f);
        float weather = (float) ((Config.getInstance().windClearWeather + getDeltaMovement().length()) + thundering * Config.getInstance().windThunderWeather + raining * Config.getInstance().windRainWeather);
        return weather * sensitivity;
    }

    public Vector3f getWindEffect() {
        float wind = getWindStrength();
        float nx = (float) (Utils.cosNoise(tickCount / 20.0 / getProperties().get(AircraftStat.MASS)) * wind);
        float nz = (float) (Utils.cosNoise(tickCount / 21.0 / getProperties().get(AircraftStat.MASS)) * wind);
        return new Vector3f(nx, 0.0f, nz);
    }

    public boolean isScoping() {
        Collection<List<Weapon>> values = getWeapons().values();
        for (List<Weapon> weapons : values) {
            for (Weapon weapon : weapons) {
                if (weapon instanceof Telescope telescope && telescope.isScoping()) {
                    return true;
                }
            }
        }
        return false;
    }
}

