package immersive_aircraft;

import immersive_aircraft.network.NetworkManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Main {
    public static final String SHORT_MOD_ID = "ic_air";
    public static final String MOD_ID = "immersive_aircraft";
    public static final Logger LOGGER = LogManager.getLogger();
    public static NetworkManager networkManager;

    public static ResourceLocation locate(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
