package immersive_aircraft.forge;

import immersive_aircraft.Main;
import immersive_aircraft.client.OverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeOverlayRenderer extends Gui {
    public ForgeOverlayRenderer(Minecraft client, ItemRenderer itemRenderer) {
        super(client, itemRenderer);
    }

    private final static ResourceLocation NamedGuiIdentifier = new ResourceLocation("minecraft:hotbar");

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void renderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay().id().equals(NamedGuiIdentifier)) {
            OverlayRenderer.renderOverlay(event.getPoseStack(), event.getPartialTick());
        }
    }
}
