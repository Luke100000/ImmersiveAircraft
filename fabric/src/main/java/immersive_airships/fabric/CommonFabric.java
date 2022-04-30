package immersive_airships.fabric;

import immersive_airships.Entities;
import immersive_airships.Items;
import immersive_airships.fabric.cobalt.network.NetworkHandlerImpl;
import immersive_airships.fabric.cobalt.registration.RegistrationImpl;
import immersive_airships.server.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public final class CommonFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        new RegistrationImpl();
        new NetworkHandlerImpl();

        Items.bootstrap();
        Entities.bootstrap();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> Command.register(dispatcher));
    }
}

