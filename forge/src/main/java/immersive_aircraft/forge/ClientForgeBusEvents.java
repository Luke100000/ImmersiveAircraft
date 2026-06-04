package immersive_aircraft.forge;

import immersive_aircraft.ClientMain;
import immersive_aircraft.Main;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT)
public final class ClientForgeBusEvents {
    private static boolean firstLoad = true;

    private ClientForgeBusEvents() {
    }

    @SubscribeEvent
    public static void onClientStart(TickEvent.ClientTickEvent.Post event) {
        // Forge does not reliably trigger the client load point this mod needs.
        if (firstLoad) {
            ClientMain.postLoad();
            firstLoad = false;
        }

        ClientMain.tick();
    }
}
