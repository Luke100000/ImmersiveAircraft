package immersive_aircraft.screen.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TypedSlot extends Slot {
    private final Class<? extends Item> clazz;
    private final int stackSize;

    public TypedSlot(Class<? extends Item> clazz, int stackSize, Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);

        this.clazz = clazz;
        this.stackSize = stackSize;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return clazz.isAssignableFrom(stack.getItem().getClass());
    }

    @Override
    public int getMaxStackSize() {
        return stackSize;
    }
}

