package immersive_aircraft.screen.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class IngredientSlot extends Slot {
    private final Ingredient ingredient;
    private final int stackSize;

    public IngredientSlot(Ingredient ingredient, int stackSize, Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);

        this.ingredient = ingredient;
        this.stackSize = stackSize;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return ingredient.test(stack);
    }

    @Override
    public int getMaxStackSize() {
        return stackSize;
    }
}
