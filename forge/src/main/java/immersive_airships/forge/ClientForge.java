package immersive_airships.forge;

import immersive_airships.Entities;
import immersive_airships.Main;
import immersive_airships.client.render.entity.renderer.AirshipEntityRenderer;
import immersive_airships.forge.cobalt.registration.RegistrationImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
public final class ClientForge {
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        RegistrationImpl.bootstrap();
        EntityRenderers.register(Entities.AIRSHIP, AirshipEntityRenderer::new);
    }
}
