package immersive_aircraft.entity.inventory;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.network.c2s.InventoryRequest;
import immersive_aircraft.network.s2c.InventoryUpdateMessage;
import immersive_aircraft.screen.VehicleScreenHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SparseSimpleInventory extends SimpleContainer {
    private final NonNullList<ItemStack> tracked;
    private boolean inventoryRequested = false;

    public SparseSimpleInventory(int size) {
        super(size);

        tracked = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public void loadFromInventory(ValueInput tag, String key) {
        for (int i = 0; i < this.getContainerSize(); i++) {
            this.setItem(i, ItemStack.EMPTY);
        }
        tag.childrenList(key).ifPresent(list -> {
            for (ValueInput entry : list) {
                int slot = entry.getByteOr("Slot", (byte) 0) & 255;
                if (slot < this.getContainerSize()) {
                    entry.read(ItemStack.MAP_CODEC).ifPresent(stack -> this.setItem(slot, stack));
                }
            }
        });
    }

    public void storeAsInventory(ValueOutput tag, String key) {
        ValueOutput.ValueOutputList list = tag.childrenList(key);
        for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack itemStack = this.getItem(i);
            if (!itemStack.isEmpty()) {
                ValueOutput entry = list.addChild();
                entry.putByte("Slot", (byte) i);
                entry.store(ItemStack.MAP_CODEC, itemStack);
            }
        }
    }

    public void tick(InventoryVehicleEntity entity) {
        if (entity.level().isClientSide()) {
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
                tracked.set(index, stack.copy());
                entity.level().players().forEach(p -> {
                    if (!(p.containerMenu instanceof VehicleScreenHandler vehicleScreenHandler && vehicleScreenHandler.getVehicle() == entity)) {
                        NetworkHandler.sendToPlayer(new InventoryUpdateMessage(entity, index, stack), (ServerPlayer) p);
                    }
                });
            }
        }
    }
}
