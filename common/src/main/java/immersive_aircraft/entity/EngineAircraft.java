package immersive_aircraft.entity;

import immersive_aircraft.Sounds;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.network.c2s.EnginePowerMessage;
import immersive_aircraft.util.InterpolatedFloat;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;

/**
 * Simulated engine behavior
 */
public abstract class EngineAircraft extends AircraftEntity {
	protected static final TrackedData<Float> ENGINE = DataTracker.registerData(EngineAircraft.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> UTILIZATION = DataTracker.registerData(EngineAircraft.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> LOW_ON_FUEL = DataTracker.registerData(EngineAircraft.class, TrackedDataHandlerRegistry.BOOLEAN);

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

    public EngineAircraft(EntityType<? extends AircraftEntity> entityType, World world, boolean canExplodeOnCrash) {
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
    protected void initDataTracker() {
        super.initDataTracker();

        dataTracker.startTracking(ENGINE, 0.0f);
        dataTracker.startTracking(UTILIZATION, 0.0f);
        dataTracker.startTracking(LOW_ON_FUEL, false);
    }

    @Override
    public void tick() {
        super.tick();

        // adapt engine reaction time
        enginePower.setSteps(getEngineReactionSpeed() / getTotalUpgrade(AircraftStat.ACCELERATION));

        // spin up the engine
        enginePower.update(getEngineTarget() * (touchingWater ? 0.1f : 1.0f));

        // simulate spin up
        engineSpinUpStrength = Math.max(0.0f, engineSpinUpStrength + enginePower.getDiff() - 0.01f);

        // rotate propeller
        if (world.isClient()) {
            engineRotation.update((engineRotation.getValue() + getEnginePower()) % 1000);
        }

        // shutdown
        if (!hasPassengers() && getEngineTarget() > 0) {
            setEngineTarget(0.0f);
        }

        // Engine sounds
        if (world.isClient) {
            engineSound += getEnginePower() * 0.25f;
            if (engineSound > 1.0f) {
                engineSound--;
                if (isFuelLow()) {
                    engineSound -= random.nextInt(2);
                }
                world.playSound(getX(), getY(), getZ(), getEngineSound(), getSoundCategory(), Math.min(1.0f, 0.25f + engineSpinUpStrength), (random.nextFloat() * 0.1f + 0.95f) * getEnginePitch(), false);
            }
        }

        // Fuel
        if (fuel.length > 0 && !world.isClient) {
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
        if (hasPassengers()) {
            refuel();

            // Fuel notification
            if (getControllingPassenger() instanceof ServerPlayerEntity player) {
                float utilization = getFuelUtilization();
                if (utilization > 0 && isFuelLow()) {
                    if (lastFuelState != FuelState.LOW) {
                        player.sendMessage(Text.translatable("immersive_aircraft." + getFuelType() + ".low"), true);
                        lastFuelState = FuelState.LOW;
                    }
                } else if (utilization > 0) {
                    lastFuelState = FuelState.FUELED;
                } else {
                    if (lastFuelState != FuelState.EMPTY) {
                        player.sendMessage(Text.translatable("immersive_aircraft." + getFuelType() + "." + (lastFuelState == FuelState.FUELED ? "out" : "none")), true);
                        lastFuelState = FuelState.EMPTY;
                    }
                }
            }
        } else {
            lastFuelState = FuelState.NEVER;
        }
    }

    protected boolean isFuelLow() {
        if (world.isClient) {
            return dataTracker.get(LOW_ON_FUEL);
        } else {
            boolean low = true;
            for (int i : fuel) {
                if (i > LOW_FUEL) {
                    low = false;
                    break;
                }
            }
            dataTracker.set(LOW_ON_FUEL, low);
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
            ItemStack stack = inventory.getStack(slots.get(i).index);
            int time = getFuelTime(stack);
            if (time > 0) {
                fuel[i] += time;
                Item item = stack.getItem();
                stack.decrement(1);
                if (stack.isEmpty()) {
                    Item item2 = item.getRecipeRemainder();
                    inventory.setStack(slots.get(i).index, item2 == null ? ItemStack.EMPTY : new ItemStack(item2));
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
        setYaw(getYaw() - getProperties().getYawSpeed() * pressingInterpolatedX.getSmooth());

        // forwards-backwards
        if (!onGround) {
            setPitch(getPitch() + getProperties().getPitchSpeed() * pressingInterpolatedZ.getSmooth());
        }
        setPitch(getPitch() * (1.0f - getStabilizer()));
    }

    @Override
    protected void updateVelocity() {
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
        if (getFuelUtilization() > 0 || engineTarget == 0) {
            if (world.isClient) {
                if (getEngineTarget() != engineTarget) {
                    NetworkHandler.sendToServer(new EnginePowerMessage(engineTarget));
                }
                if (getFuelUtilization() > 0 && getEngineTarget() == 0.0 && engineTarget > 0) {
                    world.playSound(getX(), getY(), getZ(), getEngineStartSound(), getSoundCategory(), 1.0f, getEnginePitch(), false);
                }
            }
            dataTracker.set(ENGINE, engineTarget);
        }
    }

    public static Map<Item, Integer> cachedFuels;

    public static int getFuelTime(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        }
        Item item = fuel.getItem();

        // Build vanilla fuel map
        if (cachedFuels == null) {
            cachedFuels = AbstractFurnaceBlockEntity.createFuelTimeMap();
        }

        // Vanilla fuel
        if (Config.getInstance().acceptVanillaFuel && cachedFuels.containsKey(item)) {
            return cachedFuels.get(item);
        }

        // Custom fuel
        return Config.getInstance().fuelList.getOrDefault(Registries.ITEM.getId(item).toString(), 0);
    }

    public float getFuelUtilization() {
        if (Config.getInstance().fuelConsumption == 0) {
            return 1.0f;
        }
        if (!Config.getInstance().burnFuelInCreative && getControllingPassenger() instanceof PlayerEntity player && player.isCreative()) {
            return 1.0f;
        }
        if (world.isClient) {
            return dataTracker.get(UTILIZATION);
        } else {
            int running = 0;
            for (int i : fuel) {
                if (i > 0) {
                    running++;
                }
            }
            float utilization = (float)running / fuel.length * (isFuelLow() ? 0.75f : 1.0f);
            dataTracker.set(UTILIZATION, utilization);
            return utilization;
        }
    }
}
