package immersive_aircraft.entity;

import immersive_aircraft.entity.misc.AircraftProperties;
import immersive_aircraft.entity.misc.Trail;
import immersive_aircraft.util.Utils;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

/**
 * Abstract aircraft, which performs basic physics
 */
public abstract class AircraftEntity extends VehicleEntity {
    private double lastY;

    public AircraftEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    private static final List<Trail> TRAILS = Collections.emptyList();

    public List<Trail> getTrails() {
        return TRAILS;
    }

    public abstract AircraftProperties getProperties();

    List<List<Vec3d>> PASSENGER_POSITIONS = List.of(List.of(new Vec3d(0.0f, 0.0f, 0.0f)));

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

        super.tick();
    }

    @Override
    void updateVelocity() {
        float velocityDecay;
        float rotationDecay = 0.99f;
        float gravity = getGravity();
        if (touchingWater) {
            gravity *= 0.25f;
            velocityDecay = 0.9f;
        } else if (!onGround) {
            velocityDecay = 0.99f;
        } else {
            float friction = getProperties().getWheelFriction();
            velocityDecay = 1.0f - friction;
            rotationDecay = 1.0f - getProperties().getWheelFriction();
        }

        // get direction
        Vec3d direction = getDirection();

        // glide
        double diff = lastY - getY();
        if (lastY != 0.0 && getProperties().getGlideFactor() > 0) {
            setVelocity(getVelocity().add(direction.multiply(diff * getProperties().getGlideFactor() * (1.0f - Math.abs(direction.getY())))));
        }
        lastY = getY();

        Vec3d velocity = getVelocity();
        double drag = Math.abs(direction.dotProduct(velocity.normalize()));

        // convert power
        velocity = velocity.normalize()
                .lerp(direction, getProperties().getLift())
                .multiply(velocity.length() * (drag * getProperties().getDriftDrag() + (1.0 - getProperties().getDriftDrag())));
        setVelocity(
                velocity.getX(),
                velocity.getY(),
                velocity.getZ()
        );

        // friction
        Vec3d vec3d = getVelocity();
        setVelocity(vec3d.x * velocityDecay, vec3d.y * velocityDecay + gravity, vec3d.z * velocityDecay);
        pressingInterpolatedX.decay(0.0f, 1.0f - rotationDecay);
        pressingInterpolatedZ.decay(0.0f, 1.0f - rotationDecay);

        // wind
        if (!onGround) {
            boolean thundering = world.getLevelProperties().isThundering();
            boolean raining = world.getLevelProperties().isRaining();
            float strength = (float)((1.0f + vec3d.length()) * (thundering ? 1.5f : 1.0f) * (raining ? 2.0f : 1.0f));
            float nx = (float)(Utils.cosNoise(age / 20.0 / getProperties().getMass() * strength) * getProperties().getWindSensitivity() * strength);
            float nz = (float)(Utils.cosNoise(age / 21.0 / getProperties().getMass() * strength) * getProperties().getWindSensitivity() * strength);
            setPitch(getPitch() + nx);
            setYaw(getYaw() + nz);
        }
    }
}

