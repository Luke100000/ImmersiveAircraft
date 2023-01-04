package immersive_aircraft.fabric;

import immersive_aircraft.*;
import immersive_aircraft.fabric.cobalt.network.NetworkHandlerImpl;
import immersive_aircraft.fabric.cobalt.registration.RegistrationImpl;
import immersive_aircraft.server.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

public final class CommonFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        new RegistrationImpl();
        new NetworkHandlerImpl();

        Items.bootstrap();
        Sounds.bootstrap();
        Entities.bootstrap();
        Messages.bootstrap();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Command.register(dispatcher));

        ItemGroups.GROUP = FabricItemGroup.builder(ItemGroups.getIdentifier())
                .displayName(ItemGroups.getDisplayName())
                .icon(ItemGroups::getIcon)
                .entries((enabledFeatures, entries, operatorEnabled) -> entries.addAll(Items.getSortedItems()))
                .build();
    }
}

