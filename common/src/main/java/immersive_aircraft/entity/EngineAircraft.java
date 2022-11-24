package immersive_aircraft.entity;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.network.c2s.EnginePowerMessage;
import immersive_aircraft.util.InterpolatedFloat;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public abstract class EngineAircraft extends AircraftEntity {
    private int oldLevel;

    static final TrackedData<Float> ENGINE = DataTracker.registerData(EngineAircraft.class, TrackedDataHandlerRegistry.FLOAT);

    float engineTarget = 0.0f;

    public final InterpolatedFloat engineRotation = new InterpolatedFloat();

    public EngineAircraft(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();

        dataTracker.startTracking(ENGINE, 0.0f);
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isClient()) {
            engineRotation.update((engineRotation.getValue() + getEnginePower()) % 1000);
        } else {
            // shutdown
            if (!hasPassengers()) {
                setEngineTarget(0.0f);
            }

            // start engine
            if (engineTarget >= getEnginePower()) {
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
    void updateController() {
        // left-right
        yawVelocity += getProperties().getYawSpeed() * movementX;
        setYaw(getYaw() + yawVelocity);

        // up-down
        if (location != Location.ON_LAND && movementZ != 0) {
            pitchVelocity += getProperties().getPitchSpeed() * movementZ;
        } else {
            setPitch(getPitch() * (1.0f - getProperties().getStabilizer()));
        }
        setPitch(Math.max(-getProperties().getMaxPitch(), Math.min(getProperties().getMaxPitch(), getPitch() + pitchVelocity)));
    }

    @Override
    void updateVelocity() {
        super.updateVelocity();

        // landing
        if (location == Location.ON_LAND) {
            setPitch((getPitch() + getProperties().getGroundPitch()) * 0.9f - getProperties().getGroundPitch());
        }
    }

    public float getEnginePower() {
        return dataTracker.get(ENGINE);
    }

    public void setEnginePower(float power) {
        dataTracker.set(ENGINE, power);
    }

    public float getEngineTarget() {
        return engineTarget;
    }

    public void setEngineTarget(float engineTarget) {
        if (world.isClient && this.engineTarget != engineTarget) {
            NetworkHandler.sendToServer(new EnginePowerMessage(engineTarget));
        }
        this.engineTarget = engineTarget;
    }
}
