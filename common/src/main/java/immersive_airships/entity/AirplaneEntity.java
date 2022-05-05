package immersive_airships.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class AirplaneEntity extends AirshipEntity {
    private int oldLevel;

    private final static float YAW_SPEED = 0.5f;
    private final static float PITCH_SPEED = 0.75f;
    private final static float PUSH_SPEED = 0.1f;
    private final static float LANDING_SPEED = 0.05f;
    private final static float ENGINE_SPEED = 0.2f;
    private final static float GLIDE_FACTOR = 0.075f;
    private final static float MAX_PITCH = 60;
    private final static float DRIFT_DRAG = 0.1f;
    private final static float LIFT = 0.01f;

    public AirplaneEntity(EntityType<? extends AirshipEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isClient()) {
            // shutdown
            if (!hasPassengers()) {
                setEngineTarget(0.0f);
            }

            // start engine
            if (engineTarget >= getEnginePower() || hasPassengers() && getEnginePower() == 1.0f) {
                setEnginePower(Math.min(1.0f, getEnginePower() + 0.01f));
            } else {
                setEnginePower(Math.max(0.0f, getEnginePower() - 0.01f));
            }

            // sounds
            if (engineTarget > 0.0f) {
                int level = (int)(Math.pow(getEnginePower(), 1.5f) * 10);
                if (oldLevel != level) {
                    oldLevel = level;
                    playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.5f, getEnginePower() * 0.5f + 0.5f + (level % 2) * 0.5f);
                }
            }
        }
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
            yawVelocity -= YAW_SPEED;
        }
        if (pressingRight) {
            yawVelocity += YAW_SPEED;
        }
        setYaw(getYaw() + yawVelocity);

        // up-down
        if (pressingDown) {
            pitchVelocity += PITCH_SPEED;
        }
        if (pressingUp) {
            pitchVelocity -= PITCH_SPEED;
        }
        setPitch(Math.max(-MAX_PITCH, Math.min(MAX_PITCH, getPitch() + pitchVelocity)));

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
        float glide = (float)(1.0f - getVelocity().normalize().getY() * GLIDE_FACTOR);
        setVelocity(getVelocity().multiply(glide));

        // perform interpolation and air drag
        Vec3d velocity = getVelocity();
        double drag = 1.0 - direction.dotProduct(velocity.normalize()) * DRIFT_DRAG;

        // convert power
        setVelocity(
                velocity.normalize()
                        .lerp(direction, LIFT)
                        .multiply(velocity.length() * drag)
        );

        // speed
        float speed = ENGINE_SPEED * getEnginePower();
        if (pressingForward) {
            if (location == Location.ON_LAND) {
                speed *= PUSH_SPEED;
            }
            speed += ENGINE_SPEED * getEnginePower();
            setEngineTarget(1.0f);
        } else {
            setEngineTarget(0.0f);
        }

        // either backwards or downwards
        if (pressingBack) {
            if (location == Location.ON_LAND) {
                speed = -PUSH_SPEED;
            } else {
                setPitch(getPitch() * 0.9f);
                setVelocity(getVelocity().add(0, -LANDING_SPEED, 0));
            }
        }

        // accelerate
        setVelocity(getVelocity().add(direction.multiply(speed)));
    }
}
