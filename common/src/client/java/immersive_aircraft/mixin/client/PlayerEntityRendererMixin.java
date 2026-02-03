package immersive_aircraft.mixin.client;

import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void setModelPose(Avatar player, AvatarRenderState state, float tickDelta, CallbackInfo ci) {
        if (player.getRootVehicle() instanceof VehicleEntity) {
            state.isCrouching = false;
        }
    }
}
