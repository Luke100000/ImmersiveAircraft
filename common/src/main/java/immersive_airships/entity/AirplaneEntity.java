package immersive_airships.entity;

import immersive_airships.entity.properties.AircraftProperties;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class AirplaneEntity extends EngineAircraft {
    private final AircraftProperties properties = new AircraftProperties(this)
            .setYawSpeed(0.5f)
            .setPitchSpeed(0.75f)
            .setPushSpeed(1.0f)
            .setEngineSpeed(0.2f)
            .setGlideFactor(0.075f)
            .setMaxPitch(60)
            .setDriftDrag(0.01f)
            .setLift(0.1f);

    public AirplaneEntity(EntityType<? extends AirshipEntity> entityType, World world) {
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
            double gravity = -0.04 * (1.0f - getEnginePower());
            if (location == Location.IN_WATER) {
                gravity = (waterLevel - getY()) / (double)getHeight() * 0.06153846016296973;
                velocityDecay = 0.9f;
            } else if (location == Location.UNDER_FLOWING_WATER) {
                gravity = -0.0001;
                velocityDecay = 0.9f;
            } else if (location == Location.UNDER_WATER) {
                gravity = 0.01f;
                velocityDecay = 0.45f;
            } else if (location == Location.IN_AIR) {
                velocityDecay = 0.99f;
            } else if (location == Location.ON_LAND) {
                velocityDecay = slipperiness * 0.25f + 0.75f;
                rotationDecay = 0.8f;
            }

            Vec3d vec3d = getVelocity();
            setVelocity(vec3d.x * velocityDecay, vec3d.y * velocityDecay + gravity, vec3d.z * velocityDecay);
            yawVelocity *= rotationDecay;
            pitchVelocity *= rotationDecay;
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
        if (pressingDown) {
            pitchVelocity += properties.getMaxPitch();
        }
        if (pressingUp) {
            pitchVelocity -= properties.getPitchSpeed();
        }
        setPitch(Math.max(-properties.getMaxPitch(), Math.min(properties.getMaxPitch(), getPitch() + pitchVelocity)));

        // landing
        if (location == Location.ON_LAND) {
            setPitch(getPitch() * 0.9f);
        }

        // get direction
        Vec3d direction = new Vec3d(
                MathHelper.sin(-getYaw() * ((float)Math.PI / 180)),
                MathHelper.sin(-getPitch() * ((float)Math.PI / 180)),
                MathHelper.cos(getYaw() * ((float)Math.PI / 180))).normalize();

        // glide
        float glide = (float)(1.0f - getVelocity().normalize().getY() * properties.getGlideFactor());
        setVelocity(getVelocity().multiply(glide));

        // perform interpolation and air drag
        Vec3d velocity = getVelocity();
        double drag = 1.0 - direction.dotProduct(velocity.normalize()) * properties.getDriftDrag();

        // convert power
        setVelocity(
                velocity.normalize()
                        .lerp(direction, properties.getLift())
                        .multiply(velocity.length() * drag)
        );

        // speed
        float speed = properties.getEngineSpeed() * getEnginePower();
        if (pressingForward) {
            if (location == Location.ON_LAND) {
                speed *= properties.getPushSpeed();
            }
            speed += properties.getEngineSpeed() * getEnginePower();
            setEngineTarget(1.0f);
        } else {
            setEngineTarget(0.0f);
        }

        // either backwards or downwards
        if (pressingBack) {
            if (location == Location.ON_LAND) {
                speed = -properties.getPushSpeed();
            } else {
                setPitch(getPitch() * 0.9f);
            }
        }

        // accelerate
        setVelocity(getVelocity().add(direction.multiply(speed)));
    }
}
