package immersive_aircraft.forge;

import net.minecraftforge.fml.ModList;

public class CompatUtilImpl {
    public static boolean isModLoaded(String modid) {
        return ModList.get().isLoaded(modid);
    }
}
