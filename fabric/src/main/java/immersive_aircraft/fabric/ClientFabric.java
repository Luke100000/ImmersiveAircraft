package immersive_aircraft.fabric;

import immersive_aircraft.ClientMain;
import immersive_aircraft.Renderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public final class ClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register((event) -> ClientMain.postLoad());

        Renderer.bootstrap();
    }
}
