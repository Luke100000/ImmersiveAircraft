package immersive_airships.entity;

import immersive_airships.util.Utils;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GyrodyneEntity extends AirshipEntity {
    private int oldLevel;

    private final static float YAW_SPEED = 0.5f;
    private final static float PITCH_SPEED = 0.5f;
    private final static float PUSH_SPEED = 0.025f;
    private final static float ENGINE_SPEED = 0.075f;
    private final static float VERTICAL_SPEED = 0.015f;
    private final static float MAX_PITCH = 10;
    private final static float GRAVITY = -0.04f;
    private final static float DRIFT_DRAG = 0.01f;
    private final static float LIFT = 0.1f;
    private final static float WIND = 0.001f;

    public GyrodyneEntity(EntityType<? extends AirshipEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isClient()) {
            // shutdown
            if (!hasPassengers()) {
                engineTarget = 0.0f;
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
            double gravity = getEnginePower() == 1.0f ? 0.0f : GRAVITY;
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
                    .lerp(direction, LIFT)
                    .multiply(velocity.length() * (drag * DRIFT_DRAG + (1.0 - DRIFT_DRAG)));
            setVelocity(
                    velocity.getX(),
                    getVelocity().getY(),
                    velocity.getZ()
            );

            // wind
            if (location == Location.IN_AIR) {
                float nx = (float)(Utils.cosNoise(age / 20.0) * WIND);
                float ny = (float)(Utils.cosNoise(age / 21.0) * WIND);
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
            yawVelocity -= YAW_SPEED;
        }
        if (pressingRight) {
            yawVelocity += YAW_SPEED;
        }
        setYaw(getYaw() + yawVelocity);

        // up-down
        if (location != Location.ON_LAND && pressingForward) {
            pitchVelocity += PITCH_SPEED;
        } else if (location != Location.ON_LAND && pressingBack) {
            pitchVelocity -= PITCH_SPEED;
        } else {
            setPitch(getPitch() * 0.8f);
        }
        setPitch(Math.max(-MAX_PITCH, Math.min(MAX_PITCH, getPitch() + pitchVelocity)));

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
            setVelocity(getVelocity().add(0.0f, getEnginePower() * VERTICAL_SPEED, 0.0f));
        } else if (pressingDown) {
            setVelocity(getVelocity().add(0.0f, -getEnginePower() * VERTICAL_SPEED, 0.0f));
        }

        // get pointing direction
        Vec3d direction = new Vec3d(
                MathHelper.sin(-getYaw() * ((float)Math.PI / 180)),
                0.0,
                MathHelper.cos(getYaw() * ((float)Math.PI / 180))
        ).normalize();

        // speed
        float sin = MathHelper.sin(getPitch() * ((float)Math.PI / 180));
        float thrust = (float)(Math.pow(getEnginePower(), 5.0) * ENGINE_SPEED) * sin;
        if (location == Location.ON_LAND) {
            if (pressingForward) {
                thrust = PUSH_SPEED;
            } else if (pressingBack) {
                thrust = -PUSH_SPEED * 0.5f;
            }
        }

        // accelerate
        setVelocity(getVelocity().add(direction.multiply(thrust)));
    }
}
