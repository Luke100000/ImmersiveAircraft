package immersive_aircraft.mixin.client;

import immersive_aircraft.Main;
import immersive_aircraft.client.OverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "renderVehicleHealth(Lnet/minecraft/client/gui/GuiGraphics;)V", at = @At("HEAD"))
    private void ic_air$renderVehicleHealth(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (Main.MOD_LOADER.equals("fabric")) {
            OverlayRenderer.renderOverlay(guiGraphics, Minecraft.getInstance().getFrameTime(), 49);
        }
    }
}
