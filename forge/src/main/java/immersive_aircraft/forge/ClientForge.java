package immersive_aircraft.forge;

import immersive_aircraft.ItemColors;
import immersive_aircraft.Main;
import immersive_aircraft.Renderer;
import immersive_aircraft.WeaponRendererRegistry;
import immersive_aircraft.client.KeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
public final class ClientForge {
    @SubscribeEvent
    public static void data(FMLConstructModEvent event) {
        ReloadableResourceManager resourceManager = (ReloadableResourceManager) Minecraft.getInstance().getResourceManager();
        ForgeBusEvents.RESOURCE_REGISTRY.getLoaders().forEach(resourceManager::registerReloadListener);
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
        ItemColors.ITEM_COLOR_PROVIDERS.forEach((item, itemColor) -> event.register(itemColor, item.get()));
    }
}
