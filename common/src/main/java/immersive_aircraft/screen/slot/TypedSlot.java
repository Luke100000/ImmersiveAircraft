package immersive_aircraft.screen.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class TypedSlot extends Slot {
    private final Class<? extends Item> clazz;
    private final int stackSize;

    public TypedSlot(Class<? extends Item> clazz, int stackSize, Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);

        this.clazz = clazz;
        this.stackSize = stackSize;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return clazz.isAssignableFrom(stack.getItem().getClass());
    }

    @Override
    public int getMaxItemCount() {
        return stackSize;
    }
}

