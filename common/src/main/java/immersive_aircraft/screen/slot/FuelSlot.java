package immersive_aircraft.screen.slot;

import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.util.Utils;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;

public class FuelSlot extends Slot {
    private final FuelValues fuelValues;

    public FuelSlot(InventoryVehicleEntity entity, Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.fuelValues = entity.level().fuelValues();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return Utils.getFuelTime(stack, fuelValues) > 0;
    }
}

