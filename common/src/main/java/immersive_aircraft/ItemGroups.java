package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public class ItemGroups {
    public static final CreativeModeTab GROUP = Registration.ObjectBuilders.ItemGroups.create(
            new ResourceLocation(Main.MOD_ID, Main.MOD_ID + "_tab"),
            () -> Items.BIPLANE.get().getDefaultInstance()
    );
}
