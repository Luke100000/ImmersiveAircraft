package immersive_aircraft.entity;

import immersive_aircraft.Main;
import immersive_aircraft.Sounds;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.network.c2s.EnginePowerMessage;
import immersive_aircraft.util.InterpolatedFloat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;

/**
 * Simulated engine behavior
 */
public abstract class EngineAircraft extends AircraftEntity {
	protected static final DataParameter<Float> ENGINE = EntityDataManager.createKey(EngineAircraft.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> UTILIZATION = EntityDataManager.createKey(EngineAircraft.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> LOW_ON_FUEL = EntityDataManager.createKey(EngineAircraft.class, DataSerializers.BOOLEAN);

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

    public final static int TARGET_FUEL = 1000;
    public final static int LOW_FUEL = 900;

    private final int[] fuel;

    public enum GUI_STYLE {
        NONE,
        ENGINE
    }

    public GUI_STYLE getGuiStyle() {
        return GUI_STYLE.ENGINE;
    }

    public EngineAircraft(World world) {
        super(world);

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
    protected void entityInit() {
        super.entityInit();

        dataManager.register(ENGINE, 0.0f);
        dataManager.register(UTILIZATION, 0.0f);
        dataManager.register(LOW_ON_FUEL, false);
    }

    /**
     * Flight-physics trace, off unless the JVM is started with
     * {@code -Dimmersive_aircraft.debugFlight=true}. Logs the state each side actually
     * simulates, which is the only way to tell a physics problem apart from a
     * client/server authority problem.
     */
    private static final boolean DEBUG_FLIGHT = Boolean.getBoolean("immersive_aircraft.debugFlight");

    private void debugFlight() {
        if (ticksExisted % 5 != 0 || !isBeingRidden()) {
            return;
        }
        Main.LOGGER.info(String.format(
                "[IA] %s steer=%b onGround=%b pitch=%.1f roll=%.1f speed=%.4f motionY=%+.4f engine=%.2f/%.2f fuel=%.2f in=(%.0f,%.0f,%.0f)",
                world.isRemote ? "CLIENT" : "SERVER",
                canPassengerSteer(), onGround, getPitch(), getRoll(),
                getVelocity().length(), motionY,
                enginePower.getSmooth(), getEngineTarget(), getFuelUtilization(),
                movementX, movementY, movementZ));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (DEBUG_FLIGHT) {
            debugFlight();
        }

        // adapt engine reaction time
        enginePower.setSteps(getEngineReactionSpeed() / getTotalUpgrade(AircraftStat.ACCELERATION));

        // spin up the engine
        enginePower.update(getEngineTarget() * (inWater ? 0.1f : 1.0f));

        // simulate spin up
        engineSpinUpStrength = Math.max(0.0f, engineSpinUpStrength + enginePower.getDiff() - 0.01f);

        // rotate propeller
        if (world.isRemote) {
            engineRotation.update((engineRotation.getValue() + getEnginePower()) % 1000);
        }

        // shutdown
        if (!isBeingRidden() && getEngineTarget() > 0) {
            setEngineTarget(0.0f);
        }

        // Engine sounds
        if (world.isRemote) {
            engineSound += getEnginePower() * 0.25f;
            if (engineSound > 1.0f) {
                engineSound--;
                if (isFuelLow()) {
                    engineSound -= rand.nextInt(2);
                }
                world.playSound(getX(), getY(), getZ(), getEngineSound(), getSoundCategory(), Math.min(1.0f, 0.25f + engineSpinUpStrength), (rand.nextFloat() * 0.1f + 0.95f) * getEnginePitch(), false);
            }
        }

        // Fuel
        if (fuel.length > 0 && !world.isRemote) {
            float consumption = getFuelConsumption();
            while (consumption > 0 && (consumption >= 1 || rand.nextFloat() < consumption)) {
                for (int i = 0; i < fuel.length; i++) {
                    if (fuel[i] > 0) {
                        fuel[i]--;
                    }
                }
                consumption--;
            }
        }

        // Refuel
        if (isBeingRidden()) {
            refuel();

            // Fuel notification
            if (getControllingPassenger() instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP)getControllingPassenger();
                float utilization = getFuelUtilization();
                if (utilization > 0 && isFuelLow()) {
                    if (lastFuelState != FuelState.LOW) {
                        player.sendStatusMessage(new TextComponentTranslation("immersive_aircraft." + getFuelType() + ".low"), true);
                        lastFuelState = FuelState.LOW;
                    }
                } else if (utilization > 0) {
                    lastFuelState = FuelState.FUELED;
                } else {
                    if (lastFuelState != FuelState.EMPTY) {
                        player.sendStatusMessage(new TextComponentTranslation("immersive_aircraft." + getFuelType() + "." + (lastFuelState == FuelState.FUELED ? "out" : "none")), true);
                        lastFuelState = FuelState.EMPTY;
                    }
                }
            }
        } else {
            lastFuelState = FuelState.NEVER;
        }
    }

    protected boolean isFuelLow() {
        if (world.isRemote) {
            return dataManager.get(LOW_ON_FUEL);
        } else {
            boolean low = true;
            for (int i : fuel) {
                if (i > LOW_FUEL) {
                    low = false;
                    break;
                }
            }
            dataManager.set(LOW_ON_FUEL, low);
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
                stack.shrink(1);
                if (stack.isEmpty()) {
                    Item item2 = item.getContainerItem();
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
        return dataManager.get(ENGINE);
    }

    public void setEngineTarget(float engineTarget) {
        if (getFuelUtilization() > 0 || engineTarget == 0) {
            if (world.isRemote) {
                if (getEngineTarget() != engineTarget) {
                    NetworkHandler.sendToServer(new EnginePowerMessage(engineTarget));
                }
                if (getFuelUtilization() > 0 && getEngineTarget() == 0.0 && engineTarget > 0) {
                    world.playSound(getX(), getY(), getZ(), getEngineStartSound(), getSoundCategory(), 1.0f, getEnginePitch(), false);
                }
            }
            dataManager.set(ENGINE, engineTarget);
        }
    }

    public static int getFuelTime(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        }
        Item item = fuel.getItem();

        // Vanilla fuel
        if (Config.getInstance().acceptVanillaFuel) {
            int time = TileEntityFurnace.getItemBurnTime(fuel);
            if (time > 0) {
                return time;
            }
        }

        // Custom fuel
        Integer time = Config.getInstance().fuelList.get(Item.REGISTRY.getNameForObject(item).toString());
        return time == null ? 0 : time;
    }

    public float getFuelUtilization() {
        if (Config.getInstance().fuelConsumption == 0) {
            return 1.0f;
        }
        if (!Config.getInstance().burnFuelInCreative && getControllingPassenger() instanceof EntityPlayer && ((EntityPlayer)getControllingPassenger()).isCreative()) {
            return 1.0f;
        }
        if (world.isRemote) {
            return dataManager.get(UTILIZATION);
        } else {
            int running = 0;
            for (int i : fuel) {
                if (i > 0) {
                    running++;
                }
            }
            float utilization = (float)running / fuel.length * (isFuelLow() ? 0.75f : 1.0f);
            dataManager.set(UTILIZATION, utilization);
            return utilization;
        }
    }
}
