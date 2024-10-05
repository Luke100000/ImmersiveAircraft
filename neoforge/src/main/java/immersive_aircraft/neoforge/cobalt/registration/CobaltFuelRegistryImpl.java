package immersive_aircraft.neoforge.cobalt.registration;

import immersive_aircraft.cobalt.registration.CobaltFuelRegistry;
import net.minecraft.world.item.ItemStack;

public class CobaltFuelRegistryImpl extends CobaltFuelRegistry {
    public CobaltFuelRegistryImpl() {
        INSTANCE = this;
    }

    @Override
    public int get(ItemStack stack) {
        return stack.getBurnTime(null);
    }
}
