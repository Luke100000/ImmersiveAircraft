package immersive_aircraft.neoforge;

import immersive_aircraft.Main;
import immersive_aircraft.client.OverlayRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT)
public class NeoForgeOverlayRenderer {
    private static final ResourceLocation NamedGuiIdentifier = ResourceLocation.withDefaultNamespace("hotbar");

    @SubscribeEvent()
    public static void renderOverlay(RenderGuiLayerEvent.Post event) {
        if (event.getName().equals(NamedGuiIdentifier)) {
            OverlayRenderer.renderOverlay(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaTicks());
        }
    }
}
