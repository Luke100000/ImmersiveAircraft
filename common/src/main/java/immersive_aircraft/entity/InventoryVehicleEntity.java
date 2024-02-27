package immersive_aircraft.entity;

import immersive_aircraft.WeaponRegistry;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.data.AircraftDataLoader;
import immersive_aircraft.entity.misc.SparseSimpleInventory;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.entity.weapons.Weapon;
import immersive_aircraft.item.WeaponItem;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.item.upgrade.AircraftUpgrade;
import immersive_aircraft.item.upgrade.AircraftUpgradeRegistry;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class InventoryVehicleEntity extends VehicleEntity implements ContainerListener, MenuProvider {
    private SparseSimpleInventory inventory;
    protected Map<Integer, List<Weapon>> weapons = new HashMap<>();

    public VehicleInventoryDescription getInventoryDescription() {
        return AircraftDataLoader.get(identifier).getInventoryDescription();
    }

    private static final List<WeaponMount> EMPTY_WEAPONS = List.of(WeaponMount.EMPTY);
    private static final Map<WeaponMount.Type, List<WeaponMount>> EMPTY_WEAPONS_MAP = Map.of();

    public List<WeaponMount> getWeaponMounts(int slot) {
        ItemStack stack = getSlot(slot).get();
        if (stack.getItem() instanceof WeaponItem weaponItem) {
            return AircraftDataLoader.get(identifier).getWeaponMounts().getOrDefault(slot, EMPTY_WEAPONS_MAP).getOrDefault(weaponItem.getMountType(), EMPTY_WEAPONS);
        }
        return EMPTY_WEAPONS;
    }

    public List<ItemStack> getSlots(VehicleInventoryDescription.SlotType slotType) {
        List<VehicleInventoryDescription.Slot> slots = getInventoryDescription().getSlots(slotType);
        List<ItemStack> list = new ArrayList<>(slots.size());
        for (VehicleInventoryDescription.Slot slot : slots) {
            list.add(getInventory().getItem(slot.index()));
        }
        return list;
    }

    //todo cache?
    public float getTotalUpgrade(AircraftStat stat) {
        float value = 1.0f;
        List<ItemStack> upgrades = getSlots(VehicleInventoryDescription.SlotType.UPGRADE);
        for (int step = 0; step < 2; step++) {
            for (ItemStack stack : upgrades) {
                AircraftUpgrade upgrade = AircraftUpgradeRegistry.INSTANCE.getUpgrade(stack.getItem());
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

    public InventoryVehicleEntity(EntityType<? extends AircraftEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);
        this.initInventory();
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
        //drop inventory
        if (getInventory() != null) {
            for (int i = 0; i < getInventory().getContainerSize(); ++i) {
                ItemStack itemStack = getInventory().getItem(i);
                if (itemStack.isEmpty() || EnchantmentHelper.hasVanishingCurse(itemStack)) continue;
                this.spawnAtLocation(itemStack);
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
        if (!player.level.isClientSide && player.isSecondaryUseActive()) {
            Entity primaryPassenger = getFirstPassenger();
            if (primaryPassenger != null) {
                // Kick out the first passenger
                primaryPassenger.stopRiding();
            } else {
                // Open inventory instead
                openInventory((ServerPlayer) player);
            }
            return InteractionResult.CONSUME;
        }
        return super.interact(player, hand);
    }


    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        ListTag nbtList = nbt.getList("Inventory", 10);
        getInventory().readNbt(nbtList);
    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag nbt) {
        super.saveWithoutId(nbt);

        nbt.put("Inventory", getInventory().writeNbt(new ListTag()));

        return nbt;
    }

    @Override
    public void boost() {
        super.boost();

        getSlots(VehicleInventoryDescription.SlotType.BOOSTER).forEach(s -> s.shrink(1));
    }

    @Override
    protected void applyBoost() {
        super.applyBoost();

        // boost
        Vec3 direction = getForwardDirection();
        float thrust = 0.05f * getBoost() / 100.0f;
        setDeltaMovement(getDeltaMovement().add(direction.scale(thrust)));

        // particles
        if (tickCount % 2 == 0) {
            Vec3 p = position();
            Vec3 velocity = getDeltaMovement().subtract(direction);
            level.addParticle(ParticleTypes.FIREWORK, p.x(), p.y(), p.z(), velocity.x, velocity.y, velocity.z);
        }
    }

    @Override
    public boolean canBoost() {
        return getSlots(VehicleInventoryDescription.SlotType.BOOSTER).stream().anyMatch(v -> !v.isEmpty()) && getBoost() <= 0;
    }

    @Override
    public void tick() {
        getInventory().tick(this);

        // Check and recreate weapon slots
        for (VehicleInventoryDescription.Slot slot : getInventoryDescription().getSlots(VehicleInventoryDescription.SlotType.WEAPON)) {
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

        // Update weapons
        for (List<Weapon> weapons : weapons.values()) {
            for (Weapon w : weapons) {
                w.tick();
            }
        }

        super.tick();
    }

    @Override
    public SlotAccess getSlot(int slot) {
        return SlotAccess.forContainer(getInventory(), slot);
    }

    public Map<Integer, List<Weapon>> getWeapons() {
        return weapons;
    }
}
