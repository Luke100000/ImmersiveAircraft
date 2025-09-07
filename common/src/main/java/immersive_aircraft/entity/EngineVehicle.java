package immersive_aircraft.entity;

import immersive_aircraft.Sounds;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.inventory.VehicleInventoryDescription;
import immersive_aircraft.entity.inventory.slots.SlotDescription;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_aircraft.network.c2s.EnginePowerMessage;
import immersive_aircraft.resources.bbmodel.BBAnimationVariables;
import immersive_aircraft.util.InterpolatedFloat;
import immersive_aircraft.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.EnumMap;
import java.util.List;

/**
 * Simulated engine behavior
 */
public abstract class EngineVehicle extends InventoryVehicleEntity {
    protected static final EntityDataAccessor<Float> ENGINE = SynchedEntityData.defineId(EngineVehicle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> UTILIZATION = SynchedEntityData.defineId(EngineVehicle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> LOW_ON_FUEL = SynchedEntityData.defineId(EngineVehicle.class, EntityDataSerializers.BOOLEAN);

    public final InterpolatedFloat engineRotation = new InterpolatedFloat();
    public final InterpolatedFloat enginePower = new InterpolatedFloat(20.0f);
    public float engineSpinUpStrength = 0.0f;
    public float engineSound = 0.0f;
    public int mainWarning = 0;
    public int mslWarning = 0;
    public final EnumMap<Cautions, Integer> cautions = new EnumMap<>(Cautions.class);

    protected enum FuelState {
        NEVER,
        EMPTY,
        FUELED,
        LOW
    }

    public enum Cautions {
        PULL_UP,
        VOID,
        DAMAGED
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

    public EngineVehicle(EntityType<? extends EngineVehicle> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);

        fuel = new int[getInventoryDescription().getSlots(VehicleInventoryDescription.BOILER).size()];

        for (EngineVehicle.Cautions c : EngineVehicle.Cautions.values()) {
            cautions.compute(c, (cautions, v) -> 0);
        }
    }

    protected SoundEvent getEngineStartSound() {
        return Sounds.ENGINE_START.get();
    }

    protected SoundEvent getEngineSound() {
        return Sounds.PROPELLER.get();
    }

    protected float getEngineVolume() {
        return 0.25f;
    }

    protected float getEnginePitch() {
        return 1.0f;
    }

    protected float getEngineReactionSpeed() {
        return 20.0f;
    }

    public boolean worksUnderWater() {
        return false;
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
        enginePower.setSteps(getEngineReactionSpeed() / getProperties().get(VehicleStat.ACCELERATION));

        // spin up the engine
        enginePower.update(getEngineTarget() * (isInWater() && !worksUnderWater() ? 0.1f : 1.0f));

        // simulate spin up
        engineSpinUpStrength = Math.max(0.0f, engineSpinUpStrength + enginePower.getDiff() - 0.01f);

        // rotate propeller
        if (level().isClientSide()) {
            engineRotation.update((engineRotation.getValue() + getPropellerSpeed()) % 1000);
        }

        // shutdown
        if (!isVehicle() && getEngineTarget() > 0) {
            setEngineTarget(0.0f);
        }

        // Engine sounds
        if (level().isClientSide) {
            engineSound += getEnginePower() * 0.25f;
            if (engineSound > 1.0f) {
                engineSound--;
                if (isFuelLow()) {
                    engineSound -= random.nextInt(2);
                }
                level().playLocalSound(getX(), getY() + getBbHeight() * 0.5, getZ(), getEngineSound(), getSoundSource(), Math.min(1.0f, getEngineVolume() + engineSpinUpStrength), (random.nextFloat() * 0.1f + 0.95f) * getEnginePitch(), false);
            }
        }

        // Fuel
        if (fuel.length > 0 && !level().isClientSide) {
            float consumption = getFuelConsumption();
            consumeFuel(consumption);
        }

        // Refuel
        if (isVehicle()) {
            if (!level().isClientSide()) {
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
            }
        } else {
            lastFuelState = FuelState.NEVER;
        }

        mainWarning = Math.max(0, mainWarning - 1);
        mslWarning = Math.max(0, mslWarning - 1);
        for (Cautions caution : Cautions.values()) {
            cautions.compute(caution, (cautions, integer) -> integer == null ? 0 : Math.max(0, --integer));
        }

        handleWarnings();
    }

    private void handleWarnings() {
        // Detects sea level.
        // Further updates may introduce GPWS that detects actual ground, which needs a radar upgrade.
        // It is Y-speed relative.
        double altRate = getSpeedVector().y * 10.0d;

        // pull-up caution
        if (getEnginePower() >= 0.5 && altRate < -2 && getY() + altRate * 3 < level().getSeaLevel()) {
            cautions.put(Cautions.PULL_UP, 40);
        }

        // void warning
        if (getY() < level().dimensionType().minY()) {
            cautions.put(Cautions.VOID, 10);
            mainWarning = 6;
        }

        // damaged warning
        if (getHealth() * 100 < 20) {
            cautions.put(Cautions.DAMAGED, 10);
            mainWarning = 6;
        }
    }

    public float consumeFuel(float consumption) {
        while (consumption > 0 && (consumption >= 1 || random.nextFloat() < consumption)) {
            for (int i = 0; i < fuel.length; i++) {
                if (fuel[i] > 0) {
                    fuel[i]--;
                }
            }
            consumption--;
        }
        return consumption;
    }

    public float getPropellerSpeed() {
        return getEnginePower();
    }

    public boolean isFuelLow() {
        if (!Config.getInstance().burnFuelInCreative && isPilotCreative()) {
            return false;
        }

        if (level().isClientSide) {
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

    public String getFuelType() {
        return "fuel";
    }

    public float getFuelConsumption() {
        return getEngineTarget() * getProperties().get(VehicleStat.FUEL) * Config.getInstance().fuelConsumption;
    }

    private void refuel(int i) {
        List<SlotDescription> slots = getInventoryDescription().getSlots(VehicleInventoryDescription.BOILER);
        while (fuel[i] <= TARGET_FUEL && i < slots.size()) {
            ItemStack stack = getInventory().getItem(slots.get(i).index());
            int time = Utils.getFuelTime(stack);
            if (time > 0) {
                fuel[i] += time;
                Item item = stack.getItem();
                stack.shrink(1);
                if (stack.isEmpty()) {
                    Item remainingItem = item.getCraftingRemainingItem();
                    getInventory().setItem(slots.get(i).index(), remainingItem == null ? ItemStack.EMPTY : new ItemStack(remainingItem));
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

    public float getEnginePower() {
        return (float) (enginePower.getSmooth() * Math.sqrt(getFuelUtilization()));
    }

    public float getEngineTarget() {
        return entityData.get(ENGINE);
    }

    public void setEngineTarget(float engineTarget) {
        if (getFuelUtilization() > 0 || engineTarget == 0) {
            if (level().isClientSide) {
                if (getEngineTarget() != engineTarget) {
                    NetworkHandler.sendToServer(new EnginePowerMessage(engineTarget));
                }
                if (getFuelUtilization() > 0 && getEngineTarget() == 0.0 && engineTarget > 0) {
                    level().playLocalSound(getX(), getY() + getBbHeight() * 0.5, getZ(), getEngineStartSound(), getSoundSource(), 1.5f, getEnginePitch(), false);
                }
            }
            entityData.set(ENGINE, engineTarget);
        }
    }

    public float getFuelUtilization() {
        if (Config.getInstance().fuelConsumption == 0) {
            return 1.0f;
        }
        if (!Config.getInstance().burnFuelInCreative && isPilotCreative()) {
            return 1.0f;
        }
        if (fuel.length == 0) {
            return 1.0f;
        }
        if (level().isClientSide) {
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

    public void emitSmokeParticle(float x, float y, float z, float nx, float ny, float nz) {
        if (!isWithinParticleRange() || !level().isClientSide) {
            return;
        }

        Matrix4f transform = getVehicleTransform();
        Matrix3f normalTransform = getVehicleNormalTransform();

        float power = getEnginePower();
        if (power > 0.05) {
            for (int i = 0; i < 1 + engineSpinUpStrength * 4; i++) {
                Vector4f p = transformPosition(transform, x, y, z);
                Vector3f vel = transformVector(normalTransform, nx, ny, nz);
                Vec3 velocity = getDeltaMovement();
                if (random.nextFloat() < engineSpinUpStrength * 0.1) {
                    vel.mul(0.5f);
                    level().addParticle(ParticleTypes.SMALL_FLAME, p.x(), p.y(), p.z(), vel.x() + velocity.x, vel.y() + velocity.y, vel.z() + velocity.z);
                } else {
                    level().addParticle(ParticleTypes.SMOKE, p.x, p.y, p.z, vel.x + velocity.x, vel.y + velocity.y, vel.z + velocity.z);
                }
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        for (int i = 0; i < fuel.length; i++) {
            tag.putInt("Fuel" + i, fuel[i]);
        }
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        for (int i = 0; i < fuel.length; i++) {
            fuel[i] = tag.getInt("Fuel" + i);
        }
    }

    @Override
    public void setAnimationVariables(float tickDelta) {
        super.setAnimationVariables(tickDelta);

        BBAnimationVariables.set("engine_rotation", engineRotation.getSmooth(tickDelta));
    }
}
