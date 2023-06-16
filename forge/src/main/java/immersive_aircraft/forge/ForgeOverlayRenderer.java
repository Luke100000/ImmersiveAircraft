package immersive_aircraft.forge;

import immersive_aircraft.Main;
import immersive_aircraft.client.OverlayRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeOverlayRenderer extends InGameHud {
    public ForgeOverlayRenderer(MinecraftClient client, ItemRenderer itemRenderer) {
        super(client, itemRenderer);
    }

    private final static Identifier NamedGuiIdentifier = new Identifier("minecraft:hotbar");

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void renderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay().id().equals(NamedGuiIdentifier)) {
            OverlayRenderer.renderOverlay(event.getGuiGraphics(), event.getPartialTick());
        }
    }
}
