package immersive_aircraft.neoforge;

import immersive_aircraft.Main;
import immersive_aircraft.client.OverlayRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT)
public class NeoForgeOverlayRenderer {
    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, Main.locate("ia_overlay"),
                (graphics, delta) -> {
                    OverlayRenderer.renderOverlay(graphics, delta.getGameTimeDeltaTicks(), 49);
                    // TODO: Where is forgeGui.rightHeight += 10;?
                });
    }
}
