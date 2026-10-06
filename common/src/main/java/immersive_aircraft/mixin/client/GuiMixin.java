package immersive_aircraft.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import immersive_aircraft.client.MouseFlight;
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
    @ModifyExpressionValue(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private boolean immersiveAircraft$mouseFlightCrosshair(boolean firstPerson) {
        return firstPerson || MouseFlight.isPiloting();
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"))
    private void immersiveAircraft$moveCrosshair(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
        if (MouseFlight.isPiloting()) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, graphics.guiHeight() * (MouseFlight.CROSSHAIR_HEIGHT - 0.5f), 0);
        }
    }

    @Inject(method = "renderCrosshair", at = @At("RETURN"))
    private void immersiveAircraft$restoreCrosshair(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
        if (MouseFlight.isPiloting()) {
            graphics.pose().popPose();
        }
    }

    @Inject(method = "renderItemHotbar", at = @At("RETURN"))
    private void immersiveAircraft$renderEngineGauge(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
        OverlayRenderer.renderEngineGauge(graphics, delta.getGameTimeDeltaTicks());
    }
}
