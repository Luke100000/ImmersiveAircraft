package immersive_aircraft.config;

import immersive_aircraft.Main;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Re-syncs the runtime mirror whenever the config changes (file edit or GUI).
 */
@Mod.EventBusSubscriber(modid = Main.MOD_ID)
public class ConfigEventHandler {
    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Main.MOD_ID)) {
            ConfigManager.sync(Main.MOD_ID, net.minecraftforge.common.config.Config.Type.INSTANCE);
            Config.sync();
        }
    }
}
