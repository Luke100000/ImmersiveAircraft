package immersive_aircraft.entity.inventory.slots;

import com.google.gson.JsonObject;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.screen.slot.TypedSlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;

public class TypedSlotDescription extends TooltippedSlotDescription {
    final Class<? extends Item> clazz;
    final int maxStackSize;

    public TypedSlotDescription(String type, int index, int x, int y, JsonObject json, Class<? extends Item> clazz, int maxStackSize) {
        super(type, index, x, y, json);
        this.clazz = clazz;
        this.maxStackSize = maxStackSize;
    }

    public TypedSlotDescription(String type, RegistryFriendlyByteBuf buffer, Class<? extends Item> clazz) {
        super(type, buffer);

        this.clazz = clazz;
        this.maxStackSize = buffer.readInt();
    }

    public Slot getSlot(InventoryVehicleEntity vehicle, Container inventory) {
        return new TypedSlot(clazz, maxStackSize, inventory, index, x, y);
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer) {
        super.encode(buffer);

        buffer.writeInt(maxStackSize);
    }
}