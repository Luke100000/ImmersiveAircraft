package immersive_aircraft.entity;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.misc.SparseSimpleInventory;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.item.UpgradeItem;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.mixin.ServerPlayerEntityMixin;
import immersive_aircraft.network.s2c.OpenGuiRequest;
import immersive_aircraft.screen.VehicleScreenHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class InventoryVehicleEntity extends VehicleEntity implements InventoryChangedListener, NamedScreenHandlerFactory {
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
            list.add(getInventory().getStack(slot.index));
        }
        return list;
    }

    //todo cache?
    public float getTotalUpgrade(AircraftStat stat) {
        float value = 1.0f;
        List<ItemStack> upgrades = getSlots(VehicleInventoryDescription.SlotType.UPGRADE);
        for (int step = 0; step < 2; step++) {
            for (ItemStack stack : upgrades) {
                if (stack.getItem() instanceof UpgradeItem upgrade) {
                    float u = upgrade.getUpgrade().get(stat);
                    if (u > 0 && step == 1) {
                        value += u;
                    } else if (u < 0 && step == 0) {
                        value *= (u + 1);
                    }
                }
            }
        }
        return Math.max(0.0f, value);
    }

    public InventoryVehicleEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
        this.initInventory();
    }

    protected void initInventory() {
        this.inventory = new SparseSimpleInventory(getInventoryDescription().getInventorySize());
        this.inventory.addListener(this);
    }

    @Override
    public void onInventoryChanged(Inventory sender) {

    }

    @Override
    protected void drop() {
        super.drop();

        //drop inventory
        if (this.inventory != null) {
            for (int i = 0; i < this.inventory.size(); ++i) {
                ItemStack itemStack = this.inventory.getStack(i);
                if (itemStack.isEmpty() || EnchantmentHelper.hasVanishingCurse(itemStack)) continue;
                this.dropStack(itemStack);
            }
        }
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new VehicleScreenHandler(i, playerInventory, this);
    }

    int syncId;

    public void openInventory(PlayerEntity player) {
        syncId = (syncId + 1) % 100 + 100;
        ScreenHandler screenHandler = createMenu(syncId, player.getInventory(), player);
        if (screenHandler != null) {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(this, screenHandler.syncId), (ServerPlayerEntity)player);
            player.currentScreenHandler = screenHandler;
            ServerPlayerEntityMixin playerAccessor = (ServerPlayerEntityMixin)player;
            screenHandler.updateSyncHandler(playerAccessor.getScreenHandlerSyncHandler());
        }
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!player.world.isClient && player.shouldCancelInteraction()) {
            openInventory(player);
            return ActionResult.CONSUME;
        }
        return super.interact(player, hand);
    }


    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        NbtList nbtList = nbt.getList("Inventory", 10);
        this.inventory.readNbt(nbtList);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.put("Inventory", this.inventory.writeNbt(new NbtList()));

        return nbt;
    }

    public SimpleInventory getInventory() {
        return inventory;
    }
}
