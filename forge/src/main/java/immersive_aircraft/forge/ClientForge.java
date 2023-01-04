package immersive_aircraft.forge;

import immersive_aircraft.Main;
import immersive_aircraft.Renderer;
import immersive_aircraft.client.KeyBindings;
import immersive_aircraft.forge.cobalt.registration.RegistrationImpl;
import immersive_aircraft.resources.ObjectLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
public final class ClientForge {
    @SubscribeEvent
    public static void data(FMLConstructModEvent event) {
        ((ReloadableResourceManagerImpl)MinecraftClient.getInstance().getResourceManager()).registerReloader(new ObjectLoader());
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        RegistrationImpl.bootstrap();

        Renderer.bootstrap();

        KeyBindings.list.forEach(ClientRegistry::registerKeyBinding);
    }
}
