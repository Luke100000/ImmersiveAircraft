package immersive_aircraft.mixin;

import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of the 1.16 {@code PlayerEntityRendererMixin}: the player model must not use the sneaking
 * pose while riding, since the sneak key doubles as the throttle. 1.16's {@code setModelPose} is
 * {@code setModelVisibilities} here.
 */
@Mixin(RenderPlayer.class)
public abstract class RenderPlayerMixin {
    @Inject(method = "setModelVisibilities", at = @At("TAIL"))
    private void immersiveAircraft$noSneakingModel(AbstractClientPlayer player, CallbackInfo ci) {
        if (player.getLowestRidingEntity() instanceof VehicleEntity) {
            ((RenderPlayer) (Object) this).getMainModel().isSneak = false;
        }
    }
}
