package immersive_aircraft.entity.misc;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.network.c2s.RequestInventory;
import immersive_aircraft.network.s2c.InventoryUpdateMessage;
import immersive_aircraft.screen.VehicleScreenHandler;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

public class SparseSimpleInventory extends SimpleInventory {
    private final DefaultedList<ItemStack> tracked;
    private boolean inventoryRequested = false;

    public SparseSimpleInventory(int size) {
        super(size);

        tracked = DefaultedList.ofSize(size, ItemStack.EMPTY);
    }

    public NbtList writeNbt(NbtList nbtList) {
        for (int i = 0; i < this.size(); ++i) {
            if (this.getStack(i).isEmpty()) continue;
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putByte("Slot", (byte)i);
            this.getStack(i).writeNbt(nbtCompound);
            nbtList.add(nbtCompound);
        }
        return nbtList;
    }

    public void readNbt(NbtList nbtList) {
        this.clear();
        for (int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            int slot = nbtCompound.getByte("Slot") & 0xFF;
            ItemStack itemStack = ItemStack.fromNbt(nbtCompound);
            if (itemStack.isEmpty()) continue;
            this.setStack(slot, itemStack);
        }
    }


    public void tick(InventoryVehicleEntity entity) {
        if (entity.world.isClient) {
            // Sync initial inventory
            if (!inventoryRequested) {
                NetworkHandler.sendToServer(new RequestInventory(entity.getEntityId()));
                inventoryRequested = true;
            }
        } else {
            // Sync changed slots
            int index = entity.age % entity.getInventoryDescription().getLastSyncIndex();
            ItemStack stack = getStack(index);
            ItemStack trackedStack = tracked.get(index);
            if (!stack.equals(trackedStack)) {
                tracked.set(index, stack);
                entity.world.getPlayers().forEach(p -> {
                    if (!(p.currentScreenHandler instanceof VehicleScreenHandler && ((VehicleScreenHandler)p.currentScreenHandler).getVehicle() == entity)) {
                        NetworkHandler.sendToPlayer(new InventoryUpdateMessage(entity.getEntityId(), index, stack), (ServerPlayerEntity)p);
                    }
                });
            }
        }
    }
}
