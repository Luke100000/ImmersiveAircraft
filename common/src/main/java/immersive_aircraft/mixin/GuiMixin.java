package immersive_aircraft.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.client.OverlayRenderer;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;F)V", at = @At("TAIL"))
    private void renderInject(PoseStack matrices, float tickDelta, CallbackInfo ci) {
        OverlayRenderer.renderOverlay(matrices, tickDelta);
    }
}
