package immersive_aircraft.screen.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import static immersive_aircraft.entity.EngineAircraft.getFuelTime;

public class FuelSlot extends Slot {
    public FuelSlot(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return getFuelTime(stack) > 0;
    }
}
