package immersive_aircraft.fabric;

import immersive_aircraft.*;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.fabric.cobalt.network.NetworkHandlerImpl;
import immersive_aircraft.fabric.cobalt.registration.CobaltFuelRegistryImpl;
import immersive_aircraft.fabric.cobalt.registration.RegistrationImpl;
import immersive_aircraft.network.s2c.AircraftDataMessage;
import immersive_aircraft.network.s2c.VehicleUpgradesMessage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;

public final class CommonFabric implements ModInitializer {
    static {
        Main.MOD_LOADER = "fabric";

        new RegistrationImpl();
        new NetworkHandlerImpl();
        new CobaltFuelRegistryImpl();
    }

    @Override
    public void onInitialize() {
        AircraftStats.bootstrap();
        Items.bootstrap();
        Sounds.bootstrap();
        Entities.bootstrap();
        WeaponRegistry.bootstrap();
        DataLoaders.bootstrap();

        Messages.loadMessages();

        CreativeModeTab group = FabricCreativeModeTab.builder()
                .title(ItemGroups.getDisplayName())
                .icon(ItemGroups::getIcon)
                .displayItems((enabledFeatures, entries) -> entries.acceptAll(Items.getSortedItems()))
                .build();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Main.locate("group"), group);

        // Register event for syncing aircraft upgrades.
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(this::onSyncDatapack);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            net.minecraft.server.level.ServerLevel level = server.overworld();
            CobaltFuelRegistryImpl.setFuelValues(level.fuelValues());
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> CobaltFuelRegistryImpl.setFuelValues(null));
    }

    /**
     * Send sync packets for upgrades when datapack is reloaded.
     */
    private void onSyncDatapack(ServerPlayer player, boolean joined) {
        NetworkHandler.sendToPlayer(new VehicleUpgradesMessage(), player);
        NetworkHandler.sendToPlayer(new AircraftDataMessage(), player);
    }
}

