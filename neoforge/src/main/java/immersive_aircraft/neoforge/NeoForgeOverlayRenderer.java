package immersive_aircraft.neoforge;

import immersive_aircraft.Main;
import immersive_aircraft.client.OverlayRenderer;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class NeoForgeOverlayRenderer {
    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.AIR_LEVEL, Main.locate("ia_overlay"),
                (graphics, delta) -> {
                    Minecraft client = Minecraft.getInstance();
                    if (client.options.hideGui || client.gameMode == null || client.player == null) return;

                    int barHeightOffset = client.gui.rightHeight;
                    OverlayRenderer.renderOverlay(graphics, delta.getGameTimeDeltaTicks(), barHeightOffset);

                    if (client.player.getRootVehicle() instanceof VehicleEntity) {
                        OverlayRenderer.renderVehicleHealth(graphics, barHeightOffset);
                        client.gui.rightHeight += 10 * (1 + Math.max(0, Config.getInstance().healthBarRow));
                    }
                });
    }
}
