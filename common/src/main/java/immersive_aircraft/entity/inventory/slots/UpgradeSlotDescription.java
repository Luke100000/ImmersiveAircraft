package immersive_aircraft.entity.inventory.slots;

import com.google.gson.JsonObject;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.screen.slot.UpgradeSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class UpgradeSlotDescription extends TooltippedSlotDescription {
    public UpgradeSlotDescription(String type, int index, int x, int y, JsonObject json) {
        super(type, index, x, y, json);
    }

    public UpgradeSlotDescription(String type, FriendlyByteBuf buffer) {
        super(type, buffer);
    }

    public Slot getSlot(InventoryVehicleEntity vehicle, Container inventory) {
        return new UpgradeSlot(vehicle, 1, inventory, index, x, y);
    }
}
