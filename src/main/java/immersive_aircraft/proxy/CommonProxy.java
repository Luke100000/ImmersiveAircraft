package immersive_aircraft.proxy;

import immersive_aircraft.GuiHandler;
import immersive_aircraft.Main;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
    }

    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(Main.instance, new GuiHandler());
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    /**
     * Server side: no-op. Client side: runs the task on the client main thread.
     */
    public void addScheduledTaskClient(Runnable task) {
    }

    /**
     * Server side: null. Client side: the client player.
     */
    public EntityPlayer getClientPlayer() {
        return null;
    }
}
