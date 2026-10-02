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
    @Inject(method = "renderItemHotbar", at = @At("RETURN"))
    private void immersiveAircraft$renderEngineGauge(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
        OverlayRenderer.renderEngineGauge(graphics, delta.getGameTimeDeltaTicks());
    }
}
