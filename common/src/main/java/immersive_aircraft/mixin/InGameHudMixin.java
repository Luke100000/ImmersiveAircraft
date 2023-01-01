package immersive_aircraft.mixin;

import immersive_aircraft.client.OverlayRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;F)V", at = @At("TAIL"))
    private void renderInject(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        OverlayRenderer.renderOverlay(matrices, tickDelta);
    }
}
