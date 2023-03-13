package immersive_aircraft.entity;

import immersive_aircraft.Sounds;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.network.c2s.EnginePowerMessage;
import immersive_aircraft.util.InterpolatedFloat;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

import java.util.List;

/**
 * Simulated engine behavior
 */
public abstract class EngineAircraft extends AircraftEntity {
    static final TrackedData<Float> ENGINE = DataTracker.registerData(EngineAircraft.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> UTILIZATION = DataTracker.registerData(EngineAircraft.class, TrackedDataHandlerRegistry.FLOAT);

    public final InterpolatedFloat engineRotation = new InterpolatedFloat();
    public final InterpolatedFloat enginePower = new InterpolatedFloat(20.0f);
    public float engineSpinUpStrength = 0.0f;
    public float engineSound = 0.0f;

    private final int[] fuel;

    public enum GUI_STYLE {
        NONE,
        ENGINE
    }

    public GUI_STYLE getGuiStyle() {
        return GUI_STYLE.ENGINE;
    }

    public EngineAircraft(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);

        fuel = new int[getInventoryDescription().getSlots(VehicleInventoryDescription.SlotType.BOILER).size()];
    }

    SoundEvent getEngineStartSound() {
        return Sounds.ENGINE_START.get();
    }

    SoundEvent getEngineSound() {
        return Sounds.PROPELLER.get();
    }

    float getEnginePitch() {
        return 1.0f;
    }

    float getStabilizer() {
        return 0.0f;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();

        dataTracker.startTracking(ENGINE, 0.0f);
        dataTracker.startTracking(UTILIZATION, 0.0f);
    }

    @Override
    public void tick() {
        super.tick();

        // spin up the engine
        enginePower.update(getEngineTarget() * (touchingWater ? 0.1f : 1.0f));

        // simulate spin up
        engineSpinUpStrength = Math.max(0.0f, engineSpinUpStrength + enginePower.getDiff() - 0.01f);

        if (world.isClient()) {
            engineRotation.update((engineRotation.getValue() + getEnginePower()) % 1000);
        } else {
            // shutdown
            if (!hasPassengers()) {
                setEngineTarget(0.0f);
            }
        }

        // Engine sounds
        if (world.isClient) {
            engineSound += getEnginePower() * 0.25f;
            if (engineSound > 1.0f) {
                engineSound--;
                world.playSound(getX(), getY(), getZ(), getEngineSound(), getSoundCategory(), Math.min(1.0f, 0.25f + engineSpinUpStrength), (random.nextFloat() * 0.1f + 0.95f) * getEnginePitch(), false);
            }
        }

        // Fuel
        float consumption = getEngineTarget();
        if (!world.isClient && consumption > 0) {
            if (random.nextFloat() < consumption) {
                for (int i = 0; i < fuel.length; i++) {
                    if (fuel[i] > 0) {
                        fuel[i]--;
                    } else {
                        List<VehicleInventoryDescription.Slot> slots = getInventoryDescription().getSlots(VehicleInventoryDescription.SlotType.BOILER);
                        ItemStack stack = inventory.getStack(slots.get(i).index);
                        int time = getFuelTime(stack);
                        if (time > 0) {
                            fuel[i] += time;
                            stack.decrement(1);
                        }
                    }
                }
            }
        }
    }

    @Override
    void updateController() {
        // left-right
        setYaw(getYaw() - getProperties().getYawSpeed() * pressingInterpolatedX.getSmooth());

        // forwards-backwards
        if (!onGround) {
            setPitch(getPitch() + getProperties().getPitchSpeed() * pressingInterpolatedZ.getSmooth());
        }
        setPitch(getPitch() * (1.0f - getStabilizer()));
    }

    @Override
    void updateVelocity() {
        super.updateVelocity();

        // landing
        if (onGround) {
            setPitch((getPitch() + getProperties().getGroundPitch()) * 0.9f - getProperties().getGroundPitch());
        }
    }

    public float getEnginePower() {
        return (float)(enginePower.getSmooth() * Math.sqrt(getFuelUtilization()));
    }

    public float getEngineTarget() {
        return dataTracker.get(ENGINE);
    }

    public void setEngineTarget(float engineTarget) {
        if (world.isClient) {
            if (getEngineTarget() != engineTarget) {
                NetworkHandler.sendToServer(new EnginePowerMessage(engineTarget));
            }

            if (getEngineTarget() == 0.0 && engineTarget > 0) {
                world.playSound(getX(), getY(), getZ(), getEngineStartSound(), getSoundCategory(), 1.0f, getEnginePitch(), false);
            }
        }
        dataTracker.set(ENGINE, engineTarget);
    }

    private int getFuelTime(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        }
        Item item = fuel.getItem();
        return AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(item, 0);
    }

    public float getFuelUtilization() {
        if (world.isClient) {
            return dataTracker.get(UTILIZATION);
        } else {
            int running = 0;
            for (int i : fuel) {
                if (i > 0) {
                    running++;
                }
            }
            float utilization = (float)running / fuel.length;
            dataTracker.set(UTILIZATION, utilization);
            return utilization;
        }
    }
}
