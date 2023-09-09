package immersive_aircraft.screen.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static immersive_aircraft.entity.EngineAircraft.getFuelTime;

public class FuelSlot extends Slot {
    public FuelSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return getFuelTime(stack) > 0;
    }
}

