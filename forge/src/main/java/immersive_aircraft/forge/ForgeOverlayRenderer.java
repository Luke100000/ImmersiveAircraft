package immersive_aircraft.forge;

import immersive_aircraft.Main;
import immersive_aircraft.client.OverlayRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ForgeOverlayRenderer {
    @SubscribeEvent
    public static void registerGuiOverlays(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().addAbove(ForgeLayeredDraw.HOTBAR_AND_DECOS, Main.locate("ia_overlay"),
                (guiGraphics, deltaTracker) -> OverlayRenderer.renderOverlay(
                        guiGraphics,
                        deltaTracker.getGameTimeDeltaPartialTick(true),
                        0
                ));
    }
}
