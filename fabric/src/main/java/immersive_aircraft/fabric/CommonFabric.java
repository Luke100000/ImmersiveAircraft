package immersive_aircraft.fabric;

import immersive_aircraft.Entities;
import immersive_aircraft.Items;
import immersive_aircraft.Messages;
import immersive_aircraft.Sounds;
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
        Messages.loadMessages();
    }
}

