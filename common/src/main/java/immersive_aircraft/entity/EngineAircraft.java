package immersive_aircraft.entity;

import immersive_aircraft.Sounds;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.network.c2s.EnginePowerMessage;
import immersive_aircraft.util.InterpolatedFloat;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import dev.architectury.registry.fuel.FuelRegistry;

import java.util.List;

/**
 * Simulated engine behavior
 */
public abstract class EngineAircraft extends AircraftEntity {
    protected static final EntityDataAccessor<Float> ENGINE = SynchedEntityData.defineId(EngineAircraft.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> UTILIZATION = SynchedEntityData.defineId(EngineAircraft.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> LOW_ON_FUEL = SynchedEntityData.defineId(EngineAircraft.class, EntityDataSerializers.BOOLEAN);

    public final InterpolatedFloat engineRotation = new InterpolatedFloat();
    public final InterpolatedFloat enginePower = new InterpolatedFloat(20.0f);
    public float engineSpinUpStrength = 0.0f;
    public float engineSound = 0.0f;

    protected enum FuelState {
        NEVER,
        EMPTY,
        FUELED,
        LOW
    }

    FuelState lastFuelState = FuelState.NEVER;

    public static final int TARGET_FUEL = 1000;
    public static final int LOW_FUEL = 900;

    private final int[] fuel;

    public enum GUI_STYLE {
        NONE,
        ENGINE
    }

    public GUI_STYLE getGuiStyle() {
        return GUI_STYLE.ENGINE;
    }

    public EngineAircraft(EntityType<? extends AircraftEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);

        fuel = new int[getInventoryDescription().getSlots(VehicleInventoryDescription.SlotType.BOILER).size()];
    }

    protected SoundEvent getEngineStartSound() {
        return Sounds.ENGINE_START.get();
    }

    protected SoundEvent getEngineSound() {
        return Sounds.PROPELLER.get();
    }

    protected float getEnginePitch() {
        return 1.0f;
    }

    protected float getStabilizer() {
        return 0.0f;
    }

    protected float getBaseFuelConsumption() {
        return 0.75f;
    }

    protected float getEngineReactionSpeed() {
        return 20.0f;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        entityData.define(ENGINE, 0.0f);
        entityData.define(UTILIZATION, 0.0f);
        entityData.define(LOW_ON_FUEL, false);
    }

    @Override
    public void tick() {
        super.tick();

        // adapt engine reaction time
        enginePower.setSteps(getEngineReactionSpeed() / getTotalUpgrade(AircraftStat.ACCELERATION));

        // spin up the engine
        enginePower.update(getEngineTarget() * (wasTouchingWater ? 0.1f : 1.0f));

        // simulate spin up
        engineSpinUpStrength = Math.max(0.0f, engineSpinUpStrength + enginePower.getDiff() - 0.01f);

        // rotate propeller
        if (level.isClientSide()) {
            engineRotation.update((engineRotation.getValue() + getEnginePower()) % 1000);
        }

        // shutdown
        if (!isVehicle() && getEngineTarget() > 0) {
            setEngineTarget(0.0f);
        }

        // Engine sounds
        if (level.isClientSide) {
            engineSound += getEnginePower() * 0.25f;
            if (engineSound > 1.0f) {
                engineSound--;
                if (isFuelLow()) {
                    engineSound -= random.nextInt(2);
                }
                level.playLocalSound(getX(), getY(), getZ(), getEngineSound(), getSoundSource(), Math.min(1.0f, 0.25f + engineSpinUpStrength), (random.nextFloat() * 0.1f + 0.95f) * getEnginePitch(), false);
            }
        }

        // Fuel
        if (fuel.length > 0 && !level.isClientSide) {
            float consumption = getFuelConsumption();
            while (consumption > 0 && (consumption >= 1 || random.nextFloat() < consumption)) {
                for (int i = 0; i < fuel.length; i++) {
                    if (fuel[i] > 0) {
                        fuel[i]--;
                    }
                }
                consumption--;
            }
        }

        // Refuel
        if (isVehicle()) {
            refuel();

            // Fuel notification
            if (getControllingPassenger() instanceof ServerPlayer player) {
                float utilization = getFuelUtilization();
                if (utilization > 0 && isFuelLow()) {
                    if (lastFuelState != FuelState.LOW) {
                        player.displayClientMessage(Component.translatable("immersive_aircraft." + getFuelType() + ".low"), true);
                        lastFuelState = FuelState.LOW;
                    }
                } else if (utilization > 0) {
                    lastFuelState = FuelState.FUELED;
                } else {
                    if (lastFuelState != FuelState.EMPTY) {
                        player.displayClientMessage(Component.translatable("immersive_aircraft." + getFuelType() + "." + (lastFuelState == FuelState.FUELED ? "out" : "none")), true);
                        lastFuelState = FuelState.EMPTY;
                    }
                }
            }
        } else {
            lastFuelState = FuelState.NEVER;
        }
    }

    protected boolean isFuelLow() {
        if (level.isClientSide) {
            return entityData.get(LOW_ON_FUEL);
        } else {
            boolean low = true;
            for (int i : fuel) {
                if (i > LOW_FUEL) {
                    low = false;
                    break;
                }
            }
            entityData.set(LOW_ON_FUEL, low);
            return low;
        }
    }

    protected String getFuelType() {
        return "fuel";
    }

    float getFuelConsumption() {
        return getEngineTarget() * getTotalUpgrade(AircraftStat.FUEL) * getBaseFuelConsumption() * Config.getInstance().fuelConsumption;
    }

    private void refuel(int i) {
        while (fuel[i] <= TARGET_FUEL) {
            List<VehicleInventoryDescription.Slot> slots = getInventoryDescription().getSlots(VehicleInventoryDescription.SlotType.BOILER);
            ItemStack stack = inventory.getItem(slots.get(i).index);
            int time = getFuelTime(stack);
            if (time > 0) {
                fuel[i] += time;
                Item item = stack.getItem();
                stack.shrink(1);
                if (stack.isEmpty()) {
                    Item item2 = item.getCraftingRemainingItem();
                    inventory.setItem(slots.get(i).index, item2 == null ? ItemStack.EMPTY : new ItemStack(item2));
                }
            } else {
                break;
            }
        }
    }

    private void refuel() {
        for (int i = 0; i < fuel.length; i++) {
            refuel(i);
        }
    }

    @Override
    protected void updateController() {
        // left-right
        setYRot(getYRot() - getProperties().getYawSpeed() * pressingInterpolatedX.getSmooth());

        // forwards-backwards
        if (!onGround) {
            setXRot(getXRot() + getProperties().getPitchSpeed() * pressingInterpolatedZ.getSmooth());
        }
        setXRot(getXRot() * (1.0f - getStabilizer()));
    }

    @Override
    protected void updateVelocity() {
        super.updateVelocity();

        // landing
        if (onGround) {
            setXRot((getXRot() + getProperties().getGroundPitch()) * 0.9f - getProperties().getGroundPitch());
        }
    }

    public float getEnginePower() {
        return (float) (enginePower.getSmooth() * Math.sqrt(getFuelUtilization()));
    }

    public float getEngineTarget() {
        return entityData.get(ENGINE);
    }

    public void setEngineTarget(float engineTarget) {
        if (getFuelUtilization() > 0 || engineTarget == 0) {
            if (level.isClientSide) {
                if (getEngineTarget() != engineTarget) {
                    NetworkHandler.sendToServer(new EnginePowerMessage(engineTarget));
                }
                if (getFuelUtilization() > 0 && getEngineTarget() == 0.0 && engineTarget > 0) {
                    level.playLocalSound(getX(), getY(), getZ(), getEngineStartSound(), getSoundSource(), 1.0f, getEnginePitch(), false);
                }
            }
            entityData.set(ENGINE, engineTarget);
        }
    }

    public static int getFuelTime(ItemStack fuel) {
        var fuelList = Config.getInstance().fuelList;

        if(!fuelList.containsKey(fuel.getItem().toString()))
            return FuelRegistry.get(fuel);

        // Custom fuel
        return Config.getInstance().fuelList.getOrDefault(Registry.ITEM.getKey(fuel.getItem()).toString(), 0);
    }

    public float getFuelUtilization() {
        if (Config.getInstance().fuelConsumption == 0) {
            return 1.0f;
        }
        if (!Config.getInstance().burnFuelInCreative && getControllingPassenger() instanceof Player player && player.isCreative()) {
            return 1.0f;
        }
        if (level.isClientSide) {
            return entityData.get(UTILIZATION);
        } else {
            int running = 0;
            for (int i : fuel) {
                if (i > 0) {
                    running++;
                }
            }
            float utilization = (float) running / fuel.length * (isFuelLow() ? 0.75f : 1.0f);
            entityData.set(UTILIZATION, utilization);
            return utilization;
        }
    }
}
