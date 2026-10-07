package immersive_aircraft.neoforge;

import immersive_aircraft.Main;
import immersive_aircraft.client.OverlayRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class NeoForgeOverlayRenderer {
    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, Main.locate("ia_overlay"),
                (graphics, delta) -> {
                    OverlayRenderer.renderOverlay(graphics, delta.getGameTimeDeltaPartialTick(true), 49);
                    OverlayRenderer.renderVehicleHealth(graphics, 49);
                });
    }
}
