package immersive_aircraft.mixin.client;

import immersive_aircraft.client.OverlayRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"))
    private void ia$renderInject(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        OverlayRenderer.renderOverlay(guiGraphics, deltaTracker.getGameTimeDeltaTicks());
    }
}
