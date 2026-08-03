package immersive_aircraft.screen.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TypedSlot extends Slot {
    private final Class<? extends Item> clazz;
    private final int stackSize;

    public TypedSlot(Class<? extends Item> clazz, int stackSize, IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);

        this.clazz = clazz;
        this.stackSize = stackSize;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return clazz.isAssignableFrom(stack.getItem().getClass());
    }

    @Override
    public int getSlotStackLimit() {
        return stackSize;
    }
}
