package immersive_aircraft.fabric.cobalt.registration;

import immersive_aircraft.cobalt.registration.CobaltFuelRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CobaltFuelRegistryImpl extends CobaltFuelRegistry {
    public CobaltFuelRegistryImpl() {
        INSTANCE = this;
    }

    @Override
    public int get(ItemStack stack) {
        // Hardcoded vanilla fuel values — avoids registry access issues on client
        if (stack.isEmpty()) return 0;
        var item = stack.getItem();
        if (item == Items.COAL) return 1600;
        if (item == Items.CHARCOAL) return 1600;
        if (item == Items.COAL_BLOCK) return 16000;
        if (item == Items.LAVA_BUCKET) return 20000;
        if (item == Items.BLAZE_ROD) return 2400;
        if (item == Items.DRIED_KELP_BLOCK) return 4001;
        if (item == Items.BAMBOO) return 50;
        if (item == Items.STICK) return 100;
        // Wood planks and logs
        if (stack.is(net.minecraft.tags.ItemTags.PLANKS)) return 300;
        if (stack.is(net.minecraft.tags.ItemTags.LOGS)) return 300;
        // Default: not fuel
        return 0;
    }
}
