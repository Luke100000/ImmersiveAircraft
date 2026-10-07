package immersive_aircraft.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import immersive_aircraft.Main;
import immersive_aircraft.client.MouseFlight;
import immersive_aircraft.client.OverlayRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class GuiMixin {
    @Inject(method = "extractVehicleHealth(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V", at = @At("HEAD"))
    private void ic_air$renderVehicleHealth(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        if (Main.MOD_LOADER.equals("fabric")) {
            OverlayRenderer.renderOverlay(guiGraphics, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false), 49);
        }
    }

    @ModifyExpressionValue(method = "extractCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private boolean immersiveAircraft$mouseFlightCrosshair(boolean firstPerson) {
        return firstPerson || MouseFlight.isEnabled();
    }

    @Inject(method = "extractCrosshair", at = @At("HEAD"))
    private void immersiveAircraft$moveCrosshair(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        if (MouseFlight.isEnabled()) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(0, graphics.guiHeight() * (MouseFlight.CROSSHAIR_HEIGHT - 0.5f));
        }
    }

    @Inject(method = "extractCrosshair", at = @At("RETURN"))
    private void immersiveAircraft$restoreCrosshair(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        if (MouseFlight.isEnabled()) {
            graphics.pose().popMatrix();
        }
    }

    @Inject(method = "extractItemHotbar", at = @At("RETURN"))
    private void immersiveAircraft$renderEngineGauge(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        OverlayRenderer.renderEngineGauge(graphics, delta.getGameTimeDeltaPartialTick(false));
    }
}
