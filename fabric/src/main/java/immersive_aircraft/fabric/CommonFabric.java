package immersive_aircraft.fabric;

import immersive_aircraft.Entities;
import immersive_aircraft.Items;
import immersive_aircraft.Messages;
import immersive_aircraft.fabric.cobalt.network.NetworkHandlerImpl;
import immersive_aircraft.fabric.cobalt.registration.RegistrationImpl;
import immersive_aircraft.server.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public final class CommonFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        new RegistrationImpl();
        new NetworkHandlerImpl();

        Items.bootstrap();
        Entities.bootstrap();
        Messages.bootstrap();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> Command.register(dispatcher));
    }
}

