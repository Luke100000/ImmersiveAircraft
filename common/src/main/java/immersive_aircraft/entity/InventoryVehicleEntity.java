package immersive_aircraft.entity;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.misc.AircraftBaseUpgradeRegistry;
import immersive_aircraft.entity.misc.SparseSimpleInventory;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.item.upgrade.AircraftUpgrade;
import immersive_aircraft.item.upgrade.AircraftUpgradeRegistry;
import immersive_aircraft.mixin.ServerPlayerEntityMixin;
import immersive_aircraft.network.s2c.OpenGuiRequest;
import immersive_aircraft.screen.VehicleScreenHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class InventoryVehicleEntity extends VehicleEntity implements ContainerListener, MenuProvider {
    protected SparseSimpleInventory inventory;
    private static final VehicleInventoryDescription inventoryDescription = new VehicleInventoryDescription()
            .addSlot(VehicleInventoryDescription.SlotType.BOILER, 8 + 9, 8 + 10)
            .build();

    public VehicleInventoryDescription getInventoryDescription() {
        return inventoryDescription;
    }

    public List<ItemStack> getSlots(VehicleInventoryDescription.SlotType slotType) {
        List<VehicleInventoryDescription.Slot> slots = getInventoryDescription().getSlots(slotType);
        List<ItemStack> list = new ArrayList<>(slots.size());
        for (VehicleInventoryDescription.Slot slot : slots) {
            list.add(getInventory().getItem(slot.index));
        }
        return list;
    }

    //todo cache?
    public float getTotalUpgrade(AircraftStat stat) {
        float value = 0.0f;
        List<ItemStack> upgrades = getSlots(VehicleInventoryDescription.SlotType.UPGRADE);
        for (int step = 0; step < 2; step++) {
            for (ItemStack stack : upgrades) {
                AircraftUpgrade upgrade = AircraftUpgradeRegistry.INSTANCE.getUpgrade(stack.getItem()); // Upgrades now pull from a very primitive registry rather than being saved on the item.
                if (upgrade != null) {
                    float u = upgrade.get(stat);

                    if (u > 0 && step == 1)
                        value += u;
                    else if (u < 0 && step == 0)
                        value *= (u + 1);
                }
            }
        }
        AircraftUpgrade baseUpgrade = AircraftBaseUpgradeRegistry.INSTANCE.getUpgradeModifier(this.getType());
        if (baseUpgrade != null)
            value += baseUpgrade.get(stat);

        return Math.max(0.0f, 1.0f + value);
    }

    public InventoryVehicleEntity(EntityType<? extends AircraftEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);
        this.initInventory();
    }

    protected void initInventory() {
        this.inventory = new SparseSimpleInventory(getInventoryDescription().getInventorySize());
        this.inventory.addListener(this);
    }

    @Override
    public void containerChanged(Container sender) {

    }

    @Override
    protected void drop() {
        super.drop();

        //drop inventory
        if (this.inventory != null) {
            for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
                ItemStack itemStack = this.inventory.getItem(i);
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

    protected int syncId;

    public void openInventory(ServerPlayer player) {
        syncId = (syncId + 1) % 100 + 100;
        AbstractContainerMenu screenHandler = createMenu(syncId, player.getInventory(), player);
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
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);

        ListTag nbtList = nbt.getList("Inventory", 10);
        this.inventory.readNbt(nbtList);
    }

    @Override
    public CompoundTag saveWithoutId(@NotNull CompoundTag nbt) {
        super.saveWithoutId(nbt);

        nbt.put("Inventory", this.inventory.writeNbt(new ListTag()));

        return nbt;
    }

    public SimpleContainer getInventory() {
        return inventory;
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
        Vector3f direction = getForwardDirection();
        float thrust = 0.05f * getBoost() / 100.0f;
        setDeltaMovement(getDeltaMovement().add(toVec3d(direction.mul(thrust))));

        // particles
        if (tickCount % 2 == 0) {
            Vec3 p = position();
            Vec3 velocity = getDeltaMovement().subtract(toVec3d(direction));
            level.addParticle(ParticleTypes.FIREWORK, p.x(), p.y(), p.z(), velocity.x, velocity.y, velocity.z);
        }
    }

    @Override
    public boolean canBoost() {
        return getSlots(VehicleInventoryDescription.SlotType.BOOSTER).stream().anyMatch(v -> !v.isEmpty()) && getBoost() <= 0;
    }

    @Override
    public void tick() {
        inventory.tick(this);

        super.tick();
    }

    @Override
    protected float getDurability() {
        return super.getDurability() * getTotalUpgrade(AircraftStat.DURABILITY);
    }
}
