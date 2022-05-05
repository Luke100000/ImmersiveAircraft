package immersive_airships.entity;

import immersive_airships.cobalt.network.NetworkHandler;
import immersive_airships.network.c2s.EnginePowerMessage;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public abstract class EngineAircraft extends AirshipEntity {
    private int oldLevel;

    static final TrackedData<Float> ENGINE = DataTracker.registerData(EngineAircraft.class, TrackedDataHandlerRegistry.FLOAT);

    float engineTarget = 0.0f;

    public EngineAircraft(EntityType<? extends AirshipEntity> entityType, World world) {
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
