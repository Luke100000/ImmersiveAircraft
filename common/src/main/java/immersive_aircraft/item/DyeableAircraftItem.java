package immersive_aircraft.item;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.DyeableLeatherItem;

public class DyeableAircraftItem extends AircraftItem implements DyeableLeatherItem {
    public DyeableAircraftItem(Properties settings, AircraftConstructor constructor) {
        super(settings, constructor);

        CauldronInteraction.WATER.put(this, CauldronInteraction.DYED_ITEM);
    }
}
