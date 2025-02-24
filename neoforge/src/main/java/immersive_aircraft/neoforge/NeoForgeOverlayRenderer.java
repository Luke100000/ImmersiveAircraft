package immersive_aircraft.neoforge;

import immersive_aircraft.Main;
import immersive_aircraft.client.OverlayRenderer;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT)
public class NeoForgeOverlayRenderer {
    @SubscribeEvent()
    public static void renderOverlay(RenderGuiLayerEvent.Post event) {
        if (event.getName() == VanillaGuiLayers.HOTBAR && !Minecraft.getInstance().options.hideGui) {
            OverlayRenderer.renderOverlay(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaTicks());
        }
    }
}
