package immersive_aircraft.fabric;

import immersive_aircraft.*;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.fabric.cobalt.network.NetworkHandlerImpl;
import immersive_aircraft.fabric.cobalt.registration.CobaltFuelRegistryImpl;
import immersive_aircraft.fabric.cobalt.registration.RegistrationImpl;
import immersive_aircraft.network.s2c.AircraftBaseUpgradesMessage;
import immersive_aircraft.network.s2c.AircraftDataMessage;
import immersive_aircraft.network.s2c.AircraftUpgradesMessage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.ServerPlayer;

public final class CommonFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        new RegistrationImpl();
        new NetworkHandlerImpl();
        new CobaltFuelRegistryImpl();

        Items.bootstrap();
        Sounds.bootstrap();
        Entities.bootstrap();
        WeaponRegistry.bootstrap();
        DataLoaders.bootstrap();

        Messages.loadMessages();

        // Register event for syncing aircraft upgrades.
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(this::onSyncDatapack);
    }
    /**
     * Send sync packets for upgrades when datapack is reloaded.
     */
    private void onSyncDatapack(ServerPlayer player, boolean joined) {
        NetworkHandler.sendToPlayer(new AircraftUpgradesMessage(), player);
        NetworkHandler.sendToPlayer(new AircraftBaseUpgradesMessage(), player);
        NetworkHandler.sendToPlayer(new AircraftDataMessage(), player);
    }
}

