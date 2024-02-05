package immersive_aircraft.cobalt.registration;

import net.minecraft.world.item.ItemStack;

public abstract class CobaltFuelRegistry {
    public static CobaltFuelRegistry INSTANCE = null;

    public abstract int get(ItemStack stack);
}
