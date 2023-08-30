package immersive_aircraft.fabric;

import immersive_aircraft.Entities;
import immersive_aircraft.Items;
import immersive_aircraft.Messages;
import immersive_aircraft.Sounds;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.fabric.cobalt.network.NetworkHandlerImpl;
import immersive_aircraft.fabric.cobalt.registration.RegistrationImpl;
import immersive_aircraft.network.s2c.AircraftUpgradeMessage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class CommonFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        new RegistrationImpl();
        new NetworkHandlerImpl();

        Items.bootstrap();
        Sounds.bootstrap();
        Entities.bootstrap();
        Messages.loadMessages();

        // Register event for syncing aircraft upgrades.
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> NetworkHandler.sendToPlayer(new AircraftUpgradeMessage(), player));
    }
}

