package immersive_aircraft.entity.inventory;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.network.c2s.InventoryRequest;
import immersive_aircraft.network.s2c.InventoryUpdateMessage;
import immersive_aircraft.screen.VehicleScreenHandler;
import net.minecraft.core.HolderLookup;
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

    @Override
    public void fromTag(ListTag tag, HolderLookup.Provider levelRegistry) {
        for (int i = 0; i < this.getContainerSize(); i++) {
            this.setItem(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < tag.size(); i++) {
            CompoundTag compoundTag = tag.getCompound(i);
            int j = compoundTag.getByte("Slot") & 255;
            if (j < this.getContainerSize()) {
                this.setItem(j, ItemStack.parse(levelRegistry, compoundTag).orElse(ItemStack.EMPTY));
            }
        }
    }

    @Override
    public ListTag createTag(HolderLookup.Provider levelRegistry) {
        ListTag listTag = new ListTag();
        for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack itemStack = this.getItem(i);
            if (!itemStack.isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putByte("Slot", (byte) i);
                listTag.add(itemStack.save(levelRegistry, compoundTag));
            }
        }
        return listTag;
    }

    public void tick(InventoryVehicleEntity entity) {
        if (entity.level().isClientSide) {
            // Sync initial inventory
            if (!inventoryRequested) {
                NetworkHandler.sendToServer(new InventoryRequest(entity.getId()));
                inventoryRequested = true;
            }
        } else {
            // Sync changed slots (excluding trailing inventory slots since they won't affect behavior)
            int lastSyncIndex = entity.getInventoryDescription().getLastSyncIndex();
            if (lastSyncIndex == 0) return;
            int index = entity.tickCount % lastSyncIndex;
            ItemStack stack = getItem(index);
            ItemStack trackedStack = tracked.get(index);
            if (!ItemStack.isSameItem(stack, trackedStack)) {
                tracked.set(index, stack);
                entity.level().players().forEach(p -> {
                    if (!(p.containerMenu instanceof VehicleScreenHandler vehicleScreenHandler && vehicleScreenHandler.getVehicle() == entity)) {
                        NetworkHandler.sendToPlayer(new InventoryUpdateMessage(entity, index, stack), (ServerPlayer) p);
                    }
                });
            }
        }
    }
}
