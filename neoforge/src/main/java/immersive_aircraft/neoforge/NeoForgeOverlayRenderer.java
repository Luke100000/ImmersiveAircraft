package immersive_aircraft.neoforge;

import immersive_aircraft.Main;
import immersive_aircraft.client.OverlayRenderer;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class NeoForgeOverlayRenderer {
    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.AIR_LEVEL, Main.locate("ia_overlay"),
                (graphics, delta) -> {
                    Minecraft client = Minecraft.getInstance();
                    if (client.gui.hud.isHidden() || client.gameMode == null || client.player == null) return;

                    int barHeightOffset = client.gui.hud.rightHeight;
                    OverlayRenderer.renderOverlay(graphics, delta.getGameTimeDeltaPartialTick(true), barHeightOffset);

                    if (client.player.getRootVehicle() instanceof VehicleEntity) {
                        OverlayRenderer.renderVehicleHealth(graphics, barHeightOffset);
                        client.gui.hud.rightHeight += 10 * (1 + Math.max(0, Config.getInstance().healthBarRow));
                    }
                });
    }
}
