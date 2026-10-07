package immersive_aircraft.cobalt.registration;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public abstract class CobaltFuelRegistry {
    public static CobaltFuelRegistry INSTANCE = null;

    public abstract int get(ItemStack stack);

    public abstract ItemStack getCraftingRemainingItem(ItemStack stack);

    public abstract int getFluidFuelTime(ItemStack stack);

    // Drain exactly one bucket, retaining the updated container in its inventory slot.
    public abstract int refuelFluid(Container inventory, int slot);
}
