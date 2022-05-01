package immersive_airships.entity;

import immersive_airships.cobalt.network.NetworkHandler;
import immersive_airships.network.c2s.EnginePowerMessage;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GyrodyneEntity extends AirshipEntity {
    static final TrackedData<Float> ENGINE = DataTracker.registerData(GyrodyneEntity.class, TrackedDataHandlerRegistry.FLOAT);

    public float engineTarget = 0.0f;
    private int oldLevel;

    private final static float YAW_SPEED = 0.5f;
    private final static float PITCH_SPEED = 0.5f;
    private final static float BASE_SPEED = 0.1f;
    private final static float ENGINE_SPEED = 0.3f;
    private final static float GLIDE_FACTOR = 0.05f;
    private final static float MAX_PITCH = 60;

    public GyrodyneEntity(EntityType<? extends AirshipEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();

        dataTracker.startTracking(ENGINE, 0.0f);
    }

    public float getEnginePower() {
        return dataTracker.get(ENGINE);
    }

    public void setEnginePower(float power) {
        dataTracker.set(ENGINE, power);
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
                int level = (int)(Math.pow(getEnginePower(), 1.5f) * 5);
                if (oldLevel != level) {
                    oldLevel = level;
                    playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.5f, getEnginePower() + 0.5f);
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
                velocityDecay = 0.95f;
            } else if (location == Location.ON_LAND) {
                velocityDecay = slipperiness * 0.5f + 0.5f;
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
            yawVelocity -= 1.0f;
        }
        if (pressingRight) {
            yawVelocity += 1.0f;
        }
        setYaw(getYaw() + yawVelocity * YAW_SPEED);

        // up-down
        if (pressingUp) {
            pitchVelocity += 1.0f;
        }
        if (pressingDown) {
            pitchVelocity -= 1.0f;
        }
        airshipPitch = Math.max(-MAX_PITCH, Math.min(MAX_PITCH, airshipPitch + pitchVelocity * PITCH_SPEED));

        // landing
        if (location == Location.ON_LAND) {
            airshipPitch *= 0.9;
        }

        // get direction
        Vec3d direction = new Vec3d(
                MathHelper.sin(-getYaw() * ((float)Math.PI / 180)),
                MathHelper.sin(airshipPitch * ((float)Math.PI / 180)),
                MathHelper.cos(getYaw() * ((float)Math.PI / 180)));
        direction = direction.normalize();

        // perform interpolation and air drag
        Vec3d velocity = getVelocity();
        double dot = direction.dotProduct(velocity.normalize());
        float reactionSpeed = 0.1f;

        // speed
        float speed = 0.0f;
        if (pressingForward) {
            speed += BASE_SPEED + ENGINE_SPEED * getEnginePower();
            setEngineTarget(1.0f);
        } else {
            setEngineTarget(0.0f);
        }

        // either backwards or downwards
        if (pressingBack) {
            if (location == Location.ON_LAND) {
                speed = -BASE_SPEED;
            } else {
                airshipPitch *= 0.9;
                setVelocity(getVelocity().add(0, -BASE_SPEED, 0));
            }
        }

        // glide
        float slowdown = (float)(1.0f - getVelocity().normalize().getY() * GLIDE_FACTOR);

        // fly
        setVelocity(
                velocity.multiply(dot * reactionSpeed + (1.0 - reactionSpeed))
                        .lerp(direction.multiply(velocity.length() + speed), reactionSpeed)
                        .multiply(slowdown)
        );
    }

    private void setEngineTarget(float v) {
        if (world.isClient && engineTarget != v) {
            NetworkHandler.sendToServer(new EnginePowerMessage(v));
        }
        engineTarget = v;
    }
}
