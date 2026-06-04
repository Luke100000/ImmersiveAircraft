package immersive_aircraft.neoforge;

import immersive_aircraft.ClientMain;
import immersive_aircraft.Main;
import immersive_aircraft.Renderer;
import immersive_aircraft.WeaponRendererRegistry;
import immersive_aircraft.client.KeyBindings;
import immersive_aircraft.neoforge.cobalt.network.NetworkHandlerImpl;
import immersive_aircraft.neoforge.cobalt.registration.CobaltFuelRegistryImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class ClientNeoForge {
    private static boolean firstLoad = true;

    public static void register(IEventBus modEventBus, NetworkHandlerImpl networkHandler) {
        modEventBus.addListener(ClientNeoForge::data);
        modEventBus.addListener(ClientNeoForge::setup);
        modEventBus.addListener(ClientNeoForge::onKeyRegister);
        modEventBus.addListener(NeoForgeOverlayRenderer::registerGuiOverlays);
        networkHandler.registerClientPayloads(modEventBus);
        NeoForge.EVENT_BUS.addListener(ClientNeoForge::onClientStart);
        NeoForge.EVENT_BUS.addListener(ClientNeoForge::onClientLogin);
        NeoForge.EVENT_BUS.addListener(ClientNeoForge::onClientLogout);
    }

    @SubscribeEvent
    public static void data(FMLConstructModEvent event) {
        ReloadableResourceManager resourceManager = (ReloadableResourceManager) Minecraft.getInstance().getResourceManager();
        NeoForgeBusEvents.RESOURCE_REGISTRY.getLoaders().forEach(resourceManager::registerReloadListener);
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Renderer.bootstrap();
            WeaponRendererRegistry.bootstrap();
        });
    }

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        KeyBindings.list.forEach(event::register);
    }

    @SubscribeEvent
    public static void onClientStart(ClientTickEvent.Post event) {
        if (firstLoad) {
            ClientMain.postLoad();
            firstLoad = false;
        }

        ClientMain.tick();
    }

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        CobaltFuelRegistryImpl.setFuelValues(event.getPlayer().level().fuelValues());
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CobaltFuelRegistryImpl.setFuelValues(null);
    }
}
