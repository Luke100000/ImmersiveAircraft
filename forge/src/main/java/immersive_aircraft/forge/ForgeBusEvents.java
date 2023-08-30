package immersive_aircraft.forge;

import immersive_aircraft.ClientMain;
import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.item.UpgradeItem;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.network.s2c.AircraftUpgradeMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;


@Mod.EventBusSubscriber(modid = Main.MOD_ID)
public class ForgeBusEvents {
    public static boolean firstLoad = true;

    @SubscribeEvent
    public static void onClientStart(TickEvent.ClientTickEvent event) {
        //forge decided to be funny and won't trigger the client load event
        if (firstLoad) {
            ClientMain.postLoad();
            firstLoad = false;
        }

        ClientMain.tick();
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if(event.getPlayer() != null) // Syncing aircraft upgrades to players.
            NetworkHandler.sendToPlayer(new AircraftUpgradeMessage(), event.getPlayer());
        else {
            for(ServerPlayerEntity player : event.getPlayerList().getPlayerList())
                NetworkHandler.sendToPlayer(new AircraftUpgradeMessage(), player);
        }
    }

}
