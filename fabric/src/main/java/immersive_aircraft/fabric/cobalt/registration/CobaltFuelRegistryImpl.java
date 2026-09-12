package immersive_aircraft.fabric.cobalt.registration;

import immersive_aircraft.cobalt.registration.CobaltFuelRegistry;
import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;

public class CobaltFuelRegistryImpl extends CobaltFuelRegistry {
    private static volatile FuelValues fuelValues;

    public CobaltFuelRegistryImpl() {
        INSTANCE = this;
    }

    public static void setFuelValues(FuelValues values) {
        fuelValues = values;
    }

    @Override
    public int get(ItemStack stack) {
        FuelValues values = fuelValues;
        if (values == null) {
            return 0;
        }
        return values.burnDuration(stack);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return ((FabricItemStack) (Object) stack).getRecipeRemainder();
    }
}
