package immersive_aircraft.fabric.cobalt.registration;

import dev.architectury.registry.fuel.FuelRegistry;
import immersive_aircraft.cobalt.registration.CobaltFuelRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.FuelValues;

public class CobaltFuelRegistryImpl extends CobaltFuelRegistry {
    public CobaltFuelRegistryImpl() {
        INSTANCE = this;
    }

    @Override
    public int get(ItemStack stack) {
        Integer time = FuelRegistry.get(stack.getItem(), RecipeType.SMELTING, FuelValues.);
        return time == null ? 0 : time;
    }
}
