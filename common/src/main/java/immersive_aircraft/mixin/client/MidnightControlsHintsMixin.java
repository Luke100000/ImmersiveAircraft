package immersive_aircraft.mixin.client;

import immersive_aircraft.client.compat.MidnightControlsInput;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "eu.midnightdust.midnightcontrols.client.gui.MidnightControlsHud", remap = false)
public class MidnightControlsHintsMixin {
    // MidnightControls 1.10 has fixed hint rows and no API to add flight controls.
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void immersiveAircraft$renderHints(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
        if (MidnightControlsInput.renderHints(graphics)) {
            ci.cancel();
        }
    }
}
