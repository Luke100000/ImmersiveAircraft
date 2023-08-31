package immersive_aircraft.screen.slot;

import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.entity.misc.VehicleInventoryDescription.SlotType;
import immersive_aircraft.item.upgrade.AircraftUpgradeRegistry;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class UpgradeSlot extends Slot {

    private final InventoryVehicleEntity vehicle;
    private final int stackSize;

    public UpgradeSlot(InventoryVehicleEntity vehicle, int stackSize, Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);

        this.vehicle = vehicle;
        this.stackSize = stackSize;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return AircraftUpgradeRegistry.INSTANCE.hasUpgrade(stack.getItem()) && vehicle.getSlots(SlotType.UPGRADE).stream().noneMatch(s -> s.getItem() == stack.getItem());
    }

    @Override
    public int getMaxItemCount() {
        return stackSize;
    }
}
