package immersive_aircraft;

import net.fabricmc.loader.api.FabricLoader;

public class CompatUtil {
    public static boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }
}
