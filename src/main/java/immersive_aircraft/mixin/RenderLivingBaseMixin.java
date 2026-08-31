package immersive_aircraft.mixin;

import immersive_aircraft.entity.AircraftEntity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of the 1.16 {@code LivingEntityRendererMixin}: a passenger is rendered banked and pitched
 * with the aircraft instead of standing bolt upright. 1.16's {@code setupTransforms} is
 * {@code applyRotations} here, and the matrix stack is the GL matrix.
 */
@Mixin(RenderLivingBase.class)
public abstract class RenderLivingBaseMixin<T extends EntityLivingBase> {
    @Inject(method = "applyRotations", at = @At("TAIL"))
    private void immersiveAircraft$rotateWithAircraft(T entity, float ageInTicks, float rotationYaw, float partialTicks, CallbackInfo ci) {
        if (entity.getLowestRidingEntity() != entity && entity.getLowestRidingEntity() instanceof AircraftEntity) {
            AircraftEntity aircraft = (AircraftEntity) entity.getLowestRidingEntity();
            GlStateManager.rotate(-aircraft.getPitch(partialTicks), 1.0f, 0.0f, 0.0f);
            GlStateManager.rotate(-aircraft.getRoll(partialTicks), 0.0f, 0.0f, 1.0f);
        }
    }
}
