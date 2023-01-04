package immersive_aircraft.forge;

import immersive_aircraft.Main;
import immersive_aircraft.client.OverlayRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeOverlayRenderer extends InGameHud {
    public ForgeOverlayRenderer(MinecraftClient p_i46325_1_) {
        super(p_i46325_1_);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void renderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            OverlayRenderer.renderOverlay(event.getMatrixStack(), event.getPartialTicks());
        }
    }
}
