package immersive_aircraft.neoforge;

import immersive_aircraft.ItemColors;
import immersive_aircraft.Main;
import immersive_aircraft.Renderer;
import immersive_aircraft.WeaponRendererRegistry;
import immersive_aircraft.client.KeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@SuppressWarnings("unused")
@Mod(value = Main.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = Main.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ClientNeoForge {
    @SubscribeEvent
    public static void data(FMLConstructModEvent event) {
        ReloadableResourceManager resourceManager = (ReloadableResourceManager) Minecraft.getInstance().getResourceManager();
        NeoForgeBusEvents.RESOURCE_REGISTRY.getLoaders().forEach(resourceManager::registerReloadListener);
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        Renderer.bootstrap();
        WeaponRendererRegistry.bootstrap();
    }

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        KeyBindings.list.forEach(event::register);
    }

    @SubscribeEvent
    public static void initItemColors(RegisterColorHandlersEvent.Item event) {
        ItemColors.ITEM_COLORS.forEach((item, itemColor) -> event.register(itemColor, item));
    }
}
