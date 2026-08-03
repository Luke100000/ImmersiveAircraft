package immersive_aircraft;

import immersive_aircraft.network.NetworkManager;
import immersive_aircraft.proxy.CommonProxy;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Main.MOD_ID, name = Main.NAME, version = Main.VERSION, guiFactory = "immersive_aircraft.config.ConfigGuiFactory")
public class Main {
    public static final String SHORT_MOD_ID = "ic_air";
    public static final String MOD_ID = "immersive_aircraft";
    public static final String NAME = "Immersive Aircraft";
    public static final String VERSION = "1.12.2-0.1.0";
    public static final Logger LOGGER = LogManager.getLogger();
    public static NetworkManager networkManager;

    public static ResourceLocation locate(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    @Mod.Instance(MOD_ID)
    public static Main instance;

    @SidedProxy(clientSide = "immersive_aircraft.proxy.ClientProxy", serverSide = "immersive_aircraft.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        immersive_aircraft.config.Config.sync();
        Messages.loadMessages();
        Entities.bootstrap();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
