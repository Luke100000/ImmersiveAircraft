package immersive_aircraft.entity;

import immersive_aircraft.WeaponRegistry;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.config.Config;
import immersive_aircraft.data.VehicleDataLoader;
import immersive_aircraft.entity.inventory.SparseSimpleInventory;
import immersive_aircraft.entity.inventory.VehicleInventoryDescription;
import immersive_aircraft.entity.inventory.slots.SlotDescription;
import immersive_aircraft.entity.misc.VehicleProperties;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.entity.weapon.Telescope;
import immersive_aircraft.entity.weapon.Weapon;
import immersive_aircraft.item.WeaponItem;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_aircraft.item.upgrade.VehicleUpgrade;
import immersive_aircraft.item.upgrade.VehicleUpgradeRegistry;
import immersive_aircraft.mixin.ServerPlayerEntityMixin;
import immersive_aircraft.network.s2c.OpenGuiRequest;
import immersive_aircraft.screen.VehicleScreenHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

public abstract class InventoryVehicleEntity extends DyeableVehicleEntity implements ContainerListener, MenuProvider, Container {
    private final VehicleProperties properties;
    private SparseSimpleInventory inventory;
    protected final Map<Integer, List<Weapon>> weapons = new HashMap<>();

    public InventoryVehicleEntity(EntityType<? extends InventoryVehicleEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);

        this.initInventory();

        this.properties = new VehicleProperties(VehicleDataLoader.get(identifier).getProperties(), this);
    }

    public VehicleProperties getProperties() {
        return properties;
    }

    public VehicleInventoryDescription getInventoryDescription() {
        return VehicleDataLoader.get(identifier).getInventoryDescription();
    }

    private static final List<WeaponMount> EMPTY_WEAPONS = List.of(WeaponMount.EMPTY);
    private static final Map<WeaponMount.Type, List<WeaponMount>> EMPTY_WEAPONS_MAP = Map.of();

    public List<WeaponMount> getWeaponMounts(int slot) {
        ItemStack stack = getSlot(slot).get();
        if (stack.getItem() instanceof WeaponItem weaponItem) {
            return VehicleDataLoader.get(identifier).getWeaponMounts().getOrDefault(slot, EMPTY_WEAPONS_MAP).getOrDefault(weaponItem.getMountType(), EMPTY_WEAPONS);
        }
        return EMPTY_WEAPONS;
    }

    public List<ItemStack> getSlots(String slotType) {
        List<SlotDescription> slots = getInventoryDescription().getSlots(slotType);
        List<ItemStack> list = new ArrayList<>(slots.size());
        for (SlotDescription slot : slots) {
            list.add(getInventory().getItem(slot.index()));
        }
        return list;
    }

    //todo cache?
    public float getTotalUpgrade(VehicleStat stat) {
        float value = 1.0f;
        List<ItemStack> upgrades = getSlots(VehicleInventoryDescription.UPGRADE);
        for (int step = 0; step < 2; step++) {
            for (ItemStack stack : upgrades) {
                VehicleUpgrade upgrade = VehicleUpgradeRegistry.INSTANCE.getUpgrade(stack.getItem());
                if (upgrade != null) {
                    float u = upgrade.get(stat);

                    if (u > 0 && step == 1)
                        value += u;
                    else if (u < 0 && step == 0)
                        value *= (u + 1);
                }
            }
        }
        return Math.max(0.0f, value);
    }

    protected void initInventory() {
        this.inventory = new SparseSimpleInventory(getInventoryDescription().getInventorySize());
        this.inventory.addListener(this);
    }

    public SparseSimpleInventory getInventory() {
        int inventorySize = getInventoryDescription().getInventorySize();
        if (inventorySize != inventory.getContainerSize()) {
            initInventory();
        }
        return inventory;
    }

    @Override
    public void containerChanged(Container sender) {

    }

    @Override
    protected void dropInventory() {
        for (SlotDescription slot : getInventoryDescription().getSlots()) {
            boolean isCargo = slot.type().equals(VehicleInventoryDescription.INVENTORY);
            if (isCargo && Config.getInstance().dropInventory || !isCargo && Config.getInstance().dropUpgrades) {
                ItemStack stack = getSlot(slot.index()).get();
                if (!stack.isEmpty()) {
                    this.spawnAtLocation(stack.copyAndClear());
                }
            }
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new VehicleScreenHandler(i, playerInventory, this);
    }

    public void openInventory(ServerPlayer player) {
        player.nextContainerCounter();
        AbstractContainerMenu screenHandler = createMenu(player.containerCounter, player.getInventory(), player);
        if (screenHandler != null) {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(this, screenHandler.containerId), player);
            player.containerMenu = screenHandler;
            ServerPlayerEntityMixin playerAccessor = (ServerPlayerEntityMixin) player;
            screenHandler.setSynchronizer(playerAccessor.getContainerSynchronizer());
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (getHealth() >= 1.0) {
            if (!player.level().isClientSide && player.isSecondaryUseActive() && !isPassengerOfSameVehicle(player)) {
                Entity primaryPassenger = getFirstPassenger();
                if (primaryPassenger != null) {
                    // Kick out the first passenger
                    primaryPassenger.stopRiding();
                } else {
                    // Open inventory instead
                    openInventory((ServerPlayer) player);
                }
                return InteractionResult.CONSUME;
            } else if (getPassengerSpace() == 0 && player instanceof ServerPlayer serverPlayer) {
                // For vehicles without passengers, just open inventory
                openInventory(serverPlayer);
            }
        }
        return super.interact(player, hand);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.put("Inventory", getInventory().writeNbt(new ListTag()));
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        ListTag nbtList = tag.getList("Inventory", 10);
        getInventory().readNbt(nbtList);
    }

    @Override
    protected void addItemTag(@NotNull CompoundTag tag) {
        super.addItemTag(tag);

        tag.put("Inventory", getInventory().writeNbt(new ListTag()));
    }

    @Override
    protected void readItemTag(@NotNull CompoundTag tag) {
        super.readItemTag(tag);

        ListTag nbtList = tag.getList("Inventory", 10);
        getInventory().readNbt(nbtList);
    }

    @Override
    public void boost() {
        int length = getSlots(VehicleInventoryDescription.BOOSTER).stream().mapToInt(s -> {
            byte l = s.getOrCreateTagElement("Fireworks").getByte("Flight");
            s.shrink(1);
            return l;
        }).sum();

        super.boost(length * 50);
    }

    @Override
    protected void applyBoost() {
        super.applyBoost();

        // boost
        Vector3f direction = getForwardDirection();
        float thrust = 0.05f * getBoost() / 100.0f;
        setDeltaMovement(getDeltaMovement().add(toVec3d(direction.mul(thrust))));

        // particles
        if (tickCount % 2 == 0) {
            Vec3 p = position();
            Vec3 velocity = getDeltaMovement().subtract(toVec3d(direction));
            level().addParticle(ParticleTypes.FIREWORK, p.x(), p.y(), p.z(), velocity.x, velocity.y, velocity.z);
        }
    }

    @Override
    public boolean canBoost() {
        return getSlots(VehicleInventoryDescription.BOOSTER).stream().anyMatch(v -> !v.isEmpty()) && getBoost() <= 0;
    }

    @Override
    public void tick() {
        getInventory().tick(this);

        // Check and recreate weapon slots
        for (SlotDescription slot : getInventoryDescription().getSlots(VehicleInventoryDescription.WEAPON)) {
            ItemStack weaponItemStack = getSlot(slot.index()).get();
            List<Weapon> weapon = weapons.get(slot.index());

            if (weaponItemStack.isEmpty() && weapon != null) {
                weapons.remove(slot.index());
            } else if (!weaponItemStack.isEmpty() && (weapon == null || weapon.get(0).getStack() != weaponItemStack)) {
                WeaponRegistry.WeaponConstructor constructor = WeaponRegistry.get(weaponItemStack);
                if (constructor != null) {
                    List<WeaponMount> weaponMounts = getWeaponMounts(slot.index());
                    ArrayList<Weapon> weapons = new ArrayList<>(weaponMounts.size());
                    for (WeaponMount weaponMount : weaponMounts) {
                        weapons.add(constructor.create(this, weaponItemStack, weaponMount, slot.index()));
                    }
                    this.weapons.put(slot.index(), weapons);
                }
            }
        }

        // Update gunner offsets
        // The first weapon is assigned to the last passenger, the second to the second last, etc.
        // If more weapons than passengers are available, the remaining weapons are assigned to the driver
        int gunnerOffset = getPassengers().size();
        for (List<Weapon> weapons : getWeapons().values()) {
            gunnerOffset--;
            for (Weapon weapon : weapons) {
                weapon.setGunnerOffset(Math.max(0, gunnerOffset));
            }
        }

        // Update weapons
        for (List<Weapon> weapons : weapons.values()) {
            for (Weapon w : weapons) {
                w.tick();
            }
        }

        super.tick();
    }

    protected float getGroundDecay() {
        return getProperties().get(VehicleStat.GROUND_FRICTION);
    }

    protected void applyFriction() {
        // Decay is the basic factor of friction, basically the density of the material slowing down the vehicle
        float decay = 1.0f - getProperties().get(VehicleStat.FRICTION);
        float gravity = getGravity();
        if (wasTouchingWater) {
            gravity *= 0.25f;
            decay = 0.9f;
        } else if (onGround()) {
            if (isVehicle()) {
                decay = getGroundDecay();
            } else {
                decay = 0.75f;
            }
        }

        // Velocity decay
        Vec3 velocity = getDeltaMovement();
        float hd = getProperties().get(VehicleStat.HORIZONTAL_DECAY);
        float vd = getProperties().get(VehicleStat.VERTICAL_DECAY);
        setDeltaMovement(velocity.x * decay * hd, velocity.y * decay * vd + gravity, velocity.z * decay * hd);

        // Rotation decay
        float rf = decay * getProperties().get(VehicleStat.ROTATION_DECAY);
        pressingInterpolatedX.decay(0.0f, 1.0f - rf);
        pressingInterpolatedZ.decay(0.0f, 1.0f - rf);
    }

    @Override
    public SlotAccess getSlot(int slot) {
        return SlotAccess.forContainer(getInventory(), slot);
    }

    public Map<Integer, List<Weapon>> getWeapons() {
        return weapons;
    }

    @Override
    public float getDurability() {
        return getProperties().get(VehicleStat.DURABILITY);
    }

    public boolean isScoping() {
        Collection<List<Weapon>> values = getWeapons().values();
        for (List<Weapon> weapons : values) {
            for (Weapon weapon : weapons) {
                if (weapon instanceof Telescope telescope && telescope.isScoping()) {
                    return true;
                }
            }
        }
        return false;
    }

    // Inventory proxy methods

    @Override
    public int getContainerSize() {
        return inventory.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return inventory.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return inventory.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return inventory.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.setItem(slot, stack);
    }

    @Override
    public void setChanged() {
        inventory.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory.stillValid(player);
    }

    @Override
    public void clearContent() {
        inventory.clearContent();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        SlotDescription slotType = getInventoryDescription().getSlots().get(index);
        Slot slot = slotType.getSlot(this, inventory);
        return slot.mayPlace(stack);
    }

    @Override
    public boolean canTakeItem(Container target, int index, ItemStack stack) {
        SlotDescription slotType = getInventoryDescription().getSlots().get(index);
        return slotType.type().equals(VehicleInventoryDescription.INVENTORY);
    }

    public void clientFireWeapons(Entity entity) {
        int gunnerIndex = getPassengers().indexOf(entity);
        for (List<Weapon> weapons : getWeapons().values()) {
            int index = 0;
            for (Weapon weapon : weapons) {
                if (weapon.getGunnerOffset() == gunnerIndex) {
                    weapon.clientFire(index++);
                }
            }
        }
    }

    public void fireWeapon(int slot, int index, Vector3f direction) {
        getWeapons().get(slot).get(index).fire(direction);
    }
}
