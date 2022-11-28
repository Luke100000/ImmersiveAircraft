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

/**
 * Simulated engine behavior
 */
public abstract class EngineAircraft extends AircraftEntity {
    private int oldLevel;

    static final TrackedData<Float> ENGINE = DataTracker.registerData(EngineAircraft.class, TrackedDataHandlerRegistry.FLOAT);

    public final InterpolatedFloat engineRotation = new InterpolatedFloat();
    public final InterpolatedFloat enginePower = new InterpolatedFloat(20.0f);
    public float engineSpinupStrength = 0.0f;

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

        // spin up the engine
        enginePower.update(getEngineTarget());

        // simulate spinup
        engineSpinupStrength = Math.max(0.0f, engineSpinupStrength + enginePower.getDiff() - 0.01f);

        if (world.isClient()) {
            engineRotation.update((engineRotation.getValue() + getEnginePower()) % 1000);
        } else {
            // shutdown
            if (!hasPassengers()) {
                setEngineTarget(0.0f);
            }

            // sounds
            if (getEngineTarget() > 0.0f) {
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
        setYaw(getYaw() - getProperties().getYawSpeed() * pressingInterpolatedX.getSmooth());

        // forwards-backwards
        if (location != Location.ON_LAND) {
            setPitch(getPitch() + getProperties().getPitchSpeed() * pressingInterpolatedZ.getSmooth());
        }
        setPitch(getPitch() * (1.0f - getProperties().getStabilizer()));
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
        return enginePower.getSmooth();
    }

    public float getEngineTarget() {
        return dataTracker.get(ENGINE);
    }

    public void setEngineTarget(float engineTarget) {
        if (world.isClient) {
            if (getEngineTarget() != engineTarget) {
                NetworkHandler.sendToServer(new EnginePowerMessage(engineTarget));
            }
        }
        dataTracker.set(ENGINE, engineTarget);
    }
}
