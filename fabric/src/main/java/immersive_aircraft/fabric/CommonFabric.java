package immersive_aircraft.fabric;

import immersive_aircraft.*;
import immersive_aircraft.fabric.cobalt.network.NetworkHandlerImpl;
import immersive_aircraft.fabric.cobalt.registration.RegistrationImpl;
import net.fabricmc.api.ModInitializer;

public final class CommonFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        new RegistrationImpl();
        new NetworkHandlerImpl();

        Items.bootstrap();
        Sounds.bootstrap();
        Entities.bootstrap();
        Messages.bootstrap();

        ItemGroups.GROUP = FabricItemGroup.builder(ItemGroups.getIdentifier())
                .displayName(ItemGroups.getDisplayName())
                .icon(ItemGroups::getIcon)
                .entries((enabledFeatures, entries, operatorEnabled) -> entries.addAll(Items.getSortedItems()))
                .build();
    }
}

