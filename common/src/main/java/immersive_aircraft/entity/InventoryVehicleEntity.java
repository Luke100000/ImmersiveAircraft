package immersive_aircraft.entity;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.misc.SparseSimpleInventory;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
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

public abstract class InventoryVehicleEntity extends VehicleEntity implements InventoryChangedListener, NamedScreenHandlerFactory {
    protected SparseSimpleInventory inventory;
    private static final VehicleInventoryDescription inventoryDescription = new VehicleInventoryDescription()
            .addSlot(VehicleInventoryDescription.SlotType.BOILER, 8 + 9, 8 + 26)
            .addSlot(VehicleInventoryDescription.SlotType.WEAPON, 8 + 18 * 2 + 6, 8 + 6)
            .addSlot(VehicleInventoryDescription.SlotType.BANNER, 8 + 18 * 2 + 6, 8 + 6 + 22)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 8 + 18 * 2 + 28, 8 + 6)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 8 + 18 * 2 + 28, 8 + 6 + 22)
            .addSlots(VehicleInventoryDescription.SlotType.INVENTORY, 8 + 18 * 5, 8, 4, 3)
            .build();

    public InventoryVehicleEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
        this.initInventory();
    }

    protected void initInventory() {
        this.inventory = new SparseSimpleInventory(inventoryDescription.getInventorySize());
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

    public VehicleInventoryDescription getInventoryDescription() {
        return inventoryDescription;
    }
}
