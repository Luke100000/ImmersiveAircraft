package immersive_aircraft.item;

import net.minecraft.core.cauldron.CauldronInteraction;

public class DyeableAircraftItem extends AircraftItem {
    public DyeableAircraftItem(Properties settings, AircraftConstructor constructor) {
        super(settings, constructor);

        CauldronInteraction.WATER.map().put(this, CauldronInteraction.DYED_ITEM);
    }
}
