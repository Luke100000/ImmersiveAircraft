package immersive_aircraft.screen.slot;

import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class UpgradeSlot extends TypedSlot {
    private final InventoryVehicleEntity vehicle;

    public UpgradeSlot(InventoryVehicleEntity vehicle, Class<? extends Item> clazz, int stackSize, Inventory inventory, int index, int x, int y) {
        super(clazz, stackSize, inventory, index, x, y);

        this.vehicle = vehicle;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return super.canInsert(stack) && vehicle.getSlots(VehicleInventoryDescription.SlotType.UPGRADE).stream().noneMatch(s -> s.getItem() == stack.getItem());
    }
}
