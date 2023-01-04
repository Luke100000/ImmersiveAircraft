package immersive_aircraft.fabric;

import immersive_aircraft.ClientMain;
import immersive_aircraft.Renderer;
import immersive_aircraft.client.KeyBindings;
import immersive_aircraft.fabric.resources.FabricObjectLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public final class ClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register((event) -> ClientMain.postLoad());

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new FabricObjectLoader());

        Renderer.bootstrap();

        KeyBindings.list.forEach(KeyBindingHelper::registerKeyBinding);
    }
}
