package immersive_aircraft.mixin;

import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Port of the 1.16 {@code ClientPlayerEntityMixin#isInSneakingPose}. The mod uses the sneak key
 * as "throttle down", so a pilot holding it must not crouch: crouching lowers the eye height and
 * shrinks the hit box mid-flight.
 */
@Mixin(EntityPlayerSP.class)
public abstract class EntityPlayerSPMixin {
    @Inject(method = "isSneaking", at = @At("HEAD"), cancellable = true)
    private void immersiveAircraft$neverSneakWhileFlying(CallbackInfoReturnable<Boolean> cir) {
        if (((EntityPlayerSP) (Object) this).getLowestRidingEntity() instanceof VehicleEntity) {
            cir.setReturnValue(false);
        }
    }
}
