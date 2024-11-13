package immersive_aircraft.entity.inventory;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.network.c2s.RequestInventory;
import immersive_aircraft.network.s2c.InventoryUpdateMessage;
import immersive_aircraft.screen.VehicleScreenHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class SparseSimpleInventory extends SimpleContainer {
    private final NonNullList<ItemStack> tracked;
    private boolean inventoryRequested = false;

    public SparseSimpleInventory(int size) {
        super(size);

        tracked = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public ListTag writeNbt(ListTag nbtList) {
        for (int i = 0; i < this.getContainerSize(); ++i) {
            if (this.getItem(i).isEmpty()) continue;
            CompoundTag nbtCompound = new CompoundTag();
            nbtCompound.putByte("Slot", (byte) i);
            this.getItem(i).save(nbtCompound);
            nbtList.add(nbtCompound);
        }
        return nbtList;
    }

    public void readNbt(ListTag nbtList) {
        this.clearContent();
        for (int i = 0; i < nbtList.size(); ++i) {
            CompoundTag nbtCompound = nbtList.getCompound(i);
            int slot = nbtCompound.getByte("Slot") & 0xFF;
            ItemStack itemStack = ItemStack.of(nbtCompound);
            if (itemStack.isEmpty()) continue;
            this.setItem(slot, itemStack);
        }
    }

    public void tick(InventoryVehicleEntity entity) {
        if (entity.level().isClientSide) {
            // Sync initial inventory
            if (!inventoryRequested) {
                NetworkHandler.sendToServer(new RequestInventory(entity.getId()));
                inventoryRequested = true;
            }
        } else {
            // Sync changed slots
            int lastSyncIndex = entity.getInventoryDescription().getLastSyncIndex();
            if (lastSyncIndex == 0) return;
            int index = entity.tickCount % lastSyncIndex;
            ItemStack stack = getItem(index);
            ItemStack trackedStack = tracked.get(index);
            if (!ItemStack.isSameItem(stack, trackedStack)) {
                tracked.set(index, stack.copy());
                entity.level().players().forEach(p -> {
                    if (!(p.containerMenu instanceof VehicleScreenHandler vehicleScreenHandler && vehicleScreenHandler.getVehicle() == entity)) {
                        NetworkHandler.sendToPlayer(new InventoryUpdateMessage(entity.getId(), index, stack), (ServerPlayer) p);
                    }
                });
            }
        }
    }
}
