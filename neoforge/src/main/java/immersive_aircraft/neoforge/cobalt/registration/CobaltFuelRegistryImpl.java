package immersive_aircraft.neoforge.cobalt.registration;

import immersive_aircraft.cobalt.registration.CobaltFuelRegistry;
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
        return values == null ? 0 : stack.getBurnTime(null, values);
    }
}
