package immersive_airships.entity;

import immersive_airships.entity.properties.AircraftProperties;
import immersive_airships.util.Utils;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GyrodyneEntity extends EngineAircraft {
    private final AircraftProperties properties = new AircraftProperties(this)
            .setYawSpeed(0.5f)
            .setPitchSpeed(0.5f)
            .setPushSpeed(0.025f)
            .setEngineSpeed(0.1f)
            .setVerticalSpeed(0.015f)
            .setGlideFactor(0.075f)
            .setMaxPitch(10)
            .setDriftDrag(0.01f)
            .setLift(0.1f)
            .setWindSensitivity(0.001f);

    public GyrodyneEntity(EntityType<? extends AirshipEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
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
            float rotationDecay = 0.9f;
            double gravity = getEnginePower() == 1.0f ? 0.0f : properties.getGravity();
            if (location == Location.IN_WATER || location == Location.UNDER_FLOWING_WATER) {
                gravity = -0.01f;
                velocityDecay = 0.9f;
            } else if (location == Location.UNDER_WATER) {
                gravity = 0.01f;
                velocityDecay = 0.45f;
            } else if (location == Location.IN_AIR) {
                velocityDecay = 0.99f;
            } else if (location == Location.ON_LAND) {
                velocityDecay = slipperiness * 0.5f + 0.5f;
                rotationDecay = 0.8f;
            }

            //friction
            Vec3d vec3d = getVelocity();
            setVelocity(vec3d.x * velocityDecay, vec3d.y * velocityDecay + gravity, vec3d.z * velocityDecay);
            yawVelocity *= rotationDecay;
            pitchVelocity *= rotationDecay;

            // get direction
            Vec3d direction = new Vec3d(
                    MathHelper.sin(-getYaw() * ((float)Math.PI / 180)),
                    0.0,
                    MathHelper.cos(getYaw() * ((float)Math.PI / 180))).normalize();

            Vec3d velocity = getVelocity().multiply(1.0, 0.0, 1.0);
            double drag = Math.abs(direction.dotProduct(velocity.normalize()));

            // convert power
            velocity = velocity.normalize()
                    .lerp(direction, properties.getLift())
                    .multiply(velocity.length() * (drag * properties.getDriftDrag() + (1.0 - properties.getDriftDrag())));
            setVelocity(
                    velocity.getX(),
                    getVelocity().getY(),
                    velocity.getZ()
            );

            // wind
            if (location == Location.IN_AIR) {
                float nx = (float)(Utils.cosNoise(age / 20.0) * properties.getWindSensitivity());
                float ny = (float)(Utils.cosNoise(age / 21.0) * properties.getWindSensitivity());
                setVelocity(getVelocity().add(nx, 0.0f, ny));
            }
        }
    }

    @Override
    void updateController() {
        if (!hasPassengers()) {
            return;
        }

        // left-right
        if (pressingLeft) {
            yawVelocity -= properties.getYawSpeed();
        }
        if (pressingRight) {
            yawVelocity += properties.getYawSpeed();
        }
        setYaw(getYaw() + yawVelocity);

        // up-down
        if (location != Location.ON_LAND && pressingForward) {
            pitchVelocity += properties.getPitchSpeed();
        } else if (location != Location.ON_LAND && pressingBack) {
            pitchVelocity -= properties.getPitchSpeed();
        } else {
            setPitch(getPitch() * 0.8f);
        }
        setPitch(Math.max(-properties.getMaxPitch(), Math.min(properties.getMaxPitch(), getPitch() + pitchVelocity)));

        // landing
        if (location == Location.ON_LAND) {
            setPitch(getPitch() * 0.9f);
        }

        //engine
        if (pressingUp) {
            setEngineTarget(1.0f);
        } else {
            setEngineTarget(0.0f);
        }

        // speed
        if (pressingUp) {
            setVelocity(getVelocity().add(0.0f, getEnginePower() * properties.getVerticalSpeed(), 0.0f));
        } else if (pressingDown) {
            setVelocity(getVelocity().add(0.0f, -getEnginePower() * properties.getVerticalSpeed(), 0.0f));
        }

        // get pointing direction
        Vec3d direction = new Vec3d(
                MathHelper.sin(-getYaw() * ((float)Math.PI / 180)),
                0.0,
                MathHelper.cos(getYaw() * ((float)Math.PI / 180))
        ).normalize();

        // speed
        float sin = MathHelper.sin(getPitch() * ((float)Math.PI / 180));
        float thrust = (float)(Math.pow(getEnginePower(), 5.0) * properties.getEngineSpeed()) * sin;
        if (location == Location.ON_LAND) {
            if (pressingForward) {
                thrust = properties.getPushSpeed();
            } else if (pressingBack) {
                thrust = -properties.getPushSpeed() * 0.5f;
            }
        }

        // accelerate
        setVelocity(getVelocity().add(direction.multiply(thrust)));
    }
}
