package immersive_aircraft.neoforge;

import net.neoforged.fml.ModList;

public class CompatUtilImpl {
    public static boolean isModLoaded(String modid) {
        return ModList.get().isLoaded(modid);
    }
}
