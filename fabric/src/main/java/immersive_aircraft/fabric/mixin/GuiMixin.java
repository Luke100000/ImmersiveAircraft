package immersive_aircraft.fabric.mixin;

import immersive_aircraft.client.OverlayRenderer;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class GuiMixin {
    @Inject(method = "extractVehicleHealth", at = @At("RETURN"))
    private void immersiveAircraft$renderVehicleHealth(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        OverlayRenderer.renderVehicleHealth(graphics, 49);
    }
}
