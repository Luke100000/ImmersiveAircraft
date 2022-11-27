package immersive_aircraft.entity;

import immersive_aircraft.client.render.entity.renderer.Trail;
import immersive_aircraft.entity.properties.AircraftProperties;
import immersive_aircraft.util.Utils;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

/**
 * Abstract aircraft, which performs basic physics and rolling
 */
public abstract class AircraftEntity extends VehicleEntity {
    private double lastY;

    public float roll;
    public float prevRoll;

    public AircraftEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    public float getRoll() {
        return roll;
    }

    public float getRoll(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevRoll, getRoll());
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
        if (location != Location.ON_LAND) {
            roll = -pressingInterpolatedX.getSmooth() * getProperties().getRollFactor();
        } else {
            roll *= 0.9;
        }

        super.tick();
    }

    @Override
    void updateVelocity() {
        if (lastLocation == Location.IN_AIR && location != Location.IN_AIR && location != Location.ON_LAND) {
            // impact on water surface
            waterLevel = getBodyY(1.0);
            setPosition(getX(), (double)(method_7544() - getHeight()) + 0.101, getZ());
            setVelocity(getVelocity().multiply(1.0, 0.0, 1.0));
            fallVelocity = 0.0;
            location = Location.IN_WATER;
        } else {
            float velocityDecay = 0.05f;
            float rotationDecay = 0.99f;
            float gravity = getGravity();
            if (location == Location.IN_WATER || location == Location.UNDER_FLOWING_WATER) {
                gravity = -0.01f;
                velocityDecay = 0.9f;
            } else if (location == Location.UNDER_WATER) {
                gravity = 0.01f;
                velocityDecay = 0.45f;
            } else if (location == Location.IN_AIR) {
                velocityDecay = 0.99f;
            } else if (location == Location.ON_LAND) {
                float friction = getProperties().getWheelFriction();
                velocityDecay = slipperiness * friction + (1.0f - friction);
                rotationDecay = 1.0f - getProperties().getWheelFriction();
            }

            // get direction
            Vec3d direction = getDirection();

            // glide
            if (lastY != 0) {
                double diff = lastY - getY();
                setVelocity(getVelocity().add(direction.multiply(diff * getProperties().getGlideFactor())));
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
            // todo property
            Vec3d vec3d = getVelocity();
            setVelocity(vec3d.x * velocityDecay, vec3d.y * velocityDecay + gravity, vec3d.z * velocityDecay);
            pressingInterpolatedX.decay(0.0f, 1.0f - rotationDecay);
            pressingInterpolatedZ.decay(0.0f, 1.0f - rotationDecay);

            // wind
            if (location == Location.IN_AIR) {
                double speed = vec3d.length();
                float nx = (float)(Utils.cosNoise(age / 20.0 / getProperties().getMass() * (1.0 + speed * 3.0)) * getProperties().getWindSensitivity());
                float nz = (float)(Utils.cosNoise(age / 21.0 / getProperties().getMass() * (1.0 + speed * 3.0)) * getProperties().getWindSensitivity());
                setVelocity(getVelocity().add(nx, 0.0f, nz));
            }
        }
    }

    protected Vector4f transformPosition(Matrix4f transform, float x, float y, float z) {
        Vector4f p0 = new Vector4f(x, y, z, 1);
        p0.transform(transform);
        return p0;
    }

    protected Vec3f transformVector(Matrix3f transform, float x, float y, float z) {
        Vec3f p0 = new Vec3f(x, y, z);
        p0.transform(transform);
        return p0;
    }

    protected Matrix4f getVehicleTransform() {
        Matrix4f transform = Matrix4f.translate((float)getX(), (float)getY(), (float)getZ());
        transform.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-getYaw()));
        transform.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(getPitch()));
        transform.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(getRoll()));
        return transform;
    }

    protected Matrix3f getVehicleNormalTransform() {
        Matrix3f transform = Matrix3f.scale(1.0f, 1.0f, 1.0f);
        transform.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-getYaw()));
        transform.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(getPitch()));
        transform.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(getRoll()));
        return transform;
    }
}

