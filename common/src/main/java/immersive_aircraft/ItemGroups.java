package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

public class ItemGroups {
    public static final ItemGroup GROUP = Registration.ObjectBuilders.ItemGroups.create(
            new Identifier(Main.MOD_ID, Main.MOD_ID + "_tab"),
            () -> Items.BIPLANE.get().getDefaultStack()
    );
}
