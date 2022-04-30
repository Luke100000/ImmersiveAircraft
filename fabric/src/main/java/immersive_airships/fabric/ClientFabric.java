package immersive_airships.fabric;

import immersive_airships.ClientMain;
import immersive_airships.Entities;
import immersive_airships.client.render.entity.renderer.AirshipEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class ClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register((event) -> ClientMain.postLoad());
        EntityRendererRegistry.register(Entities.AIRSHIP, AirshipEntityRenderer::new);
    }
}
